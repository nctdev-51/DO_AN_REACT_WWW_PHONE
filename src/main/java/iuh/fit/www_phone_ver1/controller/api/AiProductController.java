package iuh.fit.www_phone_ver1.controller.api;

import iuh.fit.www_phone_ver1.dto.ai.ChatResponseDTO;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.service.ai.ChatbotService;
import iuh.fit.www_phone_ver1.service.ai.ProductDescriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API cho module AI — Tài
 * GET  /api/ai/description/{productId}          → mô tả AI + điểm nổi bật (cache, active product)
 * POST /api/ai/description/{productId}/refresh  → làm mới mô tả AI (ROLE_ADMIN + CSRF)
 * GET  /api/ai/compare?a={id}&b={id}            → so sánh 2 sản phẩm active
 * POST /api/ai/chat                             → chatbot tư vấn (có rate-limit)
 */
@RestController
@RequestMapping("/api/ai")
public class AiProductController {

    private static final int MAX_CHAT_MESSAGE_LENGTH = 500;
    private static final int RATE_LIMIT_MAX_REQUESTS = 20;
    private static final long RATE_LIMIT_WINDOW_MS = 60_000L;
    private static final int MAX_RATE_BUCKETS = 10_000;

    private final ConcurrentHashMap<String, Deque<Long>> rateBuckets = new ConcurrentHashMap<>();

    @Autowired
    private ProductDescriptionService descriptionService;

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/description/{productId}")
    public ResponseEntity<?> getDescription(@PathVariable Long productId) {
        Optional<Product> opt = findActiveProduct(productId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Product product = opt.get();
        return ResponseEntity.ok(Map.of(
                "description", descriptionService.generateDescription(product),
                "keyFeatures", descriptionService.generateKeyFeatures(product)
        ));
    }

    @PostMapping("/description/{productId}/refresh")
    public ResponseEntity<?> refreshDescription(@PathVariable Long productId) {
        Optional<Product> opt = findActiveProduct(productId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Product product = opt.get();
        descriptionService.clearCache(productId);
        return ResponseEntity.ok(Map.of(
                "description", descriptionService.regenerateDescription(product),
                "keyFeatures", descriptionService.regenerateKeyFeatures(product)
        ));
    }

    @GetMapping("/compare")
    public ResponseEntity<?> compareProducts(
            @RequestParam Long a,
            @RequestParam Long b,
            HttpServletRequest request) {
        if (!allowAiRequest(request)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "error", "Bạn gửi yêu cầu so sánh quá nhanh. Vui lòng thử lại sau ít phút."
            ));
        }
        Optional<Product> optA = findActiveProduct(a);
        Optional<Product> optB = findActiveProduct(b);
        if (optA.isEmpty() || optB.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Không tìm thấy sản phẩm đang kinh doanh. Vui lòng kiểm tra lại ID."
            ));
        }
        String comparison = descriptionService.compareProducts(optA.get(), optB.get());
        return ResponseEntity.ok(Map.of("comparison", comparison));
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        if (!allowAiRequest(request)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "error", "Bạn đang nhắn quá nhanh. Vui lòng thử lại sau ít phút."
            ));
        }

        String rawMessage = body == null ? null : body.get("message");
        if (rawMessage == null || rawMessage.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Tin nhắn không được để trống."
            ));
        }
        String trimmed = rawMessage.trim();
        if (trimmed.length() > MAX_CHAT_MESSAGE_LENGTH) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Tin nhắn tối đa 500 ký tự."
            ));
        }

        String rawSession = body.get("sessionId");
        String sessionId = (rawSession != null && !rawSession.isBlank()) ? rawSession.trim() : "default";
        ChatResponseDTO response = chatbotService.chat(trimmed, sessionId);
        return ResponseEntity.ok(response);
    }

    private Optional<Product> findActiveProduct(Long productId) {
        if (productId == null) {
            return Optional.empty();
        }
        return productRepository
                .findById(productId)
                .filter(p -> Integer.valueOf(1).equals(p.getStatus()));
    }

    private boolean allowAiRequest(HttpServletRequest request) {
        String key = clientKey(request);
        long now = System.currentTimeMillis();
        if (rateBuckets.size() >= MAX_RATE_BUCKETS) {
            rateBuckets.entrySet().removeIf(entry -> {
                Deque<Long> q = entry.getValue();
                synchronized (q) {
                    pruneExpired(q, now);
                    return q.isEmpty();
                }
            });
        }
        Deque<Long> timestamps = rateBuckets.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            pruneExpired(timestamps, now);
            if (timestamps.size() >= RATE_LIMIT_MAX_REQUESTS) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    private static void pruneExpired(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) >= RATE_LIMIT_WINDOW_MS) {
            timestamps.pollFirst();
        }
    }

    private static String clientKey(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        HttpSession session = request.getSession(false);
        String ip = request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
        return session != null ? ip + ":" + session.getId() : ip;
    }
}
