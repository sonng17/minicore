\# Đặc tả: Chuyển khoản nội bộ



\## Mô tả

Chuyển tiền giữa hai tài khoản cùng ngân hàng, hạch toán kép (tổng Nợ = tổng Có), thực hiện trong một transaction.



\## Đầu vào — `POST /v1/transfers`

| Trường | Kiểu | Bắt buộc | Ghi chú |

|---|---|---|---|

| Header `Idempotency-Key` | string ≤ 40 | Có | Lưu thành `txn\_ref`, chống trừ tiền trùng |

| fromAccount | string | Có | Số tài khoản nguồn |

| toAccount | string | Có | Số tài khoản đích |

| amount | decimal(19,2) | Có | > 0 |

| channel | enum | Có | TELLER / DIGITAL / PARTNER |

| description | string ≤ 500 | Không | |



\## Quy tắc nghiệp vụ

1\. Tài khoản nguồn khác tài khoản đích, cùng loại tiền.

2\. Cả hai tài khoản ở trạng thái ACTIVE.

3\. Số dư khả dụng = `balance - hold\_amount` phải ≥ `amount`.

4\. Khóa hai tài khoản theo thứ tự `account\_id` tăng dần để tránh deadlock khi có lệnh ngược chiều chạy đồng thời.

5\. Ghi 1 dòng `TXN` + 2 dòng `JOURNAL\_ENTRY` (D cho nguồn, C cho đích).

6\. Trùng `Idempotency-Key`: không trừ tiền lần hai, trả lại kết quả của lần đầu.

7\. Giao dịch trên 100.000.000 VND tại quầy cần kiểm soát viên duyệt (maker-checker) — triển khai ở giai đoạn sau.



\## Mã lỗi

| Mã | Ý nghĩa |

|---|---|

| MC-0000 | Thành công |

| MC-1001 | Tài khoản nguồn không tồn tại |

| MC-1002 | Tài khoản đích không tồn tại |

| MC-1003 | Tài khoản bị phong tỏa hoặc đã đóng |

| MC-1004 | Số dư khả dụng không đủ |

| MC-1005 | Số tiền không hợp lệ |

| MC-1006 | Trùng mã giao dịch |

| MC-1007 | Tài khoản nguồn trùng tài khoản đích |

| MC-1008 | Khác loại tiền |

| MC-9999 | Lỗi hệ thống (do tầng ứng dụng gán khi DB ném lỗi) |



\## Luồng xử lý



```mermaid

sequenceDiagram

&#x20; autonumber

&#x20; actor U as Khách hàng / Giao dịch viên

&#x20; participant GW as api-gateway

&#x20; participant PAY as payment-service

&#x20; participant CORE as core-service

&#x20; participant DB as Oracle PKG\_TRANSFER

&#x20; U->>GW: POST /v1/transfers kèm Idempotency-Key

&#x20; GW->>GW: Kiểm tra JWT, rate limit, gắn X-Correlation-Id

&#x20; GW->>PAY: Chuyển tiếp request

&#x20; PAY->>PAY: Kiểm tra Idempotency-Key

&#x20; alt Key đã tồn tại

&#x20;   PAY-->>U: Trả lại kết quả lần đầu

&#x20; else Key mới

&#x20;   PAY->>CORE: Yêu cầu chuyển nội bộ

&#x20;   CORE->>DB: internal\_transfer

&#x20;   DB->>DB: Khóa 2 tài khoản theo ID tăng dần

&#x20;   DB->>DB: Kiểm tra trạng thái, số dư

&#x20;   DB->>DB: Ghi TXN và 2 JOURNAL\_ENTRY

&#x20;   DB-->>CORE: txn\_id, MC-0000

&#x20;   CORE-->>PAY: SUCCESS

&#x20;   PAY->>PAY: Ghi sự kiện vào OUTBOX cùng transaction

&#x20;   PAY-->>U: 200 OK, response chuẩn

&#x20; end

```

