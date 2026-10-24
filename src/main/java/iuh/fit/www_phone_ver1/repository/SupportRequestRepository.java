package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.SupportRequest;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
  List<SupportRequest> findByOrderDetail_Order_User_EmailOrderByCreatedAtDesc(String email);

  List<SupportRequest> findByOrderDetail_Order_IdOrderByCreatedAtDesc(Long orderId);

  List<SupportRequest> findByOrderDetail_Id(Long detailId);
}
