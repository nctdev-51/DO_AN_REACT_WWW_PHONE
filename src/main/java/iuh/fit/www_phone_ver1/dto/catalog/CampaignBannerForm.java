package iuh.fit.www_phone_ver1.dto.catalog;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class CampaignBannerForm {
  private Long version;

  @NotBlank
  @Size(max = 120)
  private String title;

  @Size(max = 240)
  private String subtitle;

  @NotBlank
  @Size(max = 40)
  private String actionLabel = "Khám phá sản phẩm";

  @NotBlank
  @Pattern(regexp = "PRODUCT|OFFERS|CHOOSE")
  private String target = "PRODUCT";

  private Long productId;

  @Size(max = 160)
  private String imageAlt;

  @NotBlank
  @Pattern(regexp = "LIGHT|DARK")
  private String theme = "LIGHT";

  @NotBlank
  @Pattern(regexp = "HERO|TILE")
  private String placement = "HERO";

  @Min(0)
  @Max(999)
  private int position;

  private boolean active = true;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime startsAt = LocalDateTime.now().withSecond(0).withNano(0);

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime endsAt = LocalDateTime.now().plusDays(30).withSecond(0).withNano(0);
}
