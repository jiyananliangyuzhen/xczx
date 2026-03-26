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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BankAccountBalanceDetailRepairServicePlugIn extends AbstractOperationServicePlugIn {
    private static Log logger = LogFactory.getLog(BankAccountBalanceDetailRepairServicePlugIn.class);
//    private static final String flag="test";
        private static final String flag="produce";
    @Override
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        String operationKey = e.getOperationKey();
        if ("bankAccountbalancedetailrepair".equals(operationKey)) {
            HashMap<String, ArrayList<DynamicObject>> bankAccountBalanceDetailMap = getBankAccountBalanceDetailMap();
            HashMap<String, String> bankAccountBalanceMasterTabIdMap = getBankAccountBalanceMasterTabIdMap();
            Set<String> companynumberSet_xh = bankAccountBalanceDetailMap.keySet();
            Set<String> companynumberSet_zqJ = bankAccountBalanceMasterTabIdMap.keySet();
            if (companynumberSet_zqJ == null||companynumberSet_xh==null||companynumberSet_xh.isEmpty()||companynumberSet_zqJ.isEmpty()) {
                return;
            }
            Connection connection = DataActuator.getConnection();
            if (connection == null) {
                logger.error("connection is null");
                return;
            }
            try {
                Statement statement = connection.createStatement();
                HashSet<String> detailTabIdSet = getDetailTabId();
                Iterator<String> iterator = companynumberSet_xh.iterator();
                while (iterator.hasNext()){
                    String companynumber = iterator.next();
                    if (!companynumberSet_zqJ.contains(companynumber)) {
                        continue;
                    }
                    ArrayList<DynamicObject> bankAccountBalanceDetailList = bankAccountBalanceDetailMap.get(companynumber);
                    if (bankAccountBalanceDetailList == null||bankAccountBalanceDetailList.isEmpty()) {
                        continue;
                    }
                    for (DynamicObject bankAccountBalanceDetail:bankAccountBalanceDetailList) {
                        if (!detailTabIdSet.contains(bankAccountBalanceDetail.getString("id"))){
                           statement.addBatch(get_02_insert_sql(bankAccountBalanceDetail,bankAccountBalanceMasterTabIdMap.get(bankAccountBalanceDetail.getString("accountbank.bankaccountnumber"))));
                        }
                    }
                }
                statement.executeBatch();
                statement.close();
                connection.close();
            } catch (SQLException ex) {
                logger.error(ex.getMessage(),ex);
            }
        }
    }
    public HashMap<String,ArrayList<DynamicObject>> getBankAccountBalanceDetailMap(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = null;
        try {
            startDate = simpleDateFormat.parse("2024-07-07 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Date endDate = null;
        try {
            endDate = simpleDateFormat.parse("2024-07-23 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        QFilter qFilter = new QFilter("bank.finorgtype", QCP.equals, 615763944224700416L);
        QFilter  startDateFilter = new QFilter("bizdate", QCP.large_equals, startDate);
        QFilter  endDateFilter = new QFilter("bizdate", QCP.less_equals,endDate);
        qFilter=qFilter.and(startDateFilter).and(endDateFilter);
        DynamicObjectCollection query = QueryServiceHelper.query("bei_bankbalance",
                "id,company.number,bank.name,company.id,accountbank.bankaccountnumber,accountbank.bank,accountbank.name,accountbank.acctstyle,accountbank.acctstatus,accountbank.createtime,amount,bizdate",
                new QFilter[]{qFilter}, "modifytime desc");
        if (query == null||query.isEmpty()) {
            return null;
        }
        HashMap<String,ArrayList<DynamicObject>> bankAccountBalanceDetailMap = new HashMap<>();
        for (DynamicObject dynamicObject:query) {
//            String companynumber = dynamicObject.getString("company.number");
            String companynumber = dynamicObject.getString("accountbank.bankaccountnumber");
            if (bankAccountBalanceDetailMap.containsKey(companynumber)) {
                ArrayList<DynamicObject> bankAccountBalanceDetailList = bankAccountBalanceDetailMap.get(companynumber);
                bankAccountBalanceDetailList.add(dynamicObject);
            }else {
                ArrayList<DynamicObject> bankAccountBalanceDetailList = new ArrayList<>();
                bankAccountBalanceDetailList.add(dynamicObject);
            }
        }
        return bankAccountBalanceDetailMap;
    }
    private HashMap<String,String> getBankAccountBalanceMasterTabIdMap(){

        try {
            Connection connection = DataActuator.getConnection();
            if (connection == null) {
                return null;
            }
            Statement statement = connection.createStatement();
            String SQL=null;
            if("produce".equals(flag)){
                SQL="select master_tab_id,xh_master_tab_id from largefund_bank_account_balance;";
            }else {
                SQL="select master_tab_id,xh_master_tab_id from largefund_bank_account_balance_test;";
            }

            ResultSet resultSet = statement.executeQuery(SQL);
            if (resultSet != null) {
                HashMap<String, String> map = new HashMap<>();
                while (resultSet.next()){
                    map.put(resultSet.getString("xh_master_tab_id"),resultSet.getString("master_tab_id"));
                }
                statement.close();
                connection.close();
                return map;
            }
            return null;
        } catch (SQLException e) {
           logger.error(e.getMessage(),e);
        }
        return null;
    }
    private HashSet<String> getDetailTabId(){
        try {
            Connection connection = DataActuator.getConnection();
            if (connection == null) {
                return null;
            }
            Statement statement = connection.createStatement();
            String SQL=null;
            if("produce".equals(flag)){
                SQL="select detail_tab_id from largefund_bank_account_balance_detail;";
            }else {
                 SQL ="select detail_tab_id from largefund_bank_account_balance_detail_test;";
            }
            ResultSet resultSet = statement.executeQuery(SQL);
            if (resultSet != null) {
                HashSet set = new HashSet<>();
                while (resultSet.next()){
                  set.add(resultSet.getString("detail_tab_id"));
                }
                statement.close();
                connection.close();
                return set;
            }
            return null;
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }
    private static String get_02_insert_sql(DynamicObject data, String masterTabId) {
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date bizdate = (Date)data.get("bizdate");
        Date createtime = (Date)data.get("accountbank.createtime");
        String accountBankName = data.getString("bank.name");
        Object amount = data.get("amount");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("INSERT INTO largefund_bank_account_balance_detail (detail_tab_id,master_tab_id,acct_tit,acct_no,bank_br_name,bank_type,acct_open_date,acct_state,acct_bal,balance_date,if_accm_acct,remark,submit_time,acct_type) VALUES(");
        stringBuffer.append("\"");
        stringBuffer.append(data.getString("id"));
        stringBuffer.append("\",\"");
        stringBuffer.append(masterTabId);
        stringBuffer.append("\",\"");
        stringBuffer.append(data.getString("accountbank.name"));
        stringBuffer.append("\",\"");
        stringBuffer.append(data.getString("accountbank.bankaccountnumber"));
        stringBuffer.append("\",\"");
        stringBuffer.append(accountBankName);
        stringBuffer.append("\",\"");
        stringBuffer.append(DataActuator.getBankCode(accountBankName));
        stringBuffer.append("\",\"");
        stringBuffer.append((createtime == null) ? "" : simpleDateFormat1.format(createtime));
        stringBuffer.append("\",\"");
        stringBuffer.append("0");
        stringBuffer.append("\",\"");
        stringBuffer.append((amount == null) ? BigDecimal.ZERO.setScale(2, 4).toString() : ((BigDecimal)amount).setScale(2, 4).toString());
        stringBuffer.append("\",\"");
        stringBuffer.append((bizdate == null) ? "" : simpleDateFormat1.format(bizdate));
        stringBuffer.append("\",\"");
        stringBuffer.append("");
        stringBuffer.append("\",\"");
        stringBuffer.append("");
        stringBuffer.append("\",\"");
        stringBuffer.append(simpleDateFormat2.format(bizdate));
        stringBuffer.append("\",\"");
        stringBuffer.append("01");
        stringBuffer.append("\"");
        stringBuffer.append(");");
        return stringBuffer.toString();
    }
}
