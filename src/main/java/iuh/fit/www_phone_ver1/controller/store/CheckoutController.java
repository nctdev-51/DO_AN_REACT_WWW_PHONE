package iuh.fit.www_phone_ver1.controller.store;

import iuh.fit.www_phone_ver1.dto.cart.Cart;
import iuh.fit.www_phone_ver1.dto.order.CheckoutForm;
import iuh.fit.www_phone_ver1.dto.user.AddressForm;
import iuh.fit.www_phone_ver1.entity.Address;
import iuh.fit.www_phone_ver1.entity.StorePolicy;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.order.OrderService;
import iuh.fit.www_phone_ver1.service.order.PricingService;
import iuh.fit.www_phone_ver1.service.payment.VnPayService;
import iuh.fit.www_phone_ver1.service.user.AccountService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
  private static final String CHECKOUT_TOKEN = "checkoutToken";
  private static final String LAST_CHECKOUT_TOKEN = "lastSubmittedCheckoutToken";
  private static final String LAST_ORDER_ID = "lastSubmittedOrderId";

  private final OrderService service;
  private final UserRepository users;
  private final OrderRepository orders;
  private final AddressRepository addresses;
  private final VnPayService payments;
  private final iuh.fit.www_phone_ver1.service.shipping.GhnClient ghn;
  private final iuh.fit.www_phone_ver1.service.shipping.GhnShippingService shipping;
  private final PricingService pricing;
  private final iuh.fit.www_phone_ver1.service.order.LoyaltyService loyalty;
  private final iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService experience;
  private final StorePolicyRepository policies;
  private final AccountService accountService;

  public CheckoutController(
      OrderService service,
      UserRepository users,
      OrderRepository orders,
      AddressRepository addresses,
      VnPayService payments,
      iuh.fit.www_phone_ver1.service.shipping.GhnClient ghn,
      iuh.fit.www_phone_ver1.service.shipping.GhnShippingService shipping,
      PricingService pricing,
      iuh.fit.www_phone_ver1.service.order.LoyaltyService loyalty,
      iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService experience,
      StorePolicyRepository policies,
      AccountService accountService) {
    this.service = service;
    this.users = users;
    this.orders = orders;
    this.addresses = addresses;
    this.payments = payments;
    this.ghn = ghn;
    this.shipping = shipping;
    this.pricing = pricing;
    this.loyalty = loyalty;
    this.experience = experience;
    this.policies = policies;
    this.accountService = accountService;
  }

  private void quote(Model model, Cart cart, CheckoutForm form, String email) {
    Long userId = users.findByEmail(email).orElseThrow().getId();
    java.math.BigDecimal pointsDiscount = java.math.BigDecimal.ZERO;
    iuh.fit.www_phone_ver1.dto.order.CheckoutQuote result;
    try {
      result =
          pricing.preview(
              cart.getTotalAmount(), form.getPromotionCode(), userId, cart.getItemList());
      pointsDiscount =
          loyalty.preview(
              userId, form.getRedeemPoints(), result.getSubtotal().subtract(result.getDiscount()));
    } catch (IllegalArgumentException ex) {
      model.addAttribute("errorMsg", ex.getMessage());
      result = pricing.preview(cart.getTotalAmount(), null, userId, cart.getItemList());
    }
    model.addAttribute("quote", result);
    model.addAttribute("pointsDiscount", pointsDiscount);
    model.addAttribute("payableTotal", result.getTotal().subtract(pointsDiscount));
    model.addAttribute("pointsBalance", loyalty.balance(userId));
    model.addAttribute("experience", experience.get());
    model.addAttribute("storePolicy", policies.findById(1L).orElseGet(StorePolicy::new));
  }

  private void options(Model model, String email) {
    model.addAttribute(
        "addresses", addresses.findByUser_EmailOrderByDefaultAddressDescIdDesc(email));
    model.addAttribute("vnpayAvailable", payments.isAvailable());
    model.addAttribute("ghnAvailable", ghn.isAvailable());
  }

  @GetMapping
  public String viewCheckout(HttpSession session, Model model, Principal principal) {
    Cart cart = (Cart) session.getAttribute("cart");
    if (cart == null || cart.getItemList().isEmpty()) return "redirect:/cart";
    var user = users.findByEmail(principal.getName()).orElseThrow();
    CheckoutForm form = new CheckoutForm();
    form.setRecipientName(user.getFullName());
    form.setShippingAddress(user.getAddress());
    form.setPhone(user.getPhone());
    form.setPromotionCode((String) session.getAttribute("selectedPromotion"));
    addresses.findByUser_EmailOrderByDefaultAddressDescIdDesc(principal.getName()).stream()
        .findFirst()
        .ifPresent(
            a -> {
              form.setRecipientName(a.getRecipientName());
              form.setPhone(a.getPhone());
              form.setShippingAddress(a.getLine());
              form.setProvince(a.getProvince());
              form.setWard(a.getWard());
              form.setProvinceCode(a.getProvinceCode());
              form.setWardCode(a.getWardCode());
            });
    String token = UUID.randomUUID().toString();
    session.setAttribute(CHECKOUT_TOKEN, token);
    model.addAttribute("checkoutToken", token);
    model.addAttribute("checkoutForm", form);
    model.addAttribute("cart", cart);
    options(model, principal.getName());
    quote(model, cart, form, principal.getName());
    return "store/checkout/index";
  }

  @PostMapping("/quote")
  public String preview(
      @ModelAttribute("checkoutForm") CheckoutForm form,
      HttpSession session,
      Model model,
      Principal principal) {
    Cart cart = (Cart) session.getAttribute("cart");
    if (cart == null || cart.getItemList().isEmpty()) return "redirect:/cart";
    model.addAttribute("checkoutToken", session.getAttribute(CHECKOUT_TOKEN));
    model.addAttribute("cart", cart);
    options(model, principal.getName());
    quote(model, cart, form, principal.getName());
    return "store/checkout/index";
  }

  @PostMapping("/delivery-quote")
  public String deliveryQuote(
      @Valid @ModelAttribute("checkoutForm") CheckoutForm form,
      BindingResult errors,
      HttpSession session,
      Model model,
      Principal principal) {
    Cart cart = (Cart) session.getAttribute("cart");
    if (cart == null || cart.getItemList().isEmpty()) return "redirect:/cart";
    model.addAttribute("checkoutToken", session.getAttribute(CHECKOUT_TOKEN));
    model.addAttribute("cart", cart);
    options(model, principal.getName());
    quote(model, cart, form, principal.getName());
    if (!errors.hasErrors())
      try {
        model.addAttribute(
            "deliveryEstimate",
            shipping.preview(
                form,
                cart.getTotalQuantity(),
                (java.math.BigDecimal) model.getAttribute("payableTotal")));
      } catch (IllegalArgumentException ex) {
        model.addAttribute("errorMsg", ex.getMessage());
      }
    return "store/checkout/index";
  }

  @PostMapping("/addresses")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> createAddress(
      @Valid @ModelAttribute AddressForm form, BindingResult errors, Principal principal) {
    if (errors.hasErrors()) {
      String msg =
          errors.getFieldErrors().stream()
              .findFirst()
              .map(f -> f.getDefaultMessage())
              .orElse("Thông tin địa chỉ chưa hợp lệ.");
      return ResponseEntity.badRequest().body(Map.of("error", msg));
    }
    try {
      accountService.saveAddress(principal.getName(), null, form);
      Address saved =
          addresses.findByUser_EmailOrderByDefaultAddressDescIdDesc(principal.getName()).stream()
              .max(Comparator.comparing(Address::getId))
              .orElseThrow();
      var body = new LinkedHashMap<String, Object>();
      body.put("id", saved.getId());
      body.put("fullLine", saved.getFullLine());
      body.put("recipientName", saved.getRecipientName());
      body.put("phone", saved.getPhone());
      body.put("line", saved.getLine());
      body.put("provinceCode", Objects.toString(saved.getProvinceCode(), ""));
      body.put("wardCode", Objects.toString(saved.getWardCode(), ""));
      body.put("province", Objects.toString(saved.getProvince(), ""));
      body.put("ward", Objects.toString(saved.getWard(), ""));
      body.put("defaultAddress", saved.isDefaultAddress());
      return ResponseEntity.ok(body);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
  }

  @PostMapping("/place-order")
  public String processOrder(
      @Valid @ModelAttribute("checkoutForm") CheckoutForm form,
      BindingResult errors,
      @RequestParam(required = false) String checkoutToken,
      HttpSession session,
      Model model,
      Principal principal,
      HttpServletRequest request) {
    synchronized (session) {
      Object lastToken = session.getAttribute(LAST_CHECKOUT_TOKEN);
      if (checkoutToken != null && checkoutToken.equals(lastToken)) {
        Object lastOrderId = session.getAttribute(LAST_ORDER_ID);
        if (lastOrderId instanceof Long orderId) {
          return "redirect:/orders/" + orderId;
        }
      }
      Cart cart = (Cart) session.getAttribute("cart");
      if (cart == null || cart.getItemList().isEmpty()) return "redirect:/cart";
      Object expectedToken = session.getAttribute(CHECKOUT_TOKEN);
      model.addAttribute("checkoutToken", expectedToken);
      model.addAttribute("cart", cart);
      options(model, principal.getName());
      quote(model, cart, form, principal.getName());
      if (expectedToken != null
          && (checkoutToken == null || !expectedToken.equals(checkoutToken))) {
        model.addAttribute(
            "errorMsg",
            "Phiên đặt hàng không còn hợp lệ hoặc đã được gửi trước đó. Vui lòng kiểm tra lại.");
        return "store/checkout/index";
      }
      if ("VNPAY".equals(form.getPaymentMethod()) && !payments.isAvailable())
        errors.reject("payment", "VNPay chưa sẵn sàng. Vui lòng chọn COD.");
      if (errors.hasErrors()) return "store/checkout/index";
      try {
        var order = service.placeOrder(cart, principal.getName(), form);
        session.removeAttribute("cart");
        session.removeAttribute("selectedPromotion");
        session.removeAttribute(CHECKOUT_TOKEN);
        if (checkoutToken != null && !checkoutToken.isBlank()) {
          session.setAttribute(LAST_CHECKOUT_TOKEN, checkoutToken);
          session.setAttribute(LAST_ORDER_ID, order.getId());
        }
        return "VNPAY".equals(order.getPaymentMethod())
            ? "redirect:" + payments.createUrl(order, request.getRemoteAddr())
            : "redirect:/checkout/success/" + order.getId();
      } catch (IllegalArgumentException error) {
        quote(model, cart, form, principal.getName());
        model.addAttribute("errorMsg", error.getMessage());
        return "store/checkout/index";
      }
    }
  }

  @GetMapping("/success/{id}")
  public String success(@PathVariable Long id, Principal principal, Model model) {
    model.addAttribute(
        "order",
        orders
            .findByIdAndUser_Email(id, principal.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    return "store/checkout/success";
  }
}
