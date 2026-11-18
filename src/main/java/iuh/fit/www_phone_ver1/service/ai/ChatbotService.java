package iuh.fit.www_phone_ver1.service.ai;

import iuh.fit.www_phone_ver1.dto.ai.ChatResponseDTO;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);
    private static final Pattern SUGGEST_PATTERN = Pattern.compile("\\[SUGGEST:(\\d+)\\]");

    @Autowired(required = false)
    private ChatModel chatModel;

    @Autowired
    private ProductRepository productRepository;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    // Session history: sessionId -> list of messages
    private final ConcurrentHashMap<String, List<Message>> sessionHistory = new ConcurrentHashMap<>();

    private boolean isAiEnabled() {
        return chatModel != null
                && apiKey != null
                && !apiKey.isBlank()
                && !apiKey.equalsIgnoreCase("placeholder")
                && !apiKey.startsWith("demo");
    }

    /**
     * Xử lý tin nhắn chatbot từ người dùng (alias theo task spec).
     */
    public ChatResponseDTO answer(String userMessage, String sessionId) {
        return chat(userMessage, sessionId);
    }

    /**
     * Xử lý tin nhắn chatbot từ người dùng.
     */
    public ChatResponseDTO chat(String userMessage, String sessionId) {
        if (!isAiEnabled()) {
            return fallbackChat(userMessage);
        }
        try {
            // Lấy hoặc khởi tạo lịch sử hội thoại
            List<Message> history = sessionHistory.computeIfAbsent(
                    sessionId != null ? sessionId : "default",
                    k -> new ArrayList<>()
            );

            // Build system prompt với danh sách sản phẩm
            String productContext = buildProductContext();
            PromptTemplate systemTemplate = new PromptTemplate(
                    new ClassPathResource("prompts/chatbot-system.st"));
            String systemText = systemTemplate.render(Map.of("productContext", productContext));

            // Build danh sách messages: system + history + user message mới
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemText));
            synchronized (history) {
                messages.addAll(history);
            }
            messages.add(new UserMessage(userMessage));

            // Gọi AI
            Prompt prompt = new Prompt(messages);
            String replyRaw = chatModel.call(prompt).getResult().getOutput().getText();

            // 1. Parse suggested product IDs từ [SUGGEST:id]
            List<Long> suggestedIds = extractSuggestedIds(replyRaw);

            // 2. Cơ chế phòng vệ tự động: Quét tên sản phẩm hoặc ID trong reply nếu AI quên gắn thẻ [SUGGEST:id]
            suggestedIds = autoMatchProductsIfEmpty(suggestedIds, replyRaw);

            // 3. Làm sạch reply (bỏ các tag [SUGGEST:...])
            String reply = replyRaw.replaceAll("\\[SUGGEST:\\d+\\]", "").trim();

            // 4. Lấy thông tin chi tiết các sản phẩm gợi ý để hiển thị thẻ "Xem chi tiết" đẹp mắt
            List<ChatResponseDTO.ProductCardDTO> productCards = buildProductCards(suggestedIds);

            // 5. Cập nhật history (giữ tối đa 20 tin nhắn)
            synchronized (history) {
                history.add(new UserMessage(userMessage));
                history.add(new AssistantMessage(reply));
                if (history.size() > 20) {
                    history.subList(0, history.size() - 20).clear();
                }
            }

            return new ChatResponseDTO(reply, suggestedIds, productCards);
        } catch (Exception e) {
            log.error("Chatbot AI failed for session {}: ", sessionId, e);
            return fallbackChat(userMessage);
        }
    }

    // ── private helpers ────────────────────────────────────────────────────────

    private String buildProductContext() {
        try {
            List<Product> products = productRepository.findByStatus(1, PageRequest.of(0, 25)).getContent();
            if (products.isEmpty()) {
                return "- Hiện chưa có dữ liệu sản phẩm trong kho.";
            }
            return products.stream()
                    .map(p -> String.format("- ID: %d | Tên: %s | Hãng: %s | Giá: %s VNĐ | Chip: %s | Camera: %s | Pin: %s",
                            p.getId() != null ? p.getId() : 0L,
                            p.getName() != null && !p.getName().isBlank() ? p.getName() : "Đang cập nhật",
                            p.getBrand() != null && !p.getBrand().isBlank() ? p.getBrand() : "Khác",
                            p.getPrice() != null ? String.format("%,d", p.getPrice().longValue()) : "Liên hệ",
                            p.getChipset() != null && !p.getChipset().isBlank() ? p.getChipset() : "Tiêu chuẩn",
                            p.getCamera() != null && !p.getCamera().isBlank() ? p.getCamera() : "Tiêu chuẩn",
                            p.getBattery() != null && !p.getBattery().isBlank() ? p.getBattery() : "Tiêu chuẩn"))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.warn("Failed to build product context: {}", e.getMessage());
            return "- Thông tin sản phẩm tạm thời chưa tải được.";
        }
    }

    private List<Long> extractSuggestedIds(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        List<Long> ids = new ArrayList<>();
        Matcher m = SUGGEST_PATTERN.matcher(text);
        while (m.find()) {
            try {
                long id = Long.parseLong(m.group(1));
                if (!ids.contains(id)) {
                    ids.add(id);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    private List<Long> autoMatchProductsIfEmpty(List<Long> currentIds, String text) {
        List<Long> result = new ArrayList<>(currentIds);
        if (text == null || text.isBlank()) {
            return result;
        }
        try {
            List<Product> all = productRepository.findByStatus(1, PageRequest.of(0, 50)).getContent();
            String lower = text.toLowerCase();
            for (Product p : all) {
                if (p.getName() != null && !p.getName().isBlank()) {
                    String pName = p.getName().toLowerCase().trim();
                    if (lower.contains(pName) && !result.contains(p.getId())) {
                        result.add(p.getId());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Auto-matching products skipped: {}", e.getMessage());
        }
        // Giới hạn tối đa 4 sản phẩm
        if (result.size() > 4) {
            return new ArrayList<>(result.subList(0, 4));
        }
        return result;
    }

    private List<ChatResponseDTO.ProductCardDTO> buildProductCards(List<Long> ids) {
        List<ChatResponseDTO.ProductCardDTO> cards = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            return cards;
        }
        for (Long id : ids) {
            try {
                productRepository.findById(id)
                        .filter(p -> Integer.valueOf(1).equals(p.getStatus()))
                        .ifPresent(p -> {
                            String priceStr = p.getPrice() != null ? String.format("%,d VNĐ", p.getPrice().longValue()) : "Liên hệ";
                            cards.add(new ChatResponseDTO.ProductCardDTO(
                                    p.getId(),
                                    p.getName() != null ? p.getName() : ("Sản phẩm #" + p.getId()),
                                    priceStr,
                                    p.getBrand() != null ? p.getBrand() : "Chính hãng"
                            ));
                        });
            } catch (Exception ignored) {
            }
        }
        return cards;
    }

    private ChatResponseDTO fallbackChat(String userMessage) {
        String lower = userMessage != null ? userMessage.toLowerCase() : "";
        String reply;
        List<Long> fallbackIds = new ArrayList<>();
        if (lower.contains("giá") || lower.contains("tiền") || lower.contains("rẻ")) {
            reply = "Tôi có thể tư vấn điện thoại theo nhiều mức giá khác nhau.\n" +
                    "Bạn có thể xem danh sách sản phẩm tại trang Sản phẩm để lọc theo giá.\n" +
                    "Hiện tính năng AI đang tạm thời không khả dụng, nhưng tôi luôn ở đây để hỗ trợ! 😊";
        } else if (lower.contains("so sánh") || lower.contains("tốt hơn")) {
            reply = "Bạn có thể dùng tính năng So sánh sản phẩm trên trang web để xem thông số chi tiết.\n" +
                    "Hiện tính năng tư vấn AI đang bảo trì, xin thông cảm! 🙏";
        } else {
            reply = "Xin chào! Tôi là trợ lý PTV Mobile.\n" +
                    "Hiện tính năng AI đang bảo trì. Bạn có thể xem thông số và lọc sản phẩm trực tiếp trên trang Sản phẩm, " +
                    "hoặc liên hệ cửa hàng để được tư vấn trực tiếp. 📱";
        }
        return new ChatResponseDTO(reply, fallbackIds, List.of());
    }
}
