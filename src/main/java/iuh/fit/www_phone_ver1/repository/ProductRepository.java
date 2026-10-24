package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository
    extends JpaRepository<Product, Long>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<Product> {
  java.util.Optional<Product> findBySku(String sku);

  List<Product> findTop4ByCategory_IdAndStatusAndIdNot(Long categoryId, Integer status, Long id);

  List<Product> findByStatus(Integer status);

  Page<Product> findByStatus(Integer status, Pageable pageable);

  default Page<Product> search(
      String keyword,
      String brand,
      Long categoryId,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Pageable pageable) {
    return searchFiltered(
        keyword, brand, categoryId, minPrice, maxPrice, null, null, false, pageable);
  }

  default Page<Product> searchFiltered(
      String keyword,
      String brand,
      Long categoryId,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      String storage,
      String ram,
      boolean inStock,
      Pageable pageable) {
    String sort =
        pageable.getSort().getOrderFor("price") == null
            ? "newest"
            : pageable.getSort().getOrderFor("price").isAscending() ? "price-asc" : "price-desc";
    var filter =
        new iuh.fit.www_phone_ver1.service.catalog.CatalogFilter(
            keyword, brand, categoryId, minPrice, maxPrice, storage, ram, inStock);
    if (pageable.isUnpaged())
      return new org.springframework.data.domain.PageImpl<>(findAll(filter.specification(sort)));
    return findAll(
        filter.specification(sort),
        org.springframework.data.domain.PageRequest.of(
            pageable.getPageNumber(), pageable.getPageSize()));
  }

  @Query(
      "select distinct p.brand from Product p where p.status = 1 and p.brand is not null order by"
          + " p.brand")
  List<String> findAvailableBrands();

  @Query("select count(p) from Product p where p.category.id = :categoryId")
  long countByCategoryId(@Param("categoryId") Long categoryId);
}
