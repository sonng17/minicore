INSERT INTO customer (cif_no, full_name, id_number, phone)
VALUES ('CIF0001', 'Nguyen Van A', '001201000001', '0900000001');
INSERT INTO customer (cif_no, full_name, id_number, phone)
VALUES ('CIF0002', 'Tran Thi B', '001201000002', '0900000002');

INSERT INTO account (account_no, customer_id, account_type, balance)
SELECT '1000000001', customer_id, 'CURRENT', 50000000 FROM customer WHERE cif_no = 'CIF0001';
INSERT INTO account (account_no, customer_id, account_type, balance)
SELECT '1000000002', customer_id, 'CURRENT', 10000000 FROM customer WHERE cif_no = 'CIF0002';
INSERT INTO account (account_no, customer_id, account_type, balance, status)
SELECT '1000000003', customer_id, 'CURRENT', 5000000, 'BLOCKED' FROM customer WHERE cif_no = 'CIF0002';

COMMIT;