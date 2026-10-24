package iuh.fit.www_phone_ver1.service.shipping;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class GhnClient {
  private final boolean enabled, production;
  private final String token;
  private final long shopId;
  private final URI base;
  private final HttpClient client;

  @org.springframework.beans.factory.annotation.Autowired
  public GhnClient(
      @Value("${app.ghn.enabled:false}") boolean enabled,
      @Value("${app.ghn.production:false}") boolean production,
      @Value("${app.ghn.token:}") String token,
      @Value("${app.ghn.shop-id:0}") long shopId) {
    this(
        enabled,
        production,
        token,
        shopId,
        URI.create(
            production
                ? "https://online-gateway.ghn.vn/shiip/public-api/"
                : "https://dev-online-gateway.ghn.vn/shiip/public-api/"),
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build());
  }

  GhnClient(
      boolean enabled, boolean production, String token, long shopId, URI base, HttpClient client) {
    this.enabled = enabled;
    this.production = production;
    this.token = token;
    this.shopId = shopId;
    this.base = base;
    this.client = client;
  }

  public boolean isAvailable() {
    return enabled && !token.isBlank() && shopId > 0;
  }

  public boolean ownsShop(long id) {
    return id == shopId;
  }

  public String environment() {
    return production ? "PRODUCTION" : "STAGING";
  }

  public record Destination(String province, String ward) {}

  private record Cached(java.time.Instant expires, JsonNode data) {}

  private final java.util.concurrent.ConcurrentHashMap<String, Cached> locations =
      new java.util.concurrent.ConcurrentHashMap<>();

  private JsonNode locations(String path) {
    var cached = locations.get(path);
    if (cached != null && cached.expires().isAfter(java.time.Instant.now())) return cached.data();
    var data = get(path);
    if (!data.isArray())
      throw new IllegalArgumentException("GHN chưa có danh sách địa chỉ hợp lệ.");
    locations.put(path, new Cached(java.time.Instant.now().plusSeconds(3600), data));
    return data;
  }

  private String normalizedName(String text) {
    return java.text.Normalizer.normalize(
            text.toLowerCase(java.util.Locale.ROOT), java.text.Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .replace('đ', 'd')
        .replaceFirst("^(thanh pho|tinh|phuong|xa|dac khu)\\s+", "")
        .trim()
        .replaceAll("\\s+", " ");
  }

  private JsonNode matching(JsonNode all, String name) {
    for (var item : all) {
      if (item.path("status").asInt() != 1) continue;
      if (normalizedName(item.path("name").asString()).equals(normalizedName(name))) return item;
      for (var alias : item.path("extension_names"))
        if (normalizedName(alias.asString()).equals(normalizedName(name))) return item;
    }
    throw new IllegalArgumentException(
        "GHN chưa xác nhận được tỉnh/phường đã chọn. Hãy kiểm tra địa chỉ với cửa hàng.");
  }

  public Destination destination(String province, String ward) {
    var p = matching(locations("v3/master-data/province/all?offset=0&limit=200"), province);
    long id = p.path("_id").asLong();
    if (id <= 0) throw new IllegalArgumentException("GHN chưa trả mã tỉnh hợp lệ.");
    var w =
        matching(
            locations(
                "v3/master-data/ward/all-by-province-id?province_id=" + id + "&offset=0&limit=200"),
            ward);
    return new Destination(p.path("name").asString(), w.path("name").asString());
  }

  public JsonNode preview(Map<String, Object> payload) {
    return call("v2/shipping-order/preview", payload);
  }

  public JsonNode create(Map<String, Object> payload) {
    return call("v2/shipping-order/create", payload);
  }

  public JsonNode detail(String code) {
    if (code == null || !code.matches("[A-Za-z0-9_-]{3,80}"))
      throw new IllegalArgumentException("Mã vận đơn chưa hợp lệ.");
    return get("v2/shipping-order/detail?order_code=" + code);
  }

  public JsonNode get(String path) {
    return send(path, null);
  }

  public JsonNode call(String path, Map<String, Object> payload) {
    return send(path, payload);
  }

  private JsonNode send(String path, Map<String, Object> payload) {
    if (!isAvailable())
      throw new IllegalArgumentException(
          "GHN chưa được cấu hình. Cửa hàng vẫn nhận giao hàng tiêu chuẩn.");
    try {
      var builder =
          HttpRequest.newBuilder(base.resolve(path))
              .timeout(Duration.ofSeconds(12))
              .header("Token", token)
              .header("ShopId", Long.toString(shopId))
              .header("Accept", "application/json");
      if (payload == null) builder.GET();
      else
        builder
            .header("Content-Type", "application/json")
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    new JsonMapper().writeValueAsString(payload),
                    java.nio.charset.StandardCharsets.UTF_8));
      var response =
          client.send(
              builder.build(),
              HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
      if (response.statusCode() != 200)
        throw new IllegalArgumentException(
            "GHN chưa xác nhận được yêu cầu. Kiểm tra cấu hình, địa chỉ và thử lại; không suy đoán"
                + " vận đơn đã được tạo hay chưa.");
      var root = new JsonMapper().readTree(response.body());
      if (root.path("code").asInt() != 200
          || root.path("data").isNull()
          || root.path("data").isMissingNode())
        throw new IllegalArgumentException(
            "GHN chưa trả về dữ liệu hợp lệ. Hãy kiểm tra lại trước khi xử lý tiếp.");
      return root.path("data");
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalArgumentException(
          "Yêu cầu GHN bị gián đoạn. Hãy kiểm tra vận đơn trước khi gửi lại.");
    } catch (tools.jackson.core.JacksonException ex) {
      throw new IllegalArgumentException("GHN trả dữ liệu không đọc được. Hãy thử lại sau.");
    } catch (java.io.IOException ex) {
      throw new IllegalArgumentException(
          "Chưa kết nối được GHN. Hãy kiểm tra vận đơn trước khi gửi lại.");
    }
  }
}
