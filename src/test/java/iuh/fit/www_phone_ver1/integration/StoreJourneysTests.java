package iuh.fit.www_phone_ver1.integration;

import static iuh.fit.www_phone_ver1.service.order.OrderStatuses.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import iuh.fit.www_phone_ver1.dto.cart.*;
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
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "app.payments.expiry-enabled=false")
@ActiveProfiles("demo")
@Transactional
class StoreJourneysTests {
  @Autowired ProductRepository products;
  @Autowired ProductVariantRepository variants;
  @Autowired UserRepository users;
  @Autowired OrderRepository orders;
  @Autowired OrderService checkout;
  @Autowired LoyaltyService loyalty;
  @Autowired LoyaltyRepository ledger;
  @Autowired RefundService refunds;
  @Autowired RefundRepository refundRows;
  @Autowired StoreExperienceRepository settings;
  @Autowired StoreExperienceService content;
  @Autowired ConsultationService consultations;
  @Autowired ConsultationRepository requests;
  @Autowired PromotionRepository promotions;
  @Autowired PricingService pricing;
  @Autowired SupportService support;
  @Autowired SupportRequestRepository tickets;
  @Autowired DeliveryService delivery;
  @Autowired WebApplicationContext context;
  User buyer;
  Product phone;
  ProductVariant base;
  MockMvc mvc;

  BigDecimal money(long n) {
    return BigDecimal.valueOf(n);
  }

