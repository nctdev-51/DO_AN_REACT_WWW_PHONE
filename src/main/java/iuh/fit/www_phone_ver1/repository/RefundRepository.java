package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.Refund;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.*;

public interface RefundRepository extends JpaRepository<Refund, Long> {
  @Query(
      "select coalesce(sum(r.amount),0) from Refund r where r.status='COMPLETED' and"
          + " r.order.status='Đã giao'")
  BigDecimal completedDeliveredTotal();

  @Query(
      "select r.order.id as orderId,sum(r.amount) as amount from Refund r where"
          + " r.status='COMPLETED' and r.order.status='Đã giao' and"
          + " coalesce(r.order.deliveredAt,r.order.orderDate)>=:start group by r.order.id")
  List<iuh.fit.www_phone_ver1.repository.projection.OrderRefundTotal> deliveredTotalsSince(
      java.time.LocalDateTime start);

  List<Refund> findByOrder_IdOrderByIdDesc(Long orderId);

  @Query(
      "select coalesce(sum(r.amount),0) from Refund r where r.order.id=:orderId and r.status in"
          + " :statuses")
  BigDecimal total(Long orderId, List<String> statuses);
}
