package com.minicore.core.account;

import com.minicore.core.common.BusinessException;
import com.minicore.core.common.ErrorCode;
import com.minicore.core.customer.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public AccountResponse getByAccountNo(String accountNo) {
        return accountRepository.findByAccountNo(accountNo)
                .map(AccountResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getByCif(String cifNo) {
        if (customerRepository.findByCifNo(cifNo).isEmpty()) {
            throw new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        return accountRepository.findByCustomer_CifNoOrderByAccountNo(cifNo).stream()
                .map(AccountResponse::from)
                .toList();
    }
}
