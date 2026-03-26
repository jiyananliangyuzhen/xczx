package dsa1.xczxx.wfs.ws.mservice.oa.entity;

public class NotifyTodoAppResult {
    private String  syscode;
    private String dateType;

    private String  operType;

    private String  operResult;
    private String  message;


    public String getSyscode() {
        return syscode;
    }

    public void setSyscode(String syscode) {
        this.syscode = syscode;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    public String getOperType() {
        return operType;
    }

    public void setOperType(String operType) {
        this.operType = operType;
    }

    public String getOperResult() {
        return operResult;
    }

    public void setOperResult(String operResult) {
        this.operResult = operResult;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "NotifyTodoAppResult{" +
                "syscode='" + syscode + '\'' +
                ", dateType='" + dateType + '\'' +
                ", operType='" + operType + '\'' +
                ", operResult='" + operResult + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
