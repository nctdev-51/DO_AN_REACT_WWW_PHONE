package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.repository.SupportRequestRepository;
import iuh.fit.www_phone_ver1.service.order.SupportService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/support")
public class AdminSupportController {
  private final SupportRequestRepository requests;
  private final SupportService service;

  public AdminSupportController(SupportRequestRepository requests, SupportService service) {
    this.requests = requests;
    this.service = service;
  }

  @GetMapping
  public String list(@RequestParam(defaultValue = "0") int page, Model model) {
    model.addAttribute("currentPage", "support");
    model.addAttribute("pageTitle", "Đổi trả & bảo hành");
    model.addAttribute(
        "requestPage",
        requests.findAll(PageRequest.of(Math.max(0, page), 20, Sort.by("id").descending())));
    return "admin/support/list";
  }

  @PostMapping("/{id}/inspection")
  public String inspect(
      @PathVariable Long id,
      @RequestParam(defaultValue = "false") boolean resellable,
      @RequestParam String note,
      RedirectAttributes flash) {
    try {
      service.inspect(id, resellable, note);
      flash.addFlashAttribute("successMsg", "Đã ghi nhận kiểm tra hàng trả.");
    } catch (IllegalArgumentException ex) {
      flash.addFlashAttribute("errorMsg", ex.getMessage());
    }
    return "redirect:/admin/support";
  }

  @PostMapping("/{id}/status")
  public String update(
      @PathVariable Long id,
      @RequestParam String status,
      @RequestParam String note,
      RedirectAttributes flash) {
    try {
      service.update(id, status, note);
      flash.addFlashAttribute("successMsg", "Đã cập nhật hồ sơ hỗ trợ.");
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute("errorMsg", e.getMessage());
    }
    return "redirect:/admin/support";
  }
}
