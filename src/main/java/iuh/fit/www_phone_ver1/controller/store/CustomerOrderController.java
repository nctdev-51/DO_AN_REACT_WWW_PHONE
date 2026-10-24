package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.service.order.OrderService;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class CustomerOrderController {
  private final OrderRepository orders;
  private final iuh.fit.www_phone_ver1.repository.RefundRepository refunds;
  private final OrderService service;

  public CustomerOrderController(
      OrderRepository orders,
      OrderService service,
      iuh.fit.www_phone_ver1.repository.RefundRepository refunds) {
    this.orders = orders;
    this.refunds = refunds;
    this.service = service;
  }

  @GetMapping
  public String list(Principal user, Model model) {
    model.addAttribute("orders", orders.findByUser_EmailOrderByOrderDateDesc(user.getName()));
    return "store/orders/list";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Principal user, Model model) {
    model.addAttribute(
        "order",
        orders
            .findByIdAndUser_Email(id, user.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    model.addAttribute("refunds", refunds.findByOrder_IdOrderByIdDesc(id));
    return "store/orders/detail";
  }

  @PostMapping("/{id}/cancel")
  public String cancel(
      @PathVariable Long id,
      @RequestParam(defaultValue = "Khách hàng yêu cầu hủy") String reason,
      Principal user,
      RedirectAttributes flash) {
    try {
      service.cancelByCustomer(id, user.getName(), reason);
      flash.addFlashAttribute("successMsg", "Đã hủy đơn và hoàn lại tồn kho.");
    } catch (IllegalArgumentException error) {
      flash.addFlashAttribute("errorMsg", error.getMessage());
    }
    return "redirect:/orders/" + id;
  }
}
