package dsa1.xczxx.wfs.ws.mservice.oa.entity;

public class NotifyTodoRemoveContext {
    private String syscode;
    private String flowid;
    private String userid;
    private String receiver;

    public String getSyscode() {
        return syscode;
    }

    public void setSyscode(String syscode) {
        this.syscode = syscode;
    }

    public String getFlowid() {
        return flowid;
    }

    public void setFlowid(String flowid) {
        this.flowid = flowid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    @Override
    public String toString() {
        return "NotifyTodoRemoveContext{" +
                "syscode='" + syscode + '\'' +
                ", flowid='" + flowid + '\'' +
                ", userid='" + userid + '\'' +
                ", receiver='" + receiver + '\'' +
                '}';
    }
}
