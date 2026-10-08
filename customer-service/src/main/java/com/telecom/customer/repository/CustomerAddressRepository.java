package com.telecom.customer.repository;

import com.telecom.customer.entity.CustomerAddress;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

  List<CustomerAddress> findByCustomerIdOrderByPrimaryAddressDescIdAsc(Long customerId);
}
