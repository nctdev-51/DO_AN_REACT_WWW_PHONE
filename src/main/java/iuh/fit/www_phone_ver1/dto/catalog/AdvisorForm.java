package iuh.fit.www_phone_ver1.dto.catalog;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdvisorForm {
  private BigDecimal budget;
  private String need;
  private String priority;
  private String sizePreference = "ANY";
  private String brand;
  private Integer step = 1;
}

