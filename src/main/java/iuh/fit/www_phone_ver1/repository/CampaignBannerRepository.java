package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.CampaignBanner;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;

public interface CampaignBannerRepository extends JpaRepository<CampaignBanner, Long> {
  @Query(
      "select b from CampaignBanner b left join fetch b.product p where b.active=true and"
          + " b.startsAt<=:now and b.endsAt>:now and (b.target<>'PRODUCT' or p.status=1) order by"
          + " b.position,b.id")
  List<CampaignBanner> liveAt(LocalDateTime now);

  long countByProduct_Id(Long productId);
}
