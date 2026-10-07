# Nhật ký dùng AI

|Ngày|Việc|AI làm gì|Tao sửa / quyết định gì|
|-|-|-|-|
|06/10/2026|Đặc tả, README, DDL|Soạn bản nháp đặc tả, sơ đồ Mermaid, schema Oracle|Rà lại nghiệp vụ, chạy schema trên Oracle, kiểm tra dữ liệu mẫu|
|06/10/2026|PKG\_TRANSFER|Viết thân procedure (khóa theo thứ tự ID, savepoint khi trùng txn\_ref)|Chạy và kiểm 5 case test; sẽ tự viết lại để nắm chắc trước phỏng vấn|
|06/10/2026|Mã lỗi MC-9999|Đề xuất gán MC-9999 trong nhánh WHEN OTHERS trước RAISE|Kiểm tra lại: tham số OUT không về phía gọi khi có exception, nên chuyển việc gán MC-9999 lên tầng Java (PR #4)|
|06/10/2026|core-service (Spring Boot)|Viết toàn bộ code: entity, repository, service, controller, gọi PKG\_TRANSFER bằng SimpleJdbcCall, xử lý lỗi, unit test|Chạy mvn test 9/9 pass; chạy 5 case API qua Swagger khớp mong đợi (MC-0000, MC-1006 replayed, MC-1004, MC-1003, MC-1007). Sẽ tự đọc hiểu luồng Controller → Service → SimpleJdbcCall trước phỏng vấn|



