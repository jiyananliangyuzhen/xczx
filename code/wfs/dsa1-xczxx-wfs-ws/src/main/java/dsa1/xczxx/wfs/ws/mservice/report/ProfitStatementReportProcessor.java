package dsa1.xczxx.wfs.ws.mservice.report;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

public class ProfitStatementReportProcessor {

    private final static Log log = LogFactory.getLog(ProfitStatementReportProcessor.class);


    // 表名常量 - 替换为您的实际表名

    private static final String FIELD_ID = "id";
    private static final String FIELD_CREATE_TIME = "dsa1_createtime";
    private static final String FIELD_MODIFY_TIME = "dsa1_modifytime";

    // 维度字段映射（API字段名 -> 数据库字段名）
    // 维度字段映射（API字段名 -> 数据库字段名）
    private static final Map<String, String> DIMENSION_FIELD_MAP = new HashMap<>();
    // 报表代码与表名的映射
    private static final Map<String, String> REPORT_TABLE_MAP = new HashMap<>();
    // 报表与科目字段映射的映射
    private static final Map<String, Map<String, String>> REPORT_ACCOUNT_FIELD_MAP = new HashMap<>();

    static {
        // 初始化维度字段映射
        DIMENSION_FIELD_MAP.put("Entity", "dsa1_organization_code");      // 组织
        DIMENSION_FIELD_MAP.put("BudgetPeriod", "dsa1_period");           // 期间
        DIMENSION_FIELD_MAP.put("Version", "dsa1_version_code");          // 版本
        DIMENSION_FIELD_MAP.put("ChangeType", "dsa1_change_type");        // 变动类型
        DIMENSION_FIELD_MAP.put("DataType", "dsa1_data_type");            // 数据类型
        DIMENSION_FIELD_MAP.put("Metric", "dsa1_metric");                 // 度量
        DIMENSION_FIELD_MAP.put("AuditTrail", "dsa1_audit_trail");        // 线索
        DIMENSION_FIELD_MAP.put("Currency", "dsa1_currency");             // 币种
        DIMENSION_FIELD_MAP.put("CPJD", "dsa1_cpjd");                     // 自定义维度
        DIMENSION_FIELD_MAP.put("CPJD1", "dsa1_cpjd1");                   // 自定义维度
        DIMENSION_FIELD_MAP.put("CostItem", "dsa1_costitem");             // 费用项目
        DIMENSION_FIELD_MAP.put("InvoiceType", "dsa1_invoicetype");       // 发票类型
        DIMENSION_FIELD_MAP.put("InvoiceTaxRate", "dsa1_invoicetaxrate"); // 发票税率
        DIMENSION_FIELD_MAP.put("InputTaxType", "dsa1_inputtaxtype");     // 进项税类型

        // 初始化GZQY11报表的科目字段映射
        Map<String, String> gzqy11AccountMap = new HashMap<>();
        gzqy11AccountMap.put("GZQY11-YYZSR", "dsa1_yyzsr_ys");
        gzqy11AccountMap.put("GZQY11-YYZSR.01", "dsa1_yyzsr01_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB", "dsa1_yyzcb_ys");
        gzqy11AccountMap.put("SJYS", "dsa1_yyzcb09_ys");
        gzqy11AccountMap.put("XSFY", "dsa1_yyzcb10_ys");
        gzqy11AccountMap.put("GLFY", "dsa1_yyzcb11_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB.13", "dsa1_yyzcb13_ys");
        gzqy11AccountMap.put("GZQY11-YYLR", "dsa1_yylr_ys");
        gzqy11AccountMap.put("GZQY11-LRZE", "dsa1_lrze_ys");
        gzqy11AccountMap.put("GZQY11-JLR", "dsa1_jlr_ys");
        gzqy11AccountMap.put("GZQY11-JLR.01", "dsa1_jlr01_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB.21", "dsa1_yyzcb21_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB.24", "dsa1_yyzcb24_ys");
        gzqy11AccountMap.put("GZQY11-YYZSR.05", "dsa1_yyzsr05_ys");
        gzqy11AccountMap.put("YYWSR", "dsa1_yylr01_ys");
        gzqy11AccountMap.put("YYWZC", "dsa1_yylr02_ys");
        gzqy11AccountMap.put("SJYS14", "dsa1_lrze01_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB.01", "dsa1_yyzcb01_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB.12", "dsa1_yyzcb12_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB.17", "dsa1_yyzcb17_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB.18", "dsa1_yyzcb18_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB.16", "dsa1_yyzcb16_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB.20", "dsa1_yyzcb20_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB.19", "dsa1_yyzcb19_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB.14", "dsa1_yyzcb14_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB.22", "dsa1_yyzcb22_ys");
        gzqy11AccountMap.put("GZQY11-MGSJLR", "dsa1_mgsjlr_ys");
        gzqy11AccountMap.put("GZQY11-FDGJJ", "dsa1_fdgjj_ys");
        gzqy11AccountMap.put("GZQY11-GJLR", "dsa1_gjlr_ys");
        gzqy11AccountMap.put("GZQY11-YYZCB.15", "dsa1_yyzcb15_ys");

        REPORT_ACCOUNT_FIELD_MAP.put("GZQY11", gzqy11AccountMap);

        // 初始化GZQY13报表的科目字段映射
        Map<String, String> gzqy13AccountMap = new HashMap<>();
        gzqy13AccountMap.put("GZQY13.01.01", "dsa1_gzqy130101");
        gzqy13AccountMap.put("GZQY13.01.04", "dsa1_gzqy130104");
        gzqy13AccountMap.put("GZQY13.01.05", "dsa1_gzqy130105");
        gzqy13AccountMap.put("GZQY13.01.06", "dsa1_gzqy130106");
        gzqy13AccountMap.put("GZQY13.01.07", "dsa1_gzqy130107");
        gzqy13AccountMap.put("GZQY13.01.08", "dsa1_gzqy130108");
        gzqy13AccountMap.put("GZQY13.01.09", "dsa1_gzqy130109");
        gzqy13AccountMap.put("GZQY13.01.13", "dsa1_gzqy130113");
        gzqy13AccountMap.put("GZQY13.01.14", "dsa1_gzqy130114");
        gzqy13AccountMap.put("GZQY13.01.16", "dsa1_gzqy130116");
        gzqy13AccountMap.put("GZQY13.01.17", "dsa1_gzqy130117");
        gzqy13AccountMap.put("GZQY13.01.18", "dsa1_gzqy130118");
        gzqy13AccountMap.put("GZQY13.01.19", "dsa1_gzqy130119");
        gzqy13AccountMap.put("GZQY13.01.20", "dsa1_gzqy130120");
        gzqy13AccountMap.put("GZQY13.01", "dsa1_gzqy1301");
        gzqy13AccountMap.put("GZQY13.07", "dsa1_gzqy1307");
        gzqy13AccountMap.put("GZQY13.02", "dsa1_gzqy1302");
        gzqy13AccountMap.put("GZQY13.02.02", "dsa1_gzqy130202");
        gzqy13AccountMap.put("GZQY13.02.03", "dsa1_gzqy130203");
        gzqy13AccountMap.put("GZQY13.02.04", "dsa1_gzqy130204");
        gzqy13AccountMap.put("GZQY13.02.05", "dsa1_gzqy130205");
        gzqy13AccountMap.put("GZQY13.02.06", "dsa1_gzqy130206");
        gzqy13AccountMap.put("GZQY13.02.07", "dsa1_gzqy130207");
        gzqy13AccountMap.put("GZQY13.02.08", "dsa1_gzqy130208");
        gzqy13AccountMap.put("GZQY13.02.09", "dsa1_gzqy130209");
        gzqy13AccountMap.put("GZQY13.02.10", "dsa1_gzqy130210");
        gzqy13AccountMap.put("GZQY13.02.11", "dsa1_gzqy130211");
        gzqy13AccountMap.put("GZQY13.02.12", "dsa1_gzqy130212");
        gzqy13AccountMap.put("GZQY13.02.13", "dsa1_gzqy130213");
        gzqy13AccountMap.put("GZQY13.02.14", "dsa1_gzqy130214");
        gzqy13AccountMap.put("GZQY13.02.15", "dsa1_gzqy130215");
        gzqy13AccountMap.put("GZQY13.02.16", "dsa1_gzqy130216");
        gzqy13AccountMap.put("GZQY13.02.17", "dsa1_gzqy130217");
        gzqy13AccountMap.put("GZQY13.02.18", "dsa1_gzqy130218");
        gzqy13AccountMap.put("GZQY13.02.19", "dsa1_gzqy130219");
        gzqy13AccountMap.put("GZQY13.11", "dsa1_gzqy1311");
        gzqy13AccountMap.put("GZQY13.12", "dsa1_gzqy1312");
        gzqy13AccountMap.put("GZQY13.14", "dsa1_gzqy1314");
        gzqy13AccountMap.put("GZQY13.05", "dsa1_gzqy1305");
        gzqy13AccountMap.put("GZQY13.10", "dsa1_gzqy1310");
        gzqy13AccountMap.put("GZQY13.05.01", "dsa1_gzqy130501");
        gzqy13AccountMap.put("GZQY13.05.02", "dsa1_gzqy130502");
        gzqy13AccountMap.put("GZQY13.05.03", "dsa1_gzqy130503");
        gzqy13AccountMap.put("GZQY13.05.04", "dsa1_gzqy130504");
        gzqy13AccountMap.put("GZQY13.05.05", "dsa1_gzqy130505");
        gzqy13AccountMap.put("GZQY13.05.06", "dsa1_gzqy130506");
        gzqy13AccountMap.put("GZQY13.05.07", "dsa1_gzqy130507");
        gzqy13AccountMap.put("GZQY13.05.08", "dsa1_gzqy130508");
        gzqy13AccountMap.put("GZQY13.05.09", "dsa1_gzqy130509");
        gzqy13AccountMap.put("GZQY13.05.11", "dsa1_gzqy130511");
        gzqy13AccountMap.put("GZQY13.03", "dsa1_gzqy1303");
        gzqy13AccountMap.put("GZQY13.04", "dsa1_gzqy1304");
        gzqy13AccountMap.put("GZQY13.03.01", "dsa1_gzqy130301");
        gzqy13AccountMap.put("GZQY13.03.04", "dsa1_gzqy130304");
        gzqy13AccountMap.put("GZQY13.03.05", "dsa1_gzqy130305");
        gzqy13AccountMap.put("GZQY13.03.06", "dsa1_gzqy130306");
        gzqy13AccountMap.put("GZQY13.03.07", "dsa1_gzqy130307");
        gzqy13AccountMap.put("GZQY13.03.08", "dsa1_gzqy130308");
        gzqy13AccountMap.put("GZQY13.03.09", "dsa1_gzqy130309");
        gzqy13AccountMap.put("GZQY13.03.14", "dsa1_gzqy130314");
        gzqy13AccountMap.put("GZQY13.03.15", "dsa1_gzqy130315");
        gzqy13AccountMap.put("GZQY13.03.16", "dsa1_gzqy130316");
        gzqy13AccountMap.put("GZQY13.03.19", "dsa1_gzqy130319");
        gzqy13AccountMap.put("GZQY13.03.20", "dsa1_gzqy130320");
        gzqy13AccountMap.put("GZQY13.03.21", "dsa1_gzqy130321");
        gzqy13AccountMap.put("GZQY13.04.02", "dsa1_gzqy130402");
        gzqy13AccountMap.put("GZQY13.04.03", "dsa1_gzqy130403");
        gzqy13AccountMap.put("GZQY13.04.04", "dsa1_gzqy130404");
        gzqy13AccountMap.put("GZQY13.04.05", "dsa1_gzqy130405");
        gzqy13AccountMap.put("GZQY13.04.06", "dsa1_gzqy130406");
        gzqy13AccountMap.put("GZQY13.04.07", "dsa1_gzqy130407");
        gzqy13AccountMap.put("GZQY13.04.08", "dsa1_gzqy130408");
        gzqy13AccountMap.put("GZQY13.04.09", "dsa1_gzqy130409");
        gzqy13AccountMap.put("GZQY13.04.10", "dsa1_gzqy130410");

        // 根据实际需求添加更多科目映射
        REPORT_ACCOUNT_FIELD_MAP.put("GZQY13", gzqy13AccountMap);


        Map<String, String> cwfyAccountMap = new HashMap<>();
        cwfyAccountMap.put("CWFY01", "dsa1_cwfy01");
        cwfyAccountMap.put("CWFY02", "dsa1_cwfy02");
        cwfyAccountMap.put("CWFY02.01", "dsa1_cwfy0201");
        cwfyAccountMap.put("CWFY02.02", "dsa1_cwfy0202");
        cwfyAccountMap.put("CWFY03", "dsa1_cwfy03");
        cwfyAccountMap.put("CWFY03.01", "dsa1_cwfy0301");
        cwfyAccountMap.put("CWFY03.02", "dsa1_cwfy0302");
        cwfyAccountMap.put("CWFY03.03", "dsa1_cwfy0303");
        cwfyAccountMap.put("CWFY03.04", "dsa1_cwfy0304");
        cwfyAccountMap.put("CWFY03.05", "dsa1_cwfy0305");
        cwfyAccountMap.put("CWFY04", "dsa1_cwfy04");
        cwfyAccountMap.put("CWFY04.01", "dsa1_cwfy0401");
        cwfyAccountMap.put("CWFY04.02", "dsa1_cwfy0402");
        cwfyAccountMap.put("CWFY04.03", "dsa1_cwfy0403");
        cwfyAccountMap.put("CWFY04.04", "dsa1_cwfy0404");
        cwfyAccountMap.put("CWFY05", "dsa1_cwfy05");
        cwfyAccountMap.put("CWFY06", "dsa1_cwfy06");

        REPORT_ACCOUNT_FIELD_MAP.put("CWFY", cwfyAccountMap);


        Map<String, String> glfyAccountMap = new HashMap<>();
        glfyAccountMap.put("GLFY01", "dsa1_glfy01");
        glfyAccountMap.put("GLFY01.01", "dsa1_glfy0101");
        glfyAccountMap.put("GLFY01.01.01", "dsa1_glfy010101");
        glfyAccountMap.put("GLFY01.01.02", "dsa1_glfy010102");
        glfyAccountMap.put("GLFY01.02", "dsa1_glfy0102");
        glfyAccountMap.put("GLFY01.03", "dsa1_glfy0103");
        glfyAccountMap.put("GLFY01.03.01", "dsa1_glfy010301");
        glfyAccountMap.put("GLFY01.03.02", "dsa1_glfy010302");
        glfyAccountMap.put("GLFY01.04", "dsa1_glfy0104");
        glfyAccountMap.put("GLFY01.04.02", "dsa1_glfy010402");
        glfyAccountMap.put("GLFY01.05", "dsa1_glfy0105");
        glfyAccountMap.put("GLFY01.05.01", "dsa1_glfy010501");
        glfyAccountMap.put("GLFY01.05.02", "dsa1_glfy010502");
        glfyAccountMap.put("GLFY01.06", "dsa1_glfy0106");
        glfyAccountMap.put("GLFY01.07", "dsa1_glfy0107");
        glfyAccountMap.put("GLFY01.08", "dsa1_glfy0108");
        glfyAccountMap.put("GLFY01.09", "dsa1_glfy0109");
        glfyAccountMap.put("GLFY01.10", "dsa1_glfy0110");
        glfyAccountMap.put("GLFY01.10.01", "dsa1_glfy011001");
        glfyAccountMap.put("GLFY01.10.02", "dsa1_glfy011002");
        glfyAccountMap.put("GLFY01.11.01", "dsa1_glfy011101");
        glfyAccountMap.put("GLFY01.11.02", "dsa1_glfy011102");
        glfyAccountMap.put("GLFY02", "dsa1_glfy02");
        glfyAccountMap.put("GLFY03", "dsa1_glfy03");
        glfyAccountMap.put("GLFY03.01", "dsa1_glfy0301");
        glfyAccountMap.put("GLFY03.02", "dsa1_glfy0302");
        glfyAccountMap.put("GLFY03.03", "dsa1_glfy0303");
        glfyAccountMap.put("GLFY03.04", "dsa1_glfy0304");
        glfyAccountMap.put("GLFY03.05", "dsa1_glfy0305");
        glfyAccountMap.put("GLFY03.06", "dsa1_glfy0306");
        glfyAccountMap.put("GLFY04", "dsa1_glfy04");
        glfyAccountMap.put("GLFY04.01", "dsa1_glfy0401");
        glfyAccountMap.put("GLFY04.02", "dsa1_glfy0402");
        glfyAccountMap.put("GLFY05", "dsa1_glfy05");
        glfyAccountMap.put("GLFY05.01", "dsa1_glfy0501");
        glfyAccountMap.put("GLFY05.01.01", "dsa1_glfy050101");
        glfyAccountMap.put("GLFY05.01.02", "dsa1_glfy050102");
        glfyAccountMap.put("GLFY05.02", "dsa1_glfy0502");
        glfyAccountMap.put("GLFY05.03", "dsa1_glfy0503");
        glfyAccountMap.put("GLFY06", "dsa1_glfy06");
        glfyAccountMap.put("GLFY06.01", "dsa1_glfy0601");
        glfyAccountMap.put("GLFY06.02", "dsa1_glfy0602");
        glfyAccountMap.put("GLFY06.03", "dsa1_glfy0603");
        glfyAccountMap.put("GLFY06.04", "dsa1_glfy0604");
        glfyAccountMap.put("GLFY06.05", "dsa1_glfy0605");
        glfyAccountMap.put("GLFY06.06", "dsa1_glfy0606");
        glfyAccountMap.put("GLFY07", "dsa1_glfy07");
        glfyAccountMap.put("GLFY07.01", "dsa1_glfy0701");
        glfyAccountMap.put("GLFY07.03", "dsa1_glfy0703");
        glfyAccountMap.put("GLFY08", "dsa1_glfy08");
        glfyAccountMap.put("GLFY08.01", "dsa1_glfy0801");
        glfyAccountMap.put("GLFY08.02", "dsa1_glfy0802");
        glfyAccountMap.put("GLFY08.03", "dsa1_glfy0803");
        glfyAccountMap.put("GLFY09", "dsa1_glfy09");
        glfyAccountMap.put("GLFY10", "dsa1_glfy10");
        glfyAccountMap.put("GLFY11", "dsa1_glfy11");
        glfyAccountMap.put("GLFY12", "dsa1_glfy12");
        glfyAccountMap.put("GLFY13", "dsa1_glfy13");
        glfyAccountMap.put("GLFY13.01", "dsa1_glfy1301");
        glfyAccountMap.put("GLFY13.02", "dsa1_glfy1302");
        glfyAccountMap.put("GLFY14", "dsa1_glfy14");
        glfyAccountMap.put("GLFY15", "dsa1_glfy15");
        glfyAccountMap.put("GLFY16", "dsa1_glfy16");
        glfyAccountMap.put("GLFY17", "dsa1_glfy17");
        glfyAccountMap.put("GLFY17.01", "dsa1_glfy1701");
        glfyAccountMap.put("GLFY17.02", "dsa1_glfy1702");
        glfyAccountMap.put("GLFY17.03", "dsa1_glfy1703");
        glfyAccountMap.put("GLFY17.04", "dsa1_glfy1704");
        glfyAccountMap.put("GLFY18", "dsa1_glfy18");
        glfyAccountMap.put("GLFY18.01", "dsa1_glfy1801");
        glfyAccountMap.put("GLFY18.02", "dsa1_glfy1802");
        glfyAccountMap.put("GLFY18.03", "dsa1_glfy1803");
        glfyAccountMap.put("GLFY18.04", "dsa1_glfy1804");
        glfyAccountMap.put("GLFY18.05", "dsa1_glfy1805");
        glfyAccountMap.put("GLFY18.06", "dsa1_glfy1806");
        glfyAccountMap.put("GLFY18.07", "dsa1_glfy1807");
        glfyAccountMap.put("GLFY18.08", "dsa1_glfy1808");
        glfyAccountMap.put("GLFY19", "dsa1_glfy19");
        glfyAccountMap.put("GLFY20", "dsa1_glfy20");
        glfyAccountMap.put("GLFY21", "dsa1_glfy21");
        glfyAccountMap.put("GLFY22", "dsa1_glfy22");
        glfyAccountMap.put("GLFY23", "dsa1_glfy23");
        glfyAccountMap.put("GLFY24", "dsa1_glfy24");
        glfyAccountMap.put("GLFY25", "dsa1_glfy25");
        glfyAccountMap.put("GLFY26", "dsa1_glfy26");
        glfyAccountMap.put("GLFY27", "dsa1_glfy27");
        glfyAccountMap.put("GLFY28", "dsa1_glfy28");
        glfyAccountMap.put("GLFY29", "dsa1_glfy29");
        glfyAccountMap.put("GLFY30", "dsa1_glfy30");
        glfyAccountMap.put("GLFY31", "dsa1_glfy31");
        glfyAccountMap.put("GLFY32", "dsa1_glfy32");
        glfyAccountMap.put("GLFY33", "dsa1_glfy33");
        glfyAccountMap.put("GLFY34", "dsa1_glfy34");
        glfyAccountMap.put("GLFY34.01", "dsa1_glfy3401");
        glfyAccountMap.put("GLFY34.02", "dsa1_glfy3402");
        glfyAccountMap.put("GLFY34.03", "dsa1_glfy3403");
        glfyAccountMap.put("GLFY34.04", "dsa1_glfy3404");

        REPORT_ACCOUNT_FIELD_MAP.put("GLFY", glfyAccountMap);



        Map<String, String> xsfyAccountMap = new HashMap<>();

        xsfyAccountMap.put("XSFY01", "dsa1_xsfy01");
        xsfyAccountMap.put("XSFY01.01", "dsa1_xsfy0101");
        xsfyAccountMap.put("XSFY01.01.01", "dsa1_xsfy010101");
        xsfyAccountMap.put("XSFY01.01.02", "dsa1_xsfy010102");
        xsfyAccountMap.put("XSFY01.02", "dsa1_xsfy0102");
        xsfyAccountMap.put("XSFY01.03", "dsa1_xsfy0103");
        xsfyAccountMap.put("XSFY01.03.01", "dsa1_xsfy010301");
        xsfyAccountMap.put("XSFY01.03.02", "dsa1_xsfy010302");
        xsfyAccountMap.put("XSFY01.04", "dsa1_xsfy0104");
        xsfyAccountMap.put("XSFY01.04.01", "dsa1_xsfy010401");
        xsfyAccountMap.put("XSFY01.04.02", "dsa1_xsfy010402");
        xsfyAccountMap.put("XSFY01.05", "dsa1_xsfy0105");
        xsfyAccountMap.put("XSFY01.51", "dsa1_xsfy0151");
        xsfyAccountMap.put("XSFY01.52", "dsa1_xsfy0152");
        xsfyAccountMap.put("XSFY01.06", "dsa1_xsfy0106");
        xsfyAccountMap.put("XSFY02", "dsa1_xsfy02");
        xsfyAccountMap.put("XSFY01.07", "dsa1_xsfy0107");
        xsfyAccountMap.put("XSFY01.08", "dsa1_xsfy0108");
        xsfyAccountMap.put("XSFY01.09", "dsa1_xsfy0109");
        xsfyAccountMap.put("XSFY01.09.01", "dsa1_xsfy010901");
        xsfyAccountMap.put("XSFY01.09.02", "dsa1_xsfy010902");
        xsfyAccountMap.put("XSFY01.10", "dsa1_xsfy0110");
        xsfyAccountMap.put("XSFY01.10.01", "dsa1_xsfy011001");
        xsfyAccountMap.put("XSFY01.10.02", "dsa1_xsfy011002");
        xsfyAccountMap.put("XSFY03", "dsa1_xsfy03");
        xsfyAccountMap.put("XSFY04-a", "dsa1_xsfy04a");
        xsfyAccountMap.put("XSFY04.01", "dsa1_xsfy0401");
        xsfyAccountMap.put("XSFY04.02", "dsa1_xsfy0402");
        xsfyAccountMap.put("XSFY04.03", "dsa1_xsfy0403");
        xsfyAccountMap.put("XSFY04.04", "dsa1_xsfy0404");
        xsfyAccountMap.put("XSFY04.05", "dsa1_xsfy0405");
        xsfyAccountMap.put("XSFY04.06", "dsa1_xsfy0406");
        xsfyAccountMap.put("XSFY05", "dsa1_xsfy05");
        xsfyAccountMap.put("XSFY05.01", "dsa1_xsfy0501");
        xsfyAccountMap.put("XSFY05.02", "dsa1_xsfy0502");
        xsfyAccountMap.put("XSFY06", "dsa1_xsfy06");
        xsfyAccountMap.put("XSFY06.01", "dsa1_xsfy0601");
        xsfyAccountMap.put("XSFY06.02", "dsa1_xsfy0602");
        xsfyAccountMap.put("XSFY07", "dsa1_xsfy07");
        xsfyAccountMap.put("XSFY08", "dsa1_xsfy08");
        xsfyAccountMap.put("XSFY08.01", "dsa1_xsfy0801");
        xsfyAccountMap.put("XSFY08.02", "dsa1_xsfy0802");
        xsfyAccountMap.put("XSFY08.03", "dsa1_xsfy0803");
        xsfyAccountMap.put("XSFY09", "dsa1_xsfy09");
        xsfyAccountMap.put("XSFY10", "dsa1_xsfy10");
        xsfyAccountMap.put("XSFY11", "dsa1_xsfy11");
        xsfyAccountMap.put("XSFY12", "dsa1_xsfy12");
        xsfyAccountMap.put("XSFY13", "dsa1_xsfy13");
        xsfyAccountMap.put("XSFY14", "dsa1_xsfy14");
        xsfyAccountMap.put("XSFY15", "dsa1_xsfy15");
        xsfyAccountMap.put("XSFY16", "dsa1_xsfy16");
        xsfyAccountMap.put("XSFY17", "dsa1_xsfy17");
        xsfyAccountMap.put("XSFY17.01", "dsa1_xsfy1701");
        xsfyAccountMap.put("XSFY17.02", "dsa1_xsfy1702");
        xsfyAccountMap.put("XSFY18", "dsa1_xsfy18");
        xsfyAccountMap.put("XSFY19", "dsa1_xsfy19");
        xsfyAccountMap.put("XSFY20", "dsa1_xsfy20");
        xsfyAccountMap.put("XSFY21", "dsa1_xsfy21");
        xsfyAccountMap.put("XSFY22", "dsa1_xsfy22");
        xsfyAccountMap.put("XSFY23", "dsa1_xsfy23");
        xsfyAccountMap.put("XSFY24", "dsa1_xsfy24");
        xsfyAccountMap.put("XSFY25", "dsa1_xsfy25");
        xsfyAccountMap.put("XSFY26", "dsa1_xsfy26");
        xsfyAccountMap.put("XSFY27", "dsa1_xsfy27");
        xsfyAccountMap.put("XSFY28", "dsa1_xsfy28");
        xsfyAccountMap.put("XSFY28.01", "dsa1_xsfy2801");
        xsfyAccountMap.put("XSFY28.02", "dsa1_xsfy2802");
        xsfyAccountMap.put("XSFY28.03", "dsa1_xsfy2803");
        xsfyAccountMap.put("XSFY28.04", "dsa1_xsfy2804");
        xsfyAccountMap.put("XSFY28.05", "dsa1_xsfy2805");
        xsfyAccountMap.put("XSFY28.06", "dsa1_xsfy2806");
        xsfyAccountMap.put("XSFY28.07", "dsa1_xsfy2807");
        xsfyAccountMap.put("XSFY28.08", "dsa1_xsfy2808");
        xsfyAccountMap.put("XSFY29", "dsa1_xsfy29");
        xsfyAccountMap.put("XSFY30", "dsa1_xsfy30");
        xsfyAccountMap.put("XSFY31", "dsa1_xsfy31");

        REPORT_ACCOUNT_FIELD_MAP.put("XSFY", xsfyAccountMap);


        // 可以继续添加其他报表的科目映射
        // REPORT_ACCOUNT_FIELD_MAP.put("GZQY14", gzqy14AccountMap);

        REPORT_TABLE_MAP.put("GZQY11", "dsa1_budgetreport");  // GZQY11报表对应的表
        REPORT_TABLE_MAP.put("GZQY13", "dsa1_budgetreport_zcfzb");  // GZQY13报表对应的表
        REPORT_TABLE_MAP.put("CWFY", "dsa1_budgetreport_cwfy");  // CWFY报表对应的表
        REPORT_TABLE_MAP.put("GLFY", "dsa1_budgetreport_glfy");  // GLFY报表对应的表
        REPORT_TABLE_MAP.put("XSFY", "dsa1_budgetreport_xsfy");  // XSFY报表对应的表


    }


