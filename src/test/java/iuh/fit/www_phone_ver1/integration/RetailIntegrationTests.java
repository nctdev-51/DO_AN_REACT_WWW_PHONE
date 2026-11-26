package iuh.fit.www_phone_ver1.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import iuh.fit.www_phone_ver1.dto.cart.Cart;
import iuh.fit.www_phone_ver1.dto.order.*;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.catalog.*;
import iuh.fit.www_phone_ver1.service.order.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "app.payments.expiry-enabled=false")
@ActiveProfiles("demo")
@Transactional
class RetailIntegrationTests {
  @Autowired ComparisonService comparison;
  @Autowired WishlistService wishlist;
  @Autowired WishlistRepository wishes;
  @Autowired ProductRepository products;
  @Autowired ProductVariantRepository variants;
  @Autowired UserRepository users;
  @Autowired RoleRepository roles;
  @Autowired OrderRepository orders;
  @Autowired OrderDetailRepository orderDetails;
  @Autowired PromotionRepository promotions;
  @Autowired StorePolicyRepository policies;
  @Autowired PricingService pricing;
  @Autowired OrderService checkout;
  @Autowired SupportService support;
  @Autowired SupportRequestRepository requests;
  @Autowired WebApplicationContext context;
  @Autowired ProductMediaService mediaService;
  @Autowired ProductMediaRepository media;
  User buyer;
  ProductVariant variant;
  MockMvc mvc;

  @Test
  void productBatchRejectsRowsAlreadyInTheRequestedStatusOrCategory() throws Exception {
    Product product = variant.getProduct();
    product.setStatus(1);
    products.saveAndFlush(product);
    mvc.perform(post("/admin/products/batch").with(user("admin").roles("ADMIN")).with(csrf())
            .param("ids", product.getId().toString()).param("action", "enable"))
        .andExpect(redirectedUrl("/admin/products")).andExpect(flash().attributeExists("errorMsg"));
    mvc.perform(post("/admin/products/batch").with(user("admin").roles("ADMIN")).with(csrf())
            .param("ids", product.getId().toString()).param("action", "category")
            .param("categoryId", product.getCategory().getId().toString()))
        .andExpect(redirectedUrl("/admin/products")).andExpect(flash().attributeExists("errorMsg"));
    assertEquals(1, product.getStatus());
  }

  @Test
  void adminCanFilterDisabledBannersAndEnableWithoutChangingTheirSchedule() throws Exception {
    var repository = context.getBean(CampaignBannerRepository.class);
    LocalDateTime now = LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).withNano(0);
    CampaignBanner disabled = new CampaignBanner();
    disabled.setTitle("Banner quản trị kiểm thử");
    disabled.setTarget("OFFERS");
    disabled.setStartsAt(now.minusDays(1));
    disabled.setEndsAt(now.plusDays(1));
    disabled.setActive(false);
    repository.saveAndFlush(disabled);
    String path = "/admin/banners/" + disabled.getId() + "/enable";

