package com.telecom.order.repository;

import com.telecom.order.entity.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  List<OrderItem> findByOrderIdOrderByIdAsc(Long orderId);

  void deleteByOrderId(Long orderId);
}
