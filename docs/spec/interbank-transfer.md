\# Đặc tả: Chuyển khoản liên ngân hàng (saga)



\## Nguyên tắc

\- Không dùng 2PC vì không kiểm soát được hệ thống đối tác; dùng saga: giữ tiền → gửi đối tác → trừ thật hoặc nhả giữ.

\- Timeout khi gọi đối tác \*\*không\*\* được hiểu là thất bại: chuyển sang trạng thái UNKNOWN, giữ nguyên hold, không tự hoàn tiền.

\- Chỉ retry lệnh hỏi trạng thái, không retry lệnh ghi tiền.



\## Trạng thái

`PENDING → SUCCESS | FAILED | UNKNOWN`; UNKNOWN được giải quyết bằng hỏi lại trạng thái hoặc đối soát cuối ngày.



\## Luồng xử lý



```mermaid

sequenceDiagram

&#x20; autonumber

&#x20; participant PAY as payment-service

&#x20; participant CORE as core-service

&#x20; participant AD as partner-adapter

&#x20; participant NP as napas-simulator

&#x20; PAY->>CORE: Giữ tiền tài khoản nguồn

&#x20; CORE-->>PAY: Giữ tiền thành công

&#x20; PAY->>AD: Gửi lệnh liên ngân hàng

&#x20; AD->>NP: Request, timeout 3 giây

&#x20; alt Đối tác xác nhận thành công

&#x20;   NP-->>AD: OK

&#x20;   AD-->>PAY: SUCCESS

&#x20;   PAY->>CORE: Trừ tiền thật từ khoản giữ

&#x20; else Đối tác báo lỗi

&#x20;   NP-->>AD: Lỗi nghiệp vụ

&#x20;   AD-->>PAY: FAILED

&#x20;   PAY->>CORE: Nhả khoản giữ (bù trừ)

&#x20; else Hết thời gian chờ

&#x20;   AD-->>PAY: UNKNOWN

&#x20;   PAY->>PAY: Giữ nguyên khoản giữ, không tự hoàn tiền

&#x20;   Note over PAY,NP: Hỏi lại trạng thái hoặc đối soát cuối ngày

&#x20; end

```

