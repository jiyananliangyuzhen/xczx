package dsa1.xczxx.wfs.ws.mservice.report;


import com.alibaba.fastjson.JSON;
import dsa1.xczxx.wfs.ws.common.util.GetTokneService;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.KDBizException;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.QueryServiceHelper;
import net.sf.json.JSONObject;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static dsa1.xczxx.wfs.ws.common.util.DataSendHttp.doPost_token;
import static dsa1.xczxx.wfs.ws.mservice.report.ProfitStatementReportProcessor.createReportDataFromApi;

public class MergeReportService extends AbstractTask {
    private final static Log logger = LogFactory.getLog(MergeReportService.class);
    private static final String ACCOUNT_ID_KEY = "accountId";
    private static final String TOKEN_URL_KEY = "tokenUrl";
    private static final String REPORT_QUERY_URL = "reportQueryUrl";

    private static final String REPORTNUMBER = "reportNumber";

    private static final String DIMENSION_NUMBER = "Account";
    private static final String MODEL_SHOWNUMBER = "XCZXYSTX";
    private static final String LONGNUMBER_FIELD = "longnumber";
    private static final String SHOWNUMBER = "shownumber";
    private static final String PARAM_ORGS = "ys_orgs";
    private static final String PARAM_YEAR = "ys_year";
    private static final String GZQY13_VERSION = "gzqy13_version";
    private static final String GZQY11_VERSION = "gzqy11_version";

    private static final Map<String, String> REPORT_ACCOUNT_PREFIX_MAP = new HashMap<>();

