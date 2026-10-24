package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.dto.order.*;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.promotion.PromotionService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/promotions")
public class AdminPromotionController {
  private final PromotionRepository promotions;
  private final StorePolicyRepository policies;
  private final PromotionService service;

  public AdminPromotionController(
      PromotionRepository promotions, StorePolicyRepository policies, PromotionService service) {
    this.promotions = promotions;
    this.policies = policies;
    this.service = service;
  }

  private void heading(Model model) {
    model.addAttribute("currentPage", "promotions");
    model.addAttribute("pageTitle", "Khuyến mãi & giao hàng");
  }

  @GetMapping
  public String list(Model model) {
    heading(model);
    var list = promotions.findAll(Sort.by("id").descending());
    model.addAttribute("promotions", list);
    model.addAttribute("remaining", service.remaining(list));
    StorePolicyForm form = new StorePolicyForm();
    BeanUtils.copyProperties(policies.findById(1L).orElseThrow(), form);
    model.addAttribute("policyForm", form);
    return "admin/promotions/list";
  }

  @GetMapping("/new")
  public String create(Model model) {
    heading(model);
    model.addAttribute("promotionForm", new PromotionForm());
    return "admin/promotions/form";
  }

  @GetMapping("/{id}/edit")
  public String edit(@PathVariable Long id, Model model) {
    heading(model);
    PromotionForm form = new PromotionForm();
    BeanUtils.copyProperties(
        promotions
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)),
        form);
    model.addAttribute("promotionForm", form);
    model.addAttribute("promotionId", id);
    return "admin/promotions/form";
  }

  @PostMapping("/save")
  public String save(
      @RequestParam(required = false) Long id,
      @Valid @ModelAttribute("promotionForm") PromotionForm form,
      BindingResult errors,
      Model model,
      RedirectAttributes flash) {
    heading(model);
    model.addAttribute("promotionId", id);
    if (!errors.hasErrors()) {
      try {
        service.save(id, form);
        flash.addFlashAttribute("successMsg", "Đã lưu khuyến mãi.");
        return "redirect:/admin/promotions";
      } catch (IllegalArgumentException
          | org.springframework.dao.DataIntegrityViolationException
          | org.springframework.orm.ObjectOptimisticLockingFailureException e) {
        errors.reject(
            "promotion",
            e instanceof IllegalArgumentException
                ? e.getMessage()
                : "Dữ liệu vừa thay đổi hoặc mã bị trùng. Hãy tải lại trang.");
      }
    }
    return "admin/promotions/form";
  }

  @PostMapping("/policy")
  public String policy(
      @Valid @ModelAttribute("policyForm") StorePolicyForm form,
      BindingResult errors,
      Model model,
      RedirectAttributes flash) {
    if (!errors.hasErrors()) {
      try {
        service.savePolicy(form);
        flash.addFlashAttribute("successMsg", "Đã lưu phí giao hàng.");
        return "redirect:/admin/promotions";
      } catch (IllegalArgumentException
          | org.springframework.orm.ObjectOptimisticLockingFailureException e) {
        errors.reject("policy", "Cấu hình vừa thay đổi. Hãy tải lại trang.");
      }
    }
    heading(model);
    var list = promotions.findAll(Sort.by("id").descending());
    model.addAttribute("promotions", list);
    model.addAttribute("remaining", service.remaining(list));
    return "admin/promotions/list";
  }
}
