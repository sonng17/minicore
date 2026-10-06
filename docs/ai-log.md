# Nhật ký dùng AI

| Ngày | Việc | AI làm gì | Tao sửa / quyết định gì |
|---|---|---|---|
| 06/10/2026 | Đặc tả, README, DDL | Soạn bản nháp đặc tả, sơ đồ Mermaid, schema Oracle | Rà lại nghiệp vụ, chạy schema trên Oracle, kiểm tra dữ liệu mẫu |
| 06/10/2026 | PKG_TRANSFER | Viết thân procedure (khóa theo thứ tự ID, savepoint khi trùng txn_ref) | Chạy và kiểm 5 case test; sẽ tự viết lại để nắm chắc trước phỏng vấn |
| 06/10/2026 | Mã lỗi MC-9999 | Đề xuất gán MC-9999 trong nhánh WHEN OTHERS trước RAISE | Kiểm tra lại: tham số OUT không về phía gọi khi có exception, nên chuyển việc gán MC-9999 lên tầng Java (PR #4) |
