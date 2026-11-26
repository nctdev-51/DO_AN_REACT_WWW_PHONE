package iuh.fit.www_phone_ver1.controller.api;

import iuh.fit.www_phone_ver1.dto.tracking.TrackEventRequest;
import iuh.fit.www_phone_ver1.service.tracking.TrackingService;
import iuh.fit.www_phone_ver1.service.tracking.TrackingSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        TrackingSession.getOrCreateId(httpRequest, response),
        request.eventType(),
        request.productId(),
        request.keyword(),
        request.pageUrl(),
        request.meta(),
        httpRequest);
    return ResponseEntity.noContent().build();
  }
}
