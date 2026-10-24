package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "support_requests")
@Getter
@Setter
@NoArgsConstructor
public class SupportRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_detail_id", nullable = false)
  private OrderDetail orderDetail;

  @Column(nullable = false, columnDefinition = "boolean default false")
  private boolean inspected;

  @Column(nullable = false, columnDefinition = "boolean default false")
  private boolean restocked;

  @Column(length = 500)
  private String inspectionNote;

  @Column(nullable = false, length = 12)
  private String type;

  @Column(nullable = false, length = 20)
  private String status = "REQUESTED";

  @Column(nullable = false)
  private int quantity;

  @Column(nullable = false, length = 2000)
  private String reason;

  @Column(length = 2000)
  private String response;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  public String getTypeLabel() {
    return "RETURN".equals(type) ? "Đổi / trả hàng" : "Bảo hành";
  }

  public String getStatusLabel() {
    return switch (status) {
      case "REQUESTED" -> "Chờ tiếp nhận";
      case "APPROVED" -> "Đã duyệt";
      case "RECEIVED" -> "Đã nhận thiết bị";
      case "COMPLETED" -> "Đã xử lý xong";
      case "REJECTED" -> "Từ chối";
      case "CANCELLED" -> "Khách đã hủy";
      default -> status;
    };
  }
}
