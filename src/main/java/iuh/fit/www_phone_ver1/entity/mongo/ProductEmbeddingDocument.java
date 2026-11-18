package iuh.fit.www_phone_ver1.entity.mongo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Cached embedding vector for a catalog product stored in MongoDB. */
@Document(collection = "product_embeddings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductEmbeddingDocument {

  @Id private String productId;

  private List<Float> vector;

  private String contentHash;

  private String provider;

  private LocalDateTime updatedAt;
}
