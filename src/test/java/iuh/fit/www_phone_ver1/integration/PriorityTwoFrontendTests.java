package iuh.fit.www_phone_ver1.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "app.payments.expiry-enabled=false")
@ActiveProfiles("demo")
@Transactional
class PriorityTwoFrontendTests {

  @Autowired WebApplicationContext context;
  @Autowired ProductRepository products;

  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  void productDetailEnablesCarouselAssetsWhileHomeSkipsThem() throws Exception {
    Product sample = products.findByStatus(1).stream().findFirst().orElseThrow();

    mvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(model().attributeDoesNotExist("includeRecommendationCarousel"));

    mvc.perform(get("/products/{id}", sample.getId()))
        .andExpect(status().isOk())
        .andExpect(model().attribute("includeRecommendationCarousel", true));
  }

  @Test
  void carouselAndPlaceholderAssetsHaveSafeFallbackAndSkeletonSizing() throws Exception {
    assertTrue(Files.exists(Path.of("src/main/resources/static/images/products/placeholder.svg")));

    String recJs = Files.readString(Path.of("src/main/resources/static/js/recommendation.js"));
    String recCss =
        Files.readString(Path.of("src/main/resources/static/css/components/recommendation.css"));

    assertTrue(recJs.contains("/images/products/placeholder.svg"));
    assertTrue(recJs.contains("fallbackApplied"));
    assertTrue(recJs.contains("new window.Swiper(swiperContainer"));
    assertTrue(recJs.contains("showFallback(section, swiperContainer, fallbackGrid)"));
    assertTrue(recCss.contains(".similar-swiper:not(.swiper-initialized) .swiper-slide"));
    assertTrue(recCss.contains("--swiper-navigation-size: 14px"));
  }

  @Test
  void responsiveMultiScreenLayoutAndMobileDrawerRulesArePresent() throws Exception {
    String headerJsp =
        Files.readString(Path.of("src/main/webapp/WEB-INF/views/shared/store/header.jsp"));
    String detailJsp =
        Files.readString(Path.of("src/main/webapp/WEB-INF/views/store/catalog/detail.jsp"));
    String addressTag =
        Files.readString(Path.of("src/main/webapp/WEB-INF/tags/addressPicker.tag"));
    String tokensCss = Files.readString(Path.of("src/main/resources/static/css/tokens.css"));
    String chatbotCss =
        Files.readString(Path.of("src/main/resources/static/css/components/chatbot.css"));
    String storeCss = Files.readString(Path.of("src/main/resources/static/css/store.css"));
    String retailCss = Files.readString(Path.of("src/main/resources/static/css/retail.css"));
    String themeCss = Files.readString(Path.of("src/main/resources/static/css/theme.css"));
    String compareCss = Files.readString(Path.of("src/main/resources/static/css/comparison.css"));
    String galleryCss = Files.readString(Path.of("src/main/resources/static/css/gallery.css"));

    assertTrue(headerJsp.contains("id=\"mobileMenuDrawer\""));
    assertTrue(
        detailJsp
            .replace("\r\n", "\n")
            .contains("<section class=\"mb-4\">\n  <div class=\"ai-description-panel panel\" id=\"ai-desc-panel\""));
    assertTrue(addressTag.contains("address-picker-grid"));
    assertFalse(addressTag.contains("class=\"filter-grid\""));
    assertTrue(tokensCss.contains("--layout-content-width: 1360px"));
    assertTrue(tokensCss.contains("--header-offset: 164px"));
    assertTrue(chatbotCss.contains("calc(100dvh - 176px)"));
    assertTrue(chatbotCss.contains(".chatbot-fab[aria-expanded=\"true\"]"));
    assertTrue(storeCss.contains(".address-picker-grid{"));
    assertTrue(storeCss.contains("flex-wrap:nowrap;\n    overflow-x:auto")
        || storeCss.contains("flex-wrap:nowrap;\r\n    overflow-x:auto"));
    assertTrue(retailCss.contains(".compare-tray{position:fixed;left:20px;right:auto"));
    assertTrue(retailCss.contains("padding:14px 40px 14px 14px"));
    assertTrue(themeCss.contains("clamp(1.2rem, 1.8vw, var(--font-size-3xl))"));
    assertTrue(compareCss.contains("position:sticky;\n  left:0")
        || compareCss.contains("position:sticky;\r\n  left:0"));
    assertTrue(galleryCss.contains("max-height: 780px"));
  }
}
