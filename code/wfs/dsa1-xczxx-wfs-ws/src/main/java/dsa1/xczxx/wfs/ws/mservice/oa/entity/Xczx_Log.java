package dsa1.xczxx.wfs.ws.mservice.oa.entity;

import java.sql.Timestamp;

@SuppressWarnings("LossyEncoding")
public class Xczx_Log {

    private String code;

    private String name;
    private String type;

    private String inParam;

    private String outParam;

    private String requestUrl;

    private Timestamp createTime;

    private Timestamp requestTime;

    private String status;
    private String responseMsg;
    private Timestamp responseTime;
    private String billno;
    private String text2;
    private String text3;
    private Timestamp date1;
    private Timestamp date2;
    private Timestamp date3;
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInParam() {
        return inParam;
    }

    public void setInParam(String inParam) {
        this.inParam = inParam;
    }

    public String getOutParam() {
        return outParam;
    }

    public void setOutParam(String outParam) {
        this.outParam = outParam;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Timestamp requestTime) {
        this.requestTime = requestTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public Timestamp getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Timestamp responseTime) {
        this.responseTime = responseTime;
    }


    public String getBillno() {
        return billno;
    }

    public void setBillno(String billno) {
        this.billno = billno;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public String getText3() {
        return text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

    public Timestamp getDate1() {
        return date1;
    }

    public void setDate1(Timestamp date1) {
        this.date1 = date1;
    }

    public Timestamp getDate2() {
        return date2;
    }

    public void setDate2(Timestamp date2) {
        this.date2 = date2;
    }

    public Timestamp getDate3() {
        return date3;
    }

    public void setDate3(Timestamp date3) {
        this.date3 = date3;
    }

    @Override
    public String toString() {
        return "Xczx_Log{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", inParam='" + inParam + '\'' +
                ", outParam='" + outParam + '\'' +
                ", requestUrl='" + requestUrl + '\'' +
                ", createTime=" + createTime +
                ", requestTime=" + requestTime +
                ", status='" + status + '\'' +
                ", responseMsg='" + responseMsg + '\'' +
                ", responseTime=" + responseTime +
                ", billno='" + billno + '\'' +
                ", text2='" + text2 + '\'' +
                ", text3='" + text3 + '\'' +
                ", date1=" + date1 +
                ", date2=" + date2 +
                ", date3=" + date3 +
                '}';
    }
}