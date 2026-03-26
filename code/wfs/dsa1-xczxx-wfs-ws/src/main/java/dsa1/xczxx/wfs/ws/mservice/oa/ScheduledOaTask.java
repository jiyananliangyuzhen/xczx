package dsa1.xczxx.wfs.ws.mservice.oa;

import com.alibaba.fastjson.JSON;
import dsa1.xczxx.wfs.ws.mservice.oa.entity.NotifyTodoAppResult;
import dsa1.xczxx.wfs.ws.mservice.oa.entity.NotifyTodoRemoveContext;
import dsa1.xczxx.wfs.ws.mservice.oa.entity.NotifyTodoSendContext;
import dsa1.xczxx.wfs.ws.mservice.oa.entity.TaskType;
import dsa1.xczxx.wfs.ws.util.OATaskDataSend;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.api.MessageHandler;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.DeleteServiceHelper;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * OA定时任务调度类
 * 负责处理待办、已办、删除待办的推送任务
 */
public class ScheduledOaTask extends AbstractTask {
    private static final Log log = LogFactory.getLog(ScheduledOaTask.class);

    // 常量定义
    private static final String ENTITY_NAME = "dsa1_xczx_log";
    private static final String TODO_CODE = "createToDo";
    private static final String DONE_CODE = "dealToDo";
    private static final String DELETE_CODE = "deleteToDo";
    private static final String SUCCESS_CODE = "1";
    private static final String BILLNO_FIELD = "billno";

    // 查询字段常量
    private static final String[] QUERY_FIELDS = {"id", "dsa1_code", "dsa1_inparam",
            "dsa1_requesturl", "dsa1_status", "billno"};


    /**
     * 获取工作流任务ID
     */
    private Optional<Long> getWfTaskId(String billno) {
        try {
            DynamicObject dynamicObject = QueryServiceHelper.queryOne("wf_task", "id",
                    new QFilter[]{new QFilter(BILLNO_FIELD, QCP.equals, billno)});

            if (dynamicObject == null) {
                log.warn("单据流程不存在或已完结: billno={}", billno);
                return Optional.empty();
            }
            return Optional.of(dynamicObject.getLong("id"));
        } catch (Exception e) {
            log.error("查询工作流任务失败: billno={}", billno, e);
            return Optional.empty();
        }
    }

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        log.info("===== OA定时任务开始执行 =====");
        long startTime = System.currentTimeMillis();

