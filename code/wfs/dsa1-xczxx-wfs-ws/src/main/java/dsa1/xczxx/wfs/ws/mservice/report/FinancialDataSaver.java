package dsa1.xczxx.wfs.ws.mservice.report;


import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


public class FinancialDataSaver {
    private final static Log log = LogFactory.getLog(FinancialDataSaver.class);


    // 定义文本到字段名的映射关系
    private static final Map<String, String[]> CPL01_MAP = new HashMap<>();
    private static final Map<String, String[]> BS01_MAP = new HashMap<>();

    private static final Map<String, String[]> CF01_MAP = new HashMap<>();


    private static final Map<String, String[]> CQZ01_MAP = new HashMap<>();

    // 字段常量
    private static final String FIELD_ID = "id";
    private static final String FIELD_DURING = "dsa1_during";
    private static final String FIELD_YEARS = "dsa1_years";
    private static final String FIELD_MONTHS = "dsa1_months";
    private static final String FIELD_COMPANY_NAME = "dsa1_companyname";
    private static final String FIELD_ORGS = "dsa1_orgs";
    private static final String FIELD_ORGS_NUMBER = "dsa1_orgnumber";
    private static final String FIELD_CURRENCY = "dsa1_currency";
    private static final String FIELD_SCENE = "dsa1_scene";
    private static final String FIELD_MODEL_NUM = "dsa1_modelNum";
    private static final String FIELD_REPORT_NUMBER = "dsa1_reportnumber";
    private static final String FIELD_CREATE_TIME = "dsa1_createtime";
    private static final String FIELD_MODIFY_TIME = "dsa1_modifytime";
    private static final Map<String, String[]> GLFY01_MAP = new HashMap<>();

    private static final Map<String, String[]> ZYYWSYB_MAP = new HashMap<>();
    private static final Map<String, String[]> CWFY01_MAP = new HashMap<>();
    private static final Map<String, String> TABLE_NAME_MAP = new HashMap<>();
    private static final Map<String, Map<String, String[]>> REPORT_TYPE_FIELD_MAPS = new HashMap<>();

