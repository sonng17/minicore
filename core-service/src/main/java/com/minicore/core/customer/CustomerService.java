package com.minicore.core.customer;

import com.minicore.core.common.BusinessException;
import com.minicore.core.common.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public CustomerResponse getByCif(String cifNo) {
        return customerRepository.findByCifNo(cifNo)
                .map(CustomerResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }
}
