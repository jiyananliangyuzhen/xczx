package dsa1.xczxx.wfs.ws.mservice.oa.entity;

// 顶层响应类
public class OaUserResponse {
    private String msg;
    private String code;      // 注意：这里是字符串类型的"0"
    private Attributes attributes;
    private String id;
    private Integer status;    // 这里是数字类型的200

    // Getters and Setters
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OaUserResponse{" +
                "msg='" + msg + '\'' +
                ", code='" + code + '\'' +
                ", attributes=" + attributes +
                ", id='" + id + '\'' +
                ", status=" + status +
                '}';
    }
}