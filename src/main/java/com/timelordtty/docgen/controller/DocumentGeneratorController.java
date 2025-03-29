package com.timelordtty.docgen.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.timelordtty.AppLogger;
import com.timelordtty.docgen.model.TemplateField;
import com.timelordtty.docgen.service.ExcelTemplateService;
import com.timelordtty.docgen.service.WordTemplateService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * 文档生成器控制器
 */
public class DocumentGeneratorController {

    // FXML控件 - 顶部区域
    @FXML private ComboBox<String> templateTypeComboBox;
    
    // 字段管理区域
    @FXML private TextField objectFieldInput;
    @FXML private TextField listFieldInput;
    
    // 字段列表区域
    @FXML private TableView<TemplateField> objectFieldsTable;
    @FXML private TableColumn<TemplateField, String> objectFieldNameColumn;
    @FXML private TableColumn<TemplateField, String> objectFieldPlaceholderColumn;
    @FXML private TableColumn<TemplateField, Void> objectFieldDeleteColumn;
    @FXML private VBox listFieldsContainer;
    
    // 模板编辑区域
    @FXML private TextArea wordEditor;
    @FXML private TableView<ObservableList<String>> excelEditor;
    
    // 数据填充区域
    @FXML private TableView<TemplateField> objectDataTable;
    @FXML private TableColumn<TemplateField, String> objectFieldNameDataColumn;
    @FXML private TableColumn<TemplateField, String> objectFieldValueColumn;
    @FXML private VBox listDataContainer;
    
    // 预览区域
    @FXML private TextArea wordPreviewArea;
    @FXML private TableView<ObservableList<String>> excelPreviewArea;
    
    // 列表数据示例表格
    @FXML private TableView<ObservableList<String>> listDataTableExample;
    
    // 私有字段
    private ObservableList<TemplateField> fields = FXCollections.observableArrayList();
    private ObservableList<TemplateField> objectFields = FXCollections.observableArrayList();
    private Map<String, TableView<Map<String, String>>> listFieldTables = new HashMap<>();
    private WordTemplateService wordTemplateService;
    private ExcelTemplateService excelTemplateService;
    private boolean isWordMode = true;
    private File currentTemplateFile;
    private String baseDir;
    private Map<String, String> fieldDataMap = new HashMap<>();
    private Map<String, List<Map<String, String>>> listFieldDataMap = new HashMap<>();
    
    /**
     * 初始化控制器
     */
    @FXML
    private void initialize() {
        AppLogger.info("初始化文档生成器控制器");
        
        // 初始化服务
        wordTemplateService = new WordTemplateService();
        excelTemplateService = new ExcelTemplateService();
        
        // 设置基础目录
        baseDir = System.getProperty("user.dir");
        
        // 初始化模板类型下拉框
        templateTypeComboBox.getSelectionModel().select(0); // 默认选择Word
        templateTypeComboBox.setOnAction(e -> handleTemplateTypeChange());
        
        // 初始化对象字段表格
        setupObjectFieldsTable();
        
        // 设置WordEditor
        wordEditor.setWrapText(true);
        
        // 初始化Excel编辑器
        setupExcelEditor();
        
        // 初始化数据填充表格
        setupObjectDataTable();
        
        // 创建必要的目录
        createDirectories();
        
        AppLogger.info("文档生成器控制器初始化完成");
    }
    
    /**
     * 创建必要的目录
     */
    private void createDirectories() {
        createDirectory(baseDir + "/templates/word");
        createDirectory(baseDir + "/templates/excel");
        createDirectory(baseDir + "/documents");
    }
    
