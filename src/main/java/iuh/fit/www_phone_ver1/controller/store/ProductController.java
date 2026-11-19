package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.service.catalog.CatalogFilter;
import iuh.fit.www_phone_ver1.service.catalog.ReviewService;
import iuh.fit.www_phone_ver1.service.catalog.StorefrontCatalogService;
import iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService;
import iuh.fit.www_phone_ver1.service.order.AddressDirectory;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Step 4 — ProductController refactored.
 *
 * <p>Controller responsibilities: receive request, validate & parse params,
 * delegate to StorefrontCatalogService, populate Model, return view name.
 *
 * <p>No repository is injected here; all data access goes through services.
 * Exception: ReviewRepository kept for read-only review list (candidates for
 * future ReviewService extension).
 */
@Controller
@RequestMapping("/products")
public class ProductController {

  private final StorefrontCatalogService catalog;
  private final ReviewService reviewService;
  private final StoreExperienceService experienceService;
  private final AddressDirectory addressDirectory;

  public ProductController(
      StorefrontCatalogService catalog,
      ReviewService reviewService,
      StoreExperienceService experienceService,
      AddressDirectory addressDirectory) {
    this.catalog = catalog;
    this.reviewService = reviewService;
    this.experienceService = experienceService;
    this.addressDirectory = addressDirectory;
  }

