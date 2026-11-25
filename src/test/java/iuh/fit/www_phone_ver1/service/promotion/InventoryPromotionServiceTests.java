package iuh.fit.www_phone_ver1.service.promotion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class InventoryPromotionServiceTests {
  private final InventoryPromotionService service =
      new InventoryPromotionService(null,
          new InventoryPromotionRules(50, 10, 30, 100, 200, 60, 90, 10, 20, 30));
  private final LocalDateTime now = LocalDateTime.of(2026, 11, 25, 12, 0);

  @Test
  void appliesInclusiveAgeAndStrictStockThresholdsWithHighestTierFirst() {
    assertEquals(10, service.discount(50, null, now));
    assertEquals(10, service.discount(100, now.minusDays(59), now));
    assertEquals(20, service.discount(101, null, now));
    assertEquals(20, service.discount(200, null, now));
    assertEquals(30, service.discount(201, null, now));
    assertEquals(20, service.discount(50, now.minusDays(60), now));
    assertEquals(30, service.discount(50, now.minusDays(90), now));
  }
}
