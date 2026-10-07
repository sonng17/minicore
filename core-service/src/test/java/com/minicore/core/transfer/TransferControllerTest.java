package com.minicore.core.transfer;

import com.minicore.core.common.BusinessException;
import com.minicore.core.common.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Test tầng web: validate đầu vào, mã lỗi và khung response chuẩn. Service được mock. */
@WebMvcTest(TransferController.class)
class TransferControllerTest {

    private static final String BODY = """
            {"fromAccount":"1000000001","toAccount":"1000000002","amount":500000,"channel":"TELLER"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @Test
    void thanhCong_tra200VaMC0000() throws Exception {
        when(transferService.transfer(eq("KEY-1"), any()))
                .thenReturn(new TransferResponse(1L, "KEY-1", "1000000001", "1000000002",
                        new BigDecimal("500000"), false));

        mockMvc.perform(post("/v1/transfers")
                        .header("Idempotency-Key", "KEY-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MC-0000"))
                .andExpect(jsonPath("$.data.txnId").value(1))
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void thieuIdempotencyKey_tra400() throws Exception {
        mockMvc.perform(post("/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MC-1000"));
    }

    @Test
    void soTienAm_tra400() throws Exception {
        mockMvc.perform(post("/v1/transfers")
                        .header("Idempotency-Key", "KEY-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccount":"1000000001","toAccount":"1000000002","amount":-1,"channel":"TELLER"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MC-1000"));
    }

    @Test
    void thieuSoDu_tra422VaMC1004() throws Exception {
        when(transferService.transfer(eq("KEY-3"), any()))
                .thenThrow(new BusinessException(ErrorCode.INSUFFICIENT_BALANCE));

        mockMvc.perform(post("/v1/transfers")
                        .header("Idempotency-Key", "KEY-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("MC-1004"));
    }
}