    /**
     * 创建目录
     */
    private void createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                AppLogger.info("创建目录: " + path);
            } else {
                AppLogger.error("无法创建目录: " + path);
            }
        }
    }
    
    /**
     * 设置对象字段表格
     */
    private void setupObjectFieldsTable() {
        objectFieldsTable.setItems(objectFields);
        
        objectFieldNameColumn.setCellValueFactory(
            cellData -> new SimpleStringProperty(cellData.getValue().getName()));
            
        objectFieldPlaceholderColumn.setCellValueFactory(
            cellData -> new SimpleStringProperty(cellData.getValue().getPlaceholder()));
            
        objectFieldDeleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("X");
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                setGraphic(deleteButton);
                deleteButton.setOnAction(event -> {
                    TemplateField field = getTableView().getItems().get(getIndex());
                    deleteField(field);
                });
            }
        });
        
        // 双击事件，插入占位符到编辑区
        objectFieldsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TemplateField selectedField = objectFieldsTable.getSelectionModel().getSelectedItem();
                if (selectedField != null) {
                    insertPlaceholderToEditor(selectedField.getPlaceholder());
                }
            }
        });
    }
    
    /**
     * 设置对象数据表格
     */
    private void setupObjectDataTable() {
        objectDataTable.setItems(objectFields);
        
        objectFieldNameDataColumn.setCellValueFactory(
            cellData -> new SimpleStringProperty(cellData.getValue().getName()));
            
        objectFieldValueColumn.setCellValueFactory(
            cellData -> new SimpleStringProperty(fieldDataMap.getOrDefault(cellData.getValue().getName(), "")));
            
        objectFieldValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        objectFieldValueColumn.setOnEditCommit(event -> {
            String fieldName = event.getRowValue().getName();
            fieldDataMap.put(fieldName, event.getNewValue());
            updatePreview();
        });
        
        objectDataTable.setEditable(true);
    }
    
    /**
     * 设置Excel编辑器
     */
    private void setupExcelEditor() {
        // 默认添加三列
        for (int i = 0; i < 3; i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>("列" + (i + 1));
            column.setCellValueFactory(param -> {
                if (param.getValue().size() > colIndex) {
                    return new SimpleStringProperty(param.getValue().get(colIndex));
                }
                return new SimpleStringProperty("");
            });
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(event -> {
                ObservableList<String> row = event.getRowValue();
                // 确保行有足够的元素
                while (row.size() <= colIndex) {
                    row.add("");
                }
                row.set(colIndex, event.getNewValue());
            });
            excelEditor.getColumns().add(column);
        }
        
        // 添加默认一行数据
        ObservableList<String> row = FXCollections.observableArrayList("", "", "");
        excelEditor.getItems().add(row);
        
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addRowItem = new MenuItem("添加行");
        addRowItem.setOnAction(e -> addExcelRow());
        MenuItem addColumnItem = new MenuItem("添加列");
        addColumnItem.setOnAction(e -> addExcelColumn());
        MenuItem removeRowItem = new MenuItem("删除行");
        removeRowItem.setOnAction(e -> removeExcelRow());
        MenuItem removeColumnItem = new MenuItem("删除列");
        removeColumnItem.setOnAction(e -> removeExcelColumn());
        
        contextMenu.getItems().addAll(addRowItem, addColumnItem, removeRowItem, removeColumnItem);
        excelEditor.setContextMenu(contextMenu);
        
        // 设置可编辑
        excelEditor.setEditable(true);
    }
    
    /**
     * 处理模板类型变更
     */
    @FXML
    private void handleTemplateTypeChange() {
        String selectedType = templateTypeComboBox.getValue();
        isWordMode = "Word文档".equals(selectedType);
        
        // 更新编辑器可见性
        wordEditor.setVisible(isWordMode);
        excelEditor.setVisible(!isWordMode);
        
        // 更新预览区域可见性
        wordPreviewArea.setVisible(isWordMode);
        excelPreviewArea.setVisible(!isWordMode);
    }
    
    /**
     * 处理选择模板
     */
    @FXML
    private void handleSelectTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择模板文件");
        
        // 设置过滤器
        if (isWordMode) {
            fileChooser.getExtensionFilters().add(
                new ExtensionFilter("Word文档", "*.docx", "*.doc"));
        } else {
            fileChooser.getExtensionFilters().add(
                new ExtensionFilter("Excel表格", "*.xlsx", "*.xls"));
        }
        
        // 设置初始目录
        String templateDir = baseDir + "/templates/" + (isWordMode ? "word" : "excel");
        fileChooser.setInitialDirectory(new File(templateDir));
        
        // 显示文件选择器
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            loadTemplate(selectedFile);
        }
    }
    
    /**
     * 加载模板
     */
    private void loadTemplate(File file) {
        try {
            currentTemplateFile = file;
            
            if (isWordMode) {
                String content = wordTemplateService.readTemplate(file);
                wordEditor.setText(content);
                extractFields(content);
            } else {
                List<List<String>> data = excelTemplateService.readTemplate(file);
                loadExcelData(data);
                extractFieldsFromExcel(data);
            }
            
            updatePreview();
        } catch (Exception e) {
            showError("加载模板失败", "无法加载模板文件: " + e.getMessage());
            AppLogger.error("加载模板失败", e);
        }
    }
    
    /**
     * 处理字段导出
     */
    @FXML
    private void handleExportFields() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出字段");
        fileChooser.getExtensionFilters().add(
            new ExtensionFilter("Excel表格", "*.xlsx"));
        
        // 设置初始文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName("模板字段_" + timestamp + ".xlsx");
        
        // 显示保存对话框
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                // 导出字段到Excel
                excelTemplateService.exportFieldsToExcel(file, objectFields, listFieldDataMap);
                showInfo("导出成功", "字段已成功导出到: " + file.getName());
            } catch (Exception e) {
                showError("导出失败", "导出字段时发生错误: " + e.getMessage());
                AppLogger.error("导出字段失败", e);
            }
        }
    }
    
    /**
     * 将占位符插入到编辑器
     */
    private void insertPlaceholderToEditor(String placeholder) {
        if (isWordMode) {
            wordEditor.insertText(wordEditor.getCaretPosition(), placeholder);
        } else {
            TablePosition pos = excelEditor.getFocusModel().getFocusedCell();
            if (pos != null && pos.getRow() >= 0 && pos.getColumn() >= 0) {
                ObservableList<String> row = excelEditor.getItems().get(pos.getRow());
                while (row.size() <= pos.getColumn()) {
                    row.add("");
                }
                row.set(pos.getColumn(), placeholder);
                excelEditor.refresh();
            }
        }
    }
    
    /**
     * 添加Excel行
     */
    private void addExcelRow() {
        int columnCount = excelEditor.getColumns().size();
        ObservableList<String> row = FXCollections.observableArrayList();
        for (int i = 0; i < columnCount; i++) {
            row.add("");
        }
        excelEditor.getItems().add(row);
    }
    
    /**
     * 添加Excel列
     */
    private void addExcelColumn() {
        int columnIndex = excelEditor.getColumns().size();
        TableColumn<ObservableList<String>, String> column = new TableColumn<>("列" + (columnIndex + 1));
        final int colIndex = columnIndex;
        
        column.setCellValueFactory(param -> {
            if (param.getValue().size() > colIndex) {
                return new SimpleStringProperty(param.getValue().get(colIndex));
            }
            return new SimpleStringProperty("");
        });
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setOnEditCommit(event -> {
            ObservableList<String> row = event.getRowValue();
            // 确保行有足够的元素
            while (row.size() <= colIndex) {
                row.add("");
            }
            row.set(colIndex, event.getNewValue());
        });
        
        excelEditor.getColumns().add(column);
        
        // 更新所有行，确保有足够的单元格
        for (ObservableList<String> row : excelEditor.getItems()) {
            while (row.size() <= columnIndex) {
                row.add("");
            }
        }
    }
    
    /**
     * 删除Excel行
     */
    private void removeExcelRow() {
        int selectedIndex = excelEditor.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && excelEditor.getItems().size() > 1) {
            excelEditor.getItems().remove(selectedIndex);
        }
    }
    
    /**
     * 删除Excel列
     */
    private void removeExcelColumn() {
        if (excelEditor.getColumns().size() > 1) {
            int lastIndex = excelEditor.getColumns().size() - 1;
            excelEditor.getColumns().remove(lastIndex);
            
            // 从每一行删除最后一个单元格数据
            for (ObservableList<String> row : excelEditor.getItems()) {
                if (row.size() > lastIndex) {
                    row.remove(lastIndex);
                }
            }
        }
    }
    
    /**
     * 删除字段
     */
    private void deleteField(TemplateField field) {
        String fieldName = field.getName();
        
        // 从字段列表中移除
        objectFields.remove(field);
        
        // 从数据映射中移除
        fieldDataMap.remove(fieldName);
        
        // 如果是列表字段，还要移除相关的列表数据
        if (field.isList()) {
            listFieldDataMap.remove(fieldName);
            
            // 还需要移除列表相关的子字段
            List<TemplateField> fieldsToRemove = new ArrayList<>();
            for (TemplateField f : objectFields) {
                if (f.getName().startsWith(fieldName + ".")) {
                    fieldsToRemove.add(f);
                }
            }
            objectFields.removeAll(fieldsToRemove);
        }
        
        // 如果是列表项字段，还需要从列表项数据中移除
        if (fieldName.contains(".")) {
            String listName = fieldName.substring(0, fieldName.indexOf("."));
            String itemField = fieldName.substring(fieldName.indexOf(".") + 1);
            
            List<Map<String, String>> listItems = listFieldDataMap.get(listName);
            if (listItems != null) {
                for (Map<String, String> item : listItems) {
                    item.remove(itemField);
                }
            }
        }
        
        // 更新预览
        updatePreview();
        
        AppLogger.info("删除字段: " + fieldName);
    }
    
    /**
     * 更新预览
     */
    private void updatePreview() {
        if (isWordMode) {
            updateWordPreview();
        } else {
            updateExcelPreview();
        }
    }
    
    /**
     * 更新Word预览
     */
    private void updateWordPreview() {
        String templateText = wordEditor.getText();
        if (templateText.isEmpty()) return;
        
        String previewText = templateText;
        
        // 替换对象字段
        for (Map.Entry<String, String> entry : fieldDataMap.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            previewText = previewText.replace(placeholder, entry.getValue());
        }
        
        // 处理列表字段
        for (Map.Entry<String, List<Map<String, String>>> listEntry : listFieldDataMap.entrySet()) {
            String listFieldName = listEntry.getKey();
            List<Map<String, String>> listItems = listEntry.getValue();
            
            // 找到列表开始和结束标记
            String startMarker = "{{#" + listFieldName + "}}";
            String endMarker = "{{/" + listFieldName + "}}";
            
            int startIndex = previewText.indexOf(startMarker);
            int endIndex = previewText.indexOf(endMarker);
            
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                String listTemplate = previewText.substring(startIndex + startMarker.length(), endIndex);
                StringBuilder listContent = new StringBuilder();
                
                for (Map<String, String> item : listItems) {
                    String itemContent = listTemplate;
                    for (Map.Entry<String, String> itemField : item.entrySet()) {
                        String itemPlaceholder = "{{" + itemField.getKey() + "}}";
                        itemContent = itemContent.replace(itemPlaceholder, itemField.getValue());
                    }
                    listContent.append(itemContent);
                }
                
                // 替换整个列表部分
                previewText = previewText.substring(0, startIndex) + listContent.toString() + previewText.substring(endIndex + endMarker.length());
            }
        }
        
        // 更新预览区域
        wordPreviewArea.setText(previewText);
    }
    
    /**
     * 更新Excel预览
     */
    private void updateExcelPreview() {
        // 清空现有预览表
        excelPreviewArea.getItems().clear();
        excelPreviewArea.getColumns().clear();
        
        // 复制Excel表格结构
        for (TableColumn<ObservableList<String>, ?> column : excelEditor.getColumns()) {
            TableColumn<ObservableList<String>, String> newColumn = new TableColumn<>(column.getText());
            final int colIndex = excelPreviewArea.getColumns().size();
            
            newColumn.setCellValueFactory(param -> {
                if (param.getValue().size() > colIndex) {
                    return new SimpleStringProperty(param.getValue().get(colIndex));
                }
                return new SimpleStringProperty("");
            });
            
            excelPreviewArea.getColumns().add(newColumn);
        }
        
        // 复制并处理数据
        for (ObservableList<String> oldRow : excelEditor.getItems()) {
            ObservableList<String> newRow = FXCollections.observableArrayList();
            
            for (String cellText : oldRow) {
                String processedText = cellText;
                
                // 替换对象字段占位符
                for (Map.Entry<String, String> entry : fieldDataMap.entrySet()) {
                    String placeholder = "{{" + entry.getKey() + "}}";
                    if (processedText.contains(placeholder)) {
                        processedText = processedText.replace(placeholder, entry.getValue());
                    }
                }
                
                newRow.add(processedText);
            }
            
            excelPreviewArea.getItems().add(newRow);
        }
    }
    
    /**
     * 显示错误对话框
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示信息对话框
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 从文本中提取字段
     */
    private void extractFields(String content) {
        // 清空现有字段
        objectFields.clear();
        
        // 支持两种占位符格式：{{field}} 和 ${field}
        List<String> foundFields = new ArrayList<>();
        Map<String, Boolean> fieldIsListMap = new HashMap<>();
        
        // 查找Mustache格式占位符 {{field}}
        java.util.regex.Pattern mustachePattern = java.util.regex.Pattern.compile("\\{\\{([^{}]+)\\}\\}");
        java.util.regex.Matcher mustacheMatcher = mustachePattern.matcher(content);
        
        while (mustacheMatcher.find()) {
            String fieldName = mustacheMatcher.group(1);
            // 忽略#和/开头的字段，这些是列表的开始和结束标记
            if (!fieldName.startsWith("#") && !fieldName.startsWith("/")) {
                foundFields.add(fieldName);
                
                // 检查是否是列表字段
                boolean isList = content.contains("{{#" + fieldName + "}}") && content.contains("{{/" + fieldName + "}}");
                fieldIsListMap.put(fieldName, isList);
            }
        }
        
        // 查找${field}格式占位符
        java.util.regex.Pattern dollarPattern = java.util.regex.Pattern.compile("\\$\\{([^{}]+)\\}");
        java.util.regex.Matcher dollarMatcher = dollarPattern.matcher(content);
        
        while (dollarMatcher.find()) {
            String fieldName = dollarMatcher.group(1);
            
            // 处理数组格式: items[0].name -> items.0.name，便于后续处理
            if (fieldName.contains("[")) {
                fieldName = fieldName.replaceAll("\\[(\\d+)\\]", ".$1");
                // 对于数组的引用，认为父字段是列表
                String parentField = fieldName.substring(0, fieldName.indexOf("."));
                foundFields.add(parentField);
                fieldIsListMap.put(parentField, true);
            }
            
            foundFields.add(fieldName);
            // 默认非列表字段
            if (!fieldIsListMap.containsKey(fieldName)) {
                fieldIsListMap.put(fieldName, false);
            }
        }
        
        // 处理嵌套字段，确保父字段先添加
        Map<String, Set<String>> parentChildMap = new HashMap<>();
        
        for (String field : foundFields) {
            if (field.contains(".")) {
                String parentField = field.substring(0, field.indexOf("."));
                if (!parentChildMap.containsKey(parentField)) {
                    parentChildMap.put(parentField, new HashSet<>());
                }
                parentChildMap.get(parentField).add(field);
            }
        }
        
        // 先添加非嵌套字段
        for (String field : foundFields) {
            if (!field.contains(".")) {
                objectFields.add(new TemplateField(field, fieldIsListMap.getOrDefault(field, false)));
            }
        }
        
        // 再添加嵌套字段
        for (String field : foundFields) {
            if (field.contains(".")) {
                objectFields.add(new TemplateField(field, fieldIsListMap.getOrDefault(field, false)));
            }
        }
        
        // 去重
        List<TemplateField> uniqueFields = new ArrayList<>();
        Set<String> addedFields = new HashSet<>();
        
        for (TemplateField field : objectFields) {
            if (!addedFields.contains(field.getName())) {
                uniqueFields.add(field);
                addedFields.add(field.getName());
            }
        }
        
        objectFields.clear();
        objectFields.addAll(uniqueFields);
        
        // 在UI上更新字段列表
        Platform.runLater(() -> {
            objectFieldsTable.setItems(FXCollections.observableArrayList(objectFields));
            updateDataTables();
        });
        
        AppLogger.info("从模板中提取了 " + objectFields.size() + " 个字段");
    }
    
    /**
     * 从Excel中提取字段
     */
    private void extractFieldsFromExcel(List<List<String>> data) {
        // 清空现有字段
        objectFields.clear();
        
        // 假设第一行是标题行
        if (!data.isEmpty() && !data.get(0).isEmpty()) {
            List<String> headers = data.get(0);
            for (String header : headers) {
                if (header != null && !header.trim().isEmpty()) {
                    // 检查是否有{{}}格式
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{([^{}]+)\\}\\}");
                    java.util.regex.Matcher matcher = pattern.matcher(header);
                    
                    if (matcher.find()) {
                        String fieldName = matcher.group(1);
                        
                        // 检查是否已存在
                        boolean exists = false;
                        for (TemplateField field : objectFields) {
                            if (field.getName().equals(fieldName)) {
                                exists = true;
                                break;
                            }
                        }
                        
                        if (!exists) {
                            // 检查是否是列表字段
                            boolean isList = false;
                            for (List<String> row : data) {
                                for (String cell : row) {
                                    if (cell.contains("{{#" + fieldName + "}}") && 
                                        cell.contains("{{/" + fieldName + "}}")) {
                                        isList = true;
                                        break;
                                    }
                                }
                                if (isList) break;
                            }
                            
                            objectFields.add(new TemplateField(fieldName, isList));
                        }
                    } else {
                        // 没有{{}}格式，将整个标题作为字段名
                        // 检查是否已存在
                        boolean exists = false;
                        for (TemplateField field : objectFields) {
                            if (field.getName().equals(header)) {
                                exists = true;
                                break;
                            }
                        }
                        
                        if (!exists) {
                            objectFields.add(new TemplateField(header, false));
                        }
                    }
                }
            }
        }
        
        AppLogger.info("从Excel模板中提取了 " + objectFields.size() + " 个字段");
    }
    
    /**
     * 将Excel数据加载到表格视图
     */
    private void loadExcelData(List<List<String>> data) {
        excelEditor.getItems().clear();
        excelEditor.getColumns().clear();
        
        if (data.isEmpty()) return;
        
        // 确定列数
        int columnCount = 0;
        for (List<String> row : data) {
            columnCount = Math.max(columnCount, row.size());
        }
        
        // 创建列
        for (int i = 0; i < columnCount; i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>("列" + (i + 1));
            
            column.setCellValueFactory(param -> {
                if (param.getValue().size() > colIndex) {
                    return new SimpleStringProperty(param.getValue().get(colIndex));
                }
                return new SimpleStringProperty("");
            });
            
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(event -> {
                ObservableList<String> row = event.getRowValue();
                // 确保行有足够的元素
                while (row.size() <= colIndex) {
                    row.add("");
                }
                row.set(colIndex, event.getNewValue());
            });
            
            excelEditor.getColumns().add(column);
        }
        
        // 添加数据
        for (List<String> rowData : data) {
            ObservableList<String> row = FXCollections.observableArrayList(rowData);
            // 确保行有足够的元素
            while (row.size() < columnCount) {
                row.add("");
            }
            excelEditor.getItems().add(row);
        }
    }
    
    /**
     * 添加对象字段
     */
    @FXML
    private void handleAddObjectField() {
        String fieldName = objectFieldInput.getText().trim();
        if (!fieldName.isEmpty()) {
            // 检查字段是否已存在
            boolean exists = false;
            for (TemplateField field : objectFields) {
                if (field.getName().equals(fieldName)) {
                    exists = true;
                    break;
                }
            }
            
            if (exists) {
                showError("添加失败", "字段 '" + fieldName + "' 已存在");
                return;
            }
            
            objectFields.add(new TemplateField(fieldName, false));
            objectFieldInput.clear();
            
            // 初始化字段数据映射
            fieldDataMap.put(fieldName, "");
            
            AppLogger.info("添加对象字段: " + fieldName);
            
            // 更新预览
            updatePreview();
        }
    }
    
    /**
     * 添加列表字段
     */
    @FXML
    private void handleAddListField() {
        String fieldName = listFieldInput.getText().trim();
        
        if (fieldName.isEmpty()) {
            showError("添加失败", "请输入列表字段名称");
            return;
        }
        
        // 检查字段是否已存在
        boolean exists = false;
        for (TemplateField field : objectFields) {
            if (field.getName().equals(fieldName)) {
                exists = true;
                break;
            }
        }
        
        if (exists) {
            showError("添加失败", "字段 '" + fieldName + "' 已存在");
            return;
        }
        
        // 添加列表字段
        objectFields.add(new TemplateField(fieldName, true));
        
        // 初始化列表数据
        listFieldDataMap.put(fieldName, new ArrayList<>());
        
        listFieldInput.clear();
        
        // 更新列表字段UI
        updateListFieldsUI();
        
        AppLogger.info("添加列表字段: " + fieldName);
        
        // 更新预览
        updatePreview();
    }
    
    /**
     * 更新列表字段UI
     */
    private void updateListFieldsUI() {
        // 清空现有列表字段UI
        listFieldsContainer.getChildren().clear();
        
        // 添加列表字段到UI
        for (TemplateField field : objectFields) {
        if (field.isList()) {
                TitledPane listPane = createListFieldPane(field);
                listFieldsContainer.getChildren().add(listPane);
            }
        }
    }
    
    /**
     * 创建列表字段面板
     */
    private TitledPane createListFieldPane(TemplateField listField) {
        TitledPane listPane = new TitledPane();
        listPane.setText(listField.getName());
        listPane.setExpanded(false);
        
        VBox content = new VBox(5);
        
        // 添加列表项输入控件
        HBox inputBox = new HBox(5);
        TextField listItemInput = new TextField();
        listItemInput.setPromptText("输入列表项字段");
        listItemInput.setPrefWidth(200);
        
        Button addButton = new Button("添加");
        addButton.getStyleClass().add("action-button");
        addButton.setOnAction(e -> {
            String itemName = listItemInput.getText().trim();
            if (!itemName.isEmpty()) {
                String fullName = listField.getName() + "." + itemName;
                
                // 检查是否已存在
                boolean exists = false;
                for (TemplateField field : objectFields) {
                    if (field.getName().equals(fullName)) {
                        exists = true;
                        break;
                    }
                }
                
                if (!exists) {
                    objectFields.add(new TemplateField(fullName, false));
                    listItemInput.clear();
                    
                    // 更新列表项UI
                    updateListItemsUI(listField, content);
                    
                    AppLogger.info("添加列表项字段: " + fullName);
                    
                    // 更新预览
                    updatePreview();
            } else {
                    showError("添加失败", "字段 '" + fullName + "' 已存在");
                }
            }
        });
        
        inputBox.getChildren().addAll(listItemInput, addButton);
        content.getChildren().add(inputBox);
        
        // 添加列表项
        updateListItemsUI(listField, content);
        
        listPane.setContent(content);
        return listPane;
    }
    
    /**
     * 更新列表项UI
     */
    private void updateListItemsUI(TemplateField listField, VBox container) {
        // 移除现有列表项UI（保留输入框）
        if (container.getChildren().size() > 1) {
            container.getChildren().remove(1, container.getChildren().size());
        }
        
        // 添加列表项字段
        for (TemplateField field : objectFields) {
            if (field.getName().startsWith(listField.getName() + ".")) {
                String itemName = field.getName().substring(field.getName().indexOf(".") + 1);
                
                HBox itemBox = new HBox(5);
                Label nameLabel = new Label(itemName);
                nameLabel.setPrefWidth(200);
                
                Button deleteButton = new Button("X");
                deleteButton.setOnAction(e -> {
                    deleteField(field);
                    updateListItemsUI(listField, container);
                });
                
                itemBox.getChildren().addAll(nameLabel, deleteButton);
                container.getChildren().add(itemBox);
            }
        }
    }
    
    /**
     * 保存模板
     */
    @FXML
    private void handleSaveTemplate() {
        if (currentTemplateFile == null) {
            // 需要选择保存位置
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存模板");
            
            // 根据当前选择的模板类型设置默认目录和文件名
            File defaultDir;
            String defaultFileName = "模板_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            
            if (isWordMode) {
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Word文档", "*.docx"));
                fileChooser.setInitialFileName(defaultFileName + ".docx");
                defaultDir = new File(baseDir, "templates/word");
            } else {
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel表格", "*.xlsx"));
                fileChooser.setInitialFileName(defaultFileName + ".xlsx");
                defaultDir = new File(baseDir, "templates/excel");
            }
            
            if (!defaultDir.exists()) {
                defaultDir.mkdirs();
            }
            fileChooser.setInitialDirectory(defaultDir);
            
            currentTemplateFile = fileChooser.showSaveDialog(wordEditor.getScene().getWindow());
        }
        
        if (currentTemplateFile != null) {
            try {
                if (isWordMode) {
                    // 保存Word模板
                    wordTemplateService.saveDocxTemplate(currentTemplateFile.getPath(), wordEditor.getText());
                } else {
                    // 保存Excel模板
                    List<List<String>> data = new ArrayList<>();
                    for (ObservableList<String> row : excelEditor.getItems()) {
                        data.add(new ArrayList<>(row));
                    }
                    excelTemplateService.saveExcelTemplate(currentTemplateFile.getPath(), data);
                }
                
                AppLogger.info("成功保存模板: " + currentTemplateFile.getName());
                showInfo("保存成功", "模板已保存到: " + currentTemplateFile.getPath());
            } catch (Exception e) {
                AppLogger.error("保存模板失败: " + e.getMessage(), e);
                showError("保存失败", "无法保存模板: " + e.getMessage());
            }
        }
    }
    
    /**
     * 生成模板
     */
    @FXML
    private void handleGenerateTemplate() {
        // 确定默认文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String defaultFileName;
        
        if (currentTemplateFile != null) {
            String templateName = currentTemplateFile.getName();
            // 去掉扩展名
            if (templateName.lastIndexOf(".") > 0) {
                templateName = templateName.substring(0, templateName.lastIndexOf("."));
            }
            defaultFileName = templateName + "_" + timestamp;
        } else {
            defaultFileName = "模板_" + timestamp;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("生成新模板");
        
        // 根据当前选择的模板类型设置默认目录和文件名
        File defaultDir;
        
        if (isWordMode) {
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Word文档", "*.docx"));
            fileChooser.setInitialFileName(defaultFileName + ".docx");
            defaultDir = new File(baseDir, "templates/word");
        } else {
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel表格", "*.xlsx"));
            fileChooser.setInitialFileName(defaultFileName + ".xlsx");
            defaultDir = new File(baseDir, "templates/excel");
        }
        
        if (!defaultDir.exists()) {
            defaultDir.mkdirs();
        }
        fileChooser.setInitialDirectory(defaultDir);
        
        File file = fileChooser.showSaveDialog(wordEditor.getScene().getWindow());
        if (file != null) {
            try {
                if (isWordMode) {
                    // 生成Word模板
                    generateCustomWordTemplate(file.getPath());
                    // 更新编辑器内容
                    String content = wordTemplateService.readDocxContent(file.getPath());
                    wordEditor.setText(content);
                } else {
                    // 生成Excel模板
                    excelTemplateService.generateExcelTemplate(file.getPath(), objectFields);
                    // 更新Excel编辑器
                    List<List<String>> data = excelTemplateService.readExcelContent(file.getPath());
                    loadExcelData(data);
                }
                
                // 更新当前模板文件
                currentTemplateFile = file;
                
                // 更新预览
                updatePreview();
                
                AppLogger.info("成功生成模板: " + file.getName());
                showInfo("生成成功", "新模板已生成: " + file.getPath());
            } catch (Exception e) {
                AppLogger.error("生成模板失败: " + e.getMessage(), e);
                showError("生成失败", "无法生成模板: " + e.getMessage());
            }
        }
    }
    
    /**
     * 生成自定义Word模板（按照HTML示例）
     */
    private void generateCustomWordTemplate(String filePath) throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {
            // 创建标题
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("合同示例模板");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            
            // 添加客户信息段落
            XWPFParagraph clientParagraph = document.createParagraph();
            XWPFRun clientRun = clientParagraph.createRun();
            clientRun.setText("甲方（客户）：");
            XWPFRun clientNameRun = clientParagraph.createRun();
            clientNameRun.setText("{{客户名称}}");
            
            // 添加合同编号段落
            XWPFParagraph contractParagraph = document.createParagraph();
            XWPFRun contractRun = contractParagraph.createRun();
            contractRun.setText("合同编号：");
            XWPFRun contractNumberRun = contractParagraph.createRun();
            contractNumberRun.setText("{{合同编号}}");
            
            // 添加签约日期段落
            XWPFParagraph dateParagraph = document.createParagraph();
            XWPFRun dateRun = dateParagraph.createRun();
            dateRun.setText("签约日期：");
            XWPFRun signDateRun = dateParagraph.createRun();
            signDateRun.setText("{{签约日期}}");
            
            // 添加项目名称段落
            XWPFParagraph projectParagraph = document.createParagraph();
            XWPFRun projectRun = projectParagraph.createRun();
            projectRun.setText("项目名称：");
            XWPFRun projectNameRun = projectParagraph.createRun();
            projectNameRun.setText("{{项目名称}}");
            
            // 项目内容标题
            XWPFParagraph contentTitleParagraph = document.createParagraph();
            contentTitleParagraph.setStyle("Heading1");
            XWPFRun contentTitleRun = contentTitleParagraph.createRun();
            contentTitleRun.setText("一、项目内容");
            contentTitleRun.setBold(true);
            
            // 项目内容描述
            XWPFParagraph contentParagraph = document.createParagraph();
            XWPFRun contentRun = contentParagraph.createRun();
            contentRun.setText("根据甲方需求，乙方提供以下服务...");
            
            // 联系方式标题
            XWPFParagraph contactTitleParagraph = document.createParagraph();
            contactTitleParagraph.setStyle("Heading1");
            XWPFRun contactTitleRun = contactTitleParagraph.createRun();
            contactTitleRun.setText("二、联系方式");
            contactTitleRun.setBold(true);
            
            // 联系人
            XWPFParagraph contactParagraph = document.createParagraph();
            XWPFRun contactLabelRun = contactParagraph.createRun();
            contactLabelRun.setText("联系人：");
            XWPFRun contactNameRun = contactParagraph.createRun();
            contactNameRun.setText("{{联系人}}");
            
            // 联系电话
            XWPFParagraph phoneParagraph = document.createParagraph();
            XWPFRun phoneLabelRun = phoneParagraph.createRun();
            phoneLabelRun.setText("联系电话：");
            XWPFRun phoneRun = phoneParagraph.createRun();
            phoneRun.setText("{{电话}}");
            
            // 合同金额标题
            XWPFParagraph amountTitleParagraph = document.createParagraph();
            amountTitleParagraph.setStyle("Heading1");
            XWPFRun amountTitleRun = amountTitleParagraph.createRun();
            amountTitleRun.setText("三、合同金额");
            amountTitleRun.setBold(true);
            
            // 合同金额
            XWPFParagraph amountParagraph = document.createParagraph();
            XWPFRun amountLabelRun = amountParagraph.createRun();
            amountLabelRun.setText("本合同金额为：");
            XWPFRun amountRun = amountParagraph.createRun();
            amountRun.setText("{{合同金额}}");
            XWPFRun amountUnitRun = amountParagraph.createRun();
            amountUnitRun.setText("元");
            
            // 其他条款标题
            XWPFParagraph otherTitleParagraph = document.createParagraph();
            otherTitleParagraph.setStyle("Heading1");
            XWPFRun otherTitleRun = otherTitleParagraph.createRun();
            otherTitleRun.setText("四、其他条款");
            otherTitleRun.setBold(true);
            
            // 其他条款内容
            XWPFParagraph otherParagraph = document.createParagraph();
            XWPFRun otherRun = otherParagraph.createRun();
            otherRun.setText("本合同未尽事宜，由双方协商解决。");
            
            // 自动添加字段到objectFields
            if (objectFields.isEmpty()) {
                objectFields.add(new TemplateField("客户名称", false));
                objectFields.add(new TemplateField("合同编号", false));
                objectFields.add(new TemplateField("签约日期", false));
                objectFields.add(new TemplateField("项目名称", false));
                objectFields.add(new TemplateField("联系人", false));
                objectFields.add(new TemplateField("电话", false));
                objectFields.add(new TemplateField("合同金额", false));
                
                // 初始化字段数据
                fieldDataMap.put("客户名称", "南京科技有限公司");
                fieldDataMap.put("合同编号", "HT-2025-001");
                fieldDataMap.put("签约日期", "2025年4月1日");
                fieldDataMap.put("项目名称", "智能管理系统开发项目");
                fieldDataMap.put("联系人", "张经理");
                fieldDataMap.put("电话", "13912345678");
                fieldDataMap.put("合同金额", "120,000.00");
            }
            
            // 保存文档
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                document.write(out);
            }
        } catch (Exception e) {
            AppLogger.error("生成Word模板失败: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 保存文档
     */
    @FXML
    private void handleSaveDocument() {
        if (currentTemplateFile == null) {
            showError("保存失败", "请先创建或上传模板");
            return;
        }
        
        // 确定默认文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String templateName = currentTemplateFile.getName();
        // 去掉扩展名
        if (templateName.lastIndexOf(".") > 0) {
            templateName = templateName.substring(0, templateName.lastIndexOf("."));
        }
        String defaultFileName = templateName + "_文档_" + timestamp;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存文档");
        
        // 设置默认目录和文件名
        File documentsDir = new File(baseDir, "documents");
        if (!documentsDir.exists()) {
            documentsDir.mkdirs();
        }
        fileChooser.setInitialDirectory(documentsDir);
        
        if (isWordMode) {
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Word文档", "*.docx"));
            fileChooser.setInitialFileName(defaultFileName + ".docx");
        } else {
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel表格", "*.xlsx"));
            fileChooser.setInitialFileName(defaultFileName + ".xlsx");
        }
        
        File file = fileChooser.showSaveDialog(wordEditor.getScene().getWindow());
        if (file != null) {
            try {
                if (isWordMode) {
                    // 保存预览中的内容作为文档
                    wordTemplateService.saveDocxDocument(file.getPath(), wordPreviewArea.getText());
                } else {
                    // 保存Excel文档
                    List<List<String>> data = new ArrayList<>();
                    for (ObservableList<String> row : excelPreviewArea.getItems()) {
                        List<String> rowData = new ArrayList<>(row);
                        data.add(rowData);
                    }
                    excelTemplateService.saveExcelDocument(file.getPath(), data);
                }
                
                AppLogger.info("成功保存文档: " + file.getName());
                showInfo("保存成功", "文档已保存到: " + file.getPath());
            } catch (Exception e) {
                AppLogger.error("保存文档失败: " + e.getMessage(), e);
                showError("保存失败", "无法保存文档: " + e.getMessage());
            }
        }
    }
    
    /**
     * 处理导入Excel数据
     */
    @FXML
    private void handleImportDataExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择数据文件");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel表格", "*.xlsx"));
        
        File file = fileChooser.showOpenDialog(wordEditor.getScene().getWindow());
        if (file != null) {
            try {
                // 读取Excel数据
                List<List<String>> data = excelTemplateService.readExcelContent(file.getPath());
                
                if (data.size() < 2) {
                    showError("导入失败", "Excel数据格式不正确，至少需要包含标题行和一行数据");
                    return;
                }
                
                // 第一行应该是标题行
                List<String> headers = data.get(0);
                List<String> firstDataRow = data.get(1);
                
                // 清空现有数据
                fieldDataMap.clear();
                listFieldDataMap.clear();
                
                // 处理对象字段数据
                for (int i = 0; i < headers.size(); i++) {
                    String header = headers.get(i);
                    if (i < firstDataRow.size()) {
                        // 处理对象字段
                        if (!header.contains(".")) {
                            // 不是列表项字段
                            fieldDataMap.put(header, firstDataRow.get(i));
                        } else {
                            // 处理列表项字段
                            String listName = header.substring(0, header.indexOf("."));
                            String itemName = header.substring(header.indexOf(".") + 1);
                            
                            // 确保列表存在
                            if (!listFieldDataMap.containsKey(listName)) {
                                listFieldDataMap.put(listName, new ArrayList<>());
                            }
                            
                            // 处理所有数据行
                            for (int rowIndex = 1; rowIndex < data.size(); rowIndex++) {
                                List<String> rowData = data.get(rowIndex);
                                if (rowIndex > listFieldDataMap.get(listName).size()) {
                                    // 需要添加新的列表项
                                    Map<String, String> newItem = new HashMap<>();
                                    listFieldDataMap.get(listName).add(newItem);
                                }
                                
                                if (i < rowData.size() && !rowData.get(i).isEmpty()) {
                                    // 添加列表项字段值
                                    int listItemIndex = rowIndex - 1;
                                    if (listItemIndex < listFieldDataMap.get(listName).size()) {
                                        listFieldDataMap.get(listName).get(listItemIndex).put(itemName, rowData.get(i));
                                    }
                                }
                            }
                        }
                    }
                }
            
            // 更新预览
            updatePreview();
                
                AppLogger.info("成功导入Excel数据: " + file.getName());
                showInfo("导入成功", "Excel数据已成功导入");
            } catch (Exception e) {
                AppLogger.error("导入Excel数据失败: " + e.getMessage(), e);
                showError("导入失败", "无法读取所选文件: " + e.getMessage());
            }
        }
    }
    
    /**
     * 更新数据表格
     */
    private void updateDataTables() {
        // 更新对象字段表格
        objectFieldsTable.setItems(FXCollections.observableArrayList(objectFields));
        
        // 更新数据填充表格
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
        for (TemplateField field : objectFields) {
            if (!field.isList() && !field.getName().contains(".")) {
                // 只添加非列表的顶级字段
                fieldDataMap.putIfAbsent(field.getName(), "");
                Map<String, String> row = new HashMap<>();
                row.put("name", field.getName());
                row.put("value", fieldDataMap.get(field.getName()));
                data.add(row);
            }
        }
        
        // 更新列表数据填充区域
        updateListDataTables();
    }
    
    /**
     * 更新列表数据表格
     */
    private void updateListDataTables() {
        listDataContainer.getChildren().clear();
        
        // 为每个列表字段创建数据表格
        for (TemplateField field : objectFields) {
            if (field.isList()) {
                createListDataTable(field);
            }
        }
    }
    
    /**
     * 为列表字段创建数据表格
     */
    private void createListDataTable(TemplateField listField) {
        // 创建列表字段的标题窗格
        TitledPane pane = new TitledPane();
        pane.setText(listField.getName());
        pane.setCollapsible(true);
        pane.setExpanded(false);
        
        // 创建VBox容器
        VBox container = new VBox(5);
        
        // 创建添加行按钮
        Button addButton = new Button("添加行");
        addButton.getStyleClass().add("action-button");
        
        // 创建表格
        TableView<Map<String, String>> table = new TableView<>();
        
        // 如果列表项数据不存在，则初始化
        if (!listFieldDataMap.containsKey(listField.getName())) {
            listFieldDataMap.put(listField.getName(), new ArrayList<>());
        }
        
        // 查找此列表下的所有列表项字段
        List<String> listItemFields = new ArrayList<>();
        for (TemplateField field : objectFields) {
            if (!field.isList() && field.getName().startsWith(listField.getName() + ".")) {
                String itemName = field.getName().substring(field.getName().indexOf(".") + 1);
                listItemFields.add(itemName);
            }
        }
        
        // 如果没有找到列表项字段，使用默认的字段
        if (listItemFields.isEmpty()) {
            listItemFields.add("value");
        }
        
        // 创建表格列
        for (String itemField : listItemFields) {
            TableColumn<Map<String, String>, String> column = new TableColumn<>(itemField);
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(itemField, "")));
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(event -> {
                Map<String, String> row = event.getRowValue();
                row.put(itemField, event.getNewValue());
            });
            table.getColumns().add(column);
        }
        
        // 添加操作列
        TableColumn<Map<String, String>, String> actionColumn = new TableColumn<>("操作");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            final Button deleteButton = new Button("删除");
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                deleteButton.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < listFieldDataMap.get(listField.getName()).size()) {
                        listFieldDataMap.get(listField.getName()).remove(index);
                        table.setItems(FXCollections.observableArrayList(listFieldDataMap.get(listField.getName())));
                        updatePreview();
                    }
                });
                
                setGraphic(deleteButton);
            }
        });
        actionColumn.setPrefWidth(80);
        table.getColumns().add(actionColumn);
        
        // 设置表格数据
        table.setItems(FXCollections.observableArrayList(listFieldDataMap.get(listField.getName())));
        
        // 添加行按钮事件
        addButton.setOnAction(e -> {
            Map<String, String> newRow = new HashMap<>();
            for (String itemField : listItemFields) {
                newRow.put(itemField, "");
            }
            listFieldDataMap.get(listField.getName()).add(newRow);
            table.setItems(FXCollections.observableArrayList(listFieldDataMap.get(listField.getName())));
            updatePreview();
        });
        
        // 添加组件到容器
        container.getChildren().addAll(addButton, table);
        pane.setContent(container);
        
        // 添加到列表数据容器
        listDataContainer.getChildren().add(pane);
    }

    /**
     * 处理添加列表数据行的操作
     */
    @FXML
    private void handleAddListDataRow() {
        // 获取当前选中的列表数据表格
        TableView<Map<String, String>> currentTable = null;
        
        for (Node node : listDataContainer.getChildren()) {
            if (node instanceof VBox) {
                VBox vbox = (VBox)node;
                for (Node child : vbox.getChildren()) {
                    if (child instanceof TableView) {
                        currentTable = (TableView<Map<String, String>>)child;
                        break;
                    }
                }
                if (currentTable != null) break;
            }
        }
        
        if (currentTable == null) {
            // 使用示例表格
            if (listDataTableExample != null) {
                // 检查最后一行是否有数据
                ObservableList<String> items = listDataTableExample.getItems().get(listDataTableExample.getItems().size() - 1);
                boolean hasData = false;
                for (String item : items) {
                    if (item != null && !item.trim().isEmpty()) {
                        hasData = true;
                        break;
                    }
                }
                
                if (hasData) {
                    // 添加新行
                    ObservableList<String> newRow = FXCollections.observableArrayList();
                    for (int i = 0; i < items.size(); i++) {
                        newRow.add("");
                    }
                    listDataTableExample.getItems().add(newRow);
                    AppLogger.info("添加列表数据行到示例表格");
                } else {
                    AppLogger.info("最后一行没有数据，不添加新行");
                    showInfo("提示", "请先填写当前行数据");
                }
            }
            return;
        }
        
        // 获取当前表格的最后一行
        if (currentTable.getItems().isEmpty()) {
            // 如果没有行，添加第一行
            Map<String, String> newRow = new HashMap<>();
            currentTable.getColumns().forEach(col -> {
                if (col.getId() != null) {
                    newRow.put(col.getId(), "");
                }
            });
            currentTable.getItems().add(newRow);
            AppLogger.info("添加第一行到列表数据表格");
        } else {
            // 检查最后一行是否有数据
            Map<String, String> lastRow = currentTable.getItems().get(currentTable.getItems().size() - 1);
            boolean hasData = false;
            for (String value : lastRow.values()) {
                if (value != null && !value.trim().isEmpty()) {
                    hasData = true;
                    break;
                }
            }
            
            if (hasData) {
                // 添加新行
                Map<String, String> newRow = new HashMap<>();
                currentTable.getColumns().forEach(col -> {
                    if (col.getId() != null) {
                        newRow.put(col.getId(), "");
                    }
                });
                currentTable.getItems().add(newRow);
                AppLogger.info("添加新行到列表数据表格");
            } else {
                AppLogger.info("最后一行没有数据，不添加新行");
                showInfo("提示", "请先填写当前行数据");
            }
        }
        
        // 更新预览
        updatePreview();
    }
} 