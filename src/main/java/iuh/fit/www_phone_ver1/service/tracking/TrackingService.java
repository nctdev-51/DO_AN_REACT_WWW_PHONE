package iuh.fit.www_phone_ver1.service.tracking;

import iuh.fit.www_phone_ver1.entity.mongo.UserEvent;
import iuh.fit.www_phone_ver1.repository.UserRepository;
import iuh.fit.www_phone_ver1.repository.mongo.UserEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

  private final UserEventRepository events;
  private final UserRepository users;

  public TrackingService(UserEventRepository events, UserRepository users) {
    this.events = events;
    this.users = users;
  }

  public boolean track(
      String sessionId,
      String eventType,
      Long productId,
      String keyword,
      String pageUrl,
      Map<String, Object> meta,
      HttpServletRequest request) {
    if (eventType == null || !EVENT_TYPES.contains(eventType.trim().toUpperCase())) {
      return false;
    }

    try {
      UserEvent event = new UserEvent();
      event.setSessionId(sessionId);
      event.setUserId(currentUserId());
      event.setEventType(eventType.trim().toUpperCase());
      event.setProductId(productId);
      event.setKeyword(keyword);
      event.setPageUrl(pageUrl);
      event.setMeta(meta);
      event.setCreatedAt(LocalDateTime.now());
      event.setIpHash(hashIp(clientIp(request)));
      event.setUserAgent(request.getHeader("User-Agent"));
      events.save(event);
      return true;
    } catch (Exception ex) {
      log.warn("Could not persist tracking event {}: {}", eventType, ex.getMessage());
      return false;
    }
  }

  private Long currentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }
    String principal = authentication.getName();
    if ("anonymousUser".equals(principal)) {
      return null;
    }
    return users.findByEmail(principal).map(user -> user.getId()).orElse(null);
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
    } catch (Exception ex) {
      return "unavailable";
    }
  }
}