    /**
     * 从API响应数据创建报表数据
     */
    public static String createReportDataFromApi(JSONObject apiResponse,String reportCode,
                                                 List<String> accounts) {
        try {
            if (!apiResponse.getBoolean("status")) {
                return "API返回状态异常：" + apiResponse.getString("message");
            }

            JSONArray dataArray = apiResponse.getJSONArray("data");
            if (dataArray == null || dataArray.size() < 2) {
                return "无有效数据";
            }

            // 获取当前报表对应的表名
            String tableName = getTableNameByReportCode(reportCode);
            if (tableName == null) {
                return String.format("报表 %s 未配置对应的表名", reportCode);
            }

            // 第一行是表头
            JSONArray headerArray = dataArray.getJSONArray(0);

            Set<String> accountSet = new HashSet<>(accounts);

            // 使用Map按维度分组聚合数据
            Map<String, DynamicObject> recordMap = new LinkedHashMap<>();

            // 从第二行开始处理数据
            for (int i = 1; i < dataArray.size(); i++) {
                JSONArray rowArray = dataArray.getJSONArray(i);

                // 第一条数据是金额
                BigDecimal amount = new BigDecimal(rowArray.getString(0));

                // 获取所有维度值
                DimensionValues dimensions = extractDimensionValues(headerArray, rowArray);

                // 检查科目是否属于当前报表
                if (!accountSet.contains(dimensions.account)) {
                    continue;
                }

                // 处理期间格式
                String period = formatPeriod(dimensions.budgetPeriod);

                // 构建记录的唯一键
                String recordKey = buildRecordKey(dimensions, period);

                // 获取或创建DynamicObject
                DynamicObject record = getOrCreateRecord(recordMap, recordKey, tableName,
                        reportCode, dimensions, period,
                        headerArray, rowArray);

                // 设置金额
                setAmountField(record, reportCode, dimensions.account, amount);
            }

            // 分离新增和更新的数据
            List<DynamicObject> newDataList = new ArrayList<>();
            List<DynamicObject> updateDataList = new ArrayList<>();

            Timestamp now = new Timestamp(System.currentTimeMillis());

            for (DynamicObject record : recordMap.values()) {
                Long id = record.getLong(FIELD_ID);

                if (id == null || id == 0L) {
                    // 新增数据
                    record.set(FIELD_CREATE_TIME, now);
                    record.set(FIELD_MODIFY_TIME, now);
                    newDataList.add(record);
                } else {
                    // 更新数据
                    record.set(FIELD_ID, id);
                    record.set(FIELD_MODIFY_TIME, now);
                    updateDataList.add(record);
                }
            }

            // 批量保存新增数据
            String newResult = saveNewData(newDataList, reportCode, tableName );

            // 批量更新数据
            String updateResult = updateExistingData(updateDataList, reportCode);

            return String.format("报表 %s 处理完成：%s；%s",
                    reportCode, newResult, updateResult);



        } catch (Exception e) {
            e.printStackTrace();
            return "创建报表数据失败：" + e.getMessage();
        }
    }



