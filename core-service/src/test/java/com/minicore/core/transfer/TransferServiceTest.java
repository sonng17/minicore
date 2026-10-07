package com.minicore.core.transfer;

import com.minicore.core.common.BusinessException;
import com.minicore.core.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit test cho TransferService: giả lập PKG_TRANSFER bằng Mockito, không cần Oracle. */
class TransferServiceTest {

    private TransferProcedure procedure;
    private TransferService service;

    private final TransferRequest request = new TransferRequest(
            "1000000001", "1000000002", new BigDecimal("500000.00"), Channel.TELLER, "test");

    @BeforeEach
    void setUp() {
        procedure = mock(TransferProcedure.class);
        service = new TransferService(procedure);
    }

    @Test
    void thanhCong_traVeTxnId() {
        when(procedure.internalTransfer("KEY-1", "1000000001", "1000000002",
                new BigDecimal("500000.00"), "TELLER", "test"))
                .thenReturn(new TransferProcedure.Result(1L, "MC-0000"));

        TransferResponse res = service.transfer("KEY-1", request);

        assertThat(res.txnId()).isEqualTo(1L);
        assertThat(res.txnRef()).isEqualTo("KEY-1");
        assertThat(res.replayed()).isFalse();
    }

    @Test
    void trungKey_traKetQuaLanDau_khongNemLoi() {
        when(procedure.internalTransfer(anyString(), anyString(), anyString(), any(), anyString(), any()))
                .thenReturn(new TransferProcedure.Result(1L, "MC-1006"));

        TransferResponse res = service.transfer("KEY-1", request);

        assertThat(res.txnId()).isEqualTo(1L);
        assertThat(res.replayed()).isTrue();
    }

    @Test
    void thieuSoDu_nemBusinessException_deRollback() {
        when(procedure.internalTransfer(anyString(), anyString(), anyString(), any(), anyString(), any()))
                .thenReturn(new TransferProcedure.Result(null, "MC-1004"));

        assertThatThrownBy(() -> service.transfer("KEY-2", request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_BALANCE);
    }

    @Test
    void maLa_coiLaLoiHeThong() {
        when(procedure.internalTransfer(anyString(), anyString(), anyString(), any(), anyString(), any()))
                .thenReturn(new TransferProcedure.Result(null, "XX-0000"));

        assertThatThrownBy(() -> service.transfer("KEY-3", request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SYSTEM_ERROR);
    }

    @Test
    void keyQuaDai_khongGoiXuongDb() {
        String longKey = "K".repeat(TransferService.MAX_KEY_LENGTH + 1);

        assertThatThrownBy(() -> service.transfer(longKey, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
        verify(procedure, never()).internalTransfer(anyString(), anyString(), anyString(), any(), anyString(), any());
    }
}
