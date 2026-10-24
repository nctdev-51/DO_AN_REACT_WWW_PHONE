package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.catalog.ReviewService;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {
  private final ProductRepository products;
  private final CategoryRepository categories;
  private final ReviewRepository reviews;
  private final ReviewService reviewService;
  private final ProductVariantRepository variants;

  public ProductController(
      ProductRepository products,
      CategoryRepository categories,
      ReviewRepository reviews,
      ReviewService reviewService,
      ProductVariantRepository variants) {
    this.products = products;
    this.categories = categories;
    this.reviews = reviews;
    this.reviewService = reviewService;
    this.variants = variants;
  }

  @GetMapping
  public String list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String kind,
      @RequestParam(required = false) String color,
      @RequestParam(required = false) String purpose,
      @RequestParam(required = false) String brand,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "newest") String sort,
      @RequestParam(required = false) String storage,
      @RequestParam(required = false) String ram,
      @RequestParam(defaultValue = "false") boolean inStock,
      Model model) {
    kind = java.util.Set.of("PHONE", "ACCESSORY").contains(kind == null ? "" : kind) ? kind : null;
    color = color == null || color.isBlank() ? null : color.trim();
    purpose =
        java.util.Set.of("EVERYDAY", "CAMERA", "GAMING", "BATTERY", "COMPACT")
                .contains(java.util.Objects.toString(purpose, ""))
            ? purpose
            : null;
    keyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
    if (keyword != null && keyword.length() > 120) keyword = keyword.substring(0, 120);
    brand = brand == null || brand.isBlank() ? null : brand.trim();
    storage = storage == null || storage.isBlank() ? null : storage.trim();
    ram = ram == null || ram.isBlank() ? null : ram.trim();
    if (minPrice != null && minPrice.signum() < 0) minPrice = BigDecimal.ZERO;
    if (maxPrice != null && minPrice != null && maxPrice.compareTo(minPrice) < 0) {
      model.addAttribute("errorMsg", "Khoảng giá không hợp lệ.");
      maxPrice = minPrice;
    }
    var filter =
        new iuh.fit.www_phone_ver1.service.catalog.CatalogFilter(
            keyword,
            brand,
            categoryId,
            minPrice,
            maxPrice,
            storage,
            ram,
            inStock,
            kind,
            color,
            purpose);
    var result =
        products.findAll(
            filter.specification(sort), PageRequest.of(Math.min(Math.max(page, 0), 10000), 12));
    model.addAttribute("products", result);
    var selected = new java.util.HashMap<Long, iuh.fit.www_phone_ver1.entity.ProductVariant>();
    result.forEach(p -> selected.put(p.getId(), filter.selected(p)));
    model.addAttribute("selectedVariants", selected);
    model.addAttribute("categories", categories.findAll(Sort.by("name")));
    model.addAttribute("brands", products.findAvailableBrands());
    model.addAttribute("keyword", keyword);
    model.addAttribute("kind", kind);
    model.addAttribute("color", color);
    model.addAttribute("purpose", purpose);
    model.addAttribute("colorOptions", variants.availableColors());
    model.addAttribute("brand", brand);
    model.addAttribute("categoryId", categoryId);
    model.addAttribute("minPrice", minPrice);
    model.addAttribute("maxPrice", maxPrice);
    model.addAttribute("sort", sort);
    model.addAttribute("storage", storage);
    model.addAttribute("ram", ram);
    model.addAttribute("inStock", inStock);
    model.addAttribute("storageOptions", variants.availableStorage());
    model.addAttribute("ramOptions", variants.availableRam());
    return "store/catalog/list";
  }

  @GetMapping("/{id}")
  public String detail(
      @PathVariable Long id,
      @RequestParam(required = false) Long variantId,
      Model model,
      Principal user,
      jakarta.servlet.http.HttpSession session) {
    return products
        .findById(id)
        .filter(p -> p.getStatus() == 1)
        .map(
            product -> {
              synchronized (session) {
                @SuppressWarnings("unchecked")
                var previous = (List<Long>) session.getAttribute("recentProducts");
                var recent = new java.util.ArrayList<Long>(previous == null ? List.of() : previous);
                recent.remove(id);
                recent.add(0, id);
                session.setAttribute(
                    "recentProducts", List.copyOf(recent.subList(0, Math.min(10, recent.size()))));
              }
              model.addAttribute("product", product);
              model.addAttribute(
                  "variants", product.getVariants().stream().filter(v -> v.isActive()).toList());
              model.addAttribute(
                  "selectedVariantId",
                  product.getVariants().stream()
                      .filter(v -> v.isActive() && v.getStock() > 0)
                      .findFirst()
                      .or(
                          () ->
                              product.getVariants().stream().filter(v -> v.isActive()).findFirst())
                      .map(v -> v.getId())
                      .orElse(null));
              product.getVariants().stream()
                  .filter(v -> v.isActive() && v.getId().equals(variantId))
                  .findFirst()
                  .ifPresent(v -> model.addAttribute("selectedVariantId", v.getId()));
              var accessories =
                  products.findByStatus(1).stream()
                      .filter(
                          p ->
                              "ACCESSORY".equals(p.getKind())
                                  && p.getCompatibleSkus() != null
                                  && java.util.Arrays.stream(p.getCompatibleSkus().split(","))
                                      .map(String::trim)
                                      .anyMatch(product.getSku()::equals))
                      .limit(4)
                      .toList();
              model.addAttribute("accessories", accessories);
              var feedback = reviews.findByProduct_IdOrderByCreatedAtDesc(id);
              model.addAttribute("reviews", feedback);
              model.addAttribute(
                  "averageRating",
                  feedback.stream().mapToInt(r -> r.getRating()).average().orElse(0));
              model.addAttribute(
                  "canReview", user != null && reviewService.canReview(user.getName(), id));
              model.addAttribute(
                  "relatedProducts",
                  product.getCategory() == null
                      ? List.of()
                      : products.findTop4ByCategory_IdAndStatusAndIdNot(
                          product.getCategory().getId(), 1, id));
              return "store/catalog/detail";
            })
        .orElse("redirect:/products");
  }

  @PostMapping("/{id}/reviews")
  public String review(
      @PathVariable Long id,
      @RequestParam int rating,
      @RequestParam String content,
      Principal user,
      RedirectAttributes flash) {
    try {
      reviewService.save(user.getName(), id, rating, content);
      flash.addFlashAttribute("successMsg", "Đã lưu đánh giá của bạn.");
    } catch (IllegalArgumentException error) {
      flash.addFlashAttribute("errorMsg", error.getMessage());
    }
    return "redirect:/products/" + id + "#reviews";
  }
}
