package iuh.fit.www_phone_ver1.security;

import iuh.fit.www_phone_ver1.entity.User;
import iuh.fit.www_phone_ver1.repository.UserRepository;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final boolean emailVerificationEnabled;

  public CustomUserDetailsService(UserRepository userRepository) {
    this(userRepository, true);
  }

  @Autowired
  public CustomUserDetailsService(
    UserRepository userRepository,
    @Value("${app.account.email-verification-enabled:true}")
    boolean emailVerificationEnabled) {
    this.userRepository = userRepository;
    this.emailVerificationEnabled = emailVerificationEnabled;
  }

  @Override
  public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
    String trimmed = identifier.trim();
    User user =
      userRepository
        .findByEmail(trimmed.toLowerCase(Locale.ROOT))
        .or(() -> userRepository.findByPhone(trimmed))
        .orElseThrow(
          () ->
            new UsernameNotFoundException("Không tìm thấy tài khoản với: " + identifier));

    if (user.getStatus() != 1) {
      throw new UsernameNotFoundException("Tài khoản đã bị khóa bởi quản trị viên");
    }

    Collection<GrantedAuthority> authorities =
      user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
        .collect(Collectors.toList());

    return new org.springframework.security.core.userdetails.User(
      user.getEmail(),
      user.getPassword(),
      !emailVerificationEnabled || user.isEmailVerified(),
      true,
      true,
      true,
      authorities);
  }
}
