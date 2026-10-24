package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(
    name = "account_tokens",
    indexes = @Index(name = "idx_token_user_purpose", columnList = "user_id,purpose,created_at"))
@Getter
@Setter
@NoArgsConstructor
public class AccountToken {
  public enum Purpose {
    VERIFY_EMAIL,
    RESET_PASSWORD
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, unique = true, length = 64)
  private String digest;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private Purpose purpose;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant expiresAt;

  private Instant consumedAt;
}
