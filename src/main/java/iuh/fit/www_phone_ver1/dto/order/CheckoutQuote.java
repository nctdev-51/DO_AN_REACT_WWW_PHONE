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
}