    /**
     * 设置金额字段
     */
    private static void setAmountField(DynamicObject record,
                                       String reportCode,
                                       String account,
                                       BigDecimal amount) {
        Map<String, String> accountFieldMap = REPORT_ACCOUNT_FIELD_MAP.get(reportCode);
        if (accountFieldMap != null) {
            String dbFieldName = accountFieldMap.get(account);
            if (dbFieldName != null) {
                record.set(dbFieldName, amount);
            }
        }
    }

    /**
     * 构建记录的唯一键
     */
    private static String buildRecordKey(DimensionValues dimensions, String period) {
        return String.format("%s|%s|%s|%s|%s|%s|%s|%s",
                dimensions.entity, period, dimensions.version,
                dimensions.dataType, dimensions.metric,
                dimensions.auditTrail, dimensions.currency, dimensions.changeType);
    }

    /**
     * 提取维度值
     */
    private static DimensionValues extractDimensionValues(JSONArray headers, JSONArray row) {
        return new DimensionValues(
                getDimensionValue(headers, row, "Entity"),
                getDimensionValue(headers, row, "BudgetPeriod"),
                getDimensionValue(headers, row, "Version"),
                getDimensionValue(headers, row, "ChangeType"),
                getDimensionValue(headers, row, "DataType"),
                getDimensionValue(headers, row, "Metric"),
                getDimensionValue(headers, row, "AuditTrail"),
                getDimensionValue(headers, row, "Currency"),
                getDimensionValue(headers, row, "Account")
        );
    }



