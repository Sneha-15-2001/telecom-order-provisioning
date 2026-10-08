package com.telecom.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.telecom.customer.dto.CorporateAccountRequest;
import com.telecom.customer.repository.CorporateAccountRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CorporateAccountServiceTest {

  @Mock CorporateAccountRepository accounts;

  @InjectMocks CorporateAccountService service;

  @Test
  void createAssignsNumberAndActive() {
    when(accounts.findByRegistrationNumber("CIN-1")).thenReturn(Optional.empty());
    when(accounts.save(any())).thenAnswer(i -> i.getArgument(0));

    var res = service.create(new CorporateAccountRequest("Acme", "CIN-1", "a@acme.example", "+911111111111"));
    assertThat(res.accountNumber()).startsWith("CORP-");
    assertThat(res.status()).isEqualTo(com.telecom.customer.entity.CustomerStatus.ACTIVE);
  }

  @Test
  void duplicateRegistrationRejected() {
    when(accounts.findByRegistrationNumber("CIN-1"))
        .thenReturn(Optional.of(new com.telecom.customer.entity.CorporateAccount()));

    assertThatThrownBy(() -> service.create(
        new CorporateAccountRequest("Acme", "CIN-1", "a@acme.example", "+911111111111")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getMissingThrows404() {
    when(accounts.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(999L)).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void listDelegates() {
    when(accounts.findAll()).thenReturn(List.of());
    assertThat(service.list()).isEmpty();
  }
}
