package iuh.fit.www_phone_ver1.controller.account;

import iuh.fit.www_phone_ver1.dto.order.ConsultationForm;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService;
import iuh.fit.www_phone_ver1.service.order.*;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CustomerBenefitsController {
  private final ConsultationRepository requests;
  private final ConsultationService consultations;
  private final ProductRepository products;
  private final UserRepository users;
  private final LoyaltyRepository ledger;
  private final StoreExperienceService settings;

  public CustomerBenefitsController(
      ConsultationRepository requests,
      ConsultationService consultations,
      ProductRepository products,
      UserRepository users,
      LoyaltyRepository ledger,
      StoreExperienceService settings) {
    this.requests = requests;
    this.consultations = consultations;
    this.products = products;
    this.users = users;
    this.ledger = ledger;
    this.settings = settings;
  }

  private void options(Model model, String email) {
    model.addAttribute("requests", requests.findByUser_EmailOrderByIdDesc(email));
    model.addAttribute("products", products.findByStatus(1));
    model.addAttribute("currentAccountPage", "consultations");
  }

  @GetMapping("/account/consultations")
  public String list(
      @RequestParam(required = false) Long productId,
      @RequestParam(defaultValue = "INSTALLMENT") String type,
      Model model,
      Principal principal) {
    var user = users.findByEmail(principal.getName()).orElseThrow();
    var form = new ConsultationForm();
    form.setContactName(user.getFullName());
    form.setPhone(user.getPhone());
    form.setProductId(productId);
    form.setType("TRADE_IN".equals(type) ? type : "INSTALLMENT");
    model.addAttribute("consultationForm", form);
    options(model, principal.getName());
    return "account/consultations";
  }

  @PostMapping("/account/consultations")
  public String create(
      @Valid @ModelAttribute("consultationForm") ConsultationForm form,
      BindingResult errors,
      Principal principal,
      Model model,
      RedirectAttributes flash) {
    if (!errors.hasErrors())
      try {
        consultations.create(principal.getName(), form);
        flash.addFlashAttribute(
            "successMsg", "Đã gửi yêu cầu. Bạn có thể theo dõi phản hồi tại đây.");
        return "redirect:/account/consultations";
      } catch (IllegalArgumentException ex) {
        errors.reject("consultation", ex.getMessage());
      }
    options(model, principal.getName());
    return "account/consultations";
  }

  @PostMapping("/account/consultations/{id}/cancel")
  public String cancel(
      @PathVariable Long id,
      @RequestParam long version,
      Principal principal,
      RedirectAttributes flash) {
    try {
      consultations.cancel(id, version, principal.getName());
      flash.addFlashAttribute("successMsg", "Đã hủy yêu cầu.");
    } catch (IllegalArgumentException
        | org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
      flash.addFlashAttribute(
          "errorMsg",
          ex instanceof IllegalArgumentException
              ? ex.getMessage()
              : "Yêu cầu vừa thay đổi. Hãy tải lại.");
    }
    return "redirect:/account/consultations";
  }

  @GetMapping("/account/points")
  public String points(Principal principal, Model model) {
    var user = users.findByEmail(principal.getName()).orElseThrow();
    model.addAttribute("balance", ledger.balance(user.getId()));
    model.addAttribute("entries", ledger.findTop100ByUser_EmailOrderByIdDesc(principal.getName()));
    model.addAttribute("experience", settings.get());
    return "account/points";
  }
}
