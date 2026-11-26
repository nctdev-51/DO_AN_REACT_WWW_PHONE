package iuh.fit.www_phone_ver1.dto.catalog;

import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.entity.ProductVariant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdvisorRecommendation {
  private final Product product;
  private final ProductVariant recommendedVariant;
  private final int score;
  private final String headlineReason;
  private final List<String> reasons;
  private final boolean withinBudget;
}

