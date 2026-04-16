package dsa1.xczxx.wfs.ws.business.report;

import java.util.List;
import java.util.Map;

public class ApiResponse {
    private List<DataItem> data;
    private String errorCode;
    private String message;
    private Boolean status;

    // 构造函数、getter、setter
    public ApiResponse() {}

    public List<DataItem> getData() { return data; }
    public void setData(List<DataItem> data) { this.data = data; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}

class DataItem {
    private String tempNum;
    private String tempName;
    private Map<String, String> pageDims;
    private Map<String, String> viewDims;
    private List<AreaInfo> areaInfos;
    private Map<String, Object> definedParams;

    // 构造函数、getter、setter
    public DataItem() {}

    public String getTempNum() { return tempNum; }
    public void setTempNum(String tempNum) { this.tempNum = tempNum; }
    public String getTempName() { return tempName; }
    public void setTempName(String tempName) { this.tempName = tempName; }
    public Map<String, String> getPageDims() { return pageDims; }
    public void setPageDims(Map<String, String> pageDims) { this.pageDims = pageDims; }
    public Map<String, String> getViewDims() { return viewDims; }
    public void setViewDims(Map<String, String> viewDims) { this.viewDims = viewDims; }
    public List<AreaInfo> getAreaInfos() { return areaInfos; }
    public void setAreaInfos(List<AreaInfo> areaInfos) { this.areaInfos = areaInfos; }
    public Map<String, Object> getDefinedParams() { return definedParams; }
    public void setDefinedParams(Map<String, Object> definedParams) { this.definedParams = definedParams; }
}

class AreaInfo {
    private String area;
    private Map<String, Object> hideDims;
    private List<List<Object>> allDatas;
    private List<Object> originFloatAreas;
    private Object originFloatDataArea;
    private List<List<String>> dataColFields;
    private String areaType;
    private List<String> rowDims;
    private String originArea;
    private Object extModel;
    private List<String> colDims;
    private Map<String, Object> areaDefinedParams;

    // 构造函数、getter、setter
    public AreaInfo() {}

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public Map<String, Object> getHideDims() { return hideDims; }
    public void setHideDims(Map<String, Object> hideDims) { this.hideDims = hideDims; }
    public List<List<Object>> getAllDatas() { return allDatas; }
    public void setAllDatas(List<List<Object>> allDatas) { this.allDatas = allDatas; }
    public List<Object> getOriginFloatAreas() { return originFloatAreas; }
    public void setOriginFloatAreas(List<Object> originFloatAreas) { this.originFloatAreas = originFloatAreas; }
    public Object getOriginFloatDataArea() { return originFloatDataArea; }
    public void setOriginFloatDataArea(Object originFloatDataArea) { this.originFloatDataArea = originFloatDataArea; }
    public List<List<String>> getDataColFields() { return dataColFields; }
    public void setDataColFields(List<List<String>> dataColFields) { this.dataColFields = dataColFields; }
    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }
    public List<String> getRowDims() { return rowDims; }
    public void setRowDims(List<String> rowDims) { this.rowDims = rowDims; }
    public String getOriginArea() { return originArea; }
    public void setOriginArea(String originArea) { this.originArea = originArea; }
    public Object getExtModel() { return extModel; }
    public void setExtModel(Object extModel) { this.extModel = extModel; }
    public List<String> getColDims() { return colDims; }
    public void setColDims(List<String> colDims) { this.colDims = colDims; }
    public Map<String, Object> getAreaDefinedParams() { return areaDefinedParams; }
    public void setAreaDefinedParams(Map<String, Object> areaDefinedParams) { this.areaDefinedParams = areaDefinedParams; }
}