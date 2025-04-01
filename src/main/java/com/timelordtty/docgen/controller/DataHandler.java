package com.timelordtty.docgen.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timelordtty.AppLogger;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * 数据处理器，负责导入导出数据
 */
public class DataHandler {
    private ObjectMapper jsonMapper;
    private String baseDir;
    
    /**
     * 构造函数
     * 
     * @param baseDir 基础目录
     */
    public DataHandler(String baseDir) {
        this.baseDir = baseDir;
        this.jsonMapper = new ObjectMapper();
    }
    
    /**
     * 导出字段定义
     * 
     * @param objectFieldItemsContainer 对象字段容器
     * @param listFieldItemsContainer 列表字段容器
     * @throws Exception 如果导出失败
     */
    public void exportFieldDefinitions(VBox objectFieldItemsContainer, VBox listFieldItemsContainer) throws Exception {
        // 创建文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出字段定义");
        
        // 设置默认输出目录
        File defaultDir = new File(baseDir);
        fileChooser.setInitialDirectory(defaultDir);
        
        // 设置默认文件名
        String defaultFileName = "字段定义_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
        fileChooser.setInitialFileName(defaultFileName);
        
        // 设置文件类型
        fileChooser.getExtensionFilters().add(new ExtensionFilter("JSON文件", "*.json"));
        
        // 显示保存对话框
        File outputFile = fileChooser.showSaveDialog(null);
        if (outputFile != null) {
            // 导出字段定义
            Map<String, Object> fieldDefinitions = new HashMap<>();
            
            // 添加普通字段
            Set<String> objectFields = new HashSet<>();
            for (Node node : objectFieldItemsContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox fieldItem = (HBox) node;
                    for (Node child : fieldItem.getChildren()) {
                        if (child instanceof Label) {
                            Label fieldNameLabel = (Label) child;
                            String fieldName = fieldNameLabel.getText();
                            if (fieldName != null && !fieldName.isEmpty()) {
                                objectFields.add(fieldName);
                            }
                            break; // 只处理第一个Label
                        }
                    }
                }
            }
            fieldDefinitions.put("objectFields", objectFields);
            
            // 添加列表字段
            Map<String, List<String>> listFields = new HashMap<>();
            for (Node node : listFieldItemsContainer.getChildren()) {
                if (node instanceof VBox) {
                    VBox listItem = (VBox) node;
                    String listName = listItem.getId();
                    List<String> fields = new ArrayList<>();
                    
                    // 获取列表字段
                    for (Node child : listItem.getChildren()) {
                        if (child instanceof javafx.scene.control.TableView) {
                            @SuppressWarnings("unchecked")
                            javafx.scene.control.TableView<String> tableView = (javafx.scene.control.TableView<String>) child;
                            fields.addAll(tableView.getItems());
                        }
                    }
                    
                    if (listName != null && !listName.isEmpty()) {
                        listFields.put(listName, fields);
                    }
                }
            }
            fieldDefinitions.put("listFields", listFields);
            
            // 将字段定义转换为JSON并保存
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, fieldDefinitions);
            
