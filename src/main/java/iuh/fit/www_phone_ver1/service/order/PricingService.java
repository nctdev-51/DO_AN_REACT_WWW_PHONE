package iuh.fit.www_phone_ver1.service.order;

import iuh.fit.www_phone_ver1.dto.order.CheckoutQuote;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import java.math.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricingService {
  private final PromotionRepository promotions;
  private final OrderRepository orders;
  private final StorePolicyRepository policies;
  private final ProductRepository products;

  public PricingService(
      PromotionRepository promotions,
      OrderRepository orders,
      StorePolicyRepository policies,
      ProductRepository products) {
    this.promotions = promotions;
    this.products = products;
    this.orders = orders;
    this.policies = policies;
  }

  @Transactional(readOnly = true)
  public CheckoutQuote preview(BigDecimal subtotal, String code, Long userId) {
    return calculate(subtotal, code, userId, false, null);
  }

  // Called inside the order transaction. The promotion lock serializes quota checks and
  // reservations.
  @Transactional
  public CheckoutQuote reserve(BigDecimal subtotal, String code, Long userId) {
    return calculate(subtotal, code, userId, true, null);
  }

  @Transactional(readOnly = true)
  public CheckoutQuote preview(
      BigDecimal subtotal,
      String code,
      Long userId,
      Collection<iuh.fit.www_phone_ver1.dto.cart.CartItem> items) {
    return calculate(subtotal, code, userId, false, items);
  }

  @Transactional
  public CheckoutQuote reserve(
      BigDecimal subtotal,
      String code,
      Long userId,
      Collection<iuh.fit.www_phone_ver1.dto.cart.CartItem> items) {
    return calculate(subtotal, code, userId, true, items);
  }

  private CheckoutQuote calculate(
      BigDecimal subtotal,
      String raw,
      Long userId,
      boolean lock,
      Collection<iuh.fit.www_phone_ver1.dto.cart.CartItem> items) {
    if (subtotal == null || subtotal.signum() <= 0)
      throw new IllegalArgumentException("Giỏ hàng trống.");
    String code = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
    Promotion promotion = null;
    BigDecimal discount = BigDecimal.ZERO;
    if (!code.isBlank()) {
      if (!code.matches("[A-Z0-9_-]{3,40}"))
        throw new IllegalArgumentException("Mã giảm giá không hợp lệ.");
      promotion =
          (lock ? promotions.lockByCode(code) : promotions.findByCode(code))
              .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại."));
      LocalDateTime now = LocalDateTime.now();
      if (!promotion.isActive()
          || promotion.getStartsAt().isAfter(now)
          || !promotion.getEndsAt().isAfter(now))
        throw new IllegalArgumentException("Mã giảm giá chưa bắt đầu hoặc đã hết hạn.");
      if (promotion.getRequiredSkus() != null && !promotion.getRequiredSkus().isBlank()) {
        if (items == null) throw new IllegalArgumentException("Mã combo cần kiểm tra giỏ hàng.");
        Set<String> present = new HashSet<>();
        products
            .findAllById(items.stream().map(i -> i.getProductId()).toList())
            .forEach(p -> present.add(p.getSku()));
        if (!present.containsAll(
            Arrays.stream(promotion.getRequiredSkus().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList()))
          throw new IllegalArgumentException(
              "Giỏ hàng chưa đủ sản phẩm bắt buộc của mã combo / quà tặng.");
      }
      BigDecimal eligible = subtotal;
      if ((promotion.getScopeBrand() != null && !promotion.getScopeBrand().isBlank())
          || (promotion.getScopeSkus() != null && !promotion.getScopeSkus().isBlank())) {
        if (items == null)
          throw new IllegalArgumentException("Mã này cần kiểm tra sản phẩm trong giỏ hàng.");
        Set<String> skus =
            new HashSet<>(
                Arrays.stream(Objects.toString(promotion.getScopeSkus(), "").split(","))
                    .map(String::trim)
                    .filter(v -> !v.isBlank())
                    .toList());
        Map<Long, Product> catalog = new HashMap<>();
        products
            .findAllById(items.stream().map(i -> i.getProductId()).toList())
            .forEach(p -> catalog.put(p.getId(), p));
        eligible = BigDecimal.ZERO;
        for (var item : items) {
          var p = catalog.get(item.getProductId());
          if (p != null
              && (promotion.getScopeBrand() == null
                  || promotion.getScopeBrand().isBlank()
                  || promotion.getScopeBrand().equals(p.getBrand()))
              && (skus.isEmpty() || skus.contains(p.getSku())))
            eligible = eligible.add(item.getSubTotal());
        }
        if (eligible.signum() <= 0)
          throw new IllegalArgumentException(
              "Giỏ hàng chưa có sản phẩm thuộc phạm vi mã giảm giá.");
      }
      if (eligible.compareTo(promotion.getMinimumSubtotal()) < 0)
        throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu của mã giảm giá.");
      if (orders.countByPromotion_IdAndStatusNot(promotion.getId(), OrderStatuses.CANCELLED)
          >= promotion.getUsageLimit())
        throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng.");
      if (userId != null
          && orders.existsByPromotion_IdAndUser_IdAndStatusNot(
              promotion.getId(), userId, OrderStatuses.CANCELLED))
        throw new IllegalArgumentException(
            "Bạn đã sử dụng mã này. Mỗi khách được dùng một lần cho đơn chưa hủy.");
      discount =
          "PERCENT".equals(promotion.getDiscountType())
              ? eligible
                  .multiply(promotion.getAmount())
                  .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
              : promotion.getAmount();
      if (promotion.getMaximumDiscount() != null)
        discount = discount.min(promotion.getMaximumDiscount());
      discount = discount.min(eligible);
    }
    StorePolicy policy = policies.findById(1L).orElseGet(StorePolicy::new);
    // Free-shipping qualification uses merchandise subtotal before discount.
    BigDecimal shipping =
        subtotal.compareTo(policy.getFreeShippingThreshold()) >= 0
            ? BigDecimal.ZERO
            : policy.getShippingFee();
    return new CheckoutQuote(
        subtotal, discount, shipping, subtotal.subtract(discount).add(shipping), promotion);
  }
}
