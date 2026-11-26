package iuh.fit.www_phone_ver1.service.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import iuh.fit.www_phone_ver1.entity.Order;
import iuh.fit.www_phone_ver1.entity.OrderDetail;
import iuh.fit.www_phone_ver1.entity.User;
import iuh.fit.www_phone_ver1.repository.OrderRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InvoicePdfServiceTest {

  @Test
  void renderPdfProducesValidPdfBytesWithVietnameseOrder() {
    OrderRepository repo = mock(OrderRepository.class);
    InvoicePdfService service = new InvoicePdfService(repo);

    User user = User.builder().fullName("Nguyễn Văn A").email("a@example.com").phone("0901234567").build();
    OrderDetail detail =
        OrderDetail.builder()
            .productName("iPhone 16 Pro Max")
            .variantLabel("Titan Tự Nhiên / 256GB")
            .sku("IP16PM-256")
            .quantity(2)
            .price(new BigDecimal("34990000"))
            .build();
    Order order =
        Order.builder()
            .id(101L)
            .user(user)
            .recipientName("Nguyễn Văn A")
            .phone("0901234567")
            .shippingAddress("12 Nguyễn Văn Bảo")
            .ward("Phường 4")
            .province("TP.HCM")
            .orderDate(LocalDateTime.of(2026, 11, 25, 16, 30))
            .status(OrderStatuses.PENDING)
            .paymentMethod("COD")
            .paymentStatus("UNPAID")
            .subtotal(new BigDecimal("69980000"))
            .discountAmount(new BigDecimal("500000"))
            .pointsSpent(100)
            .pointsDiscount(new BigDecimal("100000"))
            .shippingFee(new BigDecimal("30000"))
            .totalAmount(new BigDecimal("69410000"))
            .orderDetails(List.of(detail))
            .build();

    byte[] pdf = service.renderPdf(order);
    assertNotNull(pdf);
    assertTrue(pdf.length > 1000);
    String header = new String(pdf, 0, 5, StandardCharsets.US_ASCII);
    assertEquals("%PDF-", header);
  }

  @Test
  void renderPdfHandlesNullFieldsSafely() {
    OrderRepository repo = mock(OrderRepository.class);
    InvoicePdfService service = new InvoicePdfService(repo);
    Order sparseOrder = Order.builder().id(202L).status(OrderStatuses.DELIVERED).paymentStatus("PAID").build();

    byte[] pdf = service.renderPdf(sparseOrder);
    assertNotNull(pdf);
    assertEquals("%PDF-", new String(pdf, 0, 5, StandardCharsets.US_ASCII));
  }

  @Test
  void generateCustomerInvoiceRejectsCancelledOrder() {
    OrderRepository repo = mock(OrderRepository.class);
    Order cancelled = Order.builder().id(303L).status(OrderStatuses.CANCELLED).build();
    when(repo.findByIdAndUser_Email(303L, "a@example.com")).thenReturn(Optional.of(cancelled));

    InvoicePdfService service = new InvoicePdfService(repo);
    assertThrows(
        IllegalStateException.class,
        () -> service.generateCustomerInvoice(303L, "a@example.com"));
  }
}
