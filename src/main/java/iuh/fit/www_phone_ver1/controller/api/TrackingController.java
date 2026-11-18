package iuh.fit.www_phone_ver1.controller.api;

import iuh.fit.www_phone_ver1.dto.tracking.TrackEventRequest;
import iuh.fit.www_phone_ver1.service.tracking.TrackingService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {
  private final TrackingService tracking;

  public TrackingController(TrackingService tracking) {
    this.tracking = tracking;
  }

  @PostMapping
  public ResponseEntity<Void> track(
      @RequestBody TrackEventRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse response) {
    tracking.track(
        sessionId(httpRequest, response),
        request.eventType(),
        request.productId(),
        request.keyword(),
        request.pageUrl(),
        request.meta(),
        httpRequest);
    return ResponseEntity.noContent().build();
  }

  public static String sessionId(HttpServletRequest request, HttpServletResponse response) {
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("ptv_sid".equals(cookie.getName()) && cookie.getValue() != null) {
          return cookie.getValue();
        }
      }
    }
    String sessionId = UUID.randomUUID().toString();
    Cookie cookie = new Cookie("ptv_sid", sessionId);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(60 * 60 * 24 * 365);
    response.addCookie(cookie);
    return sessionId;
  }
}
