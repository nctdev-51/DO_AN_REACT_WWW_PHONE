package iuh.fit.www_phone_ver1.service.order;

import iuh.fit.www_phone_ver1.dto.order.ConsultationForm;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultationService {
  private final ConsultationRepository requests;
  private final UserRepository users;
  private final ProductRepository products;

  public ConsultationService(
      ConsultationRepository requests, UserRepository users, ProductRepository products) {
    this.requests = requests;
    this.users = users;
    this.products = products;
  }

  @Transactional
  public void create(String email, ConsultationForm form) {
    var user =
        users
            .lockByEmail(email)
            .filter(u -> u.getStatus() == 1)
            .orElseThrow(() -> new IllegalArgumentException("Tài khoản không khả dụng."));
    if (requests.countByUser_EmailAndStatusIn(email, List.of("REQUESTED", "CONTACTED", "QUOTED"))
        >= 5)
      throw new IllegalArgumentException(
          "Bạn đang có 5 yêu cầu chờ xử lý. Hãy chờ phản hồi hoặc hủy yêu cầu cũ.");
    var product =
        products
            .findById(form.getProductId())
            .filter(p -> p.getStatus() == 1)
            .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không còn khả dụng."));
    var r = new Consultation();
    r.setUser(user);
    r.setProduct(product);
    r.setType(form.getType());
    r.setContactName(form.getContactName().trim());
    r.setPhone(form.getPhone());
    r.setMessage(form.getMessage().trim());
    requests.save(r);
  }

  @Transactional
  public void update(Long id, long version, String status, String response, BigDecimal amount) {
    var r =
        requests
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu."));
    if (r.getVersion() != version)
      throw new IllegalArgumentException("Yêu cầu vừa thay đổi. Hãy tải lại.");
    if (!Set.of("CONTACTED", "QUOTED", "ACCEPTED", "REJECTED").contains(status))
      throw new IllegalArgumentException("Trạng thái không hợp lệ.");
    boolean valid =
        switch (r.getStatus()) {
          case "REQUESTED" -> Set.of("CONTACTED", "REJECTED").contains(status);
          case "CONTACTED" -> Set.of("QUOTED", "REJECTED").contains(status);
          case "QUOTED" -> Set.of("ACCEPTED", "REJECTED", "QUOTED").contains(status);
          default -> false;
        };
    if (!valid) throw new IllegalArgumentException("Không thể chuyển trạng thái yêu cầu này.");
    if (response == null || response.isBlank() || response.length() > 1500)
      throw new IllegalArgumentException("Nhập phản hồi từ 1 đến 1.500 ký tự.");
    if (amount != null
        && (amount.signum() < 0
            || amount.scale() > 0
            || amount.compareTo(BigDecimal.valueOf(1000000000)) > 0))
      throw new IllegalArgumentException("Số tiền phương án không hợp lệ.");
    if ("QUOTED".equals(status) && amount == null)
      throw new IllegalArgumentException("Nhập giá trị phương án để báo khách.");
    r.setStatus(status);
    r.setResponse(response.trim());
    r.setQuotedAmount(amount);
    r.setUpdatedAt(LocalDateTime.now());
    requests.saveAndFlush(r);
  }

  @Transactional
  public void cancel(Long id, long version, String email) {
    var r =
        requests
            .findById(id)
            .filter(q -> q.getUser().getEmail().equals(email))
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu."));
    if (r.getVersion() != version
        || !Set.of("REQUESTED", "CONTACTED", "QUOTED").contains(r.getStatus()))
      throw new IllegalArgumentException("Yêu cầu không thể hủy hoặc vừa thay đổi.");
    r.setStatus("CANCELLED");
    r.setUpdatedAt(LocalDateTime.now());
    requests.saveAndFlush(r);
  }
}
