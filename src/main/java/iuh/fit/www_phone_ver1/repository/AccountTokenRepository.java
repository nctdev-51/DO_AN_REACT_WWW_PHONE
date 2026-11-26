package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.AccountToken;
import iuh.fit.www_phone_ver1.entity.AccountToken.Purpose;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {
  Optional<AccountToken> findByDigest(String digest);

  Optional<AccountToken> findFirstByUser_IdAndPurposeOrderByCreatedAtDesc(
      Long userId, Purpose purpose);

  List<AccountToken> findByUser_IdAndPurposeAndConsumedAtIsNull(Long userId, Purpose purpose);

  void deleteByUser_Id(Long userId);
}
