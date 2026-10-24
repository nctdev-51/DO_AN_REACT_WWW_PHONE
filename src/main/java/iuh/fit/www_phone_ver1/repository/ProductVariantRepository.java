package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
  List<ProductVariant> findByProduct_IdOrderByPriceAscIdAsc(Long productId);

  @Query(
      "select distinct v.storage from ProductVariant v where v.active=true and v.product.status=1"
          + " order by v.storage")
  List<String> availableStorage();

  @Query(
      "select distinct v.ram from ProductVariant v where v.active=true and v.product.status=1 and"
          + " v.ram is not null and v.ram<>'' order by v.ram")
  List<String> availableRam();

  @Query(
      "select distinct v.color from ProductVariant v where v.active=true and v.product.status=1"
          + " order by v.color")
  List<String> availableColors();

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select v from ProductVariant v where v.id in :ids order by v.id")
  List<ProductVariant> lockAll(@Param("ids") Collection<Long> ids);
}
