package iuh.fit.www_phone_ver1.service.catalog;

import iuh.fit.www_phone_ver1.config.CacheConfig;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Step 4 — StorefrontCatalogService.
 *
 * <p>Encapsulates all business logic previously scattered in ProductController,
 * enforcing the Controller → Service → Repository architecture rule.
 *
 * <p>ProductController now delegates here instead of injecting repositories directly.
 */
@Service
@Transactional(readOnly = true)
public class StorefrontCatalogService {

  private final ProductRepository products;
  private final CategoryRepository categories;
  private final ProductVariantRepository variants;
  private final PromotionRepository promotions;
  private final StorePolicyRepository policies;
  private final ReviewService reviewService;

  public StorefrontCatalogService(
      ProductRepository products,
      CategoryRepository categories,
      ProductVariantRepository variants,
      PromotionRepository promotions,
      StorePolicyRepository policies,
      ReviewService reviewService) {
    this.products = products;
    this.categories = categories;
    this.variants = variants;
    this.promotions = promotions;
    this.policies = policies;
    this.reviewService = reviewService;
  }

  // ── Catalog list ────────────────────────────────────────────────────────────

  /** Run a paginated, sorted, filtered product query for the catalog list page. */
  public Page<Product> listProducts(CatalogFilter filter, String sort, int page) {
    int safePage = Math.min(Math.max(page, 0), 10_000);
    return products.findAll(filter.specification(sort), PageRequest.of(safePage, 12));
  }

  /** Build the selected-variant map for the product grid. */
  public Map<Long, ProductVariant> selectedVariants(Page<Product> result, CatalogFilter filter) {
    Map<Long, ProductVariant> selected = new HashMap<>();
    result.forEach(p -> selected.put(p.getId(), filter.selected(p)));
    return selected;
  }

  /**
   * Batch-query sold quantities for the current page of products.
   * Returns Map&lt;productId, soldQuantity&gt;.
   */
  public Map<Long, Long> soldQuantitiesForPage(Page<Product> result) {
    List<Long> ids = result.getContent().stream().map(Product::getId).toList();
    if (ids.isEmpty()) return Map.of();
    Map<Long, Long> sales = new HashMap<>();
    products
        .findSalesForProducts(ids)
        .forEach(sale -> sales.put(sale.getProductId(), sale.getSoldQuantity()));
    return sales;
  }

  /** All distinct brands of active products. */
  @Cacheable(value = CacheConfig.CACHE_BRANDS, key = "'all'")
  public List<String> availableBrands() {
    return products.findAvailableBrands();
  }

  /** All distinct colors from active variants. */
  @Cacheable(value = CacheConfig.CACHE_COLORS, key = "'all'")
  public List<String> availableColors() {
    return variants.availableColors();
  }

  /** All distinct storage values from active variants. */
  @Cacheable(value = CacheConfig.CACHE_STORAGE, key = "'all'")
  public List<String> availableStorage() {
    return variants.availableStorage();
  }

  /** All distinct RAM values from active variants. */
  @Cacheable(value = CacheConfig.CACHE_RAM, key = "'all'")
  public List<String> availableRam() {
    return variants.availableRam();
  }

  /** All categories sorted by name. */
  @Cacheable(value = CacheConfig.CACHE_CATEGORIES, key = "'all'")
  public List<Category> allCategories() {
    return categories.findAll(Sort.by("name"));
  }

  // ── Product detail ──────────────────────────────────────────────────────────

  /** Find a product by id only if it is active (status=1), eagerly fetching category & variants. */
  public Optional<Product> findActiveProduct(Long id) {
    return products.findActiveDetailById(id);
  }

  /**
   * Find related products (same category, same status, different id), limit 4.
   */
  public List<Product> relatedProducts(Product product) {
    if (product.getCategory() == null) return List.of();
    return products.findTop4ByCategory_IdAndStatusAndIdNot(
        product.getCategory().getId(), 1, product.getId());
  }

  /**
   * Find accessories compatible with the given product SKU.
   * Step 1 fix: replaced full findByStatus(1) table scan with targeted LIKE query.
   */
  public List<Product> accessoriesFor(String productSku) {
    if (productSku == null || productSku.isBlank()) return List.of();
    return products
        .findAccessoriesCompatibleWith(productSku, PageRequest.of(0, 20))
        .stream()
        .filter(
            p ->
                p.getCompatibleSkus() != null
                    && Arrays.stream(p.getCompatibleSkus().split(","))
                        .map(String::trim)
                        .anyMatch(productSku::equals))
        .limit(4)
        .toList();
  }

  /** All currently active promotions. */
  @Cacheable(value = CacheConfig.CACHE_PROMOTIONS, key = "'active'")
  public List<Promotion> activePromotions() {
    LocalDateTime now = LocalDateTime.now();
    return promotions.findByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByEndsAtAsc(now, now);
  }

  /** Current active promotions applicable to a specific product. */
  public List<Promotion> applicablePromotions(Product product) {
    return activePromotions().stream()
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

  /** Human-readable usage fit reasons derived from usageTags + specs. */
  public List<String> usageFitReasons(Product product) {
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

  /** Store policy (singleton record, id=1). */
  @Cacheable(value = CacheConfig.CACHE_STORE_POLICY, key = "'singleton'")
  public StorePolicy storePolicy() {
    return policies.findById(1L).orElseGet(StorePolicy::new);
  }

  /** Whether the given user can still post a review for the product. */
  public boolean canReview(String username, Long productId) {
    return reviewService.canReview(username, productId);
  }

  /** All reviews for a product, newest first. */
  public java.util.List<iuh.fit.www_phone_ver1.entity.Review> findReviews(Long productId) {
    return reviewService.findByProductId(productId);
  }

  // ── Recently viewed ─────────────────────────────────────────────────────────

  /**
   * Update the recently-viewed list in session and return the previous list
   * (excluding the current product).
   */
  public List<Product> trackAndGetRecentProducts(HttpSession session, Long currentProductId) {
    if (session == null) return List.of();

    // Update session
    synchronized (session) {
      @SuppressWarnings("unchecked")
      var previous = (List<Long>) session.getAttribute("recentProducts");
      var recent = new ArrayList<Long>(previous == null ? List.of() : previous);
      recent.remove(currentProductId);
      recent.add(0, currentProductId);
      session.setAttribute("recentProducts", List.copyOf(recent.subList(0, Math.min(10, recent.size()))));
    }

    return recentProducts(session, currentProductId);
  }

  /** Get recently viewed products from session, excluding one id (pass null to include all). */
  public List<Product> recentProducts(HttpSession session, Long excludeId) {
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

  // ── Search suggestions ──────────────────────────────────────────────────────

  /** Name suggestions for autocomplete (max 8 results). */
  public List<String> nameSuggestions(String q) {
    if (q == null || q.isBlank() || q.trim().length() < 2) return List.of();
    String keyword = q.trim();
    return products.findNameSuggestions(
        keyword.substring(0, Math.min(80, keyword.length())), PageRequest.of(0, 8));
  }
}
