package iuh.fit.www_phone_ver1.service.order;

import iuh.fit.www_phone_ver1.entity.Order;
import iuh.fit.www_phone_ver1.entity.OrderDetail;
import iuh.fit.www_phone_ver1.entity.SupportRequest;
import iuh.fit.www_phone_ver1.repository.OrderRepository;
import iuh.fit.www_phone_ver1.repository.StorePolicyRepository;
import iuh.fit.www_phone_ver1.repository.SupportRequestRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {
  private final OrderRepository orders;
  private final SupportRequestRepository supportRequests;
  private final StorePolicyRepository policies;

  public DeviceService(
      OrderRepository orders,
      SupportRequestRepository supportRequests,
      StorePolicyRepository policies) {
    this.orders = orders;
    this.supportRequests = supportRequests;
    this.policies = policies;
  }

  @Getter
  @AllArgsConstructor
  public static class CustomerDevice {
    private final Long orderId;
    private final Long detailId;
    private final Long productId;
    private final String productName;
    private final String variantLabel;
    private final String sku;
    private final String image;
    private final int quantity;
    private final LocalDateTime purchasedAt;
    private final LocalDateTime deliveredAt;
    private final int warrantyMonths;
    private final LocalDate warrantyExpiresOn;
    private final long warrantyDaysRemaining;
    private final boolean warrantyActive;
    private final boolean returnEligible;
    private final List<SupportRequest> supportHistory;

    public String getPurchasedDateLabel() {
      return purchasedAt == null
          ? ""
          : purchasedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getDeliveredDateLabel() {
      return deliveredAt == null
          ? "Chưa ghi nhận"
          : deliveredAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getWarrantyExpiresLabel() {
      return warrantyExpiresOn == null
          ? "Chưa xác định"
          : warrantyExpiresOn.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
  }

  @Transactional(readOnly = true)
  public List<CustomerDevice> devicesForCustomer(String email) {
    List<Order> deliveredOrders = orders.findDeliveredWithDetailsByUserEmail(email);
    if (deliveredOrders.isEmpty()) return List.of();
    int returnDays = policies.findById(1L).map(p -> p.getReturnWindowDays()).orElse(7);

    Map<Long, List<SupportRequest>> ticketsByDetail = new HashMap<>();
    for (SupportRequest req :
        supportRequests.findByOrderDetail_Order_User_EmailOrderByCreatedAtDesc(email)) {
      if (req.getOrderDetail() != null && req.getOrderDetail().getId() != null) {
        ticketsByDetail
            .computeIfAbsent(req.getOrderDetail().getId(), ignored -> new ArrayList<>())
            .add(req);
      }
    }

    List<CustomerDevice> result = new ArrayList<>();
    LocalDate today = LocalDate.now();
    for (Order order : deliveredOrders) {
      if (!Set.of("PAID", "PARTIALLY_REFUNDED").contains(Objects.toString(order.getPaymentStatus(), ""))) {
        continue;
      }
      for (OrderDetail detail : order.getOrderDetails()) {
        int months =
            detail.getWarrantyMonths() != null
                ? detail.getWarrantyMonths()
                : (detail.getProduct() != null && detail.getProduct().getWarrantyMonths() != null
                    ? detail.getProduct().getWarrantyMonths()
                    : 12);
        LocalDate baseDate =
            order.getDeliveredAt() != null
                ? order.getDeliveredAt().toLocalDate()
                : order.getOrderDate().toLocalDate();
        LocalDate expires = months > 0 ? baseDate.plusMonths(months) : null;
        long remaining = expires == null ? 0 : ChronoUnit.DAYS.between(today, expires);
        boolean warrantyActive = expires != null && !today.isAfter(expires);
        boolean returnEligible =
            order.getDeliveredAt() != null
                && !LocalDateTime.now().isAfter(order.getDeliveredAt().plusDays(returnDays));
        String image =
            detail.getVariant() != null && detail.getVariant().getImage() != null
                ? detail.getVariant().getImage()
                : (detail.getProduct() != null ? detail.getProduct().getImage() : null);

        result.add(
            new CustomerDevice(
                order.getId(),
                detail.getId(),
                detail.getProduct() != null ? detail.getProduct().getId() : null,
                detail.getDisplayName(),
                detail.getVariantLabel(),
                detail.getSku(),
                image,
                detail.getQuantity(),
                order.getOrderDate(),
                order.getDeliveredAt(),
                months,
                expires,
                Math.max(0, remaining),
                warrantyActive,
                returnEligible,
                ticketsByDetail.getOrDefault(detail.getId(), List.of())));
      }
    }
    return result;
  }
}

