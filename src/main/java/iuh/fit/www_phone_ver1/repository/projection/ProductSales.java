package iuh.fit.www_phone_ver1.repository.projection;

/** Read model used by catalog cards; sales remain derived from completed order lines. */
public interface ProductSales {
  Long getProductId();

  Long getSoldQuantity();
}
