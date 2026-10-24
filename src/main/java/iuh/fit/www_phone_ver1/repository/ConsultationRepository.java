package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.Consultation;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
  long countByProduct_Id(Long productId);

  List<Consultation> findByUser_EmailOrderByIdDesc(String email);

  long countByUser_EmailAndStatusIn(String email, Collection<String> statuses);
}
