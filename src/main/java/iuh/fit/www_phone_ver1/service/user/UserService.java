package iuh.fit.www_phone_ver1.service.user;

import iuh.fit.www_phone_ver1.dto.user.UserRegistrationDto;
import iuh.fit.www_phone_ver1.entity.Role;
import iuh.fit.www_phone_ver1.entity.User;
import iuh.fit.www_phone_ver1.repository.RoleRepository;
import iuh.fit.www_phone_ver1.repository.UserRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final AccountTokenService tokens;

  public UserService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      AccountTokenService tokens) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokens = tokens;
  }

  @org.springframework.transaction.annotation.Transactional
  public void registerUser(UserRegistrationDto registrationDto) {
    registrationDto.setEmail(registrationDto.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
    if (registrationDto.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length
        > 72) {
      throw new IllegalArgumentException("Mật khẩu tối đa 72 byte UTF-8.");
    }
    if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
      throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
    }

    if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
      throw new IllegalArgumentException("Email đã được sử dụng");
    }

    User user = new User();
    user.setFullName(registrationDto.getFullName());
    user.setEmail(registrationDto.getEmail());
    user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
    user.setPhone(registrationDto.getPhone());
    user.setAddress(registrationDto.getAddress());
    user.setStatus(1);
    user.setEmailVerified(false);

    Optional<Role> roleOpt = roleRepository.findByName("USER");
    Role role =
        roleOpt.orElseGet(
            () -> {
              Role newRole = new Role();
              newRole.setName("USER");
              return roleRepository.save(newRole);
            });

    user.getRoles().add(role);

    try {
      userRepository.saveAndFlush(user);
    } catch (org.springframework.dao.DataIntegrityViolationException e) {
      throw new IllegalArgumentException("Email đã được sử dụng hoặc dữ liệu không hợp lệ.");
    }

    tokens.sendVerification(user);
  }
}
