package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
public class Address {
  @Column(length = 10)
  private String provinceCode;

  @Column(length = 10)
  private String wardCode;

  @Column(length = 100)
  private String province;

  @Column(length = 100)
  private String ward;

  public String getFullLine() {
    return java.util.stream.Stream.of(line, ward, province)
        .filter(v -> v != null && !v.isBlank())
        .collect(java.util.stream.Collectors.joining(", "));
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 100)
  private String recipientName;

  @Column(nullable = false, length = 20)
  private String phone;

  @Column(nullable = false, length = 255)
  private String line;

  @Column(nullable = false)
  private boolean defaultAddress;
}
