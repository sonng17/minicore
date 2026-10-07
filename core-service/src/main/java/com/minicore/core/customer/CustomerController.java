package com.minicore.core.customer;

import com.minicore.core.account.AccountResponse;
import com.minicore.core.account.AccountService;
import com.minicore.core.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final AccountService accountService;

    public CustomerController(CustomerService customerService, AccountService accountService) {
        this.customerService = customerService;
        this.accountService = accountService;
    }

    @GetMapping("/{cifNo}")
    public ApiResponse<CustomerResponse> getCustomer(@PathVariable String cifNo) {
        return ApiResponse.ok(customerService.getByCif(cifNo));
    }

    @GetMapping("/{cifNo}/accounts")
    public ApiResponse<List<AccountResponse>> getAccounts(@PathVariable String cifNo) {
        return ApiResponse.ok(accountService.getByCif(cifNo));
    }
}