  @BeforeEach
  void setup() {
    buyer = new User();
    buyer.setEmail("journey-" + UUID.randomUUID() + "@example.test");
    buyer.setFullName("Khách thử");
    buyer.setPassword("unused");
    buyer.setStatus(1);
    users.saveAndFlush(buyer);
    phone = product("UXTEST Phone", 1000000);
    base = variant(phone, "256GB", 1000000, 100);
    var rule = settings.findById(1L).orElseThrow();
    rule.setLoyaltyEnabled(true);
    rule.setEarnEvery(money(100000));
    rule.setPointValue(money(1000));
    rule.setRedeemPercent(10);
    settings.saveAndFlush(rule);
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  Product product(String name, long price) {
    return products.saveAndFlush(
        Product.builder()
            .name(name)
            .sku("TEST-" + UUID.randomUUID())
            .brand("UXTEST")
            .price(money(price))
            .status(1)
            .build());
  }

  ProductVariant variant(Product p, String storage, long price, int stock) {
    var v = new ProductVariant();
    v.setProduct(p);
    v.setSku("VAR-" + UUID.randomUUID());
    v.setStorage(storage);
    v.setColor("Đen");
    v.setRam("8GB");
    v.setPrice(money(price));
    v.setStock(stock);
    variants.saveAndFlush(v);
    p.getVariants().add(v);
    return v;
  }

  CheckoutForm form() {
    var f = new CheckoutForm();
    f.setRecipientName("Khách thử");
    f.setPhone("0901234567");
    f.setShippingAddress("12 Đường thử, phường thử, TP.HCM");
    return f;
  }

  Cart cart() {
    var c = new Cart();
    c.addItem(base, 1);
    return c;
  }

  Order order() {
    return checkout.placeOrder(cart(), buyer.getEmail(), form());
  }

  Order delivered() {
    var o = order();
    checkout.updateStatus(o.getId(), SHIPPING);
    checkout.updateStatus(o.getId(), DELIVERED);
    return o;
  }

  @Test
  void priceFiltersAndOrderingUseMatchingVariant() {
    var large = variant(phone, "512GB", 3000000, 10);
    var p2 = product("UXTEST Other", 2000000);
    variant(p2, "256GB", 2000000, 10);
    variant(p2, "512GB", 2500000, 10);
    var filter =
        new CatalogFilter(
            "UXTEST", null, null, money(2400000), money(3500000), "512GB", null, true);
    var result = products.findAll(filter.specification("price-asc"), PageRequest.of(0, 12));
    assertEquals(List.of(p2.getId(), phone.getId()), result.stream().map(Product::getId).toList());
    assertEquals(large.getId(), filter.selected(phone).getId());
    assertEquals(
        phone.getId(),
        products
            .findAll(filter.specification("price-desc"), PageRequest.of(0, 12))
            .getContent()
            .get(0)
            .getId());
  }

  @Test
  void cannotMixStoragePriceAndStockFromDifferentVariants() {
    variant(phone, "512GB", 3000000, 0);
    assertFalse(
        products
            .searchFiltered(
                "UXTEST",
                null,
                null,
                null,
                money(1500000),
                "512GB",
                null,
                false,
                Pageable.unpaged())
            .stream()
            .anyMatch(p -> p.getId().equals(phone.getId())));
    assertFalse(
        products
            .searchFiltered(
                "UXTEST", null, null, null, null, "512GB", null, true, Pageable.unpaged())
            .stream()
            .anyMatch(p -> p.getId().equals(phone.getId())));
  }

  @Test
  void searchMatchesEveryTokenAndDoesNotTreatWildcardsAsEverything() {
    assertTrue(
        products.search("phone UXTEST", null, null, null, null, Pageable.unpaged()).stream()
            .anyMatch(p -> p.getId().equals(phone.getId())));
    assertTrue(products.search("%_", null, null, null, null, Pageable.unpaged()).isEmpty());
  }

  @Test
  void controllerSelectsFilteredVariant() throws Exception {
    var selected = variant(phone, "512GB", 3000000, 10);
    var response =
        mvc.perform(get("/products").param("keyword", "UXTEST").param("storage", "512GB"))
            .andExpect(status().isOk())
            .andReturn();
    @SuppressWarnings("unchecked")
    var choices =
        (Map<Long, ProductVariant>) response.getModelAndView().getModel().get("selectedVariants");
    assertEquals(selected.getId(), choices.get(phone.getId()).getId());
  }

  @Test
  void earnOnceOnlyAfterDeliveryAndUsePurchaseRuleSnapshot() {
    var o = order();
    assertEquals(0, loyalty.balance(buyer.getId()));
    var rule = settings.findById(1L).orElseThrow();
    rule.setEarnEvery(money(200000));
    settings.saveAndFlush(rule);
    checkout.updateStatus(o.getId(), SHIPPING);
    assertEquals(0, loyalty.balance(buyer.getId()));
    checkout.updateStatus(o.getId(), DELIVERED);
    checkout.updateStatus(o.getId(), DELIVERED);
    assertEquals(10, loyalty.balance(buyer.getId()));
  }

  @Test
  void spendingReservesPointsAndCancelRestoresOnce() {
    delivered();
    var f = form();
    f.setRedeemPoints(5);
    var o = checkout.placeOrder(cart(), buyer.getEmail(), f);
    assertEquals(5, loyalty.balance(buyer.getId()));
    assertEquals(0, o.getPointsDiscount().compareTo(money(5000)));
    checkout.cancelByCustomer(o.getId(), buyer.getEmail(), "Đổi nhu cầu");
    assertEquals(10, loyalty.balance(buyer.getId()));
    assertThrows(
        IllegalArgumentException.class,
        () -> checkout.cancelByCustomer(o.getId(), buyer.getEmail(), "Lặp"));
    assertEquals(10, loyalty.balance(buyer.getId()));
  }

  @Test
  void rejectsOverspendingAndCapViolation() {
    delivered();
    assertThrows(
        IllegalArgumentException.class, () -> loyalty.preview(buyer.getId(), 11, money(1000000)));
    assertThrows(
        IllegalArgumentException.class, () -> loyalty.preview(buyer.getId(), 10, money(50000)));
    assertThrows(
        IllegalArgumentException.class, () -> loyalty.preview(buyer.getId(), -1, money(1000000)));
  }

  @Test
  void disabledProgramRejectsRedemption() {
    delivered();
    var rule = settings.findById(1L).orElseThrow();
    rule.setLoyaltyEnabled(false);
    settings.saveAndFlush(rule);
    assertThrows(
        IllegalArgumentException.class, () -> loyalty.preview(buyer.getId(), 1, money(1000000)));
  }

  @Test
  void partialAndFullRefundAdjustPointsAndCannotExceedPaidAmount() {
    var o = delivered();
    var half = o.getTotalAmount().divide(BigDecimal.valueOf(2));
    refunds.request(o.getId(), half, "Trả hàng đã kiểm tra");
    var r = refundRows.findByOrder_IdOrderByIdDesc(o.getId()).get(0);
    refunds.settle(r.getId(), "COMPLETED", "BANK-REF-1");
    assertEquals("PARTIALLY_REFUNDED", o.getPaymentStatus());
    assertEquals(5, loyalty.balance(buyer.getId()));
    assertThrows(
        IllegalArgumentException.class,
        () -> refunds.request(o.getId(), money(600000), "Vượt tiền"));
    refunds.request(o.getId(), half, "Hoàn phần còn lại");
    var second = refundRows.findByOrder_IdOrderByIdDesc(o.getId()).get(0);
    refunds.settle(second.getId(), "COMPLETED", "BANK-REF-2");
    assertEquals("REFUNDED", o.getPaymentStatus());
    assertEquals(0, loyalty.balance(buyer.getId()));
    assertThrows(
        IllegalArgumentException.class,
        () -> refunds.settle(second.getId(), "COMPLETED", "REPLAY"));
  }

  @Test
  void rejectedRefundReleasesReservedAmount() {
    var o = delivered();
    refunds.request(o.getId(), money(1000000), "Kiểm tra giao dịch");
    var r = refundRows.findByOrder_IdOrderByIdDesc(o.getId()).get(0);
    refunds.settle(r.getId(), "REJECTED", "Sai yêu cầu");
    refunds.request(o.getId(), money(1000000), "Yêu cầu đã xác minh");
    assertEquals(10, loyalty.balance(buyer.getId()));
  }

  @Test
  void paidCancellationDoesNotRestoreSpentPointsAgainOnRefund() {
    delivered();
    var f = form();
    f.setRedeemPoints(5);
    var o = checkout.placeOrder(cart(), buyer.getEmail(), f);
    o.setPaymentMethod("VNPAY");
    o.setPaymentStatus("PAID");
    orders.saveAndFlush(o);
    assertThrows(
        IllegalArgumentException.class,
        () -> checkout.cancelByCustomer(o.getId(), buyer.getEmail(), "Không tự hủy tiền đã thu"));
    checkout.updateStatus(o.getId(), CANCELLED);
    assertEquals(10, loyalty.balance(buyer.getId()));
    refunds.request(o.getId(), o.getTotalAmount(), "Hủy đơn đã thanh toán");
    var r = refundRows.findByOrder_IdOrderByIdDesc(o.getId()).get(0);
    refunds.settle(r.getId(), "COMPLETED", "BANK-CANCEL");
    assertEquals(10, loyalty.balance(buyer.getId()));
  }

  Promotion promotion() {
    var p = new Promotion();
    p.setCode(("UX" + UUID.randomUUID().toString().substring(0, 8)).toUpperCase(Locale.ROOT));
    p.setDescription("Test coupon");
    p.setAmount(money(50));
    p.setDiscountType("PERCENT");
    p.setMinimumSubtotal(BigDecimal.ZERO);
    p.setStartsAt(LocalDateTime.now().minusDays(1));
    p.setEndsAt(LocalDateTime.now().plusDays(1));
    return p;
  }

  @Test
  void scopedCouponDiscountsOnlyEligibleItemsAndRequiresCombo() {
    var accessory = product("UXTEST Accessory", 200000);
    var av = variant(accessory, "Standard", 200000, 10);
    var c = cart();
    c.addItem(av, 1);
    var p = promotion();
    p.setScopeSkus(accessory.getSku());
    p.setRequiredSkus(phone.getSku());
    promotions.saveAndFlush(p);
    assertEquals(
        0,
        pricing
            .preview(c.getTotalAmount(), p.getCode(), buyer.getId(), c.getItemList())
            .getDiscount()
            .compareTo(money(100000)));
    c.removeItem(base.getId());
    assertThrows(
        IllegalArgumentException.class,
        () -> pricing.preview(c.getTotalAmount(), p.getCode(), buyer.getId(), c.getItemList()));
  }

  @Test
  void scopedCouponMinimumUsesEligibleSubtotal() {
    var p = promotion();
    p.setScopeBrand("Other Brand");
    promotions.saveAndFlush(p);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            pricing.preview(
                cart().getTotalAmount(), p.getCode(), buyer.getId(), cart().getItemList()));
  }

