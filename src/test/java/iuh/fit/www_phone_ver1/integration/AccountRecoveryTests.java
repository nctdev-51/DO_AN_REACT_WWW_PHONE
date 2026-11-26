package iuh.fit.www_phone_ver1.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import iuh.fit.www_phone_ver1.dto.user.UserRegistrationDto;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.entity.AccountToken.Purpose;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.mail.MailService;
import iuh.fit.www_phone_ver1.service.user.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:authflows;MODE=MariaDB;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
      "app.payments.expiry-enabled=false",
      "app.catalog.seed-enabled=false"
    })
@ActiveProfiles("demo")
class AccountRecoveryTests {
  @Autowired AccountTokenService service;
  @Autowired UserService registration;
  @Autowired UserRepository users;
  @Autowired RoleRepository roles;
  @Autowired AccountTokenRepository tokens;
  @Autowired PasswordEncoder encoder;
  @Autowired WebApplicationContext context;
  @Autowired PlatformTransactionManager transactions;
  @MockitoBean MailService mail;
  User user;
  MockMvc mvc;

  @BeforeEach
  void setup() {
    var role =
        roles.findByName("USER").orElseGet(() -> roles.save(Role.builder().name("USER").build()));
    user =
        users.saveAndFlush(
            User.builder()
                .fullName("Recovery Test")
                .email(UUID.randomUUID() + "@example.com")
                .password(encoder.encode("OldPass123!"))
                .roles(Set.of(role))
                .build());
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @AfterEach
  void cleanup() {
    new TransactionTemplate(transactions)
        .executeWithoutResult(
            s -> {
              tokens.deleteByUser_Id(user.getId());
              users.deleteById(user.getId());
            });
  }

  String link(Purpose purpose) {
    service.request(user.getEmail(), purpose);
    var capture = ArgumentCaptor.forClass(String.class);
    verify(mail)
        .sendAccountLink(
            eq(user.getEmail()), capture.capture(), eq(purpose == Purpose.RESET_PASSWORD));
    return capture.getValue().split("token=")[1];
  }

  @Test
  void registrationRequiresVerificationAndOnlyPostConsumesToken() throws Exception {
    // Use a separate new user so the existing test fixture stays available for cleanup.
    var dto = new UserRegistrationDto();
    dto.setFullName("New Customer");
    dto.setEmail("new-" + user.getEmail());
    dto.setPassword("NewPass123!");
    dto.setConfirmPassword("NewPass123!");
    registration.registerUser(dto);
    User created = users.findByEmail(dto.getEmail()).orElseThrow();
    try {
      assertFalse(created.isEmailVerified());
      var capture = ArgumentCaptor.forClass(String.class);
      verify(mail).sendAccountLink(eq(created.getEmail()), capture.capture(), eq(false));
      String raw = capture.getValue().split("token=")[1];
      mvc.perform(
              post("/login")
                  .with(csrf())
                  .param("username", created.getEmail())
                  .param("password", "NewPass123!"))
          .andExpect(redirectedUrl("/login?error"));
      mvc.perform(get("/verify-email").param("token", raw))
          .andExpect(status().isOk())
          .andExpect(header().string("Referrer-Policy", "no-referrer"));
      assertFalse(users.findById(created.getId()).orElseThrow().isEmailVerified());
      mvc.perform(post("/verify-email").param("token", raw)).andExpect(status().isForbidden());
      mvc.perform(post("/verify-email").with(csrf()).param("token", raw))
          .andExpect(redirectedUrl("/login?verified"));
      assertTrue(users.findById(created.getId()).orElseThrow().isEmailVerified());
      assertFalse(service.isValid(raw, Purpose.VERIFY_EMAIL));
      mvc.perform(
              post("/login")
                  .with(csrf())
                  .param("username", created.getEmail())
                  .param("password", "NewPass123!"))
          .andExpect(redirectedUrl("/"));
    } finally {
      new TransactionTemplate(transactions)
          .executeWithoutResult(
              s -> {
                tokens.deleteByUser_Id(created.getId());
                users.deleteById(created.getId());
              });
    }
  }

  @Test
  void resetConsumesTokenAndExpiresActiveSessions() throws Exception {
    var logged =
        mvc.perform(
                post("/login")
                    .with(csrf())
                    .param("username", user.getEmail())
                    .param("password", "OldPass123!"))
            .andExpect(redirectedUrl("/"))
            .andReturn();
    String raw = link(Purpose.RESET_PASSWORD);
    assertTrue(service.isValid(raw, Purpose.RESET_PASSWORD));
    var stored =
        tokens.findAll().stream()
            .filter(t -> t.getUser().getId().equals(user.getId()))
            .findFirst()
            .orElseThrow();
    assertEquals(64, stored.getDigest().length());
    assertNotEquals(raw, stored.getDigest());
    assertThrows(
        IllegalArgumentException.class, () -> service.resetPassword(raw, "NewPass123!", "wrong"));
    assertTrue(service.isValid(raw, Purpose.RESET_PASSWORD));
    service.resetPassword(raw, "NewPass123!", "NewPass123!");
    assertTrue(
        encoder.matches("NewPass123!", users.findById(user.getId()).orElseThrow().getPassword()));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.resetPassword(raw, "OtherPass123!", "OtherPass123!"));
    mvc.perform(
            get("/account")
                .session(
                    (org.springframework.mock.web.MockHttpSession)
                        logged.getRequest().getSession(false)))
        .andExpect(redirectedUrl("/login?expired"));
    verify(mail).sendPasswordChanged(user.getEmail());
  }

