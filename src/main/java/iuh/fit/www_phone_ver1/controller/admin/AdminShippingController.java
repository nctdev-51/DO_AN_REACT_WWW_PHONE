package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.dto.order.ParcelForm;
import iuh.fit.www_phone_ver1.service.shipping.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
public class AdminShippingController {
  private final GhnShippingService shipping;

  public AdminShippingController(GhnShippingService shipping) {
    this.shipping = shipping;
  }

  @PostMapping("/{id}/ghn/create")
  public String create(
      @PathVariable Long id,
      @Valid @ModelAttribute ParcelForm form,
      BindingResult errors,
      RedirectAttributes flash) {
    if (errors.hasErrors())
      flash.addFlashAttribute(
          "errorMsg",
          "Kiểm tra khối lượng 1–19.999g, kích thước 1–200cm và giá trị khai bảo hiểm 0–5 triệu.");
    else
      try {
        var result = shipping.create(id, form);
        flash.addFlashAttribute(result.confirmed() ? "successMsg" : "errorMsg", result.message());
      } catch (IllegalArgumentException ex) {
        flash.addFlashAttribute("errorMsg", ex.getMessage());
      }
    return "redirect:/admin/orders/" + id;
  }

  @PostMapping("/{id}/ghn/sync")
  public String sync(@PathVariable Long id, RedirectAttributes flash) {
    try {
      shipping.sync(id);
      flash.addFlashAttribute(
          "successMsg",
          "Đã lấy trạng thái xác nhận từ GHN. Kiểm tra giao nhận trước khi chốt trạng thái đơn.");
    } catch (IllegalArgumentException ex) {
      flash.addFlashAttribute("errorMsg", ex.getMessage());
    }
    return "redirect:/admin/orders/" + id;
  }
}
