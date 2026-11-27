package iuh.fit.www_phone_ver1.config;

import iuh.fit.www_phone_ver1.service.catalog.ProductImageStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.concurrent.TimeUnit;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {
  private final ProductImageStorageService imageStorageService;

  public UploadResourceConfig(ProductImageStorageService imageStorageService) {
    this.imageStorageService = imageStorageService;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String location = imageStorageService.getUploadDirectory().toUri().toString();
    if (!location.endsWith("/")) {
      location += "/";
    }
    registry
        .addResourceHandler("/uploads/products/**")
        .addResourceLocations(location)
        .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
  }
}
