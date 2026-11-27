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
  private final OrderDetailRepository orderDetails;

  public OrderService(
      OrderRepository orders,
      ProductVariantRepository variants,
      UserRepository users,
      MailService mail,
      PricingService pricing,
      OrderHistoryService history,
      LoyaltyService loyalty,
      AddressDirectory directory,
      iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService experience,
      OrderDetailRepository orderDetails) {
    this.orders = orders;
    this.variants = variants;
    this.users = users;
    this.mail = mail;
    this.pricing = pricing;
    this.history = history;
    this.loyalty = loyalty;
    this.directory = directory;
    this.experience = experience;
    this.orderDetails = orderDetails;
  }

  @io.micrometer.core.annotation.Timed(value = "ptv.orders.place", histogram = true)
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
    List<Long> variantIds =
        items.stream()
            .map(CartItem::getVariantId)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .toList();
    variants
        .lockAll(variantIds)
        .forEach(v -> locked.put(v.getId(), v));
    // Validate every item before reserving stock; sorted row locks prevent overselling.
    for (CartItem item : items) {
      ProductVariant variant = locked.get(item.getVariantId());
      if (variant == null || !variant.isActive() || variant.getProduct().getStatus() != 1)
        throw new IllegalArgumentException(
            item.getProductName()
                + " ("
                + item.getVariantLabel()
                + ") đã ngừng kinh doanh. Vui lòng quay lại giỏ hàng để cập nhật.");
      if (item.getQuantity() < 1
          || item.getQuantity() > 99
          || item.getQuantity() > variant.getStock())
        throw new IllegalArgumentException(
            variant.getStock() <= 0
                ? item.getProductName()
                    + " ("
                    + item.getVariantLabel()
                    + ") vừa hết hàng. Vui lòng chọn phiên bản khác trong giỏ hàng."
                : "Chỉ còn "
                    + variant.getStock()
                    + " sản phẩm cho "
                    + item.getProductName()
                    + " ("
                    + variant.getLabel()
                    + "). Vui lòng điều chỉnh số lượng trong giỏ hàng.");
      if (variant.getPrice().compareTo(item.getUnitPrice()) != 0) {
        item.setUnitPrice(variant.getPrice());
        throw new IllegalArgumentException(
            "Giá của "
                + item.getProductName()
                + " ("
                + item.getVariantLabel()
                + ") đã thay đổi. Vui lòng kiểm tra lại tổng tiền và xác nhận đặt lại.");
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
    if ("QR".equals(form.getPaymentMethod())) {
      order.setPaymentMethod("QR");
      order.setPaymentChannel(
          form.getPaymentChannel() == null ? "VIETQR" : form.getPaymentChannel().trim());
      order.setPaymentExpiresAt(LocalDateTime.now().plusMinutes(5));
      order.setQrToken(UUID.randomUUID().toString().replace("-", ""));
    } else if ("VNPAY".equals(form.getPaymentMethod())) {
      order.setPaymentExpiresAt(LocalDateTime.now().plusMinutes(15));
    }
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
    quote.validatePointsCombination(form.getRedeemPoints());
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
    notifyAfterCommit(() -> mail.sendOrderStatusUpdate(order.getUser() == null ? null : order.getUser().getEmail(), order));
  }

  @Transactional
  public Order retryVnPay(Long id, String email) {
    Order order = locked(id);
    if (order.getUser() == null || !order.getUser().getEmail().equals(email))
      throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
    if (!PENDING.equals(order.getStatus())
        || !"VNPAY".equals(order.getPaymentMethod())
        || !Set.of("UNPAID", "FAILED").contains(order.getPaymentStatus()))
      throw new IllegalArgumentException("Đơn hàng không ở trạng thái chờ thanh toán VNPay.");
    if (isPaymentWindowExpired(order)) {
      cancelLocked(order, "Quá thời hạn 15 phút thanh toán VNPay");
      throw new IllegalArgumentException(
          "Đơn hàng đã quá hạn giữ chỗ 15 phút và đã được hủy tự động.");
    }
    order.setPaymentStatus("UNPAID");
    order.setPaymentReference(UUID.randomUUID().toString().replace("-", ""));
    order.setPaymentExpiresAt(LocalDateTime.now().plusMinutes(15));
    history.append(order, "Khách hàng", "Khách hàng yêu cầu thanh toán lại qua VNPay.");
    return orders.saveAndFlush(order);
  }

  @Transactional
  public Order switchToCod(Long id, String email) {
    Order order = locked(id);
    if (order.getUser() == null || !order.getUser().getEmail().equals(email))
      throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
    if (!PENDING.equals(order.getStatus())
        || !"VNPAY".equals(order.getPaymentMethod())
        || !Set.of("UNPAID", "FAILED").contains(order.getPaymentStatus()))
      throw new IllegalArgumentException("Chỉ có thể chuyển sang COD khi đơn VNPay chưa thanh toán.");
    if (isPaymentWindowExpired(order)) {
      cancelLocked(order, "Quá thời hạn 15 phút thanh toán VNPay");
      throw new IllegalArgumentException(
          "Đơn hàng đã quá hạn giữ chỗ 15 phút và đã được hủy tự động.");
    }
    order.setPaymentMethod("COD");
    order.setPaymentStatus("UNPAID");
    order.setPaymentExpiresAt(null);
    order.setQrToken(null);
    order.setPaymentChannel(null);
    history.append(order, "Khách hàng", "Chuyển phương thức thanh toán từ VNPay sang COD.");
    Order saved = orders.saveAndFlush(order);
    notifyAfterCommit(() -> mail.sendOrderConfirmation(email, saved));
    return saved;
  }

  public boolean isPaymentWindowExpired(Order order) {
    LocalDateTime expiresAt =
        order.getPaymentExpiresAt() != null
            ? order.getPaymentExpiresAt()
            : (order.getOrderDate() == null
                ? null
                : order.getOrderDate().plusMinutes("VNPAY".equals(order.getPaymentMethod()) ? 15 : 5));
    return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
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
    notifyAfterCommit(
        () ->
            mail.sendOrderStatusUpdate(
                order.getUser() == null ? null : order.getUser().getEmail(), order));
  }

  /**
   * Admin: cập nhật số lượng một dòng trong đơn hàng đang chờ xử lý.
   * Tự động cộng/trừ tồn kho và tính lại tổng tiền đơn hàng.
   */
  @Transactional
  public void updateOrderDetailQuantity(Long orderId, Long detailId, int newQty) {
    if (newQty < 1 || newQty > 99)
      throw new IllegalArgumentException("Số lượng phải từ 1 đến 99.");
    Order order = locked(orderId);
    if (!PENDING.equals(order.getStatus()))
      throw new IllegalArgumentException("Chỉ có thể sửa đơn đang chờ xử lý.");
    OrderDetail detail = orderDetails.findForUpdate(detailId, orderId)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dòng đơn hàng."));
    int oldQty = detail.getQuantity();
    int delta = newQty - oldQty; // dương = cần lấy thêm kho, âm = trả lại kho
    if (delta != 0 && detail.getVariant() != null) {
      var variant = variants.lockAll(List.of(detail.getVariant().getId())).stream().findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên bản sản phẩm."));
      if (delta > 0 && variant.getStock() < delta)
        throw new IllegalArgumentException(
            "Chỉ còn " + variant.getStock() + " sản phẩm trong kho.");
      variant.setStock(variant.getStock() - delta);
    }
    detail.setQuantity(newQty);
    // Tính lại tổng tiền đơn hàng (subtotal + shippingFee - discount - pointsDiscount)
    BigDecimal newSubtotal = order.getOrderDetails().stream()
        .map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    order.setSubtotal(newSubtotal);
    BigDecimal newTotal = newSubtotal
        .subtract(order.getDiscountAmount())
        .subtract(order.getPointsDiscount())
        .add(order.getShippingFee());
    order.setTotalAmount(newTotal.max(BigDecimal.ZERO));
    history.append(order, "Quản trị viên",
        String.format("Cập nhật số lượng dòng #%d: %d → %d.", detailId, oldQty, newQty));
  }

  private void notifyAfterCommit(Runnable action) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              action.run();
            }
          });
    } else {
      action.run();
    }
  }

  private Order locked(Long id) {
    return orders
        .findForUpdate(id)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
  }

  @Transactional
  public void expireUnpaidOrders() {
    var expired =
        orders.findExpiredUnpaidOrders(
            LocalDateTime.now(), PENDING, List.of("UNPAID", "FAILED"));
    for (Order candidate : expired) {
      Order order = locked(candidate.getId());
      if (PENDING.equals(order.getStatus())
          && Set.of("UNPAID", "FAILED").contains(order.getPaymentStatus()))
        cancelLocked(order, "Quá thời hạn thanh toán VNPay");
    }
  }
}
