package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(Integer status);

    Page<Product> findByStatus(Integer status, Pageable pageable);

    @Query("""
            select p from Product p
            where p.status = 1
              and (:keyword is null or lower(p.name) like lower(concat('%', :keyword, '%')))
              and (:brand is null or p.brand = :brand)
              and (:categoryId is null or p.category.id = :categoryId)
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
            """)
    Page<Product> search(@Param("keyword") String keyword,
                         @Param("brand") String brand,
                         @Param("categoryId") Long categoryId,
                         @Param("minPrice") Double minPrice,
                         @Param("maxPrice") Double maxPrice,
                         Pageable pageable);

    @Query("select distinct p.brand from Product p where p.status = 1 and p.brand is not null order by p.brand")
    List<String> findAvailableBrands();

    @Query("select count(p) from Product p where p.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
}