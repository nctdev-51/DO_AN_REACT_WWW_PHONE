package iuh.fit.www_phone_ver1.service.tracking;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

public final class TrackingSession {
  private static final String COOKIE_NAME = "ptv_sid";
  private static final int COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 365;

  private TrackingSession() {}

  public static String getOrCreateId(
      HttpServletRequest request, HttpServletResponse response) {
    String existing = existingId(request);
    if (existing != null) return existing;
    String sessionId = UUID.randomUUID().toString();
    Cookie cookie = new Cookie(COOKIE_NAME, sessionId);
    cookie.setHttpOnly(true);
    cookie.setSecure(request.isSecure());
    cookie.setPath("/");
    cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
    response.addCookie(cookie);
    return sessionId;
  }

  public static String existingId(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (COOKIE_NAME.equals(cookie.getName())) {
          try {
            if (cookie.getValue() != null) {
              return UUID.fromString(cookie.getValue()).toString();
            }
          } catch (IllegalArgumentException ex) {
            break;
          }
        }
      }
    }

    return null;
  }
}
