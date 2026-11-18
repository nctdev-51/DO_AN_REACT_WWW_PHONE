package iuh.fit.www_phone_ver1.service.vector;

import iuh.fit.www_phone_ver1.dto.catalog.RecommendationResponseDTO;
import iuh.fit.www_phone_ver1.dto.catalog.RecommendedProductDTO;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.entity.mongo.ProductEmbeddingDocument;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.repository.mongo.ProductEmbeddingRepository;
import iuh.fit.www_phone_ver1.service.ai.EmbeddingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates product recommendations using Milvus vector similarity cached in MongoDB, with an
 * automatic MariaDB fallback when Milvus or MongoDB is unavailable.
 */
@Service
public class RecommendationService {

  private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
  public static final String SOURCE_MILVUS = "milvus";
  public static final String SOURCE_FALLBACK = "fallback";

  private final VectorStoreService vectorStore;
  private final EmbeddingService embeddingService;
  private final ProductRepository products;
  private final ProductEmbeddingRepository embeddingRepo;

  public record ReindexSummary(int totalProducts, int indexed, int failed, boolean milvusAvailable) {}

  public RecommendationService(
      VectorStoreService vectorStore,
      EmbeddingService embeddingService,
      ProductRepository products,
      ProductEmbeddingRepository embeddingRepo) {
    this.vectorStore = vectorStore;
    this.embeddingService = embeddingService;
    this.products = products;
    this.embeddingRepo = embeddingRepo;
  }

  @Transactional(readOnly = true)
  public Optional<RecommendationResponseDTO> recommend(Long productId, int limit) {
    if (productId == null) {
      return Optional.empty();
    }
    int safeLimit = Math.max(1, Math.min(limit, 12));
    Optional<Product> anchorOpt =
        products.findById(productId).filter(p -> Integer.valueOf(1).equals(p.getStatus()));
    if (anchorOpt.isEmpty()) {
      return Optional.empty();
    }
    Product anchor = anchorOpt.get();

    List<Product> selected = new ArrayList<>(safeLimit);
    Set<Long> seenIds = new HashSet<>();
    seenIds.add(anchor.getId());

    boolean usedMilvus = false;
    if (vectorStore.isAvailable()) {
      Optional<float[]> queryVecOpt = resolveOrIndexEmbedding(anchor, true);
      List<Long> candidateIds =
          queryVecOpt.map(vec -> vectorStore.searchSimilar(vec, safeLimit + 6)).orElse(List.of());
      if (!candidateIds.isEmpty()) {
        Map<Long, Product> byId = new LinkedHashMap<>();
        for (Product p : products.findAllById(candidateIds)) {
          if (Integer.valueOf(1).equals(p.getStatus())) {
            byId.put(p.getId(), p);
          }
        }
        for (Long cid : candidateIds) {
          if (selected.size() >= safeLimit) {
            break;
          }
          if (seenIds.contains(cid)) {
            continue;
          }
          Product candidate = byId.get(cid);
          if (candidate != null && Objects.equals(anchor.getKind(), candidate.getKind())) {
            selected.add(candidate);
            seenIds.add(cid);
            usedMilvus = true;
          }
        }
        // Fill remaining vector hits if kind filtering left room
        for (Long cid : candidateIds) {
          if (selected.size() >= safeLimit) {
            break;
          }
          if (seenIds.contains(cid)) {
            continue;
          }
          Product candidate = byId.get(cid);
          if (candidate != null) {
            selected.add(candidate);
            seenIds.add(cid);
            usedMilvus = true;
          }
        }
      }
    }

    if (selected.size() < safeLimit) {
      appendFallbackProducts(anchor, selected, seenIds, safeLimit);
    }

    List<RecommendedProductDTO> dtos = selected.stream().map(RecommendedProductDTO::from).toList();
    String source = usedMilvus ? SOURCE_MILVUS : SOURCE_FALLBACK;
    return Optional.of(new RecommendationResponseDTO(anchor.getId(), source, dtos));
  }

