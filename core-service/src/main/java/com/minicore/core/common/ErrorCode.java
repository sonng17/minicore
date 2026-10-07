package com.minicore.core.common;

import org.springframework.http.HttpStatus;

/**
 * Bộ mã lỗi chung, khớp với docs/spec/internal-transfer.md.
 * Mã do PKG_TRANSFER trả về được ánh xạ sang HTTP status ở đây.
 */
public enum ErrorCode {

    SUCCESS("MC-0000", "Thành công", HttpStatus.OK),
    INVALID_REQUEST("MC-1000", "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    SOURCE_ACCOUNT_NOT_FOUND("MC-1001", "Tài khoản nguồn không tồn tại", HttpStatus.NOT_FOUND),
    TARGET_ACCOUNT_NOT_FOUND("MC-1002", "Tài khoản đích không tồn tại", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_ACTIVE("MC-1003", "Tài khoản bị phong tỏa hoặc đã đóng", HttpStatus.UNPROCESSABLE_ENTITY),
    INSUFFICIENT_BALANCE("MC-1004", "Số dư khả dụng không đủ", HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_AMOUNT("MC-1005", "Số tiền không hợp lệ", HttpStatus.BAD_REQUEST),
    DUPLICATE_TRANSACTION("MC-1006", "Trùng mã giao dịch, trả lại kết quả lần đầu", HttpStatus.OK),
    SAME_ACCOUNT("MC-1007", "Tài khoản nguồn trùng tài khoản đích", HttpStatus.UNPROCESSABLE_ENTITY),
    CURRENCY_MISMATCH("MC-1008", "Khác loại tiền", HttpStatus.UNPROCESSABLE_ENTITY),
    CUSTOMER_NOT_FOUND("MC-1101", "Khách hàng không tồn tại", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND("MC-1102", "Tài khoản không tồn tại", HttpStatus.NOT_FOUND),
    SYSTEM_ERROR("MC-9999", "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    /** Tìm ErrorCode theo mã chuỗi; mã lạ coi là lỗi hệ thống. */
    public static ErrorCode fromCode(String code) {
        for (ErrorCode ec : values()) {
            if (ec.code.equals(code)) {
                return ec;
            }
        }
        return SYSTEM_ERROR;
    }
}
