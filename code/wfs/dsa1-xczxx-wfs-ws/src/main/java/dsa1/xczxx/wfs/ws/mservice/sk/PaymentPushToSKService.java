package dsa1.xczxx.wfs.ws.mservice.sk;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import dsa1.xczxx.wfs.ws.common.util.DataSendHttp;
import dsa1.xczxx.wfs.ws.mservice.sk.entity.*;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 付款单推送司库服务
 */
public class PaymentPushToSKService {

    private static final Log log = LogFactory.getLog(PaymentPushToSKService.class);

    // 司库接口地址（从系统配置获取或硬编码）
    private static final String TOKEN_URL = "http://10.20.1.120:8080/api/ifpf/open-api/auth/token";
    private static final String SERVICE_URL = "http://10.20.1.120:8080/api/ifpf/open-api/api/service";
    private static final String APP_ID = "nnxz";
    private static final String SERVICE_CODE = "SETT-SettPayment";

    // 付款单表名
    private static final String TABLE_NAME = "cas_paybill";

    // 字段名（请根据实际字段名调整）
    private static final String FIELD_ID = "id";
    private static final String DSA1_SOURCEID = "dsa1_sourceid";    //司库付款单
    private static final String FIELD_BILLNO = "billno";                    // 单据编号
    private static final String FIELD_PAYMENT_DATE = "paydate";         // 付款日期
    private static final String FIELD_PAY_AMOUNT = "actpayamt";             // 支付金额
    private static final String FIELD_PAY_ORG = "org";          // 付款单位编码
    private static final String PAYERACCTCASH = "payeracctcash";          // 付款单位编码
    private static final String FIELD_PAY_ACCOUNT_NO = "payeracctbank";      // 付款账号
    private static final String FIELD_SUPPLIER_CODE = "payeenumber";       // 收款人编码
    private static final String FIELD_SUPPLIER_NAME = "payeename";       // 供应商名称
    private static final String FIELD_RECEIVE_ACCOUNT_NO = "payeebanknum"; // 收款账号
    private static final String FIELD_RECEIVE_ACCOUNT_NAME = "recaccbankname"; // 收款账户名称

    private static final String ISPERSONPAY = "ispersonpay"; // 是否对私

    private static final String PAYEEBANK = "payeebank"; // 收款账户名称
    private static final String PROVINCE = "recstateorprovince"; // 收款账户名称

    private static final String RECTOWN = "rectown"; // 收款账户名称

    private static final String FIELD_BANK_CODE = "bankcode";               // 联行号
    private static final String FIELD_BANK_NAME = "bankname";               // 开户行名称
    private static final String FIELD_PAY_CHANNEL = "paymentchannel";           // 支付方式 1直联 3网银
    private static final String FIELD_MEMO = "usage";                        // 摘要
    private static final String FIELD_REMARK = "e_remark";                    // 备注
    private static final String FIELD_PAY_STATUS = "billstatus";             // 付款状态


    private static final String PAYMENTTYPE = "paymenttype";             // 付款状态

    private static final String FIELD_IS_PUSH_SK = "dsa1_is_push_sk";       // 是否推送司库 0否 1是

    // 付款状态值（请根据实际确认）
    private static final String PAY_STATUS_PAID = "D";

    /**
     * 自动任务：推送近3天已付款且未推送的付款单
     */
    public void pushAuto() {
        log.info("【付款单推送司库-自动任务】开始执行");

        // 计算近3天时间范围
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        Date startDate = calendar.getTime();
        Date endDate = new Date();

        doPush(startDate, endDate);
    }

    /**
     * 手动任务：按指定日期范围推送
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    public void pushManual(Date startDate, Date endDate) {
        log.info("【付款单推送司库-手动任务】开始执行, startDate={}, endDate={}", startDate, endDate);
        doPush(startDate, endDate);
    }

    /**
     * 核心推送逻辑
     */
    private void doPush(Date startDate, Date endDate) {
        try {
            // 1. 查询待推送的付款单
            List<DynamicObject> payBillList = queryPayBills(startDate, endDate);
            log.info("查询到待推送付款单数量：{}", payBillList.size());

            if (payBillList == null || payBillList.isEmpty()) {
                log.info("没有需要推送的付款单");
                return;
            }

            // 2. 组装推送参数
            List<PaymentPushDTO> pushList = buildPushData(payBillList);
            if (pushList.isEmpty()) {
                log.info("组装推送数据为空");
                return;
            }

            // 3. 获取Token
            String token = getToken();
            if (StringUtils.isEmpty(token)) {
                log.error("获取Token失败，本次推送终止");
                return;
            }

            // 4. 批量推送
            ServiceResponseDTO response = pushToSK(pushList, token);
            if (response == null) {
                log.error("推送司库接口无响应");
                return;
            }

            // 5. 处理返回结果，更新标记
            handleResponseAndUpdate(response, payBillList);

            log.info("【付款单推送司库】执行完成");

        } catch (Exception e) {
            log.error("【付款单推送司库】执行异常：{}", e.getMessage(), e);
        }
    }

