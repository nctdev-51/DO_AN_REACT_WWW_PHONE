package iuh.fit.www_phone_ver1.service.user;

import iuh.fit.www_phone_ver1.dto.user.*;
import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
  private final UserRepository users;
  private final AddressRepository addresses;
  private final PasswordEncoder passwords;
  private final AccountTokenRepository tokens;
  private final iuh.fit.www_phone_ver1.service.order.AddressDirectory directory;

  public AccountService(
      UserRepository users,
      AddressRepository addresses,
      PasswordEncoder passwords,
      AccountTokenRepository tokens,
      iuh.fit.www_phone_ver1.service.order.AddressDirectory directory) {
    this.users = users;
    this.addresses = addresses;
    this.passwords = passwords;
    this.tokens = tokens;
    this.directory = directory;
  }

  public User user(String email) {
    return users
        .findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
  }

  @Transactional
  public void updateProfile(String email, ProfileForm form) {
    User user = user(email);
    user.setFullName(form.getFullName().trim());
    user.setPhone(form.getPhone());
  }

  @Transactional
  public void changePassword(String email, PasswordForm form) {
    User user = users.lockByEmail(email).orElseThrow();
    if (!passwords.matches(form.getCurrentPassword(), user.getPassword()))
      throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
    if (!form.getNewPassword().equals(form.getConfirmPassword()))
      throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
    if (form.getNewPassword().getBytes(StandardCharsets.UTF_8).length > 72)
      throw new IllegalArgumentException("Mật khẩu tối đa 72 byte UTF-8.");
    user.setPassword(passwords.encode(form.getNewPassword()));
    tokens
        .findByUser_IdAndPurposeAndConsumedAtIsNull(
            user.getId(), AccountToken.Purpose.RESET_PASSWORD)
        .forEach(token -> token.setConsumedAt(java.time.Instant.now()));
  }

  @Transactional
  public void saveAddress(String email, Long id, AddressForm form) {
    User user = users.lockByEmail(email).orElseThrow();
    List<Address> existing = addresses.findByUser_EmailOrderByDefaultAddressDescIdDesc(email);
    if (id == null && existing.size() >= 10)
      throw new IllegalArgumentException("Tối đa 10 địa chỉ.");
    Address address =
        id == null
            ? new Address()
            : addresses
                .findByIdAndUser_Email(id, email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ."));
    boolean makeDefault =
        form.isDefaultAddress() || existing.isEmpty() || address.isDefaultAddress();
    if (makeDefault) existing.forEach(a -> a.setDefaultAddress(false));
    address.setUser(user);
    address.setRecipientName(form.getRecipientName().trim());
    address.setPhone(form.getPhone());
    var resolved =
        directory.resolve(
            form.getProvinceCode(),
            form.getWardCode(),
            form.getProvince(),
            form.getWard(),
            "structured".equals(form.getAddressMode()));
    address.setProvinceCode(resolved == null ? null : resolved.provinceCode());
    address.setWardCode(resolved == null ? null : resolved.wardCode());
    address.setProvince(resolved == null ? null : resolved.province());
    address.setWard(resolved == null ? null : resolved.ward());
    address.setLine(form.getLine().trim());
    address.setDefaultAddress(makeDefault);
    addresses.save(address);
  }

  @Transactional
  public void deleteAddress(String email, Long id) {
    users.lockByEmail(email).orElseThrow();
    Address address =
        addresses
            .findByIdAndUser_Email(id, email)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ."));
    boolean wasDefault = address.isDefaultAddress();
    addresses.delete(address);
    addresses.flush();
    if (wasDefault)
      addresses.findByUser_EmailOrderByDefaultAddressDescIdDesc(email).stream()
          .findFirst()
          .ifPresent(a -> a.setDefaultAddress(true));
  }
}
