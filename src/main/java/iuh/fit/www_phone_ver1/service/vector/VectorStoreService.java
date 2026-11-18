package iuh.fit.www_phone_ver1.service.vector;

import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.dml.UpsertParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import iuh.fit.www_phone_ver1.service.ai.EmbeddingService;
import jakarta.annotation.PostConstruct;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Manages the Milvus product_embeddings collection with graceful degradation and a failure cooldown
 * circuit breaker so the storefront never stalls when Milvus is offline.
 */
@Service
public class VectorStoreService {

  private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);
  private static final String FIELD_ID = "product_id";
  private static final String FIELD_EMBEDDING = "embedding";
  private static final String INDEX_NAME = "idx_product_embedding";

  private final MilvusServiceClient milvus;
  private final String collectionName;
  private final int dimension;
  private final long retryCooldownMs;
  private final Clock clock;
  private final AtomicLong lastFailureAt = new AtomicLong(0);
  private volatile boolean collectionReady = false;

  @Autowired
  public VectorStoreService(
      ObjectProvider<MilvusServiceClient> milvusProvider,
      @Value("${app.milvus.collection:product_embeddings}") String collectionName,
      @Value("${app.milvus.dimension:1536}") int dimension,
      @Value("${app.milvus.retry-cooldown-ms:30000}") long retryCooldownMs) {
    this(milvusProvider.getIfAvailable(), collectionName, dimension, retryCooldownMs, Clock.systemUTC());
  }

  public VectorStoreService(
      ObjectProvider<MilvusServiceClient> milvusProvider,
      String collectionName,
      int dimension) {
    this(milvusProvider.getIfAvailable(), collectionName, dimension, 30_000L, Clock.systemUTC());
  }

  VectorStoreService(
      MilvusServiceClient milvus,
      String collectionName,
      int dimension,
      long retryCooldownMs,
      Clock clock) {
    this.milvus = milvus;
    this.collectionName = collectionName;
    this.dimension = dimension;
    this.retryCooldownMs = Math.max(0, retryCooldownMs);
    this.clock = clock;
  }

  @PostConstruct
  public void ensureCollection() {
    if (milvus == null || collectionReady || isInCooldown()) {
      return;
    }
    synchronized (this) {
      if (collectionReady || isInCooldown()) {
        return;
      }
      try {
        R<Boolean> hasRes =
            milvus.hasCollection(
                HasCollectionParam.newBuilder().withCollectionName(collectionName).build());
        if (hasRes == null || hasRes.getStatus() != R.Status.Success.getCode()) {
          markUnavailable("hasCollection failed: " + (hasRes == null ? "null response" : hasRes.getMessage()));
          return;
        }
        if (Boolean.FALSE.equals(hasRes.getData())) {
          createCollectionAndIndex();
        }
        R<RpcStatus> loadRes =
            milvus.loadCollection(
                LoadCollectionParam.newBuilder().withCollectionName(collectionName).build());
        if (loadRes == null || loadRes.getStatus() != R.Status.Success.getCode()) {
          markUnavailable("loadCollection failed: " + (loadRes == null ? "null response" : loadRes.getMessage()));
          return;
        }
        collectionReady = true;
        lastFailureAt.set(0);
        log.info("Milvus collection '{}' ready (dim={})", collectionName, dimension);
      } catch (Exception ex) {
        markUnavailable("initialization failed: " + ex.getMessage());
      }
    }
  }

  private void createCollectionAndIndex() {
    FieldType idField =
        FieldType.newBuilder()
            .withName(FIELD_ID)
            .withDataType(DataType.Int64)
            .withPrimaryKey(true)
            .withAutoID(false)
            .build();
    FieldType vectorField =
        FieldType.newBuilder()
            .withName(FIELD_EMBEDDING)
            .withDataType(DataType.FloatVector)
            .withDimension(dimension)
            .build();

    R<RpcStatus> createRes =
        milvus.createCollection(
            CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("PTV Mobile product embedding vectors")
                .addFieldType(idField)
                .addFieldType(vectorField)
                .build());
    if (createRes.getStatus() != R.Status.Success.getCode()) {
      throw new IllegalStateException("createCollection failed: " + createRes.getMessage());
    }

    R<RpcStatus> indexRes =
        milvus.createIndex(
            CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName(FIELD_EMBEDDING)
                .withIndexName(INDEX_NAME)
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.IP)
                .withExtraParam("{\"nlist\":64}")
                .withSyncMode(Boolean.TRUE)
                .build());
    if (indexRes.getStatus() != R.Status.Success.getCode()) {
      throw new IllegalStateException("createIndex failed: " + indexRes.getMessage());
    }
  }

  public boolean isConfigured() {
    return milvus != null;
  }

  public boolean isAvailable() {
    if (milvus == null || isInCooldown()) {
      return false;
    }
    if (!collectionReady) {
      ensureCollection();
    }
    return collectionReady;
  }

  boolean isInCooldown() {
    long failedAt = lastFailureAt.get();
    return failedAt > 0 && retryCooldownMs > 0 && (clock.millis() - failedAt) < retryCooldownMs;
  }

  private void markUnavailable(String reason) {
    collectionReady = false;
    lastFailureAt.set(clock.millis());
    log.warn("Milvus unavailable ({}), cooldown {} ms", reason, retryCooldownMs);
  }

  public boolean upsertEmbedding(Long productId, float[] vector) {
    if (productId == null || vector == null || vector.length != dimension || !isAvailable()) {
      return false;
    }
    try {
      List<InsertParam.Field> fields =
          List.of(
              new InsertParam.Field(FIELD_ID, List.of(productId)),
              new InsertParam.Field(FIELD_EMBEDDING, List.of(EmbeddingService.toFloatList(vector))));

      R<MutationResult> res =
          milvus.upsert(
              UpsertParam.newBuilder()
                  .withCollectionName(collectionName)
                  .withFields(fields)
                  .build());
      if (res == null || res.getStatus() != R.Status.Success.getCode()) {
        markUnavailable(
            "upsert failed for product "
                + productId
                + ": "
                + (res == null ? "null response" : res.getMessage()));
        return false;
      }
      return true;
    } catch (Exception ex) {
      markUnavailable("upsert error for product " + productId + ": " + ex.getMessage());
      return false;
    }
  }

  public List<Long> searchSimilar(float[] queryVec, int topK) {
    if (queryVec == null || queryVec.length != dimension || topK <= 0 || !isAvailable()) {
      return List.of();
    }
    try {
      R<SearchResults> res =
          milvus.search(
              SearchParam.newBuilder()
                  .withCollectionName(collectionName)
                  .withMetricType(MetricType.IP)
                  .withVectorFieldName(FIELD_EMBEDDING)
                  .withTopK(topK)
                  .withVectors(List.of(EmbeddingService.toFloatList(queryVec)))
                  .withParams("{\"nprobe\":16}")
                  .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
                  .build());
      if (res == null || res.getStatus() != R.Status.Success.getCode() || res.getData() == null) {
        markUnavailable("search failed: " + (res == null ? "null response" : res.getMessage()));
        return List.of();
      }
      SearchResultsWrapper wrapper = new SearchResultsWrapper(res.getData().getResults());
      List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0);
      List<Long> ids = new ArrayList<>(scores.size());
      for (SearchResultsWrapper.IDScore score : scores) {
        ids.add(score.getLongID());
      }
      return ids;
    } catch (Exception ex) {
      markUnavailable("search error: " + ex.getMessage());
      return List.of();
    }
  }

  public boolean deleteEmbedding(Long productId) {
    return productId != null && deleteEmbeddings(List.of(productId));
  }

  public boolean deleteEmbeddings(Collection<Long> productIds) {
    if (productIds == null || productIds.isEmpty()) {
      return true;
    }
    List<Long> validIds = productIds.stream().filter(Objects::nonNull).distinct().toList();
    if (validIds.isEmpty()) {
      return true;
    }
    if (!isAvailable()) {
      return false;
    }
    String idListExpr =
        validIds.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"));
    try {
      R<MutationResult> res =
          milvus.delete(
              DeleteParam.newBuilder()
                  .withCollectionName(collectionName)
                  .withExpr(FIELD_ID + " in " + idListExpr)
                  .build());
      if (res == null || res.getStatus() != R.Status.Success.getCode()) {
        markUnavailable(
            "delete failed for "
                + validIds.size()
                + " ids: "
                + (res == null ? "null response" : res.getMessage()));
        return false;
      }
      return true;
    } catch (Exception ex) {
      markUnavailable("delete error: " + ex.getMessage());
      return false;
    }
  }
}
