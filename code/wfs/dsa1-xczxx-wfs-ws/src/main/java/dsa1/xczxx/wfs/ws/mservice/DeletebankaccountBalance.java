package dsa1.xczxx.wfs.ws.mservice;

import dsa1.xczxx.wfs.ws.common.datalayer.DataActuator;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeletebankaccountBalance extends AbstractOperationServicePlugIn {
    private static final String flag = "produce";

    private static Log logger = LogFactory.getLog(DeletebankaccountBalance.class);

    @Override
    public void beginOperationTransaction(BeginOperationTransactionArgs e) {
        DynamicObject[] dataEntities = e.getDataEntities();
        if (dataEntities == null || dataEntities.length == 0) {
            logger.info("dataEntities is null");
            return;
        }
        Connection connection = DataActuator.getConnection();
        if (connection == null) {
            return;
        }
        Statement statement = null;
        try {
            connection.setAutoCommit(false);
             statement = connection.createStatement();
            for (DynamicObject dataEntitie : dataEntities) {
                statement.addBatch(deleteBalance(dataEntitie))   ;
            }
            int[] i =  statement.executeBatch();
            connection.commit();
            logger.info("executeBatch_largefund_bank_account_balance_detail delete end:" + flag);

        }catch (Exception exception){
            logger.error(exception.getMessage(),exception);
            try {
                connection.rollback();
            } catch (SQLException exception1) {
                logger.error(exception1.getMessage(),exception1);
            }
            throw new KDBizException(exception, new ErrorCode("DeletebankaccountBalance",   exception.getMessage()));


        }finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e1) {
                logger.error("finally:" + e1.getMessage(), e1);
            }
        }

    }




    private String deleteBalance(DynamicObject data) {
        StringBuilder sqlBuilder = new StringBuilder();
        String tablename;
        if ("produce".equals(flag)) {
            tablename = "largefund_bank_account_balance_detail";
        } else {
            tablename = "largefund_bank_account_balance_detail_test";
        }
        Date bizdate = (Date) data.get("bizdate");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        sqlBuilder.append("UPDATE ").append(tablename)
                .append(" SET data_state = '1', submit_time = '")
                .append(simpleDateFormat.format(new Date()))
                .append("' WHERE xh_id = ")
                .append(data.getLong("id"))
                .append(" AND balance_date = '")
                .append(bizdate == null ? "" : simpleDateFormat1.format(bizdate))
                .append("'");

        return sqlBuilder.toString();

    }



    private String deleteTrading(DynamicObject data) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer stringBuffer = new StringBuffer();
        String tableName=null;
        if ("produce".equals(flag)) {
            tableName="largefund_bank_account_trading";
        }else {
            tableName="largefund_bank_account_trading_test";
        }
        stringBuffer.append("update "+tableName+" set data_state = \"");
        stringBuffer.append("1");
        stringBuffer.append("\",");
        stringBuffer.append("submit_time = \"");
        stringBuffer.append(simpleDateFormat.format(new Date()));
        stringBuffer.append("\" where xh_id = \"");
        stringBuffer.append(data.getLong("id"));
        stringBuffer.append("\" and data_state=\"0\";");
        String SQL = stringBuffer.toString();
        logger.info("SQL1:"+SQL);
        return SQL;

    }



}




