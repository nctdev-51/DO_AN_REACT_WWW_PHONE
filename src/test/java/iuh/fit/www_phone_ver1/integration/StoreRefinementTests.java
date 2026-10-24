package iuh.fit.www_phone_ver1.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import iuh.fit.www_phone_ver1.dto.cart.Cart;
import iuh.fit.www_phone_ver1.dto.catalog.CampaignBannerForm;
import iuh.fit.www_phone_ver1.dto.order.*;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.admin.SalesReportService;
import iuh.fit.www_phone_ver1.service.catalog.*;
import iuh.fit.www_phone_ver1.service.mail.MailService;
import iuh.fit.www_phone_ver1.service.order.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "app.payments.expiry-enabled=false")
@ActiveProfiles("demo")
@Transactional
class StoreRefinementTests {
  @Autowired SalesReportService sales;
  @Autowired OrderRepository orders;
  @Autowired RefundRepository refunds;
  @Autowired ProductRepository products;
  @Autowired ProductVariantRepository variants;
  @Autowired ReviewRepository reviews;
  @Autowired UserRepository users;
  @Autowired OrderService checkout;
  @Autowired AddressDirectory locations;
  @Autowired CampaignBannerService banners;
  @Autowired CampaignBannerRepository bannerRows;
  @Autowired WebApplicationContext context;
  User buyer;
  Product phone;
  ProductVariant base;
  MockMvc mvc;

