package dsa1.xczxx.wfs.ws.mservice.sk.entity;

/**
 * 付款单推送结果（单条）
 */
public class PaymentSyncResultDTO {
    private String orderNo;     // 单据编号
    private String status;      // success 成功 / fail 失败
    private String message;     // 同步结果描述

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PaymentSyncResultDTO{" +
                "orderNo='" + orderNo + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}