  @Transactional(readOnly = true)
  public ReindexSummary reindexAll() {
    List<Product> activeProducts = products.findByStatus(1);
    int indexed = 0;
    int failed = 0;
    boolean milvusReady = vectorStore.isAvailable();

    for (Product product : activeProducts) {
      try {
        Optional<float[]> vecOpt = resolveOrIndexEmbedding(product, false);
        if (vecOpt.isPresent() && vecOpt.get().length == embeddingService.getDimension()) {
          if (!milvusReady || vectorStore.upsertEmbedding(product.getId(), vecOpt.get())) {
            indexed++;
          } else {
            failed++;
          }
        } else {
          failed++;
        }
      } catch (Exception ex) {
        failed++;
        log.warn("Failed to index product {}: {}", product.getId(), ex.getMessage());
      }
    }

    log.info(
        "Vector reindex completed: total={}, indexed={}, failed={}, milvusAvailable={}",
        activeProducts.size(),
        indexed,
        failed,
        milvusReady);
    return new ReindexSummary(activeProducts.size(), indexed, failed, milvusReady);
  }

  private Optional<float[]> resolveOrIndexEmbedding(Product product, boolean syncToMilvus) {
    String expectedHash = embeddingService.contentHash(product);
    String activeProvider = embeddingService.activeProvider();
    String key = String.valueOf(product.getId());
    try {
      Optional<ProductEmbeddingDocument> cached = embeddingRepo.findById(key);
      if (cached.isPresent()) {
        ProductEmbeddingDocument doc = cached.get();
        if (expectedHash.equals(doc.getContentHash())
            && activeProvider.equals(doc.getProvider())
            && doc.getVector() != null
            && doc.getVector().size() == embeddingService.getDimension()) {
          float[] vec = EmbeddingService.toFloatArray(doc.getVector());
          if (syncToMilvus) {
            vectorStore.upsertEmbedding(product.getId(), vec);
          }
          return Optional.of(vec);
        }
      }
    } catch (Exception ex) {
      log.debug("MongoDB embedding cache unavailable ({}), computing inline", ex.getMessage());
    }

    EmbeddingService.EmbeddingResult generated = embeddingService.embedWithMetadata(product);
    if (!activeProvider.equals(generated.provider())) {
      log.warn(
          "Skipping cache/Milvus write for product {}: expected provider '{}' but generated '{}' due to transient fallback",
          product.getId(),
          activeProvider,
          generated.provider());
      return Optional.empty();
    }
    try {
      ProductEmbeddingDocument doc =
          new ProductEmbeddingDocument(
              key,
              EmbeddingService.toFloatList(generated.vector()),
              generated.contentHash(),
              generated.provider(),
              LocalDateTime.now());
      embeddingRepo.save(doc);
    } catch (Exception ex) {
      log.debug("Could not persist embedding to MongoDB ({})", ex.getMessage());
    }

    if (syncToMilvus) {
      vectorStore.upsertEmbedding(product.getId(), generated.vector());
    }
    return Optional.of(generated.vector());
  }

  private void appendFallbackProducts(
      Product anchor, List<Product> selected, Set<Long> seenIds, int limit) {
    Long anchorCatId = anchor.getCategory() != null ? anchor.getCategory().getId() : null;
    BigDecimal anchorPrice = anchor.getPrice() != null ? anchor.getPrice() : BigDecimal.ZERO;

    List<Product> pool =
        products.findByStatus(1).stream()
            .filter(p -> !seenIds.contains(p.getId()))
            .sorted(
                Comparator.comparingInt((Product p) -> fallbackPriority(anchor, anchorCatId, p))
                    .thenComparing(
                        p ->
                            p.getPrice() == null
                                ? anchorPrice
                                : p.getPrice().subtract(anchorPrice).abs())
                    .thenComparing(Product::getId))
            .limit(limit - selected.size())
            .toList();

    for (Product p : pool) {
      selected.add(p);
      seenIds.add(p.getId());
    }
  }

  private static int fallbackPriority(Product anchor, Long anchorCatId, Product candidate) {
    boolean sameKind = Objects.equals(anchor.getKind(), candidate.getKind());
    boolean sameBrand =
        anchor.getBrand() != null && anchor.getBrand().equalsIgnoreCase(candidate.getBrand());
    Long candidateCatId = candidate.getCategory() != null ? candidate.getCategory().getId() : null;
    boolean sameCategory = anchorCatId != null && anchorCatId.equals(candidateCatId);

    if (sameKind && sameCategory && sameBrand) return 0;
    if (sameKind && sameCategory) return 1;
    if (sameKind && sameBrand) return 2;
    if (sameKind) return 3;
    return 4;
  }
}
