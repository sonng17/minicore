package com.minicore.core.account;

import java.math.BigDecimal;

public record AccountResponse(String accountNo,
                              String cifNo,
                              String accountType,
                              String currency,
                              BigDecimal balance,
                              BigDecimal holdAmount,
                              BigDecimal availableBalance,
                              String status) {

    /** Gọi bên trong transaction vì customer được nạp kiểu LAZY. */
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getAccountNo(), a.getCustomer().getCifNo(), a.getAccountType(),
                a.getCurrency(), a.getBalance(), a.getHoldAmount(), a.getAvailableBalance(), a.getStatus());
    }
}
