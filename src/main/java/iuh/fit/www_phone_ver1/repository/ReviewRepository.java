package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.Review;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  List<Review> findByProduct_IdOrderByCreatedAtDesc(Long productId);

  Optional<Review> findByUser_EmailAndProduct_Id(String email, Long productId);

  void deleteByUser_Id(Long userId);

  void deleteByProduct_Id(Long productId);
}
