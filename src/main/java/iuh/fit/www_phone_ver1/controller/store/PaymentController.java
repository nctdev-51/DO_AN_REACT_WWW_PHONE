package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.service.order.OrderService;
import iuh.fit.www_phone_ver1.service.payment.VnPayService;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/payments/vnpay")
public class PaymentController {
  private final VnPayService payments;
  private final OrderRepository orders;
  private final OrderService orderService;

  public PaymentController(VnPayService payments, OrderRepository orders, OrderService orderService) {
    this.payments = payments;
    this.orders = orders;
    this.orderService = orderService;
  }

  @GetMapping("/ipn")
  @ResponseBody
  public Map<String, String> notify(@RequestParam Map<String, String> values) {
    return payments.handleNotification(values);
  }

  @GetMapping("/return")
  public String result(
      @RequestParam("vnp_TxnRef") String reference,
      @RequestParam Map<String, String> values,
      Principal user,
      Model model) {
    var order =
        orders
            .findByPaymentReference(reference)
            .filter(o -> o.getUser() != null && o.getUser().getEmail().equals(user.getName()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    payments.recordReturnFailureIfPending(reference, values);
    order = orders.findByIdAndUser_Email(order.getId(), user.getName()).orElse(order);
    model.addAttribute("order", order);
    model.addAttribute("vnpResponseCode", values.get("vnp_ResponseCode"));
    model.addAttribute("vnpayAvailable", payments.isAvailable());
    model.addAttribute("paymentExpired", orderService.isPaymentWindowExpired(order));
    model.addAttribute("paymentDeadlineIso", order.getOrderDate().plusMinutes(15).toString());
    return "store/checkout/payment-result";
  }
}
