package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.LoyaltyEntry;
import java.util.List;
import org.springframework.data.jpa.repository.*;

public interface LoyaltyRepository extends JpaRepository<LoyaltyEntry, Long> {
  @Query("select coalesce(sum(e.points),0) from LoyaltyEntry e where e.user.id=:userId")
  long balance(Long userId);

  boolean existsByReference(String reference);

  List<LoyaltyEntry> findTop100ByUser_EmailOrderByIdDesc(String email);
}
