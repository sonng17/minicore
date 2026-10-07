package com.minicore.core.common;

import java.time.OffsetDateTime;

/**
 * Khung response chung cho mọi API: { code, message, data, traceId, timestamp }.
 */
public record ApiResponse<T>(String code, String message, T data, String traceId, OffsetDateTime timestamp) {

    public static <T> ApiResponse<T> of(ErrorCode errorCode, T data) {
        return new ApiResponse<>(errorCode.code(), errorCode.message(), data,
                TraceIdFilter.currentTraceId(), OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> ok(T data) {
        return of(ErrorCode.SUCCESS, data);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.code(), message, null,
                TraceIdFilter.currentTraceId(), OffsetDateTime.now());
    }
}
