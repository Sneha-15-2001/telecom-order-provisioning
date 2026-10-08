package com.telecom.order.repository;

import com.telecom.order.entity.CustomerOrder;
import com.telecom.order.entity.OrderStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

  Optional<CustomerOrder> findByOrderNumber(String orderNumber);

  Page<CustomerOrder> findByCustomerId(Long customerId, Pageable pageable);

  Page<CustomerOrder> findByStatus(OrderStatus status, Pageable pageable);

  Page<CustomerOrder> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

  @Query(
      "SELECT o FROM CustomerOrder o WHERE "
          + "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
          + "LOWER(COALESCE(o.customerNumber, '')) LIKE LOWER(CONCAT('%', :q, '%'))")
  Page<CustomerOrder> search(@Param("q") String query, Pageable pageable);
}
