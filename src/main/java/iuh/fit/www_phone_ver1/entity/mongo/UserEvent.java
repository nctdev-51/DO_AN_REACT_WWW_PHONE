package iuh.fit.www_phone_ver1.entity.mongo;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
  @Id
  private String id;
  @Indexed
  private String sessionId;
  private Long userId;
  @Indexed
  private String eventType;
  @Indexed
  private Long productId;
  private String keyword;
  private String pageUrl;
  private Map<String, Object> meta;
  @Indexed
  private LocalDateTime createdAt;
  private String ipHash;
  private String userAgent;
}
