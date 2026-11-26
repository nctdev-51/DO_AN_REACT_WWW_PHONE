package iuh.fit.www_phone_ver1.service.catalog;

import iuh.fit.www_phone_ver1.entity.*;
import iuh.fit.www_phone_ver1.repository.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistService {
  private final WishlistRepository entries;
  private final UserRepository users;
  private final ProductRepository products;

  public WishlistService(
      WishlistRepository entries, UserRepository users, ProductRepository products) {
    this.entries = entries;
    this.users = users;
    this.products = products;
  }

  @Transactional(readOnly = true)
  public List<WishlistEntry> list(String email) {
    return entries.findByUser_EmailOrderByCreatedAtDescIdDesc(email);
  }

  @Transactional
  public void add(String email, Long productId) {
    User user = users.lockByEmail(email).orElseThrow();
    if (user.getStatus() != 1) throw new IllegalArgumentException("Tài khoản đã bị khóa.");
    Product product =
        products
            .findById(productId)
            .filter(p -> p.getStatus() == 1)
            .orElseThrow(() -> new IllegalArgumentException("Sản phẩm đã ngừng kinh doanh."));
    if (entries.existsByUser_IdAndProduct_Id(user.getId(), productId)) return;
    if (entries.countByUser_Id(user.getId()) >= 100)
      throw new IllegalArgumentException("Danh sách yêu thích tối đa 100 sản phẩm.");
    WishlistEntry entry = new WishlistEntry();
    entry.setUser(user);
    entry.setProduct(product);
    entries.save(entry);
  }

  @Transactional
  public void remove(String email, Long productId) {
    User user = users.lockByEmail(email).orElseThrow();
    entries.deleteByUser_IdAndProduct_Id(user.getId(), productId);
  }
}
