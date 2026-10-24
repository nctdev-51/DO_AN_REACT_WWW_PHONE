package iuh.fit.www_phone_ver1.service.order;

import static iuh.fit.www_phone_ver1.service.order.OrderStatuses.*;

import iuh.fit.www_phone_ver1.dto.cart.*;
import iuh.fit.www_phone_ver1.dto.order.CheckoutForm;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.mail.MailService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class OrderService {
  private final OrderRepository orders;
  private final ProductVariantRepository variants;
  private final UserRepository users;
  private final MailService mail;
  private final PricingService pricing;
  private final OrderHistoryService history;
  private final LoyaltyService loyalty;
  private final AddressDirectory directory;
  private final iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService experience;

  public OrderService(
      OrderRepository orders,
      ProductVariantRepository variants,
      UserRepository users,
      MailService mail,
      PricingService pricing,
      OrderHistoryService history,
      LoyaltyService loyalty,
      AddressDirectory directory,
      iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService experience) {
    this.orders = orders;
    this.variants = variants;
    this.users = users;
    this.mail = mail;
    this.pricing = pricing;
    this.history = history;
    this.loyalty = loyalty;
    this.directory = directory;
    this.experience = experience;
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public Order placeOrder(Cart cart, String email, CheckoutForm form) {
    if (cart == null || cart.getItemList().isEmpty())
      throw new IllegalArgumentException("Giỏ hàng trống.");
    User user =
        users
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
    if (user.getStatus() != 1) throw new IllegalArgumentException("Tài khoản đã bị khóa.");
    Collection<CartItem> items = cart.getItemList();
    Map<Long, ProductVariant> locked = new HashMap<>();
    variants
        .lockAll(items.stream().map(CartItem::getVariantId).toList())
        .forEach(v -> locked.put(v.getId(), v));
    // Validate every item before reserving stock; sorted row locks prevent overselling.
    for (CartItem item : items) {
      ProductVariant variant = locked.get(item.getVariantId());
      if (variant == null || !variant.isActive() || variant.getProduct().getStatus() != 1)
        throw new IllegalArgumentException(item.getProductName() + " đã ngừng bán.");
      if (item.getQuantity() < 1
          || item.getQuantity() > 99
          || item.getQuantity() > variant.getStock())
        throw new IllegalArgumentException("Không đủ tồn kho cho " + item.getProductName() + ".");
      if (variant.getPrice().compareTo(item.getUnitPrice()) != 0) {
        item.setUnitPrice(variant.getPrice());
        throw new IllegalArgumentException(
            "Giá sản phẩm đã thay đổi. Vui lòng kiểm tra tổng tiền và đặt lại.");
      }
    }
    Order order =
        Order.builder()
            .user(user)
            .status(PENDING)
            .recipientName(form.getRecipientName().trim())
            .shippingAddress(form.getShippingAddress().trim())
            .phone(form.getPhone())
            .paymentMethod(form.getPaymentMethod())
            .paymentStatus("UNPAID")
            .paymentReference(UUID.randomUUID().toString().replace("-", ""))
            .build();
    BigDecimal total = BigDecimal.ZERO;
    for (CartItem item : items) {
      ProductVariant variant = locked.get(item.getVariantId());
      variant.setStock(variant.getStock() - item.getQuantity());
      OrderDetail detail =
          OrderDetail.builder()
              .order(order)
              .product(variant.getProduct())
              .variant(variant)
              .productName(variant.getProduct().getName())
              .variantLabel(variant.getLabel())
              .sku(variant.getSku())
              .warrantyMonths(variant.getProduct().getWarrantyMonths())
              .quantity(item.getQuantity())
              .price(variant.getPrice())
              .build();
      order.getOrderDetails().add(detail);
      total = total.add(detail.getSubTotal());
    }
    var quote = pricing.reserve(total, form.getPromotionCode(), user.getId(), items);
    if ("VNPAY".equals(form.getPaymentMethod()) && quote.getTotal().signum() == 0)
      throw new IllegalArgumentException("Đơn 0 đồng vui lòng chọn thanh toán khi nhận hàng.");
    order.setSubtotal(total);
    order.setDiscountAmount(quote.getDiscount());
    order.setShippingFee(quote.getShippingFee());
    var pointDiscount =
        loyalty.reserve(order, form.getRedeemPoints(), total.subtract(quote.getDiscount()));
    order.setTotalAmount(quote.getTotal().subtract(pointDiscount));
    var resolved =
        directory.resolve(
            form.getProvinceCode(),
            form.getWardCode(),
            form.getProvince(),
            form.getWard(),
            "structured".equals(form.getAddressMode()));
    order.setProvince(resolved == null ? form.getProvince() : resolved.province());
    order.setWard(resolved == null ? form.getWard() : resolved.ward());
    order.setProvinceCode(resolved == null ? null : resolved.provinceCode());
    order.setWardCode(resolved == null ? null : resolved.wardCode());
    var delivery = experience.get();
    order.setEstimatedDeliveryFrom(
        java.time.LocalDate.now().plusDays(delivery.getDeliveryMinDays()));
    order.setEstimatedDeliveryTo(java.time.LocalDate.now().plusDays(delivery.getDeliveryMaxDays()));
    order.setPromotion(quote.getPromotion());
    if (quote.getPromotion() != null) order.setPromotionCode(quote.getPromotion().getCode());
    history.append(order, "Khách hàng", "Đã đặt hàng, cửa hàng đang chờ xử lý.");
    Order saved = orders.saveAndFlush(order);
    loyalty.recordSpend(saved);
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            mail.sendOrderConfirmation(user.getEmail(), saved);
          }
        });
    return saved;
  }

  @Transactional
  public void updateStatus(Long id, String next) {
    Order order = locked(id);
    if (!ALL.contains(next)) throw new IllegalArgumentException("Trạng thái không hợp lệ.");
    if (order.getStatus().equals(next)) return;
    if (PENDING.equals(order.getStatus()) && CANCELLED.equals(next)) {
      cancelLocked(
          order, "Hủy bởi quản trị viên; khoản đã thanh toán cần đối soát hoàn tiền", true);
      return;
    }
    boolean valid =
        (PENDING.equals(order.getStatus()) && SHIPPING.equals(next))
            || (SHIPPING.equals(order.getStatus()) && DELIVERED.equals(next));
    if (!valid)
      throw new IllegalArgumentException(
          "Không thể chuyển trạng thái đơn hàng từ " + order.getStatus() + " sang " + next + ".");
    if ("VNPAY".equals(order.getPaymentMethod()) && !"PAID".equals(order.getPaymentStatus()))
      throw new IllegalArgumentException("Đơn VNPay chưa được thanh toán.");
    order.setStatus(next);
    if (DELIVERED.equals(next) && "COD".equals(order.getPaymentMethod()))
      order.setPaymentStatus("PAID");
    if (DELIVERED.equals(next)) {
      order.setDeliveredAt(LocalDateTime.now());
      loyalty.delivered(order);
    }
    history.append(order, "Quản trị viên", "Cập nhật đơn hàng: " + next + ".");
  }

  @Transactional
  public void cancelByCustomer(Long id, String email, String reason) {
    Order order = locked(id);
    if (order.getUser() == null || !order.getUser().getEmail().equals(email))
      throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
    cancelLocked(
        order, reason == null || reason.isBlank() ? "Khách hàng yêu cầu hủy" : reason.trim());
  }

  public void cancelLocked(Order order, String reason) {
    cancelLocked(order, reason, false);
  }

  private void cancelLocked(Order order, String reason, boolean adminRefund) {
    if (order.getGhnRequestJson() != null && !"cancel".equals(order.getCarrierStatus()))
      throw new IllegalArgumentException(
          "Hãy hủy vận đơn tại GHN và đồng bộ trạng thái hủy trước khi hủy đơn cửa hàng.");
    if (!PENDING.equals(order.getStatus()))
      throw new IllegalArgumentException("Chỉ có thể hủy đơn đang chờ xử lý.");
    if (!adminRefund
        && java.util.Set.of("PAID", "PARTIALLY_REFUNDED", "REFUNDED", "REFUND_REQUIRED")
            .contains(order.getPaymentStatus()))
      throw new IllegalArgumentException(
          "Đơn đã thanh toán online: vui lòng liên hệ cửa hàng để xử lý hoàn tiền.");
    if (reason.length() > 500) throw new IllegalArgumentException("Lý do hủy tối đa 500 ký tự.");
    List<Long> ids =
        order.getOrderDetails().stream()
            .filter(d -> d.getVariant() != null)
            .map(d -> d.getVariant().getId())
            .toList();
    Map<Long, ProductVariant> stock = new HashMap<>();
    if (!ids.isEmpty()) variants.lockAll(ids).forEach(v -> stock.put(v.getId(), v));
    for (OrderDetail detail : order.getOrderDetails()) {
      if (detail.getVariant() != null) {
        ProductVariant variant = stock.get(detail.getVariant().getId());
        variant.setStock(variant.getStock() + detail.getQuantity());
      }
    }
    loyalty.cancelled(order);
    order.setStatus(CANCELLED);
    order.setCancellationReason(reason);
    history.append(order, "Hệ thống", "Hủy đơn: " + reason);
  }

  private Order locked(Long id) {
    return orders
        .findForUpdate(id)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
  }

  @Transactional
  public void expireUnpaidOrders() {
    var expired =
        orders.findByPaymentMethodAndPaymentStatusAndStatusAndOrderDateBefore(
            "VNPAY", "UNPAID", PENDING, LocalDateTime.now().minusMinutes(15));
    for (Order candidate : expired) {
      Order order = locked(candidate.getId());
      if (PENDING.equals(order.getStatus()) && "UNPAID".equals(order.getPaymentStatus()))
        cancelLocked(order, "Quá thời hạn thanh toán VNPay");
    }
  }
}
