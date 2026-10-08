package com.telecom.order.repository;

import com.telecom.order.entity.OrderHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

  List<OrderHistory> findByOrderIdOrderByCreatedAtAscIdAsc(Long orderId);
}