    /**
     * 获取或创建记录
     */

    private static DynamicObject getOrCreateRecord(Map<String, DynamicObject> recordMap,
                                                   String recordKey,
                                                   String tableName,
                                                   String reportCode,
                                                   DimensionValues dimensions,
                                                   String period,
                                                   JSONArray headers,
                                                   JSONArray row) {
        DynamicObject record = recordMap.get(recordKey);

        if (record == null) {
            // 重要：始终创建新对象，不要直接使用查询出来的对象
            record = BusinessDataServiceHelper.newDynamicObject(tableName);

            // 查询是否已存在记录（用于后续判断是新增还是更新）
            DynamicObject existingRecord = queryExistingRecord(tableName, reportCode, dimensions, period);

            if (existingRecord != null) {
                // 如果存在，将ID设置到新对象中，表示这是更新操作
                Long id = existingRecord.getLong(FIELD_ID);
                if (id != null && id > 0) {
                    record.set(FIELD_ID, id);
                    log.debug("找到已存在的记录 ID: {}，将进行更新操作", id);
                }
            }

            // 设置公共字段（包括所有维度字段）
            setCommonFields(record, reportCode, dimensions, period, headers, row);

            recordMap.put(recordKey, record);
        }

        return record;
    }


    /**
     * 设置公共字段
     */
    private static void setCommonFields(DynamicObject record,
                                        String reportCode,
                                        DimensionValues dimensions,
                                        String period,
                                        JSONArray headers,
                                        JSONArray row) {
        // 设置报表代码
        record.set("dsa1_reportnumber", reportCode);

        // 设置基本维度字段
        DynamicObject dynamicObject  =  getOrgMapping( dimensions.entity);
        if (dynamicObject==null){
            record.set("dsa1_organization_code", dimensions.entity);
            record.set("dsa1_companyname", queryOrgName(dimensions.entity));
        }else {
            record.set("dsa1_organization_code", dynamicObject.getString("number"));
            record.set("dsa1_companyname", dynamicObject.getString("name"));
        }
        record.set("dsa1_period", period);
        record.set("dsa1_version_code", dimensions.version);
        record.set("dsa1_change_type", dimensions.changeType);
        record.set("dsa1_data_type", dimensions.dataType);
        record.set("dsa1_metric", dimensions.metric);
        record.set("dsa1_audit_trail", dimensions.auditTrail);
        record.set("dsa1_currency", dimensions.currency);
        if ("GZQY13".equals(reportCode)){
            record.set("dsa1_total", "TOTAL");

        }


        // 设置其他维度字段
        setOtherDimensionFields(record, headers, row);

        // 解析期间
        parsePeriod(record, dimensions.budgetPeriod);
    }
    private static DynamicObject getOrgMapping(String number) {
        try {
            DynamicObject orgMapping = BusinessDataServiceHelper.loadSingle("dsa1_reportorgmapping", "number,name", new QFilter[]{new QFilter("dsa1_hborgnumber", QCP.equals, number)});
            return  orgMapping  ;
        }catch (Exception e){
            log.error("getOrgMapping: {}", e.getMessage(), e);
            return null;
        }
    }
    /**
     * 根据报表代码获取表名
     */
    private static String getTableNameByReportCode(String reportCode) {
        return REPORT_TABLE_MAP.get(reportCode);
    }
    /**
     * 保存新增数据
     */
    private static String saveNewData(List<DynamicObject> newDataList, String reportCode,String table_name) {
        if (newDataList == null || newDataList.isEmpty()) {
            return String.format("报表 %s 无新增数据", reportCode);
        }

        try {
            DynamicObject[] saveArray = newDataList.toArray(new DynamicObject[0]);
            OperationResult saveResult = SaveServiceHelper.saveOperate(
                    table_name,
                    saveArray,
                    OperateOption.create()
            );

            // 从OperationResult中获取成功记录数
            int successCount = saveResult.getSuccessPkIds().size();

            // 获取所有错误信息（包括验证错误）
            List<IOperateInfo> allErrors = saveResult.getAllErrorOrValidateInfo();
            int failCount = allErrors.size();

            if (failCount > 0) {
                // 记录错误信息
                for (IOperateInfo error : allErrors) {
                    log.error("保存失败: {}", error.getMessage());
                }
            }

            return String.format("新增：成功 %d 条，失败 %d 条", successCount, failCount);

        } catch (Exception e) {
            e.printStackTrace();
            return String.format("新增失败：%s", e.getMessage());
        }
    }

