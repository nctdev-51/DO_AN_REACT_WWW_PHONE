package iuh.fit.www_phone_ver1.dto.catalog;

import java.util.List;

/** JSON response wrapper for product similarity recommendations. */
public record RecommendationResponseDTO(
    Long productId, String source, List<RecommendedProductDTO> recommendations) {}
