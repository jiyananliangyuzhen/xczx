//package dsa1.xczxx.wfs.ws.mservice.oa;
//
//import dsa1.xczxx.wfs.ws.mservice.oa.entity.Xczx_Log;
//import dsa1.xczxx.wfs.ws.mservice.oa.entity.NotifyTodoAppResult;
//import dsa1.xczxx.wfs.ws.mservice.oa.entity.NotifyTodoRemoveContext;
//import dsa1.xczxx.wfs.ws.mservice.oa.entity.NotifyTodoSendContext;
//import dsa1.xczxx.wfs.ws.util.OATaskDataSend;
//import kd.bos.workflow.engine.msg.AbstractMessageServiceHandler;
//import kd.bos.workflow.engine.msg.ctx.MessageContext;
//import kd.bos.workflow.engine.msg.info.ToDoInfo;
//
//import com.alibaba.fastjson.JSON;
//import kd.bos.dataentity.entity.DynamicObject;
//import kd.bos.dataentity.entity.DynamicObjectCollection;
//import kd.bos.dataentity.utils.StringUtils;
//import kd.bos.exception.ErrorCode;
//import kd.bos.exception.KDBizException;
//import kd.bos.logging.Log;
//import kd.bos.logging.LogFactory;
//import kd.bos.orm.query.QCP;
//import kd.bos.orm.query.QFilter;
//import kd.bos.servicehelper.QueryServiceHelper;
//
//
//import java.sql.Timestamp;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class ApprovalTaskSyncOa extends AbstractMessageServiceHandler {
//    private static final Log log = LogFactory.getLog(ApprovalTaskSyncOa.class);
//    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    @Override
//    public void createToDo(MessageContext messageContext, ToDoInfo toDoInfo) {
//        log.info("messageContext = {}",messageContext);
//        log.info("toDoInfo = {}",toDoInfo);
//        log.info("createToDo begin ");
//        Xczx_Log xczxLog = new Xczx_Log();
//        xczxLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
//        xczxLog.setCode("createToDo");
//        xczxLog.setName("OA待办推送");
//        xczxLog.setBillno(toDoInfo.getBillNo());
//        try {
//            log.info("messageContext = {}",messageContext);
//            log.info("toDoInfo = {}",toDoInfo);
//            String createToDoPath = System.getProperty("createtodo");
//            log.info("createToDoPath = {}",createToDoPath);
//            if (StringUtils.isEmpty(createToDoPath)) {
//                log.error("ApprovalTaskSyncOA createToDo createTodo的Path为空,请检查MC配置");
//                throw new KDBizException("createTodo的Path为空,请检查MC配置");
//            }
//            xczxLog.setRequestUrl(createToDoPath);
//            List<Long> userIds = toDoInfo.getUserIds();
//            if (userIds.isEmpty()) {
//                log.error("ApprovalTaskSyncOAPlugin createToDo 任务接收人为空！");
//                throw new KDBizException("任务接收人为空！");
//            }
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String taskId = messageContext.getTaskId().toString();
//            log.info("taskId = {}",taskId);
//            Map<String, Object> nodeName = getNodeName(taskId);
//            log.info("nodeName = {}",nodeName);
//            NotifyTodoSendContext context = new NotifyTodoSendContext();
//            context.setSyscode("xczx");
//            context.setFlowid(taskId);
//            String subject = StringUtils.isEmpty(nodeName.get("subject").toString())?toDoInfo.getContent() : nodeName.get("subject").toString();
//            context.setRequestname(subject);
//            context.setWorkflowname(subject);
//
//            String originalUrl = toDoInfo.getUrl();
//            String relativePath = convertToRelativePath(originalUrl);
//            log.info("originalUrl = {}, relativePath = {}", originalUrl, relativePath);
//            context.setPcurl(relativePath);
//            context.setAppurl(relativePath);
//            context.setIsremark("0");
//            context.setViewtype("0");
//            List<String> username = getUsername(userIds);
//            log.info("username = {}",username);
//            List<String> loginName =  getOaLoginName(userIds);
//            context.setReceiver(String.join(",", loginName));
//            context.setCreatedatetime(simpleDateFormat.format(nodeName.get("createDate")));
//            context.setReceivedatetime(simpleDateFormat.format(new Date()));
//            log.info("starter",String.valueOf(nodeName.get("starter")));
//            String docCreator = getOaLoginName(Long.valueOf(nodeName.get("starter").toString()));
//            context.setCreator(docCreator);
//            log.info(" createToDo context = {}",JSON.toJSONString(context));
//            xczxLog.setInParam(JSON.toJSONString(context));
//            try {
//                xczxLog.setRequestTime(new Timestamp(System.currentTimeMillis()));
//                String result = OATaskDataSend.doPost2(JSON.toJSONString(context), createToDoPath);
//                log.info("createToDo result = {}",result);
//                xczxLog.setOutParam(result);
//                NotifyTodoAppResult res = JSON.parseObject(result, NotifyTodoAppResult.class);
//                log.info("res  = {}",res);
//                if ("0".equals(res.getOperResult())){
//                    throw  new KDBizException(res.getMessage());
//                }
//            }catch (Exception e){
//                xczxLog.setStatus("0");
//                log.error("ApprovalTaskSyncOA createToDo OA待办数据推送异常" + e.getMessage(), e);
//                throw new KDBizException(e, new ErrorCode("OACreateToDoDataError", "OA待办数据推送异常:" + e.getMessage()));
//            }
//        }catch (Exception e){
//            log.info("进入存储待办");
//            if (xczxLog.getStatus()==null){
//                xczxLog.setStatus("1");
//            }
//            xczxLog.setResponseMsg(e.getMessage());
//            SyncLogUtil.gxjt_log_save(xczxLog);
////            throw new KDBizException(e, new ErrorCode("OACreateToDoDataError",   e.getMessage()));
//        }
//
//
//
//
//    }
//
//    @Override
//    public void dealToDo(MessageContext messageContext, ToDoInfo toDoInfo) {
//        log.info("dealToDo begin ");
//        Xczx_Log xczxLog = new Xczx_Log();
//        xczxLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
//        xczxLog.setCode("dealToDo");
//        xczxLog.setName("OA已办推送");
//        xczxLog.setBillno(toDoInfo.getBillNo());
//        try {
//            log.info("messageContext = {}",messageContext);
//            log.info("toDoInfo = {}",toDoInfo);
//            String dealToDoPath = System.getProperty("createtodo");
//            if (StringUtils.isEmpty(dealToDoPath)) {
//                log.error("ApprovalTaskSyncOAPlugin dealToDo dealToDoPath的Path为空,请检查MC配置");
//                throw new KDBizException("dealToDoPath的Path为空,请检查MC配置");
//            }
//
//            List<Long> userIds = toDoInfo.getUserIds();
//            if (userIds.isEmpty()) {
//                log.error("ApprovalTaskSyncOAPlugin createToDo 任务接收人为空！");
//                throw new KDBizException("任务接收人为空！");
//            }
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//            String taskId = messageContext.getTaskId().toString();
//            Map<String, Object> nodeName = getNodeName(taskId);
//            NotifyTodoSendContext context = new NotifyTodoSendContext();
//            context.setSyscode("xczx");
//            context.setFlowid(taskId);
//            String subject = StringUtils.isEmpty(nodeName.get("subject").toString())?toDoInfo.getContent() : nodeName.get("subject").toString();
//            context.setRequestname(subject);
//            context.setWorkflowname(subject);
//            context.setPcurl(toDoInfo.getUrl());
//            context.setAppurl(toDoInfo.getUrl());
//            context.setIsremark("2");
//            context.setViewtype("1");
//
//            List<String> username = getUsername(userIds);
//            log.info("username = {}",username);
//            List<String> loginName =  getOaLoginName(userIds);
//            log.info("loginName",loginName);
//            context.setReceiver(String.join(",", loginName));
//            context.setCreatedatetime(simpleDateFormat.format(nodeName.get("createDate")));
//            context.setReceivedatetime(simpleDateFormat.format(new Date()));
//            log.info("starter",String.valueOf(nodeName.get("starter")));
//            String docCreator = getOaLoginName(Long.valueOf(nodeName.get("starter").toString()));
//            context.setCreator(docCreator);
//            log.info("dealToDo context = {}",context);
//            xczxLog.setInParam(JSON.toJSONString(context));
//            try {
//                xczxLog.setRequestTime(new Timestamp(System.currentTimeMillis()));
//                String result = OATaskDataSend.doPost2(JSON.toJSONString(context), dealToDoPath);
//                log.info("dealToDo result = {}",result);
//                xczxLog.setOutParam(result);
//                NotifyTodoAppResult res = JSON.parseObject(result, NotifyTodoAppResult.class);
//                if ("0".equals(res.getOperResult())){
//                    throw  new KDBizException(res.getMessage());
//                }
//            }catch (Exception e){
//                xczxLog.setStatus("2");
//                log.error("ApprovalTaskSyncOa dealToDo OA已办数据推送异常" + e.getMessage(), e);
//                throw new KDBizException(e, new ErrorCode("OADealToDoDataError", "待办数据封装异常:" + e.getMessage()));
//            }
//        }catch (Exception e){
//            if (xczxLog.getStatus()==null){
//                xczxLog.setStatus("1");
//            }
//            xczxLog.setResponseMsg(e.getMessage());
//            SyncLogUtil.gxjt_log_save(xczxLog);
////            throw new KDBizException(e, new ErrorCode("OADealToDoDataError",   e.getMessage()));
//
//        }
//
//    }
//
////    @Override
////    public void deleteToDo(MessageContext messageContext, ToDoInfo toDoInfo) {
////        log.info("deleteToDo begin ");
////        Xczx_Log xczxLog = new Xczx_Log();
////        xczxLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
////        xczxLog.setCode("deleteToDo");
////        xczxLog.setName("OA删除待办");
////        xczxLog.setBillno(toDoInfo.getBillNo());
////        try {
////            String deleteToDoPath = System.getProperty("deleteto");
////            if (StringUtils.isEmpty(deleteToDoPath)) {
////                log.error("ApprovalTaskSyncOAPlugin deleteToDo deleteToDoPath的Path为空,请检查MC配置");
////                throw new KDBizException("deleteToDoPath的Path为空,请检查MC配置");
////            }
////            List<Long> userIds = toDoInfo.getUserIds();
////            if (userIds.isEmpty()) {
////                log.error("ApprovalTaskSyncOAPlugin createToDo 任务接收人为空！");
////                throw new KDBizException("任务接收人为空！");
////            }
////            List<String> username = getUsername(userIds);
////            log.info("username = {}",username);
////            List<String>   loginName =  getOaLoginName(userIds);
////            log.info("loginName",loginName);
////            String taskId = messageContext.getTaskId().toString();
////
////            for (String user:loginName){
////                NotifyTodoRemoveContext context = new NotifyTodoRemoveContext();
////                context.setSyscode("xczx");
////                context.setFlowid(taskId);
////                context.setReceiver(user);
////
////                log.info("deleteToDo context = {}",context);
////                xczxLog.setInParam(JSON.toJSONString(context));
////            }
////
////            try {
////                xczxLog.setRequestTime(new Timestamp(System.currentTimeMillis()));
////                String result = OATaskDataSend.doPost2(JSON.toJSONString(context), deleteToDoPath);
////                log.info("deleteToDo result = {}",result);
////                xczxLog.setOutParam(result);
////                NotifyTodoAppResult res = JSON.parseObject(result, NotifyTodoAppResult.class);
////                if ("0".equals(res.getOperResult())){
////                    throw  new RuntimeException(res.getMessage());
////                }
////            }catch (Exception e){
////                xczxLog.setStatus("2");
////                log.error("ApprovalTaskSyncOa deleteToDo OA待办数据封装异常" + e.getMessage(), e);
////                throw new KDBizException(e, new ErrorCode("OADealToDoDataError", "待办数据封装异常:" + e.getMessage()));
////            }
////        }catch (Exception e){
////            if (xczxLog.getStatus()==null){
////                xczxLog.setStatus("1");
////            }
////            xczxLog.setResponseMsg(e.getMessage());
////            SyncLogUtil.gxjt_log_save(xczxLog);
////            throw new KDBizException(e, new ErrorCode("OADealToDoDataError", e.getMessage()));
////
////        }
////
////    }
//
//
//    @Override
//    public void deleteToDo(MessageContext messageContext, ToDoInfo toDoInfo) {
//        log.info("deleteToDo begin");
//
//        // 创建日志对象
//        Xczx_Log xczxLog = createLog(toDoInfo.getBillNo());
//
//        try {
//            // 1. 验证配置路径
//             String deleteToDoPath = System.getProperty("deleteto");
//            if (StringUtils.isEmpty(deleteToDoPath)) {
//                log.error("ApprovalTaskSyncOAPlugin deleteToDo deleteToDoPath的Path为空,请检查MC配置");
//                throw new KDBizException("deleteToDoPath的Path为空,请检查MC配置");
//            }
//
//            // 2. 获取用户信息
//            List<Long> userIds = toDoInfo.getUserIds();
//            if (userIds == null || userIds.isEmpty()) {
//                log.error("ApprovalTaskSyncOAPlugin deleteToDo 任务接收人为空！");
//                throw new KDBizException("任务接收人为空！");
//            }
//
//            List<String> loginNames = getOaLoginName(userIds);
//            log.info("loginNames = {}", loginNames);
//
//            String taskId = messageContext.getTaskId().toString();
//
//            // 3. 逐个处理用户删除待办
//            processDeleteToDoForUsers(loginNames, taskId, deleteToDoPath, xczxLog);
//
//            // 4. 保存成功日志
//            xczxLog.setStatus("0");
//            SyncLogUtil.gxjt_log_save(xczxLog);
//
//        } catch (Exception e) {
//            handleException(xczxLog, e);
//        }
//
//        log.info("deleteToDo end");
//    }
//
//    /**
//     * 创建日志对象
//     */
//    private Xczx_Log createLog(String billNo) {
//        Xczx_Log xczxLog = new Xczx_Log();
//        xczxLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
//        xczxLog.setCode("deleteToDo");
//        xczxLog.setName("OA删除待办");
//        xczxLog.setBillno(billNo);
//        return xczxLog;
//    }
//
//
//
//    /**
//     * 逐个处理用户删除待办
//     */
//    private void processDeleteToDoForUsers(List<String> loginNames, String taskId,
//                                           String deleteToDoPath, Xczx_Log xczxLog) {
//        List<String> successUsers = new ArrayList<>();
//        List<String> failedUsers = new ArrayList<>();
//
//        for (String loginName : loginNames) {
//            try {
//                // 创建删除待办上下文
//                NotifyTodoRemoveContext context = createNotifyContext(taskId, loginName);
//                log.info("deleteToDo context for user {} = {}", loginName, context);
//
//                // 调用OA接口
//                String result = callOADeleteToDo(context, deleteToDoPath, xczxLog);
//
//                // 验证结果
//                if (validateResult(result)) {
//                    successUsers.add(loginName);
//                } else {
//                    failedUsers.add(loginName);
//                }
//
//            } catch (Exception e) {
//                log.error("处理用户{}删除待办失败", loginName, e);
//                failedUsers.add(loginName);
//            }
//        }
//
//        // 记录处理结果
//        log.info("deleteToDo处理完成：成功{}个，失败{}个", successUsers.size(), failedUsers.size());
//        if (!failedUsers.isEmpty()) {
//            log.warn("失败的登录名：{}", failedUsers);
//        }
//    }
//
//    /**
//     * 创建删除待办上下文
//     */
//    private NotifyTodoRemoveContext createNotifyContext(String taskId, String loginName) {
//        NotifyTodoRemoveContext context = new NotifyTodoRemoveContext();
//        context.setSyscode("xczx");
//        context.setFlowid(taskId);
//        context.setReceiver(loginName);  // 现在只传单个用户
//        return context;
//    }
//
//    /**
//     * 调用OA删除待办接口
//     */
//    private String callOADeleteToDo(NotifyTodoRemoveContext context, String deleteToDoPath,
//                                    Xczx_Log xczxLog) {
//        try {
//            String requestJson = JSON.toJSONString(context);
//            xczxLog.setInParam(requestJson);
//            xczxLog.setRequestTime(new Timestamp(System.currentTimeMillis()));
//
//            String result = OATaskDataSend.doPost2(requestJson, deleteToDoPath);
//            log.info("deleteToDo result = {}", result);
//            xczxLog.setOutParam(result);
//
//            return result;
//        } catch (Exception e) {
//            xczxLog.setStatus("2");
//            log.error("ApprovalTaskSyncOa deleteToDo OA待办数据封装异常", e);
//            throw new KDBizException(e, new ErrorCode("OADealToDoDataError",
//                    "待办数据封装异常:" + e.getMessage()));
//        }
//    }
//
//    /**
//     * 验证调用结果
//     */
//    private boolean validateResult(String result) {
//        try {
//            NotifyTodoAppResult res = JSON.parseObject(result, NotifyTodoAppResult.class);
//            if ("0".equals(res.getOperResult())) {
//                throw new RuntimeException(res.getMessage());
//            }
//            return true;
//        } catch (Exception e) {
//            log.error("验证删除待办结果失败", e);
//            return false;
//        }
//    }
//
//    /**
//     * 处理异常
//     */
//    private void handleException(Xczx_Log xczxLog, Exception e) {
//        if (xczxLog.getStatus() == null) {
//            xczxLog.setStatus("1");
//        }
//        xczxLog.setResponseMsg(e.getMessage());
//        SyncLogUtil.gxjt_log_save(xczxLog);
//
//        log.error("deleteToDo处理失败", e);
////        throw new KDBizException(e, new ErrorCode("OADealToDoDataError", e.getMessage()));
//    }
//
//    private Map<String, Object> getNodeName(String idStr) {
//        Map<String, Object> map = new HashMap<>();
//        Long id = Long.valueOf(idStr);
//        DynamicObject task = QueryServiceHelper.queryOne("wf_task", "starterid,name,createdate,processtype,subject,billno", new QFilter[]{new QFilter("id", QCP.equals, id)});
//        if (task == null) {
//            throw new KDBizException("任务:[" + id + "]不存在!");
//        }
//        String nodeName = task.getString("name");
//        Long starter = task.getLong("starterid");
//        Date createDate = task.getDate("createdate");
//        String type = task.getString("processtype");
//        String subject = task.getString("subject");
//        map.put("nodeName", nodeName);
//        map.put("starter",starter);
//        map.put("createDate", createDate);
//        map.put("processType", "AuditFlow".equals(type) ? "审批流" : "业务流");
//        map.put("subject", subject);
//        map.put("billno", task.getString("billno"));
//        return map;
//    }
//    private List<String> getUsername(List<Long> ids) {
//        DynamicObjectCollection userCol = QueryServiceHelper.query("bos_user", "username", new QFilter[]{new QFilter("id", QCP.in, ids)});
//        if (userCol.isEmpty()) {
//            throw new KDBizException("用户:[" + ids + "]不存在!");
//        }
//        return userCol.stream().map(user -> user.getString("username")).collect(Collectors.toList());
//    }
//
//    private   List<String>   getOaLoginName(List<Long> id) {
//        log.info("getOaLoginName begin");
//        DynamicObjectCollection   dynamicObjects = QueryServiceHelper.query("bos_user", "dsa1_oamapping",
//                new QFilter[]{new QFilter("id", QCP.in, id)});
//        log.info("dynamicObject = {}",dynamicObjects);
//        List<String> list;
//        if (dynamicObjects == null) {
//            throw new KDBizException("用户:[" + id + "]的OA映射不存在,请维护!");
//        }else{
//          list = dynamicObjects.stream()
//                    .map(obj -> obj.getString("dsa1_oamapping"))
//                    .filter(phone -> phone != null && !phone.isEmpty())
//                    .collect(Collectors.toList());
//        }
//        return list;
//    }
//
//
//    private String getOaLoginName(Long id) {
//        log.info("getOaLoginName begin");
//        DynamicObject   dynamicObject = QueryServiceHelper.queryOne("bos_user", "dsa1_oamapping",
//                new QFilter[]{new QFilter("id", QCP.equals, id)});
//
//        DynamicObject   dynamicObject1 = QueryServiceHelper.queryOne("bos_user", "name", new QFilter[]{new QFilter("id", QCP.equals, id)});
//        log.info("dynamicObject1 = {}",dynamicObject1);
//        log.info("dynamicObject = {}",dynamicObject);
//        if (dynamicObject == null) {
//            throw new KDBizException("用户:[" + id + "]的LoginName不存在,请维护!");
//        }
//        return dynamicObject.getString("dsa1_oamapping");
//    }
//
//
//    // 添加路径转换方法
//    private String convertToRelativePath(String url) {
//        if (StringUtils.isEmpty(url)) {
//            return url;
//        }
//        try {
//            java.net.URL urlObj = new java.net.URL(url);
//            // 返回路径部分（包括查询参数）
//            String path = urlObj.getPath();
//            String query = urlObj.getQuery();
//            if (StringUtils.isEmpty(query)) {
//                return path;
//            } else {
//                return path + "?" + query;
//            }
//        } catch (Exception e) {
//            log.error("URL转换异常，使用简单截取方式: {}", e.getMessage());
//            // 简单截取方式：找到第一个://之后的下一个/开始截取
//            int protocolIndex = url.indexOf("://");
//            if (protocolIndex != -1) {
//                int pathStartIndex = url.indexOf("/", protocolIndex + 3);
//                if (pathStartIndex != -1) {
//                    return url.substring(pathStartIndex);
//                }
//            }
//            // 如果都不是，返回原值并记录警告
//            log.warn("无法转换URL为相对路径: {}", url);
//            return url;
//        }
//    }
//
//}
//




