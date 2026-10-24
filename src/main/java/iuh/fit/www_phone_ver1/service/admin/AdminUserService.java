package iuh.fit.www_phone_ver1.service.admin;

import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {
  private final OrderRepository orders;
  private final UserRepository users;
  private final AddressRepository addresses;
  private final ReviewRepository reviews;
  private final AccountTokenRepository tokens;
  private final WishlistRepository wishlist;

  public AdminUserService(
      OrderRepository orders,
      UserRepository users,
      AddressRepository addresses,
      ReviewRepository reviews,
      AccountTokenRepository tokens,
      WishlistRepository wishlist) {
    this.orders = orders;
    this.users = users;
    this.addresses = addresses;
    this.reviews = reviews;
    this.tokens = tokens;
    this.wishlist = wishlist;
  }

  @Transactional
  public void updateUser(
      Long id, String name, String phone, String address, Integer status, String actor) {
    User user =
        users
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));
    if (name == null || name.isBlank() || name.trim().length() > 100)
      throw new IllegalArgumentException("Họ tên phải từ 1 đến 100 ký tự.");
    if (phone != null && !phone.isBlank() && !phone.matches("0[0-9]{9}"))
      throw new IllegalArgumentException("Số điện thoại phải gồm 10 chữ số, bắt đầu bằng 0.");
    if (address != null && address.length() > 255)
      throw new IllegalArgumentException("Địa chỉ tối đa 255 ký tự.");
    if (status == null || (status != 0 && status != 1))
      throw new IllegalArgumentException("Trạng thái không hợp lệ.");
    if (status == 0 && (user.getEmail().equals(actor) || user.isAdmin()))
      throw new IllegalArgumentException("Không thể khóa tài khoản quản trị viên.");
    user.setFullName(name.trim());
    user.setPhone(phone == null ? null : phone.trim());
    user.setAddress(address == null ? null : address.trim());
    user.setStatus(status);
    users.save(user);
  }

  @Transactional
  public void deleteUser(Long id, String actor) {
    User user =
        users
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));
    if (user.getEmail().equals(actor) || user.isAdmin())
      throw new IllegalArgumentException("Không thể xóa tài khoản quản trị viên hoặc chính mình.");
    if (orders.countByUser_Id(id) > 0)
      throw new IllegalArgumentException("Không thể xóa người dùng đã có đơn hàng.");
    addresses.deleteByUser_Id(id);
    reviews.deleteByUser_Id(id);
    tokens.deleteByUser_Id(id);
    wishlist.deleteByUser_Id(id);
    users.delete(user);
  }
}
