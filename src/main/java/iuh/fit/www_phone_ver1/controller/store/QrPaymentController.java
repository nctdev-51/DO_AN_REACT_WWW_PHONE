package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.entity.Order;
import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.service.payment.QrPaymentService;
import iuh.fit.www_phone_ver1.service.payment.QrPlatform;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/payments/qr")
public class QrPaymentController {
  private final OrderRepository orders;
  private final QrPaymentService qrPayments;

  public QrPaymentController(OrderRepository orders, QrPaymentService qrPayments) {
    this.orders = orders;
    this.qrPayments = qrPayments;
  }

  @GetMapping("/{orderId}")
  public String qrPage(@PathVariable Long orderId,
      @RequestParam(defaultValue = "VIETQR") String platform,
      Principal principal, HttpServletRequest request, Model model) {
    Order order = ownedOrder(orderId, principal);
    if (!"QR".equals(order.getPaymentMethod()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đơn không dùng thanh toán QR.");
    QrPlatform selected = QrPlatform.from(platform);
    order.setPaymentChannel(selected.name());
    orders.save(order);
    model.addAttribute("order", order);
    model.addAttribute("platform", selected);
    model.addAttribute("platforms", QrPlatform.values());
    model.addAttribute("qrImage", qrPayments.generateQrDataUrl(baseUrl(request), order, selected));
    model.addAttribute("accountInfo", qrPayments.formatAccountInfo(order, selected));
    model.addAttribute("paymentExpired",
        order.getPaymentExpiresAt() == null
            || java.time.LocalDateTime.now().isAfter(order.getPaymentExpiresAt()));
    return "store/checkout/qr-payment";
  }

  @GetMapping(value = "/scan", produces = "text/html")
  public String scanPage(@RequestParam String ref,
      @RequestParam String token,
      @RequestParam(defaultValue = "VIETQR") String platform,
      Model model) {
    Order order = orders.findByPaymentReference(ref)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!token.equals(order.getQrToken()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mã QR không hợp lệ.");
    model.addAttribute("order", order);
    model.addAttribute("platform", QrPlatform.from(platform));
    model.addAttribute("paymentExpired",
        order.getPaymentExpiresAt() == null
            || java.time.LocalDateTime.now().isAfter(order.getPaymentExpiresAt()));
    return "store/checkout/qr-scan-mobile";
  }

  @PostMapping(value = "/{orderId}/simulate")
  @ResponseBody
  public Map<String, Object> simulate(@PathVariable Long orderId,
      @RequestParam(required = false) String ref,
      @RequestParam(required = false) String token,
      @RequestParam(defaultValue = "VIETQR") String platform,
      Principal principal) {
    Order order = ownedOrder(orderId, principal);
    if (ref == null) ref = order.getPaymentReference();
    if (token == null) token = order.getQrToken();
    return qrPayments.confirmSimulatedPayment(orderId, ref, token, QrPlatform.from(platform));
  }

  @PostMapping(value = "/scan/confirm")
  @ResponseBody
  public Map<String, Object> scanConfirm(@RequestParam Long orderId,
      @RequestParam String ref,
      @RequestParam String token,
      @RequestParam(defaultValue = "VIETQR") String platform) {
    return qrPayments.confirmSimulatedPayment(orderId, ref, token, QrPlatform.from(platform));
  }

  @GetMapping("/{orderId}/status")
  @ResponseBody
  public Map<String, Object> status(@PathVariable Long orderId, Principal principal) {
    return qrPayments.status(ownedOrder(orderId, principal));
  }

  private Order ownedOrder(Long orderId, Principal principal) {
    if (principal == null)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    return orders.findByIdAndUser_Email(orderId, principal.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  private String baseUrl(HttpServletRequest request) {
    String forwardedHost = request.getHeader("X-Forwarded-Host");
    if (forwardedHost != null && !forwardedHost.isBlank()) {
      String proto = request.getHeader("X-Forwarded-Proto");
      return (proto == null || proto.isBlank() ? "http" : proto) + "://" + forwardedHost
          + request.getContextPath();
    }
    String scheme = request.getScheme();
    String serverName = request.getServerName();
    int port = request.getServerPort();
    String portPart = ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443))
        ? "" : ":" + port;
    return scheme + "://" + serverName + portPart + request.getContextPath();
  }
}
