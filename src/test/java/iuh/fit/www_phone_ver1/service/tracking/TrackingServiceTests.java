package iuh.fit.www_phone_ver1.service.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import iuh.fit.www_phone_ver1.entity.mongo.UserEvent;
import iuh.fit.www_phone_ver1.service.ai.ChatTopic;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class TrackingServiceTests {
  private final TrackingPersistenceService persistence = mock(TrackingPersistenceService.class);
  private final TrackingService tracking = new TrackingService(persistence);
  private final HttpServletRequest request = mock(HttpServletRequest.class);

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createsServerAssociatedEventAndDropsUnapprovedMetadata() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "customer@example.com", "credentials", List.of()));
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("203.0.113.8");
    when(request.getHeader("User-Agent")).thenReturn("test-agent");

    assertTrue(
        tracking.track(
            "session-id",
            "chat_message",
            null,
            null,
            "/api/ai/chat?token=private",
            Map.of("durationMs", 40, "quantity", 2, "topic", ChatTopic.WARRANTY,
                "secret", "private"),
            request));

    ArgumentCaptor<UserEvent> eventCaptor = ArgumentCaptor.forClass(UserEvent.class);
    verify(persistence).persist(eventCaptor.capture(), eq("customer@example.com"));
    UserEvent event = eventCaptor.getValue();
    assertEquals("CHAT_MESSAGE", event.getEventType());
    assertEquals("/api/ai/chat", event.getPageUrl());
    assertEquals(Map.of("durationMs", 40, "quantity", 2, "topic", "Bảo hành"), event.getMeta());
    assertNotEquals("203.0.113.8", event.getIpHash());
    assertEquals(64, event.getIpHash().length());
    assertNull(event.getUserId());
  }

  @Test
  void rejectsUnknownEventTypesWithoutSchedulingPersistence() {
    assertFalse(tracking.track("session-id", "UNKNOWN", null, null, "/", null, request));

    verify(persistence, never()).persist(any(UserEvent.class), any());
  }
}