  @Test
  void expiredWrongPurposeAndMalformedTokensFail() {
    String raw = link(Purpose.RESET_PASSWORD);
    assertFalse(service.isValid(raw, Purpose.VERIFY_EMAIL));
    assertThrows(IllegalArgumentException.class, () -> service.verify(raw));
    var stored =
        tokens.findAll().stream()
            .filter(t -> t.getUser().getId().equals(user.getId()))
            .findFirst()
            .orElseThrow();
    stored.setExpiresAt(Instant.now().minusSeconds(1));
    tokens.saveAndFlush(stored);
    assertFalse(service.isValid(raw, Purpose.RESET_PASSWORD));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.resetPassword(raw, "NewPass123!", "NewPass123!"));
    assertFalse(service.isValid("<invalid>", Purpose.RESET_PASSWORD));
    assertFalse(service.isValid(null, Purpose.RESET_PASSWORD));
  }

  @Test
  void requestIsGenericAndRateLimited() throws Exception {
    mvc.perform(post("/forgot-password").with(csrf()).param("email", "missing@example.com"))
        .andExpect(redirectedUrl("/forgot-password?sent"));
    verifyNoInteractions(mail);
    link(Purpose.RESET_PASSWORD);
    service.request(user.getEmail(), Purpose.RESET_PASSWORD);
    verify(mail, times(1)).sendAccountLink(eq(user.getEmail()), anyString(), eq(true));
    user.setStatus(0);
    users.saveAndFlush(user);
    service.request(user.getEmail(), Purpose.VERIFY_EMAIL);
    verifyNoMoreInteractions(mail);
  }

  @Test
  void onlyOneConcurrentResetCanConsumeTheLink() throws Exception {
    String raw = link(Purpose.RESET_PASSWORD);
    var start = new java.util.concurrent.CountDownLatch(1);
    var pool = java.util.concurrent.Executors.newFixedThreadPool(2);
    try {
      java.util.concurrent.Callable<Boolean> attempt =
          () -> {
            start.await();
            try {
              service.resetPassword(raw, "Concurrent123!", "Concurrent123!");
              return true;
            } catch (IllegalArgumentException expected) {
              return false;
            }
          };
      var first = pool.submit(attempt);
      var second = pool.submit(attempt);
      start.countDown();
      assertNotEquals(
          first.get(10, java.util.concurrent.TimeUnit.SECONDS),
          second.get(10, java.util.concurrent.TimeUnit.SECONDS));
      verify(mail, times(1)).sendPasswordChanged(user.getEmail());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void resendRevokesOldLinkAfterCooldown() {
    user.setEmailVerified(false);
    users.saveAndFlush(user);
    String first = link(Purpose.VERIFY_EMAIL);
    var stored =
        tokens.findAll().stream()
            .filter(t -> t.getUser().getId().equals(user.getId()))
            .findFirst()
            .orElseThrow();
    stored.setCreatedAt(Instant.now().minusSeconds(61));
    tokens.saveAndFlush(stored);
    clearInvocations(mail);
    String second = link(Purpose.VERIFY_EMAIL);
    assertNotEquals(first, second);
    assertFalse(service.isValid(first, Purpose.VERIFY_EMAIL));
    assertTrue(service.isValid(second, Purpose.VERIFY_EMAIL));
  }
}
