package iuh.fit.www_phone_ver1.service.mail;

import iuh.fit.www_phone_ver1.entity.Order;
import java.nio.file.*;
import java.text.NumberFormat;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class MailService {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MailService.class);
  private final JavaMailSender sender;
  private final boolean enabled;
  private final String from;
  private final Path outbox;

  public MailService(
      JavaMailSender sender,
      @Value("${app.mail.enabled:false}") boolean enabled,
      @Value("${app.mail.from:noreply@ptvmobile.local}") String from,
      @Value("${app.mail.outbox:target/mail-outbox}") String outbox) {
    this.sender = sender;
    this.enabled = enabled;
    this.from = from;
    this.outbox = Path.of(outbox);
  }

  public void sendOrderConfirmation(String email, Order order) {
    var money = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    StringBuilder body =
        new StringBuilder(
            "<h2>Đã nhận đơn hàng #"
                + order.getId()
                + "</h2><p>Phương thức: "
                + escape(order.getPaymentMethod())
                + "</p><ul>");
    order
        .getOrderDetails()
        .forEach(
            d ->
                body.append("<li>")
                    .append(escape(d.getDisplayName()))
                    .append(" — ")
                    .append(escape(d.getVariantLabel()))
                    .append(" × ")
                    .append(d.getQuantity())
                    .append(": ")
                    .append(money.format(d.getSubTotal()))
                    .append("</li>"));
    body.append("</ul><p>Tiền sản phẩm: ")
        .append(money.format(order.getSubtotal()))
        .append("</p><p>Giảm giá: ")
        .append(money.format(order.getDiscountAmount()))
        .append("</p><p>Đổi ")
        .append(order.getPointsSpent())
        .append(" điểm: − ")
        .append(money.format(order.getPointsDiscount()))
        .append("</p><p>Phí giao hàng: ")
        .append(money.format(order.getShippingFee()))
        .append("</p><p>Tổng tiền: ")
        .append(money.format(order.getTotalAmount()))
        .append("</p><p>Giao đến: ")
        .append(escape(order.getFullShippingAddress()))
        .append("</p>");
    deliver(email, "PTV Mobile — Đã nhận đơn #" + order.getId(), body.toString());
  }

  public void sendAccountLink(String email, String link, boolean reset) {
    deliver(
        email,
        reset ? "PTV Mobile — Đặt lại mật khẩu" : "PTV Mobile — Xác minh email",
        "<h2>"
            + (reset ? "Đặt lại mật khẩu" : "Xác minh email")
            + "</h2><p><a href=\""
            + escape(link)
            + "\">"
            + (reset ? "Tạo mật khẩu mới" : "Xác minh tài khoản")
            + "</a></p><p>Liên kết chỉ dùng một lần, có hiệu lực "
            + (reset ? "15 phút" : "24 giờ")
            + ". Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>");
  }

  public void sendPasswordChanged(String email) {
    deliver(
        email,
        "PTV Mobile — Mật khẩu đã được thay đổi",
        "<p>Mật khẩu tài khoản đã được đặt lại. Các phiên đăng nhập cũ đã được đăng xuất.</p>");
  }

  public void sendPaymentSuccess(String email, Order order) {
    if (email == null || email.isBlank() || order == null) return;
    var money = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    String body =
        "<h2>Thanh toán thành công cho đơn hàng #"
            + order.getId()
            + "</h2><p>Phương thức: "
            + escape(order.getPaymentMethod())
            + "</p><p>Số tiền đã thanh toán: <strong>"
            + money.format(order.getTotalAmount())
            + "</strong></p><p>Đơn hàng đang được cửa hàng xác nhận và chuẩn bị giao đến: "
            + escape(order.getFullShippingAddress())
            + ".</p>";
    deliver(email, "PTV Mobile — Xác nhận thanh toán đơn #" + order.getId(), body);
  }

  public void sendOrderStatusUpdate(String email, Order order) {
    if (email == null || email.isBlank() || order == null) return;
    StringBuilder body =
        new StringBuilder(
            "<h2>Cập nhật đơn hàng #"
                + order.getId()
                + "</h2><p>Trạng thái mới: <strong>"
                + escape(order.getStatus())
                + "</strong></p>");
    if (order.getTrackingCode() != null && !order.getTrackingCode().isBlank()) {
      body.append("<p>Đơn vị vận chuyển: ")
          .append(escape(order.getCarrier()))
          .append(" · Mã vận đơn: <strong>")
          .append(escape(order.getTrackingCode()))
          .append("</strong></p>");
    }
    if (order.getCancellationReason() != null && !order.getCancellationReason().isBlank()) {
      body.append("<p>Lý do: ").append(escape(order.getCancellationReason())).append("</p>");
    }
    deliver(
        email,
        "PTV Mobile — Đơn #" + order.getId() + ": " + order.getStatus(),
        body.toString());
  }

  public void sendRefundUpdate(String email, Order order, iuh.fit.www_phone_ver1.entity.Refund refund) {
    if (email == null || email.isBlank() || order == null || refund == null) return;
    var money = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    String statusText =
        "COMPLETED".equals(refund.getStatus())
            ? "Đã xác nhận hoàn tiền"
            : "Đã từ chối yêu cầu hoàn tiền";
    String body =
        "<h2>Thông báo hoàn tiền đơn hàng #"
            + order.getId()
            + "</h2><p>Trạng thái: <strong>"
            + statusText
            + "</strong></p><p>Số tiền: "
            + money.format(refund.getAmount())
            + "</p><p>Tham chiếu / Ghi chú: "
            + escape(refund.getSettlementReference())
            + "</p>";
    deliver(email, "PTV Mobile — Hoàn tiền đơn #" + order.getId(), body);
  }

  private void deliver(String email, String subject, String body) {
    try {
      var message = sender.createMimeMessage();
      var helper = new MimeMessageHelper(message, false, "UTF-8");
      helper.setFrom(from);
      helper.setTo(email);
      helper.setSubject(subject);
      helper.setText(body, true);
      if (enabled) sender.send(message);
      else {
        Files.createDirectories(outbox);
        message.saveChanges();
        try (var output = Files.newOutputStream(outbox.resolve(UUID.randomUUID() + ".eml"))) {
          message.writeTo(output);
        }
      }
    } catch (Exception error) {
      log.warn("Không gửi/lưu được email: {}", error.getClass().getSimpleName());
    }
  }

  private String escape(String value) {
    return value == null ? "" : HtmlUtils.htmlEscape(value);
  }
}
