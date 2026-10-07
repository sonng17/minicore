package com.minicore.core.transfer;

import com.minicore.core.common.BusinessException;
import com.minicore.core.common.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    /** Cột TXN.TXN_REF là VARCHAR2(40). */
    static final int MAX_KEY_LENGTH = 40;

    private final TransferProcedure transferProcedure;

    public TransferService(TransferProcedure transferProcedure) {
        this.transferProcedure = transferProcedure;
    }

    /**
     * Chuyển khoản nội bộ. Idempotency-Key được dùng làm TXN_REF nên client gửi lại
     * cùng key sẽ không bị trừ tiền lần hai.
     *
     * Khi PKG_TRANSFER trả mã lỗi nghiệp vụ, phương thức ném BusinessException
     * (RuntimeException) để Spring rollback và nhả khóa trên hai tài khoản.
     */
    @Transactional
    public TransferResponse transfer(String idempotencyKey, TransferRequest request) {
        if (!StringUtils.hasText(idempotencyKey) || idempotencyKey.length() > MAX_KEY_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Idempotency-Key bắt buộc, tối đa " + MAX_KEY_LENGTH + " ký tự");
        }

        TransferProcedure.Result result = transferProcedure.internalTransfer(
                idempotencyKey,
                request.fromAccount(),
                request.toAccount(),
                request.amount(),
                request.channel().name(),
                request.description());

        ErrorCode code = ErrorCode.fromCode(result.resultCode());
        log.info("Chuyển khoản {} -> {} số tiền {}: {} (txnId={})", request.fromAccount(),
                request.toAccount(), request.amount(), code.code(), result.txnId());

        return switch (code) {
            case SUCCESS -> toResponse(result, idempotencyKey, request, false);
            case DUPLICATE_TRANSACTION -> toResponse(result, idempotencyKey, request, true);
            default -> throw new BusinessException(code);
        };
    }

    private static TransferResponse toResponse(TransferProcedure.Result result, String key,
                                               TransferRequest request, boolean replayed) {
        return new TransferResponse(result.txnId(), key, request.fromAccount(), request.toAccount(),
                request.amount(), replayed);
    }
}
