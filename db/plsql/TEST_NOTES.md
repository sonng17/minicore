\# Kết quả test PKG\_TRANSFER



Môi trường: Oracle Database 23ai Free (Docker `gvenzl/oracle-free:23-slim-faststart`), dữ liệu mẫu từ `V2\_\_seed\_data.sql`.



| # | Case | Mong đợi | Thực tế |

|---|---|---|---|

| 1 | Chuyển 500.000 từ ...001 sang ...002 | MC-0000, số dư 49.500.000 / 10.500.000, 2 bút toán D/C | MC-0000, txn\_id=1 ✅ |

| 2 | Chạy lại TEST-001 | MC-1006, trả txn\_id cũ, số dư không đổi | MC-1006, txn\_id=1 ✅ |

| 3 | Chuyển 999.000.000 | MC-1004 | MC-1004 ✅ |

| 4 | Chuyển sang ...003 (BLOCKED) | MC-1003 | MC-1003 ✅ |

| 5 | Nguồn trùng đích | MC-1007 | MC-1007 ✅ |



Kiểm tra sau test:

\- Số dư: 1000000001 = 49.500.000; 1000000002 = 10.500.000; 1000000003 = 5.000.000 (không đổi).

\- `journal\_entry`: đúng 2 dòng cho txn\_id=1 (D tài khoản 1, C tài khoản 2), tổng Nợ = tổng Có.



Chưa test: 2 request cùng `txn\_ref` chạy song song và 200 lệnh chuyển đồng thời (làm bằng Testcontainers ở tuần 2).

