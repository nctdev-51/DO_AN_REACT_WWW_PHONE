package iuh.fit.www_phone_ver1.controller.admin;

import iuh.fit.www_phone_ver1.dto.order.*;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.promotion.PromotionService;
import iuh.fit.www_phone_ver1.service.promotion.InventoryPromotionService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Collections;
import java.util.TreeSet;
import java.util.UUID;
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
  private final InventoryPromotionService inventory;

  public AdminPromotionController(
      PromotionRepository promotions, StorePolicyRepository policies, PromotionService service,
      InventoryPromotionService inventory) {
    this.promotions = promotions;
    this.policies = policies;
    this.service = service;
    this.inventory = inventory;
  }

  private void heading(Model model) {
    model.addAttribute("currentPage", "promotions");
    model.addAttribute("pageTitle", "Khuyến mãi & giao hàng");
  }

  @GetMapping
  public String list(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false) Integer quarter,
      @RequestParam(defaultValue = "") String state,
      @RequestParam(defaultValue = "") String combination,
      Model model) {
    if (year != null && (year < 1900 || year > 9999)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Năm không hợp lệ.");
    }
    if (quarter != null && (quarter < 1 || quarter > 4)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quý không hợp lệ.");
    }
    int currentYear = LocalDateTime.now().getYear();
    Integer selectedYear = year == null && quarter != null ? Integer.valueOf(currentYear) : year;
    populateList(model, q, selectedYear, quarter, state, combination);
    StorePolicyForm form = new StorePolicyForm();
    BeanUtils.copyProperties(policies.findById(1L).orElseThrow(), form);
    model.addAttribute("policyForm", form);
    return "admin/promotions/list";
  }

  private void populateList(Model model, String q, Integer year, Integer quarter,
      String state, String combination) {
    heading(model);
    String keyword = q.trim().toLowerCase(Locale.ROOT);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime periodStart = year == null ? null
        : LocalDateTime.of(year, quarter == null ? 1 : (quarter - 1) * 3 + 1, 1, 0, 0);
    LocalDateTime periodEnd = periodStart == null ? null
        : periodStart.plusMonths(quarter == null ? 12 : 3);
    var all = promotions.findAll(Sort.by("id").descending());
    var availableYears = new TreeSet<Integer>(Collections.reverseOrder());
    availableYears.add(now.getYear());
    if (year != null) availableYears.add(year);
    for (var promotion : all) {
      int first = Math.max(1900, promotion.getStartsAt().getYear());
      int last = Math.min(9999, promotion.getEndsAt().minusNanos(1).getYear());
      for (int candidate = first; candidate <= last; candidate++) availableYears.add(candidate);
    }
    var list = all.stream()
        .filter(p -> p.getCode().toLowerCase(Locale.ROOT).contains(keyword)
            || p.getDescription().toLowerCase(Locale.ROOT).contains(keyword))
        .filter(p -> periodStart == null
            || (p.getStartsAt().isBefore(periodEnd) && p.getEndsAt().isAfter(periodStart)))
        .filter(p -> state.isEmpty() || state.equals(statusOf(p, now)))
        .filter(p -> combination.isEmpty()
            || combination.equals(p.isCombinableWithPoints() ? "compatible" : "exclusive"))
        .toList();
    model.addAttribute("promotions", list);
    model.addAttribute("remaining", service.remaining(list));
    model.addAttribute("promotionStates", list.stream().collect(java.util.stream.Collectors.toMap(
        iuh.fit.www_phone_ver1.entity.Promotion::getId, p -> statusOf(p, now))));
    model.addAttribute("q", q);
    model.addAttribute("year", year);
    model.addAttribute("quarter", quarter);
    model.addAttribute("availableYears", availableYears);
    model.addAttribute("state", state);
    model.addAttribute("combination", combination);
    model.addAttribute("inventorySuggestions", inventory.suggestions());
    model.addAttribute("salesDays", inventory.salesDays());
  }

  private String statusOf(iuh.fit.www_phone_ver1.entity.Promotion promotion, LocalDateTime now) {
    if (!promotion.getEndsAt().isAfter(now)) return "expired";
    if (!promotion.isActive()) return "disabled";
    return promotion.getStartsAt().isAfter(now) ? "scheduled" : "active";
  }

  @GetMapping("/inventory-suggestions")
  @ResponseBody
  public java.util.List<InventoryPromotionService.Suggestion> inventorySuggestions() {
    return inventory.suggestions();
  }

  @PostMapping("/batch")
  public String batch(@RequestParam(required = false) java.util.List<Long> ids,
      @RequestParam String action, RedirectAttributes flash) {
    if (!java.util.Set.of("enable", "disable", "delete").contains(action)) {
      flash.addFlashAttribute("errorMsg", "Chọn thao tác hợp lệ.");
    } else {
      AdminBatch.run(ids, id -> service.updateBatch(id, action), flash);
    }
    return "redirect:/admin/promotions";
  }

  @GetMapping("/inventory-suggestions/{productId}/apply")
  public String applySuggestion(@PathVariable Long productId, Model model) {
    var suggestion = inventory.suggestions().stream()
        .filter(row -> row.productId().equals(productId))
        .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    var form = new PromotionForm();
    form.setCode("KHO-" + productId + "-" + UUID.randomUUID().toString().substring(0, 8));
    String description = "Ưu đãi tồn kho - " + suggestion.productName();
    form.setDescription(description.substring(0, Math.min(description.length(), 200)));
    form.setScopeSkus(suggestion.sku());
    form.setDiscountType("PERCENT");
    form.setAmount(BigDecimal.valueOf(suggestion.discountPercent()));
    heading(model);
    model.addAttribute("promotionForm", form);
    return "admin/promotions/form";
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
    populateList(model, "", null, null, "", "");
    return "admin/promotions/list";
  }
}
