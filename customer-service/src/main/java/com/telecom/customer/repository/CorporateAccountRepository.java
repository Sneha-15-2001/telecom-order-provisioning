package com.telecom.customer.repository;

import com.telecom.customer.entity.CorporateAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorporateAccountRepository extends JpaRepository<CorporateAccount, Long> {

  Optional<CorporateAccount> findByAccountNumber(String accountNumber);

  Optional<CorporateAccount> findByRegistrationNumber(String registrationNumber);
}