        try {
            // 批量查询所有类型的任务
            Map<String, List<DynamicObject>> taskGroups = queryTaskGroups();

            // 记录各类型任务数量
            taskGroups.forEach((code, list) ->
                    log.info("{}任务数量: {}", code, list.size()));

            // 处理各类型任务
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            taskGroups.forEach((code, records) -> {
                TaskType taskType = TaskType.fromCode(code);
                if (taskType != null) {
                    records.forEach(record -> {
                        try {
                            taskType.process(this, record);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                            log.error("处理{}任务失败: recordId={}",
                                    taskType.getDescription(), record.getString("id"), e);
                        }
                    });
                }
            });

            long costTime = System.currentTimeMillis() - startTime;
            log.info("===== OA定时任务执行完成，耗时: {}ms，成功: {}，失败: {} =====",
                    costTime, successCount.get(), failCount.get());

        } catch (Exception e) {
            log.error("OA定时任务执行异常", e);
            throw new KDBizException(e, new ErrorCode("OATask", "OA任务执行异常:" + e.getMessage()));
        }
    }

    /**
     * 批量查询所有类型的任务
     */
    private Map<String, List<DynamicObject>> queryTaskGroups() {
        try {
            QFilter[] filters = new QFilter[]{
                    new QFilter("dsa1_code", QCP.in, new Object[]{TODO_CODE, DONE_CODE, DELETE_CODE})
            };

            DynamicObjectCollection allTasks = QueryServiceHelper.query(
                    ENTITY_NAME,
                    String.join(",", QUERY_FIELDS),
                    filters
            );

            // 按任务类型分组
            return allTasks.stream()
                    .collect(Collectors.groupingBy(
                            task -> task.getString("dsa1_code"),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

        } catch (Exception e) {
            log.error("查询任务数据失败", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 通用处理方法
     */
    private void processWithContext(DynamicObject record, String contextClass,
                                    String successMsg, String errorMsg) {
        Long recordId = record.getLong("id");
        String billno = record.getString(BILLNO_FIELD);
        String requestUrl = record.getString("dsa1_requesturl");
        String inParam = record.getString("dsa1_inparam");

        log.info("开始处理{}: recordId={}, billno={}", errorMsg, recordId, billno);

        try {
            // 验证必要字段
            if (!validateRecord(record)) {
                return;
            }

            // 获取工作流任务ID
            Optional<Long> taskIdOpt = getWfTaskId(billno);
            if (!taskIdOpt.isPresent()) {
                return;
            }

            // 解析上下文并验证flowid
            Object context = parseContext(inParam, contextClass);
            if (!validateFlowId(context, taskIdOpt.get())) {
                log.warn("flowid不匹配: recordId={}, expected={}", recordId, taskIdOpt.get());
                return;
            }

            // 发送请求
            String result = OATaskDataSend.doPost2(inParam, requestUrl);
            log.info("{}响应结果: {}", errorMsg, result);

            // 处理结果
            NotifyTodoAppResult appResult = JSON.parseObject(result, NotifyTodoAppResult.class);

            if (SUCCESS_CODE.equals(appResult.getOperResult())) {
                // 成功则删除日志记录
                deleteLogRecord(recordId);
                log.info("{}处理成功并删除记录: recordId={}", errorMsg, recordId);
            } else {
                log.error("{}处理失败: recordId={}, message={}",
                        errorMsg, recordId, appResult.getMessage());
            }

        } catch (Exception e) {
            log.error("{}处理异常: recordId={}", errorMsg, recordId, e);
        }
    }

    /**
     * 验证记录的必要字段
     */
    private boolean validateRecord(DynamicObject record) {
        if (record.getString(BILLNO_FIELD) == null) {
            log.warn("记录缺少billno字段: id={}", record.getLong("id"));
            return false;
        }
        if (record.getString("dsa1_requesturl") == null) {
            log.warn("记录缺少requesturl字段: id={}", record.getLong("id"));
            return false;
        }
        if (record.getString("dsa1_inparam") == null) {
            log.warn("记录缺少inparam字段: id={}", record.getLong("id"));
            return false;
        }
        return true;
    }

    /**
     * 解析上下文对象
     */
    private Object parseContext(String inParam, String contextClass) {
        switch (contextClass) {
            case "NotifyTodoSendContext":
                return JSON.parseObject(inParam, NotifyTodoSendContext.class);
            case "NotifyTodoRemoveContext":
                return JSON.parseObject(inParam, NotifyTodoRemoveContext.class);
            default:
                throw new IllegalArgumentException("未知的上下文类型: " + contextClass);
        }
    }

    /**
     * 验证flowid
     */
    private boolean validateFlowId(Object context, Long taskId) {
        String flowId = null;
        if (context instanceof NotifyTodoSendContext) {
            flowId = ((NotifyTodoSendContext) context).getFlowid();
        } else if (context instanceof NotifyTodoRemoveContext) {
            flowId = ((NotifyTodoRemoveContext) context).getFlowid();
        }
        return taskId.toString().equals(flowId);
    }

    /**
     * 删除日志记录
     */
    private void deleteLogRecord(Long recordId) {
        try {
            DeleteServiceHelper.delete(ENTITY_NAME,
                    new QFilter[]{new QFilter("id", QCP.equals, recordId)});
        } catch (Exception e) {
            log.error("删除日志记录失败: recordId={}", recordId, e);
        }
    }

    /**
     * 处理待办创建任务
     */
    public void processTodo(DynamicObject record) {
        processWithContext(record, "NotifyTodoSendContext",
                "待办创建成功", "待办创建");
    }

    /**
     * 处理待办完成任务
     */
    public void processDone(DynamicObject record) {
        processWithContext(record, "NotifyTodoRemoveContext",
                "待办处理成功", "待办处理");
    }

    /**
     * 处理待办删除任务
     */
    public void processDelete(DynamicObject record) {
        processWithContext(record, "NotifyTodoRemoveContext",
                "待办删除成功", "待办删除");
    }

    @Override
    public MessageHandler getMessageHandle() {
        return super.getMessageHandle();
    }

    @Override
    public boolean isSupportReSchedule() {
        return super.isSupportReSchedule();
    }
}