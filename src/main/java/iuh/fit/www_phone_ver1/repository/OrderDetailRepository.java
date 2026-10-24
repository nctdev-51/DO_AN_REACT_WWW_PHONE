package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
  boolean existsByProduct_Id(Long productId);

  boolean existsByProduct_IdAndOrder_User_EmailAndOrder_Status(
      Long productId, String email, String status);
}
