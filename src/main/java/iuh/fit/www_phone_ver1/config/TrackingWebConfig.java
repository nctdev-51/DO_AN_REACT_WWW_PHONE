package iuh.fit.www_phone_ver1.config;

import iuh.fit.www_phone_ver1.interceptor.TrackingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TrackingWebConfig implements WebMvcConfigurer {
  private final TrackingInterceptor trackingInterceptor;

  public TrackingWebConfig(TrackingInterceptor trackingInterceptor) {
    this.trackingInterceptor = trackingInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(trackingInterceptor).addPathPatterns("/products/**", "/store/**");
  }
}
