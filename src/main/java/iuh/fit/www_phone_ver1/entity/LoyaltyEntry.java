package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "loyalty_entries")
@Getter
@Setter
@NoArgsConstructor
public class LoyaltyEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Order order;

  @Column(nullable = false, unique = true, length = 100)
  private String reference;

  @Column(nullable = false)
  private long points;

  @Column(nullable = false, length = 200)
  private String description;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  public String getCreatedAtLabel() {
    return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
  }
}
