package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.service.payment.QrPaymentService;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/payments/qr")
public class QrPaymentStatusController {
  private final OrderRepository orders;
  private final QrPaymentService qrPayments;

  public QrPaymentStatusController(OrderRepository orders, QrPaymentService qrPayments) {
    this.orders = orders;
    this.qrPayments = qrPayments;
  }

  @GetMapping("/{orderId}/status")
  public Map<String, Object> status(@PathVariable Long orderId, Principal principal) {
    if (principal == null)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    var order = orders.findByIdAndUser_Email(orderId, principal.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return qrPayments.status(order);
  }
}
