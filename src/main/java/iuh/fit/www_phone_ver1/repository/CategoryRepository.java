package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  java.util.List<Category> findByNameContainingIgnoreCase(String name, org.springframework.data.domain.Sort sort);
  Optional<Category> findFirstByName(String name);
}