    // 文本到字段名的映射关系
    static {
        //报表映射
        TABLE_NAME_MAP.put("CPL01", "dsa1_mergereport");      // 利润表
        TABLE_NAME_MAP.put("CF01", "dsa1_mergereport_xjllb");     // 现金流量表
        TABLE_NAME_MAP.put("BS01", "dsa1_mergereport_zcfzb");     // 资产负债表
        TABLE_NAME_MAP.put("CQZ01", "dsa1_mergereport_gzwkb");     // 国资委快报
        TABLE_NAME_MAP.put("ZYYWSYB", "dsa1_mergereport_zyyw"); //主要业务损益表
        TABLE_NAME_MAP.put("CWFY01", "dsa1_mergereport_cwfy"); //财务费用明细表
        TABLE_NAME_MAP.put("GLFY01", "dsa1_mergereport_glfy");  // 管理费用明细表

        // 初始化所有80个文本对应的字段映射
        // 格式：{文本关键字, [本期金额字段, 本年累计字段, 上年同期字段]}
        CPL01_MAP.put("R2006", new String[]{"dsa1_r2006_bqs", "dsa1_r2006_bnlj", "dsa1_r2006_sntq"});
        CPL01_MAP.put("R2007", new String[]{"dsa1_r2007_bqs", "dsa1_r2007_bnlj", "dsa1_r2007_sntq"});
        CPL01_MAP.put("R2009", new String[]{"dsa1_r2009_bqs", "dsa1_r2009_bnlj", "dsa1_r2009_sntq"});
        CPL01_MAP.put("R2010", new String[]{"dsa1_r2010_bqs", "dsa1_r2010_bnlj", "dsa1_r2010_sntq"});
        CPL01_MAP.put("R2011", new String[]{"dsa1_r2011_bqs", "dsa1_r2011_bnlj", "dsa1_r2011_sntq"});
        CPL01_MAP.put("R2005", new String[]{"dsa1_r2005_bqs", "dsa1_r2005_bnlj", "dsa1_r2005_sntq"});
        CPL01_MAP.put("R2004", new String[]{"dsa1_r2004_bqs", "dsa1_r2004_bnlj", "dsa1_r2004_sntq"});
        CPL01_MAP.put("R2003", new String[]{"dsa1_r2003_bqs", "dsa1_r2003_bnlj", "dsa1_r2003_sntq"});
        CPL01_MAP.put("R2002", new String[]{"dsa1_r2002_bqs", "dsa1_r2002_bnlj", "dsa1_r2002_sntq"});
        CPL01_MAP.put("R2020", new String[]{"dsa1_r2020_bqs", "dsa1_r2020_bnlj", "dsa1_r2020_sntq"});
        CPL01_MAP.put("R2016", new String[]{"dsa1_r2016_bqs", "dsa1_r2016_bnlj", "dsa1_r2016_sntq"});
        CPL01_MAP.put("R2017", new String[]{"dsa1_r2017_bqs", "dsa1_r2017_bnlj", "dsa1_r2017_sntq"});
        CPL01_MAP.put("R2018", new String[]{"dsa1_r2018_bqs", "dsa1_r2018_bnlj", "dsa1_r2018_sntq"});
        CPL01_MAP.put("R2019", new String[]{"dsa1_r2019_bqs", "dsa1_r2019_bnlj", "dsa1_r2019_sntq"});
        CPL01_MAP.put("R2065", new String[]{"dsa1_r2065_bqs", "dsa1_r2065_bnlj", "dsa1_r2065_sntq"});
        CPL01_MAP.put("R2012", new String[]{"dsa1_r2012_bqs", "dsa1_r2012_bnlj", "dsa1_r2012_sntq"});
        CPL01_MAP.put("R2013", new String[]{"dsa1_r2013_bqs", "dsa1_r2013_bnlj", "dsa1_r2013_sntq"});
        CPL01_MAP.put("R2014", new String[]{"dsa1_r2014_bqs", "dsa1_r2014_bnlj", "dsa1_r2014_sntq"});
        CPL01_MAP.put("R2015", new String[]{"dsa1_r2015_bqs", "dsa1_r2015_bnlj", "dsa1_r2015_sntq"});
        CPL01_MAP.put("R2059", new String[]{"dsa1_r2059_bqs", "dsa1_r2059_bnlj", "dsa1_r2059_sntq"});
        CPL01_MAP.put("R2060", new String[]{"dsa1_r2060_bqs", "dsa1_r2060_bnlj", "dsa1_r2060_sntq"});
        CPL01_MAP.put("R2061", new String[]{"dsa1_r2061_bqs", "dsa1_r2061_bnlj", "dsa1_r2061_sntq"});
        CPL01_MAP.put("R2062", new String[]{"dsa1_r2062_bqs", "dsa1_r2062_bnlj", "dsa1_r2062_sntq"});
        CPL01_MAP.put("R2063", new String[]{"dsa1_r2063_bqs", "dsa1_r2063_bnlj", "dsa1_r2063_sntq"});
        CPL01_MAP.put("R2064", new String[]{"dsa1_r2064_bqs", "dsa1_r2064_bnlj", "dsa1_r2064_sntq"});
        CPL01_MAP.put("R2035", new String[]{"dsa1_r2035_bqs", "dsa1_r2035_bnlj", "dsa1_r2035_sntq"});
        CPL01_MAP.put("R2040", new String[]{"dsa1_r2040_bqs", "dsa1_r2040_bnlj", "dsa1_r2040_sntq"});
        CPL01_MAP.put("R2008", new String[]{"dsa1_r2008_bqs", "dsa1_r2008_bnlj", "dsa1_r2008_sntq"});
        CPL01_MAP.put("R2054", new String[]{"dsa1_r2054_bqs", "dsa1_r2054_bnlj", "dsa1_r2054_sntq"});
        CPL01_MAP.put("R2055", new String[]{"dsa1_r2055_bqs", "dsa1_r2055_bnlj", "dsa1_r2055_sntq"});
        CPL01_MAP.put("R2056", new String[]{"dsa1_r2056_bqs", "dsa1_r2056_bnlj", "dsa1_r2056_sntq"});
        CPL01_MAP.put("R2057", new String[]{"dsa1_r2057_bqs", "dsa1_r2057_bnlj", "dsa1_r2057_sntq"});
        CPL01_MAP.put("R2058", new String[]{"dsa1_r2058_bqs", "dsa1_r2058_bnlj", "dsa1_r2058_sntq"});
        CPL01_MAP.put("R2067", new String[]{"dsa1_r2067_bqs", "dsa1_r2067_bnlj", "dsa1_r2067_sntq"});
        CPL01_MAP.put("R2070", new String[]{"dsa1_r2070_bqs", "dsa1_r2070_bnlj", "dsa1_r2070_sntq"});

        REPORT_TYPE_FIELD_MAPS.put("CPL01", CPL01_MAP);


        BS01_MAP.put("R1038", new String[]{"dsa1_r1038_qm","dsa1_r1038_nc"});
        BS01_MAP.put("R1003", new String[]{"dsa1_r1003_qm","dsa1_r1003_nc"});
        BS01_MAP.put("R1005", new String[]{"dsa1_r1005_qm","dsa1_r1005_nc"});
        BS01_MAP.put("R1019", new String[]{"dsa1_r1019_qm","dsa1_r1019_nc"});
        BS01_MAP.put("R1006", new String[]{"dsa1_r1006_qm","dsa1_r1006_nc"});
        BS01_MAP.put("R1008", new String[]{"dsa1_r1008_qm","dsa1_r1008_nc"});
        BS01_MAP.put("R1009", new String[]{"dsa1_r1009_qm","dsa1_r1009_nc"});
        BS01_MAP.put("R1010", new String[]{"dsa1_r1010_qm","dsa1_r1010_nc"});
        BS01_MAP.put("R1011", new String[]{"dsa1_r1011_qm","dsa1_r1011_nc"});
        BS01_MAP.put("R1012", new String[]{"dsa1_r1012_qm","dsa1_r1012_nc"});
        BS01_MAP.put("R1013", new String[]{"dsa1_r1013_qm","dsa1_r1013_nc"});
        BS01_MAP.put("R1014", new String[]{"dsa1_r1014_qm","dsa1_r1014_nc"});
        BS01_MAP.put("R1015", new String[]{"dsa1_r1015_qm","dsa1_r1015_nc"});
        BS01_MAP.put("R1016", new String[]{"dsa1_r1016_qm","dsa1_r1016_nc"});
        BS01_MAP.put("R1017", new String[]{"dsa1_r1017_qm","dsa1_r1017_nc"});
        BS01_MAP.put("R1076", new String[]{"dsa1_r1076_qm","dsa1_r1076_nc"});
        BS01_MAP.put("R1077", new String[]{"dsa1_r1077_qm","dsa1_r1077_nc"});
        BS01_MAP.put("R1078", new String[]{"dsa1_r1078_qm","dsa1_r1078_nc"});
        BS01_MAP.put("R1082", new String[]{"dsa1_r1082_qm","dsa1_r1082_nc"});
        BS01_MAP.put("R1084", new String[]{"dsa1_r1084_qm","dsa1_r1084_nc"});
        BS01_MAP.put("R1085", new String[]{"dsa1_r1085_qm","dsa1_r1085_nc"});
        BS01_MAP.put("R1086", new String[]{"dsa1_r1086_qm","dsa1_r1086_nc"});
        BS01_MAP.put("R1020", new String[]{"dsa1_r1020_qm","dsa1_r1020_nc"});
        BS01_MAP.put("R1021", new String[]{"dsa1_r1021_qm","dsa1_r1021_nc"});
        BS01_MAP.put("R1022", new String[]{"dsa1_r1022_qm","dsa1_r1022_nc"});
        BS01_MAP.put("R1023", new String[]{"dsa1_r1023_qm","dsa1_r1023_nc"});
        BS01_MAP.put("R1024", new String[]{"dsa1_r1024_qm","dsa1_r1024_nc"});
        BS01_MAP.put("R1025", new String[]{"dsa1_r1025_qm","dsa1_r1025_nc"});
        BS01_MAP.put("R1026", new String[]{"dsa1_r1026_qm","dsa1_r1026_nc"});
        BS01_MAP.put("R1027", new String[]{"dsa1_r1027_qm","dsa1_r1027_nc"});
        BS01_MAP.put("R1028", new String[]{"dsa1_r1028_qm","dsa1_r1028_nc"});
        BS01_MAP.put("R1029", new String[]{"dsa1_r1029_qm","dsa1_r1029_nc"});
        BS01_MAP.put("R1030", new String[]{"dsa1_r1030_qm","dsa1_r1030_nc"});
        BS01_MAP.put("R1031", new String[]{"dsa1_r1031_qm","dsa1_r1031_nc"});
        BS01_MAP.put("R1032", new String[]{"dsa1_r1032_qm","dsa1_r1032_nc"});
        BS01_MAP.put("R1033", new String[]{"dsa1_r1033_qm","dsa1_r1033_nc"});
        BS01_MAP.put("R1034", new String[]{"dsa1_r1034_qm","dsa1_r1034_nc"});
        BS01_MAP.put("R1035", new String[]{"dsa1_r1035_qm","dsa1_r1035_nc"});
        BS01_MAP.put("R1036", new String[]{"dsa1_r1036_qm","dsa1_r1036_nc"});
        BS01_MAP.put("R1092", new String[]{"dsa1_r1092_qm","dsa1_r1092_nc"});
        BS01_MAP.put("R1093", new String[]{"dsa1_r1093_qm","dsa1_r1093_nc"});
        BS01_MAP.put("R1094", new String[]{"dsa1_r1094_qm","dsa1_r1094_nc"});
        BS01_MAP.put("R1095", new String[]{"dsa1_r1095_qm","dsa1_r1095_nc"});
        BS01_MAP.put("R1096", new String[]{"dsa1_r1096_qm","dsa1_r1096_nc"});
        BS01_MAP.put("R1097", new String[]{"dsa1_r1097_qm","dsa1_r1097_nc"});
        BS01_MAP.put("R1098", new String[]{"dsa1_r1098_qm","dsa1_r1098_nc"});
        BS01_MAP.put("R1099", new String[]{"dsa1_r1099_qm","dsa1_r1099_nc"});
        BS01_MAP.put("R1040", new String[]{"dsa1_r1040_qm","dsa1_r1040_nc"});
        BS01_MAP.put("R1042", new String[]{"dsa1_r1042_qm","dsa1_r1042_nc"});
        BS01_MAP.put("R1057", new String[]{"dsa1_r1057_qm","dsa1_r1057_nc"});
        BS01_MAP.put("R1067", new String[]{"dsa1_r1067_qm","dsa1_r1067_nc"});
        BS01_MAP.put("R1043", new String[]{"dsa1_r1043_qm","dsa1_r1043_nc"});
        BS01_MAP.put("R1045", new String[]{"dsa1_r1045_qm","dsa1_r1045_nc"});
        BS01_MAP.put("R1046", new String[]{"dsa1_r1046_qm","dsa1_r1046_nc"});
        BS01_MAP.put("R1047", new String[]{"dsa1_r1047_qm","dsa1_r1047_nc"});
        BS01_MAP.put("R1048", new String[]{"dsa1_r1048_qm","dsa1_r1048_nc"});
        BS01_MAP.put("R1049", new String[]{"dsa1_r1049_qm","dsa1_r1049_nc"});
        BS01_MAP.put("R1050", new String[]{"dsa1_r1050_qm","dsa1_r1050_nc"});
        BS01_MAP.put("R1051", new String[]{"dsa1_r1051_qm","dsa1_r1051_nc"});
        BS01_MAP.put("R1052", new String[]{"dsa1_r1052_qm","dsa1_r1052_nc"});
        BS01_MAP.put("R1053", new String[]{"dsa1_r1053_qm","dsa1_r1053_nc"});
        BS01_MAP.put("R1054", new String[]{"dsa1_r1054_qm","dsa1_r1054_nc"});
        BS01_MAP.put("R1055", new String[]{"dsa1_r1055_qm","dsa1_r1055_nc"});
        BS01_MAP.put("R1103", new String[]{"dsa1_r1103_qm","dsa1_r1103_nc"});
        BS01_MAP.put("R1104", new String[]{"dsa1_r1104_qm","dsa1_r1104_nc"});
        BS01_MAP.put("R1105", new String[]{"dsa1_r1105_qm","dsa1_r1105_nc"});
        BS01_MAP.put("R1111", new String[]{"dsa1_r1111_qm","dsa1_r1111_nc"});
        BS01_MAP.put("R1114", new String[]{"dsa1_r1114_qm","dsa1_r1114_nc"});
        BS01_MAP.put("R1115", new String[]{"dsa1_r1115_qm","dsa1_r1115_nc"});
        BS01_MAP.put("R1116", new String[]{"dsa1_r1116_qm","dsa1_r1116_nc"});
        BS01_MAP.put("R1058", new String[]{"dsa1_r1058_qm","dsa1_r1058_nc"});
        BS01_MAP.put("R1059", new String[]{"dsa1_r1059_qm","dsa1_r1059_nc"});
        BS01_MAP.put("R1060", new String[]{"dsa1_r1060_qm","dsa1_r1060_nc"});
        BS01_MAP.put("R1061", new String[]{"dsa1_r1061_qm","dsa1_r1061_nc"});
        BS01_MAP.put("R1062", new String[]{"dsa1_r1062_qm","dsa1_r1062_nc"});
        BS01_MAP.put("R1063", new String[]{"dsa1_r1063_qm","dsa1_r1063_nc"});
        BS01_MAP.put("R1064", new String[]{"dsa1_r1064_qm","dsa1_r1064_nc"});
        BS01_MAP.put("R1065", new String[]{"dsa1_r1065_qm","dsa1_r1065_nc"});
        BS01_MAP.put("R1120", new String[]{"dsa1_r1120_qm","dsa1_r1120_nc"});
        BS01_MAP.put("R1121", new String[]{"dsa1_r1121_qm","dsa1_r1121_nc"});
        BS01_MAP.put("R1122", new String[]{"dsa1_r1122_qm","dsa1_r1122_nc"});
        BS01_MAP.put("R1123", new String[]{"dsa1_r1123_qm","dsa1_r1123_nc"});
        BS01_MAP.put("R1124", new String[]{"dsa1_r1124_qm","dsa1_r1124_nc"});
        BS01_MAP.put("R1068", new String[]{"dsa1_r1068_qm","dsa1_r1068_nc"});
        BS01_MAP.put("R1069", new String[]{"dsa1_r1069_qm","dsa1_r1069_nc"});
        BS01_MAP.put("R1070", new String[]{"dsa1_r1070_qm","dsa1_r1070_nc"});
        BS01_MAP.put("R1071", new String[]{"dsa1_r1071_qm","dsa1_r1071_nc"});
        BS01_MAP.put("R1072", new String[]{"dsa1_r1072_qm","dsa1_r1072_nc"});
        BS01_MAP.put("R1073", new String[]{"dsa1_r1073_qm","dsa1_r1073_nc"});
        BS01_MAP.put("R1074", new String[]{"dsa1_r1074_qm","dsa1_r1074_nc"});
        BS01_MAP.put("R1075", new String[]{"dsa1_r1075_qm","dsa1_r1075_nc"});
        BS01_MAP.put("R1125", new String[]{"dsa1_r1125_qm","dsa1_r1125_nc"});
        BS01_MAP.put("R1126", new String[]{"dsa1_r1126_qm","dsa1_r1126_nc"});
        BS01_MAP.put("R1127", new String[]{"dsa1_r1127_qm","dsa1_r1127_nc"});
        BS01_MAP.put("R1128", new String[]{"dsa1_r1128_qm","dsa1_r1128_nc"});
        BS01_MAP.put("R1129", new String[]{"dsa1_r1129_qm","dsa1_r1129_nc"});
        BS01_MAP.put("R1130", new String[]{"dsa1_r1130_qm","dsa1_r1130_nc"});
        BS01_MAP.put("R1131", new String[]{"dsa1_r1131_qm","dsa1_r1131_nc"});
        BS01_MAP.put("R1132", new String[]{"dsa1_r1132_qm","dsa1_r1132_nc"});
        BS01_MAP.put("R1133", new String[]{"dsa1_r1133_qm","dsa1_r1133_nc"});
        BS01_MAP.put("R1134", new String[]{"dsa1_r1134_qm","dsa1_r1134_nc"});
        BS01_MAP.put("R1135", new String[]{"dsa1_r1135_qm","dsa1_r1135_nc"});
        BS01_MAP.put("R1136", new String[]{"dsa1_r1136_qm","dsa1_r1136_nc"});
        BS01_MAP.put("R1137", new String[]{"dsa1_r1137_qm","dsa1_r1137_nc"});
        BS01_MAP.put("R1138", new String[]{"dsa1_r1138_qm","dsa1_r1138_nc"});
        BS01_MAP.put("R1139", new String[]{"dsa1_r1139_qm","dsa1_r1139_nc"});
        BS01_MAP.put("R1140", new String[]{"dsa1_r1140_qm","dsa1_r1140_nc"});
        BS01_MAP.put("R1141", new String[]{"dsa1_r1141_qm","dsa1_r1141_nc"});
        BS01_MAP.put("R1142", new String[]{"dsa1_r1142_qm","dsa1_r1142_nc"});
        BS01_MAP.put("R1143", new String[]{"dsa1_r1143_qm","dsa1_r1143_nc"});
        BS01_MAP.put("R1144", new String[]{"dsa1_r1144_qm","dsa1_r1144_nc"});

        REPORT_TYPE_FIELD_MAPS.put("BS01", BS01_MAP);

        CF01_MAP.put("R3003", new String[]{"dsa1_r3003_bq","dsa1_r3003_bnlj","dsa1_r3003_sntq"});
        CF01_MAP.put("R3040", new String[]{"dsa1_r3040_bq","dsa1_r3040_bnlj","dsa1_r3040_sntq"});
        CF01_MAP.put("R3041", new String[]{"dsa1_r3041_bq","dsa1_r3041_bnlj","dsa1_r3041_sntq"});
        CF01_MAP.put("R3005", new String[]{"dsa1_r3005_bq","dsa1_r3005_bnlj","dsa1_r3005_sntq"});
        CF01_MAP.put("R3006", new String[]{"dsa1_r3006_bq","dsa1_r3006_bnlj","dsa1_r3006_sntq"});
        CF01_MAP.put("R3007", new String[]{"dsa1_r3007_bq","dsa1_r3007_bnlj","dsa1_r3007_sntq"});
        CF01_MAP.put("R3008", new String[]{"dsa1_r3008_bq","dsa1_r3008_bnlj","dsa1_r3008_sntq"});
        CF01_MAP.put("R3009", new String[]{"dsa1_r3009_bq","dsa1_r3009_bnlj","dsa1_r3009_sntq"});
        CF01_MAP.put("R3010", new String[]{"dsa1_r3010_bq","dsa1_r3010_bnlj","dsa1_r3010_sntq"});
        CF01_MAP.put("R3011", new String[]{"dsa1_r3011_bq","dsa1_r3011_bnlj","dsa1_r3011_sntq"});
        CF01_MAP.put("R3012", new String[]{"dsa1_r3012_bq","dsa1_r3012_bnlj","dsa1_r3012_sntq"});
        CF01_MAP.put("R3013", new String[]{"dsa1_r3013_bq","dsa1_r3013_bnlj","dsa1_r3013_sntq"});
        CF01_MAP.put("R3014", new String[]{"dsa1_r3014_bq","dsa1_r3014_bnlj","dsa1_r3014_sntq"});
        CF01_MAP.put("R3016", new String[]{"dsa1_r3016_bq","dsa1_r3016_bnlj","dsa1_r3016_sntq"});
        CF01_MAP.put("R3017", new String[]{"dsa1_r3017_bq","dsa1_r3017_bnlj","dsa1_r3017_sntq"});
        CF01_MAP.put("R3018", new String[]{"dsa1_r3018_bq","dsa1_r3018_bnlj","dsa1_r3018_sntq"});
        CF01_MAP.put("R3019", new String[]{"dsa1_r3019_bq","dsa1_r3019_bnlj","dsa1_r3019_sntq"});
        CF01_MAP.put("R3020", new String[]{"dsa1_r3020_bq","dsa1_r3020_bnlj","dsa1_r3020_sntq"});
        CF01_MAP.put("R3021", new String[]{"dsa1_r3021_bq","dsa1_r3021_bnlj","dsa1_r3021_sntq"});
        CF01_MAP.put("R3022", new String[]{"dsa1_r3022_bq","dsa1_r3022_bnlj","dsa1_r3022_sntq"});
        CF01_MAP.put("R3023", new String[]{"dsa1_r3023_bq","dsa1_r3023_bnlj","dsa1_r3023_sntq"});
        CF01_MAP.put("R3024", new String[]{"dsa1_r3024_bq","dsa1_r3024_bnlj","dsa1_r3024_sntq"});
        CF01_MAP.put("R3025", new String[]{"dsa1_r3025_bq","dsa1_r3025_bnlj","dsa1_r3025_sntq"});
        CF01_MAP.put("R3026", new String[]{"dsa1_r3026_bq","dsa1_r3026_bnlj","dsa1_r3026_sntq"});
        CF01_MAP.put("R3027", new String[]{"dsa1_r3027_bq","dsa1_r3027_bnlj","dsa1_r3027_sntq"});
        CF01_MAP.put("R3029", new String[]{"dsa1_r3029_bq","dsa1_r3029_bnlj","dsa1_r3029_sntq"});
        CF01_MAP.put("R3030", new String[]{"dsa1_r3030_bq","dsa1_r3030_bnlj","dsa1_r3030_sntq"});
        CF01_MAP.put("R3031", new String[]{"dsa1_r3031_bq","dsa1_r3031_bnlj","dsa1_r3031_sntq"});
        CF01_MAP.put("R3032", new String[]{"dsa1_r3032_bq","dsa1_r3032_bnlj","dsa1_r3032_sntq"});
        CF01_MAP.put("R3033", new String[]{"dsa1_r3033_bq","dsa1_r3033_bnlj","dsa1_r3033_sntq"});
        CF01_MAP.put("R3034", new String[]{"dsa1_r3034_bq","dsa1_r3034_bnlj","dsa1_r3034_sntq"});
        CF01_MAP.put("R3035", new String[]{"dsa1_r3035_bq","dsa1_r3035_bnlj","dsa1_r3035_sntq"});
        CF01_MAP.put("R3036", new String[]{"dsa1_r3036_bq","dsa1_r3036_bnlj","dsa1_r3036_sntq"});
        CF01_MAP.put("R3037", new String[]{"dsa1_r3037_bq","dsa1_r3037_bnlj","dsa1_r3037_sntq"});
        CF01_MAP.put("R3038", new String[]{"dsa1_r3038_bq","dsa1_r3038_bnlj","dsa1_r3038_sntq"});
        CF01_MAP.put("R3039", new String[]{"dsa1_r3039_bq","dsa1_r3039_bnlj","dsa1_r3039_sntq"});

        REPORT_TYPE_FIELD_MAPS.put("CF01", CF01_MAP);

        CQZ01_MAP.put("QZ002", new String[]{"dsa1_qz002_qm","dsa1_qz002_bnlj","dsa1_qz002_sntq"});
        CQZ01_MAP.put("QZ003", new String[]{"dsa1_qz003_qm","dsa1_qz003_bnlj","dsa1_qz003_sntq"});
        CQZ01_MAP.put("QZ004", new String[]{"dsa1_qz004_qm","dsa1_qz004_bnlj","dsa1_qz004_sntq"});
        CQZ01_MAP.put("QZ005", new String[]{"dsa1_qz005_qm","dsa1_qz005_bnlj","dsa1_qz005_sntq"});
        CQZ01_MAP.put("QZ006", new String[]{"dsa1_qz006_qm","dsa1_qz006_bnlj","dsa1_qz006_sntq"});
        CQZ01_MAP.put("QZ007", new String[]{"dsa1_qz007_qm","dsa1_qz007_bnlj","dsa1_qz007_sntq"});
        CQZ01_MAP.put("QZ008", new String[]{"dsa1_qz008_qm","dsa1_qz008_bnlj","dsa1_qz008_sntq"});
        CQZ01_MAP.put("QZ009", new String[]{"dsa1_qz009_qm","dsa1_qz009_bnlj","dsa1_qz009_sntq"});
        CQZ01_MAP.put("QZ010", new String[]{"dsa1_qz010_qm","dsa1_qz010_bnlj","dsa1_qz010_sntq"});
        CQZ01_MAP.put("QZ011", new String[]{"dsa1_qz011_qm","dsa1_qz011_bnlj","dsa1_qz011_sntq"});
        CQZ01_MAP.put("QZ012", new String[]{"dsa1_qz012_qm","dsa1_qz012_bnlj","dsa1_qz012_sntq"});
        CQZ01_MAP.put("QZ058", new String[]{"dsa1_qz058_qm","dsa1_qz058_bnlj","dsa1_qz058_sntq"});
        CQZ01_MAP.put("QZ013", new String[]{"dsa1_qz013_qm","dsa1_qz013_bnlj","dsa1_qz013_sntq"});
        CQZ01_MAP.put("QZ014", new String[]{"dsa1_qz014_qm","dsa1_qz014_bnlj","dsa1_qz014_sntq"});
        CQZ01_MAP.put("QZ015", new String[]{"dsa1_qz015_qm","dsa1_qz015_bnlj","dsa1_qz015_sntq"});
        CQZ01_MAP.put("QZ016", new String[]{"dsa1_qz016_qm","dsa1_qz016_bnlj","dsa1_qz016_sntq"});
        CQZ01_MAP.put("QZ017", new String[]{"dsa1_qz017_qm","dsa1_qz017_bnlj","dsa1_qz017_sntq"});
        CQZ01_MAP.put("QZ018", new String[]{"dsa1_qz018_qm","dsa1_qz018_bnlj","dsa1_qz018_sntq"});
        CQZ01_MAP.put("QZ019", new String[]{"dsa1_qz019_qm","dsa1_qz019_bnlj","dsa1_qz019_sntq"});
        CQZ01_MAP.put("QZ020", new String[]{"dsa1_qz020_qm","dsa1_qz020_bnlj","dsa1_qz020_sntq"});
        CQZ01_MAP.put("QZ021", new String[]{"dsa1_qz021_qm","dsa1_qz021_bnlj","dsa1_qz021_sntq"});
        CQZ01_MAP.put("QZ022", new String[]{"dsa1_qz022_qm","dsa1_qz022_bnlj","dsa1_qz022_sntq"});
        CQZ01_MAP.put("QZ023", new String[]{"dsa1_qz023_qm","dsa1_qz023_bnlj","dsa1_qz023_sntq"});
        CQZ01_MAP.put("QZ024", new String[]{"dsa1_qz024_qm","dsa1_qz024_bnlj","dsa1_qz024_sntq"});
        CQZ01_MAP.put("QZ025", new String[]{"dsa1_qz025_qm","dsa1_qz025_bnlj","dsa1_qz025_sntq"});
        CQZ01_MAP.put("QZ026", new String[]{"dsa1_qz026_qm","dsa1_qz026_bnlj","dsa1_qz026_sntq"});
        CQZ01_MAP.put("QZ027", new String[]{"dsa1_qz027_qm","dsa1_qz027_bnlj","dsa1_qz027_sntq"});
        CQZ01_MAP.put("QZ028", new String[]{"dsa1_qz028_qm","dsa1_qz028_bnlj","dsa1_qz028_sntq"});
        CQZ01_MAP.put("QZ029", new String[]{"dsa1_qz029_qm","dsa1_qz029_bnlj","dsa1_qz029_sntq"});
        CQZ01_MAP.put("QZ030", new String[]{"dsa1_qz030_qm","dsa1_qz030_bnlj","dsa1_qz030_sntq"});
        CQZ01_MAP.put("QZ031", new String[]{"dsa1_qz031_qm","dsa1_qz031_bnlj","dsa1_qz031_sntq"});
        CQZ01_MAP.put("QZ032", new String[]{"dsa1_qz032_qm","dsa1_qz032_bnlj","dsa1_qz032_sntq"});
        CQZ01_MAP.put("QZ033", new String[]{"dsa1_qz033_qm","dsa1_qz033_bnlj","dsa1_qz033_sntq"});
        CQZ01_MAP.put("QZ034", new String[]{"dsa1_qz034_qm","dsa1_qz034_bnlj","dsa1_qz034_sntq"});
        CQZ01_MAP.put("QZ035", new String[]{"dsa1_qz035_qm","dsa1_qz035_bnlj","dsa1_qz035_sntq"});
        CQZ01_MAP.put("QZ036", new String[]{"dsa1_qz036_qm","dsa1_qz036_bnlj","dsa1_qz036_sntq"});
        CQZ01_MAP.put("QZ037", new String[]{"dsa1_qz037_qm","dsa1_qz037_bnlj","dsa1_qz037_sntq"});
        CQZ01_MAP.put("QZ038", new String[]{"dsa1_qz038_qm","dsa1_qz038_bnlj","dsa1_qz038_sntq"});
        CQZ01_MAP.put("QZ039", new String[]{"dsa1_qz039_qm","dsa1_qz039_bnlj","dsa1_qz039_sntq"});
        CQZ01_MAP.put("QZ040", new String[]{"dsa1_qz040_qm","dsa1_qz040_bnlj","dsa1_qz040_sntq"});
        CQZ01_MAP.put("QZ041", new String[]{"dsa1_qz041_qm","dsa1_qz041_bnlj","dsa1_qz041_sntq"});
        CQZ01_MAP.put("QZ042", new String[]{"dsa1_qz042_qm","dsa1_qz042_bnlj","dsa1_qz042_sntq"});
        CQZ01_MAP.put("QZ043", new String[]{"dsa1_qz043_qm","dsa1_qz043_bnlj","dsa1_qz043_sntq"});
        CQZ01_MAP.put("QZ044", new String[]{"dsa1_qz044_qm","dsa1_qz044_bnlj","dsa1_qz044_sntq"});
        CQZ01_MAP.put("QZ045", new String[]{"dsa1_qz045_qm","dsa1_qz045_bnlj","dsa1_qz045_sntq"});
        CQZ01_MAP.put("QZ046", new String[]{"dsa1_qz046_qm","dsa1_qz046_bnlj","dsa1_qz046_sntq"});
        CQZ01_MAP.put("QZ047", new String[]{"dsa1_qz047_qm","dsa1_qz047_bnlj","dsa1_qz047_sntq"});
        CQZ01_MAP.put("QZ048", new String[]{"dsa1_qz048_qm","dsa1_qz048_bnlj","dsa1_qz048_sntq"});
        CQZ01_MAP.put("QZ049", new String[]{"dsa1_qz049_qm","dsa1_qz049_bnlj","dsa1_qz049_sntq"});
        CQZ01_MAP.put("QZ050", new String[]{"dsa1_qz050_qm","dsa1_qz050_bnlj","dsa1_qz050_sntq"});
        CQZ01_MAP.put("QZ051", new String[]{"dsa1_qz051_qm","dsa1_qz051_bnlj","dsa1_qz051_sntq"});
        CQZ01_MAP.put("QZ052", new String[]{"dsa1_qz052_qm","dsa1_qz052_bnlj","dsa1_qz052_sntq"});
        CQZ01_MAP.put("QZ053", new String[]{"dsa1_qz053_qm","dsa1_qz053_bnlj","dsa1_qz053_sntq"});
        CQZ01_MAP.put("QZ054", new String[]{"dsa1_qz054_qm","dsa1_qz054_bnlj","dsa1_qz054_sntq"});
        CQZ01_MAP.put("QZ055", new String[]{"dsa1_qz055_qm","dsa1_qz055_bnlj","dsa1_qz055_sntq"});
        CQZ01_MAP.put("QZ056", new String[]{"dsa1_qz056_qm","dsa1_qz056_bnlj","dsa1_qz056_sntq"});
        CQZ01_MAP.put("QZ057", new String[]{"dsa1_qz057_qm","dsa1_qz057_bnlj","dsa1_qz057_sntq"});
        CQZ01_MAP.put("QZ059", new String[]{"dsa1_qz059_qm","dsa1_qz059_bnlj","dsa1_qz059_sntq"});
        CQZ01_MAP.put("QZ060", new String[]{"dsa1_qz060_qm","dsa1_qz060_bnlj","dsa1_qz060_sntq"});
        CQZ01_MAP.put("QZ061", new String[]{"dsa1_qz061_qm","dsa1_qz061_bnlj","dsa1_qz061_sntq"});
        CQZ01_MAP.put("QZ062", new String[]{"dsa1_qz062_qm","dsa1_qz062_bnlj","dsa1_qz062_sntq"});
        CQZ01_MAP.put("QZ063", new String[]{"dsa1_qz063_qm","dsa1_qz063_bnlj","dsa1_qz063_sntq"});
        CQZ01_MAP.put("QZ064", new String[]{"dsa1_qz064_qm","dsa1_qz064_bnlj","dsa1_qz064_sntq"});
        CQZ01_MAP.put("QZ065", new String[]{"dsa1_qz065_qm","dsa1_qz065_bnlj","dsa1_qz065_sntq"});
        CQZ01_MAP.put("QZ066", new String[]{"dsa1_qz066_qm","dsa1_qz066_bnlj","dsa1_qz066_sntq"});
        CQZ01_MAP.put("QZ067", new String[]{"dsa1_qz067_qm","dsa1_qz067_bnlj","dsa1_qz067_sntq"});
        CQZ01_MAP.put("QZ068", new String[]{"dsa1_qz068_qm","dsa1_qz068_bnlj","dsa1_qz068_sntq"});
        CQZ01_MAP.put("QZ069", new String[]{"dsa1_qz069_qm","dsa1_qz069_bnlj","dsa1_qz069_sntq"});
        CQZ01_MAP.put("QZ070", new String[]{"dsa1_qz070_qm","dsa1_qz070_bnlj","dsa1_qz070_sntq"});
        CQZ01_MAP.put("QZ071", new String[]{"dsa1_qz071_qm","dsa1_qz071_bnlj","dsa1_qz071_sntq"});

        REPORT_TYPE_FIELD_MAPS.put("CQZ01", CQZ01_MAP);


// ZYYWSYB 科目映射 - 只需要科目编码，用于去重和查询
        // 实际金额取值按固定位置，不需要配置具体字段
        ZYYWSYB_MAP.put("ZYXM", new String[]{});      // 主业项目合计
        ZYYWSYB_MAP.put("HWLHJ", new String[]{});     // 货物类合计
        ZYYWSYB_MAP.put("HWLHJ01", new String[]{});   // 商品销售
        ZYYWSYB_MAP.put("HWLHJ02", new String[]{});   // 粮油销售
        ZYYWSYB_MAP.put("HWLHJ03", new String[]{});   // 大宗贸易
        ZYYWSYB_MAP.put("HWLHJ04", new String[]{});   // 林下经济
        ZYYWSYB_MAP.put("HWLHJ05", new String[]{});   // 林下销售
        ZYYWSYB_MAP.put("HWLHJ06", new String[]{});   // 材料销售
        ZYYWSYB_MAP.put("HWLHJ07", new String[]{});   // 水费
        ZYYWSYB_MAP.put("HWLHJ08", new String[]{});   // 电费
        ZYYWSYB_MAP.put("HWLHJ09", new String[]{});   // 水费公摊
        ZYYWSYB_MAP.put("HWLHJ10", new String[]{});   // 电费公摊
        ZYYWSYB_MAP.put("HWLHJ11", new String[]{});   // 电损平摊
        ZYYWSYB_MAP.put("HWLHJ12", new String[]{});   // 其他公摊
        ZYYWSYB_MAP.put("HWLHJ13", new String[]{});   // 货物类-其他
        ZYYWSYB_MAP.put("FDCHJ", new String[]{});     // 房地产合计
        ZYYWSYB_MAP.put("FDCHJ01", new String[]{});   // 住宅
        ZYYWSYB_MAP.put("FDCHJ02", new String[]{});   // 商铺
        ZYYWSYB_MAP.put("FDCHJ03", new String[]{});   // 办公楼
        ZYYWSYB_MAP.put("FDCHJ04", new String[]{});   // 停车位
        ZYYWSYB_MAP.put("FDCHJ05", new String[]{});   // 房地产-其他
        ZYYWSYB_MAP.put("FWLHJ", new String[]{});     // 服务类合计
        ZYYWSYB_MAP.put("FWLHJ01", new String[]{});   // 物业管理
        ZYYWSYB_MAP.put("FWLHJ02", new String[]{});   // 卫生费
        ZYYWSYB_MAP.put("FWLHJ03", new String[]{});   // 维修服务
        ZYYWSYB_MAP.put("FWLHJ04", new String[]{});   // 冷库收入
        ZYYWSYB_MAP.put("FWLHJ05", new String[]{});   // 殡葬服务收入
        ZYYWSYB_MAP.put("FWLHJ06", new String[]{});   // 医护服务
        ZYYWSYB_MAP.put("FWLHJ07", new String[]{});   // 养护服务
        ZYYWSYB_MAP.put("FWLHJ08", new String[]{});   // 鉴证咨询服务
        ZYYWSYB_MAP.put("FWLHJ09", new String[]{});   // 代理服务
        ZYYWSYB_MAP.put("FWLHJ10", new String[]{});   // 林业调查设计服务
        ZYYWSYB_MAP.put("FWLHJ11", new String[]{});   // 资产代管费
        ZYYWSYB_MAP.put("FWLHJ12", new String[]{});   // 餐饮服务
        ZYYWSYB_MAP.put("FWLHJ13", new String[]{});   // 工程服务
        ZYYWSYB_MAP.put("FWLHJ14", new String[]{});   // 代建服务
        ZYYWSYB_MAP.put("FWLHJ15", new String[]{});   // 综合服务费
        ZYYWSYB_MAP.put("FWLHJ16", new String[]{});   // 资金占用费
        ZYYWSYB_MAP.put("FWLHJ17", new String[]{});   // 增值服务
        ZYYWSYB_MAP.put("FWLHJ18", new String[]{});   // 装卸服务
        ZYYWSYB_MAP.put("FWLHJ19", new String[]{});   // 运输服务
        ZYYWSYB_MAP.put("FWLHJ20", new String[]{});   // 仓储服务
        ZYYWSYB_MAP.put("FWLHJ21", new String[]{});   // 产权转让
        ZYYWSYB_MAP.put("FWLHJ22", new String[]{});   // 低空经济农业服务
        ZYYWSYB_MAP.put("FWLHJ23", new String[]{});   // 平台服务
        ZYYWSYB_MAP.put("FWLHJ24", new String[]{});   // 营业管理服务
        ZYYWSYB_MAP.put("FWLHJ25", new String[]{});   // 管理费收入
        ZYYWSYB_MAP.put("FWLHJ26", new String[]{});   // 污水处理服务
        ZYYWSYB_MAP.put("FWLHJ27", new String[]{});   // 融资租赁服务
        ZYYWSYB_MAP.put("FWLHJ28", new String[]{});   // 服务类-其他
        ZYYWSYB_MAP.put("GCSGHJ01", new String[]{});  // 工程施工
        ZYYWSYB_MAP.put("ZLLHJ", new String[]{});     // 租赁类合计
        ZYYWSYB_MAP.put("ZLLHJ01", new String[]{});   // 房屋建筑物租赁
        ZYYWSYB_MAP.put("ZLLHJ02", new String[]{});   // 土地租赁
        ZYYWSYB_MAP.put("ZLLHJ03", new String[]{});   // 场地租赁
        ZYYWSYB_MAP.put("ZLLHJ04", new String[]{});   // 摊位租赁
        ZYYWSYB_MAP.put("ZLLHJ05", new String[]{});   // 车辆停放服务
        ZYYWSYB_MAP.put("ZLLHJ06", new String[]{});   // 动产租赁
        ZYYWSYB_MAP.put("ZLLHJ07", new String[]{});   // 铺面租赁
        ZYYWSYB_MAP.put("ZLLHJ08", new String[]{});   // 租赁类-其他
        ZYYWSYB_MAP.put("ZYQT", new String[]{});      // 其他（主业）
        REPORT_TYPE_FIELD_MAPS.put("ZYYWSYB", ZYYWSYB_MAP);



        // 将 ZYYWSYB 映射添加到 REPORT_TYPE_FIELD_MAPS
        // CWFY01 财务费用明细表映射
        CWFY01_MAP.put("CWFY01", new String[]{});      // 手续费
        CWFY01_MAP.put("CWFY02", new String[]{});      // 贷款费用
        CWFY01_MAP.put("CWFY0201", new String[]{});    // 抵押
        CWFY01_MAP.put("CWFY0202", new String[]{});    // 服务
        CWFY01_MAP.put("CWFY03", new String[]{});      // 利息支出
        CWFY01_MAP.put("CWFY0301", new String[]{});    // 银行贷款利息支出
        CWFY01_MAP.put("CWFY0302", new String[]{});    // 租赁负债利息支出
        CWFY01_MAP.put("CWFY0303", new String[]{});    // 汇票贴现支出
        CWFY01_MAP.put("CWFY0304", new String[]{});    // 内部借款利息支出
        CWFY01_MAP.put("CWFY0305", new String[]{});    // 其他
        CWFY01_MAP.put("CWFY04", new String[]{});      // 利息收入
        CWFY01_MAP.put("CWFY0401", new String[]{});    // 银行存款利息收入
        CWFY01_MAP.put("CWFY0402", new String[]{});    // 内部存款利息收入
        CWFY01_MAP.put("CWFY0403", new String[]{});    // 客户逾期利息收入
        CWFY01_MAP.put("CWFY0404", new String[]{});    // 其他
        CWFY01_MAP.put("CWFY05", new String[]{});      // 现金折扣
        CWFY01_MAP.put("CWFY06", new String[]{});      // 其他费用

        // 将 CWFY01 映射添加到 REPORT_TYPE_FIELD_MAPS
        REPORT_TYPE_FIELD_MAPS.put("CWFY01", CWFY01_MAP);


// GLFY01 管理费用明细表映射
        GLFY01_MAP.put("GLFY01", new String[]{});      // 人工成本
        GLFY01_MAP.put("GLFY0101", new String[]{});    // 职工薪酬
        GLFY01_MAP.put("GLFY0102", new String[]{});    // 职工福利费
        GLFY01_MAP.put("GLFY0103", new String[]{});    // 社会保险费
        GLFY01_MAP.put("GLFY0104", new String[]{});    // 住房公积金
        GLFY01_MAP.put("GLFY0105", new String[]{});    // 职工教育经费
        GLFY01_MAP.put("GLFY0106", new String[]{});    // 工会经费
        GLFY01_MAP.put("GLFY0107", new String[]{});    // 劳保费
        GLFY01_MAP.put("GLFY0108", new String[]{});    // 非货币性福利
        GLFY01_MAP.put("GLFY0109", new String[]{});    // 辞退福利
        GLFY01_MAP.put("GLFY0110", new String[]{});    // 企业年金
        GLFY01_MAP.put("GLFY0111", new String[]{});    // 其他
        GLFY01_MAP.put("GLFY02", new String[]{});      // 劳务费
        GLFY01_MAP.put("GLFY03", new String[]{});      // 办公费
        GLFY01_MAP.put("GLFY0301", new String[]{});    // 办公用品费
        GLFY01_MAP.put("GLFY0302", new String[]{});    // 报刊费
        GLFY01_MAP.put("GLFY0303", new String[]{});    // 快递费
        GLFY01_MAP.put("GLFY0304", new String[]{});    // 图文印刷
        GLFY01_MAP.put("GLFY0305", new String[]{});    // 其他
        GLFY01_MAP.put("GLFY04", new String[]{});      // 差旅费
        GLFY01_MAP.put("GLFY05", new String[]{});      // 通讯费
        GLFY01_MAP.put("GLFY0501", new String[]{});    // 电话费
        GLFY01_MAP.put("GLFY0502", new String[]{});    // 网络费
        GLFY01_MAP.put("GLFY0503", new String[]{});    // 其他
        GLFY01_MAP.put("GLFY06", new String[]{});      // 车辆使用费
        GLFY01_MAP.put("GLFY0601", new String[]{});    // 燃油费
        GLFY01_MAP.put("GLFY0602", new String[]{});    // 保养费
        GLFY01_MAP.put("GLFY0603", new String[]{});    // 年审及保险费
        GLFY01_MAP.put("GLFY0604", new String[]{});    // 过路过桥费与停车费
        GLFY01_MAP.put("GLFY0605", new String[]{});    // 车辆租赁
        GLFY01_MAP.put("GLFY0606", new String[]{});    // 其他
        GLFY01_MAP.put("GLFY07", new String[]{});      // 折旧费
        GLFY01_MAP.put("GLFY0701", new String[]{});    // 固定资产
        GLFY01_MAP.put("GLFY0702", new String[]{});    // 使用权资产
        GLFY01_MAP.put("GLFY08", new String[]{});      // 维修保养费
        GLFY01_MAP.put("GLFY0801", new String[]{});    // 房屋维修费
        GLFY01_MAP.put("GLFY0802", new String[]{});    // 设备维修费
        GLFY01_MAP.put("GLFY0803", new String[]{});    // 其他
        GLFY01_MAP.put("GLFY09", new String[]{});      // 保险费
        GLFY01_MAP.put("GLFY10", new String[]{});      // 低值易耗品摊销
        GLFY01_MAP.put("GLFY11", new String[]{});      // 会议费
        GLFY01_MAP.put("GLFY12", new String[]{});      // 董事会费
        GLFY01_MAP.put("GLFY13", new String[]{});      // 业务招待费
        GLFY01_MAP.put("GLFY14", new String[]{});      // 坏账损失
        GLFY01_MAP.put("GLFY15", new String[]{});      // 广告宣传费
        GLFY01_MAP.put("GLFY16", new String[]{});      // 残疾人保障金
        GLFY01_MAP.put("GLFY17", new String[]{});      // 租赁费
        GLFY01_MAP.put("GLFY1701", new String[]{});    // 房屋租赁
        GLFY01_MAP.put("GLFY1702", new String[]{});    // 设备租赁
        GLFY01_MAP.put("GLFY1703", new String[]{});    // 其他
        GLFY01_MAP.put("GLFY18", new String[]{});      // 无形资产摊销
        GLFY01_MAP.put("GLFY19", new String[]{});      // 聘请中介机构费
        GLFY01_MAP.put("GLFY1901", new String[]{});    // 审计费
        GLFY01_MAP.put("GLFY1902", new String[]{});    // 评估费
        GLFY01_MAP.put("GLFY1903", new String[]{});    // 司法费
        GLFY01_MAP.put("GLFY1904", new String[]{});    // 咨询费
        GLFY01_MAP.put("GLFY1905", new String[]{});    // 融资服务
        GLFY01_MAP.put("GLFY1906", new String[]{});    // 信用评级
        GLFY01_MAP.put("GLFY1907", new String[]{});    // 产权服务
        GLFY01_MAP.put("GLFY1908", new String[]{});    // 其他
        GLFY01_MAP.put("GLFY20", new String[]{});      // 党务经费
        GLFY01_MAP.put("GLFY21", new String[]{});      // 系统建设费
        GLFY01_MAP.put("GLFY22", new String[]{});      // 水费
        GLFY01_MAP.put("GLFY23", new String[]{});      // 电费
        GLFY01_MAP.put("GLFY24", new String[]{});      // 安全生产费
        GLFY01_MAP.put("GLFY25", new String[]{});      // 协会费
        GLFY01_MAP.put("GLFY26", new String[]{});      // 长期待摊费用
        GLFY01_MAP.put("GLFY27", new String[]{});      // 档案管理费
        GLFY01_MAP.put("GLFY28", new String[]{});      // 物业费
        GLFY01_MAP.put("GLFY29", new String[]{});      // 研发费用
        GLFY01_MAP.put("GLFY30", new String[]{});      // 物料消耗
        GLFY01_MAP.put("GLFY31", new String[]{});      // 团员费
        GLFY01_MAP.put("GLFY32", new String[]{});      // 纪检经费
        GLFY01_MAP.put("GLFY33", new String[]{});      // 服务费
        GLFY01_MAP.put("GLFY3301", new String[]{});    // 融资咨询服务
        GLFY01_MAP.put("GLFY3302", new String[]{});    // 投资顾问服务
        GLFY01_MAP.put("GLFY3303", new String[]{});    // 运营服务
        GLFY01_MAP.put("GLFY34", new String[]{});      // 其他
        GLFY01_MAP.put("GLFY", new String[]{});        // 管理费用明细

        // 将 GLFY01 映射添加到 REPORT_TYPE_FIELD_MAPS
        REPORT_TYPE_FIELD_MAPS.put("GLFY01", GLFY01_MAP);

        // 添加其他报表类型的映射


    }

