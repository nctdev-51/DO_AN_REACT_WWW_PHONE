package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.catalog.ReviewService;
import iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService;
import iuh.fit.www_phone_ver1.service.order.AddressDirectory;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
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
  private final PromotionRepository promotions;
  private final StorePolicyRepository policies;
  private final StoreExperienceService experienceService;
  private final AddressDirectory addressDirectory;

  public ProductController(
      ProductRepository products,
      CategoryRepository categories,
      ReviewRepository reviews,
      ReviewService reviewService,
      ProductVariantRepository variants,
      PromotionRepository promotions,
      StorePolicyRepository policies,
      StoreExperienceService experienceService,
      AddressDirectory addressDirectory) {
    this.products = products;
    this.categories = categories;
    this.reviews = reviews;
    this.reviewService = reviewService;
    this.variants = variants;
    this.promotions = promotions;
    this.policies = policies;
    this.experienceService = experienceService;
    this.addressDirectory = addressDirectory;
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
      HttpSession session,
      Model model) {
    sort =
        java.util.Set.of("newest", "price-asc", "price-desc", "bestselling", "popular", "rating")
                .contains(sort)
            ? sort
            : "newest";
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
    var ids = result.getContent().stream().map(iuh.fit.www_phone_ver1.entity.Product::getId).toList();
    var sales = new java.util.HashMap<Long, Long>();
    if (!ids.isEmpty())
      products.findSalesForProducts(ids).forEach(sale -> sales.put(sale.getProductId(), sale.getSoldQuantity()));
    model.addAttribute("soldQuantities", sales);
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
    model.addAttribute("recentProducts", recentProducts(session, null));
    model.addAttribute("storePolicy", policies.findById(1L).orElseGet(StorePolicy::new));
    return "store/catalog/list";
  }

  @GetMapping(value = "/search/suggestions", produces = "application/json")
  @ResponseBody
  public List<String> suggestions(@RequestParam(required = false) String q) {
    if (q == null || q.isBlank()) return List.of();
    String keyword = q.trim();
    if (keyword.length() < 2) return List.of();
    return products.findNameSuggestions(keyword.substring(0, Math.min(80, keyword.length())), PageRequest.of(0, 8));
  }

  @GetMapping("/popular")
  public String popular() {
    return "redirect:/products?sort=bestselling";
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
              List<ProductVariant> activeVariants =
                  product.getVariants().stream().filter(ProductVariant::isActive).toList();
              model.addAttribute("variants", activeVariants);
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
              model.addAttribute(
                  "selectedVariantId", chosenVariant == null ? null : chosenVariant.getId());
              BigDecimal effectivePrice =
                  chosenVariant == null ? product.getPrice() : chosenVariant.getPrice();
              model.addAttribute(
                  "installment12Monthly",
                  effectivePrice.divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP));
              model.addAttribute(
                  "installment6Monthly",
                  effectivePrice.divide(BigDecimal.valueOf(6), 0, RoundingMode.HALF_UP));
              var accessories =
                  products.findByStatus(1).stream()
                      .filter(
                          p ->
                              "ACCESSORY".equals(p.getKind())
                                  && p.getCompatibleSkus() != null
                                  && Arrays.stream(p.getCompatibleSkus().split(","))
                                      .map(String::trim)
                                      .anyMatch(product.getSku()::equals))
                      .limit(4)
                      .toList();
              model.addAttribute("accessories", accessories);
              var feedback = reviews.findByProduct_IdOrderByCreatedAtDesc(id);
              model.addAttribute("reviews", feedback);
              model.addAttribute(
                  "averageRating",
                  feedback.stream().mapToInt(Review::getRating).average().orElse(0));
              model.addAttribute("ratingCounts", ratingCounts(feedback));
              model.addAttribute(
                  "canReview", user != null && reviewService.canReview(user.getName(), id));
              model.addAttribute("applicablePromotions", applicablePromotions(product));
              model.addAttribute("usageFitReasons", usageFitReasons(product));
              model.addAttribute("recentProducts", recentProducts(session, id));
              model.addAttribute("storePolicy", policies.findById(1L).orElseGet(StorePolicy::new));
              model.addAttribute("experience", experienceService.get());
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

  @GetMapping("/{id}/delivery-estimate")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> deliveryEstimate(
      @PathVariable Long id,
      @RequestParam String provinceCode,
      @RequestParam(required = false) Long variantId) {
    var productOpt = products.findById(id).filter(p -> p.getStatus() == 1);
    if (productOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    var provinceOpt =
        addressDirectory.provinces().stream()
            .filter(p -> p.code().equals(provinceCode))
            .findFirst();
    if (provinceOpt.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(Map.of("error", "Vui lòng chọn tỉnh/thành phố hợp lệ."));
    }
    Product product = productOpt.get();
    BigDecimal itemPrice =
        product.getVariants().stream()
            .filter(v -> v.isActive() && v.getId().equals(variantId))
            .findFirst()
            .map(ProductVariant::getPrice)
            .orElse(product.getPrice());
    StorePolicy policy = policies.findById(1L).orElseGet(StorePolicy::new);
    StoreExperience exp = experienceService.get();
    BigDecimal fee =
        itemPrice.compareTo(policy.getFreeShippingThreshold()) >= 0
            ? BigDecimal.ZERO
            : policy.getShippingFee();
    String pName = provinceOpt.get().name();
    boolean majorHub =
        pName.contains("Hồ Chí Minh") || pName.contains("Hà Nội") || pName.contains("Đà Nẵng");
    int minDays = majorHub ? Math.max(1, exp.getDeliveryMinDays() - 1) : exp.getDeliveryMinDays();
    int maxDays = majorHub ? Math.max(minDays, exp.getDeliveryMaxDays() - 1) : exp.getDeliveryMaxDays();
    var body = new LinkedHashMap<String, Object>();
    body.put("provinceName", pName);
    body.put("fee", fee);
    body.put("freeThreshold", policy.getFreeShippingThreshold());
    body.put("minDays", minDays);
    body.put("maxDays", maxDays);
    body.put("windowLabel", minDays == maxDays ? minDays + " ngày" : minDays + "–" + maxDays + " ngày");
    body.put(
        "note",
        "Thời gian dự kiến theo khung tiêu chuẩn của cửa hàng; phí vận chuyển thực tế xác nhận tại bước Thanh toán.");
    return ResponseEntity.ok(body);
  }

  private List<Product> recentProducts(HttpSession session, Long excludeId) {
    if (session == null) return List.of();
    Object raw = session.getAttribute("recentProducts");
    if (!(raw instanceof List<?> list) || list.isEmpty()) return List.of();
    List<Long> ids =
        list.stream()
            .filter(Long.class::isInstance)
            .map(Long.class::cast)
            .filter(pid -> excludeId == null || !pid.equals(excludeId))
            .limit(4)
            .toList();
    if (ids.isEmpty()) return List.of();
    var found = products.findAllById(ids);
    return ids.stream()
        .flatMap(pid -> found.stream().filter(p -> p.getId().equals(pid) && p.getStatus() == 1))
        .toList();
  }

  private Map<Integer, Long> ratingCounts(List<Review> feedback) {
    Map<Integer, Long> counts = new LinkedHashMap<>();
    for (int star = 5; star >= 1; star--) {
      int target = star;
      counts.put(star, feedback.stream().filter(r -> r.getRating() == target).count());
    }
    return counts;
  }

  private List<Promotion> applicablePromotions(Product product) {
    LocalDateTime now = LocalDateTime.now();
    return promotions
        .findByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByEndsAtAsc(now, now)
        .stream()
        .filter(
            promo -> {
              if (promo.getScopeSkus() != null && !promo.getScopeSkus().isBlank()) {
                return Arrays.stream(promo.getScopeSkus().split(","))
                    .map(String::trim)
                    .anyMatch(s -> s.equals(product.getSku()));
              }
              if (promo.getScopeBrand() != null && !promo.getScopeBrand().isBlank()) {
                return promo.getScopeBrand().equalsIgnoreCase(product.getBrand());
              }
              return true;
            })
        .limit(4)
        .toList();
  }

  private List<String> usageFitReasons(Product product) {
    List<String> list = new ArrayList<>();
    String tags = Objects.toString(product.getUsageTags(), "").toUpperCase(Locale.ROOT);
    if (tags.contains("CAMERA")) list.add("Chụp ảnh & quay video sắc nét");
    if (tags.contains("GAMING")) list.add("Chơi game & xử lý tác vụ nặng mượt mà");
    if (tags.contains("BATTERY")) list.add("Ưu tiên pin bền bỉ cho cả ngày dài");
    if (tags.contains("COMPACT")) list.add("Thiết kế gọn nhẹ, thao tác thoải mái một tay");
    if (tags.contains("EVERYDAY")) list.add("Học tập, làm việc & giải trí hằng ngày");
    if (list.isEmpty()) {
      if (product.getCamera() != null && !product.getCamera().isBlank())
        list.add("Camera " + product.getCamera());
      if (product.getChipset() != null && !product.getChipset().isBlank())
        list.add("Hiệu năng " + product.getChipset());
      if (product.getBattery() != null && !product.getBattery().isBlank())
        list.add("Pin " + product.getBattery());
    }
    return list;
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
