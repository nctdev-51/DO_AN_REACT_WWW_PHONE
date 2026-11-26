package iuh.fit.www_phone_ver1.service.tracking;

import iuh.fit.www_phone_ver1.entity.mongo.UserEvent;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

@Service
public class TrackingService {
  private static final Logger log = LoggerFactory.getLogger(TrackingService.class);
  private static final Set<String> EVENT_TYPES =
      Set.of(
          "PAGE_VIEW",
          "PRODUCT_CLICK",
          "ADD_TO_CART",
          "PURCHASE",
          "SEARCH",
          "CHAT_MESSAGE",
          "AI_DESCRIPTION_VIEW");

  private final TrackingPersistenceService persistence;

  public TrackingService(TrackingPersistenceService persistence) {
    this.persistence = persistence;
  }

  public boolean track(
      String sessionId,
      String eventType,
      Long productId,
      String keyword,
      String pageUrl,
      Map<String, Object> meta,
      HttpServletRequest request) {
    if (eventType == null || !EVENT_TYPES.contains(eventType.trim().toUpperCase(Locale.ROOT))) {
      return false;
    }

    UserEvent event = new UserEvent();
    event.setSessionId(sessionId);
    event.setEventType(eventType.trim().toUpperCase(Locale.ROOT));
    event.setProductId(productId);
    event.setKeyword(keyword);
    event.setPageUrl(safePageUrl(pageUrl));
    event.setMeta(safeMeta(meta));
    event.setCreatedAt(LocalDateTime.now());
    event.setIpHash(hashIp(clientIp(request)));
    event.setUserAgent(request.getHeader("User-Agent"));
    try {
      persistence.persist(event, authenticatedEmail());
      return true;
    } catch (TaskRejectedException ex) {
      log.warn("Tracking executor rejected event {}: {}", eventType, ex.getMessage());
      return false;
    }
  }

  private String authenticatedEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }
    String principal = authentication.getName();
    if ("anonymousUser".equals(principal)) {
      return null;
    }
    return principal;
  }

  private String safePageUrl(String pageUrl) {
    if (pageUrl == null || !pageUrl.startsWith("/")) {
      return null;
    }
    int queryStart = pageUrl.indexOf('?');
    int fragmentStart = pageUrl.indexOf('#');
    int end = pageUrl.length();
    if (queryStart >= 0) end = Math.min(end, queryStart);
    if (fragmentStart >= 0) end = Math.min(end, fragmentStart);
    return pageUrl.substring(0, Math.min(end, 512));
  }

  private Map<String, Object> safeMeta(Map<String, Object> meta) {
    if (meta == null || meta.isEmpty()) {
      return null;
    }
    Map<String, Object> safe = new HashMap<>();
    for (String key : Set.of("durationMs", "quantity")) {
      Object value = meta.get(key);
      if (value instanceof Number) {
        safe.put(key, value);
      }
    }
    if (meta.get("topic") instanceof iuh.fit.www_phone_ver1.service.ai.ChatTopic topic) {
      safe.put("topic", topic.label());
    }
    return safe.isEmpty() ? null : Map.copyOf(safe);
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private String hashIp(String ip) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(ip.getBytes(StandardCharsets.UTF_8));
      StringBuilder result = new StringBuilder();
      for (byte value : hash) {
        result.append(String.format("%02x", value));
      }
      return result.toString();
    } catch (NoSuchAlgorithmException ex) {
      log.error("SHA-256 is unavailable; tracking IP cannot be hashed.", ex);
      return "unavailable";
    }
  }
}
