package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "campaign_banners")
@Getter
@Setter
@NoArgsConstructor
public class CampaignBanner {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private long version;

  @Column(nullable = false, length = 120)
  private String title;

  @Column(length = 240)
  private String subtitle;

  @Column(nullable = false, length = 40)
  private String actionLabel = "Khám phá sản phẩm";

  @Column(nullable = false, length = 20)
  private String target = "PRODUCT";

  @ManyToOne(fetch = FetchType.LAZY)
  private Product product;

  @Column(length = 255)
  private String image;

  @Column(length = 160)
  private String imageAlt;

  @Column(nullable = false, length = 10)
  private String theme = "LIGHT";

  @Column(nullable = false, length = 10)
  private String placement = "HERO";

  @Column(nullable = false)
  private int position;

  @Column(nullable = false)
  private boolean active = true;

  @Column(nullable = false)
  private LocalDateTime startsAt = LocalDateTime.now();

  @Column(nullable = false)
  private LocalDateTime endsAt = LocalDateTime.now().plusDays(30);

  public String getImagePath() {
    return image != null && !image.isBlank() ? image : product == null ? null : product.getImage();
  }

  public String getImageLabel() {
    return imageAlt != null && !imageAlt.isBlank()
        ? imageAlt
        : product == null ? title : product.getName();
  }

  public String getTargetPath() {
    return switch (target) {
      case "OFFERS" -> "/offers";
      case "CHOOSE" -> "/choose";
      default -> "/products/" + product.getId();
    };
  }

  public String getScheduleLabel() {
    var f = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    return startsAt.format(f) + " – " + endsAt.format(f);
  }
}
