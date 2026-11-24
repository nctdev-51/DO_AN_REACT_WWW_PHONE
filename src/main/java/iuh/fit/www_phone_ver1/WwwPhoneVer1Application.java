package iuh.fit.www_phone_ver1;

import iuh.fit.www_phone_ver1.config.ProductionConfigurationValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class WwwPhoneVer1Application extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(WwwPhoneVer1Application.class);
    application.addInitializers(new ProductionConfigurationValidator());
    application.run(args);
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(WwwPhoneVer1Application.class);
  }
}
