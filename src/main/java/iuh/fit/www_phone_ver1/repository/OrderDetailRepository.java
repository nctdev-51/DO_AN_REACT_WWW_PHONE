package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.OrderDetail;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
  boolean existsByProduct_Id(Long productId);

  boolean existsByProduct_IdAndOrder_User_EmailAndOrder_Status(
      Long productId, String email, String status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT d FROM OrderDetail d WHERE d.id = :id AND d.order.id = :orderId")
  Optional<OrderDetail> findForUpdate(Long id, Long orderId);
}
