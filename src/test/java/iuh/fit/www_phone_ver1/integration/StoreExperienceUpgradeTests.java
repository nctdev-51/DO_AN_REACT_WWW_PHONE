package iuh.fit.www_phone_ver1.integration;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import iuh.fit.www_phone_ver1.dto.cart.Cart;
import iuh.fit.www_phone_ver1.dto.catalog.AdvisorForm;
import iuh.fit.www_phone_ver1.dto.catalog.AdvisorRecommendation;
import iuh.fit.www_phone_ver1.dto.order.CheckoutForm;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.catalog.PhoneAdvisorService;
import iuh.fit.www_phone_ver1.service.mail.MailService;
import iuh.fit.www_phone_ver1.service.order.*;
import iuh.fit.www_phone_ver1.service.payment.VnPayService;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(
    properties = {
      "app.payments.expiry-enabled=false",
      "app.vnpay.enabled=true",
      "app.vnpay.terminal=TESTTERM",
      "app.vnpay.secret=upgrade-secret",
      "app.vnpay.payment-url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
      "app.vnpay.return-url=http://localhost:8080/payments/vnpay/return"
    })
@ActiveProfiles("demo")
@Transactional
class StoreExperienceUpgradeTests {

  @Autowired WebApplicationContext context;
  @Autowired PhoneAdvisorService advisor;
  @Autowired ProductRepository products;
  @Autowired ProductVariantRepository variants;
  @Autowired UserRepository users;
  @Autowired RoleRepository roles;
  @Autowired OrderRepository orders;
  @Autowired AddressRepository addresses;
  @Autowired OrderService orderService;
  @Autowired VnPayService vnPayService;
  @Autowired AddressDirectory addressDirectory;
  @Autowired PasswordEncoder passwords;

  MockMvc mvc;
  User buyer;
  ProductVariant variant;

