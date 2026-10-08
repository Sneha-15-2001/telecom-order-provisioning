package com.telecom.order.repository;

import com.telecom.order.entity.Promotion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

  Optional<Promotion> findByPromoCode(String promoCode);

  List<Promotion> findByActiveTrue();
}
