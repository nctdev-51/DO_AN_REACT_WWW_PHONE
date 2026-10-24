package iuh.fit.www_phone_ver1.service.payment;

import iuh.fit.www_phone_ver1.entity.Order;
import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.service.order.OrderHistoryService;
import iuh.fit.www_phone_ver1.service.order.OrderService;
import iuh.fit.www_phone_ver1.service.order.OrderStatuses;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VnPayService {
  private final String terminal;
  private final String secret;
  private final String paymentUrl;
  private final String returnUrl;
  private final boolean enabled;
  private final OrderRepository orders;
  private final OrderService orderService;
  private final OrderHistoryService history;

  public VnPayService(
      @Value("${app.vnpay.terminal:}") String terminal,
      @Value("${app.vnpay.secret:}") String secret,
      @Value("${app.vnpay.payment-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
          String paymentUrl,
      @Value("${app.vnpay.return-url:http://localhost:8080/payments/vnpay/return}")
          String returnUrl,
      @Value("${app.vnpay.enabled:false}") boolean enabled,
      OrderRepository orders,
      OrderService orderService,
      OrderHistoryService history) {
    this.terminal = terminal;
    this.secret = secret;
    this.paymentUrl = paymentUrl;
    this.returnUrl = returnUrl;
    this.enabled = enabled;
    this.orders = orders;
    this.orderService = orderService;
    this.history = history;
  }

  public boolean isAvailable() {
    return enabled && !terminal.isBlank() && !secret.isBlank();
  }

  public String createUrl(Order order, String ip) {
    if (!isAvailable())
      throw new IllegalArgumentException("Thanh toán VNPay hiện chưa sẵn sàng. Vui lòng chọn COD.");
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    Map<String, String> values = new TreeMap<>();
    values.put("vnp_Version", "2.1.0");
    values.put("vnp_Command", "pay");
    values.put("vnp_TmnCode", terminal);
    values.put(
        "vnp_Amount",
        order.getTotalAmount().multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString());
    values.put("vnp_CurrCode", "VND");
    values.put("vnp_TxnRef", order.getPaymentReference());
    values.put("vnp_OrderInfo", "Payment for PTV Mobile order " + order.getId());
    values.put("vnp_OrderType", "other");
    values.put("vnp_Locale", "vn");
    values.put("vnp_ReturnUrl", returnUrl);
    values.put("vnp_IpAddr", ip);
    values.put("vnp_CreateDate", now.format(format));
    values.put("vnp_ExpireDate", now.plusMinutes(15).format(format));
    String query = canonical(values);
    return paymentUrl + "?" + query + "&vnp_SecureHash=" + sign(query);
  }

  public boolean verify(Map<String, String> values) {
    if (!isAvailable()) return false;
    String supplied = values.getOrDefault("vnp_SecureHash", "");
    return MessageDigest.isEqual(
        sign(canonical(values)).getBytes(StandardCharsets.US_ASCII),
        supplied.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
  }

  @Transactional
  public Map<String, String> handleNotification(Map<String, String> values) {
    if (!verify(values) || !terminal.equals(values.get("vnp_TmnCode")))
      return result("97", "Invalid signature");
    Order order = orders.lockByPaymentReference(values.get("vnp_TxnRef")).orElse(null);
    if (order == null || !"VNPAY".equals(order.getPaymentMethod()))
      return result("01", "Order not found");
    try {
      if (order
              .getTotalAmount()
              .multiply(BigDecimal.valueOf(100))
              .compareTo(new BigDecimal(values.getOrDefault("vnp_Amount", "-1")))
          != 0) return result("04", "Invalid amount");
    } catch (NumberFormatException error) {
      return result("04", "Invalid amount");
    }
    if (!"UNPAID".equals(order.getPaymentStatus())) return result("02", "Order already confirmed");
    boolean paid =
        "00".equals(values.get("vnp_ResponseCode"))
            && "00".equals(values.get("vnp_TransactionStatus"));
    order.setGatewayTransaction(values.get("vnp_TransactionNo"));
    if (paid)
      order.setPaymentStatus(
          OrderStatuses.CANCELLED.equals(order.getStatus()) ? "REFUND_REQUIRED" : "PAID");
    else {
      order.setPaymentStatus("FAILED");
      if (OrderStatuses.PENDING.equals(order.getStatus()))
        orderService.cancelLocked(order, "Thanh toán VNPay không thành công");
    }
    history.append(
        order,
        "VNPay",
        paid
            ? "Đã nhận xác nhận thanh toán từ VNPay."
            : "VNPay thông báo thanh toán không thành công.");
    return result("00", "Confirm success");
  }

  String canonical(Map<String, String> input) {
    return new TreeMap<>(input)
        .entrySet().stream()
            .filter(
                e ->
                    e.getKey().startsWith("vnp_")
                        && !Set.of("vnp_SecureHash", "vnp_SecureHashType").contains(e.getKey())
                        && e.getValue() != null
                        && !e.getValue().isEmpty())
            .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
            .collect(Collectors.joining("&"));
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private String sign(String data) {
    try {
      Mac mac = Mac.getInstance("HmacSHA512");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
      return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    } catch (java.security.GeneralSecurityException error) {
      throw new IllegalStateException("Cannot sign payment", error);
    }
  }

  private Map<String, String> result(String code, String message) {
    return Map.of("RspCode", code, "Message", message);
  }
}