package dsa1.xczxx.wfs.ws.mservice.oa;

import dsa1.xczxx.wfs.ws.mservice.oa.entity.Xczx_Log;
import dsa1.xczxx.wfs.ws.mservice.oa.entity.NotifyTodoAppResult;
import dsa1.xczxx.wfs.ws.mservice.oa.entity.NotifyTodoRemoveContext;
import dsa1.xczxx.wfs.ws.mservice.oa.entity.NotifyTodoSendContext;
import dsa1.xczxx.wfs.ws.util.OATaskDataSend;
import kd.bos.workflow.engine.msg.AbstractMessageServiceHandler;
import kd.bos.workflow.engine.msg.ctx.MessageContext;
import kd.bos.workflow.engine.msg.info.ToDoInfo;

import com.alibaba.fastjson.JSON;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ApprovalTaskSyncOa extends AbstractMessageServiceHandler {
    private static final Log log = LogFactory.getLog(ApprovalTaskSyncOa.class);
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void createToDo(MessageContext messageContext, ToDoInfo toDoInfo) {
        log.info("createToDo begin");
        log.info("messageContext = {}", messageContext);
        log.info("toDoInfo = {}", toDoInfo);

        // 创建日志对象
        Xczx_Log xczxLog = createLog(toDoInfo.getBillNo(), "createToDo", "OA待办推送");

        try {
            // 1. 验证配置路径
            String createToDoPath = System.getProperty("createtodo");
            if (StringUtils.isEmpty(createToDoPath)) {
                log.error("ApprovalTaskSyncOA createToDo createTodo的Path为空,请检查MC配置");
                throw new KDBizException("createTodo的Path为空,请检查MC配置");
            }
            xczxLog.setRequestUrl(createToDoPath);

            // 2. 获取用户信息
            List<Long> userIds = toDoInfo.getUserIds();
            if (userIds == null || userIds.isEmpty()) {
                log.error("ApprovalTaskSyncOAPlugin createToDo 任务接收人为空！");
                throw new KDBizException("任务接收人为空！");
            }

            // 3. 获取任务节点信息
            String taskId = messageContext.getTaskId().toString();
            Map<String, Object> nodeInfo = getNodeInfo(taskId);

            // 4. 获取登录名列表
            List<String> loginNames = getOaLoginName(userIds);
            log.info("loginNames = {}", loginNames);

            // 5. 获取创建人OA登录名
            String docCreator = getOaLoginName(Long.valueOf(nodeInfo.get("starter").toString()));

            // 6. 获取主题
            String subject = StringUtils.isEmpty(nodeInfo.get("subject").toString()) ?
                    toDoInfo.getContent() : nodeInfo.get("subject").toString();

            // 7. 处理URL
            String relativePath = convertToRelativePath(toDoInfo.getUrl());

            // 8. 逐个用户推送待办
            processCreateToDoForUsers(loginNames, taskId, subject, relativePath,
                    docCreator, nodeInfo, createToDoPath, xczxLog);

            // 9. 保存成功日志
            xczxLog.setStatus("0");
            SyncLogUtil.gxjt_log_save(xczxLog);

        } catch (Exception e) {
            handleException(xczxLog, e, "createToDo");
        }

        log.info("createToDo end");
    }

    @Override
    public void dealToDo(MessageContext messageContext, ToDoInfo toDoInfo) {
        log.info("dealToDo begin");
        log.info("messageContext = {}", messageContext);
        log.info("toDoInfo = {}", toDoInfo);

        // 创建日志对象
        Xczx_Log xczxLog = createLog(toDoInfo.getBillNo(), "dealToDo", "OA已办推送");

        try {
            // 1. 验证配置路径
            String dealToDoPath = System.getProperty("dealtodo");
            if (StringUtils.isEmpty(dealToDoPath)) {
                log.error("ApprovalTaskSyncOAPlugin dealToDo dealToDoPath的Path为空,请检查MC配置");
                throw new KDBizException("dealToDoPath的Path为空,请检查MC配置");
            }

            // 2. 获取用户信息
            List<Long> userIds = toDoInfo.getUserIds();
            if (userIds == null || userIds.isEmpty()) {
                log.error("ApprovalTaskSyncOAPlugin dealToDo 任务接收人为空！");
                throw new KDBizException("任务接收人为空！");
            }

            // 3. 获取任务节点信息
            String taskId = messageContext.getTaskId().toString();
            Map<String, Object> nodeInfo = getNodeInfo(taskId);

            // 4. 获取登录名列表
            List<String> loginNames = getOaLoginName(userIds);
            log.info("loginNames = {}", loginNames);

            // 5. 获取创建人OA登录名
            String docCreator = getOaLoginName(Long.valueOf(nodeInfo.get("starter").toString()));

            // 6. 获取主题
            String subject = StringUtils.isEmpty(nodeInfo.get("subject").toString()) ?
                    toDoInfo.getContent() : nodeInfo.get("subject").toString();

            // 7. 逐个用户推送已办
            processDealToDoForUsers(loginNames, taskId, subject, toDoInfo.getUrl(),
                    docCreator, nodeInfo, dealToDoPath, xczxLog);

            // 8. 保存成功日志
            xczxLog.setStatus("0");
            SyncLogUtil.gxjt_log_save(xczxLog);

        } catch (Exception e) {
            handleException(xczxLog, e, "dealToDo");
        }

        log.info("dealToDo end");
    }

    @Override
    public void deleteToDo(MessageContext messageContext, ToDoInfo toDoInfo) {
        log.info("deleteToDo begin");

        // 创建日志对象
        Xczx_Log xczxLog = createLog(toDoInfo.getBillNo(), "deleteToDo", "OA删除待办");

        try {
            // 1. 验证配置路径
            String deleteToDoPath = System.getProperty("deleteto");
            if (StringUtils.isEmpty(deleteToDoPath)) {
                log.error("ApprovalTaskSyncOAPlugin deleteToDo deleteToDoPath的Path为空,请检查MC配置");
                throw new KDBizException("deleteToDoPath的Path为空,请检查MC配置");
            }

            // 2. 获取用户信息
            List<Long> userIds = toDoInfo.getUserIds();
            if (userIds == null || userIds.isEmpty()) {
                log.error("ApprovalTaskSyncOAPlugin deleteToDo 任务接收人为空！");
                throw new KDBizException("任务接收人为空！");
            }

            List<String> loginNames = getOaLoginName(userIds);
            log.info("loginNames = {}", loginNames);

            String taskId = messageContext.getTaskId().toString();

            // 3. 逐个用户删除待办
            processDeleteToDoForUsers(loginNames, taskId, deleteToDoPath, xczxLog);

            // 4. 保存成功日志
            xczxLog.setStatus("0");
            SyncLogUtil.gxjt_log_save(xczxLog);

        } catch (Exception e) {
            handleException(xczxLog, e, "deleteToDo");
        }

        log.info("deleteToDo end");
    }

    /**
     * 创建日志对象
     */
    private Xczx_Log createLog(String billNo, String code, String name) {
        Xczx_Log xczxLog = new Xczx_Log();
        xczxLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
        xczxLog.setCode(code);
        xczxLog.setName(name);
        xczxLog.setBillno(billNo);
        return xczxLog;
    }

    /**
     * 获取节点信息
     */
    private Map<String, Object> getNodeInfo(String taskId) {
        Map<String, Object> map = new HashMap<>();
        Long id = Long.valueOf(taskId);
        DynamicObject task = QueryServiceHelper.queryOne("wf_task",
                "starterid,name,createdate,processtype,subject,billno",
                new QFilter[]{new QFilter("id", QCP.equals, id)});

        if (task == null) {
            throw new KDBizException("任务:[" + id + "]不存在!");
        }
        map.put("nodeName", task.getString("name"));
        map.put("starter", task.getLong("starterid"));
        map.put("createDate", task.getDate("createdate"));
        map.put("processType", "AuditFlow".equals(task.getString("processtype")) ? "审批流" : "业务流");
        map.put("subject", task.getString("subject"));
        map.put("billno", task.getString("billno"));

        return map;
    }

    /**
     * 获取用户名列表
     */
    private List<String> getUsername(List<Long> ids) {
        DynamicObjectCollection userCol = QueryServiceHelper.query("bos_user", "username",
                new QFilter[]{new QFilter("id", QCP.in, ids)});
        if (userCol.isEmpty()) {
            throw new KDBizException("用户:[" + ids + "]不存在!");
        }
        return userCol.stream()
                .map(user -> user.getString("username"))
                .collect(Collectors.toList());
    }

    /**
     * 批量获取OA登录名
     */
    private List<String> getOaLoginName(List<Long> ids) {
        log.info("getOaLoginName batch begin");
        DynamicObjectCollection dynamicObjects = QueryServiceHelper.query("bos_user", "dsa1_oamapping",
                new QFilter[]{new QFilter("id", QCP.in, ids)});

        log.info("dynamicObject = {}", dynamicObjects);

        if (dynamicObjects == null || dynamicObjects.isEmpty()) {
            throw new KDBizException("用户:[" + ids + "]的OA映射不存在,请维护!");
        }

        return dynamicObjects.stream()
                .map(obj -> obj.getString("dsa1_oamapping"))
                .filter(phone -> phone != null && !phone.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 单个获取OA登录名
     */
    private String getOaLoginName(Long id) {
        log.info("getOaLoginName single begin, id = {}", id);
        DynamicObject dynamicObject = QueryServiceHelper.queryOne("bos_user", "dsa1_oamapping",
                new QFilter[]{new QFilter("id", QCP.equals, id)});

        if (dynamicObject == null || StringUtils.isEmpty(dynamicObject.getString("dsa1_oamapping"))) {
            throw new KDBizException("用户:[" + id + "]的LoginName不存在,请维护!");
        }

        return dynamicObject.getString("dsa1_oamapping");
    }

    /**
     * 转换URL为相对路径
     */
    private String convertToRelativePath(String url) {
        if (StringUtils.isEmpty(url)) {
            return url;
        }
        try {
            java.net.URL urlObj = new java.net.URL(url);
            String path = urlObj.getPath();
            String query = urlObj.getQuery();
            if (StringUtils.isEmpty(query)) {
                return path;
            } else {
                return path + "?" + query;
            }
        } catch (Exception e) {
            log.error("URL转换异常，使用简单截取方式: {}", e.getMessage());
            int protocolIndex = url.indexOf("://");
            if (protocolIndex != -1) {
                int pathStartIndex = url.indexOf("/", protocolIndex + 3);
                if (pathStartIndex != -1) {
                    return url.substring(pathStartIndex);
                }
            }
            log.warn("无法转换URL为相对路径: {}", url);
            return url;
        }
    }

    /**
     * 逐个用户推送待办
     */
    private void processCreateToDoForUsers(List<String> loginNames, String taskId,
                                           String subject, String relativePath,
                                           String docCreator, Map<String, Object> nodeInfo,
                                           String createToDoPath, Xczx_Log xczxLog) {
        List<String> successUsers = new ArrayList<>();
        List<String> failedUsers = new ArrayList<>();
        Map<String, String> errorMessages = new HashMap<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (String loginName : loginNames) {
            try {
                // 创建待办上下文（单用户）
                NotifyTodoSendContext context = createTodoSendContext(
                        taskId, subject, relativePath, loginName, docCreator, nodeInfo, dateFormat, "0", "0");

                log.info("createToDo context for user {} = {}", loginName, context);

                // 调用OA接口
                String result = callOACreateToDo(context, createToDoPath, xczxLog);

                // 验证结果
                if (validateResult(result)) {
                    successUsers.add(loginName);
                } else {
                    failedUsers.add(loginName);
                    errorMessages.put(loginName, "接口返回失败");
                }

            } catch (Exception e) {
                log.error("处理用户{}推送待办失败", loginName, e);
                failedUsers.add(loginName);
                errorMessages.put(loginName, e.getMessage());
            }
        }

        // 记录处理结果
        log.info("createToDo处理完成：成功{}个，失败{}个", successUsers.size(), failedUsers.size());
        if (!failedUsers.isEmpty()) {
            log.warn("失败的登录名及原因：{}", errorMessages);
            // 如果有失败的用户，标记日志状态为部分失败
            xczxLog.setStatus("3"); // 3表示部分成功
            xczxLog.setResponseMsg("部分用户推送失败: " + errorMessages);
        } else {
            xczxLog.setStatus("0");
        }
    }

    /**
     * 逐个用户推送已办
     */
    private void processDealToDoForUsers(List<String> loginNames, String taskId,
                                         String subject, String url,
                                         String docCreator, Map<String, Object> nodeInfo,
                                         String dealToDoPath, Xczx_Log xczxLog) {
        List<String> successUsers = new ArrayList<>();
        List<String> failedUsers = new ArrayList<>();
        Map<String, String> errorMessages = new HashMap<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (String loginName : loginNames) {
            try {
                // 创建已办上下文（单用户）
                NotifyTodoSendContext context = createTodoSendContext(
                        taskId, subject, url, loginName, docCreator, nodeInfo, dateFormat, "2", "1");

                log.info("dealToDo context for user {} = {}", loginName, context);

                // 调用OA接口
                String result = callOADealToDo(context, dealToDoPath, xczxLog);

                // 验证结果
                if (validateResult(result)) {
                    successUsers.add(loginName);
                } else {
                    failedUsers.add(loginName);
                    errorMessages.put(loginName, "接口返回失败");
                }

            } catch (Exception e) {
                log.error("处理用户{}推送已办失败", loginName, e);
                failedUsers.add(loginName);
                errorMessages.put(loginName, e.getMessage());
            }
        }

        // 记录处理结果
        log.info("dealToDo处理完成：成功{}个，失败{}个", successUsers.size(), failedUsers.size());
        if (!failedUsers.isEmpty()) {
            log.warn("失败的登录名及原因：{}", errorMessages);
            xczxLog.setStatus("3"); // 3表示部分成功
            xczxLog.setResponseMsg("部分用户推送失败: " + errorMessages);
        } else {
            xczxLog.setStatus("0");
        }
    }

    /**
     * 创建待办/已办上下文
     */
    private NotifyTodoSendContext createTodoSendContext(String taskId, String subject,
                                                        String url, String loginName,
                                                        String docCreator, Map<String, Object> nodeInfo,
                                                        SimpleDateFormat dateFormat,
                                                        String isRemark, String viewType) {
        NotifyTodoSendContext context = new NotifyTodoSendContext();
        context.setSyscode("xczx");
        context.setFlowid(taskId);
        context.setRequestname(subject);
        context.setWorkflowname(subject);
        context.setPcurl(url);
        context.setAppurl(url);
        context.setIsremark(isRemark);
        context.setViewtype(viewType);
        context.setReceiver(loginName);  // 只传单个用户
        context.setCreatedatetime(dateFormat.format(nodeInfo.get("createDate")));
        context.setReceivedatetime(dateFormat.format(new Date()));
        context.setCreator(docCreator);

        return context;
    }

    /**
     * 调用OA待办接口
     */
    private String callOACreateToDo(NotifyTodoSendContext context, String createToDoPath,
                                    Xczx_Log xczxLog) {
        try {
            String requestJson = JSON.toJSONString(context);
            xczxLog.setInParam(requestJson);
            xczxLog.setRequestTime(new Timestamp(System.currentTimeMillis()));

            String result = OATaskDataSend.doPost2(requestJson, createToDoPath);
            log.info("createToDo result = {}", result);
            xczxLog.setOutParam(result);

            return result;
        } catch (Exception e) {
            xczxLog.setStatus("2");
            log.error("ApprovalTaskSyncOA createToDo OA待办数据推送异常", e);
            throw new KDBizException(e, new ErrorCode("OACreateToDoDataError",
                    "待办数据推送异常:" + e.getMessage()));
        }
    }

    /**
     * 调用OA已办接口
     */
    private String callOADealToDo(NotifyTodoSendContext context, String dealToDoPath,
                                  Xczx_Log xczxLog) {
        try {
            String requestJson = JSON.toJSONString(context);
            xczxLog.setInParam(requestJson);
            xczxLog.setRequestTime(new Timestamp(System.currentTimeMillis()));

            String result = OATaskDataSend.doPost2(requestJson, dealToDoPath);
            log.info("dealToDo result = {}", result);
            xczxLog.setOutParam(result);

            return result;
        } catch (Exception e) {
            xczxLog.setStatus("2");
            log.error("ApprovalTaskSyncOa dealToDo OA已办数据推送异常", e);
            throw new KDBizException(e, new ErrorCode("OADealToDoDataError",
                    "已办数据推送异常:" + e.getMessage()));
        }
    }

    /**
     * 逐个用户删除待办
     */
    private void processDeleteToDoForUsers(List<String> loginNames, String taskId,
                                           String deleteToDoPath, Xczx_Log xczxLog) {
        List<String> successUsers = new ArrayList<>();
        List<String> failedUsers = new ArrayList<>();
        Map<String, String> errorMessages = new HashMap<>();

        for (String loginName : loginNames) {
            try {
                // 创建删除待办上下文（单用户）
                NotifyTodoRemoveContext context = createRemoveContext(taskId, loginName);
                log.info("deleteToDo context for user {} = {}", loginName, context);

                // 调用OA接口
                String result = callOADeleteToDo(context, deleteToDoPath, xczxLog);

                // 验证结果
                if (validateResult(result)) {
                    successUsers.add(loginName);
                } else {
                    failedUsers.add(loginName);
                    errorMessages.put(loginName, "接口返回失败");
                }

            } catch (Exception e) {
                log.error("处理用户{}删除待办失败", loginName, e);
                failedUsers.add(loginName);
                errorMessages.put(loginName, e.getMessage());
            }
        }

        // 记录处理结果
        log.info("deleteToDo处理完成：成功{}个，失败{}个", successUsers.size(), failedUsers.size());
        if (!failedUsers.isEmpty()) {
            log.warn("失败的登录名及原因：{}", errorMessages);
            xczxLog.setStatus("3"); // 3表示部分成功
            xczxLog.setResponseMsg("部分用户删除失败: " + errorMessages);
        } else {
            xczxLog.setStatus("0");
        }
    }

    /**
     * 创建删除待办上下文
     */
    private NotifyTodoRemoveContext createRemoveContext(String taskId, String loginName) {
        NotifyTodoRemoveContext context = new NotifyTodoRemoveContext();
        context.setSyscode("xczx");
        context.setFlowid(taskId);
        context.setReceiver(loginName);
        return context;
    }

    /**
     * 调用OA删除待办接口
     */
    private String callOADeleteToDo(NotifyTodoRemoveContext context, String deleteToDoPath,
                                    Xczx_Log xczxLog) {
        try {
            String requestJson = JSON.toJSONString(context);
            xczxLog.setInParam(requestJson);
            xczxLog.setRequestTime(new Timestamp(System.currentTimeMillis()));

            String result = OATaskDataSend.doPost2(requestJson, deleteToDoPath);
            log.info("deleteToDo result = {}", result);
            xczxLog.setOutParam(result);

            return result;
        } catch (Exception e) {
            xczxLog.setStatus("2");
            log.error("ApprovalTaskSyncOa deleteToDo OA删除待办异常", e);
            throw new KDBizException(e, new ErrorCode("OADeleteToDoDataError",
                    "删除待办异常:" + e.getMessage()));
        }
    }

    /**
     * 验证调用结果
     */
    private boolean validateResult(String result) {
        try {
            if (StringUtils.isEmpty(result)) {
                log.error("返回结果为空");
                return false;
            }
            NotifyTodoAppResult res = JSON.parseObject(result, NotifyTodoAppResult.class);
            if (res == null) {
                log.error("解析返回结果失败");
                return false;
            }
            // 根据实际接口返回判断成功条件
            return "1".equals(res.getOperResult());
        } catch (Exception e) {
            log.error("验证调用结果失败", e);
            return false;
        }
    }

    /**
     * 处理异常
     */
    private void handleException(Xczx_Log xczxLog, Exception e, String methodName) {
        if (xczxLog.getStatus() == null) {
            xczxLog.setStatus("1");
        }
        xczxLog.setResponseMsg(e.getMessage());
        SyncLogUtil.gxjt_log_save(xczxLog);

        log.error("{}处理失败", methodName, e);
        // 可以选择是否抛出异常
        // throw new KDBizException(e, new ErrorCode("OAProcessError", e.getMessage()));
    }
}