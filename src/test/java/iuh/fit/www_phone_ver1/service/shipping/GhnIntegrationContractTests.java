package iuh.fit.www_phone_ver1.service.shipping;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sun.net.httpserver.HttpServer;
import iuh.fit.www_phone_ver1.dto.order.*;
import iuh.fit.www_phone_ver1.entity.Order;
import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.service.order.*;
import java.math.BigDecimal;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

class GhnIntegrationContractTests {
  @Test
  void disabledClientCannotDispatchRequests() {
    var client = new GhnClient(false, false, "", 0);
    assertFalse(client.isAvailable());
    assertThrows(IllegalArgumentException.class, () -> client.create(Map.of()));
  }

  @Test
  void publicAddressNamesAreMatchedToCarrierCatalogAndPreviewNeverCreatesShipment()
      throws Exception {
    var seen = new ArrayList<String>();
    var payload = new AtomicReference<String>();
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/",
        exchange -> {
          String path = exchange.getRequestURI().toString();
          seen.add(path);
          assertEquals("test-secret", exchange.getRequestHeaders().getFirst("Token"));
          assertEquals("123", exchange.getRequestHeaders().getFirst("ShopId"));
          String body;
          if (path.contains("province/all"))
            body = "{\"code\":200,\"data\":[{\"_id\":1000002,\"name\":\"Hà Nội\",\"status\":1}]}";
          else if (path.contains("ward/all"))
            body =
                "{\"code\":200,\"data\":[{\"_id\":1000010,\"name\":\"Phường Ba"
                    + " Đình\",\"status\":1}]}";
          else {
            payload.set(
                new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            body =
                "{\"code\":200,\"data\":{\"order_code\":\"\",\"total_fee\":25000,\"expected_delivery_time\":\"2026-10-25T03:00:00Z\"}}";
          }
          byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          exchange.getResponseBody().write(bytes);
          exchange.close();
        });
    server.start();
    try {
      var client =
          new GhnClient(
              true,
              false,
              "test-secret",
              123,
              URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/"),
              HttpClient.newHttpClient());
      var dest = client.destination("Thành phố Hà Nội", "Phường Ba Đình");
      assertEquals("Hà Nội", dest.province());
      assertTrue(seen.get(1).contains("province_id=1000002"));
      client.preview(
          Map.of(
              "is_new_to_address",
              true,
              "to_province_name",
              dest.province(),
              "to_ward_name",
              dest.ward()));
      assertTrue(payload.get().contains("is_new_to_address"));
      assertTrue(seen.stream().noneMatch(p -> p.endsWith("/create")));
    } finally {
      server.stop(0);
    }
  }

  @Test
  void providerErrorsDoNotExposeTokenOrResponseBody() throws Exception {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/",
        e -> {
          byte[] b = "token test-secret, customer private-data".getBytes(StandardCharsets.UTF_8);
          e.sendResponseHeaders(401, b.length);
          e.getResponseBody().write(b);
          e.close();
        });
    server.start();
    try {
      var client =
          new GhnClient(
              true,
              false,
              "test-secret",
              123,
              URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/"),
              HttpClient.newHttpClient());
      var error = assertThrows(IllegalArgumentException.class, () -> client.create(Map.of()));
      assertFalse(error.getMessage().contains("test-secret"));
      assertFalse(error.getMessage().contains("private-data"));
    } finally {
      server.stop(0);
    }
  }

  private Order fixture(AddressDirectory locations) {
    var p = locations.provinces().get(0);
    var w = p.wards().get(0);
    return Order.builder()
        .id(42L)
        .paymentReference("unique-payment-reference")
        .paymentMethod("COD")
        .paymentStatus("UNPAID")
        .status(OrderStatuses.PENDING)
        .recipientName("Fixture")
        .phone("0901234567")
        .shippingAddress("12 Test Street")
        .provinceCode(p.code())
        .wardCode(w.code())
        .province(p.name())
        .ward(w.name())
        .totalAmount(BigDecimal.valueOf(1000000))
        .build();
  }

  @Test
  void repeatedCreateUsesOneShipmentAndRetryKeepsOriginalPayload() throws Exception {
    var directory = new AddressDirectory();
    var o = fixture(directory);
    var client = mock(GhnClient.class);
    var orders = mock(OrderRepository.class);
    when(orders.findForUpdate(42L)).thenReturn(Optional.of(o));
    when(client.isAvailable()).thenReturn(true);
    when(client.environment()).thenReturn("STAGING");
    when(client.destination(anyString(), anyString()))
        .thenReturn(new GhnClient.Destination("Hà Nội", "Phường Ba Đình"));
    when(client.create(anyMap()))
        .thenThrow(new IllegalArgumentException("Timeout"))
        .thenReturn(new JsonMapper().readTree("{\"order_code\":\"GHN42\"}"));
    var service =
        new GhnShippingService(client, orders, directory, mock(OrderHistoryService.class));
    var first = new ParcelForm();
    assertFalse(service.create(42L, first).confirmed());
    assertEquals("UNCONFIRMED", o.getCarrierStatus());
    var changed = new ParcelForm();
    changed.setWeight(900);
    assertTrue(service.create(42L, changed).confirmed());
    assertTrue(service.create(42L, changed).confirmed());
    var captor = ArgumentCaptor.forClass(Map.class);
    verify(client, times(2)).create(captor.capture());
    assertEquals(captor.getAllValues().get(0), captor.getAllValues().get(1));
    assertEquals(500, ((Number) captor.getValue().get("weight")).intValue());
    assertEquals("GHN42", o.getTrackingCode());
  }

  @Test
  void trackingMustMatchConfiguredShopAndOrder() throws Exception {
    var directory = new AddressDirectory();
    var o = fixture(directory);
    o.setCarrier("GHN");
    o.setTrackingCode("GHN42");
    o.setGhnEnvironment("STAGING");
    var client = mock(GhnClient.class);
    var orders = mock(OrderRepository.class);
    when(orders.findForUpdate(42L)).thenReturn(Optional.of(o));
    when(client.environment()).thenReturn("STAGING");
    when(client.detail("GHN42"))
        .thenReturn(
            new JsonMapper()
                .readTree("{\"order_code\":\"GHN42\",\"shop_id\":999,\"status\":\"delivered\"}"));
    when(client.ownsShop(999)).thenReturn(false);
    var service =
        new GhnShippingService(client, orders, directory, mock(OrderHistoryService.class));
    assertThrows(IllegalArgumentException.class, () -> service.sync(42L));
    assertNull(o.getCarrierStatus());
  }
}
