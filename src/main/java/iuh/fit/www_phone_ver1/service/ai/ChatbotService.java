package iuh.fit.www_phone_ver1.service.ai;

import iuh.fit.www_phone_ver1.dto.ai.ChatResponseDTO;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.memory.ChatMemory;
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
    private ChatMemory chatMemory;

    @Autowired
    private ProductRepository productRepository;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${app.chatbot.knowledge:classpath:prompts/chatbot-knowledge.txt}")
    private org.springframework.core.io.Resource knowledge =
            new ClassPathResource("prompts/chatbot-knowledge.txt");

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
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("A conversation ID is required.");
        }
        List<Message> history = chatMemory.get(sessionId, 20);
        if (!isAiEnabled()) {
            return persistFallback(userMessage, sessionId);
        }

        String productContext = buildProductContext(userMessage);
        PromptTemplate systemTemplate =
                new PromptTemplate(new ClassPathResource("prompts/chatbot-system.st"));
        String systemText = systemTemplate.render(Map.of(
                "productContext", productContext, "knowledgeContext", loadKnowledge()));
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemText));
        messages.addAll(history);
        messages.add(new UserMessage(userMessage));

        String replyRaw;
        try {
            var response = chatModel.call(new Prompt(messages));
            replyRaw =
                    response != null
                                    && response.getResult() != null
                                    && response.getResult().getOutput() != null
                            ? response.getResult().getOutput().getText()
                            : null;
        } catch (RuntimeException ex) {
            log.error("Chatbot AI failed for conversation {}.", sessionId, ex);
            return persistFallback(userMessage, sessionId);
        }
        if (replyRaw == null || replyRaw.isBlank()) {
            return persistFallback(userMessage, sessionId);
        }

        List<Long> suggestedIds = extractSuggestedIds(replyRaw);
        suggestedIds = autoMatchProductsIfEmpty(suggestedIds, replyRaw);
        String reply = replyRaw.replaceAll("\\[SUGGEST:\\d+\\]", "").trim();
        List<ChatResponseDTO.ProductCardDTO> productCards = buildProductCards(suggestedIds);
        chatMemory.add(
                sessionId, List.of(new UserMessage(userMessage), new AssistantMessage(reply)));
        return new ChatResponseDTO(reply, suggestedIds, productCards);
    }

    private ChatResponseDTO persistFallback(String userMessage, String conversationId) {
        ChatResponseDTO response = fallbackChat(userMessage);
        chatMemory.add(
                conversationId,
                List.of(new UserMessage(userMessage), new AssistantMessage(response.getReply())));
        return response;
    }

    // ── private helpers ────────────────────────────────────────────────────────

    private String loadKnowledge() {
        try (var stream = knowledge.getInputStream()) {
            return new String(stream.readNBytes(16_384), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException ex) {
            log.warn("Chatbot knowledge resource unavailable.", ex);
            return "Hướng dẫn khách kiểm tra chính sách hiện hành với cửa hàng.";
        }
    }

    private String buildProductContext(String userMessage) {
        try {
            List<Product> active = productRepository.findByStatus(1, PageRequest.of(0, 60)).getContent();
            if (active.isEmpty()) {
                return "- Hiện chưa có dữ liệu sản phẩm trong kho.";
            }
            List<Product> ordered = prioritizeContextProducts(active, userMessage, 25);
            return ordered.stream()
                    .map(p -> String.format("- ID: %d | Tên: %s | Hãng: %s | Giá: %s VNĐ | Chip: %s | Camera: %s | Pin: %s | Bảo hành: %s",
                            p.getId() != null ? p.getId() : 0L,
                            p.getName() != null && !p.getName().isBlank() ? p.getName() : "Đang cập nhật",
                            p.getBrand() != null && !p.getBrand().isBlank() ? p.getBrand() : "Khác",
                            p.getPrice() != null ? String.format("%,d", p.getPrice().longValue()) : "Liên hệ",
                            p.getChipset() != null && !p.getChipset().isBlank() ? p.getChipset() : "Tiêu chuẩn",
                            p.getCamera() != null && !p.getCamera().isBlank() ? p.getCamera() : "Tiêu chuẩn",
                            p.getBattery() != null && !p.getBattery().isBlank() ? p.getBattery() : "Tiêu chuẩn",
                            p.getWarrantyMonths() != null ? p.getWarrantyMonths() + " tháng" : "Liên hệ"))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.warn("Failed to build product context: {}", e.getMessage());
            return "- Thông tin sản phẩm tạm thời chưa tải được.";
        }
    }

    private List<Product> prioritizeContextProducts(List<Product> products, String userMessage, int limit) {
        if (products.size() <= limit || userMessage == null || userMessage.isBlank()) {
            return products.size() <= limit ? products : products.subList(0, limit);
        }
        String[] tokens = userMessage.toLowerCase(Locale.ROOT).trim().split("[^\\p{L}\\p{Nd}]+");
        List<Product> matched = new ArrayList<>();
        List<Product> rest = new ArrayList<>();
        for (Product p : products) {
            String haystack =
                    ((p.getName() == null ? "" : p.getName())
                            + " "
                            + (p.getBrand() == null ? "" : p.getBrand())
                            + " "
                            + (p.getUsageTags() == null ? "" : p.getUsageTags())
                            + " "
                            + (p.getKind() == null ? "" : p.getKind()))
                            .toLowerCase(Locale.ROOT);
            boolean hit = false;
            for (String token : tokens) {
                if (token.length() >= 2 && haystack.contains(token)) {
                    hit = true;
                    break;
                }
            }
            if (hit) {
                matched.add(p);
            } else {
                rest.add(p);
            }
        }
        List<Product> result = new ArrayList<>(limit);
        for (Product p : matched) {
            if (result.size() >= limit) break;
            result.add(p);
        }
        for (Product p : rest) {
            if (result.size() >= limit) break;
            result.add(p);
        }
        return result;
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
