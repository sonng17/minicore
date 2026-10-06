# Đặc tả: Chuyển khoản liên ngân hàng (saga)

## Nguyên tắc
- Không dùng 2PC vì không kiểm soát được hệ thống đối tác; dùng saga: giữ tiền → gửi đối tác → trừ thật hoặc nhả giữ.
- Timeout khi gọi đối tác **không** được hiểu là thất bại: chuyển sang trạng thái UNKNOWN, giữ nguyên hold, không tự hoàn tiền.
- Chỉ retry lệnh hỏi trạng thái, không retry lệnh ghi tiền.

## Trạng thái
`PENDING → SUCCESS | FAILED | UNKNOWN`; UNKNOWN được giải quyết bằng hỏi lại trạng thái hoặc đối soát cuối ngày.

## Luồng xử lý

```mermaid
sequenceDiagram
  autonumber
  participant PAY as payment-service
  participant CORE as core-service
  participant AD as partner-adapter
  participant NP as napas-simulator
  PAY->>CORE: Giữ tiền tài khoản nguồn
  CORE-->>PAY: Giữ tiền thành công
  PAY->>AD: Gửi lệnh liên ngân hàng
  AD->>NP: Request, timeout 3 giây
  alt Đối tác xác nhận thành công
    NP-->>AD: OK
    AD-->>PAY: SUCCESS
    PAY->>CORE: Trừ tiền thật từ khoản giữ
  else Đối tác báo lỗi
    NP-->>AD: Lỗi nghiệp vụ
    AD-->>PAY: FAILED
    PAY->>CORE: Nhả khoản giữ (bù trừ)
  else Hết thời gian chờ
    AD-->>PAY: UNKNOWN
    PAY->>PAY: Giữ nguyên khoản giữ, không tự hoàn tiền
    Note over PAY,NP: Hỏi lại trạng thái hoặc đối soát cuối ngày
  end
```
