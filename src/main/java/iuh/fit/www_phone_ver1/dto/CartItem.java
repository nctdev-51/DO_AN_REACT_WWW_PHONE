package iuh.fit.www_phone_ver1.dto;

import iuh.fit.www_phone_ver1.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Product product;
    private int quantity;

    public Double getSubTotal() {
        if (product == null || product.getPrice() == null) {
            return 0.0;
        }
        return product.getPrice() * quantity;
    }
}