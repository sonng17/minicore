package com.minicore.core.account;

import com.minicore.core.customer.Customer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ánh xạ bảng ACCOUNT. Java chỉ ĐỌC số dư; mọi thay đổi tiền đi qua PKG_TRANSFER
 * trong Oracle để giữ hạch toán kép và khóa theo thứ tự ở một chỗ duy nhất.
 */
@Entity
@Table(name = "ACCOUNT")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACCOUNT_ID")
    private Long accountId;

    @Column(name = "ACCOUNT_NO", nullable = false, unique = true, length = 20)
    private String accountNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @Column(name = "ACCOUNT_TYPE", nullable = false, length = 10)
    private String accountType;

    @Column(name = "CURRENCY", nullable = false, length = 3)
    private String currency;

    /** Tiền luôn là BigDecimal, không dùng double. */
    @Column(name = "BALANCE", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "HOLD_AMOUNT", nullable = false, precision = 19, scale = 2)
    private BigDecimal holdAmount;

    @Column(name = "STATUS", nullable = false, length = 10)
    private String status;

    @Column(name = "OPENED_AT", nullable = false, insertable = false, updatable = false)
    private LocalDateTime openedAt;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    protected Account() {
        // cho JPA
    }

    /** Số dư khả dụng = số dư - số tiền đang bị giữ (hold). */
    public BigDecimal getAvailableBalance() {
        return balance.subtract(holdAmount);
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getHoldAmount() {
        return holdAmount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public Integer getVersion() {
        return version;
    }
}
