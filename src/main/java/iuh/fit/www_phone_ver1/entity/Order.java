package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

  @Column(length = 10)
  private String provinceCode;

  @Column(length = 10)
  private String wardCode;

  @Column(length = 12)
  private String ghnEnvironment;

  @Column(columnDefinition = "TEXT")
  @ToString.Exclude
  private String ghnRequestJson;

  @Column(length = 40)
  private String carrierStatus;

  private LocalDateTime carrierUpdatedAt;

  public String getCarrierStatusLabel() {
    return switch (java.util.Objects.toString(carrierStatus, "")) {
      case "ready_to_pick" -> "Chờ GHN lấy hàng";
      case "picking" -> "GHN đang đến lấy";
      case "picked" -> "GHN đã lấy hàng";
      case "storing", "sorting" -> "Đang xử lý tại kho GHN";
      case "transporting" -> "Đang trung chuyển";
      case "delivering", "money_collect_delivering" -> "GHN đang giao";
      case "delivered" -> "GHN báo giao thành công";
      case "cancel" -> "GHN đã hủy vận đơn";
      case "returned" -> "GHN đã trả về cửa hàng";
      case "UNCONFIRMED" -> "Chưa xác nhận kết quả tạo vận đơn";
      default -> "Cần kiểm tra tiến trình vận chuyển";
    };
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, columnDefinition = "bigint default 0")
  @Builder.Default
  private long pointsSpent = 0;

  @Column(nullable = false, columnDefinition = "bigint default 0")
  @Builder.Default
  private long pointsEarned = 0;

  @Column(nullable = false, columnDefinition = "bigint default 0")
  @Builder.Default
  private long pointsReversed = 0;

  @Column(nullable = false, columnDefinition = "bigint default 0")
  @Builder.Default
  private long pointsRestored = 0;

  @Column(nullable = false, precision = 19, scale = 0, columnDefinition = "decimal(19,0) default 0")
  @Builder.Default
  private BigDecimal pointsDiscount = BigDecimal.ZERO;

  @Column(precision = 19, scale = 0)
  private BigDecimal earnEverySnapshot;

  @Column(length = 80)
  private String carrier;

  @Column(length = 80)
  private String trackingCode;

  @Column(length = 100)
  private String province;

  @Column(length = 100)
  private String ward;

  private java.time.LocalDate estimatedDeliveryFrom;
  private java.time.LocalDate estimatedDeliveryTo;

  @Column(name = "order_date", nullable = false)
  @Builder.Default
  private LocalDateTime orderDate = LocalDateTime.now();

  @Column(name = "total_amount", nullable = false, precision = 19, scale = 0)
  private BigDecimal totalAmount;

  @Column(precision = 19, scale = 0)
  private BigDecimal subtotal;

  @Column(nullable = false, precision = 19, scale = 0, columnDefinition = "decimal(19,0) default 0")
  @Builder.Default
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(nullable = false, precision = 19, scale = 0, columnDefinition = "decimal(19,0) default 0")
  @Builder.Default
  private BigDecimal shippingFee = BigDecimal.ZERO;

  @Column(length = 40)
  private String promotionCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "promotion_id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Promotion promotion;

  public String getFullShippingAddress() {
    var parts = new java.util.ArrayList<String>();
    if (shippingAddress != null && !shippingAddress.isBlank()) parts.add(shippingAddress.trim());
    for (String part :
        java.util.List.of(
            java.util.Objects.toString(ward, ""), java.util.Objects.toString(province, "")))
      if (!part.isBlank()
          && (shippingAddress == null
              || !shippingAddress
                  .toLowerCase(java.util.Locale.ROOT)
                  .contains(part.toLowerCase(java.util.Locale.ROOT)))) parts.add(part);
    return String.join(", ", parts);
  }

  public String getEstimatedDeliveryLabel() {
    var f = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
    return estimatedDeliveryFrom == null
        ? ""
        : estimatedDeliveryFrom.format(f) + " – " + estimatedDeliveryTo.format(f);
  }

  public BigDecimal getSubtotal() {
    return subtotal == null
        ? orderDetails.stream()
            .map(OrderDetail::getSubTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
        : subtotal;
  }

  @Builder.Default
  @Column(length = 20, columnDefinition = "varchar(20) default 'COD'")
  private String paymentMethod = "COD";

  @Builder.Default
  @Column(length = 30, columnDefinition = "varchar(30) default 'UNPAID'")
  private String paymentStatus = "UNPAID";

  @Column(unique = true, length = 80)
  private String paymentReference;

  @Column(length = 100)
  private String gatewayTransaction;

  @Column(length = 100)
  private String recipientName;

  @Column(length = 500)
  private String cancellationReason;

  @Column(nullable = false, length = 50)
  private String status; // Chờ xử lý, Đang giao, Đã giao, Đã hủy...

  @Column(name = "shipping_address", length = 255)
  private String shippingAddress;

  @Column(length = 20)
  private String phone;

  private LocalDateTime deliveredAt;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt ASC, id ASC")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  private List<OrderEvent> events = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private User user;

  @OneToMany(
      mappedBy = "order",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  private List<OrderDetail> orderDetails = new ArrayList<>();
}
