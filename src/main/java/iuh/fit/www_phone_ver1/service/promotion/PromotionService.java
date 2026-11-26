package iuh.fit.www_phone_ver1.service.promotion;

import iuh.fit.www_phone_ver1.dto.order.*;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionService {
  private final PromotionRepository promotions;
  private final StorePolicyRepository policies;
  private final OrderRepository orders;
  private final ProductRepository products;

  public PromotionService(
      PromotionRepository promotions,
      StorePolicyRepository policies,
      OrderRepository orders,
      ProductRepository products) {
    this.promotions = promotions;
    this.policies = policies;
    this.orders = orders;
    this.products = products;
  }

  @Transactional
  public void save(Long id, PromotionForm form) {
    if (!form.getEndsAt().isAfter(form.getStartsAt()))
      throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
    if ("PERCENT".equals(form.getDiscountType())
        && form.getAmount().compareTo(BigDecimal.valueOf(100)) > 0)
      throw new IllegalArgumentException("Mức giảm phần trăm tối đa 100%.");
    for (String raw :
        List.of(
            Objects.toString(form.getScopeSkus(), ""),
            Objects.toString(form.getRequiredSkus(), "")))
      for (String sku : raw.split(","))
        if (!sku.isBlank() && products.findBySku(sku.trim()).isEmpty())
          throw new IllegalArgumentException("SKU không tồn tại: " + sku.trim());
    if (form.getScopeBrand() != null
        && !form.getScopeBrand().isBlank()
        && !products.findAvailableBrands().contains(form.getScopeBrand()))
      throw new IllegalArgumentException("Hãng áp dụng chưa tồn tại.");
    String code = form.getCode().toUpperCase(Locale.ROOT);
    if (promotions.findByCode(code).filter(p -> !p.getId().equals(id)).isPresent())
      throw new IllegalArgumentException("Mã giảm giá đã tồn tại.");
    Promotion promotion =
        id == null
            ? new Promotion()
            : promotions
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến mãi."));
    if (id != null && (form.getVersion() == null || form.getVersion() != promotion.getVersion()))
      throw new IllegalArgumentException("Khuyến mãi vừa thay đổi. Hãy tải lại trang.");
    BeanUtils.copyProperties(form, promotion, "version", "code");
    promotion.setCode(code);
    promotions.saveAndFlush(promotion);
  }

  @Transactional
  public void savePolicy(StorePolicyForm form) {
    StorePolicy policy = policies.findById(1L).orElseThrow();
    if (form.getVersion() != policy.getVersion())
      throw new IllegalArgumentException("Cấu hình vừa thay đổi. Hãy tải lại trang.");
    policy.setShippingFee(form.getShippingFee());
    policy.setReturnWindowDays(form.getReturnWindowDays());
    policy.setFreeShippingThreshold(form.getFreeShippingThreshold());
    policies.saveAndFlush(policy);
  }

  @Transactional(readOnly = true)
  public Map<Long, Long> remaining(List<Promotion> list) {
    Map<Long, Long> result = new LinkedHashMap<>();
    list.forEach(
        p ->
            result.put(
                p.getId(),
                Math.max(
                    0L,
                    p.getUsageLimit()
                        - orders.countByPromotion_IdAndStatusNot(
                            p.getId(),
                            iuh.fit.www_phone_ver1.service.order.OrderStatuses.CANCELLED))));
    return result;
  }
}
