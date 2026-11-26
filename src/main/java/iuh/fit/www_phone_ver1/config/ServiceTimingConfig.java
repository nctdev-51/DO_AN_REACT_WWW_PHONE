package iuh.fit.www_phone_ver1.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("dev")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ServiceTimingConfig {
  @Bean
  TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }
}
