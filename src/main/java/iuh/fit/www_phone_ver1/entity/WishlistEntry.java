package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
    name = "wishlist_entries",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Getter
@Setter
@NoArgsConstructor
public class WishlistEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}
