package iuh.fit.www_phone_ver1.service.order;

import iuh.fit.www_phone_ver1.dto.order.SupportRequestForm;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class SupportService {
  private final SupportRequestRepository requests;
  private final OrderRepository orders;
  private final StorePolicyRepository policies;
  private final OrderHistoryService history;

  public SupportService(
      SupportRequestRepository requests,
      OrderRepository orders,
      StorePolicyRepository policies,
      OrderHistoryService history) {
    this.requests = requests;
    this.orders = orders;
    this.policies = policies;
    this.history = history;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void create(Long orderId, String email, SupportRequestForm form) {
    Order order =
        orders
            .findForUpdate(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
    if (order.getUser() == null || !email.equals(order.getUser().getEmail()))
      throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
    if (!OrderStatuses.DELIVERED.equals(order.getStatus())
        || !Set.of("PAID", "PARTIALLY_REFUNDED").contains(order.getPaymentStatus()))
      throw new IllegalArgumentException("Chỉ tiếp nhận yêu cầu cho đơn đã giao và thanh toán.");
    if (order.getDeliveredAt() == null)
      throw new IllegalArgumentException(
          "Đơn cũ chưa có ngày giao hàng. Vui lòng liên hệ cửa hàng để kiểm tra.");
    OrderDetail detail =
        order.getOrderDetails().stream()
            .filter(d -> d.getId().equals(form.getDetailId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không thuộc đơn hàng."));
    if (!Set.of("RETURN", "WARRANTY").contains(form.getType() == null ? "" : form.getType())
        || form.getQuantity() < 1
        || form.getQuantity() > detail.getQuantity())
      throw new IllegalArgumentException("Loại yêu cầu hoặc số lượng không hợp lệ.");
    if (form.getReason() == null
        || form.getReason().trim().length() < 10
        || form.getReason().length() > 2000)
      throw new IllegalArgumentException("Nội dung yêu cầu từ 10 đến 2000 ký tự.");
    LocalDateTime deadline;
    if ("RETURN".equals(form.getType()))
      deadline =
          order
              .getDeliveredAt()
              .plusDays(policies.findById(1L).orElseThrow().getReturnWindowDays());
    else {
      Integer months =
          detail.getWarrantyMonths() == null
              ? detail.getProduct().getWarrantyMonths()
              : detail.getWarrantyMonths();
      if (months == null || months < 1)
        throw new IllegalArgumentException("Sản phẩm không còn điều kiện bảo hành.");
      deadline = order.getDeliveredAt().plusMonths(months);
    }
    if (LocalDateTime.now().isAfter(deadline))
      throw new IllegalArgumentException("Đã quá thời hạn tiếp nhận loại yêu cầu này.");
    int reserved =
        requests.findByOrderDetail_Id(detail.getId()).stream()
            .filter(r -> !Set.of("REJECTED", "CANCELLED").contains(r.getStatus()))
            .filter(r -> !"COMPLETED".equals(r.getStatus()) || "RETURN".equals(r.getType()))
            .mapToInt(SupportRequest::getQuantity)
            .sum();
    if (reserved + form.getQuantity() > detail.getQuantity())
      throw new IllegalArgumentException("Số lượng đã có yêu cầu đang xử lý hoặc đã đổi/trả.");
    SupportRequest request = new SupportRequest();
    request.setOrderDetail(detail);
    request.setType(form.getType());
    request.setQuantity(form.getQuantity());
    request.setReason(form.getReason().trim());
    requests.save(request);
    history.append(
        order,
        "Khách hàng",
        "Gửi yêu cầu "
            + request.getTypeLabel()
            + " cho "
            + detail.getDisplayName()
            + " ("
            + form.getQuantity()
            + " sản phẩm).");
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void update(Long id, String next, String note) {
    SupportRequest request = locked(id);
    if (note == null || note.trim().length() < 10 || note.length() > 2000)
      throw new IllegalArgumentException("Phản hồi xử lý từ 10 đến 2000 ký tự.");
    boolean valid =
        switch (request.getStatus()) {
          case "REQUESTED" -> Set.of("APPROVED", "REJECTED").contains(next);
          case "APPROVED" -> Set.of("RECEIVED", "REJECTED").contains(next);
          case "RECEIVED" -> "COMPLETED".equals(next);
          default -> false;
        };
    if (!valid)
      throw new IllegalArgumentException("Không thể chuyển trạng thái yêu cầu như đã chọn.");
    request.setStatus(next);
    request.setResponse(note.trim());
    request.setUpdatedAt(LocalDateTime.now());
    history.append(
        request.getOrderDetail().getOrder(),
        "Cửa hàng",
        "Yêu cầu #" + id + ": " + request.getStatusLabel() + " — " + note.trim());
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void cancel(Long id, String email) {
    SupportRequest request = locked(id);
    if (!email.equals(request.getOrderDetail().getOrder().getUser().getEmail()))
      throw new IllegalArgumentException("Không tìm thấy yêu cầu.");
    if (!"REQUESTED".equals(request.getStatus()))
      throw new IllegalArgumentException("Chỉ hủy được yêu cầu chưa tiếp nhận.");
    request.setStatus("CANCELLED");
    request.setUpdatedAt(LocalDateTime.now());
    history.append(
        request.getOrderDetail().getOrder(), "Khách hàng", "Hủy yêu cầu hỗ trợ #" + id + ".");
  }

  @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager stockManager;

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void inspect(Long id, boolean resellable, String note) {
    var request = locked(id);
    if (!"RETURN".equals(request.getType())
        || !Set.of("RECEIVED", "COMPLETED").contains(request.getStatus())
        || request.isInspected())
      throw new IllegalArgumentException("Chỉ kiểm tra một lần cho hàng trả đã nhận.");
    if (note == null || note.trim().length() < 10 || note.length() > 500)
      throw new IllegalArgumentException("Kết quả kiểm tra từ 10 đến 500 ký tự.");
    if (resellable) {
      var variant = request.getOrderDetail().getVariant();
      if (variant == null) throw new IllegalArgumentException("Không có biến thể để nhập lại tồn.");
      stockManager.refresh(variant, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
      variant.setStock(Math.addExact(variant.getStock(), request.getQuantity()));
    }
    request.setInspected(true);
    request.setRestocked(resellable);
    request.setInspectionNote(note.trim());
    history.append(
        request.getOrderDetail().getOrder(),
        "Quản trị viên",
        "Kiểm tra hàng trả #"
            + id
            + ": "
            + (resellable
                ? "Đã nhập lại tồn " + request.getQuantity() + " sản phẩm. "
                : "Không nhập tồn bán mới. ")
            + note.trim());
  }

  private SupportRequest locked(Long id) {
    SupportRequest request =
        requests
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu."));
    orders.findForUpdate(request.getOrderDetail().getOrder().getId()).orElseThrow();
    entityManager.flush();
    entityManager.refresh(request, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    return request;
  }

  @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
}
