package iuh.fit.www_phone_ver1.service.order;

import iuh.fit.www_phone_ver1.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryService {
  private final OrderRepository orders;
  private final OrderHistoryService history;

  public DeliveryService(OrderRepository orders, OrderHistoryService history) {
    this.orders = orders;
    this.history = history;
  }

  @Transactional
  public void update(Long id, String carrier, String tracking, String note) {
    var o =
        orders
            .findForUpdate(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn."));
    if (!java.util.Set.of(OrderStatuses.PENDING, OrderStatuses.SHIPPING).contains(o.getStatus()))
      throw new IllegalArgumentException("Chỉ sửa vận đơn của đơn chờ xử lý hoặc đang giao.");
    if (o.getGhnRequestJson() != null)
      throw new IllegalArgumentException("Đơn đã gửi GHN; dùng Đồng bộ GHN để cập nhật vận đơn.");
    if (carrier == null
        || carrier.isBlank()
        || carrier.length() > 80
        || tracking == null
        || !tracking.matches("[A-Za-z0-9_-]{3,80}")
        || note == null
        || note.isBlank()
        || note.length() > 500)
      throw new IllegalArgumentException(
          "Nhập hãng, mã vận đơn 3–80 ký tự và ghi chú tối đa 500 ký tự.");
    o.setCarrier(carrier.trim());
    o.setTrackingCode(tracking);
    history.append(
        o, "Quản trị viên", "Vận chuyển " + carrier.trim() + " · " + tracking + ": " + note.trim());
  }
}
