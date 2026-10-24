package iuh.fit.www_phone_ver1.repository;

import iuh.fit.www_phone_ver1.entity.Address;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
  List<Address> findByUser_EmailOrderByDefaultAddressDescIdDesc(String email);

  Optional<Address> findByIdAndUser_Email(Long id, String email);

  void deleteByUser_Id(Long userId);
}
