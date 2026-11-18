package iuh.fit.www_phone_ver1.controller.api;

import iuh.fit.www_phone_ver1.dto.catalog.RecommendationResponseDTO;
import iuh.fit.www_phone_ver1.service.vector.RecommendationService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** REST API for vector-based product similarity recommendations (Module TV3 - Vi). */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

  private final RecommendationService recommendationService;

  public RecommendationController(RecommendationService recommendationService) {
    this.recommendationService = recommendationService;
  }

  @GetMapping("/{productId}")
  public ResponseEntity<RecommendationResponseDTO> recommendations(
      @PathVariable Long productId, @RequestParam(defaultValue = "6") int limit) {
    return recommendationService
        .recommend(productId, limit)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/reindex")
  public ResponseEntity<RecommendationService.ReindexSummary> reindex() {
    return ResponseEntity.ok(recommendationService.reindexAll());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, String>> handleInvalidId() {
    return ResponseEntity.badRequest().body(Map.of("error", "Mã sản phẩm không hợp lệ."));
  }
}
