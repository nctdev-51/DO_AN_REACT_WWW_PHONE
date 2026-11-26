package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "store_policy")
@Getter
@Setter
@NoArgsConstructor
public class StorePolicy {
  @Column(nullable = false, columnDefinition = "integer default 7")
  private int returnWindowDays = 7;

  @Id private Long id = 1L;
  @Version private long version;

  @Column(nullable = false, precision = 19, scale = 0)
  private BigDecimal shippingFee = BigDecimal.valueOf(30000);

  @Column(nullable = false, precision = 19, scale = 0)
  private BigDecimal freeShippingThreshold = BigDecimal.valueOf(2000000);
}