    /**
     * 主方法：保存财务数据
     */
    public static boolean saveFinancialData(String data,
                                            Map<String, Object> params) {


        log.warn("saveFinancialData begin ");
        try {
            if (StringUtils.isBlank(data)) {
                log.warn("保存财务数据失败：数据为空");
                return false;
            }

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ApiResponse apiResponse = objectMapper.readValue(data, ApiResponse.class);
                if (apiResponse.getData() == null || apiResponse.getData().isEmpty()) {
                    log.warn("API返回数据为空");
                    return false;
                }
                // 预加载所有需要的组织信息
                log.info("apiResponse.getData() = {}",apiResponse.getData());
                Set<String> allEntities = extractAllEntities(apiResponse.getData());
                Map<String, DynamicObject> entityMap = BudgetReportService.queryEntities(new ArrayList<>(allEntities));
                int successCount = 0;
                for (DataItem dataItem : apiResponse.getData()) {
                    boolean success = processDataItem(dataItem, params, entityMap);
                    if (success) {
                        successCount++;
                    }
                }
                log.info("财务数据保存完成，成功: {}/{}", successCount, apiResponse.getData().size());
                return successCount > 0;

            } catch (Exception e) {
                log.error("保存财务数据失败: {}", e.getMessage(), e);
                return false;
            }


        } catch (Exception e) {
            System.err.println("保存财务数据失败: " + e.getMessage());
            log.info("catch error ", e.getMessage());
            return false;
        }
    }


    /**
     * 处理单个数据项
     */
    private static boolean processDataItem(DataItem dataItem, Map<String, Object> params,
                                           Map<String, DynamicObject> entityMap) {
        try {
            // 提取基础信息
            String reportNumber = dataItem.getTempNum();
            Map<String, String> pageDims = dataItem.getPageDims();

            if (pageDims == null) {
                log.warn("数据项缺少页面维度信息: {}", reportNumber);
                return false;
            }
            if ("ZYYWSYB".equals(reportNumber)) {
                return processZyywsybData(dataItem, entityMap);
            }

            // 特殊处理 CWFY01 和 GLFY01 报表（结构相同，共用处理方法）
            if ("CWFY01".equals(reportNumber) || "GLFY01".equals(reportNumber)) {
                return processExpenseDetailData(dataItem, entityMap);
            }

            // CPL01、BS01、CF01、CQZ01 统一处理
            return processStandardReport(dataItem, params, entityMap);


        } catch (Exception e) {
            log.error("处理数据项失败: {}", e.getMessage(), e);
            return false;
        }
    }


    private static DynamicObject findExistingData(DynamicObject financialData, String tableName) {
        try {
            List<QFilter> filters = Arrays.asList(
                    new QFilter(FIELD_ORGS, QCP.equals, financialData.getLong(FIELD_ORGS)),
                    new QFilter(FIELD_CURRENCY, QCP.equals, financialData.get(FIELD_CURRENCY)),
                    new QFilter(FIELD_SCENE, QCP.equals, financialData.get(FIELD_SCENE)),
                    new QFilter(FIELD_MODEL_NUM, QCP.equals, financialData.get(FIELD_MODEL_NUM)),
                    new QFilter(FIELD_DURING, QCP.equals, financialData.get(FIELD_DURING)),
                    new QFilter(FIELD_YEARS, QCP.equals, financialData.get(FIELD_YEARS)),
                    new QFilter(FIELD_MONTHS, QCP.equals, financialData.get(FIELD_MONTHS))
            );

            return BusinessDataServiceHelper.loadSingle(tableName, FIELD_ID,
                    filters.toArray(new QFilter[0]));

        } catch (Exception e) {
            log.debug("查找已存在数据失败: {}", e.getMessage());
            return null;
        }
    }

    private static DynamicObject getOrgMapping(String number) {
        try {
            return BusinessDataServiceHelper.loadSingle("dsa1_reportorgmapping", "number,name", new QFilter[]{new QFilter("dsa1_hborgnumber", QCP.equals, number)});
        }catch (Exception e){
            log.error("getOrgMapping: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 保存或更新数据
     */
    private static boolean saveOrUpdate(DynamicObject financialData, DynamicObject existingData, String tableName
    ) {
        try {
            Date now = new Date();
            if (existingData == null) {
                // 新增
                financialData.set(FIELD_CREATE_TIME, now);
                financialData.set(FIELD_MODIFY_TIME, now);

                OperationResult result = SaveServiceHelper.saveOperate(
                        tableName,
                        new DynamicObject[]{financialData},
                        OperateOption.create()
                );

                boolean success = result != null && result.isSuccess();
                if (success) {
                    log.debug("新增数据成功");
                }
                return success;

            } else {
                // 更新
                financialData.set(FIELD_ID, existingData.getLong(FIELD_ID));
                financialData.set(FIELD_MODIFY_TIME, now);

                SaveServiceHelper.update(new DynamicObject[]{financialData});
                log.debug("更新数据成功");
                return true;
            }

        } catch (Exception e) {
            log.error("保存数据失败: {}", e.getMessage(), e);
            return false;
        }
    }


    /**
     * 创建财务数据对象
     */
    private static DynamicObject createFinancialData(DataItem dataItem, Map<String, Object> params,
                                                     String year, String period, String entity,
                                                     Map<String, DynamicObject> entityMap,
                                                     String tableName) {

        DynamicObject financialData = BusinessDataServiceHelper.newDynamicObject(tableName);

        // 处理期间信息
        String yearNum = year.substring(2); // 去掉"FY"前缀
        String month = period.substring(3); // 去掉"M_"前缀
        String during = yearNum + month;

        financialData.set(FIELD_DURING, during);
        financialData.set(FIELD_YEARS, yearNum);
        financialData.set(FIELD_MONTHS, month);

        // 查询组织信息
        DynamicObject org = entityMap.get(entity);
        if (org != null) {
            DynamicObject  dynamicObject  =  getOrgMapping(org.getString("number"));
            if (dynamicObject==null){
                financialData.set(FIELD_COMPANY_NAME, org.getString("name"));
                financialData.set(FIELD_ORGS, org.getLong("id"));
                financialData.set(FIELD_ORGS_NUMBER, org.getString("number"));
            }else{
                financialData.set(FIELD_COMPANY_NAME,dynamicObject.getString("name") );
                financialData.set(FIELD_ORGS, org.getLong("id"));
                financialData.set(FIELD_ORGS_NUMBER, dynamicObject.getString("number"));

            }

        } else {
            log.warn("未找到组织信息: {}", entity);
        }

        // 设置其他基本信息
        financialData.set(FIELD_CURRENCY, params.get("currency"));
        financialData.set(FIELD_SCENE, params.get("scene"));
        financialData.set(FIELD_MODEL_NUM, params.get("modelNum"));
        financialData.set(FIELD_REPORT_NUMBER, dataItem.getTempNum());

        return financialData;
    }

    private static Set<String> extractAllEntities(List<DataItem> dataItems) {
        Set<String> entities = new HashSet<>();
        for (DataItem item : dataItems) {
            Map<String, String> pageDims = item.getPageDims();
            if (pageDims != null && pageDims.containsKey("Entity")) {
                entities.add(pageDims.get("Entity"));
            }
        }
        return entities;
    }



    /**
     * 设置金额数据 - 支持动态字段映射和灵活的数据格式
     */
    private static void setAmountData(DynamicObject financialData,
                                      List<List<Object>> allData,
                                      String tempNum,
                                      Map<String, String[]> fieldMapping) {
        if (CollectionUtils.isEmpty(allData) || fieldMapping == null) {
            return;
        }

        AtomicInteger unmatchedCount = new AtomicInteger(0);
        AtomicInteger matchedCount = new AtomicInteger(0);

        for (List<Object> row : allData) {
            if (row == null || row.isEmpty()) {
                continue;
            }

            // 1. 提取科目编码（只使用编码进行匹配）
            String textWithCode = Objects.toString(row.get(0), "");
            String code = extractCode(textWithCode);      // 提取前面的编码，如 "R1043"

            if (StringUtils.isBlank(code)) {
                log.info("行数据没有有效的科目编码: {}", textWithCode);
                continue;
            }

            // 2. 动态获取金额字段（根据报表类型决定有多少个金额字段）
            List<BigDecimal> amountValues = extractAmountValues(row, tempNum);

            if (amountValues.isEmpty()) {
                log.info("行数据没有金额值: 编码={}", code);
                continue;
            }

            // 3. 使用编码进行匹配
            boolean matched = setAmountByMapping(financialData, code, amountValues, fieldMapping);

            if (matched) {
                matchedCount.incrementAndGet();
            } else {
                unmatchedCount.incrementAndGet();
                log.info("未匹配的科目编码: {}, 报表类型={}", code, tempNum);
            }
        }

        if (unmatchedCount.get() > 0) {
            log.info("报表类型 {} 处理完成：匹配 {} 条，未匹配 {} 条",
                    tempNum, matchedCount.get(), unmatchedCount.get());
        }
    }

    /**
     * 提取科目编码（如 "R1043"）- 只提取编码部分
     */
    private static String extractCode(String textWithCode) {
        if (StringUtils.isBlank(textWithCode)) {
            return "";
        }

        // 格式如 "R1043|短期借款" - 取"|"前面的部分
        if (textWithCode.contains("|")) {
            return textWithCode.split("\\|", 2)[0].trim();
        }

        // 如果没有分隔符，尝试提取开头的字母+数字组合
        String trimmed = textWithCode.trim();
        int index = 0;
        while (index < trimmed.length()) {
            char c = trimmed.charAt(index);
            if (!Character.isLetterOrDigit(c)) {
                break;
            }
            index++;
        }

        if (index > 0) {
            String possibleCode = trimmed.substring(0, index);
            // 检查是否包含至少一个字母和一个数字
            if (possibleCode.matches(".*[A-Za-z].*") && possibleCode.matches(".*[0-9].*")) {
                return possibleCode;
            }
        }

        return "";
    }

    /**
     * 根据映射设置金额数据 - 使用编码匹配
     */
    private static boolean setAmountByMapping(DynamicObject financialData,
                                              String code,
                                              List<BigDecimal> amountValues,
                                              Map<String, String[]> mappingMap) {
        if (mappingMap == null || amountValues.isEmpty() || StringUtils.isBlank(code)) {
            return false;
        }

        // 直接使用编码进行精确匹配
        String[] fieldNames = mappingMap.get(code);

        if (fieldNames != null) {
            // 根据实际金额数量设置字段
            for (int i = 0; i < Math.min(fieldNames.length, amountValues.size()); i++) {
                if (fieldNames[i] != null && amountValues.get(i) != null) {
                    financialData.set(fieldNames[i], amountValues.get(i));
                    log.info("financialData.set(fieldNames[i] = {}, amountValues.get(i) = {})",fieldNames[i],amountValues.get(i));
                }
            }
            return true;
        }

        return false;
    }

    /**
     * 动态提取金额值 - 根据报表类型决定取哪些列
     */
    private static List<BigDecimal> extractAmountValues(List<Object> row, String tempNum) {
        List<BigDecimal> amounts = new ArrayList<>();

        if (row.size() < 2) {
            return amounts;
        }

        // 根据报表类型决定从第几列开始取，取多少列
        int startCol = 1; // 默认从第2列开始（索引1）
        int colCount = getAmountColumnCount(tempNum);

        for (int i = startCol; i < startCol + colCount && i < row.size(); i++) {
            BigDecimal amount = parseBigDecimal(row.get(i));
            // 如果金额为null，也添加null占位，保持列数一致
            amounts.add(amount);
        }

        return amounts;
    }



    private static int getAmountColumnCount(String tempNum) {
        switch (tempNum) {
            case "CPL01":
                return 3;
            case "BS01":
            case "CQY01":
                return 2;
            case "CF01":
                return 3;
            case "ZYYWSYB":
                return 24;
            case "CWFY01":
            case "GLFY01":
                return 5;
            default:
                return 3;
        }
    }


    /**
     * 解析BigDecimal
     */
    private static BigDecimal parseBigDecimal(Object value) {
        if (value == null || "null".equals(value.toString())) {
            return null;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }






    // 新增专门处理 ZYYWSYB 的方法
    private static boolean processZyywsybData(DataItem dataItem,
                                              Map<String, DynamicObject> entityMap) {
        try {
            log.info("processZyywsybData begin and tempNum = {}",dataItem.getTempNum());
            Map<String, String> pageDims = dataItem.getPageDims();
            if (pageDims == null) {
                log.warn("ZYYWSYB数据项缺少页面维度信息");
                return false;
            }

            String year = pageDims.get("Year");
            String period = pageDims.get("Period");
            String entity = pageDims.get("Entity");
            String tempNum = dataItem.getTempNum();

            if (Stream.of(year, period, entity).anyMatch(s -> s == null || s.trim().isEmpty())) {
                log.warn("ZYYWSYB缺少必要的维度信息: year={}, period={}, entity={}", year, period, entity);
                return false;
            }

            // 提取所有数据
            List<List<Object>> allData = DataExtractor.extractAllDatas(dataItem);
            if (CollectionUtils.isEmpty(allData)) {
                log.warn("ZYYWSYB数据项没有数据内容");
                return false;
            }

            // 处理期间信息
            String yearNum = year.substring(2);
            String month = period.substring(3);
            String during = yearNum + month;

            // 获取组织信息
            DynamicObject org = entityMap.get(entity);
            if (org == null) {
                log.warn("ZYYWSYB未找到组织信息: {}", entity);
                return false;
            }

            DynamicObject orgMapping = getOrgMapping(org.getString("number"));
            Long orgId = org.getLong("id");
            String orgName = orgMapping != null ? orgMapping.getString("name") : org.getString("name");
            String orgNumber = orgMapping != null ? orgMapping.getString("number") : org.getString("number");

            int successCount = 0;
            Date nowDate = new Date();

            for (List<Object> row : allData) {
                if (row == null || row.isEmpty()) {
                    continue;
                }

                // 解析科目信息
                String accountInfo = Objects.toString(row.get(0), "");
                String[] accountArr = parseAccountInfo(accountInfo);
                String accountCode = accountArr[0];
                String accountName = accountArr[1];

                if (StringUtils.isBlank(accountCode)) {
                    continue;
                }
                // 计算项目分类（去掉"合计"）
                String projectCategory = getProjectCategory(accountName);
                // 处理4个指标
                boolean result = saveIndicatorRecord(orgId, orgName, orgNumber, during, yearNum, month,
                        pageDims, accountCode, accountName, projectCategory, row, nowDate, tempNum);

                if (result) {
                    successCount++;
                }
            }

            log.info("ZYYWSYB数据处理完成，成功保存 {} 条记录", successCount);
            return successCount > 0;

        } catch (Exception e) {
            log.error("处理ZYYWSYB数据失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 解析科目编号和名称
     */
    private static String[] parseAccountInfo(String accountStr) {
        String[] result = new String[2];
        if (StringUtils.isBlank(accountStr)) {
            result[0] = null;
            result[1] = null;
            return result;
        }

        int pipeIndex = accountStr.indexOf("|");
        if (pipeIndex > 0) {
            result[0] = accountStr.substring(0, pipeIndex);
            result[1] = accountStr.substring(pipeIndex + 1);
        } else {
            result[0] = accountStr;
            result[1] = accountStr;
        }
        return result;
    }

    /**
     * 获取项目分类（去掉"合计"后缀）
     */
    private static String getProjectCategory(String accountName) {
        if (StringUtils.isBlank(accountName)) {
            return null;
        }
        // 如果以"合计"结尾，去掉"合计"
        if (accountName.endsWith("合计")) {
            return accountName.substring(0, accountName.length() - 2);
        }
        return accountName;
    }

    /**
     * 保存单个指标记录
     */
    private static boolean saveIndicatorRecord(Long orgId, String orgName, String orgNumber,
                                               String during, String yearNum, String month,
                                               Map<String, String> pageDims, String accountCode,
                                               String accountName,String projectCategory,
                                               List<Object> row, Date nowDate, String tempNum) {

        // 定义指标配置
        IndicatorConfig[] indicators = {
                new IndicatorConfig("营业总收入", 1, 3, 5, null, null),      // 本期/本年/上年
                new IndicatorConfig("营业成本", 8, 10, 12, null, null),
                new IndicatorConfig("毛利", 15, 17, 19, null, null),
                new IndicatorConfig("毛利率", null, 22, 23, null, null)       // 毛利率无本期数
        };

        boolean allSuccess = true;
        for (IndicatorConfig config : indicators) {
            DynamicObject financialData = BusinessDataServiceHelper.newDynamicObject("dsa1_mergereport_zyyw");

            // 设置基础维度
            financialData.set("dsa1_orgs", orgId);
            financialData.set("dsa1_companyname", orgName);
            financialData.set("dsa1_orgnumber", orgNumber);
            financialData.set("dsa1_during", during);
            financialData.set("dsa1_years", yearNum);
            financialData.set("dsa1_months", month);
            financialData.set("dsa1_currency", pageDims.get("Currency"));
            financialData.set("dsa1_scene", pageDims.get("Scenario"));
            financialData.set("dsa1_modelNum", "XCZX001");
            financialData.set("dsa1_reportnumber", tempNum);
            financialData.set("dsa1_account", accountCode);
            financialData.set("dsa1_accountname", accountName);
            financialData.set("dsa1_projectcategory", projectCategory);
            financialData.set("dsa1_accountclassificati", config.indicatorName);

            // 设置金额
            if (config.bqsIndex != null) {
                financialData.set("dsa1_bqs", parseBigDecimal(getValueFromRow(row, config.bqsIndex)));
            }
            if (config.bnljsIndex != null) {
                financialData.set("dsa1_bnljs", parseBigDecimal(getValueFromRow(row, config.bnljsIndex)));
            }
            if (config.sntqsIndex != null) {
                financialData.set("dsa1_sntqs", parseBigDecimal(getValueFromRow(row, config.sntqsIndex)));
            }

            financialData.set("dsa1_modifytime", nowDate);

            // 查询是否存在（组织+期间+科目+指标）
            DynamicObject existingData = findZyywsybExistingData(orgId, during, accountCode, config.indicatorName);

            boolean saveResult;
            if (existingData == null) {
                financialData.set("dsa1_createtime", nowDate);
                OperationResult result = SaveServiceHelper.saveOperate(
                        "dsa1_mergereport_zyyw",
                        new DynamicObject[]{financialData},
                        OperateOption.create()
                );
                saveResult = result != null && result.isSuccess();
            } else {
                financialData.set("id", existingData.getLong("id"));
                SaveServiceHelper.update(new DynamicObject[]{financialData});
                saveResult = true;
            }

            if (!saveResult) {
                allSuccess = false;
                log.warn("保存指标失败: 科目={}, 指标={}", accountCode, config.indicatorName);
            }
        }
        return allSuccess;
    }

    /**
     * 查询已存在的 ZYYWSYB 记录
     */
    private static DynamicObject findZyywsybExistingData(Long orgId, String during,
                                                         String accountCode, String indicatorName) {
        try {
            QFilter[] filters = {
                    new QFilter("dsa1_orgs", QCP.equals, orgId),
                    new QFilter("dsa1_during", QCP.equals, during),
                    new QFilter("dsa1_account", QCP.equals, accountCode),
                    new QFilter("dsa1_accountclassificati", QCP.equals, indicatorName)
            };
            return BusinessDataServiceHelper.loadSingle("dsa1_mergereport_zyyw", "id", filters);
        } catch (Exception e) {
            log.debug("查找已存在ZYYWSYB数据失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从行数据中获取指定位置的值（1-based索引转0-based）
     */
    private static Object getValueFromRow(List<Object> row, int index) {
        if (row == null || index < 1 || index >= row.size()) {
            return null;
        }
        return row.get(index);
    }

    /**
     * 指标配置内部类
     */
    private static class IndicatorConfig {
        String indicatorName;
        Integer bqsIndex;      // 本期数索引（1-based）
        Integer bnljsIndex;    // 本年数索引（1-based）
        Integer sntqsIndex;    // 上年数索引（1-based）

        IndicatorConfig(String indicatorName, Integer bqsIndex, Integer bnljsIndex, Integer sntqsIndex,
                        Integer bnljsGrowthIndex, Integer sntqsGrowthIndex) {
            this.indicatorName = indicatorName;
            this.bqsIndex = bqsIndex;
            this.bnljsIndex = bnljsIndex;
            this.sntqsIndex = sntqsIndex;
        }
    }





    /**
     * 处理费用明细表（CWFY01 财务费用明细表、GLFY01 管理费用明细表）
     * 数据格式：每个集合有5个元素
     * 索引0: 科目编码|名称
     * 索引1: 本期数
     * 索引2: 本年累计数
     * 索引3: 上年累计数
     * 索引4: 同比增减率（暂不存储）
     */
    private static boolean processExpenseDetailData(DataItem dataItem,
                                                    Map<String, DynamicObject> entityMap) {
        log.info("processExpenseDetailData begin and tempNum  = {}",dataItem.getTempNum());
        try {
            Map<String, String> pageDims = dataItem.getPageDims();
            if (pageDims == null) {
                log.warn("{}数据项缺少页面维度信息", dataItem.getTempNum());
                return false;
            }

            String year = pageDims.get("Year");
            String period = pageDims.get("Period");
            String entity = pageDims.get("Entity");
            String tempNum = dataItem.getTempNum();

            // 根据报表类型获取表名
            String tableName = TABLE_NAME_MAP.get(tempNum);
            if (StringUtils.isBlank(tableName)) {
                log.warn("未找到报表 {} 对应的表名", tempNum);
                return false;
            }

            if (Stream.of(year, period, entity).anyMatch(s -> s == null || s.trim().isEmpty())) {
                log.warn("{}缺少必要的维度信息: year={}, period={}, entity={}", tempNum, year, period, entity);
                return false;
            }

            // 提取所有数据（有些报表可能有多个 area）
            List<List<Object>> allData = DataExtractor.extractAllDatas(dataItem);


            if (CollectionUtils.isEmpty(allData)) {
                log.warn("{}数据项没有数据内容", tempNum);
                return false;
            }

            // 处理期间信息
            String yearNum = year.substring(2);  // FY2026 -> 2026
            String month = period.substring(3);  // M_M01 -> 01
            String during = yearNum + month;     // 202601

            // 获取组织信息
            DynamicObject org = entityMap.get(entity);
            if (org == null) {
                log.warn("{}未找到组织信息: {}", tempNum, entity);
                return false;
            }

            DynamicObject orgMapping = getOrgMapping(org.getString("number"));
            Long orgId = org.getLong("id");
            String orgName = orgMapping != null ? orgMapping.getString("name") : org.getString("name");
            String orgNumber = orgMapping != null ? orgMapping.getString("number") : org.getString("number");

            int successCount = 0;
            Date nowDate = new Date();

            for (List<Object> row : allData) {
                if (row == null || row.isEmpty()) {
                    continue;
                }

                // 解析科目信息（索引0：科目编码|名称）
                String accountInfo = Objects.toString(row.get(0), "");
                String[] accountArr = parseAccountInfo(accountInfo);
                String accountCode = accountArr[0];
                String accountName = accountArr[1];

                if (StringUtils.isBlank(accountCode)) {
                    continue;
                }

                // 保存费用明细记录
                boolean result = saveExpenseDetailRecord(orgId, orgName, orgNumber, during, yearNum, month,
                        pageDims, accountCode, accountName, row, nowDate, tempNum, tableName);

                if (result) {
                    successCount++;
                }
            }

            log.info("{}数据处理完成，成功保存 {} 条记录", tempNum, successCount);
            return successCount > 0;

        } catch (Exception e) {
            log.error("处理费用明细表数据失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 保存费用明细记录（通用方法，适用于 CWFY01、GLFY01 等）
     * @param row 数据行，包含5个元素
     *       索引0: 科目编码|名称
     *       索引1: 本期数
     *       索引2: 本年累计数
     *       索引3: 上年累计数
     *       索引4: 同比增减率（暂不存储）
     * @param tableName 目标表名
     */
    private static boolean saveExpenseDetailRecord(Long orgId, String orgName, String orgNumber,
                                                   String during, String yearNum, String month,
                                                   Map<String, String> pageDims, String accountCode,
                                                   String accountName, List<Object> row,
                                                   Date nowDate, String tempNum, String tableName) {

        DynamicObject financialData = BusinessDataServiceHelper.newDynamicObject(tableName);

        // 设置基础维度
        financialData.set("dsa1_orgs", orgId);
        financialData.set("dsa1_companyname", orgName);
        financialData.set("dsa1_orgnumber", orgNumber);
        financialData.set("dsa1_during", during);
        financialData.set("dsa1_years", yearNum);
        financialData.set("dsa1_months", month);
        financialData.set("dsa1_currency", pageDims.get("Currency"));
        financialData.set("dsa1_scene", pageDims.get("Scenario"));
        financialData.set("dsa1_modelNum", "XCZX001");
        financialData.set("dsa1_reportnumber", tempNum);
        financialData.set("dsa1_account", accountCode);
        financialData.set("dsa1_accountname", accountName);

        // 设置金额
        financialData.set("dsa1_bqs", parseBigDecimal(getValueFromRow(row, 1)));    // 本期数
        financialData.set("dsa1_bnljs", parseBigDecimal(getValueFromRow(row, 2)));  // 本年累计数
        financialData.set("dsa1_snljs", parseBigDecimal(getValueFromRow(row, 3)));  // 上年累计数
        // 索引4: 同比增减率（暂不存储，预留字段）

        financialData.set("dsa1_modifytime", nowDate);

        // 查询是否存在（组织 + 期间 + 科目）
        DynamicObject existingData = findExpenseDetailExistingData(orgId, during, accountCode, tableName);

        boolean saveResult;
        if (existingData == null) {
            financialData.set("dsa1_createtime", nowDate);
            OperationResult result = SaveServiceHelper.saveOperate(
                    tableName,
                    new DynamicObject[]{financialData},
                    OperateOption.create()
            );
            saveResult = result != null && result.isSuccess();
            if (saveResult) {
                log.debug("新增{}记录成功: 科目={}, 期间={}", tempNum, accountCode, during);
            }
        } else {
            financialData.set("id", existingData.getLong("id"));
            SaveServiceHelper.update(new DynamicObject[]{financialData});
            saveResult = true;
            log.debug("更新{}记录成功: 科目={}, 期间={}", tempNum, accountCode, during);
        }

        if (!saveResult) {
            log.warn("保存{}记录失败: 科目={}, 期间={}", tempNum, accountCode, during);
        }
        return saveResult;
    }

    /**
     * 查询已存在的费用明细记录（通用方法）
     * 去重维度：组织 + 期间 + 科目编码
     */
    private static DynamicObject findExpenseDetailExistingData(Long orgId, String during,
                                                               String accountCode, String tableName) {
        try {
            QFilter[] filters = {
                    new QFilter("dsa1_orgs", QCP.equals, orgId),
                    new QFilter("dsa1_during", QCP.equals, during),
                    new QFilter("dsa1_account", QCP.equals, accountCode)
            };
            return BusinessDataServiceHelper.loadSingle(tableName, "id", filters);
        } catch (Exception e) {
            log.debug("查找已存在数据失败: table={}, 科目={}", tableName, accountCode);
            return null;
        }
    }



    /**
     * 标准报表处理方法（适用于 CPL01、BS01、CF01、CQZ01）
     */
    private static boolean processStandardReport(DataItem dataItem, Map<String, Object> params,
                                                 Map<String, DynamicObject> entityMap ) {
       log.info("processStandardReport begin and tempNum = {}",dataItem.getTempNum());
        Map<String, String> pageDims = dataItem.getPageDims();
        String year = pageDims.get("Year");
        String period = pageDims.get("Period");
        String entity = pageDims.get("Entity");
        String tempNum = dataItem.getTempNum();
        String tableName = TABLE_NAME_MAP.get(tempNum);

        if (Stream.of(year, period, entity).anyMatch(s -> s == null || s.trim().isEmpty())) {
            log.warn("数据项缺少必要的维度信息: year={}, period={}, entity={}", year, period, entity);
            return false;
        }

        // 获取对应报表类型的字段映射
        Map<String, String[]> fieldMapping = REPORT_TYPE_FIELD_MAPS.get(tempNum);
        if (fieldMapping == null) {
            log.warn("跳过报表类型 {} 的数据：未配置字段映射", tempNum);
            return false;
        }

        // 提取数据
        List<List<Object>> allData = DataExtractor.extractAllDatas(dataItem);
        if (CollectionUtils.isEmpty(allData)) {
            log.warn("数据项没有数据内容: {}", tempNum);
            return false;
        }

        // 创建并填充数据对象
        DynamicObject financialData = createFinancialData(dataItem, params, year, period, entity, entityMap, tableName);

        // 设置金额数据
        setAmountData(financialData, allData, tableName, fieldMapping);

        // 判断是新增还是更新
        DynamicObject existingData = findExistingData(financialData, tableName);

        // 保存数据
        return saveOrUpdate(financialData, existingData, tableName);
    }

}