package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.WishlistEntry;
import java.util.List;
import org.springframework.data.jpa.repository.*;

public interface WishlistRepository extends JpaRepository<WishlistEntry, Long> {
  @EntityGraph(attributePaths = {"product"})
  List<WishlistEntry> findByUser_EmailOrderByCreatedAtDescIdDesc(String email);

  boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

  long countByUser_Id(Long userId);

  void deleteByUser_IdAndProduct_Id(Long userId, Long productId);

  void deleteByUser_Id(Long userId);

  void deleteByProduct_Id(Long productId);
}
