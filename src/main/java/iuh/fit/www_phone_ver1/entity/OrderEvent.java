package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "order_events")
@Getter
@Setter
@NoArgsConstructor
public class OrderEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(nullable = false, length = 30)
  private String actor;

  @Column(nullable = false, length = 500)
  private String note;

  @Column(nullable = false, length = 50)
  private String orderStatus;

  @Column(nullable = false, length = 30)
  private String paymentStatus;
}