  @BeforeEach
  void setup() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    Role role =
        roles.findByName("USER").orElseGet(() -> roles.save(Role.builder().name("USER").build()));
    buyer =
        users.saveAndFlush(
            User.builder()
                .fullName("Upgrade Customer")
                .email("upgrade-" + UUID.randomUUID() + "@example.com")
                .password(passwords.encode("Pass1234!"))
                .phone("0901234567")
                .status(1)
                .roles(Set.of(role))
                .build());
    variant =
        variants.findAll().stream()
            .filter(v -> v.isActive() && v.getStock() > 0 && "PHONE".equals(v.getProduct().getKind()))
            .findFirst()
            .orElseThrow();
    variant.setStock(10);
    variants.saveAndFlush(variant);
  }

  private Cart cart() {
    Cart c = new Cart();
    c.addItem(variant, 1);
    return c;
  }

  private CheckoutForm checkoutForm(String method) {
    CheckoutForm f = new CheckoutForm();
    f.setRecipientName("Upgrade Buyer");
    f.setPhone("0901234567");
    f.setShippingAddress("120 Nguyen Hue");
    f.setPaymentMethod(method);
    return f;
  }

  private Map<String, String> signParams(Map<String, String> raw) throws Exception {
    TreeMap<String, String> sorted = new TreeMap<>(raw);
    StringJoiner joined = new StringJoiner("&");
    sorted.forEach(
        (k, v) ->
            joined.add(
                URLEncoder.encode(k, StandardCharsets.UTF_8)
                    + "="
                    + URLEncoder.encode(v, StandardCharsets.UTF_8)));
    Mac mac = Mac.getInstance("HmacSHA512");
    mac.init(new SecretKeySpec("upgrade-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
    sorted.put(
        "vnp_SecureHash",
        HexFormat.of().formatHex(mac.doFinal(joined.toString().getBytes(StandardCharsets.UTF_8))));
    return sorted;
  }

  @Test
  void phoneAdvisorScoresWithinBudgetStockAndValidatesBounds() throws Exception {
    AdvisorForm form =
        advisor.normalize(
            BigDecimal.valueOf(30_000_000), "CAMERA", "CAMERA", "ANY", null, null, 4);
    List<AdvisorRecommendation> recs = advisor.recommend(form);
    assertFalse(recs.isEmpty());
    assertTrue(recs.size() <= 6);
    assertTrue(
        recs.stream()
            .allMatch(
                r ->
                    "PHONE".equals(r.getProduct().getKind())
                        && r.getRecommendedVariant().getStock() > 0
                        && r.getRecommendedVariant().getPrice().compareTo(form.getBudget()) <= 0
                        && !r.getReasons().isEmpty()));

    assertThrows(
        IllegalArgumentException.class,
        () -> advisor.normalize(BigDecimal.valueOf(500_000), "EVERYDAY", null, null, null, null, 1));

    mvc.perform(get("/choose").param("budget", "500000"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("errorMsg"));

    mvc.perform(
            get("/choose")
                .param("budget", "30000000")
                .param("need", "GAMING")
                .param("priority", "PERFORMANCE"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("recommendations"));
  }

  @Test
  void detailDeliveryEstimateAndSafeLoginRedirect() throws Exception {
    String provinceCode = addressDirectory.provinces().get(0).code();
    mvc.perform(
            get("/products/" + variant.getProduct().getId() + "/delivery-estimate")
                .param("provinceCode", provinceCode)
                .param("variantId", variant.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.provinceName").isNotEmpty())
        .andExpect(jsonPath("$.windowLabel").isNotEmpty())
        .andExpect(jsonPath("$.fee").isNumber());

    mvc.perform(
            get("/products/" + variant.getProduct().getId() + "/delivery-estimate")
                .param("provinceCode", "999999"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").isNotEmpty());

    mvc.perform(get("/login").param("redirect", "//evil.example.com"))
        .andExpect(status().isOk())
        .andExpect(model().attributeDoesNotExist("redirectTarget"));

    String target = "/products/" + variant.getProduct().getId();
    mvc.perform(get("/login").param("redirect", target))
        .andExpect(status().isOk())
        .andExpect(model().attribute("redirectTarget", target));

    mvc.perform(
            post("/login")
                .with(csrf())
                .param("username", buyer.getEmail())
                .param("password", "Pass1234!")
                .param("redirect", target))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(target));
  }

  @Test
  void checkoutIdempotencyTokenPreventsDuplicateOrderAndInlineAddressModalWorks()
      throws Exception {
    var province = addressDirectory.provinces().get(0);
    var ward = province.wards().get(0);

    mvc.perform(
            post("/checkout/addresses")
                .with(user(buyer.getEmail()))
                .with(csrf())
                .param("recipientName", "Modal Receiver")
                .param("phone", "0909876543")
                .param("line", "45 Le Loi")
                .param("addressMode", "structured")
                .param("provinceCode", province.code())
                .param("wardCode", ward.code())
                .param("defaultAddress", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.recipientName").value("Modal Receiver"))
        .andExpect(jsonPath("$.provinceCode").value(province.code()));
    assertEquals(1, addresses.findByUser_EmailOrderByDefaultAddressDescIdDesc(buyer.getEmail()).size());

    mvc.perform(
            post("/checkout/addresses")
                .with(user(buyer.getEmail()))
                .with(csrf())
                .param("recipientName", "Bad Phone")
                .param("phone", "123")
                .param("line", "45 Le Loi"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").isNotEmpty());

    MockHttpSession session = new MockHttpSession();
    session.setAttribute("cart", cart());
    mvc.perform(get("/checkout").session(session).with(user(buyer.getEmail())))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("checkoutToken"));

    String token = (String) session.getAttribute("checkoutToken");
    assertNotNull(token);
    long beforeOrders = orders.count();

    mvc.perform(
            post("/checkout/place-order")
                .session(session)
                .with(user(buyer.getEmail()))
                .with(csrf())
                .param("checkoutToken", token)
                .param("recipientName", "Upgrade Buyer")
                .param("phone", "0901234567")
                .param("shippingAddress", "120 Nguyen Hue")
                .param("paymentMethod", "COD"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/checkout/success/*"));

    assertEquals(beforeOrders + 1, orders.count());
    Long createdOrderId = (Long) session.getAttribute("lastSubmittedOrderId");
    assertNotNull(createdOrderId);

    mvc.perform(
            post("/checkout/place-order")
                .session(session)
                .with(user(buyer.getEmail()))
                .with(csrf())
                .param("checkoutToken", token)
                .param("recipientName", "Upgrade Buyer")
                .param("phone", "0901234567")
                .param("shippingAddress", "120 Nguyen Hue")
                .param("paymentMethod", "COD"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/orders/" + createdOrderId));

    assertEquals(beforeOrders + 1, orders.count());
  }

  @Test
  void detailedStockErrorNamesVariantAndRemainingStock() {
    variant.setStock(5);
    variants.saveAndFlush(variant);
    Cart large = new Cart();
    large.addItem(variant, 2);
    variant.setStock(1);
    variants.saveAndFlush(variant);
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> orderService.placeOrder(large, buyer.getEmail(), checkoutForm("COD")));
    assertTrue(ex.getMessage().contains(variant.getProduct().getName()));
    assertTrue(ex.getMessage().contains("1"));
  }

  @Test
  void vnPayFailureKeepsHoldAllowsRetryAndSwitchCodAndBlocksReturnEscalation() throws Exception {
    Order order = orderService.placeOrder(cart(), buyer.getEmail(), checkoutForm("VNPAY"));
    assertEquals(9, variant.getStock());
    String firstRef = order.getPaymentReference();

    Map<String, String> failedIpn =
        signParams(
            Map.of(
                "vnp_TmnCode",
                "TESTTERM",
                "vnp_TxnRef",
                firstRef,
                "vnp_Amount",
                order.getTotalAmount().multiply(BigDecimal.valueOf(100)).toPlainString(),
                "vnp_ResponseCode",
                "24",
                "vnp_TransactionStatus",
                "02",
                "vnp_TransactionNo",
                "FAIL999"));
    assertEquals("00", vnPayService.handleNotification(failedIpn).get("RspCode"));
    assertEquals("FAILED", order.getPaymentStatus());
    assertEquals(OrderStatuses.PENDING, order.getStatus());
    assertEquals(9, variant.getStock());

    // Simulate client returning from gateway with vnp_ResponseCode=00 WITHOUT IPN: must NOT become PAID.
    Map<String, String> returnSuccessAttempt =
        signParams(
            Map.of(
                "vnp_TmnCode",
                "TESTTERM",
                "vnp_TxnRef",
                firstRef,
                "vnp_Amount",
                order.getTotalAmount().multiply(BigDecimal.valueOf(100)).toPlainString(),
                "vnp_ResponseCode",
                "00"));
    mvc.perform(
            get("/payments/vnpay/return")
                .with(user(buyer.getEmail()))
                .param("vnp_TxnRef", firstRef)
                .param("vnp_TmnCode", "TESTTERM")
                .param("vnp_Amount", returnSuccessAttempt.get("vnp_Amount"))
                .param("vnp_ResponseCode", "00")
                .param("vnp_SecureHash", returnSuccessAttempt.get("vnp_SecureHash")))
        .andExpect(status().isOk());
    assertNotEquals("PAID", orders.findById(order.getId()).orElseThrow().getPaymentStatus());

    mvc.perform(post("/orders/" + order.getId() + "/retry-vnpay").with(user(buyer.getEmail())).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", containsString("sandbox.vnpayment.vn")));
    Order retried = orders.findById(order.getId()).orElseThrow();
    assertNotEquals(firstRef, retried.getPaymentReference());
    assertEquals("UNPAID", retried.getPaymentStatus());

    mvc.perform(post("/orders/" + order.getId() + "/switch-cod").with(user(buyer.getEmail())).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/orders/" + order.getId()));
    Order switched = orders.findById(order.getId()).orElseThrow();
    assertEquals("COD", switched.getPaymentMethod());
    assertEquals("UNPAID", switched.getPaymentStatus());
    assertEquals(9, variant.getStock());

    // Expired FAILED VNPay order rejects retry/switch and expires cleanly
    Order expired = orderService.placeOrder(cart(), buyer.getEmail(), checkoutForm("VNPAY"));
    expired.setPaymentStatus("FAILED");
    expired.setOrderDate(LocalDateTime.now().minusMinutes(20));
    orders.saveAndFlush(expired);
    assertThrows(
        IllegalArgumentException.class,
        () -> orderService.retryVnPay(expired.getId(), buyer.getEmail()));
    assertThrows(
        IllegalArgumentException.class,
        () -> orderService.switchToCod(expired.getId(), buyer.getEmail()));
    orderService.expireUnpaidOrders();
    assertEquals(OrderStatuses.CANCELLED, orders.findById(expired.getId()).orElseThrow().getStatus());
  }

  @Test
  void myDevicesWarrantyTrackingAndMilestoneOutboxEmails(@TempDir Path tempOutbox) throws Exception {
    Order order = orderService.placeOrder(cart(), buyer.getEmail(), checkoutForm("COD"));
    orderService.updateStatus(order.getId(), OrderStatuses.SHIPPING);
    orderService.updateStatus(order.getId(), OrderStatuses.DELIVERED);

    mvc.perform(get("/account/devices").with(user(buyer.getEmail())))
        .andExpect(status().isOk())
        .andExpect(view().name("account/devices"))
        .andExpect(model().attribute("devices", hasSize(1)));

    MailService mail =
        new MailService(new JavaMailSenderImpl(), false, "noreply@ptvmobile.local", tempOutbox.toString());
    Refund refund = new Refund();
    refund.setAmount(BigDecimal.valueOf(500000));
    refund.setStatus("COMPLETED");
    refund.setSettlementReference("REF-2026-01");
    mail.sendPaymentSuccess(buyer.getEmail(), order);
    mail.sendOrderStatusUpdate(buyer.getEmail(), order);
    mail.sendRefundUpdate(buyer.getEmail(), order, refund);
    try (var files = Files.list(tempOutbox)) {
      assertEquals(3, files.filter(p -> p.toString().endsWith(".eml")).count());
    }
  }

  @Test
  void compareApiAddsRemovesAndClearsWithoutRedirect() throws Exception {
    MockHttpSession session = new MockHttpSession();
    mvc.perform(
            post("/compare/api/add")
                .session(session)
                .with(csrf())
                .param("variantId", variant.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.count").value(1))
        .andExpect(jsonPath("$.items[0].variantId").value(variant.getId()));

    mvc.perform(
            post("/compare/api/remove")
                .session(session)
                .with(csrf())
                .param("variantId", variant.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.count").value(0));

    mvc.perform(post("/compare/api/clear").session(session).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.count").value(0));
  }
}
