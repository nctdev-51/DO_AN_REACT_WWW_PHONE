package iuh.fit.www_phone_ver1.service.ai;

import iuh.fit.www_phone_ver1.entity.Product;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class ProductDescriptionService {

    private static final Logger log = LoggerFactory.getLogger(ProductDescriptionService.class);

    @Autowired(required = false)
    private ChatModel chatModel;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    // In-memory cache: productId -> description
    private final ConcurrentHashMap<Long, String> descriptionCache = new ConcurrentHashMap<>();
    // In-memory cache: productId -> key features list
    private final ConcurrentHashMap<Long, List<String>> featuresCache = new ConcurrentHashMap<>();

    private boolean isAiEnabled() {
        return chatModel != null
                && apiKey != null
                && !apiKey.isBlank()
                && !apiKey.equalsIgnoreCase("placeholder")
                && !apiKey.startsWith("demo");
    }

    /**
     * Sinh mô tả AI cho sản phẩm (có cache).
     * Nếu AI không khả dụng, trả về mô tả fallback từ dữ liệu sản phẩm.
     */
    public String generateDescription(Product p) {
        return descriptionCache.computeIfAbsent(p.getId(), id -> doGenerateDescription(p));
    }

    /**
     * Sinh lại mô tả (bỏ qua cache cũ).
     */
    public String regenerateDescription(Product p) {
        String desc = doGenerateDescription(p);
        descriptionCache.put(p.getId(), desc);
        return desc;
    }

    /**
     * Sinh danh sách điểm nổi bật (có cache).
     */
    public List<String> generateKeyFeatures(Product p) {
        return featuresCache.computeIfAbsent(p.getId(), id -> doGenerateKeyFeatures(p));
    }

    /**
     * Sinh lại danh sách điểm nổi bật (bỏ qua cache cũ).
     */
    public List<String> regenerateKeyFeatures(Product p) {
        List<String> features = doGenerateKeyFeatures(p);
        featuresCache.put(p.getId(), features);
        return features;
    }

    /**
     * Xóa cache của sản phẩm (hoặc toàn bộ nếu productId == null).
     */
    public void clearCache(Long productId) {
        if (productId != null) {
            descriptionCache.remove(productId);
            featuresCache.remove(productId);
        } else {
            descriptionCache.clear();
            featuresCache.clear();
        }
    }

    /**
     * So sánh hai sản phẩm bằng AI.
     */
    public String compareProducts(Product a, Product b) {
        if (!isAiEnabled()) {
            return fallbackCompare(a, b);
        }
        try {
            PromptTemplate template = new PromptTemplate(
                    new ClassPathResource("prompts/product-compare.st"));
            Prompt prompt = template.create(Map.ofEntries(
                    Map.entry("nameA", safe(a.getName())),
                    Map.entry("chipsetA", safe(a.getChipset())),
                    Map.entry("displayA", safe(a.getDisplay())),
                    Map.entry("cameraA", safe(a.getCamera())),
                    Map.entry("batteryA", safe(a.getBattery())),
                    Map.entry("priceA", a.getPrice() != null ? a.getPrice().toPlainString() : "N/A"),
                    Map.entry("nameB", safe(b.getName())),
                    Map.entry("chipsetB", safe(b.getChipset())),
                    Map.entry("displayB", safe(b.getDisplay())),
                    Map.entry("cameraB", safe(b.getCamera())),
                    Map.entry("batteryB", safe(b.getBattery())),
                    Map.entry("priceB", b.getPrice() != null ? b.getPrice().toPlainString() : "N/A")));
            return chatModel.call(prompt).getResult().getOutput().getText();
        } catch (Exception e) {
            log.warn("AI compare failed for products {}/{}: {}", a.getId(), b.getId(), e.getMessage());
            return fallbackCompare(a, b);
        }
    }

    // ── private helpers ────────────────────────────────────────────────────────

    private String doGenerateDescription(Product p) {
        if (!isAiEnabled()) {
            return fallbackDescription(p);
        }
        try {
            PromptTemplate template = new PromptTemplate(
                    new ClassPathResource("prompts/product-description.st"));
            Prompt prompt = template.create(Map.of(
                    "name", safe(p.getName()),
                    "brand", safe(p.getBrand()),
                    "chipset", safe(p.getChipset()),
                    "display", safe(p.getDisplay()),
                    "camera", safe(p.getCamera()),
                    "battery", safe(p.getBattery()),
                    "os", safe(p.getOperatingSystem()),
                    "price", p.getPrice() != null ? p.getPrice().toPlainString() : "Liên hệ"));
            return chatModel.call(prompt).getResult().getOutput().getText();
        } catch (Exception e) {
            log.warn("AI description failed for product {}: {}", p.getId(), e.getMessage());
            return fallbackDescription(p);
        }
    }

    private List<String> doGenerateKeyFeatures(Product p) {
        if (!isAiEnabled()) {
            return fallbackFeatures(p);
        }
        try {
            PromptTemplate template = new PromptTemplate(
                    new ClassPathResource("prompts/product-features.st"));
            Prompt prompt = template.create(Map.of(
                    "name", safe(p.getName()),
                    "chipset", safe(p.getChipset()),
                    "camera", safe(p.getCamera()),
                    "battery", safe(p.getBattery()),
                    "display", safe(p.getDisplay())));
            String raw = chatModel.call(prompt).getResult().getOutput().getText();
            if (raw == null || raw.isBlank()) {
                return fallbackFeatures(p);
            }
            List<String> list = Arrays.stream(raw.split("\\r?\\n"))
                    .map(line -> line.replaceAll("^[-*•\\d.)\\s]+", "").trim())
                    .filter(line -> !line.isBlank())
                    .limit(5)
                    .toList();
            return list.isEmpty() ? fallbackFeatures(p) : list;
        } catch (Exception e) {
            log.warn("AI features failed for product {}: {}", p.getId(), e.getMessage());
            return fallbackFeatures(p);
        }
    }

    private String fallbackDescription(Product p) {
        StringBuilder sb = new StringBuilder();
        sb.append(safe(p.getName())).append(" là lựa chọn đáng cân nhắc từ ").append(safe(p.getBrand())).append(".");
        if (!safe(p.getChipset()).equals("Đang cập nhật")) {
            sb.append(" Được trang bị chip ").append(p.getChipset())
                    .append(", thiết bị mang lại hiệu năng ổn định cho công việc hằng ngày.");
        }
        if (!safe(p.getCamera()).equals("Đang cập nhật")) {
            sb.append(" Hệ thống camera ").append(p.getCamera()).append(" giúp ghi lại khoảnh khắc đẹp.");
        }
        return sb.toString();
    }

    private List<String> fallbackFeatures(Product p) {
        List<String> list = new ArrayList<>(List.of(
                "📱 Màn hình: " + safe(p.getDisplay()),
                "⚡ Chip xử lý: " + safe(p.getChipset()),
                "📸 Camera: " + safe(p.getCamera()),
                "🔋 Pin: " + safe(p.getBattery()),
                "🖥️ Hệ điều hành: " + safe(p.getOperatingSystem())).stream().filter(s -> !s.endsWith("Đang cập nhật"))
                .toList());

        // Nếu database chưa có thông số, trả về các điểm nổi bật chuẩn mực thay vì trả
        // về mảng rỗng []
        if (list.isEmpty()) {
            list.add("📱 Thiết kế hiện đại, màn hình hiển thị sắc nét");
            list.add("⚡ Hiệu năng mượt mà, tối ưu cho tác vụ hằng ngày");
            list.add("📸 Camera chụp ảnh chân thực, ghi trọn từng khoảnh khắc");
            list.add("🔋 Thời lượng pin ấn tượng, đáp ứng trọn vẹn ngày dài");
        }
        return list;
    }

    private String fallbackCompare(Product a, Product b) {
        return String.format(
                "%s và %s đều là những lựa chọn tốt từ %s và %s. " +
                        "Hãy cân nhắc dựa trên chip xử lý, camera và mức giá để chọn máy phù hợp nhất với nhu cầu của bạn.",
                safe(a.getName()), safe(b.getName()),
                safe(a.getBrand()), safe(b.getBrand()));
    }

    private String safe(String val) {
        return (val == null || val.isBlank()) ? "Đang cập nhật" : val;
    }
}