    /**
     * 查询待推送的付款单
     * 条件：付款状态=已付款 AND 是否推送司库=否 AND 付款日期在指定范围内
     */
    private List<DynamicObject> queryPayBills(Date startDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startStr = sdf.format(startDate);
        String endStr = sdf.format(endDate);

        List<QFilter> filters = new ArrayList<>();
        filters.add(new QFilter(FIELD_PAY_STATUS, "=", PAY_STATUS_PAID));
        filters.add(new QFilter(FIELD_IS_PUSH_SK, "=", "0"));
        filters.add(new QFilter(FIELD_PAYMENT_DATE, ">=", startStr));
        filters.add(new QFilter(FIELD_PAYMENT_DATE, "<=", endStr));

        // 指定查询字段列表
        String selectFields = "id,billno,paydate,actpayamt,org.number,payeracctbank," +
                "payeenumber,payeename,payeebanknum,recaccbankname,bankcode,bankname," +
                "paymentchannel,usage,remark,billstatus,dsa1_is_push_sk,dsa1_sourceid,payeebank,payeracctcash,recstateorprovince,rectown,entry,paymenttype";
        DynamicObject[] result = BusinessDataServiceHelper.load(
                TABLE_NAME,
                selectFields,
                filters.toArray(new QFilter[0])
        );

        List<DynamicObject> list = new ArrayList<>();
        if (result != null) {
            for (DynamicObject obj : result) {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * 组装推送数据
     */
    private List<PaymentPushDTO> buildPushData(List<DynamicObject> payBillList) {
        List<PaymentPushDTO> pushList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (DynamicObject obj : payBillList) {
            try {
                PaymentPushDTO dto = new PaymentPushDTO();

                Date paymentDate =  obj.getDate(FIELD_PAYMENT_DATE);
                Date relPaymentDate = obj.getDate(FIELD_PAYMENT_DATE);
                dto.setOrderNo( obj.getString(FIELD_BILLNO));
                dto.setPayType(1);
                dto.setBillsPayOrderNo(obj.getString(DSA1_SOURCEID));
                dto.setPaymentDate(paymentDate != null ? sdf.format(paymentDate) : null);
                dto.setRelPaymentDate(relPaymentDate != null ? sdf.format(relPaymentDate) : null);
                dto.setPayAmount( obj.getBigDecimal(FIELD_PAY_AMOUNT));
                String payOrgCode = obj.getDynamicObject(FIELD_PAY_ORG).getString("number");
                dto.setPayOrgCode( payOrgCode);
                if (obj.getDynamicObject(FIELD_PAY_ACCOUNT_NO)==null){
                    String accountNo = obj.getDynamicObject(PAYERACCTCASH).getString("number");
                    dto.setPayAccountNo( accountNo);
                }else{
                    String accountNo = obj.getDynamicObject(FIELD_PAY_ACCOUNT_NO).getString("number");
                    dto.setPayAccountNo( accountNo);
                }
                dto.setReceiveOpenOrgCode( obj.getString(FIELD_SUPPLIER_CODE));
                dto.setReceiveOpenOrgName( obj.getString(FIELD_SUPPLIER_NAME));
                dto.setReceiveAccountNo( obj.getString(FIELD_RECEIVE_ACCOUNT_NO));
                dto.setReceiveAccountName(obj.getString(FIELD_RECEIVE_ACCOUNT_NAME));
                dto.setReceiveAreaNameOfProvince(obj.getString(PROVINCE));
                dto.setReceiveAreaNameOfCity(obj.getString(RECTOWN));
                DynamicObject payeebank =   obj.getDynamicObject(PAYEEBANK);
                dto.setRecDirectBank(payeebank.getString("number"));
                dto.setReceiveBranchCode(payeebank.getString("number"));
                dto.setReceiveBranchName( payeebank.getString("name"));
                if ("1".equals(obj.getString(ISPERSONPAY))){
                    dto.setReceiveNature(2);
                }else {
                    dto.setReceiveNature(1);
                }

                if ("counter".equals( obj.getString(FIELD_PAY_CHANNEL))){
                    dto.setPayChannel(3);
                }else{
                    dto.setPayChannel(1);
                }
                dto.setBusinessTypeCode("");
                dto.setBusinessTypeName("");
                dto.setMemo(obj.getString(FIELD_MEMO));
                dto.setRemark(obj.getString(FIELD_REMARK));

                // 必填字段校验
                if (StringUtils.isEmpty(dto.getOrderNo())) {
                    log.warn("单据编号为空，跳过该条数据");
                    continue;
                }
                if (StringUtils.isEmpty(dto.getPaymentDate())) {
                    log.warn("单据{}预计付款日期为空，跳过", dto.getOrderNo());
                    continue;
                }
                if (dto.getPayAmount() == null) {
                    log.warn("单据{}支付金额为空，跳过", dto.getOrderNo());
                    continue;
                }

                pushList.add(dto);

            } catch (Exception e) {
                log.error("组装单据数据异常：{}", e.getMessage(), e);
            }
        }

        return pushList;
    }

    /**
     * 获取Token
     */
    private String getToken() {
        try {
            TokenRequestDTO request = new TokenRequestDTO();
            request.setAppId(APP_ID);

            String response = DataSendHttp.doPost_token(
                    JSON.toJSONString(request),
                    TOKEN_URL,
                    null
            );

            log.info("Token接口返回：{}", response);

            TokenResponseDTO result = JSON.parseObject(response, TokenResponseDTO.class);
            if (result.getSuccess()) {
                TokenResponseDTO.TokenData data = result.getData();
                return data.getToken();
            } else {
                log.error("获取Token失败：{}", result.getMessage());
                return null;
            }

        } catch (Exception e) {
            log.error("获取Token异常：{}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 推送数据到司库
     */
    private ServiceResponseDTO pushToSK(List<PaymentPushDTO> pushList, String token) {
        try {
            // 构建请求头
            Map<String, String> header = new HashMap<>();
            header.put("appId", APP_ID);
            header.put("Authorization", token);

            // 构建请求参数
            ServiceRequestDTO serviceRequest = new ServiceRequestDTO();
            serviceRequest.setServiceCode(SERVICE_CODE);
            serviceRequest.setData(JSON.toJSONString(pushList));

            String requestBody = JSON.toJSONString(serviceRequest);

            log.info("推送请求参数：{}", requestBody);

            // 调用接口
            String response = DataSendHttp.doPost_token(requestBody, SERVICE_URL, header);
            log.info("司库接口返回：{}", response);

            if (StringUtils.isEmpty(response)) {
                log.error("司库接口返回为空");
                return null;
            }

            return JSON.parseObject(response, ServiceResponseDTO.class);

        } catch (Exception e) {
            log.error("推送司库异常：{}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 处理返回结果并更新标记
     */
    private void handleResponseAndUpdate(ServiceResponseDTO response, List<DynamicObject> payBillList) {
        if (response == null) {
            log.error("司库接口返回为空，无法处理结果");
            return;
        }

        try {
            // 判断整体调用是否成功
            if (!response.getSuccess()) {
                log.error("司库接口调用失败：{}", response.getMessage());
                return;
            }

            // 解析返回的data（List<PaymentSyncResultDTO>）
            String dataStr = response.getData();
            if (StringUtils.isEmpty(dataStr)) {
                log.warn("返回结果data为空");
                return;
            }

            List<PaymentSyncResultDTO> resultList = JSON.parseArray(dataStr, PaymentSyncResultDTO.class);
            if (resultList == null || resultList.isEmpty()) {
                log.warn("返回结果数组为空");
                return;
            }

            // 构建 orderNo -> 推送结果 的映射
            Map<String, PaymentSyncResultDTO> resultMap = new HashMap<>();
            for (PaymentSyncResultDTO dto : resultList) {
                resultMap.put(dto.getOrderNo(), dto);
            }

            // 逐条更新标记
            List<DynamicObject> updateList = new ArrayList<>();
            int failCount = 0;

            for (DynamicObject obj : payBillList) {
                String orderNo = (String) obj.get(FIELD_BILLNO);
                PaymentSyncResultDTO syncResult = resultMap.get(orderNo);

                if (syncResult == null) {
                    log.warn("单据{}未在返回结果中找到", orderNo);
                    failCount++;
                    continue;
                }

                if ("success".equals(syncResult.getStatus())) {
                    // 推送成功，更新标记
                    DynamicObject updateObj = BusinessDataServiceHelper.newDynamicObject(TABLE_NAME);
                    updateObj.set(FIELD_ID, obj.get(FIELD_ID));
                    updateObj.set(FIELD_IS_PUSH_SK, "1");
                    updateList.add(updateObj);
                    log.info("单据{}推送成功", orderNo);
                } else {
                    // 推送失败，不改变标记，等待下次重试
                    log.info("单据{}推送失败：{}", orderNo, syncResult.getMessage());
                    failCount++;
                }
            }

            // 批量更新
            if (!updateList.isEmpty()) {
                OperationResult opResult = SaveServiceHelper.saveOperate(
                        TABLE_NAME,
                        updateList.toArray(new DynamicObject[0]),
                        OperateOption.create()
                );
                log.info("更新推送标记完成，成功更新{}条", updateList.size());
            }

            // 输出统计
            int successCount = updateList.size();
            log.info("推送完成，成功{}条，失败{}条", successCount, failCount);

        } catch (Exception e) {
            log.error("处理返回结果异常：{}", e.getMessage(), e);
        }
    }
}