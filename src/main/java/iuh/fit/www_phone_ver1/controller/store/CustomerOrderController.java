package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.entity.Order;
import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.service.order.OrderService;
import iuh.fit.www_phone_ver1.service.order.OrderStatuses;
import iuh.fit.www_phone_ver1.service.payment.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
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
  private final VnPayService payments;

  public CustomerOrderController(
      OrderRepository orders,
      OrderService service,
      iuh.fit.www_phone_ver1.repository.RefundRepository refunds,
      VnPayService payments) {
    this.orders = orders;
    this.refunds = refunds;
    this.service = service;
    this.payments = payments;
  }

  @GetMapping
  public String list(Principal user, Model model) {
    model.addAttribute("orders", orders.findByUser_EmailOrderByOrderDateDesc(user.getName()));
    return "store/orders/list";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Principal user, Model model) {
    Order order =
        orders
            .findByIdAndUser_Email(id, user.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    model.addAttribute("order", order);
    model.addAttribute("refunds", refunds.findByOrder_IdOrderByIdDesc(id));
    model.addAttribute("vnpayAvailable", payments.isAvailable());
    model.addAttribute("paymentExpired", service.isPaymentWindowExpired(order));
    model.addAttribute("paymentDeadlineIso", order.getOrderDate().plusMinutes(15).toString());
    model.addAttribute("milestoneStep", resolveMilestoneStep(order));
    return "store/orders/detail";
  }

  private int resolveMilestoneStep(Order order) {
    if (OrderStatuses.CANCELLED.equals(order.getStatus())) return 0;
    if (OrderStatuses.DELIVERED.equals(order.getStatus())) return 5;
    if (OrderStatuses.SHIPPING.equals(order.getStatus())) return 4;
    if (order.getTrackingCode() != null && !order.getTrackingCode().isBlank()) return 3;
    if ("PAID".equals(order.getPaymentStatus()) || "COD".equals(order.getPaymentMethod())) return 2;
    return 1;
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

  @PostMapping("/{id}/retry-vnpay")
  public String retryVnPay(
      @PathVariable Long id,
      Principal user,
      HttpServletRequest request,
      RedirectAttributes flash) {
    try {
      if (!payments.isAvailable()) {
        throw new IllegalArgumentException("VNPay hiện chưa sẵn sàng. Bạn có thể chuyển sang thanh toán COD.");
      }
      Order updated = service.retryVnPay(id, user.getName());
      return "redirect:" + payments.createUrl(updated, request.getRemoteAddr());
    } catch (IllegalArgumentException error) {
      flash.addFlashAttribute("errorMsg", error.getMessage());
      return "redirect:/orders/" + id;
    }
  }

  @PostMapping("/{id}/switch-cod")
  public String switchToCod(
      @PathVariable Long id, Principal user, RedirectAttributes flash) {
    try {
      service.switchToCod(id, user.getName());
      flash.addFlashAttribute(
          "successMsg", "Đã chuyển đơn hàng sang thanh toán khi nhận hàng (COD).");
    } catch (IllegalArgumentException error) {
      flash.addFlashAttribute("errorMsg", error.getMessage());
    }
    return "redirect:/orders/" + id;
  }
}
