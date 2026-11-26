package iuh.fit.www_phone_ver1.dto.cart;

import iuh.fit.www_phone_ver1.entity.ProductVariant;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

public class Cart implements Serializable {
  private static final long serialVersionUID = 2L;
  private final Map<Long, CartItem> items = new LinkedHashMap<>();

  public synchronized void addItem(ProductVariant variant, int quantity) {
    if (!variant.isActive() || variant.getProduct().getStatus() != 1)
      throw new IllegalArgumentException("Phiên bản đã ngừng bán.");
    validate(quantity);
    CartItem existing = items.get(variant.getId());
    int total = (existing == null ? 0 : existing.getQuantity()) + quantity;
    validate(total);
    if (total > variant.getStock())
      throw new IllegalArgumentException("Số lượng vượt quá tồn kho (" + variant.getStock() + ").");
    if (existing == null) items.put(variant.getId(), new CartItem(variant, quantity));
    else existing.setQuantity(total);
  }

  public synchronized void updateQuantity(Long variantId, int quantity) {
    if (quantity == 0) {
      items.remove(variantId);
      return;
    }
    validate(quantity);
    CartItem item = items.get(variantId);
    if (item != null) item.setQuantity(quantity);
  }

  public synchronized void removeItem(Long variantId) {
    items.remove(variantId);
  }

  public synchronized Collection<CartItem> getItemList() {
    return List.copyOf(items.values());
  }

  public synchronized BigDecimal getTotalAmount() {
    return items.values().stream()
        .map(CartItem::getSubTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public synchronized int getTotalQuantity() {
    return items.values().stream().mapToInt(CartItem::getQuantity).sum();
  }

  public synchronized void clear() {
    items.clear();
  }

  private static void validate(int quantity) {
    if (quantity < 1 || quantity > 99)
      throw new IllegalArgumentException("Số lượng phải từ 1 đến 99.");
  }
}
