package iuh.fit.www_phone_ver1.dto.catalog;

import iuh.fit.www_phone_ver1.entity.ProductVariant;
import java.util.List;
import lombok.*;

@Getter
@AllArgsConstructor
public class ComparisonView {
  private final List<ProductVariant> variants;
  private final List<Row> rows;
  private final String sharePath;

  @Getter
  @AllArgsConstructor
  public static class Row {
    private final String label;
    private final List<String> values;
    private final boolean different;
  }
}
