package iuh.fit.www_phone_ver1.service.order;

import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.catalog.StoreExperienceService;
import java.math.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class LoyaltyService {
  private final LoyaltyRepository ledger;
  private final UserRepository users;
  private final StoreExperienceService settings;

  public LoyaltyService(
      LoyaltyRepository ledger, UserRepository users, StoreExperienceService settings) {
    this.ledger = ledger;
    this.users = users;
    this.settings = settings;
  }

  @Transactional(readOnly = true)
  public long balance(Long userId) {
    return ledger.balance(userId);
  }

  public BigDecimal preview(Long userId, long points, BigDecimal merchandise) {
    if (points < 0) throw new IllegalArgumentException("Số điểm không hợp lệ.");
    if (points == 0) return BigDecimal.ZERO;
    var rule = settings.get();
    if (!rule.isLoyaltyEnabled())
      throw new IllegalArgumentException("Chương trình điểm đang tạm dừng.");
    if (points > Math.max(0, ledger.balance(userId)))
      throw new IllegalArgumentException("Bạn không đủ điểm để đổi.");
    var discount = rule.getPointValue().multiply(BigDecimal.valueOf(points));
    var cap =
        merchandise
            .multiply(BigDecimal.valueOf(rule.getRedeemPercent()))
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
    if (discount.compareTo(cap) > 0)
      throw new IllegalArgumentException(
          "Điểm đổi tối đa " + rule.getRedeemPercent() + "% tiền hàng sau mã giảm giá.");
    return discount;
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public BigDecimal reserve(Order order, long points, BigDecimal merchandise) {
    users.lockByEmail(order.getUser().getEmail()).orElseThrow();
    var discount = preview(order.getUser().getId(), points, merchandise);
    var rule = settings.get();
    order.setPointsSpent(points);
    order.setPointsDiscount(discount);
    order.setEarnEverySnapshot(rule.isLoyaltyEnabled() ? rule.getEarnEvery() : null);
    return discount;
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void recordSpend(Order order) {
    if (order.getPointsSpent() > 0)
      entry(order, -order.getPointsSpent(), "SPEND", "Đổi điểm khi đặt hàng");
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void delivered(Order order) {
    if (order.getEarnEverySnapshot() == null) return;
    long points =
        order
            .getSubtotal()
            .subtract(order.getDiscountAmount())
            .subtract(order.getPointsDiscount())
            .max(BigDecimal.ZERO)
            .divide(order.getEarnEverySnapshot(), 0, RoundingMode.DOWN)
            .longValueExact();
    if (points > 0 && !ledger.existsByReference(order.getId() + ":EARN")) {
      order.setPointsEarned(points);
      entry(order, points, "EARN", "Tích điểm đơn đã giao");
    }
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void cancelled(Order order) {
    if (order.getPointsSpent() > 0) {
      entry(order, order.getPointsSpent(), "CANCEL", "Trả điểm đơn đã hủy");
      order.setPointsRestored(order.getPointsSpent());
    }
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void refunded(Order order, BigDecimal totalRefunded) {
    if (order.getTotalAmount().signum() <= 0) return;
    long reverse =
        BigDecimal.valueOf(order.getPointsEarned())
            .multiply(totalRefunded)
            .divide(order.getTotalAmount(), 0, RoundingMode.DOWN)
            .longValueExact();
    long restore =
        BigDecimal.valueOf(order.getPointsSpent())
            .multiply(totalRefunded)
            .divide(order.getTotalAmount(), 0, RoundingMode.DOWN)
            .longValueExact();
    restore = Math.max(restore, order.getPointsRestored());
    long delta = restore - order.getPointsRestored() - reverse + order.getPointsReversed();
    if (delta != 0)
      entry(
          order, delta, "REFUND:" + totalRefunded.toPlainString(), "Điều chỉnh điểm do hoàn tiền");
    order.setPointsRestored(restore);
    order.setPointsReversed(reverse);
  }

  private void entry(Order order, long points, String kind, String text) {
    users.lockByEmail(order.getUser().getEmail()).orElseThrow();
    String reference = order.getId() + ":" + kind;
    if (ledger.existsByReference(reference)) return;
    var e = new LoyaltyEntry();
    e.setUser(order.getUser());
    e.setOrder(order);
    e.setPoints(points);
    e.setReference(reference);
    e.setDescription(text);
    ledger.saveAndFlush(e);
  }
}
