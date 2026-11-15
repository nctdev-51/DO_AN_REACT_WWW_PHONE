package iuh.fit.www_phone_ver1.service.ai;

import iuh.fit.www_phone_ver1.dto.ai.ChatResponseDTO;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AiModuleUnitTests {

    @InjectMocks
    private ProductDescriptionService descriptionService;

    @InjectMocks
    private ChatbotService chatbotService;

    @Mock
    private ProductRepository productRepository;

    private Product samplePhone;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        samplePhone = new Product();
        samplePhone.setId(1L);
        samplePhone.setName("iPhone 16 Pro Max");
        samplePhone.setBrand("Apple");
        samplePhone.setChipset("Apple A18 Pro");
        samplePhone.setDisplay("6.9 inch OLED Super Retina XDR");
        samplePhone.setCamera("48MP + 12MP + 12MP");
        samplePhone.setBattery("4685 mAh");
        samplePhone.setOperatingSystem("iOS 18");
        samplePhone.setPrice(new BigDecimal("34990000"));
    }

    @Test
    @DisplayName("ProductDescriptionService: Fallback description hoạt động mượt mà khi chưa có OpenAI API key")
    void testFallbackDescription() {
        String desc = descriptionService.generateDescription(samplePhone);

        Assertions.assertNotNull(desc);
        Assertions.assertTrue(desc.contains("iPhone 16 Pro Max"));
        Assertions.assertTrue(desc.contains("Apple"));
    }

    @Test
    @DisplayName("ProductDescriptionService: Sinh danh sách điểm nổi bật (Key Features)")
    void testKeyFeatures() {
        List<String> features = descriptionService.generateKeyFeatures(samplePhone);

        Assertions.assertNotNull(features);
        Assertions.assertFalse(features.isEmpty());
        Assertions.assertTrue(features.stream().anyMatch(f -> f.contains("A18 Pro")));
    }

    @Test
    @DisplayName("ProductDescriptionService: Điểm nổi bật không bao giờ rỗng ngay cả khi sản phẩm không có thông số")
    void testKeyFeaturesNeverEmptyWhenSpecsNull() {
        Product emptySpecsPhone = new Product();
        emptySpecsPhone.setId(99L);
        emptySpecsPhone.setName("Điện thoại thử nghiệm");

        List<String> features = descriptionService.generateKeyFeatures(emptySpecsPhone);

        Assertions.assertNotNull(features);
        Assertions.assertFalse(features.isEmpty());
        Assertions.assertTrue(features.size() >= 3);
    }

    @Test
    @DisplayName("ProductDescriptionService: Xóa cache hoạt động chính xác")
    void testClearCache() {
        descriptionService.generateDescription(samplePhone);
        descriptionService.generateKeyFeatures(samplePhone);
        Assertions.assertDoesNotThrow(() -> descriptionService.clearCache(samplePhone.getId()));
        Assertions.assertDoesNotThrow(() -> descriptionService.clearCache(null));
    }

    @Test
    @DisplayName("ProductDescriptionService: So sánh 2 sản phẩm khi không có key trả về thông tin khách quan")
    void testCompareProducts() {
        Product phoneB = new Product();
        phoneB.setId(2L);
        phoneB.setName("Samsung Galaxy S24 Ultra");
        phoneB.setBrand("Samsung");
        phoneB.setChipset("Snapdragon 8 Gen 3");
        phoneB.setPrice(new BigDecimal("29990000"));

        String comparison = descriptionService.compareProducts(samplePhone, phoneB);

        Assertions.assertNotNull(comparison);
        Assertions.assertTrue(comparison.contains("iPhone 16 Pro Max"));
        Assertions.assertTrue(comparison.contains("Samsung Galaxy S24 Ultra"));
    }

    @Test
    @DisplayName("ChatbotService: Fallback tư vấn khi hỏi về giá")
    void testChatbotPriceInquiry() {
        ChatResponseDTO response = chatbotService.chat("Tôi muốn mua điện thoại giá rẻ dưới 10 triệu", "session-test-1");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getReply());
        Assertions.assertTrue(response.getReply().toLowerCase().contains("giá"));
    }

    @Test
    @DisplayName("ChatbotService: Phương thức answer() tương thích với task spec")
    void testChatbotAnswerAlias() {
        ChatResponseDTO response = chatbotService.answer("Xin chào cửa hàng", "session-test-2");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getReply());
        Assertions.assertFalse(response.getReply().isBlank());
    }
}
