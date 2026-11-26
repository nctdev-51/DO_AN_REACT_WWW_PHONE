package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.Promotion;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
  Optional<Promotion> findByCode(String code);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Promotion p where p.code = :code")
  Optional<Promotion> lockByCode(@Param("code") String code);

  List<Promotion> findByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByEndsAtAsc(
      LocalDateTime now, LocalDateTime end);
}
