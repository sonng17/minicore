package com.minicore.core.transfer;

import com.minicore.core.common.ApiResponse;
import com.minicore.core.common.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {
        TransferResponse result = transferService.transfer(idempotencyKey, request);
        ErrorCode code = result.replayed() ? ErrorCode.DUPLICATE_TRANSACTION : ErrorCode.SUCCESS;
        return ResponseEntity.status(code.httpStatus()).body(ApiResponse.of(code, result));
    }
}
