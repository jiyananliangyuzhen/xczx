package dsa1.xczxx.wfs.ws.mservice.report;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class DataExtractor {

    /**
     * 提取所有 allDatas 中的数据
     */
    public static List<List<Object>> extractAllDatas(DataItem dataItem) {
        List<List<Object>> result = new ArrayList<>();

        try {
            if (dataItem.getAreaInfos() != null) {
                for (AreaInfo areaInfo : dataItem.getAreaInfos()) {
                    if (areaInfo.getAllDatas() != null) {
                        for (List<Object> row : areaInfo.getAllDatas()) {
                            // 过滤空行
                            if (row != null && !row.isEmpty() && hasData(row)) {
                                result.add(row);
                            }
                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    public static List<List<Object>> extractAllDatas(String jsonResponse) {
        List<List<Object>> result = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ApiResponse apiResponse = objectMapper.readValue(jsonResponse, ApiResponse.class);

            if (apiResponse.getData() != null) {
                for (DataItem dataItem : apiResponse.getData()) {
                    if (dataItem.getAreaInfos() != null) {
                        for (AreaInfo areaInfo : dataItem.getAreaInfos()) {
                            if (areaInfo.getAllDatas() != null) {
                                for (List<Object> row : areaInfo.getAllDatas()) {
                                    // 过滤空行
                                    if (row != null && !row.isEmpty() && hasData(row)) {
                                        result.add(row);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 检查行是否有数据
     */
    private static boolean hasData(List<Object> row) {
        for (Object cell : row) {
            if (cell != null) {
                if (cell instanceof String) {
                    String str = (String) cell;
                    if (!str.trim().isEmpty()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 提取所有数据并格式化输出
     */
    public static List<Map<String, Object>> extractStructuredData(String jsonResponse) {
        List<Map<String, Object>> structuredData = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ApiResponse apiResponse = objectMapper.readValue(jsonResponse, ApiResponse.class);

            if (apiResponse.getData() != null) {
                for (DataItem dataItem : apiResponse.getData()) {
                    String templateName = dataItem.getTempName();

                    if (dataItem.getAreaInfos() != null) {
                        for (AreaInfo areaInfo : dataItem.getAreaInfos()) {
                            List<List<Object>> allDatas = areaInfo.getAllDatas();
                            List<List<String>> dataColFields = areaInfo.getDataColFields();

                            if (allDatas != null && dataColFields != null) {
                                // 提取列名
                                List<String> columnNames = new ArrayList<>();
                                for (List<String> colField : dataColFields) {
                                    if (colField != null && !colField.isEmpty()) {
                                        columnNames.add(colField.get(0));
                                    }
                                }

                                // 处理每一行数据
                                int rowIndex = 0;
                                for (List<Object> row : allDatas) {
                                    if (row != null && !row.isEmpty() && hasData(row)) {
                                        Map<String, Object> rowData = new LinkedHashMap<>();

                                        // 添加行索引
                                        rowData.put("rowIndex", rowIndex);
                                        rowData.put("template", templateName);

                                        // 添加列数据
                                        for (int i = 0; i < row.size() && i < columnNames.size(); i++) {
                                            rowData.put(columnNames.get(i), row.get(i));
                                        }

                                        // 添加原始行数据
                                        rowData.put("rawData", row);

                                        structuredData.add(rowData);
                                    }
                                    rowIndex++;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return structuredData;
    }

    /**
     * 提取数据并转换为字符串列表
     */
    public static List<List<String>> extractAllDatasAsString(String jsonResponse) {
        List<List<String>> result = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ApiResponse apiResponse = objectMapper.readValue(jsonResponse, ApiResponse.class);

            if (apiResponse.getData() != null) {
                for (DataItem dataItem : apiResponse.getData()) {
                    if (dataItem.getAreaInfos() != null) {
                        for (AreaInfo areaInfo : dataItem.getAreaInfos()) {
                            if (areaInfo.getAllDatas() != null) {
                                for (List<Object> row : areaInfo.getAllDatas()) {
                                    if (row != null && !row.isEmpty() && hasData(row)) {
                                        List<String> stringRow = new ArrayList<>();
                                        for (Object cell : row) {
                                            stringRow.add(cell != null ? cell.toString() : "");
                                        }
                                        result.add(stringRow);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 提取特定模板的数据
     */
    public static Map<String, List<List<Object>>> extractDataByTemplate(String jsonResponse) {
        Map<String, List<List<Object>>> result = new HashMap<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ApiResponse apiResponse = objectMapper.readValue(jsonResponse, ApiResponse.class);

            if (apiResponse.getData() != null) {
                for (DataItem dataItem : apiResponse.getData()) {
                    String templateKey = dataItem.getTempNum() + "_" + dataItem.getTempName();
                    List<List<Object>> templateData = new ArrayList<>();

                    if (dataItem.getAreaInfos() != null) {
                        for (AreaInfo areaInfo : dataItem.getAreaInfos()) {
                            if (areaInfo.getAllDatas() != null) {
                                for (List<Object> row : areaInfo.getAllDatas()) {
                                    if (row != null && !row.isEmpty() && hasData(row)) {
                                        templateData.add(row);
                                    }
                                }
                            }
                        }
                    }

                    result.put(templateKey, templateData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}