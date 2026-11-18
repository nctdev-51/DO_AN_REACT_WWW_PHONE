package iuh.fit.www_phone_ver1.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.www_phone_ver1.entity.Product;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Generates normalized embedding vectors for products. Uses OpenAI Embeddings when an OpenAI API
 * key (sk-...) is configured and falls back to a deterministic 1536-dim local semantic vector
 * otherwise.
 */
@Service
public class EmbeddingService {

  private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
  public static final String PROVIDER_OPENAI = "openai";
  public static final String PROVIDER_LOCAL = "local-fallback";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${app.ai.embedding.base-url:https://api.openai.com/v1}")
  private String baseUrl;

  @Value("${app.ai.embedding.api-key:}")
  private String apiKey;

  @Value("${app.ai.embedding.model:text-embedding-3-small}")
  private String model;

  @Value("${app.milvus.dimension:1536}")
  private int dimension = 1536;

  public record EmbeddingResult(float[] vector, String contentHash, String provider) {}

  public int getDimension() {
    return dimension > 0 ? dimension : 1536;
  }

  public String toText(Product product) {
    if (product == null) {
      return "";
    }
    return String.join(
        " | ",
        "loai:" + safe(product.getKind()),
        "hang:" + safe(product.getBrand()),
        "ten:" + safe(product.getName()),
        "chip:" + safe(product.getChipset()),
        "manhinh:" + safe(product.getDisplay()),
        "camera:" + safe(product.getCamera()),
        "pin:" + safe(product.getBattery()),
        "hdh:" + safe(product.getOperatingSystem()),
        "nhucau:" + safe(product.getUsageTags()),
        "gia:" + (product.getPrice() == null ? "0" : product.getPrice().toPlainString()));
  }

  public String contentHash(Product product) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(toText(product).getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashed);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }

  public float[] embed(Product product) {
    return embedWithMetadata(product).vector();
  }

  public EmbeddingResult embedWithMetadata(Product product) {
    String hash = contentHash(product);
    if (isOpenAiEmbeddingEnabled()) {
      try {
        float[] remote = callOpenAiEmbeddings(toText(product));
        if (remote.length == getDimension()) {
          return new EmbeddingResult(normalize(remote), hash, PROVIDER_OPENAI);
        }
        log.warn(
            "OpenAI embedding dimension {} != configured {}, falling back to local vector",
            remote.length,
            getDimension());
      } catch (Exception ex) {
        log.warn("OpenAI embedding call failed ({}), using local vector fallback", ex.getMessage());
      }
    }
    return new EmbeddingResult(localSemanticVector(product), hash, PROVIDER_LOCAL);
  }

  boolean isOpenAiEmbeddingEnabled() {
    if (apiKey == null) {
      return false;
    }
    String trimmed = apiKey.trim();
    return !trimmed.isEmpty()
        && trimmed.startsWith("sk-")
        && !trimmed.startsWith("gsk_")
        && !trimmed.equalsIgnoreCase("placeholder")
        && !trimmed.startsWith("demo");
  }

  private float[] callOpenAiEmbeddings(String input) throws Exception {
    RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();
    Map<String, Object> body = Map.of("model", model, "input", input);
    String response =
        restClient
            .post()
            .uri("/embeddings")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(String.class);

    JsonNode array = objectMapper.readTree(response).path("data").path(0).path("embedding");
    if (!array.isArray() || array.isEmpty()) {
      throw new IllegalStateException("Empty embedding vector in OpenAI response");
    }
    float[] vec = new float[array.size()];
    for (int i = 0; i < array.size(); i++) {
      vec[i] = (float) array.get(i).asDouble();
    }
    return vec;
  }

  /**
   * Builds a deterministic unit-length vector from structured specs and price band so vector
   * similarity search remains meaningful even without an OpenAI key.
   */
  public float[] localSemanticVector(Product product) {
    int dim = getDimension();
    float[] vec = new float[dim];
    if (product == null) {
      vec[0] = 1.0f;
      return vec;
    }

    addWeightedText(vec, product.getKind(), "kind", 4.0f);
    addWeightedText(vec, product.getBrand(), "brand", 3.0f);
    addWeightedText(vec, product.getName(), "name", 2.0f);
    addWeightedText(vec, product.getChipset(), "chip", 2.2f);
    addWeightedText(vec, product.getDisplay(), "disp", 1.4f);
    addWeightedText(vec, product.getCamera(), "cam", 1.8f);
    addWeightedText(vec, product.getBattery(), "bat", 1.6f);
    addWeightedText(vec, product.getOperatingSystem(), "os", 1.6f);
    addWeightedText(vec, product.getUsageTags(), "tag", 2.5f);
    addPriceFeatures(vec, product.getPrice());

    return normalize(vec);
  }

  private void addPriceFeatures(float[] vec, BigDecimal price) {
    if (price == null) {
      return;
    }
    long millions = price.longValue() / 1_000_000L;
    int bucket = (int) Math.max(0, Math.min(40, millions / 3));
    addHashedFeature(vec, "price_tier:" + bucket, 2.5f);
    if (bucket > 0) {
      addHashedFeature(vec, "price_tier:" + (bucket - 1), 1.2f);
    }
    addHashedFeature(vec, "price_tier:" + (bucket + 1), 1.2f);
  }

  private void addWeightedText(float[] vec, String raw, String prefix, float weight) {
    if (raw == null || raw.isBlank()) {
      return;
    }
    String normalized = raw.toLowerCase(Locale.ROOT).trim();
    addHashedFeature(vec, prefix + "_full:" + normalized, weight * 0.8f);
    String[] tokens = normalized.split("[\\s,/|_+\\-()]+");
    for (String token : tokens) {
      if (token.length() < 2) {
        continue;
      }
      addHashedFeature(vec, prefix + ":" + token, weight);
      for (int i = 0; i <= token.length() - 3; i++) {
        addHashedFeature(vec, prefix + "_g3:" + token.substring(i, i + 3), weight * 0.35f);
      }
    }
  }

  private void addHashedFeature(float[] vec, String feature, float weight) {
    int h1 = fnv1a(feature, 0x811c9dc5);
    int h2 = fnv1a(feature, 0x9e3779b9);
    int index = Math.floorMod(h1, vec.length);
    float sign = (h2 & 1) == 0 ? 1.0f : -1.0f;
    vec[index] += sign * weight;
  }

  private static int fnv1a(String text, int seed) {
    int hash = seed;
    for (int i = 0; i < text.length(); i++) {
      hash ^= text.charAt(i);
      hash *= 0x01000193;
    }
    return hash;
  }

  public static float[] normalize(float[] vec) {
    double sumSq = 0.0;
    for (float v : vec) {
      sumSq += (double) v * v;
    }
    if (sumSq <= 1e-12) {
      vec[0] = 1.0f;
      return vec;
    }
    float invNorm = (float) (1.0 / Math.sqrt(sumSq));
    for (int i = 0; i < vec.length; i++) {
      vec[i] *= invNorm;
    }
    return vec;
  }

  public static List<Float> toFloatList(float[] vec) {
    Float[] boxed = new Float[vec.length];
    for (int i = 0; i < vec.length; i++) {
      boxed[i] = vec[i];
    }
    return List.of(boxed);
  }

  public static float[] toFloatArray(List<Float> list) {
    float[] vec = new float[list.size()];
    for (int i = 0; i < list.size(); i++) {
      Float val = list.get(i);
      vec[i] = val != null ? val : 0.0f;
    }
    return vec;
  }

  private static String safe(String value) {
    return value == null ? "" : value.trim();
  }
}
