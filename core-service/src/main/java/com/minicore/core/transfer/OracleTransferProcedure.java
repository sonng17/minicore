package com.minicore.core.transfer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Map;

/**
 * Gọi PKG_TRANSFER.INTERNAL_TRANSFER bằng SimpleJdbcCall.
 *
 * - Khai báo tham số tường minh, đúng thứ tự trong package spec, và tắt việc đọc
 *   metadata để không phụ thuộc quyền xem từ điển dữ liệu của Oracle.
 * - Thủ tục KHÔNG commit: transaction do TransferService (@Transactional) quản lý.
 * - Nếu thủ tục ném exception, Spring đổi SQLException thành DataAccessException;
 *   GlobalExceptionHandler trả MC-9999 (tham số OUT không về được trong trường hợp này).
 */
@Repository
public class OracleTransferProcedure implements TransferProcedure {

    private static final String P_TXN_REF = "P_TXN_REF";
    private static final String P_FROM_ACC = "P_FROM_ACC";
    private static final String P_TO_ACC = "P_TO_ACC";
    private static final String P_AMOUNT = "P_AMOUNT";
    private static final String P_CHANNEL = "P_CHANNEL";
    private static final String P_DESCRIPTION = "P_DESCRIPTION";
    private static final String P_TXN_ID = "P_TXN_ID";
    private static final String P_RESULT_CODE = "P_RESULT_CODE";

    private final SimpleJdbcCall call;

    public OracleTransferProcedure(JdbcTemplate jdbcTemplate) {
        this.call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_TRANSFER")
                .withProcedureName("INTERNAL_TRANSFER")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter(P_TXN_REF, Types.VARCHAR),
                        new SqlParameter(P_FROM_ACC, Types.VARCHAR),
                        new SqlParameter(P_TO_ACC, Types.VARCHAR),
                        new SqlParameter(P_AMOUNT, Types.NUMERIC),
                        new SqlParameter(P_CHANNEL, Types.VARCHAR),
                        new SqlParameter(P_DESCRIPTION, Types.VARCHAR),
                        new SqlOutParameter(P_TXN_ID, Types.NUMERIC),
                        new SqlOutParameter(P_RESULT_CODE, Types.VARCHAR));
    }

    @Override
    public Result internalTransfer(String txnRef, String fromAccount, String toAccount,
                                   BigDecimal amount, String channel, String description) {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue(P_TXN_REF, txnRef)
                .addValue(P_FROM_ACC, fromAccount)
                .addValue(P_TO_ACC, toAccount)
                .addValue(P_AMOUNT, amount)
                .addValue(P_CHANNEL, channel)
                .addValue(P_DESCRIPTION, description);

        Map<String, Object> out = call.execute(in);

        Number txnId = (Number) get(out, P_TXN_ID);
        String resultCode = (String) get(out, P_RESULT_CODE);
        return new Result(txnId == null ? null : txnId.longValue(), resultCode);
    }

    /** Lấy giá trị OUT không phân biệt hoa thường tên tham số. */
    private static Object get(Map<String, Object> out, String name) {
        for (Map.Entry<String, Object> e : out.entrySet()) {
            if (e.getKey().equalsIgnoreCase(name)) {
                return e.getValue();
            }
        }
        return null;
    }
}
