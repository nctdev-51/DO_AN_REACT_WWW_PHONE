package iuh.fit.www_phone_ver1.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

  public static final String CACHE_BRANDS = "brands";
  public static final String CACHE_COLORS = "colors";
  public static final String CACHE_STORAGE = "storage";
  public static final String CACHE_RAM = "ram";
  public static final String CACHE_CATEGORIES = "categories";
  public static final String CACHE_STORE_POLICY = "storePolicy";
  public static final String CACHE_PROMOTIONS = "promotions";

  @Override
  @Primary
  @Bean(name = "cacheManager")
  public CacheManager cacheManager() {
    SimpleCacheManager manager = new SimpleCacheManager();
    manager.setCaches(
        List.of(
            // Dữ liệu danh mục & thuộc tính phần cứng ít thay đổi -> TTL 30 phút
            buildCache(CACHE_BRANDS, 200, Duration.ofMinutes(30)),
            buildCache(CACHE_COLORS, 200, Duration.ofMinutes(30)),
            buildCache(CACHE_STORAGE, 200, Duration.ofMinutes(30)),
            buildCache(CACHE_RAM, 200, Duration.ofMinutes(30)),
            buildCache(CACHE_CATEGORIES, 200, Duration.ofMinutes(30)),
            // Chính sách cửa hàng -> TTL 60 phút
            buildCache(CACHE_STORE_POLICY, 50, Duration.ofMinutes(60)),
            // Khuyến mãi thay đổi thường xuyên hơn -> TTL 5 phút
            buildCache(CACHE_PROMOTIONS, 300, Duration.ofMinutes(5))));
    return manager;
  }

  private CaffeineCache buildCache(String name, int maxSize, Duration ttl) {
    return new CaffeineCache(
        name,
        Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(ttl)
            .recordStats()
            .build());
  }
}
