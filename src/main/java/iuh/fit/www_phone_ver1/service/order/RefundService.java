package iuh.fit.www_phone_ver1.service.order;

import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records an independently verified manual settlement, never pretends to call a payment gateway.
 */
@Service
public class RefundService {
  private final RefundRepository refunds;
  private final OrderRepository orders;
  private final LoyaltyService loyalty;
  private final OrderHistoryService history;

  public RefundService(
      RefundRepository refunds,
      OrderRepository orders,
      LoyaltyService loyalty,
      OrderHistoryService history) {
    this.refunds = refunds;
    this.orders = orders;
    this.loyalty = loyalty;
    this.history = history;
  }

  @Transactional
  public void request(Long orderId, BigDecimal amount, String reason) {
    var o =
        orders
            .findForUpdate(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn."));
    if (!Set.of("PAID", "PARTIALLY_REFUNDED", "REFUND_REQUIRED")
        .contains(Objects.toString(o.getPaymentStatus(), "")))
      throw new IllegalArgumentException("Đơn chưa có khoản thanh toán để hoàn tiền.");
    if (!Set.of(OrderStatuses.DELIVERED, OrderStatuses.CANCELLED).contains(o.getStatus()))
      throw new IllegalArgumentException(
          "Hãy hủy đơn chưa giao trước khi hoàn tiền; đơn đang giao cần tiếp nhận hàng trả trước.");
    if (amount == null
        || amount.signum() <= 0
        || amount.scale() > 0
        || amount.compareTo(
                o.getTotalAmount()
                    .subtract(refunds.total(orderId, List.of("PENDING", "COMPLETED"))))
            > 0)
      throw new IllegalArgumentException(
          "Số tiền hoàn vượt khoản còn được hoàn hoặc không hợp lệ.");
    if (reason == null || reason.isBlank() || reason.length() > 500)
      throw new IllegalArgumentException("Lý do phải từ 1 đến 500 ký tự.");
    var r = new Refund();
    r.setOrder(o);
    r.setAmount(amount);
    r.setReason(reason.trim());
    refunds.save(r);
    history.append(o, "Quản trị viên", "Ghi nhận yêu cầu hoàn " + amount.toPlainString() + "đ.");
  }

  @Transactional
  public void settle(Long id, String status, String reference) {
    // Lock the order first for the same lock ordering as delivery, cancellation and callbacks.
    var candidate =
        refunds
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoản hoàn."));
    var o = orders.findForUpdate(candidate.getOrder().getId()).orElseThrow();
    var r = refunds.findById(id).orElseThrow();
    if (!"PENDING".equals(r.getStatus()))
      throw new IllegalArgumentException("Khoản hoàn đã được xử lý.");
    if (!Set.of("COMPLETED", "REJECTED").contains(status))
      throw new IllegalArgumentException("Trạng thái không hợp lệ.");
    if (reference == null || reference.isBlank() || reference.length() > 100)
      throw new IllegalArgumentException(
          "Nhập tham chiếu giao dịch hoặc lý do từ chối, tối đa 100 ký tự.");
    r.setStatus(status);
    r.setSettlementReference(reference.trim());
    r.setCompletedAt(LocalDateTime.now());
    refunds.saveAndFlush(r);
    if ("COMPLETED".equals(status)) {
      var total = refunds.total(o.getId(), List.of("COMPLETED"));
      o.setPaymentStatus(
          total.compareTo(o.getTotalAmount()) >= 0 ? "REFUNDED" : "PARTIALLY_REFUNDED");
      loyalty.refunded(o, total);
    }
    history.append(
        o,
        "Quản trị viên",
        "Hoàn tiền #" + id + ": " + status + ". Tham chiếu: " + reference.trim());
  }
}