    private static String updateExistingData(List<DynamicObject> updateDataList, String reportCode) {
        try {
            DynamicObject[] updateArray = updateDataList.toArray(new DynamicObject[0]);
            SaveServiceHelper.update(updateArray);
            return String.format("更新：%d 条", updateDataList.size());
        } catch (Exception e) {
            return String.format("更新失败：%s", e.getMessage());
        }
    }

    /**
     * 查询组织名称
     */
    private static String queryOrgName(String orgNumber) {
        try {
            QFilter[] filters = {
                    new QFilter("dimension.number", QCP.equals, "Entity"),
                    new QFilter("model.shownumber", QCP.equals, "XCZXYSTX"),
                    new QFilter("number", QCP.equals, orgNumber),
                    new QFilter("view.number", QCP.equals, "E001")
            };

            DynamicObject org = BusinessDataServiceHelper.loadSingle("eb_viewmember",
                    String.join(",", "name", "number", "memberid"),
                    filters);

            return org != null ? org.getString("name") : "";
        } catch (Exception e) {
            return "";
        }
    }
    /**
     * 查询已存在的记录
     */

    private static DynamicObject queryExistingRecord(String tableName,
                                                     String reportCode,
                                                     DimensionValues dimensions,
                                                     String period) {

        try {
            QFilter[] filters = {
                    new QFilter("dsa1_reportnumber", QCP.equals, reportCode),
                    new QFilter("dsa1_organization_code", QCP.equals, dimensions.entity),
                    new QFilter("dsa1_period", QCP.equals, period),
                    new QFilter("dsa1_version_code", QCP.equals, dimensions.version),
                    new QFilter("dsa1_change_type", QCP.equals, dimensions.changeType),
                    new QFilter("dsa1_data_type", QCP.equals, dimensions.dataType),
                    new QFilter("dsa1_metric", QCP.equals, dimensions.metric),
                    new QFilter("dsa1_audit_trail", QCP.equals, dimensions.auditTrail),
                    new QFilter("dsa1_currency", QCP.equals, dimensions.currency)
            };

            DynamicObject results = BusinessDataServiceHelper.loadSingle(tableName, "*", filters);

            return results;
        }catch (Exception e){
            log.debug("查找已存在数据失败: {}", e.getMessage());
            return null;
        }

    }
    private static DynamicObject queryOrg(String orgNumber) {
        QFilter[] filters = {
                new QFilter("dimension.number", QCP.equals, "Entity"),
                new QFilter("model.shownumber", QCP.equals, "XCZXYSTX"),
                new QFilter("number", QCP.equals, orgNumber),
                new QFilter("view.number", QCP.equals, "E001")
        };

        return BusinessDataServiceHelper.loadSingle("eb_viewmember",
                String.join(",", "name", "number", "memberid"),
                filters);
    }
    /**
     * 构建记录的唯一键
     */
    private static String buildRecordKey(String entity, String period, String version,
                                         String dataType, String metric,
                                         String auditTrail, String currency) {
        return String.format("%s|%s|%s|%s|%s|%s|%s",
                entity, period, version, dataType, metric, auditTrail, currency);
    }