            UIHelper.showInfo("成功", "字段定义已导出: " + outputFile.getAbsolutePath());
        }
    }
    
    /**
     * 从Excel导入数据
     * 
     * @param fieldManager 字段管理器
     * @param excelFile Excel文件
     * @return 是否成功导入数据
     */
    private boolean importDataFromExcel(FieldManager fieldManager, File excelFile) {
        try {
            // 读取Excel文件
            try (FileInputStream fis = new FileInputStream(excelFile);
                XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
                
                // 清空当前数据
                Map<String, String> originalFieldDataMap = new HashMap<>(fieldManager.getFieldDataMap());
                Map<String, List<Map<String, String>>> originalListFieldDataMap = new HashMap<>();
                
                // 深度复制列表数据
                for (Map.Entry<String, List<Map<String, String>>> entry : fieldManager.getListFieldDataMap().entrySet()) {
                    List<Map<String, String>> copyList = new ArrayList<>();
                    for (Map<String, String> item : entry.getValue()) {
                        copyList.add(new HashMap<>(item));
                    }
                    originalListFieldDataMap.put(entry.getKey(), copyList);
                }
                
                // 清空数据，但保留字段定义
                fieldManager.clearData();
                
                // 加载普通字段数据（在第一个工作表）
                XSSFSheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    Cell keyCell = row.getCell(0);
                    Cell valueCell = row.getCell(1);
                    if (keyCell != null && valueCell != null) {
                        String fieldName = getStringCellValue(keyCell);
                        String fieldValue = getStringCellValue(valueCell);
                        
                        if (!fieldName.isEmpty()) {
                            // 更新数据填充区域，但不影响字段定义
                            fieldManager.updateFieldData(fieldName, fieldValue);
                        }
                    }
                }
                
                // 加载列表字段数据（其他工作表）
                for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
                    XSSFSheet listSheet = workbook.getSheetAt(i);
                    String sheetName = workbook.getSheetName(i);
                    
                    // 第一行是表头，包含字段名
                    Row headerRow = listSheet.getRow(0);
                    if (headerRow == null) {
                        continue;
                    }
                    
                    // 获取所有字段
                    List<String> fields = new ArrayList<>();
                    for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                        Cell cell = headerRow.getCell(j);
                        if (cell != null) {
                            fields.add(getStringCellValue(cell));
                        }
                    }
                    
                    if (fields.isEmpty()) {
                        continue;
                    }
                    
                    // 如果列表字段不存在，则记录一下但不会影响字段定义区域
                    boolean listExists = false;
                    for (String existingList : fieldManager.getListFieldNames()) {
                        if (existingList.equals(sheetName)) {
                            listExists = true;
                            break;
                        }
                    }
                    
                    // 如果列表不存在，只记录数据但不影响字段定义
                    if (!listExists) {
                        continue;
                    }
                    
                    // 获取数据行
                    List<Map<String, String>> itemsList = new ArrayList<>();
                    for (int rowNum = 1; rowNum <= listSheet.getLastRowNum(); rowNum++) {
                        Row dataRow = listSheet.getRow(rowNum);
                        if (dataRow == null) {
                            continue;
                        }
                        
                        Map<String, String> rowMap = new HashMap<>();
                        for (int j = 0; j < fields.size(); j++) {
                            Cell cell = dataRow.getCell(j);
                            String value = cell == null ? "" : getStringCellValue(cell);
                            rowMap.put(fields.get(j), value);
                        }
                        
                        if (!rowMap.isEmpty()) {
                            itemsList.add(rowMap);
                        }
                    }
                    
                    // 更新列表数据
                    fieldManager.updateListData(sheetName, itemsList);
                }
                
                UIHelper.showInfo("成功", "数据已导入: " + excelFile.getAbsolutePath());
                return true;
            }
        } catch (Exception e) {
            AppLogger.error("从Excel导入数据失败", e);
            UIHelper.showError("导入失败", "从Excel导入数据时出错: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 导入数据
     * 
     * @param fieldManager 字段管理器
     * @return 是否成功导入数据
     */
    public boolean importData(FieldManager fieldManager) {
        try {
            // 创建文件选择器
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("导入数据");
            
            // 设置默认目录
            File defaultDir = new File(baseDir);
            fileChooser.setInitialDirectory(defaultDir);
            
            // 设置文件类型
            fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Excel文件", "*.xlsx"),
                new ExtensionFilter("JSON文件", "*.json")
            );
            
            // 显示打开对话框
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                String fileName = selectedFile.getName().toLowerCase();
                if (fileName.endsWith(".xlsx")) {
                    return importDataFromExcel(fieldManager, selectedFile);
                } else if (fileName.endsWith(".json")) {
                    return importDataFromJson(fieldManager, selectedFile);
                }
            }
        } catch (Exception e) {
            AppLogger.error("导入数据失败", e);
            UIHelper.showError("导入失败", "导入数据时出错: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 从JSON导入数据
     * 
     * @param fieldManager 字段管理器
     * @param jsonFile JSON文件
     * @return 是否成功导入数据
     */
    private boolean importDataFromJson(FieldManager fieldManager, File jsonFile) {
        try {
            // 读取JSON文件
            JsonNode rootNode = jsonMapper.readTree(jsonFile);
            
            // 保存原始数据映射，以备需要回滚
            Map<String, String> originalFieldDataMap = new HashMap<>(fieldManager.getFieldDataMap());
            Map<String, List<Map<String, String>>> originalListFieldDataMap = new HashMap<>();
            
            // 深度复制列表数据
            for (Map.Entry<String, List<Map<String, String>>> entry : fieldManager.getListFieldDataMap().entrySet()) {
                List<Map<String, String>> copyList = new ArrayList<>();
                for (Map<String, String> item : entry.getValue()) {
                    copyList.add(new HashMap<>(item));
                }
                originalListFieldDataMap.put(entry.getKey(), copyList);
            }
            
            // 清空数据，但保留字段定义
            fieldManager.clearData();
            
            // 加载普通字段数据
            if (rootNode.has("objectFields")) {
                JsonNode objectFieldsNode = rootNode.get("objectFields");
                Iterator<Map.Entry<String, JsonNode>> fields = objectFieldsNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String fieldName = field.getKey();
                    String fieldValue = field.getValue().asText();
                    
                    // 更新数据填充区域，但不影响字段定义
                    fieldManager.updateFieldData(fieldName, fieldValue);
                }
            }
            
            // 加载列表字段数据
            if (rootNode.has("listFields")) {
                JsonNode listFieldsNode = rootNode.get("listFields");
                Iterator<Map.Entry<String, JsonNode>> lists = listFieldsNode.fields();
                while (lists.hasNext()) {
                    Map.Entry<String, JsonNode> list = lists.next();
                    String listName = list.getKey();
                    JsonNode listItems = list.getValue();
                    
                    // 检查列表是否已存在于字段定义中
                    boolean listExists = false;
                    for (String existingList : fieldManager.getListFieldNames()) {
                        if (existingList.equals(listName)) {
                            listExists = true;
                            break;
                        }
                    }
                    
                    // 如果列表不存在，跳过，不影响字段定义
                    if (!listExists) {
                        continue;
                    }
                    
                    if (listItems.isArray()) {
                        // 获取当前列表的字段
                        List<String> fieldList = fieldManager.getListFields(listName);
                        
                        // 添加列表数据
                        List<Map<String, String>> itemsList = new ArrayList<>();
                        for (JsonNode item : listItems) {
                            Map<String, String> itemMap = new HashMap<>();
                            
                            if (item.isObject()) {
                                // 为每个字段设置值（如果存在）
                                for (String fieldName : fieldList) {
                                    if (item.has(fieldName)) {
                                        itemMap.put(fieldName, item.get(fieldName).asText());
                                    } else {
                                        itemMap.put(fieldName, ""); // 如果字段不存在，设置为空值
                                    }
                                }
                                
                                // 添加额外的字段（存在于数据但不在定义中的字段）
                                Iterator<Map.Entry<String, JsonNode>> itemFields = item.fields();
                                while (itemFields.hasNext()) {
                                    Map.Entry<String, JsonNode> itemField = itemFields.next();
                                    String fieldName = itemField.getKey();
                                    if (!fieldList.contains(fieldName)) {
                                        itemMap.put(fieldName, itemField.getValue().asText());
                                    }
                                }
                                
                                itemsList.add(itemMap);
                            }
                        }
                        
                        // 更新列表数据
                        fieldManager.updateListData(listName, itemsList);
                    }
                }
            }
            
            UIHelper.showInfo("成功", "数据已导入: " + jsonFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            AppLogger.error("从JSON导入数据失败", e);
            UIHelper.showError("导入失败", "从JSON导入数据时出错: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 保存数据
     * 
     * @param fieldManager 字段管理器
     * @return 是否成功保存数据
     */
    public boolean saveData(FieldManager fieldManager) {
        try {
            Map<String, String> fieldDataMap = fieldManager.getFieldDataMap();
            Map<String, List<Map<String, String>>> listFieldDataMap = fieldManager.getListFieldDataMap();
            
            if (fieldDataMap.isEmpty() && listFieldDataMap.isEmpty()) {
                UIHelper.showError("错误", "没有数据可保存");
                return false;
            }
            
            // 创建文件选择器
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存数据");
            
            // 设置默认输出目录
            File defaultDir = new File(baseDir);
            fileChooser.setInitialDirectory(defaultDir);
            
            // 设置默认文件名
            String defaultFileName = "数据_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
            fileChooser.setInitialFileName(defaultFileName);
            
            // 设置文件类型
            fileChooser.getExtensionFilters().add(new ExtensionFilter("JSON文件", "*.json"));
            
            // 显示保存对话框
            File outputFile = fileChooser.showSaveDialog(null);
            if (outputFile != null) {
                // 创建数据对象
                Map<String, Object> data = new HashMap<>();
                data.put("objectFields", fieldDataMap);
                data.put("listFields", listFieldDataMap);
                
                // 保存为JSON
                jsonMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, data);
                
                UIHelper.showInfo("成功", "数据已保存: " + outputFile.getAbsolutePath());
                return true;
            }
        } catch (Exception e) {
            AppLogger.error("保存数据失败", e);
            UIHelper.showError("保存失败", "保存数据时出错: " + e.getMessage());
        }
        return false;
    }

    /**
     * 导出字段到Excel文件
     * 
     * @param objectFields 普通字段列表
     * @param listFields 列表字段映射
     * @param outputPath 输出路径
     * @throws Exception 如果导出失败
     */
    public void exportFieldsToExcel(List<String> objectFields, Map<String, List<String>> listFields, String outputPath) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // 创建普通字段工作表
            XSSFSheet objectSheet = workbook.createSheet("普通字段");
            
            // 创建表头
            XSSFRow headerRow = objectSheet.createRow(0);
            headerRow.createCell(0).setCellValue("字段名");
            headerRow.createCell(1).setCellValue("字段值");
            
            // 添加字段行
            int rowNum = 1;
            for (String field : objectFields) {
                XSSFRow row = objectSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(field);
                row.createCell(1).setCellValue(""); // 留空供用户填写
            }
            
            // 调整列宽
            objectSheet.autoSizeColumn(0);
            objectSheet.autoSizeColumn(1);
            
            // 为每个列表字段创建工作表
            for (Map.Entry<String, List<String>> entry : listFields.entrySet()) {
                String listName = entry.getKey();
                List<String> fields = entry.getValue();
                
                // 创建列表工作表
                XSSFSheet listSheet = workbook.createSheet(listName);
                
                // 创建表头
                XSSFRow listHeaderRow = listSheet.createRow(0);
                for (int i = 0; i < fields.size(); i++) {
                    listHeaderRow.createCell(i).setCellValue(fields.get(i));
                }
                
                // 添加一个空白行作为示例
                XSSFRow dataRow = listSheet.createRow(1);
                for (int i = 0; i < fields.size(); i++) {
                    dataRow.createCell(i).setCellValue(""); // 留空供用户填写
                }
                
                // 调整列宽
                for (int i = 0; i < fields.size(); i++) {
                    listSheet.autoSizeColumn(i);
                }
            }
            
            // 写入文件
            try (FileOutputStream outputStream = new FileOutputStream(outputPath)) {
                workbook.write(outputStream);
            }
        }
    }

    /**
     * 获取单元格的字符串值
     * 
     * @param cell 单元格
     * @return 字符串值
     */
    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 转换数字为字符串，避免科学计数法
                    double value = cell.getNumericCellValue();
                    if (value == (long) value) {
                        return String.format("%d", (long) value);
                    } else {
                        return String.format("%s", value);
                    }
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return "";
                    }
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }
} 