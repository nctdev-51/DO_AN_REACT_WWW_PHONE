package iuh.fit.www_phone_ver1.interceptor;

import iuh.fit.www_phone_ver1.controller.api.TrackingController;
import iuh.fit.www_phone_ver1.service.tracking.TrackingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TrackingInterceptor implements HandlerInterceptor {
  private final TrackingService tracking;

  public TrackingInterceptor(TrackingService tracking) {
    this.tracking = tracking;
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    String uri = request.getRequestURI();
    if ("GET".equalsIgnoreCase(request.getMethod())
        && (uri.contains("/products") || uri.contains("/store"))) {
      tracking.track(
          TrackingController.sessionId(request, response),
          "PAGE_VIEW",
          null,
          null,
          uri,
          null,
          request);
    }
    return true;
  }
}
