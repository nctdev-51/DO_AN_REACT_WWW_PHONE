package iuh.fit.www_phone_ver1.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import iuh.fit.www_phone_ver1.dto.cart.*;
import iuh.fit.www_phone_ver1.dto.order.CheckoutForm;
import iuh.fit.www_phone_ver1.dto.user.*;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.admin.AdminUserService;
import iuh.fit.www_phone_ver1.service.catalog.ReviewService;
import iuh.fit.www_phone_ver1.service.order.*;
import iuh.fit.www_phone_ver1.service.payment.VnPayService;
import iuh.fit.www_phone_ver1.service.user.AccountService;
import java.math.BigDecimal;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "app.payments.expiry-enabled=false")
@ActiveProfiles("demo")
@Transactional
class StoreIntegrationTests {
  @Autowired WebApplicationContext context;
  @Autowired UserRepository users;
  @Autowired RoleRepository roles;
  @Autowired ProductRepository products;
  @Autowired ProductVariantRepository variants;
  @Autowired OrderRepository orders;
  @Autowired AddressRepository addresses;
  @Autowired ReviewRepository reviews;
  @Autowired AdminUserService admin;
  @Autowired OrderService checkout;
  @Autowired OrderHistoryService history;
  @Autowired AccountService accounts;
  @Autowired ReviewService feedback;
  @Autowired iuh.fit.www_phone_ver1.service.catalog.CatalogService catalog;
  @Autowired PasswordEncoder passwords;
  MockMvc mvc;
  User customer;
  ProductVariant variant;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    Role role =
        roles.findByName("USER").orElseGet(() -> roles.save(Role.builder().name("USER").build()));
    customer =
        users.saveAndFlush(
            User.builder()
                .fullName("Test Customer")
                .email("integration@example.com")
                .password(passwords.encode("OldPass123!"))
                .status(1)
                .roles(Set.of(role))
                .build());
    variant = variants.findAll().get(0);
    variant.setStock(10);
    variants.saveAndFlush(variant);
  }

  Cart cart() {
    Cart cart = new Cart();
    cart.addItem(variant, 2);
    return cart;
  }

  @Test
  void anInsufficientSecondItemDoesNotReserveTheFirstItem() {
    ProductVariant other =
        variants.findAll().stream()
            .filter(v -> !v.getId().equals(variant.getId()))
            .findFirst()
            .orElseThrow();
    other.setStock(5);
    Cart cart = cart();
    cart.addItem(other, 2);
    other.setStock(0);
    variants.flush();
    long before = orders.count();
    assertThrows(
        IllegalArgumentException.class,
        () -> checkout.placeOrder(cart, customer.getEmail(), form()));
    assertEquals(10, variant.getStock());
    assertEquals(before, orders.count());
  }

  @Test
  void anOldAdminStockFormCannotOverwriteACompletedSale() {
    var edit = new iuh.fit.www_phone_ver1.dto.catalog.VariantForm();
    org.springframework.beans.BeanUtils.copyProperties(variant, edit);
    edit.setVersion(variant.getVersion());
    order();
    assertThrows(
        IllegalArgumentException.class,
        () -> catalog.saveVariant(variant.getProduct().getId(), variant.getId(), edit));
    assertEquals(8, variant.getStock());
  }

  @Test
  void customerReturnsToCheckoutAfterLogin() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("cart", cart());
    mvc.perform(get("/checkout").session(session)).andExpect(status().is3xxRedirection());
    mvc.perform(
            post("/login")
                .session(session)
                .with(csrf())
                .param("username", customer.getEmail())
                .param("password", "OldPass123!"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/checkout")));
  }

  CheckoutForm form() {
    CheckoutForm form = new CheckoutForm();
    form.setRecipientName("Test Customer");
    form.setPhone("0901234567");
    form.setShippingAddress("12 Test Street");
    return form;
  }

  Order order() {
    return checkout.placeOrder(cart(), customer.getEmail(), form());
  }

  @Test
  void catalogHasRealLocalImagesAndSpecifications() {
    assertTrue(products.count() >= 30);
    assertTrue(variants.count() >= 60);
    assertTrue(
        products.findAll().stream().allMatch(p -> p.getImage().startsWith("/images/products/")));
  }

  @Test
  void checkoutPersistsSnapshotsAndReservesStock() {
    Cart cart = cart();
    Order order = checkout.placeOrder(cart, customer.getEmail(), form());
    assertNotNull(order.getId());
    assertEquals(0, cart.getTotalAmount().compareTo(order.getTotalAmount()));
    assertEquals(8, variant.getStock());
    var detail = order.getOrderDetails().get(0);
    assertEquals(variant.getSku(), detail.getSku());
    assertEquals(variant.getLabel(), detail.getVariantLabel());
    String snapshot = detail.getDisplayName();
    variant.getProduct().setName("Changed name");
    assertEquals(snapshot, detail.getDisplayName());
  }

  @Test
  void discontinuedProductCannotBeOrdered() {
    Cart cart = cart();
    variant.getProduct().setStatus(0);
    assertThrows(
        IllegalArgumentException.class,
        () -> checkout.placeOrder(cart, customer.getEmail(), form()));
    assertEquals(10, variant.getStock());
  }

  @Test
  void insufficientStockDoesNotCreateAnOrder() {
    Cart cart = cart();
    variant.setStock(1);
    long before = orders.count();
    assertThrows(
        IllegalArgumentException.class,
        () -> checkout.placeOrder(cart, customer.getEmail(), form()));
    assertEquals(before, orders.count());
    assertEquals(1, variant.getStock());
  }

  @Test
  void changedPriceRequiresConfirmationAndRefreshesCart() {
    Cart cart = cart();
    variant.setPrice(variant.getPrice().add(BigDecimal.valueOf(100000)));
    variants.saveAndFlush(variant);
    assertThrows(
        IllegalArgumentException.class,
        () -> checkout.placeOrder(cart, customer.getEmail(), form()));
    assertEquals(variant.getPrice(), cart.getItemList().iterator().next().getUnitPrice());
    assertEquals(10, variant.getStock());
  }

  @Test
  void cancelRestoresStockExactlyOnce() {
    Order order = order();
    checkout.cancelByCustomer(order.getId(), customer.getEmail(), "Đổi ý");
    assertEquals(10, variant.getStock());
    assertEquals(OrderStatuses.CANCELLED, order.getStatus());
    assertThrows(
        IllegalArgumentException.class,
        () -> checkout.cancelByCustomer(order.getId(), customer.getEmail(), "Again"));
    assertEquals(10, variant.getStock());
  }

  @Test
  void customerCannotCancelAnotherCustomersOrder() {
    Order order = order();
    assertThrows(
        IllegalArgumentException.class,
        () -> checkout.cancelByCustomer(order.getId(), "other@example.com", "Not mine"));
    assertEquals(8, variant.getStock());
  }

  @Test
  void shippingOrderCannotBeCancelled() {
    Order order = order();
    checkout.updateStatus(order.getId(), OrderStatuses.SHIPPING);
    assertThrows(
        IllegalArgumentException.class,
        () -> checkout.cancelByCustomer(order.getId(), customer.getEmail(), "Late"));
    assertEquals(8, variant.getStock());
  }

  @Test
  void transitionsValidateAndCodIsPaidOnDelivery() {
    Order order = order();
    assertThrows(
        IllegalArgumentException.class,
        () -> checkout.updateStatus(order.getId(), OrderStatuses.DELIVERED));
    checkout.updateStatus(order.getId(), OrderStatuses.SHIPPING);
    checkout.updateStatus(order.getId(), OrderStatuses.DELIVERED);
    assertEquals("PAID", order.getPaymentStatus());
    assertThrows(
        IllegalArgumentException.class,
        () -> checkout.updateStatus(order.getId(), OrderStatuses.CANCELLED));
  }

  @Test
  void userChangesPersist() {
    admin.updateUser(customer.getId(), "Updated Name", "0912345678", "New address", 0, "admin");
    assertEquals("Updated Name", customer.getFullName());
    assertEquals(0, customer.getStatus());
  }

  @Test
  void userWithOrdersCannotBeDeleted() {
    order();
    assertThrows(IllegalArgumentException.class, () -> admin.deleteUser(customer.getId(), "admin"));
    assertTrue(users.existsById(customer.getId()));
  }

  @Test
  void adminCannotBeDeletedOrLocked() {
    User manager = users.findByEmail("admin").orElseThrow();
    assertThrows(
        IllegalArgumentException.class, () -> admin.deleteUser(manager.getId(), "another-admin"));
    assertThrows(
        IllegalArgumentException.class,
        () -> admin.updateUser(manager.getId(), "Admin", "", "", 0, "another-admin"));
  }

  AddressForm address(String line, boolean primary) {
    AddressForm form = new AddressForm();
    form.setRecipientName("Test");
    form.setPhone("0901234567");
    form.setLine(line);
    form.setDefaultAddress(primary);
    return form;
  }

  @Test
  void defaultAddressIsUniqueAndReassignedWhenDeleted() {
    accounts.saveAddress(customer.getEmail(), null, address("First", false));
    accounts.saveAddress(customer.getEmail(), null, address("Second", true));
    var all = addresses.findByUser_EmailOrderByDefaultAddressDescIdDesc(customer.getEmail());
    assertEquals(1, all.stream().filter(Address::isDefaultAddress).count());
    assertEquals("Second", all.get(0).getLine());
    accounts.deleteAddress(customer.getEmail(), all.get(0).getId());
    assertTrue(
        addresses
            .findByUser_EmailOrderByDefaultAddressDescIdDesc(customer.getEmail())
            .get(0)
            .isDefaultAddress());
  }

  @Test
  void addressOwnershipIsEnforced() {
    accounts.saveAddress(customer.getEmail(), null, address("Private", true));
    Long id = addresses.findAll().get(0).getId();
    assertThrows(IllegalArgumentException.class, () -> accounts.deleteAddress("admin", id));
    assertTrue(addresses.existsById(id));
  }

  @Test
  void userWithoutOrdersAndWithAddressCanBeDeleted() {
    accounts.saveAddress(customer.getEmail(), null, address("Test", true));
    admin.deleteUser(customer.getId(), "admin");
    assertFalse(users.existsById(customer.getId()));
    assertTrue(addresses.findAll().isEmpty());
  }

  @Test
  void passwordRequiresCurrentPasswordAndConfirmation() {
    PasswordForm form = new PasswordForm();
    form.setCurrentPassword("wrong");
    form.setNewPassword("NewPass123!");
    form.setConfirmPassword("NewPass123!");
    assertThrows(
        IllegalArgumentException.class, () -> accounts.changePassword(customer.getEmail(), form));
    form.setCurrentPassword("OldPass123!");
    accounts.changePassword(customer.getEmail(), form);
    assertTrue(passwords.matches("NewPass123!", customer.getPassword()));
  }

  @Test
  void onlyDeliveredPurchasesCanBeReviewed() {
    assertFalse(feedback.canReview(customer.getEmail(), variant.getProduct().getId()));
    assertThrows(
        IllegalArgumentException.class,
        () -> feedback.save(customer.getEmail(), variant.getProduct().getId(), 5, "No purchase"));
    Order order = order();
    checkout.updateStatus(order.getId(), OrderStatuses.SHIPPING);
    checkout.updateStatus(order.getId(), OrderStatuses.DELIVERED);
    feedback.save(customer.getEmail(), variant.getProduct().getId(), 5, "Sản phẩm tốt");
    feedback.save(customer.getEmail(), variant.getProduct().getId(), 4, "Cập nhật đánh giá");
    assertEquals(1, reviews.count());
    assertEquals(4, reviews.findAll().get(0).getRating());
  }

  @Test
  void adminAndAccountRequireAuthorization() throws Exception {
    mvc.perform(get("/admin/orders").with(user(customer.getEmail()).roles("USER")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/checkout")).andExpect(status().is3xxRedirection());
    mvc.perform(get("/account")).andExpect(status().is3xxRedirection());
  }

  @Test
  void postRequiresCsrfAndGetCannotDelete() throws Exception {
    mvc.perform(
            post("/admin/users/" + customer.getId() + "/delete").with(user("admin").roles("ADMIN")))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/admin/users/" + customer.getId() + "/delete").with(user("admin").roles("ADMIN")))
        .andExpect(status().isMethodNotAllowed());
  }

  @Test
  void invalidCheckoutDoesNotSaveOrder() throws Exception {
    long before = orders.count();
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("cart", cart());
    mvc.perform(
            post("/checkout/place-order")
                .session(session)
                .with(user(customer.getEmail()))
                .with(csrf())
                .param("shippingAddress", " ")
                .param("phone", "abc")
                .param("recipientName", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("store/checkout/index"))
        .andExpect(
            model()
                .attributeHasFieldErrors(
                    "checkoutForm", "shippingAddress", "phone", "recipientName"));
    assertEquals(before, orders.count());
    assertNotNull(session.getAttribute("cart"));
  }

  @Test
  void customerCannotReadSomeoneElsesOrder() throws Exception {
    Order order = order();
    mvc.perform(get("/orders/" + order.getId()).with(user("other@example.com")))
        .andExpect(status().isNotFound());
    mvc.perform(get("/orders/" + order.getId()).with(user(customer.getEmail())))
        .andExpect(status().isOk())
        .andExpect(view().name("store/orders/detail"));
  }

  @Test
  void cartQuantityCannotOverflowOrExceedLimit() {
    Cart cart = cart();
    assertThrows(IllegalArgumentException.class, () -> cart.addItem(variant, Integer.MAX_VALUE));
    assertThrows(IllegalArgumentException.class, () -> cart.updateQuantity(variant.getId(), 100));
    assertEquals(2, cart.getTotalQuantity());
  }

  VnPayService gateway() {
    return new VnPayService(
        "TESTTERM",
        "integration-secret",
        "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
        "http://localhost:8080/payments/vnpay/return",
        true,
        orders,
        checkout,
        history);
  }

  Order onlineOrder() {
    CheckoutForm form = form();
    form.setPaymentMethod("VNPAY");
    return checkout.placeOrder(cart(), customer.getEmail(), form);
  }

  Map<String, String> notification(Order order, String amount) throws Exception {
    Map<String, String> values = new TreeMap<>();
    values.put("vnp_TmnCode", "TESTTERM");
    values.put("vnp_TxnRef", order.getPaymentReference());
    values.put("vnp_Amount", amount);
    values.put("vnp_ResponseCode", "00");
    values.put("vnp_TransactionStatus", "00");
    values.put("vnp_TransactionNo", "TEST123");
    StringJoiner joined = new StringJoiner("&");
    values.forEach(
        (key, value) ->
            joined.add(
                URLEncoder.encode(key, StandardCharsets.UTF_8)
                    + "="
                    + URLEncoder.encode(value, StandardCharsets.UTF_8)));
    Mac mac = Mac.getInstance("HmacSHA512");
    mac.init(
        new SecretKeySpec("integration-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
    values.put(
        "vnp_SecureHash",
        HexFormat.of().formatHex(mac.doFinal(joined.toString().getBytes(StandardCharsets.UTF_8))));
    return values;
  }

  @Test
  void unsignedGatewayNotificationCannotMarkPaid() {
    Order order = onlineOrder();
    var result =
        gateway()
            .handleNotification(
                Map.of("vnp_TxnRef", order.getPaymentReference(), "vnp_ResponseCode", "00"));
    assertEquals("97", result.get("RspCode"));
    assertEquals("UNPAID", order.getPaymentStatus());
  }

  @Test
  void signedGatewayNotificationMustMatchAmount() throws Exception {
    Order order = onlineOrder();
    assertEquals("04", gateway().handleNotification(notification(order, "100")).get("RspCode"));
    assertEquals("UNPAID", order.getPaymentStatus());
  }

  @Test
  void validGatewayNotificationIsIdempotent() throws Exception {
    Order order = onlineOrder();
    var values =
        notification(
            order, order.getTotalAmount().multiply(BigDecimal.valueOf(100)).toPlainString());
    assertEquals("00", gateway().handleNotification(values).get("RspCode"));
    assertEquals("PAID", order.getPaymentStatus());
    assertEquals("02", gateway().handleNotification(values).get("RspCode"));
    assertEquals(8, variant.getStock());
  }

  @Test
  void unpaidOnlineOrderCannotShipAndExpires() {
    Order order = onlineOrder();
    assertThrows(
        IllegalArgumentException.class,
        () -> checkout.updateStatus(order.getId(), OrderStatuses.SHIPPING));
    order.setOrderDate(LocalDateTime.now().minusMinutes(20));
    orders.flush();
    checkout.expireUnpaidOrders();
    assertEquals(OrderStatuses.CANCELLED, order.getStatus());
    assertEquals(10, variant.getStock());
  }

  @Test
  void latePaymentIsFlaggedForRefundWithoutTakingStockAgain() throws Exception {
    Order order = onlineOrder();
    checkout.cancelByCustomer(order.getId(), customer.getEmail(), "Cancel before payment");
    gateway()
        .handleNotification(
            notification(
                order, order.getTotalAmount().multiply(BigDecimal.valueOf(100)).toPlainString()));
    assertEquals("REFUND_REQUIRED", order.getPaymentStatus());
    assertEquals(10, variant.getStock());
  }
}
