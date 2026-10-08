package com.telecom.customer.repository;

import com.telecom.customer.entity.Customer;
import com.telecom.customer.entity.CustomerStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

  Optional<Customer> findByCustomerNumber(String customerNumber);

  Optional<Customer> findByEmail(String email);

  Optional<Customer> findByPhone(String phone);

  Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);

  @Query(
      "SELECT c FROM Customer c WHERE "
          + "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
          + "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
          + "LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%')) OR "
          + "c.phone LIKE CONCAT('%', :q, '%') OR "
          + "LOWER(c.customerNumber) LIKE LOWER(CONCAT('%', :q, '%'))")
  Page<Customer> search(@Param("q") String query, Pageable pageable);
}
