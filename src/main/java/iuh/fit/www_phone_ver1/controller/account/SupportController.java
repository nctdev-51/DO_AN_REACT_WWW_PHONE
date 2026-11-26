package iuh.fit.www_phone_ver1.controller.account;

import iuh.fit.www_phone_ver1.dto.order.SupportRequestForm;
import iuh.fit.www_phone_ver1.repository.SupportRequestRepository;
import iuh.fit.www_phone_ver1.service.order.SupportService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SupportController {
  private final SupportRequestRepository requests;
  private final SupportService service;

  public SupportController(SupportRequestRepository requests, SupportService service) {
    this.requests = requests;
    this.service = service;
  }

  @GetMapping("/account/support")
  public String list(Principal user, Model model) {
    model.addAttribute(
        "requests",
        requests.findByOrderDetail_Order_User_EmailOrderByCreatedAtDesc(user.getName()));
    return "account/support";
  }

  @PostMapping("/orders/{id}/support")
  public String create(
      @PathVariable Long id,
      @Valid @ModelAttribute SupportRequestForm form,
      BindingResult errors,
      Principal user,
      RedirectAttributes flash) {
    if (errors.hasErrors())
      flash.addFlashAttribute(
          "errorMsg", "Kiểm tra loại yêu cầu, số lượng và nội dung (10–2000 ký tự).");
    else
      try {
        service.create(id, user.getName(), form);
        flash.addFlashAttribute("successMsg", "Đã gửi yêu cầu. Cửa hàng sẽ kiểm tra và phản hồi.");
      } catch (IllegalArgumentException e) {
        flash.addFlashAttribute("errorMsg", e.getMessage());
      }
    return "redirect:/orders/" + id;
  }

  @PostMapping("/account/support/{id}/cancel")
  public String cancel(@PathVariable Long id, Principal user, RedirectAttributes flash) {
    try {
      service.cancel(id, user.getName());
      flash.addFlashAttribute("successMsg", "Đã hủy yêu cầu hỗ trợ.");
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute("errorMsg", e.getMessage());
    }
    return "redirect:/account/support";
  }
}
