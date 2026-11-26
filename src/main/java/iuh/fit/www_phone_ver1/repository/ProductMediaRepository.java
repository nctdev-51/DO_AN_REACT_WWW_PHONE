package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.ProductMedia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMediaRepository extends JpaRepository<ProductMedia, Long> {
  List<ProductMedia> findByProduct_IdOrderBySortOrderAscIdAsc(Long productId);

  boolean existsByProduct_IdAndPath(Long productId, String path);

  void deleteByProduct_Id(Long productId);
}
