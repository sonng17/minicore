package com.minicore.core.transfer;

import java.math.BigDecimal;

/**
 * Kết quả chuyển khoản. replayed = true nghĩa là Idempotency-Key đã dùng trước đó:
 * không trừ tiền lần hai, chỉ trả lại txnId của lần đầu.
 */
public record TransferResponse(Long txnId,
                               String txnRef,
                               String fromAccount,
                               String toAccount,
                               BigDecimal amount,
                               boolean replayed) {
}
