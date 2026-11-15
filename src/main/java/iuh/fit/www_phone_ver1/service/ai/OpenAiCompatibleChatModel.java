package iuh.fit.www_phone_ver1.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;

/**
 * ChatModel tuong thich 100% voi Spring Boot 4.1.1 (Spring 7).
 * Ho tro tu dong ca OpenAI API Key (sk-...) lan Groq API Key (gsk_...).
 */
@Component
@Primary
public class OpenAiCompatibleChatModel implements ChatModel {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleChatModel.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:}")
    private String customBaseUrl;

    @Value("${spring.ai.openai.chat.options.model:}")
    private String customModel;

    @Override
    public ChatResponse call(Prompt prompt) {
        if (apiKey == null || apiKey.isBlank()
                || apiKey.equalsIgnoreCase("placeholder")
                || apiKey.startsWith("demo")) {
            throw new IllegalStateException("OpenAI / Groq API key chua duoc cau hinh.");
        }

        boolean isGroq = apiKey.startsWith("gsk_");
        String baseUrl = resolveBaseUrl(isGroq);
        String model   = resolveModel(isGroq);

        try {
            // Build messages list for OpenAI-compatible API
            List<Map<String, String>> messages = new ArrayList<>();
            for (Message msg : prompt.getInstructions()) {
                String role = switch (msg.getMessageType()) {
                    case SYSTEM    -> "system";
                    case ASSISTANT -> "assistant";
                    default        -> "user";
                };
                String content = msg.getText();
                Map<String, String> entry = new HashMap<>();
                entry.put("role", role);
                entry.put("content", content != null ? content : "");
                messages.add(entry);
            }

            // Build request body
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("model", model);
            requestPayload.put("messages", messages);
            requestPayload.put("temperature", 0.7);
            requestPayload.put("max_tokens", 500);

            RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();

            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            String replyText = root.path("choices").path(0)
                    .path("message").path("content").asText("");

            List<Generation> generations = new ArrayList<>();
            generations.add(new Generation(new AssistantMessage(replyText)));
            return new ChatResponse(generations);

        } catch (Exception e) {
            log.error("Loi goi AI API ({}): {}", baseUrl, e.getMessage());
            throw new RuntimeException("Loi AI API: " + e.getMessage(), e);
        }
    }

    private String resolveBaseUrl(boolean isGroq) {
        if (customBaseUrl != null && !customBaseUrl.isBlank()) {
            String clean = customBaseUrl.endsWith("/")
                    ? customBaseUrl.substring(0, customBaseUrl.length() - 1)
                    : customBaseUrl;
            return clean.endsWith("/v1") ? clean : clean + "/v1";
        }
        return isGroq ? "https://api.groq.com/openai/v1" : "https://api.openai.com/v1";
    }

    private String resolveModel(boolean isGroq) {
        if (customModel != null && !customModel.isBlank() && !customModel.equals("gpt-4o-mini")) {
            return customModel;
        }
        return isGroq ? "openai/gpt-oss-20b" : "gpt-4o-mini";
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return null;
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        throw new UnsupportedOperationException("Streaming not implemented");
    }
}
