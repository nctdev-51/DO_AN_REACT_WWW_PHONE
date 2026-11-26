package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.OrderDetail;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
  @Query(value = """
      SELECT p.id AS productId, p.name AS productName, p.sku AS sku,
             stock.total AS stock,
             COALESCE(sales.units, 0) AS unitsSold,
             sales.lastSale AS lastSale
      FROM products p
      JOIN (
        SELECT product_id, SUM(stock) AS total
        FROM product_variants
        WHERE active = TRUE
        GROUP BY product_id
        HAVING SUM(stock) >= :minimumStock
      ) stock ON stock.product_id = p.id
      LEFT JOIN (
        SELECT d.product_id,
               SUM(CASE WHEN COALESCE(o.delivered_at, o.order_date) >= :since
                        THEN d.quantity ELSE 0 END) AS units,
               MAX(COALESCE(o.delivered_at, o.order_date)) AS lastSale
        FROM order_details d
        JOIN orders o ON o.id = d.order_id
        WHERE o.status = :completed AND COALESCE(o.delivered_at, o.order_date) <= :now
        GROUP BY d.product_id
      ) sales ON sales.product_id = p.id
      WHERE p.status = 1 AND p.sku IS NOT NULL AND TRIM(p.sku) <> ''
        AND COALESCE(sales.units, 0) < :salesLimit
      ORDER BY stock.total DESC, p.id
      """, nativeQuery = true)
  List<SlowStock> findSlowStock(@Param("minimumStock") int minimumStock,
      @Param("salesLimit") int salesLimit, @Param("since") LocalDateTime since,
      @Param("now") LocalDateTime now, @Param("completed") String completed);

  interface SlowStock {
    Long getProductId();
    String getProductName();
    String getSku();
    long getStock();
    long getUnitsSold();
    LocalDateTime getLastSale();
  }

  boolean existsByProduct_Id(Long productId);

  boolean existsByProduct_IdAndOrder_User_EmailAndOrder_Status(
      Long productId, String email, String status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT d FROM OrderDetail d WHERE d.id = :id AND d.order.id = :orderId")
  Optional<OrderDetail> findForUpdate(Long id, Long orderId);
}
