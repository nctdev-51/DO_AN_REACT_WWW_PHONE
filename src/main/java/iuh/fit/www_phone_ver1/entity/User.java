package iuh.fit.www_phone_ver1.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
  public boolean isAdmin() {
    return roles.stream().anyMatch(role -> "ADMIN".equals(role.getName()));
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "full_name", nullable = false, length = 100)
  private String fullName;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false, columnDefinition = "boolean default true")
  @Builder.Default
  private boolean emailVerified = true;

  @Column(length = 20)
  private String phone;

  @Column(nullable = false)
  @ToString.Exclude
  private String password;

  @Column(length = 255)
  private String address;

  @Column(nullable = false)
  @Builder.Default
  private Integer status = 1; // 1: Hoạt động, 0: Đã khóa

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  private List<Order> orders = new ArrayList<>();
}
