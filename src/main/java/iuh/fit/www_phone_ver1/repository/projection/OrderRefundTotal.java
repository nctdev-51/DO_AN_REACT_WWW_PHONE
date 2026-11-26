package iuh.fit.www_phone_ver1.repository.projection;

import java.math.BigDecimal;

public interface OrderRefundTotal {
  Long getOrderId();

  BigDecimal getAmount();
}
