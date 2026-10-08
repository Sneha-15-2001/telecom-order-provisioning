package com.telecom.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.telecom.order.dto.CreatePromotionRequest;
import com.telecom.order.entity.DiscountType;
import com.telecom.order.entity.Promotion;
import com.telecom.order.repository.PromotionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

  @Mock PromotionRepository promotions;

  @InjectMocks PromotionService service;

  @Test
  void createUpperCasesCode() {
    when(promotions.findByPromoCode("FESTIVE10")).thenReturn(Optional.empty());
    when(promotions.save(any(Promotion.class))).thenAnswer(i -> i.getArgument(0));

    var res = service.create(new CreatePromotionRequest("festive10", "desc", DiscountType.PERCENTAGE,
        new BigDecimal("10"), true, null, null));
    assertThat(res.promoCode()).isEqualTo("FESTIVE10");
  }

  @Test
  void duplicateRejected() {
    when(promotions.findByPromoCode("FESTIVE10")).thenReturn(Optional.of(new Promotion()));

    assertThatThrownBy(() -> service.create(new CreatePromotionRequest("FESTIVE10", "desc",
        DiscountType.FLAT, new BigDecimal("5"), true, null, null)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getMissingThrows404() {
    when(promotions.findById(999L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.get(999L)).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void listDelegates() {
    when(promotions.findAll()).thenReturn(List.of());
    assertThat(service.list()).isEmpty();
  }
}
