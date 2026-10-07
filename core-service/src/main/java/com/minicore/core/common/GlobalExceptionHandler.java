package com.minicore.core.common;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Xử lý lỗi tập trung: mọi lỗi đều trả về cùng khung ApiResponse với mã MC-xxxx.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        ErrorCode ec = ex.getErrorCode();
        return ResponseEntity.status(ec.httpStatus()).body(ApiResponse.error(ec, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return badRequest(detail);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
        return badRequest("Thiếu header " + ex.getHeaderName());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(Exception ex) {
        return badRequest("Body hoặc tham số không đúng định dạng");
    }

    /** Lỗi CSDL, gồm cả SQLException khi PKG_TRANSFER ném lỗi: không lộ chi tiết ra ngoài. */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException ex) {
        log.error("Lỗi truy cập CSDL", ex);
        return ResponseEntity.status(ErrorCode.SYSTEM_ERROR.httpStatus())
                .body(ApiResponse.of(ErrorCode.SYSTEM_ERROR, null));
    }

    /** Lỗi chuẩn của Spring MVC (sai đường dẫn, sai method, sai Content-Type): giữ nguyên HTTP status. */
    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ApiResponse<Void>> handleSpringError(Exception ex) {
        ErrorResponse er = (ErrorResponse) ex;
        return ResponseEntity.status(er.getStatusCode())
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception ex) {
        log.error("Lỗi không lường trước", ex);
        return ResponseEntity.status(ErrorCode.SYSTEM_ERROR.httpStatus())
                .body(ApiResponse.of(ErrorCode.SYSTEM_ERROR, null));
    }

    private ResponseEntity<ApiResponse<Void>> badRequest(String detail) {
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.httpStatus())
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST, detail));
    }
}
