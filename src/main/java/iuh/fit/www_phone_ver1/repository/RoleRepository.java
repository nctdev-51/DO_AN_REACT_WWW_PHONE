package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(String name);
}
