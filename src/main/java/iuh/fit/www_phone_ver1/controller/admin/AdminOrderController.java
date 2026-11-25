package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.service.order.OrderService;
import iuh.fit.www_phone_ver1.service.order.OrderStatuses;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
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
  public String listOrders(
      @RequestParam(defaultValue = "") String code,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate placedOn,
      @RequestParam(defaultValue = "") String status,
      @RequestParam(defaultValue = "") String phone,
      Model model) {
    model.addAttribute("currentPage", "orders");
    model.addAttribute("pageTitle", "Quản lý đơn hàng");
    model.addAttribute("statuses", OrderStatuses.ALL);
    model.addAttribute("orderCode", code.trim());
    model.addAttribute("placedOn", placedOn);
    model.addAttribute("orderStatus", status);
    model.addAttribute("orderPhone", phone.trim());
    model.addAttribute("hasFilters", !code.isBlank() || placedOn != null
        || !status.isBlank() || !phone.isBlank());

    String normalizedPhone = phone.trim().replace(" ", "").replace("-", "");
    Long id = null;
    try {
      if (!code.isBlank()) id = Long.valueOf(code.trim().replaceFirst("^#\\s*", ""));
      if ((!status.isEmpty() && !OrderStatuses.ALL.contains(status))
          || !normalizedPhone.matches("[0-9]{0,20}") || (id != null && id <= 0)) {
        throw new IllegalArgumentException();
      }
      model.addAttribute("orders", orders.searchAdminOrders(id,
          placedOn == null ? null : placedOn.atStartOfDay(),
          placedOn == null ? null : placedOn.plusDays(1).atStartOfDay(), status, normalizedPhone));
    } catch (IllegalArgumentException error) {
      model.addAttribute("orders", List.of());
      model.addAttribute("filterError", "Kiểm tra mã đơn, trạng thái và số điện thoại.");
    }
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

  @PostMapping("/batch")
  public String batch(@RequestParam(required = false) List<Long> ids,
      @RequestParam("action") String status, RedirectAttributes flash) {
    if (!List.of(OrderStatuses.SHIPPING, OrderStatuses.DELIVERED, OrderStatuses.CANCELLED)
        .contains(status)) {
      flash.addFlashAttribute("errorMsg", "Chọn trạng thái hợp lệ.");
    } else {
      AdminBatch.run(ids, id -> {
        admin.updateStatus(id, status);
        return null;
      }, flash);
    }
    return "redirect:/admin/orders";
  }

  @PostMapping("/{id}/items/{detailId}")
  public String updateDetailQty(
      @PathVariable Long id,
      @PathVariable Long detailId,
      @RequestParam int quantity,
      RedirectAttributes flash) {
    try {
      admin.updateOrderDetailQuantity(id, detailId, quantity);
      flash.addFlashAttribute("successMsg", "Đã cập nhật số lượng dòng đơn hàng.");
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute("errorMsg", e.getMessage());
    }
    return "redirect:/admin/orders/" + id;
  }
}
