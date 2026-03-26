package dsa1.xczxx.wfs.ws.mservice;

import dsa1.xczxx.wfs.ws.common.datalayer.DataActuator;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

public class DeletebankaccountBalanceServicePlugIn  extends AbstractOperationServicePlugIn {
    private static Log logger = LogFactory.getLog(DeletebankaccountBalanceServicePlugIn.class);
//    private static final String flag="test";
    private static final String flag="produce";
    @Override
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        DynamicObject[] dataEntities = e.getDataEntities();
        if (dataEntities == null||dataEntities.length==0) {
            logger.info("dataEntities is null");
            return;
        }
        ArrayList<Long> idList = new ArrayList<>();
        for (DynamicObject dataEntitie:dataEntities) {
            idList.add(dataEntitie.getLong("id"));
        }
        String operationKey = e.getOperationKey();
        switch (operationKey){
            case "deletebankaccountbalance":
                deleteBankAccountBalanceDetail(idList);
                break;
            case "deletebankaccounttrading":
                deleteBankAccountTrading(idList);
                break;
            default:

        }
    }

    /**
     * largefund_bank_account_balance: data_state 修改 1 submit_time 更新成最新的
     * largefund_bank_account_balance: 新增
     * largefund_bank_account_balance_detail : delete 相关的记录
     * @param idList
     */
    private static void deleteBankAccountBalanceDetail( ArrayList<Long> idList){
        HashMap<String, HashSet<String>> bankAccountBalanceMap = getBankAccountBalanceMap(idList);
        if (bankAccountBalanceMap == null) {
            logger.error("bankAccountBalanceMap is null");
            return;
        }
        Set<String> keyset = bankAccountBalanceMap.keySet();
        HashSet<String> companyNumberSet = new HashSet<>(keyset);
        HashMap<String, String> bankAccountBalanceQuerys = DataActuator.query("largefund_bank_account_balance", companyNumberSet, "xh_master_tab_id,master_tab_id", "xh_master_tab_id");
        if (bankAccountBalanceQuerys == null||bankAccountBalanceQuerys.isEmpty()) {
            logger.error("bankAccountBalanceQuerys is null");
            return;
        }
        Set<String> keySet = bankAccountBalanceQuerys.keySet();//需要删除的数据
        Iterator<String> iterator = keySet.iterator();
        Connection connection = DataActuator.getConnection();
        try {
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            if (connection == null) {
                return;
            }
            String tableName=null;
            if ("produce".equals(flag)) {
                tableName="largefund_bank_account_balance";
            }else {
                tableName="largefund_bank_account_balance_test";
            }
            while (iterator.hasNext()){
                String companyNumber = iterator.next();
                HashSet<String> deleteBankAccountBalanceDetailIdSet = bankAccountBalanceMap.get(companyNumber);//需要删除的余额明细id
                logger.info("deleteBankAccountBalanceDetailIdSet:"+deleteBankAccountBalanceDetailIdSet);

                HashMap<String, String> deletequery = DataActuator.query("largefund_bank_account_balance_detail", deleteBankAccountBalanceDetailIdSet, "detail_tab_id,xh_id", "xh_id");
                if (deletequery == null||deletequery.isEmpty()) {
                    logger.info("deleteBankAccountBalanceDetailIdSet:"+deleteBankAccountBalanceDetailIdSet+"已经标注成删除");
                    continue;
                }
                String master_tab_id = bankAccountBalanceQuerys.get(companyNumber);
                String selectSQL="select submit_unit_code,submit_unit_name,data_year,data_month,data_date,submitter,remark,group_code,group_name,xh_master_tab_id,submit_time " +
                        "from "+tableName+" where master_tab_id = \""+master_tab_id+"\" and data_state =\"0\";";
                ResultSet resultSet = statement.executeQuery(selectSQL);
                if (resultSet == null) {
                    continue;
                }
                HashMap<String, Object> bankAccountBalancemap = new HashMap<>();
                while (resultSet.next()){
                    if (!bankAccountBalancemap.isEmpty()) {
                        continue;
                    }
                    bankAccountBalancemap.put("submit_unit_code",resultSet.getString("submit_unit_code"));
                    bankAccountBalancemap.put("submit_unit_name",resultSet.getString("submit_unit_name"));
                    bankAccountBalancemap.put("data_year",resultSet.getString("data_year"));
                    bankAccountBalancemap.put("data_month",resultSet.getString("data_month"));
                    bankAccountBalancemap.put("data_date",resultSet.getString("data_date"));
                    bankAccountBalancemap.put("submitter",resultSet.getString("submitter"));
                    bankAccountBalancemap.put("remark",resultSet.getString("remark"));
                    bankAccountBalancemap.put("group_code",resultSet.getString("group_code"));
                    bankAccountBalancemap.put("group_name",resultSet.getString("group_name"));
                    bankAccountBalancemap.put("submit_time",resultSet.getString("submit_time"));
                    bankAccountBalancemap.put("xh_master_tab_id",resultSet.getString("xh_master_tab_id"));
                }
                //新增余额主表数据
                String new_master_tab_id = DataActuator.getID();
                statement.execute(get_insert_largefund_bank_account_balance_sql(bankAccountBalancemap,new_master_tab_id));
                //旧数据 data_state=1 submit_time
                statement.execute(get_update_largefund_bank_account_balance_sql(master_tab_id));
                //新增余额明细数据（剔除删除的）
                ArrayList<HashMap<String, Object>> bank_account_balance_detail_list_old = query_largefund_bank_account_balance_detail(master_tab_id);
                if (bank_account_balance_detail_list_old.isEmpty()) {
                    logger.error("bank_account_balance_detail_list_old is null");
                    continue;
                }
                ArrayList<HashMap<String, Object>> mapList = new ArrayList<>();
                for (HashMap<String, Object> bank_account_balance_detail_map:bank_account_balance_detail_list_old) {
                    String xh_id = bank_account_balance_detail_map.get("xh_id").toString();
                    logger.info("xh_id:"+xh_id);
                    if(!deleteBankAccountBalanceDetailIdSet.contains(xh_id)){
                        statement.addBatch(get_insert_largefund_bank_account_balance_detail_sql(bank_account_balance_detail_map,new_master_tab_id));
                    }else {
                        mapList.add(bank_account_balance_detail_map);
                    }
                }
                statement.executeBatch();
                if (mapList == null) {
                    logger.error("hashMaps is null");
                    continue;
                }
                for (HashMap<String,Object> map:mapList) {
                    statement.addBatch(get_upate_largefund_bank_account_balance_detail_sql(map,master_tab_id));
                }
                statement.executeBatch();
                connection.commit();
            }
        }catch (Exception exception){
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error(e.getMessage(),e);
            }
            logger.error(exception.getMessage(),exception);
        }
    }

    private static String get_upate_largefund_bank_account_balance_detail_sql(HashMap<String, Object> map,String master_tab_id) {
        StringBuffer stringBuffer = new StringBuffer();
        String tableName=null;
        if ("produce".equals(flag)) {
            tableName="largefund_bank_account_balance_detail";
        }else {
            tableName="largefund_bank_account_balance_detail_test";
        }
        stringBuffer.append("update "+tableName+" ");
        stringBuffer.append("set data_state = \"1\"");
        stringBuffer.append(" where xh_id =\"");
        stringBuffer.append(map.get("xh_id"));
        stringBuffer.append("\" and master_tab_id = \"");
        stringBuffer.append(master_tab_id);
        stringBuffer.append("\";");
        String SQl=stringBuffer.toString();
        logger.info("SQl:"+SQl);
        return SQl;
    }

    private static String get_insert_largefund_bank_account_balance_detail_sql(HashMap<String,Object> bank_account_balance_detail_map,String new_master_tab_id){
        StringBuffer stringBuffer = new StringBuffer();
        String tableName=null;
        if ("produce".equals(flag)) {
            tableName="largefund_bank_account_balance_detail";
        }else {
            tableName="largefund_bank_account_balance_detail_test";
        }
        stringBuffer.append("INSERT INTO "+tableName+" " +
                "(detail_tab_id,master_tab_id,xh_id,acct_tit,acct_no,bank_br_name,bank_type,acct_open_date," +
                "acct_state,acct_bal,balance_date,if_accm_acct,remark,submit_time,acct_type,data_state) VALUES(");
        stringBuffer.append("\"");
        stringBuffer.append(DataActuator.getID());
        stringBuffer.append("\",\"");
        stringBuffer.append(new_master_tab_id);
        stringBuffer.append("\",\"");
        stringBuffer.append(bank_account_balance_detail_map.get("xh_id"));
        stringBuffer.append("\",\"");
        stringBuffer.append(bank_account_balance_detail_map.get("acct_tit"));
        stringBuffer.append("\",\"");
        stringBuffer.append(bank_account_balance_detail_map.get("acct_no"));
        stringBuffer.append("\",\"");
        stringBuffer.append(bank_account_balance_detail_map.get("bank_br_name"));
        stringBuffer.append("\",\"");
        stringBuffer.append(bank_account_balance_detail_map.get("bank_type"));
        stringBuffer.append("\",\"");
        stringBuffer.append(bank_account_balance_detail_map.get("acct_open_date"));
        stringBuffer.append("\",\"");
        stringBuffer.append(bank_account_balance_detail_map.get("acct_state"));
        stringBuffer.append("\",\"");
        stringBuffer.append(bank_account_balance_detail_map.get("acct_bal"));
        stringBuffer.append("\",\"");
        stringBuffer.append(bank_account_balance_detail_map.get("balance_date"));
        stringBuffer.append("\",\"");
        stringBuffer.append(bank_account_balance_detail_map.get("if_accm_acct"));
        stringBuffer.append("\",\"");
        stringBuffer.append(bank_account_balance_detail_map.get("remark"));
        stringBuffer.append("\",\"");
        stringBuffer.append(bank_account_balance_detail_map.get("submit_time"));
        stringBuffer.append("\",\"");
        stringBuffer.append(bank_account_balance_detail_map.get("acct_type"));
        stringBuffer.append("\",\"");
        stringBuffer.append("0");
        stringBuffer.append("\");");
        String SQL = stringBuffer.toString();
        return SQL;
    }
    private static String get_insert_largefund_bank_account_balance_sql(HashMap<String, Object> map,String new_master_tab_id) {
        StringBuffer stringBuffer = new StringBuffer();
        String tableName=null;
        if ("produce".equals(flag)) {
            tableName="largefund_bank_account_balance";
        }else {
            tableName="largefund_bank_account_balance_test";
        }
        stringBuffer.append("INSERT INTO "+tableName+" (" +
                "master_tab_id," +
                "submit_unit_code," +
                "submit_unit_name," +
                "data_year," +
                "data_month," +
                "data_date," +
                "submitter," +
                "submit_time," +
                "remark," +
                "group_code," +
                "group_name," +
                "xh_master_tab_id," +
                "data_state) " +
                "VALUES (");
        stringBuffer.append("\"");
        stringBuffer.append(new_master_tab_id);
        stringBuffer.append("\",\"");
        stringBuffer.append(map.get("submit_unit_code"));
        stringBuffer.append("\",\"");
        stringBuffer.append(map.get("submit_unit_name"));
        stringBuffer.append("\",\"");
        stringBuffer.append(map.get("data_year"));
        stringBuffer.append("\",\"");
        stringBuffer.append(map.get("data_month"));
        stringBuffer.append("\",\"");
        stringBuffer.append(map.get("data_date"));
        stringBuffer.append("\",\"");
        stringBuffer.append(map.get("submitter"));
        stringBuffer.append("\",\"");
        stringBuffer.append(map.get("submit_time"));
        stringBuffer.append("\",\"");
        stringBuffer.append(map.get("remark"));
        stringBuffer.append("\",\"");
        stringBuffer.append(map.get("group_code"));
        stringBuffer.append("\",\"");
        stringBuffer.append(map.get("group_name"));
        stringBuffer.append("\",\"");
        stringBuffer.append(map.get("xh_master_tab_id"));
        stringBuffer.append("\",\"");
        stringBuffer.append("0");
        stringBuffer.append("\"");
        stringBuffer.append(");");
        String SQL=stringBuffer.toString();
        logger.info("SQL:"+SQL);
        return SQL;
    }
    private static ArrayList<HashMap<String,Object>> query_largefund_bank_account_balance_detail(String master_tab_id) throws SQLException {
        ArrayList<HashMap<String,Object>> bankAccountBalanceDetailList = new ArrayList();
        Connection connection = DataActuator.getConnection();
        if (connection == null) {
            logger.error("connection is null");
            return bankAccountBalanceDetailList ;
        }
        Statement statement = connection.createStatement();
        if (statement == null) {
            logger.error("statement is null");
            return bankAccountBalanceDetailList;
        }
        String tableName=null;
        if ("produce".equals(flag)) {
            tableName="largefund_bank_account_balance_detail";
        }else {
            tableName="largefund_bank_account_balance_detail_test";
        }
        String SQL="select detail_tab_id,acct_tit,acct_no,bank_br_name,bank_type,acct_open_date,acct_state,acct_bal,balance_date,if_accm_acct,remark,submit_time,acct_type,xh_id" +
                   " from "+tableName+" where master_tab_id = \"" +master_tab_id+"\" and data_state = \"0\";";
        ResultSet resultSet = statement.executeQuery(SQL);
        if (resultSet == null) {
            logger.error("resultSet is null");
            return bankAccountBalanceDetailList;
        }
        while (resultSet.next()){
            HashMap<String, Object> map = new HashMap<>();
            map.put("acct_tit", resultSet.getString("acct_tit"));
            map.put("acct_no", resultSet.getString("acct_no"));
            map.put("bank_br_name", resultSet.getString("bank_br_name"));
            map.put("bank_type", resultSet.getString("bank_type"));
            map.put("acct_open_date", resultSet.getString("acct_open_date"));
            map.put("acct_state", resultSet.getString("acct_state"));
            map.put("acct_bal", resultSet.getString("acct_bal"));
            map.put("balance_date", resultSet.getString("balance_date"));
            map.put("if_accm_acct", resultSet.getString("if_accm_acct"));
            map.put("remark", resultSet.getString("remark"));
            map.put("submit_time", resultSet.getString("submit_time"));
            map.put("acct_type", resultSet.getString("acct_type"));
            map.put("xh_id", resultSet.getString("xh_id"));
            map.put("detail_tab_id", resultSet.getString("detail_tab_id"));
            bankAccountBalanceDetailList.add(map);
        }
        return bankAccountBalanceDetailList;
    }
    private static String get_update_largefund_bank_account_balance_sql(String master_tab_id){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        String tableName=null;
        if ("produce".equals(flag)) {
            tableName="largefund_bank_account_balance";
        }else {
            tableName="largefund_bank_account_balance_test";
        }
        String SQL= "update "+tableName+" set " +
                    "data_state =\"1\"," +
                    "submit_time = \""+
                    format+"\""+
                    " where master_tab_id =\""+master_tab_id+"\";";
        logger.info("SQL:"+SQL);
        return SQL;
    }
    private static String get_update_largefund_bank_account_trading_sql(Long id){
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
        stringBuffer.append(id);
        stringBuffer.append("\" and data_state=\"0\";");
        String SQL = stringBuffer.toString();
        logger.info("SQL1:"+SQL);
        return SQL;
    }
    /**
     * largefund_bank_account_trading : data_state 修改 1 submit_time 更新成最新的
     * @param idList
     */
    private static void deleteBankAccountTrading( ArrayList<Long> idList){
        Connection connection = DataActuator.getConnection();
        if (connection == null) {
            logger.error("connection is null");
            return;
        }
        try {
            Statement statement = connection.createStatement();
            if (idList == null||idList.isEmpty()) {
                logger.error("idList is null");
                return;
            }
            for (Long id:idList) {
                statement.addBatch(get_update_largefund_bank_account_trading_sql(id));
            }
            statement.executeBatch();
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
    }
    private static HashMap<String, HashSet<String>> getBankAccountBalanceMap(ArrayList<Long> idList){
        DynamicObjectCollection bankbalanceCollection = QueryServiceHelper.query("bei_bankbalance", "id,accountbank.bankaccountnumber",
                new QFilter[]{new QFilter("id", QCP.in,idList)});
        if (bankbalanceCollection == null||bankbalanceCollection.isEmpty()) {
            return null;
        }
        HashMap<String, HashSet<String>> bankAccountBalanceMap = new HashMap<>();
        for (DynamicObject bankbalance:bankbalanceCollection) {
//            String companyNumber = bankbalance.getString("company.number");
            String companyNumber = bankbalance.getString("accountbank.bankaccountnumber");
            if (bankAccountBalanceMap.containsKey(companyNumber)) {
                HashSet<String> IdSet = bankAccountBalanceMap.get(companyNumber);
                IdSet.add(bankbalance.getString("id"));
                bankAccountBalanceMap.put(companyNumber,IdSet);
            }else {
                HashSet<String> IdSet = new HashSet<>();
                IdSet.add(bankbalance.getString("id"));
                bankAccountBalanceMap.put(companyNumber,IdSet);
            }
        }
        return bankAccountBalanceMap;
    }
}
