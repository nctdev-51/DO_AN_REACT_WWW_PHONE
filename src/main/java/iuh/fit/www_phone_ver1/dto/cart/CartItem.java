package iuh.fit.www_phone_ver1.dto.cart;

import iuh.fit.www_phone_ver1.entity.ProductVariant;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CartItem implements Serializable {
  private static final long serialVersionUID = 2L;
  private final Long variantId;
  private final Long productId;
  private final String productName;
  private final String variantLabel;
  private final String sku;
  private final String image;
  @Setter private int quantity;
  @Setter private BigDecimal unitPrice;

  public CartItem(ProductVariant variant, int quantity) {
    this.variantId = variant.getId();
    this.productId = variant.getProduct().getId();
    this.productName = variant.getProduct().getName();
    this.variantLabel = variant.getLabel();
    this.sku = variant.getSku();
    this.image = variant.getImage();
    this.unitPrice = variant.getPrice();
    this.quantity = quantity;
  }

  public BigDecimal getSubTotal() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }
}