  // ── Catalog list ─────────────────────────────────────────────────────────────

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
      HttpSession session,
      Model model) {

    // ── Input sanitization ───────────────────────────────────────────────────
    sort =
        Set.of("newest", "price-asc", "price-desc", "bestselling", "popular", "rating")
                .contains(sort)
            ? sort
            : "newest";
    kind = Set.of("PHONE", "ACCESSORY").contains(kind == null ? "" : kind) ? kind : null;
    color = color == null || color.isBlank() ? null : color.trim();
    purpose =
        Set.of("EVERYDAY", "CAMERA", "GAMING", "BATTERY", "COMPACT")
                .contains(Objects.toString(purpose, ""))
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

    // ── Build filter & query ─────────────────────────────────────────────────
    var filter = new CatalogFilter(keyword, brand, categoryId, minPrice, maxPrice,
        storage, ram, inStock, kind, color, purpose);
    Page<Product> result = catalog.listProducts(filter, sort, page);

    // ── Populate model ───────────────────────────────────────────────────────
    model.addAttribute("products", result);
    model.addAttribute("selectedVariants", catalog.selectedVariants(result, filter));
    model.addAttribute("soldQuantities", catalog.soldQuantitiesForPage(result));
    model.addAttribute("categories", catalog.allCategories());
    model.addAttribute("brands", catalog.availableBrands());
    model.addAttribute("colorOptions", catalog.availableColors());
    model.addAttribute("storageOptions", catalog.availableStorage());
    model.addAttribute("ramOptions", catalog.availableRam());
    model.addAttribute("keyword", keyword);
    model.addAttribute("kind", kind);
    model.addAttribute("color", color);
    model.addAttribute("purpose", purpose);
    model.addAttribute("brand", brand);
    model.addAttribute("categoryId", categoryId);
    model.addAttribute("minPrice", minPrice);
    model.addAttribute("maxPrice", maxPrice);
    model.addAttribute("sort", sort);
    model.addAttribute("storage", storage);
    model.addAttribute("ram", ram);
    model.addAttribute("inStock", inStock);
    model.addAttribute("recentProducts", catalog.recentProducts(session, null));
    model.addAttribute("storePolicy", catalog.storePolicy());
    return "store/catalog/list";
  }

  // ── Autocomplete suggestions ─────────────────────────────────────────────────

  @GetMapping(value = "/search/suggestions", produces = "application/json")
  @ResponseBody
  public List<String> suggestions(@RequestParam(required = false) String q) {
    return catalog.nameSuggestions(q);
  }

  // ── Popular redirect ──────────────────────────────────────────────────────────

  @GetMapping("/popular")
  public String popular() {
    return "redirect:/products?sort=bestselling";
  }

  // ── Product detail ────────────────────────────────────────────────────────────

  @GetMapping("/{id}")
  public String detail(
      @PathVariable Long id,
      @RequestParam(required = false) Long variantId,
      Model model,
      Principal user,
      HttpSession session) {

    return catalog
        .findActiveProduct(id)
        .map(
            product -> {
              // Track recently viewed & get previous list (excluding current)
              List<Product> recentProducts = catalog.trackAndGetRecentProducts(session, id);

              // ── Variant selection ──────────────────────────────────────────
              List<ProductVariant> activeVariants =
                  product.getVariants().stream().filter(ProductVariant::isActive).toList();
              ProductVariant chosenVariant =
                  activeVariants.stream()
                      .filter(v -> v.getId().equals(variantId))
                      .findFirst()
                      .orElseGet(
                          () ->
                              activeVariants.stream()
                                  .filter(v -> v.getStock() > 0)
                                  .findFirst()
                                  .orElse(activeVariants.isEmpty() ? null : activeVariants.get(0)));

              BigDecimal effectivePrice =
                  chosenVariant == null ? product.getPrice() : chosenVariant.getPrice();

              // ── Reviews ────────────────────────────────────────────────────
              var feedback = catalog.findReviews(id);

              // ── Populate model ─────────────────────────────────────────────
              model.addAttribute("product", product);
              model.addAttribute("variants", activeVariants);
              model.addAttribute("selectedVariantId", chosenVariant == null ? null : chosenVariant.getId());
              model.addAttribute("installment12Monthly",
                  effectivePrice.divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP));
              model.addAttribute("installment6Monthly",
                  effectivePrice.divide(BigDecimal.valueOf(6), 0, RoundingMode.HALF_UP));
              model.addAttribute("accessories", catalog.accessoriesFor(product.getSku()));
              model.addAttribute("includeRecommendationCarousel", true);
              model.addAttribute("reviews", feedback);
              model.addAttribute("averageRating",
                  feedback.stream().mapToInt(Review::getRating).average().orElse(0));
              model.addAttribute("ratingCounts", ratingCounts(feedback));
              model.addAttribute("canReview",
                  user != null && catalog.canReview(user.getName(), id));
              model.addAttribute("applicablePromotions", catalog.applicablePromotions(product));
              model.addAttribute("usageFitReasons", catalog.usageFitReasons(product));
              model.addAttribute("recentProducts", recentProducts);
              model.addAttribute("storePolicy", catalog.storePolicy());
              model.addAttribute("experience", experienceService.get());
              model.addAttribute("relatedProducts", catalog.relatedProducts(product));
              return "store/catalog/detail";
            })
        .orElse("redirect:/products");
  }

  // ── Delivery estimate ─────────────────────────────────────────────────────────

  @GetMapping("/{id}/delivery-estimate")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> deliveryEstimate(
      @PathVariable Long id,
      @RequestParam String provinceCode,
      @RequestParam(required = false) Long variantId) {

    var productOpt = catalog.findActiveProduct(id);
    if (productOpt.isEmpty()) return ResponseEntity.notFound().build();

    var provinceOpt =
        addressDirectory.provinces().stream()
            .filter(p -> p.code().equals(provinceCode))
            .findFirst();
    if (provinceOpt.isEmpty())
      return ResponseEntity.badRequest()
          .body(Map.of("error", "Vui lòng chọn tỉnh/thành phố hợp lệ."));

    Product product = productOpt.get();
    BigDecimal itemPrice =
        product.getVariants().stream()
            .filter(v -> v.isActive() && v.getId().equals(variantId))
            .findFirst()
            .map(ProductVariant::getPrice)
            .orElse(product.getPrice());

    StorePolicy policy = catalog.storePolicy();
    StoreExperience exp = experienceService.get();
    BigDecimal fee =
        itemPrice.compareTo(policy.getFreeShippingThreshold()) >= 0
            ? BigDecimal.ZERO
            : policy.getShippingFee();

    String pName = provinceOpt.get().name();
    boolean majorHub =
        pName.contains("Hồ Chí Minh") || pName.contains("Hà Nội") || pName.contains("Đà Nẵng");
    int minDays = majorHub ? Math.max(1, exp.getDeliveryMinDays() - 1) : exp.getDeliveryMinDays();
    int maxDays =
        majorHub ? Math.max(minDays, exp.getDeliveryMaxDays() - 1) : exp.getDeliveryMaxDays();

    var body = new LinkedHashMap<String, Object>();
    body.put("provinceName", pName);
    body.put("fee", fee);
    body.put("freeThreshold", policy.getFreeShippingThreshold());
    body.put("minDays", minDays);
    body.put("maxDays", maxDays);
    body.put("windowLabel", minDays == maxDays ? minDays + " ngày" : minDays + "–" + maxDays + " ngày");
    body.put("note",
        "Thời gian dự kiến theo khung tiêu chuẩn của cửa hàng; phí vận chuyển thực tế xác nhận tại bước Thanh toán.");
    return ResponseEntity.ok(body);
  }

  // ── Reviews (POST) ────────────────────────────────────────────────────────────

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

  // ── Helpers ───────────────────────────────────────────────────────────────────

  private Map<Integer, Long> ratingCounts(List<Review> feedback) {
    Map<Integer, Long> counts = new LinkedHashMap<>();
    for (int star = 5; star >= 1; star--) {
      int target = star;
      counts.put(star, feedback.stream().filter(r -> r.getRating() == target).count());
    }
    return counts;
  }
}
