package iuh.fit.www_phone_ver1.service.vector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.CollectionSchema;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.FieldSchema;
import io.milvus.grpc.KeyValuePair;
import io.milvus.grpc.MutationResult;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.dml.UpsertParam;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class VectorStoreServiceTests {

  @Test
  void nullClientGracefullyDegrades() {
    @SuppressWarnings("unchecked")
    ObjectProvider<MilvusServiceClient> emptyProvider = mock(ObjectProvider.class);
    when(emptyProvider.getIfAvailable()).thenReturn(null);

    VectorStoreService service = new VectorStoreService(emptyProvider, "product_embeddings", 1536);

    assertFalse(service.isAvailable());
    assertFalse(service.upsertEmbedding(1L, new float[1536]));
    assertTrue(service.searchSimilar(new float[1536], 6).isEmpty());
  }

  @Test
  void entersCooldownAfterInitFailureAndRecoversWhenCooldownExpires() {
    MilvusServiceClient client = mock(MilvusServiceClient.class);
    AtomicLong nowMs = new AtomicLong(1_000_000L);
    Clock mutableClock =
        new Clock() {
          @Override
          public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
          }

          @Override
          public Clock withZone(java.time.ZoneId zone) {
            return this;
          }

          @Override
          public Instant instant() {
            return Instant.ofEpochMilli(nowMs.get());
          }
        };

    when(client.hasCollection(any())).thenThrow(new RuntimeException("Connection refused"));

    VectorStoreService service =
        new VectorStoreService(client, "product_embeddings", 1536, 30_000L, mutableClock);

    // First call hits Milvus and enters cooldown
    assertFalse(service.isAvailable());
    // Subsequent calls inside cooldown must short-circuit without calling Milvus again
    assertFalse(service.isAvailable());
    assertFalse(service.upsertEmbedding(1L, new float[1536]));
    assertTrue(service.searchSimilar(new float[1536], 5).isEmpty());
    verify(client, times(1)).hasCollection(any());

    // Advance past 30s cooldown and verify Milvus recovery succeeds
    nowMs.addAndGet(31_000L);
    org.mockito.Mockito.reset(client);
    when(client.hasCollection(any())).thenReturn(R.success(Boolean.TRUE));
    when(client.loadCollection(any())).thenReturn(R.success(new RpcStatus(RpcStatus.SUCCESS_MSG)));

    assertTrue(service.isAvailable());
    assertFalse(service.isInCooldown());
    verify(client, times(1)).hasCollection(any());
  }

  @Test
  void searchFailureResetsReadyStateAndActivatesCooldown() {
    MilvusServiceClient client = mock(MilvusServiceClient.class);
    AtomicLong nowMs = new AtomicLong(2_000_000L);
    Clock mutableClock = Clock.fixed(Instant.ofEpochMilli(nowMs.get()), ZoneOffset.UTC);

    when(client.hasCollection(any())).thenReturn(R.success(Boolean.TRUE));
    when(client.loadCollection(any())).thenReturn(R.success(new RpcStatus(RpcStatus.SUCCESS_MSG)));
    when(client.search(any(SearchParam.class)))
        .thenReturn(R.failed(new RuntimeException("Deadline exceeded")));

    VectorStoreService service =
        new VectorStoreService(client, "product_embeddings", 1536, 30_000L, mutableClock);

    assertTrue(service.isAvailable());
    List<Long> hits = service.searchSimilar(new float[1536], 4);
    assertTrue(hits.isEmpty());
    assertTrue(service.isInCooldown());
    assertFalse(service.isAvailable());
  }

  @Test
  void deleteEmbeddingsCallsMilvusDeleteWhenAvailable() {
    MilvusServiceClient client = mock(MilvusServiceClient.class);
    when(client.hasCollection(any())).thenReturn(R.success(Boolean.TRUE));
    when(client.loadCollection(any())).thenReturn(R.success(new RpcStatus(RpcStatus.SUCCESS_MSG)));
    when(client.delete(any(DeleteParam.class)))
        .thenReturn(R.success(MutationResult.newBuilder().build()));

    VectorStoreService service =
        new VectorStoreService(
            client,
            "product_embeddings",
            1536,
            30_000L,
            Clock.fixed(Instant.ofEpochMilli(3_000_000L), ZoneOffset.UTC));

    assertTrue(service.isConfigured());
    assertTrue(service.deleteEmbeddings(List.of(10L, 12L)));
    verify(client, times(1)).delete(any(DeleteParam.class));
  }

  @Test
  void createsCollectionAndIndexWhenMissingAndCooldownsOnUpsertFailure() {
    MilvusServiceClient client = mock(MilvusServiceClient.class);
    when(client.hasCollection(any())).thenReturn(R.success(Boolean.FALSE));
    when(client.createCollection(any(CreateCollectionParam.class)))
        .thenReturn(R.success(new RpcStatus(RpcStatus.SUCCESS_MSG)));
    when(client.createIndex(any())).thenReturn(R.success(new RpcStatus(RpcStatus.SUCCESS_MSG)));
    when(client.loadCollection(any())).thenReturn(R.success(new RpcStatus(RpcStatus.SUCCESS_MSG)));
    when(client.upsert(any(UpsertParam.class)))
        .thenReturn(R.failed(new RuntimeException("Upsert rejected")));

    VectorStoreService service =
        new VectorStoreService(
            client,
            "product_embeddings",
            1536,
            30_000L,
            Clock.fixed(Instant.ofEpochMilli(4_000_000L), ZoneOffset.UTC));

    assertTrue(service.isAvailable());
    verify(client, times(1)).createCollection(any(CreateCollectionParam.class));
    verify(client, times(1)).createIndex(any());

    assertFalse(service.upsertEmbedding(1L, new float[1536]));
    assertTrue(service.isInCooldown());
    assertFalse(service.isAvailable());
  }

  @Test
  void rejectsExistingCollectionWithDimensionMismatch() {
    MilvusServiceClient client = mock(MilvusServiceClient.class);
    when(client.hasCollection(any())).thenReturn(R.success(Boolean.TRUE));
    DescribeCollectionResponse mismatchDesc =
        DescribeCollectionResponse.newBuilder()
            .setSchema(
                CollectionSchema.newBuilder()
                    .addFields(
                        FieldSchema.newBuilder()
                            .setName("embedding")
                            .setDataType(io.milvus.grpc.DataType.FloatVector)
                            .addTypeParams(
                                KeyValuePair.newBuilder()
                                    .setKey("dim")
                                    .setValue("768")
                                    .build())
                            .build())
                    .build())
            .build();
    when(client.describeCollection(any())).thenReturn(R.success(mismatchDesc));

    VectorStoreService service =
        new VectorStoreService(
            client,
            "product_embeddings",
            1536,
            30_000L,
            Clock.fixed(Instant.ofEpochMilli(5_000_000L), ZoneOffset.UTC));

    assertFalse(service.isAvailable());
    assertTrue(service.isInCooldown());
  }
}
