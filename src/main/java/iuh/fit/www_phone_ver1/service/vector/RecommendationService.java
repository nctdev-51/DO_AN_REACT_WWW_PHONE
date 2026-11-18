package iuh.fit.www_phone_ver1.service.vector;

import iuh.fit.www_phone_ver1.dto.catalog.RecommendationResponseDTO;
import iuh.fit.www_phone_ver1.dto.catalog.RecommendedProductDTO;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.entity.mongo.ProductEmbeddingDocument;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import iuh.fit.www_phone_ver1.repository.mongo.ProductEmbeddingRepository;
import iuh.fit.www_phone_ver1.service.ai.EmbeddingService;
import iuh.fit.www_phone_ver1.service.ai.ProductDescriptionService;
import iuh.fit.www_phone_ver1.service.catalog.CatalogService;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Generates product recommendations using Milvus vector similarity cached in MongoDB, with an
 * automatic MariaDB fallback when Milvus or MongoDB is unavailable.
 */
@Service
public class RecommendationService {

  private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
  public static final String SOURCE_MILVUS = "milvus";
  public static final String SOURCE_FALLBACK = "fallback";
  private static final int MIN_MILVUS_CANDIDATES = 24;

  private final VectorStoreService vectorStore;
  private final EmbeddingService embeddingService;
  private final ProductRepository products;
  private final ProductEmbeddingRepository embeddingRepo;
  private final TransactionTemplate readTxTemplate;
  private final ProductDescriptionService descriptionService;
  private final AtomicBoolean reindexInProgress = new AtomicBoolean(false);

  public record ReindexSummary(int totalProducts, int indexed, int failed, boolean milvusAvailable) {}

  @Autowired
  public RecommendationService(
      VectorStoreService vectorStore,
      EmbeddingService embeddingService,
      ProductRepository products,
      ProductEmbeddingRepository embeddingRepo,
      ObjectProvider<PlatformTransactionManager> txManagerProvider,
      ObjectProvider<ProductDescriptionService> descriptionServiceProvider) {
    this.vectorStore = vectorStore;
    this.embeddingService = embeddingService;
    this.products = products;
    this.embeddingRepo = embeddingRepo;
    PlatformTransactionManager txManager = txManagerProvider.getIfAvailable();
    if (txManager != null) {
      TransactionTemplate template = new TransactionTemplate(txManager);
      template.setReadOnly(true);
      template.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
      this.readTxTemplate = template;
    } else {
      this.readTxTemplate = null;
    }
    this.descriptionService = descriptionServiceProvider.getIfAvailable();
  }

  public RecommendationService(
      VectorStoreService vectorStore,
      EmbeddingService embeddingService,
      ProductRepository products,
      ProductEmbeddingRepository embeddingRepo) {
    this.vectorStore = vectorStore;
    this.embeddingService = embeddingService;
    this.products = products;
    this.embeddingRepo = embeddingRepo;
    this.readTxTemplate = null;
    this.descriptionService = null;
  }

  public Optional<RecommendationResponseDTO> recommend(Long productId, int limit) {
    if (productId == null) {
      return Optional.empty();
    }
    int safeLimit = Math.max(1, Math.min(limit, 12));

    // Short read-only DB query: release MariaDB connection before any network I/O
    Optional<Product> anchorOpt =
        inReadTx(
            () ->
                products
                    .findById(productId)
                    .filter(p -> Integer.valueOf(1).equals(p.getStatus()))
                    .map(
                        p -> {
                          if (p.getCategory() != null) {
                            p.getCategory().getId();
                          }
                          return p;
                        }));
    if (anchorOpt.isEmpty()) {
      return Optional.empty();
    }
    Product anchor = anchorOpt.get();

    List<Long> candidateIds = List.of();
    if (vectorStore.isAvailable()) {
      Optional<float[]> queryVecOpt = resolveOrIndexEmbedding(anchor, true);
      int candidateTopK = Math.max(safeLimit * 4, MIN_MILVUS_CANDIDATES);
      candidateIds =
          queryVecOpt.map(vec -> vectorStore.searchSimilar(vec, candidateTopK)).orElse(List.of());
    }

    final List<Long> milvusIds = candidateIds;
    RecommendationResponseDTO response =
        inReadTx(() -> assembleRecommendationResponse(anchor, milvusIds, safeLimit));
    return Optional.of(response);
  }

