package iuh.fit.www_phone_ver1.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.repository.mongo.ProductEmbeddingRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "app.payments.expiry-enabled=false")
@ActiveProfiles("demo")
@Transactional
class RecommendationApiTests {

  @Autowired WebApplicationContext context;
  @Autowired ProductRepository products;
  @MockitoBean ProductEmbeddingRepository embeddingRepository;

  private MockMvc mvc;
  private Product sampleProduct;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    Mockito.when(embeddingRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
    sampleProduct =
        products.findByStatus(1).stream()
            .filter(p -> "PHONE".equals(p.getKind()))
            .findFirst()
            .orElseThrow();
  }

  @Test
  void recommendationsEndpointRejectsMalformedIdWithJsonBadRequest() throws Exception {
    mvc.perform(get("/api/recommendations/not-a-number"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").isString());
  }

  @Test
  void recommendationsEndpointReturnsValidJsonWithFallback() throws Exception {
    mvc.perform(get("/api/recommendations/{id}", sampleProduct.getId()).param("limit", "6"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productId", is(sampleProduct.getId().intValue())))
        .andExpect(jsonPath("$.source", is("fallback")))
        .andExpect(jsonPath("$.recommendations.length()", greaterThan(0)))
        .andExpect(jsonPath("$.recommendations.length()", lessThanOrEqualTo(6)))
        .andExpect(jsonPath("$.recommendations[0].name").isString())
        .andExpect(jsonPath("$.recommendations[0].brand").isString())
        .andExpect(jsonPath("$.recommendations[0].kind").isString())
        .andExpect(jsonPath("$.recommendations[0].inStock").isBoolean())
        .andExpect(
            jsonPath(
                "$.recommendations[*].id",
                everyItem(not(sampleProduct.getId().intValue()))));
  }

  @Test
  void recommendationsEndpointReturnsNotFoundForMissingProduct() throws Exception {
    mvc.perform(get("/api/recommendations/999999"))
        .andExpect(status().isNotFound());
  }

  @Test
  void reindexEndpointRequiresAdminAndReturnsSummary() throws Exception {
    mvc.perform(post("/api/recommendations/reindex").with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").isString());

    mvc.perform(
            post("/api/recommendations/reindex")
                .with(user("customer").roles("USER"))
                .with(csrf()))
        .andExpect(status().isForbidden());

    mvc.perform(
            post("/api/recommendations/reindex")
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isForbidden());

    mvc.perform(
            post("/api/recommendations/reindex")
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalProducts", greaterThan(0)))
        .andExpect(jsonPath("$.failed", is(0)));
  }

  @Test
  void detailPageRendersRecommendationCarouselAndScripts() throws Exception {
    mvc.perform(get("/products/{id}", sampleProduct.getId()))
        .andExpect(status().isOk())
        .andExpect(view().name("store/catalog/detail"))
        .andExpect(forwardedUrl("/WEB-INF/views/store/catalog/detail.jsp"))
        .andExpect(model().attributeExists("product", "relatedProducts"));

    String detailJsp =
        Files.readString(Path.of("src/main/webapp/WEB-INF/views/store/catalog/detail.jsp"));
    String footerJsp =
        Files.readString(Path.of("src/main/webapp/WEB-INF/views/shared/store/footer.jsp"));
    assertTrue(detailJsp.contains("id=\"similar-products\""));
    assertTrue(detailJsp.contains("data-product-id=\"${product.id}\""));
    assertTrue(footerJsp.contains("swiper-bundle.min.js"));
    assertTrue(footerJsp.contains("js/recommendation.js"));
  }
}
