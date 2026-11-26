package iuh.fit.www_phone_ver1.controller.api;

import java.security.Principal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides a fresh CSRF token for the offline replay engine before re-submitting queued requests.
 */
@RestController
@RequestMapping("/api/csrf")
public class CsrfController {

  @GetMapping
  public ResponseEntity<Map<String, String>> csrf(CsrfToken token, Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "UNAUTHORIZED"));
    }
    return ResponseEntity.ok(
        Map.of(
            "parameterName", token.getParameterName(),
            "headerName", token.getHeaderName(),
            "token", token.getToken()));
  }
}
