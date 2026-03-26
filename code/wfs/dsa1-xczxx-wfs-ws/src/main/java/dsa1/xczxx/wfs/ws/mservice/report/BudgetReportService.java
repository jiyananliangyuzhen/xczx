package dsa1.xczxx.wfs.ws.mservice.report;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.grapecity.documents.excel.E;
import dsa1.xczxx.wfs.ws.common.util.GetTokneService;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.KDBizException;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;

import java.util.*;
import java.util.stream.Collectors;

import static dsa1.xczxx.wfs.ws.common.util.DataSendHttp.doPost_token;
import static dsa1.xczxx.wfs.ws.mservice.report.FinancialDataSaver.saveFinancialData;

public class BudgetReportService extends AbstractTask {

    private final static Log log = LogFactory.getLog(MergeReportService.class);

    private static final String ACCOUNT_ID_KEY = "accountId";
    private static final String TOKEN_URL_KEY = "tokenUrl";
    private static final String REPORT_QUERY_URL = "reportQueryUrl";
    private static final String MERGE_REPORT_QUERY_URL = "mergeReportQueryUrl";
    private static final String DIMENSION_NUMBER = "Entity";
    private static final String MODEL_SHOWNUMBER = "XCZX001";
    private static final String SCENE = "MRpt";

    // 报表查询参数常量
    private static final String PARAM_MODEL_NUM = "modelNum";
    private static final String PARAM_SCENE = "scene";
    private static final String PARAM_YEAR = "hb_year";
    private static final String PARAM_PERIOD = "hb_period";
    private static final String PARAM_CURRENCY = "currency";
    private static final String PARAM_ORGS = "hb_orgs";
    private static final String PARAM_TMPS = "hb_tmps";
    private static final String PARAM_DIM2_MEMS = "dim2Mems";

    // 常量定义
    private static final String MERGE_REPORT_QUERY_URL_KEY = "mergeReportQueryUrl";

    private static final String MODEL_NUMBER = "XCZX001";
    private static final String CSL_SCHEME_NUMBER = "ManageMergedViews";
    private static final String DIM_PROCESS = "Process";
    private static final String DIM_AUDIT_TRAIL = "AuditTrail";
    private static final String DIM_PROCESS_VALUE = "EIRpt";
    private static final String DIM_AUDIT_VALUE = "EntityInput";