  @BeforeEach
  void setup() {
    buyer =
        users.saveAndFlush(
            User.builder()
                .fullName("Refinement fixture")
                .email(UUID.randomUUID() + "@example.test")
                .password("unused")
                .status(1)
                .build());
    phone = product("REFINE Phone", 1000000);
    base = variant(phone, "Đen", "256GB", 1000000);
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  Product product(String name, long value) {
    return products.saveAndFlush(
        Product.builder()
            .name(name)
            .sku("REFINE-" + UUID.randomUUID())
            .brand("REFINE")
            .price(BigDecimal.valueOf(value))
            .build());
  }

  ProductVariant variant(Product p, String color, String storage, long amount) {
    var v = new ProductVariant();
    v.setProduct(p);
    v.setSku(UUID.randomUUID().toString());
    v.setColor(color);
    v.setStorage(storage);
    v.setRam("8GB");
    v.setPrice(BigDecimal.valueOf(amount));
    v.setStock(20);
    variants.saveAndFlush(v);
    p.getVariants().add(v);
    return v;
  }

  Order order(Product p, int qty, String payment) {
    var d =
        OrderDetail.builder()
            .product(p)
            .variant(p.getDefaultVariant())
            .quantity(qty)
            .price(p.getPrice())
            .build();
    var o =
        Order.builder()
            .user(buyer)
            .totalAmount(p.getPrice().multiply(BigDecimal.valueOf(qty)))
            .status(OrderStatuses.DELIVERED)
            .paymentStatus(payment)
            .deliveredAt(LocalDateTime.now())
            .build();
    d.setOrder(o);
    o.getOrderDetails().add(d);
    return orders.saveAndFlush(o);
  }

  void refund(Order o, long amount, String status) {
    var r = new Refund();
    r.setOrder(o);
    r.setAmount(BigDecimal.valueOf(amount));
    r.setReason("Refund fixture");
    r.setStatus(status);
    refunds.saveAndFlush(r);
  }

  @Test
  void reportsSubtractOnlyCompletedRefundsOfDeliveredOrders() {
    var before = sales.report(YearMonth.now());
    var o = order(phone, 1, "PARTIALLY_REFUNDED");
    o.setOrderDate(LocalDateTime.now().minusMonths(3));
    orders.saveAndFlush(o);
    refund(o, 200000, "COMPLETED");
    refund(o, 300000, "PENDING");
    refund(o, 100000, "REJECTED");
    var cancelled = order(phone, 1, "REFUNDED");
    cancelled.setStatus(OrderStatuses.CANCELLED);
    orders.saveAndFlush(cancelled);
    refund(cancelled, 1000000, "COMPLETED");
    var after = sales.report(YearMonth.now());
    assertEquals(0, after.net().subtract(before.net()).compareTo(BigDecimal.valueOf(800000)));
    assertEquals(
        0,
        after
            .values()
            .get(5)
            .subtract(before.values().get(5))
            .compareTo(BigDecimal.valueOf(800000)));
    assertEquals(
        0, after.refunded().subtract(before.refunded()).compareTo(BigDecimal.valueOf(200000)));
  }

  @Test
  void fullyRefundedDeliveredOrderContributesZero() {
    var before = sales.report(YearMonth.now());
    var o = order(phone, 1, "REFUNDED");
    refund(o, 1000000, "COMPLETED");
    assertEquals(0, sales.report(YearMonth.now()).net().compareTo(before.net()));
  }

  @Test
  void legacyDeliveryUsesOrderDateForChart() {
    var before = sales.report(YearMonth.now());
    var o = order(phone, 1, "PAID");
    o.setDeliveredAt(null);
    o.setOrderDate(LocalDateTime.now());
    orders.saveAndFlush(o);
    assertEquals(
        0,
        sales
            .report(YearMonth.now())
            .values()
            .get(5)
            .subtract(before.values().get(5))
            .compareTo(BigDecimal.valueOf(1000000)));
  }

  @Test
  void confirmationShowsPointsAndFullAddressAndEscapesInput(@TempDir Path out) throws Exception {
    var o =
        Order.builder()
            .id(42L)
            .totalAmount(BigDecimal.valueOf(990000))
            .subtotal(BigDecimal.valueOf(1000000))
            .pointsSpent(10)
            .pointsDiscount(BigDecimal.valueOf(10000))
            .shippingAddress("12 <script>")
            .ward("Phường thử")
            .province("Thành phố thử")
            .build();
    new MailService(
            new org.springframework.mail.javamail.JavaMailSenderImpl(),
            false,
            "test@example.test",
            out.toString())
        .sendOrderConfirmation("buyer@example.test", o);
    try (var files = Files.list(out)) {
      var file = files.findFirst().orElseThrow();
      try (var in = Files.newInputStream(file)) {
        var message =
            new jakarta.mail.internet.MimeMessage(
                jakarta.mail.Session.getInstance(new Properties()), in);
        String body = (String) message.getContent();
        assertTrue(body.contains("Đổi 10 điểm"));
        assertTrue(body.contains("10.000"));
        assertTrue(body.contains("Phường thử"));
        assertTrue(body.contains("&lt;script&gt;"));
        assertFalse(body.contains("12 <script>"));
      }
    }
  }

  @Test
  void colorMustMatchSameVariantAsStorageAndPrice() {
    variant(phone, "Xanh", "512GB", 3000000);
    var f =
        new CatalogFilter(
            "REFINE",
            null,
            null,
            null,
            BigDecimal.valueOf(1500000),
            "512GB",
            null,
            true,
            "PHONE",
            "Xanh",
            null);
    assertFalse(
        products.findAll(f.specification("newest")).stream()
            .anyMatch(p -> p.getId().equals(phone.getId())));
    var correct =
        new CatalogFilter(
            "REFINE", null, null, null, null, "512GB", null, true, "PHONE", "Xanh", null);
    assertEquals("Xanh", correct.selected(phone).getColor());
  }

  @Test
  void purposeFilteringUsesExplicitTags() {
    phone.setUsageTags("EVERYDAY,CAMERA");
    products.saveAndFlush(phone);
    var f =
        new CatalogFilter(
            "REFINE", null, null, null, null, null, null, true, "PHONE", null, "CAMERA");
    assertTrue(
        products.findAll(f.specification("newest")).stream()
            .anyMatch(p -> p.getId().equals(phone.getId())));
    var gaming =
        new CatalogFilter(
            "REFINE", null, null, null, null, null, null, true, "PHONE", null, "GAMING");
    assertFalse(
        products.findAll(gaming.specification("newest")).stream()
            .anyMatch(p -> p.getId().equals(phone.getId())));
  }

  @Test
  void bestSellingIgnoresCancelledAndFullyRefundedOrders() {
    var other = product("REFINE Other", 1000000);
    variant(other, "Đen", "256GB", 1000000);
    order(phone, 1, "PAID");
    order(other, 3, "PAID");
    order(phone, 10, "REFUNDED");
    var cancelled = order(phone, 20, "PAID");
    cancelled.setStatus(OrderStatuses.CANCELLED);
    orders.saveAndFlush(cancelled);
    var f = new CatalogFilter("REFINE", null, null, null, null, null, null, false);
    assertEquals(other.getId(), products.findAll(f.specification("bestselling")).get(0).getId());
  }

  @Test
  void ratingSortUsesActualReviews() {
    var other = product("REFINE Other", 1000000);
    variant(other, "Đen", "256GB", 1000000);
    for (var pair : List.of(Map.entry(phone, 3), Map.entry(other, 5))) {
      var r = new Review();
      r.setUser(buyer);
      r.setProduct(pair.getKey());
      r.setRating(pair.getValue());
      r.setContent("Review fixture");
      reviews.saveAndFlush(r);
    }
    var f = new CatalogFilter("REFINE", null, null, null, null, null, null, false);
    assertEquals(other.getId(), products.findAll(f.specification("rating")).get(0).getId());
  }

  @Test
  void directoryRejectsWardOfAnotherProvince() {
    var p = locations.provinces().get(0);
    var other = locations.provinces().get(1);
    assertThrows(
        IllegalArgumentException.class,
        () -> locations.resolve(p.code(), other.wards().get(0).code(), null, null, true));
    var resolved = locations.resolve(p.code(), p.wards().get(0).code(), "spoof", "spoof", true);
    assertEquals(p.name(), resolved.province());
    assertEquals(p.wards().get(0).name(), resolved.ward());
  }

  @Test
  void structuredCheckoutKeepsCanonicalAddressSnapshot() {
    var p = locations.provinces().get(0);
    var w = p.wards().get(0);
    var form = new CheckoutForm();
    form.setAddressMode("structured");
    form.setProvinceCode(p.code());
    form.setWardCode(w.code());
    form.setProvince("spoof");
    form.setWard("spoof");
    form.setRecipientName("Fixture");
    form.setPhone("0901234567");
    form.setShippingAddress("12 Test Street");
    var cart = new Cart();
    cart.addItem(base, 1);
    var o = checkout.placeOrder(cart, buyer.getEmail(), form);
    assertEquals(p.name(), o.getProvince());
    assertEquals(w.code(), o.getWardCode());
    assertTrue(o.getFullShippingAddress().contains(w.name()));
  }

  @Test
  void addressFallbackWorksWithoutJavaScript() {
    var p = locations.provinces().get(0);
    var w = p.wards().get(0);
    assertEquals(w.code(), locations.resolve(null, null, p.name(), w.name(), true).wardCode());
    assertThrows(
        IllegalArgumentException.class, () -> locations.resolve(null, null, null, null, true));
  }

  CampaignBannerForm banner() {
    var f = new CampaignBannerForm();
    f.setTitle("Fixture campaign");
    f.setProductId(phone.getId());
    f.setStartsAt(LocalDateTime.now().minusDays(1));
    f.setEndsAt(LocalDateTime.now().plusDays(1));
    return f;
  }

  @Test
  void bannerScheduleIsStartInclusiveEndExclusiveAndHidesStoppedProducts() throws Exception {
    var now = LocalDateTime.now();
    var f = banner();
    f.setStartsAt(now);
    f.setEndsAt(now.plusHours(1));
    banners.save(null, f, null);
    var b = bannerRows.findAll().get(0);
    assertTrue(bannerRows.liveAt(now).stream().anyMatch(v -> v.getId().equals(b.getId())));
    assertFalse(
        bannerRows.liveAt(now.plusHours(1)).stream().anyMatch(v -> v.getId().equals(b.getId())));
    phone.setStatus(0);
    products.saveAndFlush(phone);
    assertFalse(bannerRows.liveAt(now).stream().anyMatch(v -> v.getId().equals(b.getId())));
  }

  @Test
  void bannerRejectsInvalidScheduleAndStaleUpdate() throws Exception {
    var invalid = banner();
    invalid.setEndsAt(invalid.getStartsAt());
    assertThrows(IllegalArgumentException.class, () -> banners.save(null, invalid, null));
    var f = banner();
    banners.save(null, f, null);
    var b = bannerRows.findAll().get(0);
    var stale = banner();
    stale.setVersion(b.getVersion() + 1);
    assertThrows(IllegalArgumentException.class, () -> banners.save(b.getId(), stale, null));
  }

  @Test
  void bannerEndpointsAreProtected() throws Exception {
    mvc.perform(get("/admin/banners").with(user(buyer.getEmail()).roles("USER")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/locations/wards").param("provinceCode", "bad"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
  }
}
