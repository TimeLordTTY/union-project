package com.timelordtty.docgen.controller;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            fileChooser.getExtensionFilters().add(new ExtensionFilter("JSON文件", "*.json"));
            
            // 显示打开对话框
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                // 读取JSON文件
                JsonNode rootNode = jsonMapper.readTree(selectedFile);
                
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
                        
                        // 处理列表字段
                        if (listItems.isArray()) {
                            // 首先创建列表字段定义
                            List<String> fieldList = new ArrayList<>();
                            for (JsonNode item : listItems) {
                                if (item.isObject()) {
                                    // 收集所有列表字段
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
                
                UIHelper.showInfo("成功", "数据已导入: " + selectedFile.getAbsolutePath());
                return true;
            }
        } catch (Exception e) {
            AppLogger.error("导入数据失败", e);
            UIHelper.showError("导入失败", "导入数据时出错: " + e.getMessage());
        }
        return false;
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
} 