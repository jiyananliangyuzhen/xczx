package dsa1.xczxx.wfs.ws.mservice.sk;

import kd.bos.context.RequestContext;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.schedule.executor.AbstractTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 付款单推送司库定时任务
 *
 * 自动模式：map中key="automatic"，推送近3天数据
 * 手动模式：map中key="manual"，需传入startDate和endDate
 */
public class PaymentPushSchedule extends AbstractTask {

    private static final Log log = LogFactory.getLog(PaymentPushSchedule.class);
    private static final PaymentPushToSKService service = new PaymentPushToSKService();

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) {
        log.info("【付款单推送司库定时任务】开始执行");
        log.info("requestContext = {}", requestContext);
        log.info("map = {}", map);

        try {
            String key = (String) map.get("key");

            if ("automatic".equals(key)) {
                // 自动模式：近3天
                log.info("执行自动模式：推送近3天数据");
                service.pushAuto();
            } else if ("manual".equals(key)) {
                // 手动模式：指定日期
                String startDateStr = (String) map.get("startDate");
                String endDateStr = (String) map.get("endDate");

                log.info("执行手动模式：startDate={}, endDate={}", startDateStr, endDateStr);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = sdf.parse(startDateStr);
                Date endDate = sdf.parse(endDateStr);
                service.pushManual(startDate, endDate);
            } else {
                log.warn("未知的key值：{}，支持 automatic 或 manual", key);
            }

            log.info("【付款单推送司库定时任务】执行完成");

        } catch (Exception e) {
            log.error("【付款单推送司库定时任务】执行失败：{}", e.getMessage(), e);
        }
    }


}