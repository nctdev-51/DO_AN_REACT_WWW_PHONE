package iuh.fit.www_phone_ver1.repository.mongo;

import iuh.fit.www_phone_ver1.entity.mongo.ProductEmbeddingDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductEmbeddingRepository
    extends MongoRepository<ProductEmbeddingDocument, String> {}
