package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.service.order.OrderService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {
  private final OrderRepository orders;
  private final iuh.fit.www_phone_ver1.repository.RefundRepository refunds;
  private final OrderService admin;

  public AdminOrderController(
      OrderRepository orders,
      OrderService admin,
      iuh.fit.www_phone_ver1.repository.RefundRepository refunds) {
    this.orders = orders;
    this.refunds = refunds;
    this.admin = admin;
  }

  @GetMapping
  public String listOrders(Model model) {
    model.addAttribute("currentPage", "orders");
    model.addAttribute("pageTitle", "Quản lý đơn hàng");
    model.addAttribute("orders", orders.findAll(Sort.by(Sort.Direction.DESC, "orderDate")));
    model.addAttribute("statuses", iuh.fit.www_phone_ver1.service.order.OrderStatuses.ALL);
    return "admin/orders/list";
  }

  @GetMapping("/{id}")
  public String orderDetail(@PathVariable Long id, Model model) {
    var order =
        orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    model.addAttribute("currentPage", "orders");
    model.addAttribute("pageTitle", "Chi tiết đơn #" + id);
    model.addAttribute("order", order);
    model.addAttribute("statuses", iuh.fit.www_phone_ver1.service.order.OrderStatuses.ALL);
    model.addAttribute("refunds", refunds.findByOrder_IdOrderByIdDesc(id));
    return "admin/orders/detail";
  }

  @PostMapping("/{id}/status")
  public String updateStatus(
      @PathVariable Long id, @RequestParam String status, RedirectAttributes flash) {
    try {
      admin.updateStatus(id, status);
      flash.addFlashAttribute("successMsg", "Đã cập nhật trạng thái đơn #" + id);
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute("errorMsg", e.getMessage());
    }
    return "redirect:/admin/orders";
  }
}
