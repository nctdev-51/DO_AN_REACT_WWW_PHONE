package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.repository.OrderRepository;
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

  public PaymentController(VnPayService payments, OrderRepository orders) {
    this.payments = payments;
    this.orders = orders;
  }

  @GetMapping("/ipn")
  @ResponseBody
  public Map<String, String> notify(@RequestParam Map<String, String> values) {
    return payments.handleNotification(values);
  }

  @GetMapping("/return")
  public String result(@RequestParam("vnp_TxnRef") String reference, Principal user, Model model) {
    var order =
        orders
            .findByPaymentReference(reference)
            .filter(o -> o.getUser() != null && o.getUser().getEmail().equals(user.getName()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    model.addAttribute("order", order);
    return "store/checkout/payment-result";
  }
}
