package iuh.fit.www_phone_ver1.service.user;

import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.entity.AccountToken.Purpose;
import iuh.fit.www_phone_ver1.repository.*;
import iuh.fit.www_phone_ver1.service.mail.MailService;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;

@Service
public class AccountTokenService {
  private final UserRepository users;
  private final AccountTokenRepository tokens;
  private final PasswordEncoder encoder;
  private final MailService mail;
  private final SessionRegistry sessions;
  private final String baseUrl;
  private final SecureRandom random = new SecureRandom();

  public AccountTokenService(
      UserRepository users,
      AccountTokenRepository tokens,
      PasswordEncoder encoder,
      MailService mail,
      SessionRegistry sessions,
      @Value("${app.public-base-url:http://localhost:8080}") String baseUrl) {
    this.users = users;
    this.tokens = tokens;
    this.encoder = encoder;
    this.mail = mail;
    this.sessions = sessions;
    URI uri = URI.create(baseUrl);
    if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
        || uri.getHost() == null
        || uri.getRawQuery() != null
        || uri.getRawFragment() != null
        || uri.getUserInfo() != null)
      throw new IllegalArgumentException("app.public-base-url không hợp lệ");
    this.baseUrl = baseUrl.replaceAll("/+$", "");
  }

  @Transactional
  public void request(String email, Purpose purpose) {
    if (email == null || email.length() > 100) return;
    users
        .lockByEmail(email.trim().toLowerCase(Locale.ROOT))
        .ifPresent(
            user -> {
              if (user.getStatus() != 1
                  || (purpose == Purpose.VERIFY_EMAIL && user.isEmailVerified())) return;
              var latest =
                  tokens.findFirstByUser_IdAndPurposeOrderByCreatedAtDesc(user.getId(), purpose);
              if (latest.isPresent()
                  && latest.get().getCreatedAt().plusSeconds(60).isAfter(Instant.now())) return;
              issue(user, purpose);
            });
  }

  @Transactional
  public void sendVerification(User user) {
    issue(user, Purpose.VERIFY_EMAIL);
  }

  private void issue(User user, Purpose purpose) {
    Instant now = Instant.now();
    invalidate(user.getId(), purpose, now);
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    AccountToken token = new AccountToken();
    token.setUser(user);
    token.setDigest(hash(raw));
    token.setPurpose(purpose);
    token.setCreatedAt(now);
    token.setExpiresAt(
        now.plus(
            purpose == Purpose.RESET_PASSWORD ? Duration.ofMinutes(15) : Duration.ofHours(24)));
    tokens.save(token);
    String link =
        baseUrl
            + (purpose == Purpose.RESET_PASSWORD ? "/reset-password" : "/verify-email")
            + "?token="
            + raw;
    String email = user.getEmail();
    afterCommit(() -> mail.sendAccountLink(email, link, purpose == Purpose.RESET_PASSWORD));
  }

  @Transactional(readOnly = true)
  public boolean isValid(String raw, Purpose purpose) {
    return lookup(raw).filter(t -> valid(t, purpose)).isPresent();
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public void verify(String raw) {
    AccountToken token = lockAndValidate(raw, Purpose.VERIFY_EMAIL);
    token.getUser().setEmailVerified(true);
    invalidate(token.getUser().getId(), Purpose.VERIFY_EMAIL, Instant.now());
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public void resetPassword(String raw, String password, String confirmation) {
    if (password == null
        || password.length() < 8
        || password.getBytes(StandardCharsets.UTF_8).length > 72)
      throw new IllegalArgumentException("Mật khẩu phải từ 8 ký tự và tối đa 72 byte UTF-8.");
    if (!password.equals(confirmation))
      throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
    AccountToken token = lockAndValidate(raw, Purpose.RESET_PASSWORD);
    token.getUser().setPassword(encoder.encode(password));
    invalidate(token.getUser().getId(), Purpose.RESET_PASSWORD, Instant.now());
    String email = token.getUser().getEmail();
    afterCommit(
        () -> {
          sessions.getAllPrincipals().stream()
              .filter(p -> p instanceof UserDetails details && email.equals(details.getUsername()))
              .forEach(p -> sessions.getAllSessions(p, false).forEach(s -> s.expireNow()));
          mail.sendPasswordChanged(email);
        });
  }

  // All issue/consume operations lock the user first, serializing competing links for the same
  // account.
  private AccountToken lockAndValidate(String raw, Purpose purpose) {
    AccountToken existing = lookup(raw).orElseThrow(this::invalid);
    users.lockByEmail(existing.getUser().getEmail()).orElseThrow(this::invalid);
    // Refresh after obtaining the lock; a concurrent transaction may have consumed this token.
    entityManager.refresh(existing, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    entityManager.refresh(existing.getUser(), jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    if (!valid(existing, purpose)) throw invalid();
    return existing;
  }

  @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;

  private boolean valid(AccountToken token, Purpose purpose) {
    return token.getPurpose() == purpose
        && token.getConsumedAt() == null
        && token.getExpiresAt().isAfter(Instant.now())
        && token.getUser().getStatus() == 1;
  }

  private Optional<AccountToken> lookup(String raw) {
    if (raw == null || !raw.matches("[A-Za-z0-9_-]{43}")) return Optional.empty();
    return tokens.findByDigest(hash(raw));
  }

  private void invalidate(Long userId, Purpose purpose, Instant now) {
    tokens
        .findByUser_IdAndPurposeAndConsumedAtIsNull(userId, purpose)
        .forEach(t -> t.setConsumedAt(now));
  }

  private IllegalArgumentException invalid() {
    return new IllegalArgumentException(
        "Liên kết không hợp lệ, đã được sử dụng hoặc đã hết hạn. Vui lòng yêu cầu liên kết mới.");
  }

  private String hash(String raw) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.US_ASCII)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private void afterCommit(Runnable task) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            task.run();
          }
        });
  }
}