    // 请求头常量
    private static final String HEADER_ACCESS_TOKEN = "accessToken";
    private static final String HEADER_CONTENT_TYPE = "ContentType";
    private static final String HEADER_CLIENT_ID = "client_id";
    private static final String CONTENT_TYPE_JSON = "application/json";

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        try {
            //第一步获取token
            Map<String, Object> params = new HashMap<>();
            Map<String, String> systemConfigs = getSystemConfigs();
            String accountId = systemConfigs.get(ACCOUNT_ID_KEY);
            String tokenUrl = systemConfigs.get(TOKEN_URL_KEY);
            String mergeReportQueryUrl = systemConfigs.get(MERGE_REPORT_QUERY_URL_KEY);
            String orgs = systemConfigs.get(PARAM_ORGS);
            String tmps = systemConfigs.get(PARAM_TMPS);
            String year = systemConfigs.get(PARAM_YEAR);
            String period = systemConfigs.get(PARAM_PERIOD);
            String[] periods = period.split(",");

            for (String singlePeriod : periods) {
                Map<String, Object> requestData = buildRequestData(params, orgs, tmps, year, singlePeriod);

                params = (Map<String, Object>) requestData.get("param");
                // 获取token
                String token = GetTokneService.getToken(accountId, tokenUrl);

                // 发送请求
                String response = sendReportRequest(requestData, token, mergeReportQueryUrl);
                saveFinancialData(response, params);
                log.info("合并报表同步任务执行完成");
            }
        } catch (Exception e) {
            log.error("合并报表同步任务执行失败: {}", e.getMessage(), e);
            throw new KDBizException("合并报表同步失败: " + e.getMessage());
        }

    }

    public static List<String> stringToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(str.split(","))
                .map(String::trim)           // 去除每个元素的前后空格
                .filter(s -> !s.isEmpty())   // 过滤掉空字符串
                .collect(Collectors.toList());
    }

    /**
     * 批量查询实体信息
     */
    public static Map<String, DynamicObject> queryEntities(List<String> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return Collections.emptyMap();
        }

        QFilter[] filters = {
                new QFilter("dimension.number", QCP.equals, DIMENSION_NUMBER),
                new QFilter("model.shownumber", QCP.equals, MODEL_NUMBER),
                new QFilter("cslscheme.number", QCP.equals, CSL_SCHEME_NUMBER),
                new QFilter("number", QCP.in, numbers)
        };

        DynamicObjectCollection collection = QueryServiceHelper.query("bcm_entitymembertree",
                String.join(",", "id", "number", "name"),
                filters);

        Map<String, DynamicObject> result = new HashMap<>();
        for (DynamicObject obj : collection) {
            result.put(obj.getString("number"), obj);
        }

        return result;
    }

    private DynamicObject getOrgMapping(String number) {
        try {
            DynamicObject orgMapping = BusinessDataServiceHelper.loadSingle("dsa1_reportorgmapping", "number,name", new QFilter[]{new QFilter("dsa1_hborgnumber", QCP.equals, number)});
            return  orgMapping  ;
        }catch (Exception e){
            log.error("getOrgMapping: {}", e.getMessage(), e);
            return null;
        }
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


        String mergeReportQueryUrl = System.getProperty(MERGE_REPORT_QUERY_URL);
        if (mergeReportQueryUrl == null) {
            throw new KDBizException("请管理员到MC配置 reportQueryUrl");
        }
        configs.put(MERGE_REPORT_QUERY_URL, mergeReportQueryUrl);


        String orgs = System.getProperty(PARAM_ORGS);
        if (orgs == null) {
            throw new KDBizException("请管理员到MC配置 hb_orgs");
        }
        configs.put(PARAM_ORGS, orgs);


        String tmps = System.getProperty(PARAM_TMPS);
        if (tmps == null) {
            throw new KDBizException("请管理员到MC配置 hb_tmps");
        }
        configs.put(PARAM_TMPS, tmps);


        String year = System.getProperty(PARAM_YEAR);
        if (year == null) {
            throw new KDBizException("请管理员到MC配置 hb_year");
        }
        configs.put(PARAM_YEAR, year);


        String period = System.getProperty(PARAM_PERIOD);
        if (period == null) {
            throw new KDBizException("请管理员到MC配置 hb_period");
        }
        configs.put(PARAM_PERIOD, period);



        return configs;
    }

    private Map<String, Object> buildRequestData(Map<String, Object> taskParams, String orgs, String tmps,String year,String period) {
        Map<String, Object> requestData = new HashMap<>();
        Map<String, Object> queryParams = extractQueryParams(taskParams);
        queryParams.put("orgs", stringToList(orgs));
        queryParams.put("tmps", stringToList(tmps));
        queryParams.put("year", year);
        queryParams.put("period",period );
        requestData.put("param", queryParams);
        return requestData;
    }

    /**
     * 提取查询参数
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractQueryParams(Map<String, Object> taskParams) {
        Map<String, Object> params = new HashMap<>();

        // 基础参数
        params.put(PARAM_MODEL_NUM, getStringParam(taskParams, PARAM_MODEL_NUM, MODEL_NUMBER));
        params.put(PARAM_SCENE, getStringParam(taskParams, PARAM_SCENE, SCENE));
        params.put(PARAM_CURRENCY, getStringParam(taskParams, PARAM_CURRENCY, "EC"));

        // 维度参数
        Map<String, Object> dimMemberMap = new HashMap<>();
        dimMemberMap.put(DIM_PROCESS, DIM_PROCESS_VALUE);
        dimMemberMap.put(DIM_AUDIT_TRAIL, DIM_AUDIT_VALUE);

        // 如果任务参数中包含自定义维度映射，则合并
        if (taskParams != null && taskParams.get(PARAM_DIM2_MEMS) instanceof Map) {
            dimMemberMap.putAll((Map<String, Object>) taskParams.get(PARAM_DIM2_MEMS));
        }
        params.put(PARAM_DIM2_MEMS, dimMemberMap);

        return params;
    }


    /**
     * 解析参数为字符串列表
     * 支持：单个字符串、List<String>、JSON数组字符串、逗号分隔的字符串
     */
    @SuppressWarnings("unchecked")
    private List<String> parseToStringList(Map<String, Object> params, String key, List<String> defaultValue) {
        if (params == null || !params.containsKey(key) || params.get(key) == null) {
            return defaultValue;
        }

        Object value = params.get(key);

        // 如果是List类型
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }

        // 如果是字符串
        if (value instanceof String) {
            String strValue = (String) value;

            // 尝试解析为JSON数组
            if (strValue.trim().startsWith("[")) {
                try {
                    JSONArray jsonArray = JSON.parseArray(strValue);
                    List<String> result = new ArrayList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        Object item = jsonArray.get(i);
                        if (item != null) {
                            result.add(item.toString());
                        }
                    }
                    return result;
                } catch (Exception e) {
                    log.debug("解析JSON数组失败，尝试按逗号分割: {}", e.getMessage());
                }
            }

            // 按逗号分割
            if (strValue.contains(",")) {
                return Arrays.stream(strValue.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }

            // 单个值
            return Collections.singletonList(strValue.trim());
        }

        // 其他类型，转换为字符串
        return Collections.singletonList(value.toString());
    }

    /**
     * 安全获取字符串参数
     */
    private String getStringParam(Map<String, Object> params, String key, String defaultValue) {
        if (params != null && params.containsKey(key) && params.get(key) != null) {
            return params.get(key).toString();
        }
        return defaultValue;
    }

    /**
     * 发送报表查询请求
     */
    private String sendReportRequest(Map<String, Object> requestData, String token, String url) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_ACCESS_TOKEN, token);
        headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
        headers.put(HEADER_CLIENT_ID, UUID.randomUUID().toString());
        return doPost_token(JSON.toJSONString(requestData), url, headers);
    }


}
