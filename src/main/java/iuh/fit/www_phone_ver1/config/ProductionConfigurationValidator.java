package iuh.fit.www_phone_ver1.config;

import java.util.List;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Profiles;

/** Fails fast before database or web-server initialization when production secrets are absent. */
public final class ProductionConfigurationValidator
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final List<String> REQUIRED_ENVIRONMENT_VARIABLES =
      List.of("DB_URL", "DB_USERNAME", "DB_PASSWORD", "ADMIN_USERNAME", "ADMIN_PASSWORD", "PUBLIC_BASE_URL");

  @Override
  public void initialize(ConfigurableApplicationContext context) {
    if (!context.getEnvironment().acceptsProfiles(Profiles.of("prod"))) return;

    var missing =
        REQUIRED_ENVIRONMENT_VARIABLES.stream()
            .filter(name -> isBlank(System.getenv(name)))
            .toList();
    if (!missing.isEmpty())
      throw new IllegalStateException(
          "Production startup blocked. Set required environment variables: "
              + String.join(", ", missing));
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
