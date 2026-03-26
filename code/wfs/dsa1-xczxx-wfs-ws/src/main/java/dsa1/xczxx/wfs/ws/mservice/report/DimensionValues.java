package dsa1.xczxx.wfs.ws.mservice.report;

/**
 * 维度值封装类
 */
public  class DimensionValues {
    String entity;
    String budgetPeriod;
    String version;
    String changeType;
    String dataType;
    String metric;
    String auditTrail;
    String currency;
    String account;

    DimensionValues(String entity, String budgetPeriod, String version,
                    String changeType, String dataType, String metric,
                    String auditTrail, String currency, String account) {
        this.entity = entity;
        this.budgetPeriod = budgetPeriod;
        this.version = version;
        this.changeType = changeType;
        this.dataType = dataType;
        this.metric = metric;
        this.auditTrail = auditTrail;
        this.currency = currency;
        this.account = account;
    }
}