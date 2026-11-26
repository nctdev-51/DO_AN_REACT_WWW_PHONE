package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "consultations")
@Getter
@Setter
@NoArgsConstructor
public class Consultation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private long version;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Product product;

  @Column(nullable = false, length = 20)
  private String type;

  @Column(nullable = false, length = 20)
  private String status = "REQUESTED";

  @Column(nullable = false, length = 100)
  private String contactName;

  @Column(nullable = false, length = 20)
  private String phone;

  @Column(nullable = false, length = 1500)
  private String message;

  @Column(length = 1500)
  private String response;

  @Column(precision = 19, scale = 0)
  private BigDecimal quotedAmount;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime updatedAt;

  public String getTypeLabel() {
    return "TRADE_IN".equals(type) ? "Thu cũ đổi mới" : "Tư vấn trả góp";
  }

  public String getStatusLabel() {
    return switch (status) {
      case "CONTACTED" -> "Đã tiếp nhận";
      case "QUOTED" -> "Đã gửi phương án";
      case "ACCEPTED" -> "Đã duyệt";
      case "REJECTED" -> "Không phù hợp";
      case "CANCELLED" -> "Đã hủy";
      default -> "Chờ tiếp nhận";
    };
  }

  public String getCreatedAtLabel() {
    return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
  }
}
