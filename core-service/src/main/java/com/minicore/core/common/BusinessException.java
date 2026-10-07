package com.minicore.core.common;

/**
 * Lỗi nghiệp vụ có mã MC-xxxx. Là RuntimeException nên khi ném ra trong
 * phương thức @Transactional, Spring sẽ rollback (nhả khóa SELECT FOR UPDATE).
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
