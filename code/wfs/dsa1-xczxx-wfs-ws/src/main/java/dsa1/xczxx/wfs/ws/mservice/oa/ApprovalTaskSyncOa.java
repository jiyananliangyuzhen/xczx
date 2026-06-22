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
            // 1. 验证配置路径  待办和已办接口地址一致
            String dealToDoPath = System.getProperty("createtodo");
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

        String system = System.getProperty("system");
        log.info("convertToRelativePath - system = [{}], url = {}", system, url);

        try {
            java.net.URL urlObj = new java.net.URL(url);
            String path = urlObj.getPath();
            String query = urlObj.getQuery();

            log.info("convertToRelativePath - 解析后 path = [{}], query = [{}]", path, query);

            if (system != null && system.equals("prod")) {
                // 生产环境：去掉 /ierp 和 /ext
                if (path != null) {
                    String originalPath = path;
                    path = removePathPrefixes(path);
                    log.info("convertToRelativePath - 路径转换: [{}] -> [{}]", originalPath, path);
                }
            } else {
                log.info("convertToRelativePath - 非生产环境，不处理前缀，system = {}", system);
            }

            if (StringUtils.isEmpty(query)) {
                return path;
            } else {
                return path + "?" + query;
            }
        } catch (Exception e) {
            log.error("convertToRelativePath - URL转换异常，使用备用方案: {}", e.getMessage(), e);

            // 备用方案：手动截取路径部分
            try {
                String processedUrl = url;

                // 找到协议后的第一个斜杠
                int protocolIndex = url.indexOf("://");
                if (protocolIndex != -1) {
                    int pathStartIndex = url.indexOf("/", protocolIndex + 3);
                    if (pathStartIndex != -1) {
                        String originalPath = url.substring(pathStartIndex);
                        // 分离路径和查询参数
                        String pathPart = originalPath;
                        String queryPart = null;
                        int queryIndex = originalPath.indexOf('?');
                        if (queryIndex != -1) {
                            pathPart = originalPath.substring(0, queryIndex);
                            queryPart = originalPath.substring(queryIndex + 1);
                        }

                        // 处理路径前缀
                        String processedPath = removePathPrefixes(pathPart);

                        if (queryPart != null) {
                            return processedPath + "?" + queryPart;
                        } else {
                            return processedPath;
                        }
                    }
                }

                // 最后的备用方案：直接处理整个URL
                String result = removePathPrefixes(url);
                log.warn("convertToRelativePath - 最终备用方案结果: {} -> {}", url, result);
                return result;

            } catch (Exception ex) {
                log.error("convertToRelativePath - 备用方案也失败了: {}", ex.getMessage(), ex);
                return url;
            }
        }
    }

    /**
     * 去掉路径中的 /ierp 和 /ext
     * @param path 原始路径
     * @return 处理后的路径
     */
    private String removePathPrefixes(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }

        String result = path;

        // 1. 处理开头的 /ierp/
        while (result.startsWith("/ierp/")) {
            result = result.substring(5);
            log.debug("removePathPrefixes - 去掉 /ierp/ 后: {}", result);
        }

        // 2. 处理开头单独的 /ierp
        if (result.equals("/ierp")) {
            result = "";
            log.debug("removePathPrefixes - 去掉 /ierp 后: {}", result);
        }

        // 3. 处理开头的 /ext/
        while (result.startsWith("/ext/")) {
            result = result.substring(4);
            log.debug("removePathPrefixes - 去掉 /ext/ 后: {}", result);
        }

        // 4. 处理开头单独的 /ext
        if (result.equals("/ext")) {
            result = "";
            log.debug("removePathPrefixes - 去掉 /ext 后: {}", result);
        }

        // 5. 处理路径中间包含 /ierp/ 或 /ext/ 的情况（兼容性保留）
        if (result.contains("/ierp/")) {
            result = result.replace("/ierp/", "/");
            log.debug("removePathPrefixes - 替换中间的 /ierp/ 后: {}", result);
        }
        if (result.contains("/ext/")) {
            result = result.replace("/ext/", "/");
            log.debug("removePathPrefixes - 替换中间的 /ext/ 后: {}", result);
        }

        // 6. 如果结果变成空字符串，返回根路径
        if (StringUtils.isEmpty(result)) {
            result = "/";
        }

        return result;
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