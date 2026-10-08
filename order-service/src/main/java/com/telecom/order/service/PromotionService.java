package com.telecom.order.service;

import com.telecom.order.config.CorrelationIdFilter;
import com.telecom.order.dto.CreatePromotionRequest;
import com.telecom.order.dto.PromotionResponse;
import com.telecom.order.entity.Promotion;
import com.telecom.order.mapper.OrderMapper;
import com.telecom.order.repository.PromotionRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Promotion catalog management. */
@Service
public class PromotionService {

  private static final Logger log = LoggerFactory.getLogger(PromotionService.class);

  private final PromotionRepository promotions;

  public PromotionService(PromotionRepository promotions) {
    this.promotions = promotions;
  }

  @Transactional
  public PromotionResponse create(CreatePromotionRequest req) {
    promotions.findByPromoCode(req.promoCode().trim().toUpperCase()).ifPresent(p -> {
      throw new IllegalArgumentException("Promotion already exists: " + req.promoCode());
    });
    Promotion p = new Promotion();
    p.setPromoCode(req.promoCode().trim().toUpperCase());
    p.setDescription(req.description().trim());
    p.setDiscountType(req.discountType());
    p.setDiscountValue(req.discountValue());
    p.setActive(req.active());
    p.setValidFrom(req.validFrom());
    p.setValidTo(req.validTo());
    Promotion saved = promotions.save(p);
    log.info("service=order-service correlationId={} event=PROMOTION_CREATED promo={} status=SUCCESS",
        correlationId(), saved.getPromoCode());
    return OrderMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<PromotionResponse> list() {
    return promotions.findAll().stream().map(OrderMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public PromotionResponse get(Long id) {
    return promotions.findById(id)
        .map(OrderMapper::toResponse)
        .orElseThrow(() -> new NoSuchElementException("Promotion not found: " + id));
  }

  private String correlationId() {
    return MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
  }
}
