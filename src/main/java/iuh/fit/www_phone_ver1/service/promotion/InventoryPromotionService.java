package iuh.fit.www_phone_ver1.service.promotion;

import iuh.fit.www_phone_ver1.repository.OrderDetailRepository;
import iuh.fit.www_phone_ver1.service.order.OrderStatuses;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(InventoryPromotionRules.class)
public class InventoryPromotionService {
  private final OrderDetailRepository details;
  private final InventoryPromotionRules rules;

  public InventoryPromotionService(OrderDetailRepository details, InventoryPromotionRules rules) {
    this.details = details;
    this.rules = rules;
  }

  public List<Suggestion> suggestions() {
    LocalDateTime now = LocalDateTime.now();
    return details.findSlowStock(rules.minimumStock(), rules.salesLimit(),
            now.minusDays(rules.salesDays()), now, OrderStatuses.DELIVERED).stream()
        .map(row -> new Suggestion(row.getProductId(), row.getProductName(), row.getSku(),
            row.getStock(), row.getUnitsSold(), discount(row.getStock(), row.getLastSale(), now)))
        .toList();
  }

  int discount(long stock, LocalDateTime lastSale, LocalDateTime now) {
    if (stock > rules.highStock()
        || (lastSale != null && !lastSale.isAfter(now.minusDays(rules.highDays())))) {
      return rules.highDiscount();
    }
    if (stock > rules.mediumStock()
        || (lastSale != null && !lastSale.isAfter(now.minusDays(rules.mediumDays())))) {
      return rules.mediumDiscount();
    }
    return rules.baseDiscount();
  }

  public int salesDays() {
    return rules.salesDays();
  }

  public record Suggestion(Long productId, String productName, String sku,
      long stock, long unitsSold, int discountPercent) {}
}