  ConsultationForm consultation() {
    var f = new ConsultationForm();
    f.setProductId(phone.getId());
    f.setContactName("Khách thử");
    f.setPhone("0901234567");
    f.setMessage("Muốn tư vấn trả trước 30 phần trăm");
    return f;
  }

  @Test
  void consultationHasControlledLifecycleAndOwnership() {
    consultations.create(buyer.getEmail(), consultation());
    var r = requests.findByUser_EmailOrderByIdDesc(buyer.getEmail()).get(0);
    assertThrows(
        IllegalArgumentException.class,
        () -> consultations.cancel(r.getId(), r.getVersion(), "other@example.test"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            consultations.update(
                r.getId(), r.getVersion(), "ACCEPTED", "Duyệt ngay", money(500000)));
    consultations.update(r.getId(), r.getVersion(), "CONTACTED", "Đã trao đổi nhu cầu", null);
    consultations.update(
        r.getId(), r.getVersion(), "QUOTED", "Phương án chờ khách xác nhận", money(500000));
    consultations.update(
        r.getId(), r.getVersion(), "ACCEPTED", "Đã xác nhận phương án với khách", money(500000));
    assertEquals("ACCEPTED", r.getStatus());
    assertThrows(
        IllegalArgumentException.class,
        () -> consultations.cancel(r.getId(), r.getVersion(), buyer.getEmail()));
  }

  @Test
  void consultationLimitsOpenRequests() {
    for (int i = 0; i < 5; i++) consultations.create(buyer.getEmail(), consultation());
    assertThrows(
        IllegalArgumentException.class,
        () -> consultations.create(buyer.getEmail(), consultation()));
  }

  @Test
  void returnInspectionRestocksOnlyOnce() {
    var o = delivered();
    var f = new SupportRequestForm();
    f.setDetailId(o.getOrderDetails().get(0).getId());
    f.setType("RETURN");
    f.setQuantity(1);
    f.setReason("Máy cần kiểm tra đổi trả");
    support.create(o.getId(), buyer.getEmail(), f);
    var ticket =
        tickets.findAll().stream()
            .filter(t -> t.getOrderDetail().getOrder().getId().equals(o.getId()))
            .findFirst()
            .orElseThrow();
    support.update(ticket.getId(), "APPROVED", "Đã duyệt tiếp nhận thiết bị");
    support.update(ticket.getId(), "RECEIVED", "Đã nhận thiết bị về cửa hàng");
    int stock = base.getStock();
    support.inspect(ticket.getId(), true, "Kiểm tra đủ điều kiện nhập tồn");
    assertEquals(stock + 1, base.getStock());
    assertThrows(
        IllegalArgumentException.class,
        () -> support.inspect(ticket.getId(), true, "Lặp lại kiểm tra thiết bị"));
    assertEquals(stock + 1, base.getStock());
  }

  @Test
  void deliverySnapshotsAndTrackingAreNotEditableAfterDelivery() {
    var o = order();
    assertNotNull(o.getEstimatedDeliveryFrom());
    delivery.update(o.getId(), "GHN", "TEST123", "Đã bàn giao vận chuyển");
    assertEquals("TEST123", o.getTrackingCode());
    checkout.updateStatus(o.getId(), SHIPPING);
    checkout.updateStatus(o.getId(), DELIVERED);
    assertThrows(
        IllegalArgumentException.class,
        () -> delivery.update(o.getId(), "GHN", "OTHER123", "Không sửa đơn đã giao"));
  }

  @Test
  void newAdminAndPrivateRoutesRequireCorrectRole() throws Exception {
    mvc.perform(get("/account/points")).andExpect(status().is3xxRedirection());
    mvc.perform(get("/admin/experience").with(user(buyer.getEmail()).roles("USER")))
        .andExpect(status().isForbidden());
    mvc.perform(post("/account/consultations").with(user(buyer.getEmail())))
        .andExpect(status().isForbidden());
    mvc.perform(get("/search/suggestions").param("q", "UXTEST"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").exists());
  }

  @Test
  void suggestionsPrioritizeExactNameOverChipsetMatches() throws Exception {
    var target = product("UXTEST 15", 1000000);
    variant(target, "256GB", 1000000, 10);
    for (int i = 0; i < 7; i++) {
      var p = product("UXTEST Other " + i, 1000000);
      p.setChipset("A15");
      products.saveAndFlush(p);
      variant(p, "256GB", 1000000, 10);
    }
    mvc.perform(get("/search/suggestions").param("q", "UXTEST 15"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(target.getId()));
  }

  @Test
  void invalidContentCannotReferenceMissingProducts() {
    var form = new StoreExperience();
    form.setVersion(settings.findById(1L).orElseThrow().getVersion());
    form.setHeroSku("MISSING-SKU");
    assertThrows(IllegalArgumentException.class, () -> content.save(form));
  }
}
