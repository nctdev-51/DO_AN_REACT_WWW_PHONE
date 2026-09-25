package iuh.fit.www_phone_ver1.dto;

import iuh.fit.www_phone_ver1.entity.Product;
import lombok.Getter;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<Long, CartItem> items = new HashMap<>();

    public void addItem(Product product, int quantity) {
        Long productId = product.getId();
        if (items.containsKey(productId)) {
            CartItem currentItem = items.get(productId);
            currentItem.setQuantity(currentItem.getQuantity() + quantity);
        } else {
            items.put(productId, new CartItem(product, quantity));
        }
    }

    public void updateQuantity(Long productId, int quantity) {
        if (items.containsKey(productId)) {
            if (quantity <= 0) {
                items.remove(productId);
            } else {
                items.get(productId).setQuantity(quantity);
            }
        }
    }

    public void removeItem(Long productId) {
        items.remove(productId);
    }

    public Collection<CartItem> getItemList() {
        return items.values();
    }

    public Double getTotalAmount() {
        return items.values().stream()
                .mapToDouble(CartItem::getSubTotal)
                .sum();
    }

    public int getTotalQuantity() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public void clear() {
        items.clear();
    }
}