package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select distinct p.category.id from Product p where p.category is not null")
  java.util.List<Long> findIdsWithProducts();
  java.util.List<Category> findByNameContainingIgnoreCase(String name, org.springframework.data.domain.Sort sort);
  Optional<Category> findFirstByName(String name);
}
