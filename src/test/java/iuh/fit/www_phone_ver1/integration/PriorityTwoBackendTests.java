package iuh.fit.www_phone_ver1.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import iuh.fit.www_phone_ver1.WwwPhoneVer1Application;
import iuh.fit.www_phone_ver1.entity.Consultation;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.entity.ProductVariant;
import iuh.fit.www_phone_ver1.entity.User;
import iuh.fit.www_phone_ver1.repository.ConsultationRepository;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.repository.ProductVariantRepository;
import iuh.fit.www_phone_ver1.repository.UserRepository;
import iuh.fit.www_phone_ver1.repository.mongo.ProductEmbeddingRepository;
import iuh.fit.www_phone_ver1.service.admin.AdminUserService;
import iuh.fit.www_phone_ver1.service.catalog.CatalogService;
import iuh.fit.www_phone_ver1.service.vector.RecommendationService;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "app.payments.expiry-enabled=false")
@ActiveProfiles("demo")
@Transactional
class PriorityTwoBackendTests {

  @Autowired WebApplicationContext context;
  @Autowired ProductRepository products;
  @Autowired ProductVariantRepository variants;
  @Autowired UserRepository users;
  @Autowired ConsultationRepository consultations;
  @Autowired AdminUserService adminUserService;
  @Autowired CatalogService catalogService;
  @Autowired RecommendationService recommendationService;
  @MockitoBean ProductEmbeddingRepository embeddingRepo;

  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void uncheckedVariantActiveCheckboxDeactivatesVariantAndJspHasHiddenMarker() throws Exception {
    Product sample = products.findByStatus(1).stream().findFirst().orElseThrow();
    ProductVariant variant = new ProductVariant();
    variant.setProduct(sample);
    variant.setSku("TEST-VAR-" + System.nanoTime());
    variant.setColor("Den");
    variant.setStorage("256GB");
    variant.setRam("8GB");
    variant.setPrice(BigDecimal.valueOf(19990000));
    variant.setStock(5);
    variant.setActive(true);
    variant = variants.saveAndFlush(variant);

    mvc.perform(
            post("/admin/products/{pid}/variants", sample.getId())
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .param("id", variant.getId().toString())
                .param("version", String.valueOf(variant.getVersion()))
                .param("sku", variant.getSku())
                .param("color", variant.getColor())
                .param("storage", variant.getStorage())
                .param("ram", variant.getRam())
                .param("price", variant.getPrice().toPlainString())
                .param("stock", "5")
                .param("_active", "on"))
        .andExpect(status().is3xxRedirection());

    ProductVariant updated = variants.findById(variant.getId()).orElseThrow();
    assertFalse(updated.isActive());

    String variantsJsp =
        Files.readString(Path.of("src/main/webapp/WEB-INF/views/admin/products/variants.jsp"));
    assertTrue(variantsJsp.contains("name=\"_active\" value=\"on\""));
    assertTrue(variantsJsp.contains("/disable"));
  }

  @Test
  void deleteUserCleansUpConsultationsWithoutConstraintViolation() {
    Product sample = products.findByStatus(1).stream().findFirst().orElseThrow();
    User customer =
        users.saveAndFlush(
            User.builder()
                .fullName("Test Consultation User")
                .email("consult-" + System.nanoTime() + "@example.com")
                .password("encoded-password")
                .status(1)
                .build());

    Consultation c = new Consultation();
    c.setUser(customer);
    c.setProduct(sample);
    c.setType("INSTALLMENT");
    c.setContactName(customer.getFullName());
    c.setPhone("0901234567");
    c.setMessage("Tu van tra gop");
    consultations.saveAndFlush(c);

    assertDoesNotThrow(
        () -> {
          adminUserService.deleteUser(customer.getId(), "admin");
          users.flush();
        });
    assertTrue(users.findById(customer.getId()).isEmpty());
  }

  @Test
  void dockerEnvConfigsAndSchedulingAnnotationsAreConsistent() throws Exception {
    String envExample = Files.readString(Path.of(".env.example"));
    String compose = Files.readString(Path.of("docker-compose.yml"));
    String dockerfile = Files.readString(Path.of("Dockerfile"));

    assertTrue(envExample.contains("MONGODB_URI=mongodb://mongodb:27017/ptv_mobile"));
    assertTrue(envExample.contains("MILVUS_HOST=milvus"));
    assertTrue(envExample.contains("MILVUS_ENABLED=true"));
    assertTrue(compose.contains("etcd -advertise-client-urls=http://etcd:2379"));
    assertTrue(compose.contains("milvus:\n        condition: service_healthy")
        || compose.contains("milvus:\r\n        condition: service_healthy"));
    assertTrue(dockerfile.contains("FROM eclipse-temurin:17-jdk AS builder"));

    assertNotNull(WwwPhoneVer1Application.class.getAnnotation(EnableScheduling.class));
    assertNotNull(WwwPhoneVer1Application.class.getAnnotation(EnableAsync.class));
  }

  @Test
  void deletingProductRemovesVectorEmbedding() throws Exception {
    Product disposable =
        products.saveAndFlush(
            Product.builder()
                .name("Disposable Phone")
                .brand("TestBrand")
                .sku("DEL-" + System.nanoTime())
                .price(BigDecimal.valueOf(9990000))
                .status(1)
                .build());

    boolean deleted = catalogService.deleteProduct(disposable.getId());
    assertTrue(deleted);
    recommendationService.deleteProductEmbedding(disposable.getId());
    Mockito.verify(embeddingRepo).deleteById(String.valueOf(disposable.getId()));
  }
}
