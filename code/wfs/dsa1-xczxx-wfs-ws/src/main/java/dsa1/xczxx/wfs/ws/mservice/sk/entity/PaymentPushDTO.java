package dsa1.xczxx.wfs.ws.mservice.sk.entity;

import java.math.BigDecimal;

public class PaymentPushDTO {
    private String orderNo;                 // 单据编号（必填）
    private Integer payType = 1;            // 付款类型：1.单笔付款
    private String billsPayOrderNo;         // 应付单据编号
    private String paymentDate;             // 预计付款日期 yyyy-MM-dd（必填）
    private String relPaymentDate;          // 实际付款日期 yyyy-MM-dd（必填）
    private BigDecimal payAmount;               // 支付金额（必填）
    private String payOrgCode;              // 付款单位（必填）
    private String payAccountNo;            // 付款账号（必填）
    private String receiveOpenOrgCode;      // 收款方公司编号
    private String receiveOpenOrgName;      // 收款方公司名称
    private String receiveAccountNo;        // 收款账号（必填）
    private String receiveAccountName;      // 收款账号名称（必填）
    private String receiveAreaNameOfProvince; // 收款省
    private String receiveAreaNameOfCity;   // 收款市
    private String recDirectBank;           // 收款银行编号
    private String receiveBranchCode;       // 收款方开户行编号(Cnaps编号)
    private String receiveBranchName;       // 收款方开户行名称
    private Integer receiveNature = 1;      // 1.对公 2.对私
    private Integer payChannel;             // 1.直联支付 3.线下支付（必填）
    private String businessTypeCode;        // 业务类型code
    private String businessTypeName;        // 业务类型名称
    private String memo;                    // 摘要（必填）
    private String remark;                  // 备注

    // Getter/Setter 省略，请自行生成


    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public String getBillsPayOrderNo() {
        return billsPayOrderNo;
    }

    public void setBillsPayOrderNo(String billsPayOrderNo) {
        this.billsPayOrderNo = billsPayOrderNo;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getRelPaymentDate() {
        return relPaymentDate;
    }

    public void setRelPaymentDate(String relPaymentDate) {
        this.relPaymentDate = relPaymentDate;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public String getPayOrgCode() {
        return payOrgCode;
    }

    public void setPayOrgCode(String payOrgCode) {
        this.payOrgCode = payOrgCode;
    }

    public String getPayAccountNo() {
        return payAccountNo;
    }

    public void setPayAccountNo(String payAccountNo) {
        this.payAccountNo = payAccountNo;
    }

    public String getReceiveOpenOrgCode() {
        return receiveOpenOrgCode;
    }

    public void setReceiveOpenOrgCode(String receiveOpenOrgCode) {
        this.receiveOpenOrgCode = receiveOpenOrgCode;
    }

    public String getReceiveOpenOrgName() {
        return receiveOpenOrgName;
    }

    public void setReceiveOpenOrgName(String receiveOpenOrgName) {
        this.receiveOpenOrgName = receiveOpenOrgName;
    }

    public String getReceiveAccountNo() {
        return receiveAccountNo;
    }

    public void setReceiveAccountNo(String receiveAccountNo) {
        this.receiveAccountNo = receiveAccountNo;
    }

    public String getReceiveAccountName() {
        return receiveAccountName;
    }

    public void setReceiveAccountName(String receiveAccountName) {
        this.receiveAccountName = receiveAccountName;
    }

    public String getReceiveAreaNameOfProvince() {
        return receiveAreaNameOfProvince;
    }

    public void setReceiveAreaNameOfProvince(String receiveAreaNameOfProvince) {
        this.receiveAreaNameOfProvince = receiveAreaNameOfProvince;
    }

    public String getReceiveAreaNameOfCity() {
        return receiveAreaNameOfCity;
    }

    public void setReceiveAreaNameOfCity(String receiveAreaNameOfCity) {
        this.receiveAreaNameOfCity = receiveAreaNameOfCity;
    }

    public String getRecDirectBank() {
        return recDirectBank;
    }

    public void setRecDirectBank(String recDirectBank) {
        this.recDirectBank = recDirectBank;
    }

    public String getReceiveBranchCode() {
        return receiveBranchCode;
    }

    public void setReceiveBranchCode(String receiveBranchCode) {
        this.receiveBranchCode = receiveBranchCode;
    }

    public String getReceiveBranchName() {
        return receiveBranchName;
    }

    public void setReceiveBranchName(String receiveBranchName) {
        this.receiveBranchName = receiveBranchName;
    }

    public Integer getReceiveNature() {
        return receiveNature;
    }

    public void setReceiveNature(Integer receiveNature) {
        this.receiveNature = receiveNature;
    }

    public Integer getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(Integer payChannel) {
        this.payChannel = payChannel;
    }

    public String getBusinessTypeCode() {
        return businessTypeCode;
    }

    public void setBusinessTypeCode(String businessTypeCode) {
        this.businessTypeCode = businessTypeCode;
    }

    public String getBusinessTypeName() {
        return businessTypeName;
    }

    public void setBusinessTypeName(String businessTypeName) {
        this.businessTypeName = businessTypeName;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "PaymentPushDTO{" +
                "orderNo='" + orderNo + '\'' +
                ", payType=" + payType +
                ", billsPayOrderNo='" + billsPayOrderNo + '\'' +
                ", paymentDate='" + paymentDate + '\'' +
                ", relPaymentDate='" + relPaymentDate + '\'' +
                ", payAmount=" + payAmount +
                ", payOrgCode='" + payOrgCode + '\'' +
                ", payAccountNo='" + payAccountNo + '\'' +
                ", receiveOpenOrgCode='" + receiveOpenOrgCode + '\'' +
                ", receiveOpenOrgName='" + receiveOpenOrgName + '\'' +
                ", receiveAccountNo='" + receiveAccountNo + '\'' +
                ", receiveAccountName='" + receiveAccountName + '\'' +
                ", receiveAreaNameOfProvince='" + receiveAreaNameOfProvince + '\'' +
                ", receiveAreaNameOfCity='" + receiveAreaNameOfCity + '\'' +
                ", recDirectBank='" + recDirectBank + '\'' +
                ", receiveBranchCode='" + receiveBranchCode + '\'' +
                ", receiveBranchName='" + receiveBranchName + '\'' +
                ", receiveNature=" + receiveNature +
                ", payChannel=" + payChannel +
                ", businessTypeCode='" + businessTypeCode + '\'' +
                ", businessTypeName='" + businessTypeName + '\'' +
                ", memo='" + memo + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}