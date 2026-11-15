package iuh.fit.www_phone_ver1.controller.api;

import iuh.fit.www_phone_ver1.dto.ai.ChatResponseDTO;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.service.ai.ChatbotService;
import iuh.fit.www_phone_ver1.service.ai.ProductDescriptionService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API cho module AI — Tài
 * GET  /api/ai/description/{productId}  → mô tả AI + điểm nổi bật
 * GET  /api/ai/compare?a={id}&b={id}    → so sánh 2 sản phẩm
 * POST /api/ai/chat                      → chatbot tư vấn
 */
@RestController
@RequestMapping("/api/ai")
public class AiProductController {

    @Autowired
    private ProductDescriptionService descriptionService;

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Lấy mô tả AI và điểm nổi bật cho sản phẩm.
     *
     * @param productId ID sản phẩm
     * @param refresh   nếu true, xóa cache cũ và sinh lại từ đầu
     */
    @GetMapping("/description/{productId}")
    public ResponseEntity<?> getDescription(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "false") boolean refresh) {

        Optional<Product> opt = productRepository.findById(productId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Product product = opt.get();

        if (refresh) {
            descriptionService.clearCache(productId);
        }

        String description = refresh
                ? descriptionService.regenerateDescription(product)
                : descriptionService.generateDescription(product);

        List<String> keyFeatures = refresh
                ? descriptionService.regenerateKeyFeatures(product)
                : descriptionService.generateKeyFeatures(product);

        return ResponseEntity.ok(Map.of(
                "description", description,
                "keyFeatures", keyFeatures
        ));
    }

    /**
     * So sánh hai sản phẩm bằng AI.
     *
     * @param a ID sản phẩm A
     * @param b ID sản phẩm B
     */
    @GetMapping("/compare")
    public ResponseEntity<?> compareProducts(
            @RequestParam Long a,
            @RequestParam Long b) {

        Optional<Product> optA = productRepository.findById(a);
        Optional<Product> optB = productRepository.findById(b);

        if (optA.isEmpty() || optB.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Không tìm thấy sản phẩm. Vui lòng kiểm tra lại ID."
            ));
        }

        String comparison = descriptionService.compareProducts(optA.get(), optB.get());
        return ResponseEntity.ok(Map.of("comparison", comparison));
    }

    /**
     * Chat tư vấn AI.
     * Body: { "message": "...", "sessionId": "..." }
     */
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        String sessionId = body.get("sessionId");

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Tin nhắn không được để trống."
            ));
        }

        ChatResponseDTO response = chatbotService.chat(
                message.trim(),
                sessionId != null && !sessionId.isBlank() ? sessionId.trim() : "default"
        );
        return ResponseEntity.ok(response);
    }
}
