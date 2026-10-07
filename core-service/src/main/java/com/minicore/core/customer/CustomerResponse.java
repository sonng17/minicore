package com.minicore.core.customer;

/** Thông tin khách hàng trả ra API; che bớt số giấy tờ tùy thân. */
public record CustomerResponse(String cifNo, String fullName, String idNumberMasked, String phone, String status) {

    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.getCifNo(), c.getFullName(), mask(c.getIdNumber()), c.getPhone(), c.getStatus());
    }

    private static String mask(String value) {
        if (value == null || value.length() <= 4) {
            return value;
        }
        return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
    }
}
