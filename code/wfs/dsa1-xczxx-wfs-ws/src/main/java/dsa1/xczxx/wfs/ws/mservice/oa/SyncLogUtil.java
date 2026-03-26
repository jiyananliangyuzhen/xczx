package dsa1.xczxx.wfs.ws.mservice.oa;


import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import dsa1.xczxx.wfs.ws.mservice.oa.entity.Xczx_Log;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Title: SyncLogUtil
 * @Author libai
 * @Package gxdm.common.log
 * @Date 2025/2/16 9:30
 * @description: 集成日志工具类
 */
public class SyncLogUtil {

    private static final Log logger = LogFactory.getLog(SyncLogUtil.class);
    public static Object saveLog(String type, String url, String param, String syncResult, String oaType){
        String name = typeName(type);
        DynamicObject log = BusinessDataServiceHelper.newDynamicObject("awrp_log");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String format = simpleDateFormat.format(date);
        log.set("awrp_number", format + name + date.getTime());
        log.set("awrp_type", type);
        log.set("awrp_oatype", oaType);
        log.set("awrp_param", name + format);
        log.set("awrp_param_tag", param);
        log.set("awrp_result", name + format);
        log.set("awrp_result_tag", syncResult);
        log.set("awrp_url", url);
        OperationResult saveResult = SaveServiceHelper.saveOperate("awrp_log", new DynamicObject[]{log}, OperateOption.create());
        if(!saveResult.isSuccess()){
            logger.error(format + name + "保存日志失败,失败原因:" + saveResult.getAllErrorOrValidateInfo());
            return null;
        }
        return saveResult.getSuccessPkIds().get(0);
    }



    public static Object gxjt_log_save(Xczx_Log gxjt_log){
        logger.info("saveLog1 begin");
        DynamicObject log = BusinessDataServiceHelper.newDynamicObject("dsa1_xczx_log");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String format = simpleDateFormat.format(date);
        log.set("dsa1_code",gxjt_log.getCode() );
        log.set("dsa1_name", gxjt_log.getName());
        log.set("dsa1_inparam", gxjt_log.getInParam());
        log.set("dsa1_outparam", gxjt_log.getOutParam());
        log.set("dsa1_status", gxjt_log.getStatus());
        log.set("dsa1_responsemsg", gxjt_log.getResponseMsg());
        log.set("dsa1_requesturl", gxjt_log.getRequestUrl());
        log.set("dsa1_createtime", gxjt_log.getCreateTime());
        log.set("dsa1_requesttime", gxjt_log.getRequestTime());
        log.set("dsa1_type","OA");
        OperationResult saveResult = new OperationResult();
        try {
            saveResult  = SaveServiceHelper.saveOperate("dsa1_xczx_log",
                    new DynamicObject[]{log}, OperateOption.create());
            logger.info("saveResult = {}",saveResult);
            if(!saveResult.isSuccess()){
                logger.error(format + gxjt_log.getName() + "保存日志失败,失败原因:" + saveResult.getAllErrorOrValidateInfo());
                return null;
            }
        }catch ( Exception e){
            logger.error(e.getMessage());
        }

        logger.info("saveResult.getSuccessPkIds().get(0)= {}",saveResult.getSuccessPkIds().get(0));
        return saveResult.getSuccessPkIds().get(0);
    }


    private static String typeName(String type){
        String result = "";
        switch (type){
            case "accountview":
                result = "会计科目";
                break;
            case "org":
                result = "组织";
                break;
            case "user":
                result = "人员";
                break;
            case "oa":
                result = "OA";
                break;
            case "customer":
                result = "客户";
                break;
            case "supplier":
                result = "供应商";
                break;
            case "bank":
                result = "客商银行账号";
                break;
            case "contract":
                result = "合同";
                break;
        }
        return result;
    }






}
