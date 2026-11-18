package iuh.fit.www_phone_ver1.repository.mongo;

import iuh.fit.www_phone_ver1.entity.mongo.UserEvent;
import java.time.LocalDateTime;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserEventRepository extends MongoRepository<UserEvent, String> {
  long countByEventTypeAndCreatedAtBetween(
      String eventType, LocalDateTime from, LocalDateTime to);

  long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
