package iuh.fit.www_phone_ver1.interceptor;

import iuh.fit.www_phone_ver1.service.tracking.TrackingService;
import iuh.fit.www_phone_ver1.service.tracking.TrackingSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TrackingInterceptor implements HandlerInterceptor {
  private static final Pattern PRODUCT_PATH = Pattern.compile("^/products/(\\d+)(?:/.*)?$");

  private final TrackingService tracking;

  public TrackingInterceptor(TrackingService tracking) {
    this.tracking = tracking;
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    String uri = request.getRequestURI();
    String contextPath = request.getContextPath();
    String path = uri.substring(contextPath.length());
    if ("GET".equalsIgnoreCase(request.getMethod())
        && (isUnder(path, "/products") || isUnder(path, "/store"))) {
      tracking.track(
          TrackingSession.getOrCreateId(request, response),
          "PAGE_VIEW",
          productId(path),
          null,
          path,
          null,
          request);
    }
    return true;
  }

  private Long productId(String path) {
    Matcher matcher = PRODUCT_PATH.matcher(path);
    if (!matcher.matches()) {
      return null;
    }
    try {
      return Long.valueOf(matcher.group(1));
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private boolean isUnder(String path, String basePath) {
    return path.equals(basePath) || path.startsWith(basePath + "/");
  }
}
