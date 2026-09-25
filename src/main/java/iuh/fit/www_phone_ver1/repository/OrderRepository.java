package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}