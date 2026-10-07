package com.minicore.core.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

/** Ánh xạ bảng CUSTOMER (db/migration/V1__init_schema.sql). */
@Entity
@Table(name = "CUSTOMER")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Column(name = "CIF_NO", nullable = false, unique = true, length = 20)
    private String cifNo;

    @Column(name = "FULL_NAME", nullable = false, length = 200)
    private String fullName;

    @Column(name = "ID_NUMBER", nullable = false, unique = true, length = 20)
    private String idNumber;

    @Column(name = "PHONE", length = 20)
    private String phone;

    @Column(name = "STATUS", nullable = false, length = 10)
    private String status;

    @Column(name = "CREATED_AT", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Optimistic lock: hai người sửa cùng lúc thì người sau bị chặn. */
    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    protected Customer() {
        // cho JPA
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCifNo() {
        return cifNo;
    }

    public String getFullName() {
        return fullName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Integer getVersion() {
        return version;
    }
}
