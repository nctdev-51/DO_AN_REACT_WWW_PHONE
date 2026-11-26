package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.CampaignBanner;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CampaignBannerRepository extends JpaRepository<CampaignBanner, Long> {
  @Query("""
      select b from CampaignBanner b left join fetch b.product
      where lower(b.title) like :title escape '!'
        and (:from is null or b.endsAt > :from)
        and (:until is null or b.startsAt < :until)
        and (:state = ''
          or (:state = 'off' and b.active = false)
          or (:state = 'scheduled' and b.active = true and b.startsAt > :now)
          or (:state = 'live' and b.active = true and b.startsAt <= :now and b.endsAt > :now)
          or (:state = 'expired' and b.active = true and b.endsAt <= :now))
      order by b.position, b.id
      """)
  List<CampaignBanner> searchSchedule(@Param("title") String title,
      @Param("from") LocalDateTime from, @Param("until") LocalDateTime until,
      @Param("state") String state, @Param("now") LocalDateTime now);

  @Query(
      "select b from CampaignBanner b left join fetch b.product p where b.active=true and"
          + " b.startsAt<=:now and b.endsAt>:now and (b.target<>'PRODUCT' or p.status=1) order by"
          + " b.position,b.id")
  List<CampaignBanner> liveAt(LocalDateTime now);

  long countByProduct_Id(Long productId);
}
