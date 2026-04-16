package dsa1.xczxx.wfs.ws.plugin.task;
import dsa1.xczxx.wfs.ws.common.datalayer.DataActuator;
import dsa1.xczxx.wfs.ws.common.datalayer.DataQueryTool;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.schedule.executor.AbstractTask;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PushBankAccountBalanceTask extends AbstractTask {
    static Log logger = LogFactory.getLog(PushBankAccountBalanceTask.class);
    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {

        logger.info("PushBankAccountBalanceTask start");
        logger.info("requestContext = {}",requestContext);
        logger.info("map = {}",map);
        logger.info("11111111111111111111111111111111111");
        String key = (String) map.get("key");
        Date date = null;
        if ("automatic".equals(key)){
            date = new Date();
        }else if ("manual".equals(key)){
             date=DataQueryTool.geDate(map);
        }
        //有key代表是 每日正常执行 推送前一天数据  没有key 则是手工执行推送历史数据
        /*第一步先 将本系统余额主表 明细表 所需数据查出*/
        //主表
        HashMap<String, DynamicObject> bankbalanceMap = DataQueryTool.getBankAccountBalance(date,key);
        if (bankbalanceMap == null||bankbalanceMap.isEmpty()) {
            logger.error("bankbalanceMap is null");
            return;
        }
        logger.info("bankbalanceMap size "+bankbalanceMap.size());
        //明细表
        DynamicObjectCollection bankAccountBalanceDdetail = DataQueryTool.getBankAccountBalanceDdetail(date,key);
        if (bankAccountBalanceDdetail == null||bankAccountBalanceDdetail.isEmpty()) {
            logger.error("bankAccountBalanceDdetail is null");
            return;
        }
        logger.info("bankAccountBalanceDdetail size "+bankAccountBalanceDdetail.size());

        if ("automatic".equals(key)){
            // 转换为LocalDateTime进行操作
            LocalDateTime localDateTime = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .minusDays(1); // 减1天
             date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        /*第二步对查询数据进行判断 新增还是更新 并进行数据库操作*/
        //主表
        DataActuator.executeBatch_largefund_bank_account_balance(bankbalanceMap,date);
        logger.info("executeBatch_largefund_bank_account_balance end");

        //余额表
        DataActuator.executeBatch_largefund_bank_account_balance_detail(bankAccountBalanceDdetail,date);
        logger.info("executeBatch_largefund_bank_account_balance_detail end");
    }
}
