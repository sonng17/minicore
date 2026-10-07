package com.minicore.core.transfer;

import java.math.BigDecimal;

/**
 * Cổng gọi xuống thủ tục chuyển khoản trong CSDL. Tách thành interface để
 * TransferService test được bằng Mockito mà không cần Oracle.
 */
public interface TransferProcedure {

    Result internalTransfer(String txnRef, String fromAccount, String toAccount,
                            BigDecimal amount, String channel, String description);

    /** Giá trị hai tham số OUT của PKG_TRANSFER.internal_transfer. */
    record Result(Long txnId, String resultCode) {
    }
}
