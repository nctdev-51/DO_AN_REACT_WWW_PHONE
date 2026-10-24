package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.promotion.PromotionService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class OfferController {
  private final PromotionRepository promotions;
  private final PromotionService service;
  private final ProductRepository products;

  public OfferController(
      PromotionRepository promotions, PromotionService service, ProductRepository products) {
    this.promotions = promotions;
    this.service = service;
    this.products = products;
  }

  @PostMapping("/offers/apply")
  public String apply(
      @RequestParam String code,
      jakarta.servlet.http.HttpSession session,
      org.springframework.web.servlet.mvc.support.RedirectAttributes flash) {
    var p = promotions.findByCode(code.toUpperCase(java.util.Locale.ROOT)).orElse(null);
    if (p != null
        && p.isActive()
        && !p.getStartsAt().isAfter(LocalDateTime.now())
        && p.getEndsAt().isAfter(LocalDateTime.now())) {
      session.setAttribute("selectedPromotion", p.getCode());
      flash.addFlashAttribute(
          "successMsg", "Đã chọn mã " + p.getCode() + ". Điều kiện được kiểm tra khi đặt hàng.");
    } else flash.addFlashAttribute("errorMsg", "Mã không còn khả dụng.");
    var cart = (iuh.fit.www_phone_ver1.dto.cart.Cart) session.getAttribute("cart");
    return cart != null && !cart.getItemList().isEmpty()
        ? "redirect:/checkout"
        : "redirect:/products";
  }

  @GetMapping("/offers")
  public String offers(Model model) {
    var now = LocalDateTime.now();
    var list =
        promotions.findByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByEndsAtAsc(
            now, now);
    model.addAttribute("promotions", list);
    model.addAttribute("remaining", service.remaining(list));
    var eligible =
        new java.util.HashMap<Long, java.util.List<iuh.fit.www_phone_ver1.entity.Product>>();
    var required =
        new java.util.HashMap<Long, java.util.List<iuh.fit.www_phone_ver1.entity.Product>>();
    list.forEach(
        p -> {
          eligible.put(p.getId(), selected(p.getScopeSkus()));
          required.put(p.getId(), selected(p.getRequiredSkus()));
        });
    model.addAttribute("eligibleProducts", eligible);
    model.addAttribute("requiredProducts", required);
    return "store/offers/index";
  }

  private java.util.List<iuh.fit.www_phone_ver1.entity.Product> selected(String skus) {
    return java.util.Arrays.stream(java.util.Objects.toString(skus, "").split(","))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .distinct()
        .map(products::findBySku)
        .flatMap(java.util.Optional::stream)
        .filter(p -> p.getStatus() == 1)
        .limit(12)
        .toList();
  }
}
