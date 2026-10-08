package com.telecom.customer.controller;

import com.telecom.customer.dto.CorporateAccountRequest;
import com.telecom.customer.dto.CorporateAccountResponse;
import com.telecom.customer.dto.CustomerResponse;
import com.telecom.customer.service.CorporateAccountService;
import com.telecom.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Corporate (B2B) accounts and linking customers to them. */
@Tag(name = "Corporate Accounts", description = "B2B accounts (Phase 2)")
@RestController
@RequestMapping(path = "/api/corporate-accounts", produces = MediaType.APPLICATION_JSON_VALUE)
public class CorporateAccountController {

  private final CorporateAccountService corporateAccountService;
  private final CustomerService customerService;

  public CorporateAccountController(
      CorporateAccountService corporateAccountService, CustomerService customerService) {
    this.corporateAccountService = corporateAccountService;
    this.customerService = customerService;
  }

  @Operation(summary = "Create a corporate account")
  @ApiResponse(responseCode = "201", description = "Corporate account created")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public CorporateAccountResponse create(@Valid @RequestBody CorporateAccountRequest request) {
    return corporateAccountService.create(request);
  }

  @Operation(summary = "List corporate accounts")
  @GetMapping
  public List<CorporateAccountResponse> list() {
    return corporateAccountService.list();
  }

  @Operation(summary = "Get a corporate account by ID")
  @GetMapping("/{id}")
  public CorporateAccountResponse get(@PathVariable Long id) {
    return corporateAccountService.get(id);
  }

  @Operation(summary = "Link a customer to a corporate account (becomes CORPORATE)")
  @PostMapping("/{id}/customers/{customerId}")
  public CustomerResponse linkCustomer(@PathVariable Long id, @PathVariable Long customerId) {
    return customerService.linkCorporateAccount(customerId, id);
  }
}
