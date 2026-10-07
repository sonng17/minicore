CREATE OR REPLACE PACKAGE pkg_transfer AS
  PROCEDURE internal_transfer (
    p_txn_ref     IN  VARCHAR2,
    p_from_acc    IN  VARCHAR2,
    p_to_acc      IN  VARCHAR2,
    p_amount      IN  NUMBER,
    p_channel     IN  VARCHAR2,
    p_description IN  VARCHAR2,
    p_txn_id      OUT NUMBER,
    p_result_code OUT VARCHAR2
  );
END pkg_transfer;
/

CREATE OR REPLACE PACKAGE BODY pkg_transfer AS

  PROCEDURE internal_transfer (
    p_txn_ref     IN  VARCHAR2,
    p_from_acc    IN  VARCHAR2,
    p_to_acc      IN  VARCHAR2,
    p_amount      IN  NUMBER,
    p_channel     IN  VARCHAR2,
    p_description IN  VARCHAR2,
    p_txn_id      OUT NUMBER,
    p_result_code OUT VARCHAR2
  ) IS
    v_from_id    account.account_id%TYPE;
    v_to_id      account.account_id%TYPE;
    v_from       account%ROWTYPE;
    v_to         account%ROWTYPE;
    v_from_after account.balance%TYPE;
    v_to_after   account.balance%TYPE;
  BEGIN
    p_txn_id := NULL;

    -- Đánh dấu điểm quay lui: nếu lỗi giữa chừng thì hoàn tác mọi thay đổi
    -- của procedure này mà không đụng tới phần việc khác của phía gọi.
    SAVEPOINT sp_internal_transfer;

    -- 1. Validate đầu vào (chưa cần đọc DB)
    IF p_amount IS NULL OR p_amount <= 0 OR p_amount <> ROUND(p_amount, 2) THEN
      p_result_code := 'MC-1005';
      RETURN;
    END IF;

    IF p_from_acc = p_to_acc THEN
      p_result_code := 'MC-1007';
      RETURN;
    END IF;

    -- 2. Trùng txn_ref: trả lại txn_id cũ, không trừ tiền lần hai
    BEGIN
      SELECT txn_id INTO p_txn_id FROM txn WHERE txn_ref = p_txn_ref;
      p_result_code := 'MC-1006';
      RETURN;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN NULL;  -- chưa có, đi tiếp
    END;

    -- 3. Tra account_id từ số tài khoản, CHƯA khóa
    BEGIN
      SELECT account_id INTO v_from_id FROM account WHERE account_no = p_from_acc;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN p_result_code := 'MC-1001'; RETURN;
    END;

    BEGIN
      SELECT account_id INTO v_to_id FROM account WHERE account_no = p_to_acc;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN p_result_code := 'MC-1002'; RETURN;
    END;

    -- 4. Khóa 2 tài khoản theo thứ tự account_id tăng dần.
    --    Lệnh A->B và lệnh B->A chạy cùng lúc đều khóa id nhỏ trước,
    --    nên không bao giờ mỗi bên giữ một khóa rồi chờ nhau (deadlock).
    IF v_from_id < v_to_id THEN
      SELECT * INTO v_from FROM account WHERE account_id = v_from_id FOR UPDATE;
      SELECT * INTO v_to   FROM account WHERE account_id = v_to_id   FOR UPDATE;
    ELSE
      SELECT * INTO v_to   FROM account WHERE account_id = v_to_id   FOR UPDATE;
      SELECT * INTO v_from FROM account WHERE account_id = v_from_id FOR UPDATE;
    END IF;

    -- 5. Kiểm tra trạng thái và loại tiền (sau khi khóa, dữ liệu đã chắc chắn mới nhất)
    IF v_from.status <> 'ACTIVE' OR v_to.status <> 'ACTIVE' THEN
      p_result_code := 'MC-1003';
      RETURN;
    END IF;

    IF v_from.currency <> v_to.currency THEN
      p_result_code := 'MC-1008';
      RETURN;
    END IF;

    -- 6. Số dư khả dụng = balance - hold_amount
    IF v_from.balance - v_from.hold_amount < p_amount THEN
      p_result_code := 'MC-1004';
      RETURN;
    END IF;

    -- 7. Trừ nguồn, cộng đích.
    --    Tăng version để nếu Java (JPA) đang giữ bản Account cũ thì không ghi đè số dư.
    v_from_after := v_from.balance - p_amount;
    v_to_after   := v_to.balance   + p_amount;

    UPDATE account SET balance = v_from_after, version = version + 1
     WHERE account_id = v_from_id;
    UPDATE account SET balance = v_to_after,   version = version + 1
     WHERE account_id = v_to_id;

    -- 8. Ghi header giao dịch
    INSERT INTO txn (txn_ref, txn_type, amount, fee, currency, status, channel, description)
    VALUES (p_txn_ref, 'INTERNAL_TRANSFER', p_amount, 0, v_from.currency,
            'SUCCESS', p_channel, p_description)
    RETURNING txn_id INTO p_txn_id;

    -- 9. Hạch toán kép: 1 dòng Nợ (nguồn), 1 dòng Có (đích), tổng Nợ = tổng Có
    INSERT INTO journal_entry (txn_id, account_id, dr_cr, amount, balance_after)
    VALUES (p_txn_id, v_from_id, 'D', p_amount, v_from_after);
    INSERT INTO journal_entry (txn_id, account_id, dr_cr, amount, balance_after)
    VALUES (p_txn_id, v_to_id,   'C', p_amount, v_to_after);

    -- 10. Thành công. KHÔNG COMMIT: transaction do phía gọi (Java) quản lý.
    p_result_code := 'MC-0000';

  EXCEPTION
    -- Hai request cùng txn_ref lọt qua bước 2 cùng lúc: request sau đã UPDATE
    -- số dư rồi mới đụng unique constraint ở bước 8. Phải quay lui về savepoint,
    -- nếu không tiền đã chuyển mà không có TXN nào.
    WHEN DUP_VAL_ON_INDEX THEN
      ROLLBACK TO sp_internal_transfer;
      SELECT txn_id INTO p_txn_id FROM txn WHERE txn_ref = p_txn_ref;
      p_result_code := 'MC-1006';

    -- Lỗi không lường trước: hoàn tác rồi ném lỗi lên.
    -- Không gán p_result_code ở đây vì OUT param không về được khi có exception;
    -- tầng Java bắt SQLException và trả MC-9999.
    WHEN OTHERS THEN
      ROLLBACK TO sp_internal_transfer;
      RAISE;
  END internal_transfer;

END pkg_transfer;
/