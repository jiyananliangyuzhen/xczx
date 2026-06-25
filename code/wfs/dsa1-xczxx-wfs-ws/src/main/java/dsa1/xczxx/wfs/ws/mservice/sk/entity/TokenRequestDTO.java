package dsa1.xczxx.wfs.ws.mservice.sk.entity;

public class TokenRequestDTO {
    private String appId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        return "TokenRequestDTO{" +
                "appId='" + appId + '\'' +
                '}';
    }
}