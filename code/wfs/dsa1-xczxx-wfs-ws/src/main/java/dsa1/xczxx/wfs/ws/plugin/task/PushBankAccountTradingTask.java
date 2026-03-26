package dsa1.xczxx.wfs.ws.plugin.task;

import dsa1.xczxx.wfs.ws.common.datalayer.DataActuator;
import dsa1.xczxx.wfs.ws.common.datalayer.DataQueryTool;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.schedule.executor.AbstractTask;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

public class PushBankAccountTradingTask extends AbstractTask {
    static Log logger = LogFactory.getLog(PushBankAccountTradingTask.class);
    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        logger.info("PushBankAccountTradingTask start");
        logger.info("requestContext = {}",requestContext);
        logger.info("map = {}",map);
        Date date= DataQueryTool.geDate(map);
        //有key代表是 每日正常执行  没有key 则是手工执行推送历史数据
        String key = (String) map.get("key");

        DynamicObjectCollection bankAccountTradingCollection = DataQueryTool.getBankAccountTrading(date,key);
        if (bankAccountTradingCollection == null||bankAccountTradingCollection.isEmpty()) {
            logger.error("bankAccountTradingCollection is null");
            return;
        }
        logger.info("bankAccountBalanceTrading size "+bankAccountTradingCollection.size());
//        if ("automatic".equals(key)){
//            // 转换为LocalDateTime进行操作
//            LocalDateTime localDateTime = date.toInstant()
//                    .atZone(ZoneId.systemDefault())
//                    .toLocalDateTime()
//                    .minusDays(1); // 减1天
//            date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
//        }
        DataActuator.executeBatch_largefund_bank_account_trading(bankAccountTradingCollection);
        logger.info("executeBatch_largefund_bank_account_trading end");
    }
}
