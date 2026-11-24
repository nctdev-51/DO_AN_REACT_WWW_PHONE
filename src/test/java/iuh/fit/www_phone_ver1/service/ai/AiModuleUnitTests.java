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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

class AiModuleUnitTests {

    @InjectMocks
    private ProductDescriptionService descriptionService;

    @InjectMocks
    private ChatbotService chatbotService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ChatMemory chatMemory;

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
        Mockito.verify(chatMemory)
                .add(
                        Mockito.eq("session-test-1"),
                        Mockito.<List<Message>>argThat(messages -> messages.size() == 2));
    }

    @Test
    @DisplayName("ChatbotService: Phương thức answer() tương thích với task spec")
    void testChatbotAnswerAlias() {
        ChatResponseDTO response = chatbotService.answer("Xin chào cửa hàng", "session-test-2");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getReply());
        Assertions.assertFalse(response.getReply().isBlank());
    }

    @Test
    @DisplayName("ChatbotService: Không trả thẻ sản phẩm đã ngừng kinh doanh (status != 1)")
    void testChatbotExcludesInactiveSuggestedProducts() {
        ChatModel mockChatModel = Mockito.mock(ChatModel.class);
        Product activeProduct = new Product();
        activeProduct.setId(10L);
        activeProduct.setName("iPhone 16");
        activeProduct.setStatus(1);
        activeProduct.setPrice(new BigDecimal("22990000"));

        Product inactiveProduct = new Product();
        inactiveProduct.setId(99L);
        inactiveProduct.setName("Legacy Phone");
        inactiveProduct.setStatus(0);
        inactiveProduct.setPrice(new BigDecimal("5000000"));

        Mockito.when(productRepository.findByStatus(Mockito.eq(1), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activeProduct)));
        Mockito.when(chatMemory.get("session-active-only", 20)).thenReturn(List.of());
        Mockito.when(productRepository.findById(10L)).thenReturn(java.util.Optional.of(activeProduct));
        Mockito.when(productRepository.findById(99L)).thenReturn(java.util.Optional.of(inactiveProduct));
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(
                        new AssistantMessage("Gợi ý cho bạn [SUGGEST:10] [SUGGEST:99]")))));

        ReflectionTestUtils.setField(chatbotService, "chatModel", mockChatModel);
        ReflectionTestUtils.setField(chatbotService, "apiKey", "gsk_test_key_valid_format");

        ChatResponseDTO response = chatbotService.chat("Tư vấn máy mới", "session-active-only");

        Assertions.assertEquals(1, response.getProductCards().size());
        Assertions.assertEquals(10L, response.getProductCards().get(0).getId());
        Mockito.verify(chatMemory).get("session-active-only", 20);
        Mockito.verify(chatMemory)
                .add(
                        Mockito.eq("session-active-only"),
                        Mockito.<List<Message>>argThat(messages -> messages.size() == 2));
    }
}
