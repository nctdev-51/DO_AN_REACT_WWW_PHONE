package iuh.fit.www_phone_ver1.service;

import iuh.fit.www_phone_ver1.entity.Order;
import iuh.fit.www_phone_ver1.entity.OrderDetail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderConfirmation(String toEmail, Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đặt hàng thành công - Mã đơn #" + order.getId());

            NumberFormat currencyVN = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedTotal = currencyVN.format(order.getTotalAmount());

            StringBuilder itemsHtml = new StringBuilder();
            for (OrderDetail detail : order.getOrderDetails()) {
                String sub = currencyVN.format(detail.getPrice() * detail.getQuantity());
                itemsHtml.append("<tr>")
                        .append("<td style='border:1px solid #ddd;padding:8px;'>").append(detail.getProduct().getName()).append("</td>")
                        .append("<td style='border:1px solid #ddd;padding:8px;text-align:center;'>").append(detail.getQuantity()).append("</td>")
                        .append("<td style='border:1px solid #ddd;padding:8px;text-align:right;'>").append(currencyVN.format(detail.getPrice())).append("</td>")
                        .append("<td style='border:1px solid #ddd;padding:8px;text-align:right;'>").append(sub).append("</td>")
                        .append("</tr>");
            }

            String content = "<h2>Cảm ơn bạn đã đặt hàng tại Cửa hàng Điện Thoại!</h2>"
                    + "<p>Mã đơn hàng: <b>#" + order.getId() + "</b></p>"
                    + "<p>Trạng thái: <b>" + order.getStatus() + "</b></p>"
                    + "<p>Người nhận: <b>" + (order.getUser() != null ? order.getUser().getFullName() : "Khách hàng") + "</b></p>"
                    + "<p>Số điện thoại: <b>" + order.getPhone() + "</b></p>"
                    + "<p>Địa chỉ nhận: <b>" + order.getShippingAddress() + "</b></p>"
                    + "<h3>Chi tiết sản phẩm:</h3>"
                    + "<table style='border-collapse:collapse;width:100%;'>"
                    + "<thead><tr style='background-color:#f2f2f2;'>"
                    + "<th style='border:1px solid #ddd;padding:8px;'>Tên máy</th>"
                    + "<th style='border:1px solid #ddd;padding:8px;'>Số lượng</th>"
                    + "<th style='border:1px solid #ddd;padding:8px;'>Đơn giá</th>"
                    + "<th style='border:1px solid #ddd;padding:8px;'>Thành tiền</th>"
                    + "</tr></thead>"
                    + "<tbody>" + itemsHtml + "</tbody>"
                    + "</table>"
                    + "<h3 style='color:red;'>Tổng thanh toán: " + formattedTotal + "</h3>"
                    + "<p>Chúng tôi sẽ liên hệ sớm nhất để xác nhận và giao hàng.</p>";

            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}