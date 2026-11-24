package iuh.fit.www_phone_ver1.service.tracking;

import com.mongodb.MongoException;
import iuh.fit.www_phone_ver1.entity.mongo.UserEvent;
import iuh.fit.www_phone_ver1.repository.UserRepository;
import iuh.fit.www_phone_ver1.repository.mongo.UserEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TrackingPersistenceService {
  private static final Logger log = LoggerFactory.getLogger(TrackingPersistenceService.class);

  private final UserEventRepository events;
  private final UserRepository users;

  public TrackingPersistenceService(UserEventRepository events, UserRepository users) {
    this.events = events;
    this.users = users;
  }

  @Async("trackingExecutor")
  public void persist(UserEvent event, String authenticatedEmail) {
    if (authenticatedEmail != null) {
      try {
        users.findByEmail(authenticatedEmail).ifPresent(user -> event.setUserId(user.getId()));
      } catch (DataAccessException ex) {
        log.warn("Could not resolve tracking user: {}", ex.getMessage());
      }
    }
    try {
      events.save(event);
    } catch (DataAccessException | MongoException ex) {
      log.warn("Could not persist tracking event {}: {}", event.getEventType(), ex.getMessage());
    }
  }
}
