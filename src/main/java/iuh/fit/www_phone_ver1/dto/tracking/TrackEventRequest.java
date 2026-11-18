package iuh.fit.www_phone_ver1.dto.tracking;

import java.util.Map;

public record TrackEventRequest(
    String eventType,
    Long productId,
    String keyword,
    String pageUrl,
    Map<String, Object> meta) {}