    mvc.perform(get("/admin/banners").with(user("admin").roles("ADMIN"))
            .param("q", disabled.getTitle()).param("state", "off"))
        .andExpect(status().isOk()).andExpect(model().attribute("banners", List.of(disabled)));
    mvc.perform(post(path).with(user("admin").roles("ADMIN")).with(csrf())
            .param("version", Long.toString(disabled.getVersion())).param("state", "off"))
        .andExpect(redirectedUrl("/admin/banners?state=off"));
    assertTrue(disabled.isActive());
    assertEquals(now.minusDays(1), disabled.getStartsAt());
    assertEquals(now.plusDays(1), disabled.getEndsAt());
    mvc.perform(get("/admin/banners").with(user("admin").roles("ADMIN"))
            .param("q", disabled.getTitle()).param("state", "live"))
        .andExpect(status().isOk()).andExpect(model().attribute("banners", List.of(disabled)));
  }

  @Test
  void adminFiltersCampaignsByNameAndInclusiveCalendarDates() throws Exception {
    var repository = context.getBean(CampaignBannerRepository.class);
    CampaignBanner matching = new CampaignBanner();
    matching.setTitle("Galaxy Demo đang chạy");
    matching.setTarget("OFFERS");
    matching.setStartsAt(LocalDateTime.of(2025, 4, 29, 12, 0));
    matching.setEndsAt(LocalDateTime.of(2025, 5, 3, 12, 0));
    repository.saveAndFlush(matching);
    CampaignBanner later = new CampaignBanner();
    later.setTitle("Galaxy Demo quý sau");
    later.setTarget("OFFERS");
    later.setStartsAt(LocalDateTime.of(2025, 5, 4, 0, 0));
    later.setEndsAt(LocalDateTime.of(2025, 5, 8, 0, 0));
    repository.saveAndFlush(later);

    mvc.perform(get("/admin/banners").with(user("admin").roles("ADMIN"))
            .param("q", "gALAxY demO").param("from", "2025-05-01").param("to", "2025-05-03"))
        .andExpect(status().isOk()).andExpect(model().attribute("banners", List.of(matching)));
    mvc.perform(get("/admin/banners").with(user("admin").roles("ADMIN"))
            .param("q", "missing campaign").param("from", "2025-05-01"))
        .andExpect(status().isOk()).andExpect(model().attribute("banners", List.of()));
    mvc.perform(get("/admin/banners").with(user("admin").roles("ADMIN"))
            .param("from", "2025-05-04").param("to", "2025-05-01"))
        .andExpect(status().isOk()).andExpect(model().attributeExists("filterError"));
  }

  @Test
  void analyticsBannerDraftUsesLiveProductOrSearchDestinationWithoutPublishing() throws Exception {
    Product product = variant.getProduct();
    var repo = context.getBean(CampaignBannerRepository.class);
    long existing = repo.count();

    var productDraft = mvc.perform(get("/admin/banners/new").with(user("admin").roles("ADMIN"))
            .param("productId", product.getId().toString()))
        .andExpect(status().isOk()).andReturn();
    var productForm = (iuh.fit.www_phone_ver1.dto.catalog.CampaignBannerForm)
        productDraft.getModelAndView().getModel().get("bannerForm");
    assertEquals(product.getId(), productForm.getProductId());
    assertEquals("PRODUCT", productForm.getTarget());
    assertFalse(productForm.isActive());

    var keywordDraft = mvc.perform(get("/admin/banners/new").with(user("admin").roles("ADMIN"))
            .param("keyword", product.getSku()))
        .andExpect(status().isOk()).andReturn();
    var keywordForm = (iuh.fit.www_phone_ver1.dto.catalog.CampaignBannerForm)
        keywordDraft.getModelAndView().getModel().get("bannerForm");
    assertEquals("SEARCH", keywordForm.getTarget());
    assertEquals(product.getSku(), keywordForm.getSearchKeyword());
    assertEquals(product.getId(), keywordForm.getProductId());
    assertFalse(keywordForm.isActive());
    assertEquals(existing, repo.count());

    keywordForm.setSearchKeyword("ốp + sạc");
    context.getBean(CampaignBannerService.class).save(null, keywordForm, null);
    var saved = repo.findAll().stream().filter(b -> "ốp + sạc".equals(b.getSearchKeyword()))
        .findFirst().orElseThrow();
    assertEquals("/products?keyword=%E1%BB%91p+%2B+s%E1%BA%A1c", saved.getTargetPath());
    assertFalse(saved.isActive());
  }

  @Test
  void adminCanFilterOrdersBySnapshotPhoneDateCodeAndStatus() throws Exception {
    Order order = checkout.placeOrder(cart(), buyer.getEmail(), form());
    String filtersDate = order.getOrderDate().toLocalDate().toString();
    var result = mvc.perform(get("/admin/orders").with(user("admin").roles("ADMIN"))
            .param("code", "#" + order.getId()).param("placedOn", filtersDate)
            .param("status", OrderStatuses.PENDING).param("phone", "090-123"))
        .andExpect(status().isOk()).andReturn();
    assertEquals(List.of(order), result.getModelAndView().getModel().get("orders"));
    mvc.perform(get("/admin/orders").with(user("admin").roles("ADMIN"))
            .param("code", order.getId().toString()).param("phone", "099999"))
        .andExpect(status().isOk()).andExpect(model().attribute("orders", List.of()));
  }

  @Test
  void adminCanSearchAndBatchEditCategoriesWhilePreservingAssignedProducts() throws Exception {
    var categories = context.getBean(CategoryRepository.class);
    Category first = categories.saveAndFlush(Category.builder().name("Test Batch One").build());
    Category second = categories.saveAndFlush(Category.builder().name("Test Batch Two").build());
    mvc.perform(get("/admin/categories").with(user("admin").roles("ADMIN")).param("q", "test batch"))
        .andExpect(status().isOk()).andExpect(model().attribute("categories", List.of(first, second)));
    mvc.perform(post("/admin/categories/batch").with(user("admin").roles("ADMIN")).with(csrf())
            .param("ids", first.getId().toString(), second.getId().toString())
            .param("action", "description").param("description", "Mô tả chung"))
        .andExpect(status().is3xxRedirection());
    assertEquals("Mô tả chung", first.getDescription());
    assertEquals("Mô tả chung", second.getDescription());
    Long categoryInUse = variant.getProduct().getCategory().getId();
    mvc.perform(post("/admin/categories/batch").with(user("admin").roles("ADMIN")).with(csrf())
            .param("ids", first.getId().toString(), categoryInUse.toString()).param("action", "delete"))
        .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("errorMsg"));
    assertFalse(categories.existsById(first.getId()));
    assertTrue(categories.existsById(categoryInUse));
  }

  @Test
  void promotionQuarterFilterIncludesOverlapsAndExcludesAdjacentQuarters() throws Exception {
    Promotion spanning = coupon();
    spanning.setCode("QUARTER-SPANNING");
    spanning.setStartsAt(LocalDateTime.of(2024, 12, 20, 0, 0));
    spanning.setEndsAt(LocalDateTime.of(2025, 1, 10, 0, 0));
    promotions.saveAndFlush(spanning);

    Promotion adjacent = coupon();
    adjacent.setCode("QUARTER-ADJACENT");
    adjacent.setStartsAt(LocalDateTime.of(2025, 4, 1, 0, 0));
    adjacent.setEndsAt(LocalDateTime.of(2025, 4, 15, 0, 0));
    promotions.saveAndFlush(adjacent);

    mvc.perform(get("/admin/promotions").with(user("admin").roles("ADMIN"))
            .param("q", "QUARTER-").param("year", "2025").param("quarter", "1"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("promotions", List.of(spanning)))
        .andExpect(model().attribute("year", 2025))
        .andExpect(model().attribute("quarter", 1));
    mvc.perform(get("/admin/promotions").with(user("admin").roles("ADMIN"))
            .param("year", "2025").param("quarter", "5"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void inventorySuggestionsCountOnlyCompletedOrdersWithinTheDeliveryWindow() {
    variant.setStock(60);
    variants.saveAndFlush(variant);
    Product product = variant.getProduct();
    LocalDateTime now = LocalDateTime.now().withNano(0);
    saveStockTestOrder(product, OrderStatuses.DELIVERED, 4, now.minusDays(45), now.minusDays(2));
    saveStockTestOrder(product, OrderStatuses.DELIVERED, 3, now.minusDays(3), null);
    saveStockTestOrder(product, OrderStatuses.DELIVERED, 20, now.minusDays(31), now.minusDays(31));
    saveStockTestOrder(product, OrderStatuses.PENDING, 30, now.minusDays(1), null);

    var result = orderDetails.findSlowStock(50, 10, now.minusDays(30), now, OrderStatuses.DELIVERED)
        .stream().filter(row -> row.getProductId().equals(product.getId())).findFirst().orElseThrow();
    assertEquals(product.getStock(), result.getStock());
    assertEquals(7, result.getUnitsSold());
    assertEquals(now.minusDays(2), result.getLastSale());

    saveStockTestOrder(product, OrderStatuses.DELIVERED, 3, now.minusDays(1), now.minusDays(1));
    assertTrue(orderDetails.findSlowStock(50, 10, now.minusDays(30), now, OrderStatuses.DELIVERED)
        .stream().noneMatch(row -> row.getProductId().equals(product.getId())));
  }

  private void saveStockTestOrder(Product product, String status, int quantity,
      LocalDateTime orderDate, LocalDateTime deliveredAt) {
    Order order = Order.builder().status(status).orderDate(orderDate).deliveredAt(deliveredAt)
        .totalAmount(BigDecimal.ONE).build();
    order.getOrderDetails().add(OrderDetail.builder().order(order).product(product)
        .quantity(quantity).price(BigDecimal.ONE).build());
    orders.saveAndFlush(order);
  }

  @Test
  void checkoutRejectsPointsWithAnExclusivePromotion() {
    Promotion promotion = coupon();
    promotion.setCombinableWithPoints(false);
    promotions.saveAndFlush(promotion);
    var checkoutForm = form();
    checkoutForm.setPromotionCode(promotion.getCode());
    checkoutForm.setRedeemPoints(1);
    var failure = assertThrows(IllegalArgumentException.class,
        () -> checkout.placeOrder(cart(), buyer.getEmail(), checkoutForm));
    assertEquals("Mã này không áp dụng cùng điểm thưởng.", failure.getMessage());
  }

  ProductMedia photo(Product product, String color, String kind, String path, int position) {
    ProductMedia entry = new ProductMedia();
    entry.setProduct(product);
    entry.setColor(color);
    entry.setKind(kind);
    entry.setPath(path);
    entry.setSortOrder(position);
    return media.saveAndFlush(entry);
  }

  @Test
  void galleryKeepsSelectedColorAndMarksGeneralPhotos() throws Exception {
    Product product = variant.getProduct();
    photo(product, "Test Red", "PHOTO", "/test-red.png", 200);
    photo(product, "Test Blue", "PHOTO", "/test-blue.png", 200);
    photo(product, null, "PHOTO", "/test-general.png", 201);
    var result = mediaService.gallery(product.getId(), "Test Red");
    assertTrue(result.getPhotos().get(0).isColorSpecific());
    assertEquals("/test-red.png", result.getPhotos().get(0).getUrl());
    assertFalse(result.getPhotos().stream().anyMatch(p -> p.getUrl().equals("/test-blue.png")));
    assertTrue(
        result.getPhotos().stream()
            .anyMatch(p -> p.getUrl().equals("/test-general.png") && !p.isColorSpecific()));
    mvc.perform(get("/products/" + product.getId() + "/media").param("color", "Test Red"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.photos[0].colorSpecific").value(true));
    product.setStatus(0);
    products.saveAndFlush(product);
    mvc.perform(get("/products/" + product.getId() + "/media")).andExpect(status().isNotFound());
  }

  @Test
  void spinRequiresEnoughFramesAndNeverUsesAnotherColorsSequence() {
    Product product = variant.getProduct();
    for (int i = 0; i < 11; i++) photo(product, "Spin Red", "SPIN", "/red-" + i + ".png", i);
    assertTrue(mediaService.gallery(product.getId(), "Spin Red").getFrames().isEmpty());
    photo(product, "Spin Red", "SPIN", "/red-11.png", 11);
    assertEquals(12, mediaService.gallery(product.getId(), "Spin Red").getFrames().size());
    assertTrue(mediaService.gallery(product.getId(), "Spin Blue").getFrames().isEmpty());
  }

  @Test
  void mediaUploadValidatesColorCountAndAdministratorAccess() throws Exception {
    var file =
        new org.springframework.mock.web.MockMultipartFile(
            "files",
            "001.png",
            "image/png",
            Base64.getDecoder()
                .decode(
                    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            mediaService.upload(
                variant.getProduct().getId(), "Unknown Color", "PHOTO", List.of(file)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            mediaService.upload(
                variant.getProduct().getId(), variant.getColor(), "SPIN", List.of(file)));
    mediaService.upload(variant.getProduct().getId(), variant.getColor(), "PHOTO", List.of(file));
    assertTrue(
        media.findByProduct_IdOrderBySortOrderAscIdAsc(variant.getProduct().getId()).stream()
            .anyMatch(
                m ->
                    m.getPath().startsWith("/uploads/products/")
                        && m.getColor().equals(variant.getColor())));
    mvc.perform(
            post("/admin/products/" + variant.getProduct().getId() + "/media/1/delete")
                .with(user(buyer.getEmail()).roles("USER"))
                .with(csrf()))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/admin/products/" + variant.getProduct().getId() + "/media/1/delete")
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isForbidden());
  }

  @Test
  void detailOnlyAcceptsVariantFromItsOwnProduct() throws Exception {
    mvc.perform(
            get("/products/" + variant.getProduct().getId())
                .param("variantId", variant.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(model().attribute("selectedVariantId", variant.getId()));
    var foreign =
        variants.findAll().stream()
            .filter(v -> !v.getProduct().getId().equals(variant.getProduct().getId()))
            .findFirst()
            .orElseThrow();
    mvc.perform(
            get("/products/" + variant.getProduct().getId())
                .param("variantId", foreign.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(
            model().attribute("selectedVariantId", org.hamcrest.Matchers.not(foreign.getId())));
  }

  @Test
  void replacingSpinRetainsOtherColorsAndRejectsForeignMediaDeletion() throws Exception {
    var file =
        new org.springframework.mock.web.MockMultipartFile(
            "files",
            "001.png",
            "image/png",
            Base64.getDecoder()
                .decode(
                    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="));
    var batch =
        java.util.stream.IntStream.range(0, 12)
            .mapToObj(i -> (org.springframework.web.multipart.MultipartFile) file)
            .toList();
    Long id = variant.getProduct().getId();
    var other = photo(variant.getProduct(), "Other Spin Color", "SPIN", "/other-spin.png", 100);
    mediaService.upload(id, variant.getColor(), "SPIN", batch);
    var first = mediaService.gallery(id, variant.getColor()).getFrames();
    assertEquals(12, first.size());
    mediaService.upload(id, variant.getColor(), "SPIN", batch);
    var replaced = mediaService.gallery(id, variant.getColor()).getFrames();
    assertEquals(12, replaced.size());
    assertTrue(Collections.disjoint(first, replaced));
    assertTrue(media.existsById(other.getId()));
    assertThrows(IllegalArgumentException.class, () -> mediaService.delete(-1L, other.getId()));
    photo(variant.getProduct(), null, "MODEL", "https://example.com/untrusted/embed", 100);
    assertTrue(
        mediaService.gallery(id, variant.getColor()).getModelUrl() == null
            || mediaService
                .gallery(id, variant.getColor())
                .getModelUrl()
                .startsWith("https://sketchfab.com/models/"));
  }

  @BeforeEach
  void setup() {
    Role role =
        roles.findByName("USER").orElseGet(() -> roles.save(Role.builder().name("USER").build()));
    buyer =
        users.saveAndFlush(
            User.builder()
                .fullName("Retail Test")
                .email(UUID.randomUUID() + "@example.com")
                .password("unused")
                .roles(Set.of(role))
                .build());
    variant = variants.findAll().get(0);
    variant.setStock(20);
    variants.saveAndFlush(variant);
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  Promotion coupon() {
    Promotion p = new Promotion();
    p.setCode("TEST" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    p.setDescription("Integration promotion");
    p.setDiscountType("PERCENT");
    p.setAmount(BigDecimal.TEN);
    p.setMinimumSubtotal(BigDecimal.ZERO);
    p.setMaximumDiscount(BigDecimal.valueOf(200000));
    p.setStartsAt(LocalDateTime.now().minusDays(1));
    p.setEndsAt(LocalDateTime.now().plusDays(1));
    return promotions.saveAndFlush(p);
  }

  CheckoutForm form() {
    CheckoutForm f = new CheckoutForm();
    f.setRecipientName("Retail Buyer");
    f.setPhone("0901234567");
    f.setShippingAddress("12 Test Street");
    return f;
  }

  Cart cart() {
    Cart c = new Cart();
    c.addItem(variant, 1);
    return c;
  }

  Order delivered() {
    Order o = checkout.placeOrder(cart(), buyer.getEmail(), form());
    checkout.updateStatus(o.getId(), OrderStatuses.SHIPPING);
    checkout.updateStatus(o.getId(), OrderStatuses.DELIVERED);
    return o;
  }

  SupportRequestForm request(Order o) {
    SupportRequestForm f = new SupportRequestForm();
    f.setDetailId(o.getOrderDetails().get(0).getId());
    f.setType("WARRANTY");
    f.setQuantity(1);
    f.setReason("Màn hình bị lỗi hiển thị sau khi sử dụng.");
    return f;
  }

  @Test
  void freshStoreSeedsPromotionsFromSharedCatalog() {
    Promotion seeded = promotions.findByCode("PTV200").orElseThrow();
    assertEquals(0, seeded.getAmount().compareTo(BigDecimal.valueOf(200000)));
    assertEquals(0, seeded.getMinimumSubtotal().compareTo(BigDecimal.valueOf(5000000)));
    assertTrue(seeded.getStartsAt().isBefore(LocalDateTime.now()));
    assertTrue(seeded.getEndsAt().isAfter(LocalDateTime.now()));
    assertTrue(promotions.findByCode("PTV5").isPresent());
  }

  @Test
  void comparisonUsesVariantPricesAndKeepsOrder() {
    var all = products.findByStatus(1);
    List<Long> ids = List.of();
    for (int i = 0; i < 3; i++) ids = comparison.select(ids, all.get(i).getId(), null);
    List<Long> full = ids;
    assertThrows(
        IllegalArgumentException.class, () -> comparison.select(full, all.get(3).getId(), null));
    var view = comparison.view(ids);
    assertEquals(3, view.getVariants().size());
    assertTrue(view.getRows().stream().anyMatch(r -> r.isDifferent()));
    assertTrue(view.getSharePath().startsWith("/compare?ids="));
    ProductVariant replacement = all.get(0).getVariants().get(1);
    var changed = comparison.select(ids, all.get(0).getId(), replacement.getId());
    assertEquals(replacement.getId(), changed.get(0));
    assertEquals(3, changed.size());
    assertThrows(
        IllegalArgumentException.class,
        () -> comparison.select(full, all.get(1).getId(), replacement.getId()));
  }

  @Test
  void unavailableModelsAreDroppedButOutOfStockCanBeCompared() {
    variant.setStock(0);
    variants.saveAndFlush(variant);
    assertEquals(1, comparison.view(List.of(variant.getId())).getVariants().size());
    variant.setActive(false);
    variants.saveAndFlush(variant);
    assertTrue(comparison.view(List.of(variant.getId(), Long.MAX_VALUE)).getVariants().isEmpty());
  }

  @Test
  void comparisonMutationsRequireCsrfAndSharedLinksDoNotReplaceSession() throws Exception {
    MockHttpSession session = new MockHttpSession();
    mvc.perform(
            post("/compare/add").session(session).param("variantId", variant.getId().toString()))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/compare/add")
                .session(session)
                .with(csrf())
                .param("variantId", variant.getId().toString()))
        .andExpect(redirectedUrl("/compare"));
    Object before = session.getAttribute("comparisonIds");
    mvc.perform(get("/compare").session(session).param("ids", "broken"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("errorMsg"));
    assertEquals(before, session.getAttribute("comparisonIds"));
    mvc.perform(get("/compare").param("ids", "1,2,3,4"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("errorMsg"));
    mvc.perform(
            post("/compare/remove")
                .session(session)
                .with(csrf())
                .param("variantId", variant.getId().toString()))
        .andExpect(redirectedUrl("/compare"));
    assertEquals(List.of(), session.getAttribute("comparisonIds"));
  }

  @Test
  void wishlistIsIdempotentAndPrivate() throws Exception {
    wishlist.add(buyer.getEmail(), variant.getProduct().getId());
    wishlist.add(buyer.getEmail(), variant.getProduct().getId());
    assertEquals(1, wishlist.list(buyer.getEmail()).size());
    assertTrue(wishlist.list("other@example.com").isEmpty());
    mvc.perform(get("/account/wishlist")).andExpect(status().is3xxRedirection());
    mvc.perform(
            post("/account/wishlist/remove")
                .with(user(buyer.getEmail()))
                .param("productId", variant.getProduct().getId().toString()))
        .andExpect(status().isForbidden());
    wishlist.remove(buyer.getEmail(), variant.getProduct().getId());
    assertTrue(wishlist.list(buyer.getEmail()).isEmpty());
  }

  @Test
  void variantFiltersMustMatchTheSameVariant() {
    Product p =
        products.saveAndFlush(
            Product.builder()
                .name("FilterUnique")
                .brand("Test")
                .price(BigDecimal.valueOf(5000))
                .build());
    for (int i = 0; i < 2; i++) {
      ProductVariant v = new ProductVariant();
      v.setProduct(p);
      v.setSku("FILTER" + UUID.randomUUID());
      v.setColor("Black");
      v.setPrice(BigDecimal.valueOf(5000));
      v.setStorage(i == 0 ? "128GB" : "256GB");
      v.setRam(i == 0 ? "8GB" : "12GB");
      v.setStock(i == 0 ? 0 : 3);
      variants.saveAndFlush(v);
    }
    assertEquals(
        0,
        products
            .searchFiltered(
                "FilterUnique", null, null, null, null, "128GB", "12GB", false, Pageable.unpaged())
            .getTotalElements());
    assertEquals(
        0,
        products
            .searchFiltered(
                "FilterUnique", null, null, null, null, "128GB", "8GB", true, Pageable.unpaged())
            .getTotalElements());
    assertEquals(
        1,
        products
            .searchFiltered(
                "FilterUnique", null, null, null, null, "256GB", "12GB", true, Pageable.unpaged())
            .getTotalElements());
  }

  @Test
  void quoteCalculatesShippingThresholdAndDiscountCap() {
    Promotion p = coupon();
    var small = pricing.preview(BigDecimal.valueOf(1000000), p.getCode(), buyer.getId());
    assertEquals(0, small.getShippingFee().compareTo(BigDecimal.valueOf(30000)));
    assertEquals(0, small.getTotal().compareTo(BigDecimal.valueOf(930000)));
    var large = pricing.preview(BigDecimal.valueOf(3000000), p.getCode(), buyer.getId());
    assertEquals(0, large.getDiscount().compareTo(BigDecimal.valueOf(200000)));
    assertEquals(0, large.getShippingFee().signum());
    p.setEndsAt(LocalDateTime.now().minusSeconds(1));
    promotions.saveAndFlush(p);
    assertThrows(
        IllegalArgumentException.class,
        () -> pricing.preview(BigDecimal.valueOf(1000000), p.getCode(), buyer.getId()));
  }

  @Test
  void couponQuotaAndPerCustomerLimitAreReleasedAfterCancellation() {
    Promotion p = coupon();
    p.setUsageLimit(1);
    promotions.saveAndFlush(p);
    CheckoutForm f = form();
    f.setPromotionCode(p.getCode());
    Order o = checkout.placeOrder(cart(), buyer.getEmail(), f);
    assertEquals(p.getCode(), o.getPromotionCode());
    assertEquals(
        0,
        o.getTotalAmount()
            .compareTo(o.getSubtotal().subtract(o.getDiscountAmount()).add(o.getShippingFee())));
    assertThrows(
        IllegalArgumentException.class,
        () -> pricing.preview(variant.getPrice(), p.getCode(), buyer.getId()));
    checkout.cancelByCustomer(o.getId(), buyer.getEmail(), "Đổi ý mua hàng");
    assertNotNull(pricing.preview(variant.getPrice(), p.getCode(), buyer.getId()));
  }

  @Test
  void orderAmountsAndWarrantyAreSnapshots() {
    Promotion p = coupon();
    CheckoutForm f = form();
    f.setPromotionCode(p.getCode());
    Order o = checkout.placeOrder(cart(), buyer.getEmail(), f);
    BigDecimal total = o.getTotalAmount();
    p.setAmount(BigDecimal.ONE);
    variant.getProduct().setWarrantyMonths(2);
    promotions.saveAndFlush(p);
    assertEquals(total, o.getTotalAmount());
    assertEquals(12, o.getOrderDetails().get(0).getWarrantyMonths());
    checkout.updateStatus(o.getId(), OrderStatuses.SHIPPING);
    checkout.updateStatus(o.getId(), OrderStatuses.DELIVERED);
    assertNotNull(o.getDeliveredAt());
    assertEquals(3, o.getEvents().size());
  }

  @Test
  void supportEnforcesOwnershipAndQuantityAndLifecycle() {
    Order o = delivered();
    var f = request(o);
    assertThrows(
        IllegalArgumentException.class, () -> support.create(o.getId(), "other@example.com", f));
    support.create(o.getId(), buyer.getEmail(), f);
    assertThrows(
        IllegalArgumentException.class, () -> support.create(o.getId(), buyer.getEmail(), f));
    SupportRequest ticket =
        requests.findByOrderDetail_Order_IdOrderByCreatedAtDesc(o.getId()).get(0);
    assertThrows(
        IllegalArgumentException.class,
        () -> support.update(ticket.getId(), "COMPLETED", "Bỏ qua các bước kiểm tra"));
    support.update(ticket.getId(), "APPROVED", "Đã duyệt tiếp nhận bảo hành.");
    support.update(ticket.getId(), "RECEIVED", "Đã nhận thiết bị tại cửa hàng.");
    support.update(ticket.getId(), "COMPLETED", "Đã sửa chữa và bàn giao lại cho khách.");
    assertEquals("COMPLETED", ticket.getStatus());
    support.create(
        o.getId(),
        buyer.getEmail(),
        f); // A completed warranty does not consume purchased quantity forever.
    assertEquals(2, requests.findByOrderDetail_Order_IdOrderByCreatedAtDesc(o.getId()).size());
  }

  @Test
  void returnDeadlineAndWarrantyDeadlineAreEnforced() {
    Order o = delivered();
    var f = request(o);
    f.setType("RETURN");
    o.setDeliveredAt(LocalDateTime.now().minusDays(8));
    orders.saveAndFlush(o);
    assertThrows(
        IllegalArgumentException.class, () -> support.create(o.getId(), buyer.getEmail(), f));
    f.setType("WARRANTY");
    support.create(o.getId(), buyer.getEmail(), f);
    SupportRequest ticket =
        requests.findByOrderDetail_Order_IdOrderByCreatedAtDesc(o.getId()).get(0);
    support.cancel(ticket.getId(), buyer.getEmail());
    o.setDeliveredAt(LocalDateTime.now().minusMonths(13));
    orders.saveAndFlush(o);
    assertThrows(
        IllegalArgumentException.class, () -> support.create(o.getId(), buyer.getEmail(), f));
  }

  @Test
  void cancelledRequestReleasesQuantityAndCannotBeCancelledBySomeoneElse() {
    Order o = delivered();
    var f = request(o);
    support.create(o.getId(), buyer.getEmail(), f);
    SupportRequest ticket =
        requests.findByOrderDetail_Order_IdOrderByCreatedAtDesc(o.getId()).get(0);
    assertThrows(
        IllegalArgumentException.class, () -> support.cancel(ticket.getId(), "other@example.com"));
    support.cancel(ticket.getId(), buyer.getEmail());
    support.create(o.getId(), buyer.getEmail(), f);
    assertEquals("CANCELLED", ticket.getStatus());
  }
}
