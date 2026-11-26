package iuh.fit.www_phone_ver1.entity.mongo;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("user_event_daily_stats")
public record UserEventDailyStats(
    @Id String day,
    Instant calculatedAt,
    int rulesVersion,
    Interaction totals,
    List<ProductStats> products,
    List<KeywordStats> keywords) {

  public record Interaction(long views, long clicks, long cartAdds) {
    public static final Interaction ZERO = new Interaction(0, 0, 0);

    public Interaction plus(Interaction other) {
      return new Interaction(views + other.views, clicks + other.clicks, cartAdds + other.cartAdds);
    }
  }

  public record ProductStats(Long productId, Long categoryId, Interaction interaction) {}

  public record KeywordStats(String keyword, long count) {}
}
