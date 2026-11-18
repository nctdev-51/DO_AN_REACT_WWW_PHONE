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
  @Autowired iuh.fit.www_phone_ver1.repository.ProductMediaRepository productMedia;
  @Autowired iuh.fit.www_phone_ver1.config.ProductMediaInitializer mediaInitializer;

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

  @Test
  void productCardNoHoverSwapGalleryLoadsMultiPhotoAndStepperIsCentered() throws Exception {
    String cardTag = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/productCard.tag"));
    String galleryJsp =
        Files.readString(Path.of("src/main/webapp/WEB-INF/views/store/catalog/gallery.jsp"));
    String detailJsp =
        Files.readString(Path.of("src/main/webapp/WEB-INF/views/store/catalog/detail.jsp"));
    String cartJsp =
        Files.readString(Path.of("src/main/webapp/WEB-INF/views/store/cart/index.jsp"));
    String galleryJs = Files.readString(Path.of("src/main/resources/static/js/gallery.js"));
    String galleryCss = Files.readString(Path.of("src/main/resources/static/css/gallery.css"));
    String storeJs = Files.readString(Path.of("src/main/resources/static/js/store.js"));
    String storeCss = Files.readString(Path.of("src/main/resources/static/css/store.css"));
    String retailCss = Files.readString(Path.of("src/main/resources/static/css/retail.css"));
    String themeCss = Files.readString(Path.of("src/main/resources/static/css/theme.css"));

    String recCss =
        Files.readString(Path.of("src/main/resources/static/css/components/recommendation.css"));
    assertFalse(cardTag.contains("img-hover"));
    assertFalse(cardTag.contains("has-hover-image"));
    assertFalse(retailCss.contains(".img-hover"));
    assertFalse(themeCss.contains("scale(1.025)"));
    assertTrue(storeCss.contains(".product-card-image img + img"));
    assertTrue(storeCss.contains("overflow:hidden"));
    assertTrue(storeCss.contains("display:flex;\n  align-items:center;\n  justify-content:center")
        || storeCss.contains("display:flex;\r\n  align-items:center;\r\n  justify-content:center"));
    assertTrue(storeCss.contains("width:auto;\n  height:auto;\n  max-width:100%;\n  max-height:100%")
        || storeCss.contains("width:auto;\r\n  height:auto;\r\n  max-width:100%;\r\n  max-height:100%"));
    assertTrue(themeCss.contains("padding: 34px 20px 14px"));
    assertTrue(recCss.contains("max-height: 100%"));
    assertTrue(storeCss.contains(".qty-stepper{"));
    assertFalse(storeCss.contains("top:115px"));
    assertTrue(storeCss.contains(".product-left-extras{"));

    for (Product prod : products.findByStatus(1)) {
      Path imgPath = Path.of("src/main/resources/static" + prod.getImage());
      assertTrue(Files.exists(imgPath), "Missing product image: " + prod.getImage());
    }

    assertFalse(galleryJsp.contains("Hình ảnh"));
    assertTrue(galleryJsp.contains("data-gallery-tabs"));
    assertTrue(galleryJsp.contains("data-gallery-thumbs"));
    assertTrue(galleryJsp.contains("data-gallery-dialog"));
    assertEquals(
        galleryJsp.split("<div\\b", -1).length, galleryJsp.split("</div>", -1).length);
    assertFalse(galleryCss.contains("position: sticky"));
    assertFalse(galleryCss.contains(".is-single .gallery-arrow"));
    assertTrue(galleryCss.contains("top: 50%; transform: translateY(-50%)"));
    assertTrue(galleryJs.contains("groups.push([key, label, group, selectedText])"));
    assertTrue(galleryJs.contains("groups.forEach(([key, label, group, selectedText])"));

    assertTrue(detailJsp.contains("data-qty-stepper"));
    assertTrue(detailJsp.contains("id=\"buy-quantity\""));
    assertTrue(detailJsp.contains("class=\"product-left-extras\""));
    assertTrue(cartJsp.contains("data-qty-stepper"));
    assertTrue(storeJs.contains("[data-qty-stepper]"));

    Product honor = products.findBySku("PS-HONOR-400").orElseThrow();
    // Simulate an existing DB that only had 1 photo row, then verify initializer backfills the rest.
    var originalHonorMedia = productMedia.findByProduct_IdOrderBySortOrderAscIdAsc(honor.getId());
    if (originalHonorMedia.size() > 1) {
      productMedia.deleteAll(originalHonorMedia.subList(1, originalHonorMedia.size()));
      productMedia.flush();
    }
    mediaInitializer.run();

    String json =
        mvc.perform(get("/products/{id}/media", honor.getId()).param("color", "Vàng"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertTrue(json.contains("\"photos\":[") && json.split("\"url\":").length > 5);
  }
}
