package iuh.fit.www_phone_ver1.config;

import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
public class RetailInitializer implements CommandLineRunner {
  private final StorePolicyRepository policies;
  private final PromotionRepository promotions;
  private final boolean seedEnabled;
  private final StoreExperienceRepository experience;

  public RetailInitializer(
      StorePolicyRepository policies,
      StoreExperienceRepository experience,
      PromotionRepository promotions,
      @Value("${app.catalog.seed-enabled:true}") boolean seedEnabled) {
    this.policies = policies;
    this.experience = experience;
    this.promotions = promotions;
    this.seedEnabled = seedEnabled;
  }

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    if (!experience.existsById(1L)) experience.save(new StoreExperience());
    if (!policies.existsById(1L)) policies.save(new StorePolicy());
    if (!seedEnabled || promotions.count() > 0) return;
    try (var input = getClass().getResourceAsStream("/catalog/promotions.json")) {
      if (input == null) throw new IllegalStateException("Missing catalog/promotions.json");
      for (var node : new JsonMapper().readTree(input)) {
        Promotion promotion = new Promotion();
        promotion.setCode(node.path("code").asString());
        promotion.setScopeBrand(node.path("scopeBrand").asString(""));
        promotion.setScopeSkus(node.path("scopeSkus").asString(""));
        promotion.setRequiredSkus(node.path("requiredSkus").asString(""));
        promotion.setDescription(node.path("description").asString());
        promotion.setDiscountType(node.path("discountType").asString());
        promotion.setAmount(new BigDecimal(node.path("amount").asString()));
        promotion.setMinimumSubtotal(new BigDecimal(node.path("minimumSubtotal").asString()));
        if (!node.path("maximumDiscount").isNull())
          promotion.setMaximumDiscount(new BigDecimal(node.path("maximumDiscount").asString()));
        promotion.setUsageLimit(node.path("usageLimit").asInt());
        promotion.setStartsAt(LocalDateTime.now().minusDays(1));
        promotion.setEndsAt(LocalDateTime.now().plusDays(node.path("validDays").asInt()));
        promotions.save(promotion);
      }
    }
  }
}
