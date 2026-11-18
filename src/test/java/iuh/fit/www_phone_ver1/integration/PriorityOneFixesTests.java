package iuh.fit.www_phone_ver1.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "app.payments.expiry-enabled=false")
@ActiveProfiles("demo")
@Transactional
class PriorityOneFixesTests {

  @Autowired WebApplicationContext context;
  @Autowired ProductRepository products;

  private MockMvc mvc;
  private Product activeProduct;
  private Product inactiveProduct;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    activeProduct =
        products.findByStatus(1).stream().findFirst().orElseThrow();
    inactiveProduct =
        products.saveAndFlush(
            Product.builder()
                .name("Inactive Hidden Device")
                .brand("TestBrand")
                .sku("INACTIVE-" + System.nanoTime())
                .price(activeProduct.getPrice())
                .status(0)
                .build());
  }

  @Test
  void devicesJspUsesValidCustomerDeviceProperties() throws Exception {
    String jsp =
        Files.readString(Path.of("src/main/webapp/WEB-INF/views/account/devices.jsp"));
    assertFalse(jsp.contains("warrantyRemainingDays"));
    assertFalse(jsp.contains("warrantyExpiresAt"));
    assertFalse(jsp.contains("d.tickets"));
    assertTrue(jsp.contains("warrantyDaysRemaining"));
    assertTrue(jsp.contains("warrantyExpiresLabel"));
    assertTrue(jsp.contains("d.supportHistory"));
  }

  @Test
  void aiDescriptionAndCompareHideInactiveProductsAndProtectRefreshEndpoint() throws Exception {
    mvc.perform(get("/api/ai/description/{id}", activeProduct.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").isString());

    mvc.perform(get("/api/ai/description/{id}", inactiveProduct.getId()))
        .andExpect(status().isNotFound());

    mvc.perform(
            get("/api/ai/compare")
                .param("a", activeProduct.getId().toString())
                .param("b", inactiveProduct.getId().toString()))
        .andExpect(status().isBadRequest());

    mvc.perform(
            post("/api/ai/description/{id}/refresh", activeProduct.getId())
                .with(csrf()))
        .andExpect(status().is3xxRedirection());

    mvc.perform(
            post("/api/ai/description/{id}/refresh", activeProduct.getId())
                .with(user("customer").roles("USER"))
                .with(csrf()))
        .andExpect(status().isForbidden());

    mvc.perform(
            post("/api/ai/description/{id}/refresh", activeProduct.getId())
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isForbidden());

    mvc.perform(
            post("/api/ai/description/{id}/refresh", activeProduct.getId())
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").isString());
  }

  @Test
  void aiChatEnforcesLengthValidationAndRateLimit() throws Exception {
    String tooLong = "x".repeat(501);
    mvc.perform(
            post("/api/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"" + tooLong + "\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("500")));

    String uniqueIp = "10.99.88.77";
    for (int i = 0; i < 20; i++) {
      mvc.perform(
              post("/api/ai/chat")
                  .with(
                      req -> {
                        req.setRemoteAddr(uniqueIp);
                        return req;
                      })
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"message\":\"Xin chào\",\"sessionId\":\"rate-test\"}"))
          .andExpect(status().isOk());
    }

    mvc.perform(
            post("/api/ai/chat")
                .with(
                    req -> {
                      req.setRemoteAddr(uniqueIp);
                      return req;
                    })
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Xin chào\",\"sessionId\":\"rate-test\"}"))
        .andExpect(status().isTooManyRequests());
  }

  @Test
  void headerSearchOverflowIsVisibleAndChatbotCardsAvoidInnerHtmlXss() throws Exception {
    String storeCss =
        Files.readString(Path.of("src/main/resources/static/css/store.css")).replace("\r\n", "\n");
    String retailCss = Files.readString(Path.of("src/main/resources/static/css/retail.css"));
    String chatbotJs = Files.readString(Path.of("src/main/resources/static/js/chatbot.js"));
    String detailJsp =
        Files.readString(Path.of("src/main/webapp/WEB-INF/views/store/catalog/detail.jsp"));

    assertTrue(storeCss.contains("border-radius:7px;\n  position:relative;\n  overflow:visible"));
    assertTrue(retailCss.contains(".header-search{position:relative;overflow:visible}"));
    assertFalse(chatbotJs.contains("'<div class=\"chatbot-card-name\">' + p.name"));
    assertTrue(detailJsp.contains("isUserInRole('ADMIN')"));
  }
}
