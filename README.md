# MiniCore

Core banking thu nhỏ và lớp tích hợp API, viết bằng Java 21 + Spring Boot 3 + Oracle.

> **Trạng thái:** đang phát triển — tuần 2/8. Hiện có: đặc tả nghiệp vụ, thiết kế kiến trúc, CSDL Oracle, package PL/SQL chuyển khoản, chạy toàn bộ bằng `docker compose up`, core-service (Spring Boot) với API khách hàng, tài khoản và chuyển khoản nội bộ.

## Mục tiêu

Mô phỏng lõi ngân hàng (CIF khách hàng, tài khoản, hạch toán kép, chuyển khoản, tính lãi cuối ngày) và lớp tích hợp API cho kênh giao dịch tại quầy, kênh số và đối tác bên ngoài.

## Kiến trúc

```mermaid
flowchart TB
  teller["Teller Portal (React)"] --> gw
  partner["Đối tác thu hộ (SOAP)"] --> gw
  kc["Keycloak"] -. JWT .-> gw
  gw["api-gateway<br/>JWT · rate limit · correlation ID"] --> core
  gw --> pay
  gw --> adapter
  pay["payment-service<br/>idempotency · saga · outbox"] -->|REST| core
  pay --> adapter
  core["core-service<br/>CIF · tài khoản · bút toán"] --> ora[("Oracle<br/>PKG_TRANSFER · PKG_EOD")]
  pay -->|outbox| kafka[["Kafka: txn.events"]]
  kafka --> audit["notification-audit"] --> mongo[("MongoDB")]
  adapter["partner-adapter<br/>SOAP · Resilience4j"] -->|IBM MQ| napas["napas-simulator"]
```

Nguyên tắc: mọi lời gọi đi qua gateway; chỉ core-service được ghi tiền vào Oracle; partner-adapter cô lập mọi giao tiếp với bên ngoài để lỗi đối tác không lan vào lõi.

## Tech stack

| Lớp | Công nghệ |
|---|---|
| Ngôn ngữ / Framework | Java 21, Spring Boot 3, Spring Data JPA, Spring Cloud Gateway |
| CSDL | Oracle Database 23ai Free, PL/SQL, Flyway |
| NoSQL | MongoDB (audit log), Redis (cache, rate limit, idempotency) |
| Message | Kafka (sự kiện nội bộ), IBM MQ + JMS (liên ngân hàng) |
| Web service | REST (OpenAPI), SOAP (WSDL) |
| Bảo mật / Resilience | Keycloak (OAuth2/JWT), Resilience4j |
| Kiểm thử | JUnit 5, Mockito, Testcontainers, k6 |

## Cấu trúc repo

```
docs/spec/                                  Đặc tả nghiệp vụ + sequence diagram
docs/testing/                               Ghi chép kết quả kiểm thử
core-service/                               Spring Boot: API khách hàng, tài khoản, chuyển khoản
core-service/src/main/resources/db/migration Schema, dữ liệu mẫu, package PL/SQL (Flyway)
docker-compose.yml                          Chạy Oracle + core-service bằng một lệnh
```

## Chạy toàn bộ

Cần Docker Desktop. Không cần cài Java hay Maven để chạy.

```bash
docker compose up --build
```

Lần đầu mất vài phút (tải image, build). Khi core-service khởi động, Flyway tự tạo schema, nạp dữ liệu mẫu và biên dịch `PKG_TRANSFER`.

- Swagger UI: http://localhost:8081/swagger-ui.html
- Request mẫu cho 5 case: `core-service/requests.http`
- Kết nối DB bằng DBeaver: `localhost:1521`, service `FREEPDB1`, user `minicore` / `minicore123`

Dừng: `Ctrl+C` hoặc `docker compose stop` (giữ dữ liệu). Xóa sạch để chạy lại từ đầu: `docker compose down`.

## Phát triển core-service

Cần JDK 21 và Maven (IntelliJ có sẵn Maven).

```bash
docker compose up -d oracle   # chỉ chạy Oracle
cd core-service
mvn test                      # 9 unit test, không cần Oracle
mvn spring-boot:run           # cổng 8081, Flyway tự cập nhật schema
```

Chi tiết: [core-service/README.md](core-service/README.md).

## Quy ước

- Tiền: `NUMBER(19,2)` trong Oracle, `BigDecimal` trong Java — không dùng `double`.
- Mã lỗi dạng `MC-xxxx`, response chuẩn `{ code, message, data, traceId, timestamp }`.
- Git Flow: `feature/*` → Pull Request vào `develop` → cuối tuần merge vào `main` và gắn tag.

## Lộ trình

- [x] Tuần 1: đặc tả, kiến trúc, schema Oracle, `PKG_TRANSFER`
- [x] core-service: API khách hàng, tài khoản, chuyển nội bộ gọi `PKG_TRANSFER`, response và mã lỗi chuẩn, Swagger, unit test
- [x] Flyway quản lý schema trong app, Docker Compose chạy Oracle + core-service bằng một lệnh
- [ ] core-service: nộp/rút, sao kê, integration test với Oracle thật, test chuyển song song
- [ ] payment-service + api-gateway (Idempotency-Key, JWT, rate limit) — mốc MVP
- [ ] Kafka + outbox, notification-audit (MongoDB)
- [ ] partner-adapter: saga liên ngân hàng, Resilience4j, SOAP
- [ ] `PKG_EOD` tính lãi cuối ngày, IBM MQ, teller portal
- [ ] SIT tự động trong CI, load test k6
- [ ] Hoàn thiện tài liệu, ADR, video demo