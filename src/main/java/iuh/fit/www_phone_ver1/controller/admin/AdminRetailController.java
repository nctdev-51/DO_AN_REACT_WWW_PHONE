package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.entity.StoreExperience;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService;
import iuh.fit.www_phone_ver1.service.order.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminRetailController {
  private final StoreExperienceService settings;
  private final ConsultationRepository requests;
  private final ConsultationService consultations;
  private final RefundService refunds;
  private final DeliveryService delivery;
  private final ProductRepository products;

  public AdminRetailController(
      StoreExperienceService settings,
      ConsultationRepository requests,
      ConsultationService consultations,
      RefundService refunds,
      DeliveryService delivery,
      ProductRepository products) {
    this.settings = settings;
    this.requests = requests;
    this.consultations = consultations;
    this.refunds = refunds;
    this.delivery = delivery;
    this.products = products;
  }

  private void heading(Model model, String title, String current) {
    model.addAttribute("pageTitle", title);
    model.addAttribute("currentPage", current);
  }

  @GetMapping("/experience")
  public String experience(Model model) {
    heading(model, "Nội dung & điểm thưởng", "experience");
    model.addAttribute("experienceForm", settings.get());
    model.addAttribute("products", products.findByStatus(1));
    return "admin/retail/settings";
  }

  @PostMapping("/experience")
  public String save(
      @Valid @ModelAttribute("experienceForm") StoreExperience form,
      BindingResult errors,
      Model model,
      RedirectAttributes flash) {
    if (!errors.hasErrors())
      try {
        settings.save(form);
        flash.addFlashAttribute("successMsg", "Đã lưu nội dung và cấu hình.");
        return "redirect:/admin/experience";
      } catch (IllegalArgumentException
          | org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
        errors.reject(
            "settings",
            ex instanceof IllegalArgumentException
                ? ex.getMessage()
                : "Nội dung vừa thay đổi. Hãy tải lại.");
      }
    heading(model, "Nội dung & điểm thưởng", "experience");
    model.addAttribute("products", products.findByStatus(1));
    return "admin/retail/settings";
  }

  @GetMapping("/consultations")
  public String requests(Model model) {
    heading(model, "Tư vấn trả góp & thu cũ", "consultations");
    model.addAttribute("requests", requests.findAll(Sort.by("id").descending()));
    return "admin/retail/consultations";
  }

  @PostMapping("/consultations/{id}")
  public String update(
      @PathVariable Long id,
      @RequestParam long version,
      @RequestParam String status,
      @RequestParam String response,
      @RequestParam(required = false) BigDecimal amount,
      RedirectAttributes flash) {
    try {
      consultations.update(id, version, status, response, amount);
      flash.addFlashAttribute("successMsg", "Đã cập nhật yêu cầu.");
    } catch (IllegalArgumentException
        | org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
      flash.addFlashAttribute(
          "errorMsg",
          ex instanceof IllegalArgumentException
              ? ex.getMessage()
              : "Yêu cầu vừa thay đổi. Hãy tải lại.");
    }
    return "redirect:/admin/consultations";
  }

  @PostMapping("/orders/{id}/delivery")
  public String delivery(
      @PathVariable Long id,
      @RequestParam String carrier,
      @RequestParam String trackingCode,
      @RequestParam String note,
      RedirectAttributes flash) {
    try {
      delivery.update(id, carrier, trackingCode, note);
      flash.addFlashAttribute("successMsg", "Đã lưu thông tin vận chuyển.");
    } catch (IllegalArgumentException ex) {
      flash.addFlashAttribute("errorMsg", ex.getMessage());
    }
    return "redirect:/admin/orders/" + id;
  }

  @PostMapping("/orders/{id}/refunds")
  public String refund(
      @PathVariable Long id,
      @RequestParam BigDecimal amount,
      @RequestParam String reason,
      RedirectAttributes flash) {
    try {
      refunds.request(id, amount, reason);
      flash.addFlashAttribute("successMsg", "Đã ghi nhận khoản hoàn chờ đối soát.");
    } catch (IllegalArgumentException ex) {
      flash.addFlashAttribute("errorMsg", ex.getMessage());
    }
    return "redirect:/admin/orders/" + id;
  }

  @PostMapping("/refunds/{id}")
  public String settle(
      @PathVariable Long id,
      @RequestParam String status,
      @RequestParam String reference,
      RedirectAttributes flash) {
    try {
      refunds.settle(id, status, reference);
      flash.addFlashAttribute("successMsg", "Đã ghi nhận kết quả đối soát.");
    } catch (IllegalArgumentException
        | org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
      flash.addFlashAttribute(
          "errorMsg",
          ex instanceof IllegalArgumentException
              ? ex.getMessage()
              : "Khoản hoàn vừa được xử lý. Hãy tải lại.");
    }
    return "redirect:/admin/orders";
  }
}
