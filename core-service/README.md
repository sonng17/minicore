# core-service

Lõi ngân hàng của MiniCore: tra cứu khách hàng, tài khoản và chuyển khoản nội bộ. Spring Boot 3, Java 21, Oracle.

## API

| Method | Đường dẫn | Mô tả |
|---|---|---|
| GET | `/v1/customers/{cifNo}` | Thông tin khách hàng (che số giấy tờ) |
| GET | `/v1/customers/{cifNo}/accounts` | Danh sách tài khoản của khách hàng |
| GET | `/v1/accounts/{accountNo}` | Số dư, số tiền giữ, số dư khả dụng |
| POST | `/v1/transfers` | Chuyển khoản nội bộ, bắt buộc header `Idempotency-Key` |

Mọi response có cùng khung:

```json
{ "code": "MC-0000", "message": "Thành công", "data": { }, "traceId": "…", "timestamp": "…" }
```

Bảng mã lỗi: [docs/spec/internal-transfer.md](../docs/spec/internal-transfer.md).

## Cấu trúc code

```
common/     ApiResponse, ErrorCode, BusinessException, GlobalExceptionHandler, TraceIdFilter
customer/   Customer (entity) → CustomerRepository → CustomerService → CustomerController
account/    Account  (entity) → AccountRepository  → AccountService  → AccountController
transfer/   TransferController → TransferService → TransferProcedure (interface)
                                                 → OracleTransferProcedure (SimpleJdbcCall → PKG_TRANSFER)
```

## Các quyết định thiết kế

- **Chỉ PKG_TRANSFER được ghi tiền.** Java chỉ đọc số dư qua JPA; mọi thay đổi số dư đi qua thủ tục trong Oracle để hạch toán kép và khóa theo thứ tự nằm ở một chỗ.
- **Transaction do Java quản lý.** `TransferService.transfer` có `@Transactional`; thủ tục không commit. Khi thủ tục trả mã lỗi (ví dụ MC-1004), service ném `BusinessException` (RuntimeException) nên Spring rollback và nhả khóa `SELECT FOR UPDATE`.
- **Idempotency-Key = TXN_REF.** Gửi lại cùng key thì thủ tục trả MC-1006 và txnId cũ; API trả 200 với `replayed: true`, không trừ tiền lần hai.
- **MC-9999 gán ở tầng Java.** Khi thủ tục ném exception, tham số OUT không về được; Spring đổi `SQLException` thành `DataAccessException` và `GlobalExceptionHandler` trả MC-9999, không lộ lỗi SQL ra ngoài.
- **Tách `TransferProcedure` thành interface** để test `TransferService` bằng Mockito mà không cần Oracle.
- **`X-Correlation-Id`**: lấy từ header nếu có, không thì tự sinh; nằm trong log và trường `traceId`.

## Chạy

```bash
mvn test             # 9 unit test, không cần Oracle
mvn spring-boot:run  # cổng 8081, cần Oracle đang chạy (xem README gốc)
```

Biến môi trường (có giá trị mặc định cho máy local): `DB_URL`, `DB_USER`, `DB_PASSWORD`.

Swagger UI: http://localhost:8081/swagger-ui.html. Request mẫu cho 5 case kiểm thử: `requests.http`.

## Gợi ý đọc code

1. `transfer/TransferController` → `TransferService` → `OracleTransferProcedure`: đi theo một request chuyển khoản.
2. `common/GlobalExceptionHandler` + `ErrorCode`: lỗi nào ra HTTP status và mã nào.
3. `src/test/.../TransferServiceTest`, `TransferControllerTest`: các tình huống đã được kiểm.

## Chưa làm

Nộp/rút, sao kê, quản lý schema bằng Flyway trong app, integration test với Oracle thật (Testcontainers), test 200 lệnh chuyển song song.
