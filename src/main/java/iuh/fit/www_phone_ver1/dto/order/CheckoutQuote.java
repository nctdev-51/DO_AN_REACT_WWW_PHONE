package iuh.fit.www_phone_ver1.dto.order;

import iuh.fit.www_phone_ver1.entity.Promotion;
import java.math.BigDecimal;
import lombok.*;

@Getter
@AllArgsConstructor
public class CheckoutQuote {
  private final BigDecimal subtotal;
  private final BigDecimal discount;
  private final BigDecimal shippingFee;
  private final BigDecimal total;
  private final Promotion promotion;

  public void validatePointsCombination(long requestedPoints) {
    if (requestedPoints > 0 && promotion != null && !promotion.isCombinableWithPoints()) {
      throw new IllegalArgumentException("Mã này không áp dụng cùng điểm thưởng.");
    }
  }
}
