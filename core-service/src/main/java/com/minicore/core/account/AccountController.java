package com.minicore.core.account;

import com.minicore.core.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountNo}")
    public ApiResponse<AccountResponse> getAccount(@PathVariable String accountNo) {
        return ApiResponse.ok(accountService.getByAccountNo(accountNo));
    }
}
