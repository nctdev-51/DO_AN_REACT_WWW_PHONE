package iuh.fit.www_phone_ver1.service.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import iuh.fit.www_phone_ver1.entity.User;
import iuh.fit.www_phone_ver1.entity.mongo.UserEvent;
import iuh.fit.www_phone_ver1.repository.UserRepository;
import iuh.fit.www_phone_ver1.repository.mongo.UserEventRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TrackingPersistenceServiceTests {
  @Test
  void resolvesAuthenticatedUserFromServerAndSavesEvent() {
    UserEventRepository events = mock(UserEventRepository.class);
    UserRepository users = mock(UserRepository.class);
    User user = new User();
    user.setId(42L);
    when(users.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
    TrackingPersistenceService persistence = new TrackingPersistenceService(events, users);
    UserEvent event = new UserEvent();

    persistence.persist(event, "customer@example.com");

    assertEquals(42L, event.getUserId());
    verify(events).save(event);
  }

  @Test
  void savesAnonymousEventWithoutLookingUpAUser() {
    UserEventRepository events = mock(UserEventRepository.class);
    UserRepository users = mock(UserRepository.class);
    TrackingPersistenceService persistence = new TrackingPersistenceService(events, users);
    UserEvent event = new UserEvent();

    persistence.persist(event, null);

    verify(users, never()).findByEmail(org.mockito.ArgumentMatchers.anyString());
    verify(events).save(event);
  }
}
