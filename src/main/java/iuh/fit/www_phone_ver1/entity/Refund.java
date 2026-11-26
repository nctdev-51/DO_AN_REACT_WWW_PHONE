package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
public class Refund {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private long version;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Order order;

  @Column(nullable = false, precision = 19, scale = 0)
  private BigDecimal amount;

  @Column(nullable = false, length = 20)
  private String status = "PENDING";

  @Column(nullable = false, length = 500)
  private String reason;

  @Column(length = 100)
  private String settlementReference;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime completedAt;

  public String getCreatedAtLabel() {
    return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
  }
}
