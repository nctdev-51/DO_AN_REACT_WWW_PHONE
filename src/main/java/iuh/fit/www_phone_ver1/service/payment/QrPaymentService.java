package iuh.fit.www_phone_ver1.service.payment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import iuh.fit.www_phone_ver1.entity.Order;
import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.service.mail.MailService;
import iuh.fit.www_phone_ver1.service.order.OrderHistoryService;
import iuh.fit.www_phone_ver1.service.order.OrderService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class QrPaymentService {
  private final OrderRepository orders;
  private final OrderService orderService;
  private final OrderHistoryService history;
  private final MailService mail;

  public QrPaymentService(OrderRepository orders, OrderService orderService,
      OrderHistoryService history, MailService mail) {
    this.orders = orders;
    this.orderService = orderService;
    this.history = history;
    this.mail = mail;
  }

  public String generateQrDataUrl(String baseUrl, Order order, QrPlatform platform) {
    try {
      String payload =
          baseUrl + "/payments/qr/scan?ref="
              + java.net.URLEncoder.encode(order.getPaymentReference(), StandardCharsets.UTF_8)
              + "&token="
              + java.net.URLEncoder.encode(order.getQrToken(), StandardCharsets.UTF_8)
              + "&platform=" + platform.name();
      BitMatrix matrix =
          new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 320, 320);
      BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      ImageIO.write(image, "PNG", output);
      return "data:image/png;base64,"
          + Base64.getEncoder().encodeToString(output.toByteArray());
    } catch (Exception error) {
      throw new IllegalStateException("Không thể tạo mã QR thanh toán.", error);
    }
  }

  @Transactional
  public Map<String, Object> confirmSimulatedPayment(
      Long orderId, String reference, String token, QrPlatform platform) {
    Order order =
        orders.findForUpdate(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
    if (reference == null || !Objects.equals(reference, order.getPaymentReference()))
      throw new IllegalArgumentException("Mã tham chiếu thanh toán không hợp lệ.");
    if (token == null || !Objects.equals(token, order.getQrToken()))
      throw new IllegalArgumentException("Mã token thanh toán không hợp lệ.");
    if (platform != null && order.getPaymentChannel() != null
        && !platform.name().equals(order.getPaymentChannel()))
      throw new IllegalArgumentException("Nền tảng thanh toán không khớp.");
    if (order.getPaymentExpiresAt() == null
        || LocalDateTime.now().isAfter(order.getPaymentExpiresAt())) {
      orderService.cancelLocked(order, "Quá thời hạn 5 phút thanh toán QR");
      return Map.of("success", false, "expired", true,
          "message", "Đơn hàng đã hết hạn thanh toán.");
    }
    if ("CANCELLED".equals(order.getStatus()))
      throw new IllegalArgumentException("Đơn hàng đã bị hủy.");
    if ("PAID".equals(order.getPaymentStatus()))
      return Map.of("success", true, "expired", false, "message", "Đơn đã được thanh toán.");

    order.setPaymentStatus("PAID");
    order.setGatewayTransaction("SIM-" + System.currentTimeMillis());
    history.append(order, "Thanh toán QR",
        "Giả lập quét mã " + platform.getDisplayName() + " thành công.");
    orders.save(order);

    String recipient = order.getUser() == null ? null : order.getUser().getEmail();
    if (recipient != null && !recipient.isBlank()) {
      Runnable notify = () -> mail.sendPaymentSuccess(recipient, order);
      if (TransactionSynchronizationManager.isSynchronizationActive())
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
              @Override
              public void afterCommit() {
                notify.run();
              }
            });
      else notify.run();
    }
    return Map.of("success", true, "expired", false,
        "message", "Thanh toán thành công.");
  }

  public Map<String, Object> status(Order order) {
    long remaining = 0;
    if (order.getPaymentExpiresAt() != null)
      remaining = java.time.Duration.between(
          LocalDateTime.now(), order.getPaymentExpiresAt()).getSeconds();
    remaining = Math.max(0, remaining);
    return Map.of(
        "paymentStatus", Objects.toString(order.getPaymentStatus(), "UNPAID"),
        "orderStatus", Objects.toString(order.getStatus(), ""),
        "remainingSeconds", remaining);
  }

  public String formatAccountInfo(Order order, QrPlatform platform) {
    return String.format(Locale.ROOT, "%s | %s | %s | PTVMOBILE #%d",
        platform.getAccountNumber(), platform.getAccountOwner(),
        platform.getContentPrefix(), order.getId());
  }
}
