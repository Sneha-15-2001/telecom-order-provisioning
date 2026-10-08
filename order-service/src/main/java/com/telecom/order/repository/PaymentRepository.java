package com.telecom.order.repository;

import com.telecom.order.entity.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);

  Optional<Payment> findByPaymentReference(String paymentReference);
}
