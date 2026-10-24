package iuh.fit.www_phone_ver1.config;

import iuh.fit.www_phone_ver1.entity.Role;
import iuh.fit.www_phone_ver1.entity.User;
import iuh.fit.www_phone_ver1.repository.RoleRepository;
import iuh.fit.www_phone_ver1.repository.UserRepository;
import iuh.fit.www_phone_ver1.security.CustomUserDetailsService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final CustomUserDetailsService userDetailsService;

  public SecurityConfig(CustomUserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public org.springframework.security.core.session.SessionRegistry sessionRegistry() {
    return new org.springframework.security.core.session.SessionRegistryImpl();
  }

  @Bean
  public org.springframework.security.web.session.HttpSessionEventPublisher
      sessionEventPublisher() {
    return new org.springframework.security.web.session.HttpSessionEventPublisher();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationSuccessHandler customSuccessHandler() {
    var customerSuccess =
        new org.springframework.security.web.authentication
            .SavedRequestAwareAuthenticationSuccessHandler();
    customerSuccess.setDefaultTargetUrl("/");
    return (request, response, authentication) -> {
      boolean isAdmin =
          authentication.getAuthorities().stream()
              .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
      if (isAdmin) {
        response.sendRedirect(request.getContextPath() + "/admin");
      } else {
        customerSuccess.onAuthenticationSuccess(request, response, authentication);
      }
    };
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, AuthenticationSuccessHandler customSuccessHandler) throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        "/checkout/**", "/orders/**", "/account/**", "/payments/vnpay/return")
                    .authenticated()
                    .requestMatchers(
                        org.springframework.http.HttpMethod.POST, "/products/*/reviews")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .formLogin(
            form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .successHandler(customSuccessHandler)
                    .failureUrl("/login?error")
                    .permitAll())
        .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll())
        .sessionManagement(
            sessions ->
                sessions
                    .maximumSessions(-1)
                    .sessionRegistry(sessionRegistry())
                    .expiredUrl("/login?expired"))
        .authenticationProvider(authenticationProvider());
    return http.build();
  }

  @Bean
  public CommandLineRunner initAdminUser(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      @Value("${app.admin.username}") String adminEmail,
      @Value("${app.admin.password}") String adminPassword) {
    return args -> {
      Optional<Role> adminRoleOpt = roleRepository.findByName("ADMIN");
      Role adminRole =
          adminRoleOpt.orElseGet(
              () -> {
                Role newRole = new Role();
                newRole.setName("ADMIN");
                return roleRepository.save(newRole);
              });

      if (userRepository.findByEmail(adminEmail).isEmpty()) {
        User admin = new User();
        admin.setFullName("Administrator");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setStatus(1);
        admin.getRoles().add(adminRole);
        userRepository.save(admin);
      }
    };
  }
}