    /**
     * 格式化期间
     * FY2026.M01 -> 202601
     * FY2026 -> 2026
     */
    private static String formatPeriod(String budgetPeriod) {
        if (budgetPeriod == null) return "";

        // 移除FY前缀
        String period = budgetPeriod.replace("FY", "");

        // 替换.M为无点格式
        if (period.contains(".")) {
            String[] parts = period.split("\\.");
            String month = parts[1].replace("M", "");
            return parts[0] + month;
        }

        return period;
    }

    /**
     * 设置其他维度字段（CPJD、CPJD1、CostItem等）
     */
    private static void setOtherDimensionFields(DynamicObject record, JSONArray headers, JSONArray row) {
        // 需要排除已经设置的字段
        Set<String> excludeFields = new HashSet<>(Arrays.asList(
                "Entity", "Account", "BudgetPeriod", "Version", "Currency",
                "AuditTrail", "ChangeType", "DataType", "Metric"
        ));

        for (int i = 1; i < headers.size(); i++) {
            String dimensionName = headers.getString(i);

            if (!excludeFields.contains(dimensionName)) {
                String dbFieldName = DIMENSION_FIELD_MAP.get(dimensionName);
                if (dbFieldName != null) {
                    String value = row.getString(i+1);
                    // 存储所有值，包括PJNone、ITNone等
                    record.set(dbFieldName, value);
                }
            }
        }
    }

    /**
     * 解析期间字段
     */
    private static void parsePeriod(DynamicObject record, String budgetPeriod) {
        if (budgetPeriod == null) return;

        String cleanPeriod = budgetPeriod.replace("FY", "");

        if (cleanPeriod.contains(".")) {
            String[] parts = cleanPeriod.split("\\.");
            record.set("dsa1_year", parts[0]);
            record.set("dsa1_month", parts[1].replace("M", ""));

        } else {
            record.set("dsa1_year", cleanPeriod);
            record.set("dsa1_month", "TOTAL");

        }
    }

    /**
     * 获取指定维度的值
     */
    private static String getDimensionValue(JSONArray headers, JSONArray row, String dimensionName) {
        for (int i = 0; i < headers.size(); i++) {
            if (dimensionName.equals(headers.getString(i))) {
                return row.getString(i+1);
            }
        }
        return null;
    }


}