  public ReindexSummary reindexAll() {
    if (!reindexInProgress.compareAndSet(false, true)) {
      log.info("Vector reindex already in progress, skipping duplicate trigger");
      return new ReindexSummary(0, 0, 0, vectorStore.isAvailable());
    }
    try {
      List<Product> activeProducts = inReadTx(() -> products.findByStatus(1));
      Set<Long> activeIds = new HashSet<>(activeProducts.size());
      for (Product p : activeProducts) {
        if (p.getId() != null) {
          activeIds.add(p.getId());
        }
      }

      int indexed = 0;
      int failed = 0;
      boolean milvusReady = vectorStore.isAvailable();
      boolean milvusConfigured = milvusReady || vectorStore.isConfigured();

      for (Product product : activeProducts) {
        try {
          Optional<float[]> vecOpt = resolveOrIndexEmbedding(product, false);
          if (vecOpt.isPresent() && vecOpt.get().length == embeddingService.getDimension()) {
            if (!milvusConfigured) {
              indexed++;
            } else if (milvusReady && vectorStore.upsertEmbedding(product.getId(), vecOpt.get())) {
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

      pruneStaleEmbeddings(activeIds, milvusReady);

      log.info(
          "Vector reindex completed: total={}, indexed={}, failed={}, milvusAvailable={}",
          activeProducts.size(),
          indexed,
          failed,
          milvusReady);
      return new ReindexSummary(activeProducts.size(), indexed, failed, milvusReady);
    } finally {
      reindexInProgress.set(false);
    }
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void onCatalogChanged(CatalogService.ProductCatalogChangedEvent event) {
    if (event == null || event.productId() == null) {
      return;
    }
    if (descriptionService != null) {
      descriptionService.clearCache(event.productId());
    }
    if (event.deleted()) {
      deleteProductEmbedding(event.productId());
      return;
    }
    Optional<Product> activeOpt =
        inReadTx(
            () ->
                products
                    .findById(event.productId())
                    .filter(p -> Integer.valueOf(1).equals(p.getStatus())));
    if (activeOpt.isEmpty()) {
      deleteProductEmbedding(event.productId());
    } else {
      resolveOrIndexEmbedding(activeOpt.get(), true);
    }
  }

  public void deleteProductEmbedding(Long productId) {
    if (productId == null) {
      return;
    }
    try {
      embeddingRepo.deleteById(String.valueOf(productId));
    } catch (Exception ex) {
      log.debug("Could not delete embedding {} from MongoDB: {}", productId, ex.getMessage());
    }
    vectorStore.deleteEmbedding(productId);
  }

  private RecommendationResponseDTO assembleRecommendationResponse(
      Product anchor, List<Long> candidateIds, int safeLimit) {
    List<Product> selected = new ArrayList<>(safeLimit);
    Set<Long> seenIds = new HashSet<>();
    seenIds.add(anchor.getId());

    boolean usedMilvus = false;
    Map<Long, Product> milvusById = new LinkedHashMap<>();
    if (!candidateIds.isEmpty()) {
      for (Product p : products.findAllById(candidateIds)) {
        if (Integer.valueOf(1).equals(p.getStatus())) {
          milvusById.put(p.getId(), p);
        }
      }
      // 1. Prefer same-kind Milvus candidates first (phones for phone detail, accessories for accessories)
      for (Long cid : candidateIds) {
        if (selected.size() >= safeLimit) {
          break;
        }
        if (seenIds.contains(cid)) {
          continue;
        }
        Product candidate = milvusById.get(cid);
        if (candidate != null && Objects.equals(anchor.getKind(), candidate.getKind())) {
          selected.add(candidate);
          seenIds.add(cid);
          usedMilvus = true;
        }
      }
    }

    // 2. Fill remaining slots with same-kind MariaDB products BEFORE allowing cross-kind hits
    List<Product> activePool = List.of();
    if (selected.size() < safeLimit) {
      activePool = products.findByStatus(1);
      appendFallbackProducts(anchor, activePool, selected, seenIds, safeLimit, true);
    }

    // 3. Only borrow cross-kind items when same-kind catalog supply is exhausted
    if (selected.size() < safeLimit && !milvusById.isEmpty()) {
      for (Long cid : candidateIds) {
        if (selected.size() >= safeLimit) {
          break;
        }
        if (seenIds.contains(cid)) {
          continue;
        }
        Product candidate = milvusById.get(cid);
        if (candidate != null) {
          selected.add(candidate);
          seenIds.add(cid);
          usedMilvus = true;
        }
      }
    }
    if (selected.size() < safeLimit) {
      appendFallbackProducts(anchor, activePool, selected, seenIds, safeLimit, false);
    }

    List<RecommendedProductDTO> dtos = selected.stream().map(RecommendedProductDTO::from).toList();
    String source = usedMilvus ? SOURCE_MILVUS : SOURCE_FALLBACK;
    return new RecommendationResponseDTO(anchor.getId(), source, dtos);
  }

  private void pruneStaleEmbeddings(Set<Long> activeIds, boolean milvusReady) {
    try {
      List<ProductEmbeddingDocument> cachedDocs = embeddingRepo.findAll();
      if (cachedDocs == null || cachedDocs.isEmpty()) {
        return;
      }
      List<String> staleDocIds = new ArrayList<>();
      List<Long> staleMilvusIds = new ArrayList<>();
      for (ProductEmbeddingDocument doc : cachedDocs) {
        if (doc == null || doc.getProductId() == null) {
          continue;
        }
        try {
          Long pid = Long.parseLong(doc.getProductId());
          if (!activeIds.contains(pid)) {
            staleDocIds.add(doc.getProductId());
            staleMilvusIds.add(pid);
          }
        } catch (NumberFormatException ignored) {
          staleDocIds.add(doc.getProductId());
        }
      }
      if (!staleDocIds.isEmpty()) {
        embeddingRepo.deleteAllById(staleDocIds);
        if (milvusReady && !staleMilvusIds.isEmpty()) {
          vectorStore.deleteEmbeddings(staleMilvusIds);
        }
      }
    } catch (Exception ex) {
      log.debug("Stale embedding pruning skipped: {}", ex.getMessage());
    }
  }

  private <T> T inReadTx(Supplier<T> supplier) {
    if (readTxTemplate == null) {
      return supplier.get();
    }
    return readTxTemplate.execute(status -> supplier.get());
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
      Product anchor,
      List<Product> activePool,
      List<Product> selected,
      Set<Long> seenIds,
      int limit,
      boolean sameKindOnly) {
    Long anchorCatId = anchor.getCategory() != null ? anchor.getCategory().getId() : null;
    BigDecimal anchorPrice = anchor.getPrice() != null ? anchor.getPrice() : BigDecimal.ZERO;

    List<Product> pool =
        activePool.stream()
            .filter(p -> !seenIds.contains(p.getId()))
            .filter(p -> !sameKindOnly || Objects.equals(anchor.getKind(), p.getKind()))
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
