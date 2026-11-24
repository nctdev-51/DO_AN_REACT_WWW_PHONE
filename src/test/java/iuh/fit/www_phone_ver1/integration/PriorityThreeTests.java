package iuh.fit.www_phone_ver1.integration;

import static org.junit.jupiter.api.Assertions.*;

import iuh.fit.www_phone_ver1.controller.admin.AdminAnalyticsController;
import iuh.fit.www_phone_ver1.service.ai.OpenAiCompatibleChatModel;
import iuh.fit.www_phone_ver1.service.payment.VnPayService;
import iuh.fit.www_phone_ver1.service.shipping.GhnClient;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PriorityThreeTests {

  @Test
  void csvExportEscapesFormulaInjectionPrefixes() {
    assertEquals("\"'=cmd|' /C calc'!A0\"", AdminAnalyticsController.safeCsvCell("=cmd|' /C calc'!A0"));
    assertEquals("\"'+SUM(A1:A2)\"", AdminAnalyticsController.safeCsvCell("+SUM(A1:A2)"));
    assertEquals("\"'-1+1\"", AdminAnalyticsController.safeCsvCell("-1+1"));
    assertEquals("\"'@SUM(1,1)\"", AdminAnalyticsController.safeCsvCell("@SUM(1,1)"));
    assertEquals("\"Normal \"\"Phone\"\"\"", AdminAnalyticsController.safeCsvCell("Normal \"Phone\""));
  }

  @Test
  void vnPayNormalizesIpv6LoopbackAndBlankIps() {
    assertEquals("127.0.0.1", VnPayService.normalizeIp("0:0:0:0:0:0:0:1"));
    assertEquals("127.0.0.1", VnPayService.normalizeIp("::1"));
    assertEquals("127.0.0.1", VnPayService.normalizeIp(null));
    assertEquals("203.0.113.10", VnPayService.normalizeIp("203.0.113.10"));
  }

  @Test
  void chatModelResolvesGroqVsOpenAiEndpointsAndModelsWithoutMismatch() {
    OpenAiCompatibleChatModel model = new OpenAiCompatibleChatModel();
    ReflectionTestUtils.setField(model, "customBaseUrl", "https://api.groq.com/openai/v1");
    ReflectionTestUtils.setField(model, "customModel", "openai/gpt-oss-20b");

    String openAiUrl = ReflectionTestUtils.invokeMethod(model, "resolveBaseUrl", false);
    String openAiModel = ReflectionTestUtils.invokeMethod(model, "resolveModel", false);
    assertEquals("https://api.openai.com/v1", openAiUrl);
    assertEquals("gpt-4o-mini", openAiModel);

    ReflectionTestUtils.setField(model, "customBaseUrl", "https://api.openai.com/v1");
    ReflectionTestUtils.setField(model, "customModel", "gpt-4o-mini");
    String groqUrl = ReflectionTestUtils.invokeMethod(model, "resolveBaseUrl", true);
    String groqModel = ReflectionTestUtils.invokeMethod(model, "resolveModel", true);
    assertEquals("https://api.groq.com/openai/v1", groqUrl);
    assertEquals("openai/gpt-oss-20b", groqModel);
  }

  @Test
  void ghnClientDefersHttpClientConstructionWhenDisabled() {
    GhnClient client = new GhnClient(false, false, "", 0);
    assertFalse(client.isAvailable());
    assertNull(ReflectionTestUtils.getField(client, "client"));
  }

  @Test
  void trackingJsUsesKeepaliveAndModalResetsWardSelect() throws Exception {
    String trackingJs = Files.readString(Path.of("src/main/resources/static/js/tracking.js"));
    String retailJs = Files.readString(Path.of("src/main/resources/static/js/retail.js"));

    assertTrue(trackingJs.contains("keepalive: true"));
    assertTrue(retailJs.contains("addressModalForm.querySelector('[data-address-picker]')"));
  }
}
