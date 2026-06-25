package dsa1.xczxx.wfs.ws.mservice.sk.entity;

public class ServiceRequestDTO {
    private String serviceCode = "SETT-SettPayment";
    private String data;    // 付款单列表JSON字符串

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ServiceRequestDTO{" +
                "serviceCode='" + serviceCode + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}