package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.Order;
import iuh.fit.www_phone_ver1.repository.projection.UserOrderCount;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  long countByPromotion_IdAndStatusNot(Long promotionId, String status);

  boolean existsByPromotion_IdAndUser_IdAndStatusNot(Long promotionId, Long userId, String status);

  @Query(
      "select o.user.id as userId, count(o.id) as orderCount from Order o where o.user is not null"
          + " group by o.user.id")
  List<UserOrderCount> userOrderCounts();

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from Order o where o.id = :id")
  Optional<Order> findForUpdate(@Param("id") Long id);

  List<Order> findByUser_EmailOrderByOrderDateDesc(String email);

  Optional<Order> findByIdAndUser_Email(Long id, String email);

  long countByUser_Id(Long userId);

  long countByStatus(String status);

  List<Order> findTop5ByOrderByOrderDateDesc();

  List<Order> findByStatusAndOrderDateGreaterThanEqual(String status, LocalDateTime start);

  @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.status = 'Đã giao'")
  BigDecimal deliveredRevenue();

  @Query(
      "select o from Order o where o.status='Đã giao' and"
          + " coalesce(o.deliveredAt,o.orderDate)>=:start")
  List<Order> deliveredSince(LocalDateTime start);

  Optional<Order> findByPaymentReference(String reference);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from Order o where o.paymentReference = :reference")
  Optional<Order> lockByPaymentReference(@Param("reference") String reference);

  List<Order> findByPaymentMethodAndPaymentStatusAndStatusAndOrderDateBefore(
      String method, String paymentStatus, String status, LocalDateTime cutoff);
}
