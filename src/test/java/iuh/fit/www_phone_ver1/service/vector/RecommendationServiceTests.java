package iuh.fit.www_phone_ver1.service.vector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import iuh.fit.www_phone_ver1.config.VectorIndexJob;
import iuh.fit.www_phone_ver1.dto.catalog.RecommendationResponseDTO;
import iuh.fit.www_phone_ver1.entity.Category;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.entity.ProductVariant;
import iuh.fit.www_phone_ver1.entity.mongo.ProductEmbeddingDocument;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.repository.mongo.ProductEmbeddingRepository;
import iuh.fit.www_phone_ver1.service.ai.EmbeddingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecommendationServiceTests {

  private EmbeddingService embeddingService;
  private VectorStoreService vectorStore;
  private ProductRepository products;
  private ProductEmbeddingRepository embeddingRepo;
  private RecommendationService recommendationService;

  private Product anchorPhone;
  private Product similarFlagship;
  private Product sameBrandMid;
  private Product phoneCase;

  @BeforeEach
  void setUp() {
    embeddingService = new EmbeddingService();
    vectorStore = mock(VectorStoreService.class);
    products = mock(ProductRepository.class);
    embeddingRepo = mock(ProductEmbeddingRepository.class);
    recommendationService =
        new RecommendationService(vectorStore, embeddingService, products, embeddingRepo);

    Category phones = Category.builder().id(1L).name("Dien thoai").build();
    Category accessories = Category.builder().id(2L).name("Phu kien").build();

    anchorPhone =
        product(
            1L,
            "Galaxy S25 Ultra",
            "Samsung",
            "PHONE",
            phones,
            "33990000",
            "Snapdragon 8 Elite",
            "200MP",
            "5000 mAh",
            "CAMERA,GAMING",
            5);
    similarFlagship =
        product(
            2L,
            "Xiaomi 15 Ultra",
            "Xiaomi",
            "PHONE",
            phones,
            "31990000",
            "Snapdragon 8 Elite",
            "200MP Leica",
            "5410 mAh",
            "CAMERA,GAMING",
            4);
    sameBrandMid =
        product(
            3L,
            "Galaxy A56",
            "Samsung",
            "PHONE",
            phones,
            "9990000",
            "Exynos 1580",
            "50MP",
            "5000 mAh",
            "EVERYDAY",
            10);
    phoneCase =
        product(
            4L,
            "Op lung MagSafe",
            "Apple",
            "ACCESSORY",
            accessories,
            "990000",
            null,
            null,
            null,
            null,
            12);
  }

  @Test
  void embeddingServiceGeneratesNormalizedDeterministicVectors() {
    float[] v1 = embeddingService.embed(anchorPhone);
    float[] v1Again = embeddingService.embed(anchorPhone);
    float[] v2 = embeddingService.embed(similarFlagship);
    float[] vCase = embeddingService.embed(phoneCase);

    assertEquals(1536, v1.length);
    assertArrayEquals(v1, v1Again, 1e-6f);

    double norm = Math.sqrt(dot(v1, v1));
    assertEquals(1.0, norm, 1e-4);

    double simPhone = dot(v1, v2);
    double simAccessory = dot(v1, vCase);
    assertTrue(
        simPhone > simAccessory,
        "Flagship phones should be more similar to each other than to a phone case");
  }

  @Test
  void recommendUsesMilvusAndMongoCacheHitWhenAvailable() {
    float[] cachedVec = embeddingService.embed(anchorPhone);
    ProductEmbeddingDocument cachedDoc =
        new ProductEmbeddingDocument(
            "1",
            EmbeddingService.toFloatList(cachedVec),
            embeddingService.contentHash(anchorPhone),
            EmbeddingService.PROVIDER_LOCAL,
            LocalDateTime.now());

    when(products.findById(1L)).thenReturn(Optional.of(anchorPhone));
    when(vectorStore.isAvailable()).thenReturn(true);
    when(embeddingRepo.findById("1")).thenReturn(Optional.of(cachedDoc));
    when(vectorStore.searchSimilar(any(float[].class), anyInt())).thenReturn(List.of(1L, 2L, 3L));
    when(products.findAllById(List.of(1L, 2L, 3L)))
        .thenReturn(List.of(anchorPhone, sameBrandMid, similarFlagship));
    when(products.findByStatus(1))
        .thenReturn(List.of(anchorPhone, similarFlagship, sameBrandMid, phoneCase));

    RecommendationResponseDTO result = recommendationService.recommend(1L, 2).orElseThrow();

    assertEquals(1L, result.productId());
    assertEquals(RecommendationService.SOURCE_MILVUS, result.source());
    assertEquals(2, result.recommendations().size());
    assertEquals(2L, result.recommendations().get(0).id());
    assertEquals(3L, result.recommendations().get(1).id());
    verify(embeddingRepo, never()).save(any());
  }

  @Test
  void recommendGeneratesAndCachesEmbeddingOnCacheMiss() {
    when(products.findById(1L)).thenReturn(Optional.of(anchorPhone));
    when(vectorStore.isAvailable()).thenReturn(true);
    when(embeddingRepo.findById("1")).thenReturn(Optional.empty());
    when(vectorStore.searchSimilar(any(float[].class), anyInt())).thenReturn(List.of(2L));
    when(products.findAllById(List.of(2L))).thenReturn(List.of(similarFlagship));
    when(products.findByStatus(1))
        .thenReturn(List.of(anchorPhone, similarFlagship, sameBrandMid));

    RecommendationResponseDTO result = recommendationService.recommend(1L, 2).orElseThrow();

    assertEquals(RecommendationService.SOURCE_MILVUS, result.source());
    assertEquals(2, result.recommendations().size());
    verify(embeddingRepo).save(any(ProductEmbeddingDocument.class));
    verify(vectorStore).upsertEmbedding(eq(1L), any(float[].class));
  }

  @Test
  void recommendFallsBackToMariaDbWhenMilvusIsUnavailable() {
    when(products.findById(1L)).thenReturn(Optional.of(anchorPhone));
    when(vectorStore.isAvailable()).thenReturn(false);
    when(products.findByStatus(1))
        .thenReturn(List.of(anchorPhone, phoneCase, similarFlagship, sameBrandMid));

    RecommendationResponseDTO result = recommendationService.recommend(1L, 3).orElseThrow();

    assertEquals(RecommendationService.SOURCE_FALLBACK, result.source());
    assertEquals(3, result.recommendations().size());
    assertFalse(result.recommendations().stream().anyMatch(p -> p.id().equals(1L)));
    // Same kind + category + brand (Galaxy A56) ranks ahead of different brand and accessory
    assertEquals(3L, result.recommendations().get(0).id());
    assertEquals(2L, result.recommendations().get(1).id());
    assertEquals(4L, result.recommendations().get(2).id());
  }

  @Test
  void vectorIndexJobReindexesAllActiveProductsWithoutErrors() {
    when(products.findByStatus(1)).thenReturn(List.of(anchorPhone, similarFlagship));
    when(vectorStore.isAvailable()).thenReturn(true);
    when(embeddingRepo.findById(any())).thenReturn(Optional.empty());
    when(vectorStore.upsertEmbedding(any(), any(float[].class))).thenReturn(true);

    VectorIndexJob job = new VectorIndexJob(recommendationService, false);
    assertDoesNotThrow(job::reindexAll);

    verify(vectorStore).upsertEmbedding(eq(1L), any(float[].class));
    verify(vectorStore).upsertEmbedding(eq(2L), any(float[].class));
  }

  @Test
  void recommendInvalidatesStaleProviderCacheAndAvoidsMixingVectorSpaces() {
    EmbeddingService mockEmbedding = mock(EmbeddingService.class);
    when(mockEmbedding.getDimension()).thenReturn(1536);
    when(mockEmbedding.activeProvider()).thenReturn(EmbeddingService.PROVIDER_OPENAI);
    when(mockEmbedding.contentHash(anchorPhone)).thenReturn("openai-hash-1");

    // Stale local-fallback document still sitting in MongoDB
    ProductEmbeddingDocument staleDoc =
        new ProductEmbeddingDocument(
            "1",
            EmbeddingService.toFloatList(embeddingService.embed(anchorPhone)),
            "old-local-hash",
            EmbeddingService.PROVIDER_LOCAL,
            LocalDateTime.now());

    float[] openAiVec = new float[1536];
    openAiVec[0] = 1.0f;
    when(mockEmbedding.embedWithMetadata(anchorPhone))
        .thenReturn(
            new EmbeddingService.EmbeddingResult(
                openAiVec, "openai-hash-1", EmbeddingService.PROVIDER_OPENAI));

    when(products.findById(1L)).thenReturn(Optional.of(anchorPhone));
    when(vectorStore.isAvailable()).thenReturn(true);
    when(embeddingRepo.findById("1")).thenReturn(Optional.of(staleDoc));
    when(vectorStore.searchSimilar(openAiVec, 8)).thenReturn(List.of(2L));
    when(products.findAllById(List.of(2L))).thenReturn(List.of(similarFlagship));
    when(products.findByStatus(1)).thenReturn(List.of(anchorPhone, similarFlagship, sameBrandMid));

    RecommendationService openAiService =
        new RecommendationService(vectorStore, mockEmbedding, products, embeddingRepo);
    RecommendationResponseDTO res = openAiService.recommend(1L, 2).orElseThrow();

    assertEquals(RecommendationService.SOURCE_MILVUS, res.source());
    verify(embeddingRepo).save(any(ProductEmbeddingDocument.class));
    verify(vectorStore).upsertEmbedding(1L, openAiVec);
  }

  @Test
  void recommendSkipsMilvusWhenOpenAiTransientlyFallsBackToLocalVector() {
    EmbeddingService mockEmbedding = mock(EmbeddingService.class);
    when(mockEmbedding.getDimension()).thenReturn(1536);
    when(mockEmbedding.activeProvider()).thenReturn(EmbeddingService.PROVIDER_OPENAI);
    when(mockEmbedding.contentHash(anchorPhone)).thenReturn("openai-hash-1");
    when(mockEmbedding.embedWithMetadata(anchorPhone))
        .thenReturn(
            new EmbeddingService.EmbeddingResult(
                embeddingService.embed(anchorPhone),
                "openai-hash-1",
                EmbeddingService.PROVIDER_LOCAL));

    when(products.findById(1L)).thenReturn(Optional.of(anchorPhone));
    when(vectorStore.isAvailable()).thenReturn(true);
    when(embeddingRepo.findById("1")).thenReturn(Optional.empty());
    when(products.findByStatus(1)).thenReturn(List.of(anchorPhone, similarFlagship, sameBrandMid));

    RecommendationService openAiService =
        new RecommendationService(vectorStore, mockEmbedding, products, embeddingRepo);
    RecommendationResponseDTO res = openAiService.recommend(1L, 2).orElseThrow();

    assertEquals(RecommendationService.SOURCE_FALLBACK, res.source());
    verify(embeddingRepo, never()).save(any());
    verify(vectorStore, never()).upsertEmbedding(any(), any(float[].class));
    verify(vectorStore, never()).searchSimilar(any(float[].class), anyInt());
  }

  private static double dot(float[] a, float[] b) {
    double sum = 0.0;
    for (int i = 0; i < a.length; i++) {
      sum += (double) a[i] * b[i];
    }
    return sum;
  }

  private static Product product(
      Long id,
      String name,
      String brand,
      String kind,
      Category category,
      String price,
      String chipset,
      String camera,
      String battery,
      String usageTags,
      int stock) {
    Product p =
        Product.builder()
            .id(id)
            .name(name)
            .brand(brand)
            .kind(kind)
            .category(category)
            .price(new BigDecimal(price))
            .chipset(chipset)
            .camera(camera)
            .battery(battery)
            .usageTags(usageTags)
            .status(1)
            .image("/images/products/" + id + ".png")
            .build();
    ProductVariant variant = new ProductVariant();
    variant.setId(id * 10);
    variant.setProduct(p);
    variant.setSku("SKU-" + id);
    variant.setColor("Den");
    variant.setStorage("256GB");
    variant.setPrice(p.getPrice());
    variant.setStock(stock);
    variant.setActive(true);
    p.setVariants(List.of(variant));
    return p;
  }
}
