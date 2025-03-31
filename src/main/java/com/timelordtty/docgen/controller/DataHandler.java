package com.timelordtty.docgen.controller;

import java.io.File;
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
        try (XSSFWorkbook workbook = new XSSFWorkbook(excelFile)) {
            // 清空当前数据
            fieldManager.clearAll();
            
            // 处理普通字段（第一个sheet）
            XSSFSheet objectSheet = workbook.getSheet("普通字段");
            if (objectSheet != null) {
                for (int i = 1; i <= objectSheet.getLastRowNum(); i++) {
                    XSSFRow row = objectSheet.getRow(i);
                    if (row != null) {
                        String fieldName = row.getCell(0) != null ? row.getCell(0).toString() : "";
                        String fieldValue = row.getCell(1) != null ? row.getCell(1).toString() : "";
                        
                        if (!fieldName.trim().isEmpty()) {
                            fieldManager.addObjectDataField(fieldName, fieldValue);
                        }
                    }
                }
            }
            
            // 处理列表字段（其他sheet）
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();
                
                if (!"普通字段".equals(sheetName)) {
                    // 获取表头
                    XSSFRow headerRow = sheet.getRow(0);
                    if (headerRow != null) {
                        List<String> fields = new ArrayList<>();
                        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                            String fieldName = headerRow.getCell(i) != null ? headerRow.getCell(i).toString() : "";
                            if (!fieldName.trim().isEmpty()) {
                                fields.add(fieldName);
                            }
                        }
                        
                        // 创建列表字段
                        fieldManager.addListField(sheetName, fields);
                        
                        // 读取数据行
                        List<Map<String, String>> itemsList = new ArrayList<>();
                        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                            XSSFRow row = sheet.getRow(i);
                            if (row != null) {
                                Map<String, String> rowData = new HashMap<>();
                                for (int j = 0; j < fields.size(); j++) {
                                    String value = row.getCell(j) != null ? row.getCell(j).toString() : "";
                                    rowData.put(fields.get(j), value);
                                }
                                itemsList.add(rowData);
                            }
                        }
                        
                        // 添加列表数据
                        fieldManager.addListDataField(sheetName, itemsList);
                    }
                }
            }
            
            return true;
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
            
            // 清空当前数据
            fieldManager.clearAll();
            
            // 加载普通字段数据
            if (rootNode.has("objectFields")) {
                JsonNode objectFieldsNode = rootNode.get("objectFields");
                Iterator<Map.Entry<String, JsonNode>> fields = objectFieldsNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String fieldName = field.getKey();
                    String fieldValue = field.getValue().asText();
                    fieldManager.addObjectDataField(fieldName, fieldValue);
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
                    
                    if (listItems.isArray()) {
                        // 首先创建列表字段定义
                        List<String> fieldList = new ArrayList<>();
                        for (JsonNode item : listItems) {
                            if (item.isObject()) {
                                Iterator<String> fieldNames = item.fieldNames();
                                while (fieldNames.hasNext()) {
                                    String fieldName = fieldNames.next();
                                    if (!fieldList.contains(fieldName)) {
                                        fieldList.add(fieldName);
                                    }
                                }
                            }
                        }
                        
                        // 创建列表字段
                        fieldManager.addListField(listName, fieldList);
                        
                        // 添加列表数据
                        List<Map<String, String>> itemsList = new ArrayList<>();
                        for (JsonNode item : listItems) {
                            Map<String, String> itemMap = new HashMap<>();
                            Iterator<Map.Entry<String, JsonNode>> itemFields = item.fields();
                            while (itemFields.hasNext()) {
                                Map.Entry<String, JsonNode> itemField = itemFields.next();
                                String fieldName = itemField.getKey();
                                String fieldValue = itemField.getValue().asText();
                                itemMap.put(fieldName, fieldValue);
                            }
                            itemsList.add(itemMap);
                        }
                        fieldManager.addListDataField(listName, itemsList);
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
} 