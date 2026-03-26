package dsa1.xczxx.wfs.ws.mservice.oa.entity;

// 内部属性类
public class Attributes {
    private Integer subcompanyid;
    private String loginid;
    private String workcode;
    private String sex;        // 注意：这里是字符串"0"
    private Integer departmentid;
    private Integer systemlanguage;
    private Integer countryid;
    private Integer id;
    private Integer status;    // 这里是数字0

    // Getters and Setters
    public Integer getSubcompanyid() {
        return subcompanyid;
    }

    public void setSubcompanyid(Integer subcompanyid) {
        this.subcompanyid = subcompanyid;
    }

    public String getLoginid() {
        return loginid;
    }

    public void setLoginid(String loginid) {
        this.loginid = loginid;
    }

    public String getWorkcode() {
        return workcode;
    }

    public void setWorkcode(String workcode) {
        this.workcode = workcode;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getDepartmentid() {
        return departmentid;
    }

    public void setDepartmentid(Integer departmentid) {
        this.departmentid = departmentid;
    }

    public Integer getSystemlanguage() {
        return systemlanguage;
    }

    public void setSystemlanguage(Integer systemlanguage) {
        this.systemlanguage = systemlanguage;
    }

    public Integer getCountryid() {
        return countryid;
    }

    public void setCountryid(Integer countryid) {
        this.countryid = countryid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
        return "Attributes{" +
                "subcompanyid=" + subcompanyid +
                ", loginid='" + loginid + '\'' +
                ", workcode='" + workcode + '\'' +
                ", sex='" + sex + '\'' +
                ", departmentid=" + departmentid +
                ", systemlanguage=" + systemlanguage +
                ", countryid=" + countryid +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}