    // 报表代码与期间配置的映射
    private static final Map<String, List<String>> REPORT_PERIOD_MAP = new HashMap<>();
    // 报表代码与版本配置的映射
    private static final Map<String, List<String>> REPORT_VERSION_MAP = new HashMap<>();
    static {
        // 初始化报表配置
        REPORT_ACCOUNT_PREFIX_MAP.put("GZQY11", "Account!GZQY11");
        REPORT_ACCOUNT_PREFIX_MAP.put("GZQY13", "Account!GZQY13");
        REPORT_ACCOUNT_PREFIX_MAP.put("CWFY", "Account!CWFY");
        REPORT_ACCOUNT_PREFIX_MAP.put("XSFY", "Account!XSFY");
        REPORT_ACCOUNT_PREFIX_MAP.put("GLFY", "Account!GLFY");



//        // GZQY11报表的期间
//        REPORT_PERIOD_MAP.put("GZQY11", Arrays.asList(
//                "FY2025", "FY2026",
//                "FY2026.M01", "FY2026.M02", "FY2026.M03", "FY2026.M04",
//                "FY2026.M05", "FY2026.M06", "FY2026.M07", "FY2026.M08",
//                "FY2026.M09", "FY2026.M10", "FY2026.M11", "FY2026.M12",
//                "FY2025.M01", "FY2025.M02", "FY2025.M03", "FY2025.M04",
//                "FY2025.M05", "FY2025.M06", "FY2025.M07", "FY2025.M08",
//                "FY2025.M09", "FY2025.M10", "FY2025.M11", "FY2025.M12"));
//
//        // GZQY13报表的期间（示例：只需要年度数据）
//        REPORT_PERIOD_MAP.put("GZQY13", Arrays.asList(
//                "FY2025.M12", "FY2026.M12"
//        ));


//        // GZQY11报表的版本
//        REPORT_VERSION_MAP.put("GZQY11", Arrays.asList(
//                "Annual_budget_V1", "Annual_budget_addjust", "Annual_budget_V2","Annual_budget_V3"
//        ));
//
//        // GZQY13报表的版本
//        REPORT_VERSION_MAP.put("GZQY13", Arrays.asList(
//                "Annual_budget_V1", "Annual_budget_V2"
//        ));
    }
    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
       try {
           logger.info("BudgetReportService begin");
           logger.info("requestContext = {}",requestContext);
           logger.info("map = {}",map);
           Map<String, String> systemConfigs = getSystemConfigs();
           String accountId = systemConfigs.get(ACCOUNT_ID_KEY);
           String tokenUrl = systemConfigs.get(TOKEN_URL_KEY);
           String reportQueryUrl = systemConfigs.get(REPORT_QUERY_URL);
           String reportNumber = systemConfigs.get(REPORTNUMBER);
           String orgs = systemConfigs.get(PARAM_ORGS);
           String year ;
           String key = (String) map.get("key");
           if ("automatic".equals(key)){
               LocalDate now = LocalDate.now();
               year = String.format("%d", now.getYear()); // 结果为 "2026"
           }else{
                year = systemConfigs.get(PARAM_YEAR);
           }
           String gzqy13_version = systemConfigs.get(GZQY13_VERSION);
           String gzqy11_version = systemConfigs.get(GZQY11_VERSION);

           REPORT_PERIOD_MAP.put("GZQY13", generateYearEndPeriods(year));
           REPORT_PERIOD_MAP.put("GZQY11", generatePeriods(year));
           REPORT_PERIOD_MAP.put("CWFY", generatePeriods(year));
           REPORT_PERIOD_MAP.put("XSFY", generatePeriods(year));
           REPORT_PERIOD_MAP.put("GLFY", generatePeriods(year));

           REPORT_VERSION_MAP.put("GZQY13",Arrays.asList(gzqy13_version.split(",")));
           REPORT_VERSION_MAP.put("GZQY11",Arrays.asList(gzqy11_version.split(",")));
           REPORT_VERSION_MAP.put("CWFY",Arrays.asList(gzqy13_version.split(",")));
           REPORT_VERSION_MAP.put("XSFY",Arrays.asList(gzqy13_version.split(",")));
           REPORT_VERSION_MAP.put("GLFY",Arrays.asList(gzqy13_version.split(",")));

           List<String> reportNumbers = new ArrayList<>(Arrays.asList(reportNumber.split(",")));
           Map<String, List<String>> reportAccountsMap = queryAccountsForReports(reportNumbers);

           for (Map.Entry<String, List<String>> entry : reportAccountsMap.entrySet()) {
               String reportCode = entry.getKey();
               List<String> accounts = entry.getValue();

               logger.info("开始处理报表 {}，科目数量: {}", reportCode, accounts.size());

               if (accounts.isEmpty()) {
                   logger.warn("报表 {} 没有查询到科目，跳过处理", reportCode);
                   continue;
               }

               List<String> periods = REPORT_PERIOD_MAP.get(reportCode);

               List<String> versions = REPORT_VERSION_MAP.get(reportCode);

                   Map<String, Object> requestData = buildRequestData(accounts,periods,versions,orgs);
                   String token = GetTokneService.getToken(accountId, tokenUrl);
                   Map<String, String> headers = buildHeaders(token);

                   String response = doPost_token(JSON.toJSONString(requestData), reportQueryUrl, headers);

               logger.info("response = {}",response);

               // 处理响应数据，传入当前报表代码和科目列表
                   JSONObject responseJson = JSONObject.fromObject(response);
                   String result = createReportDataFromApi(responseJson, reportCode, accounts);
               logger.info("报表 {} 处理结果: {}", reportCode, result);
           }

           logger.info("所有报表数据处理完成");

       }catch (Exception e){
           logger.error(e.getMessage());
       }



    }


    /**
     * 生成年度末期间列表（GZQY13报表使用）
     * @param year 基准年份
     * @return 期间列表
     */
    public static List<String> generateYearEndPeriods(String year) {
        List<String> periods = new ArrayList<>();
        int yearNum = Integer.parseInt(year);
        String currentYear = year;
        String nextYear = String.valueOf(yearNum + 1);

        // 只添加12月份的数据
        periods.add(String.format("FY%s.M12", currentYear));
        periods.add(String.format("FY%s.M12", nextYear));

        return periods;
    }
    // 生成期间列表的工具方法
    private List<String> generatePeriods(String year) {
        List<String> periods = new ArrayList<>();
        // 添加年度期间
        periods.add("FY" + year);
        // 添加当前年的月份期间
        for (int month = 1; month <= 12; month++) {
            periods.add(String.format("FY%s.M%02d", year, month));
        }

        return periods;
    }
    private Map<String, String> buildHeaders(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("accessToken", token);
        headers.put("ContentType", "application/json");
        headers.put("client_id", String.valueOf(UUID.randomUUID()));
        return headers;
    }

    /**
     * 构建请求数据
     */
    private Map<String, Object> buildRequestData(List<String> accounts,List<String>periods,List<String>versions,String orgs) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> params = new HashMap<>();

        params.put("modelNumber", "XCZXYSTX");
        params.put("datasetNumber", "default");

        Map<String, Object> dimMemberMap = new HashMap<>();

        // 组织
        dimMemberMap.put("Entity",Arrays.asList(orgs.split(",")));
        // 科目（使用当前报表的科目列表）
        dimMemberMap.put("Account", accounts);
        // 预算期间
        dimMemberMap.put("BudgetPeriod",periods );
        // 版本
        dimMemberMap.put("Version", versions);
        // 币种
        dimMemberMap.put("Currency", Arrays.asList("CNY"));
        // 线索
        dimMemberMap.put("AuditTrail", Arrays.asList("EntityInput"));
        // 变动类型
        dimMemberMap.put("ChangeType", Arrays.asList("CurrentPeriod"));
        // 数据类型
        dimMemberMap.put("DataType", Arrays.asList("Budget"));
        // 度量
        dimMemberMap.put("Metric", Arrays.asList("Money"));
        // 自定义维度
        dimMemberMap.put("CPJD", Arrays.asList("PJNone"));
        dimMemberMap.put("CPJD1", Arrays.asList("PJ1None"));
        dimMemberMap.put("CostItem", Arrays.asList("ITNone"));
        dimMemberMap.put("InvoiceType", Arrays.asList("TYNone"));
        dimMemberMap.put("InvoiceTaxRate", Arrays.asList("TRNone"));
        dimMemberMap.put("InputTaxType", Arrays.asList("TINone"));

        params.put("data", dimMemberMap);
        data.put("params", params);
        return data;
    }

    /**
     * 为多个报表查询对应的科目
     */
    private Map<String, List<String>> queryAccountsForReports(List<String> reportNumbers) {
        Map<String, List<String>> result = new HashMap<>();

        for (String reportNumber : reportNumbers) {
            List<String> accounts = queryAccountsForSingleReport(reportNumber);
            if ("GZQY11".equals(reportNumber)){
                Collections.addAll(accounts, "SJYS", "XSFY", "GLFY", "YYWSR", "YYWZC", "SJYS14");
            }
            result.put(reportNumber, accounts);
            logger.info("报表 {} 查询到 {} 个科目", reportNumber, accounts.size());
        }

        return result;
    }


    /**
     * 为单个报表查询科目
     */
    private List<String> queryAccountsForSingleReport(String reportNumber) {
        String accountPrefix = REPORT_ACCOUNT_PREFIX_MAP.get(reportNumber);
//        String viewNumber = REPORT_VIEW_MAP.get(reportNumber);

        if (accountPrefix == null) {
            logger.warn("未找到报表 {} 对应的科目前缀配置", reportNumber);
            return new ArrayList<>();
        }

        QFilter[] filters = {
                new QFilter("dimension.number", QCP.equals, DIMENSION_NUMBER),
                new QFilter("model.shownumber", QCP.equals, MODEL_SHOWNUMBER),
                new QFilter(LONGNUMBER_FIELD, QCP.like, accountPrefix + "%"),
                new QFilter(LONGNUMBER_FIELD, QCP.not_equals,accountPrefix)
        };

        DynamicObjectCollection projects = QueryServiceHelper.query("epm_accountmembertree",
                String.join(",", LONGNUMBER_FIELD, SHOWNUMBER),
                filters);

        return projects.stream()
                .map(d -> d.getString(SHOWNUMBER))
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }




    /**
     * 获取系统配置
     */
    private Map<String, String> getSystemConfigs() {
        Map<String, String> configs = new HashMap<>();

        String accountId = System.getProperty(ACCOUNT_ID_KEY);
        if (accountId == null) {
            throw new KDBizException("请管理员到MC配置 accountId");
        }
        configs.put(ACCOUNT_ID_KEY, accountId);

        String tokenUrl = System.getProperty(TOKEN_URL_KEY);
        if (tokenUrl == null) {
            throw new KDBizException("请管理员到MC配置 tokenUrl");
        }
        configs.put(TOKEN_URL_KEY, tokenUrl);

        String reportQueryUrl = System.getProperty(REPORT_QUERY_URL);
        if (reportQueryUrl == null) {
            throw new KDBizException("请管理员到MC配置 reportQueryUrl");
        }
        configs.put(REPORT_QUERY_URL, reportQueryUrl);

        String reportnumber = System.getProperty(REPORTNUMBER);
        if (reportnumber == null) {
            throw new KDBizException("请管理员到MC配置 reportnumber");
        }
        configs.put(REPORTNUMBER, reportnumber);



        String orgs = System.getProperty(PARAM_ORGS);
        if (orgs == null) {
            throw new KDBizException("请管理员到MC配置 ys_orgs");
        }
        configs.put(PARAM_ORGS, orgs);


        String year = System.getProperty(PARAM_YEAR);
        if (year == null) {
            throw new KDBizException("请管理员到MC配置 ys_year");
        }
        configs.put(PARAM_YEAR, year);

        String gzqy13_version = System.getProperty(GZQY13_VERSION);
        if (gzqy13_version == null) {
            throw new KDBizException("请管理员到MC配置 gzqy13_version");
        }
        configs.put(GZQY13_VERSION, gzqy13_version);


        String gzqy11_version = System.getProperty(GZQY11_VERSION);
        if (gzqy11_version == null) {
            throw new KDBizException("请管理员到MC配置 gzqy11_version");
        }
        configs.put(GZQY11_VERSION, gzqy11_version);


        return configs;
    }
}
