package com.timelordtty.docgen.controller;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.timelordtty.AppLogger;
import com.timelordtty.docgen.service.ExcelTemplateService;
import com.timelordtty.docgen.service.WordTemplateService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * 文档生成器控制器
 */
public class DocumentGeneratorController {

    // FXML控件 - 顶部区域
    @FXML private ComboBox<String> templateTypeComboBox;
    @FXML private Button selectTemplateButton;
    @FXML private Label templateNameLabel;
    @FXML private Button importDataButton;
    @FXML private Button exportFieldsButton;
    
    // 字段管理区域
    @FXML private TextField objectFieldInput;
    @FXML private TextField listFieldInput;
    @FXML private Button addObjectFieldButton;
    @FXML private Button addListFieldButton;
    @FXML private VBox objectFieldItemsContainer;
    @FXML private VBox listFieldItemsContainer;
    
    // 模板编辑区域
    @FXML private TextArea wordEditor;
    @FXML private TableView<ObservableList<String>> excelEditor;
    @FXML private Button saveTemplateButton;
    @FXML private Button generateTemplateButton;
    
    // 数据填充区域
    @FXML private VBox objectDataItemsContainer;
    @FXML private VBox listDataItemsContainer;
    
    // 预览区域
    @FXML private TextArea wordPreviewArea;
    @FXML private TableView<ObservableList<String>> excelPreviewArea;
    @FXML private Button previewPdfButton;
    @FXML private Button generateDocumentButton;
    
    // 服务和数据
    private WordTemplateService wordTemplateService;
    private ExcelTemplateService excelTemplateService;
    private boolean isWordMode = true;
    private File currentTemplateFile;
    private String baseDir;
    private Map<String, String> fieldDataMap = new HashMap<>();
    private Map<String, List<Map<String, String>>> listFieldDataMap = new HashMap<>();
    
    // 辅助类
    private TemplateHandler templateHandler;
    private FieldManager fieldManager;
    private DataHandler dataHandler;
    
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
        
        // 初始化辅助类
        templateHandler = new TemplateHandler();
        fieldManager = new FieldManager(
            objectFieldItemsContainer, 
            listFieldItemsContainer, 
            objectDataItemsContainer, 
            listDataItemsContainer,
            this::updatePreview
        );
        dataHandler = new DataHandler(baseDir);
        
        // 初始化模板类型下拉框
        templateTypeComboBox.getSelectionModel().select(0); // 默认选择Word
        templateTypeComboBox.setOnAction(e -> handleTemplateTypeChange());
        
        // 设置WordEditor
        wordEditor.setWrapText(true);
        
        // 初始化Excel编辑器
        setupExcelEditor();
        
        // 创建必要的目录
        createDirectories();
        
        AppLogger.info("文档生成器控制器初始化完成");
    }
    
    /**
     * 处理模板类型变更
     */
    private void handleTemplateTypeChange() {
        String selectedType = templateTypeComboBox.getValue();
        isWordMode = "Word".equals(selectedType);
        
        // 切换编辑器可见性
        wordEditor.setVisible(isWordMode);
        wordEditor.setManaged(isWordMode);
        excelEditor.setVisible(!isWordMode);
        excelEditor.setManaged(!isWordMode);
        
        // 切换预览区域可见性
        wordPreviewArea.setVisible(isWordMode);
        wordPreviewArea.setManaged(isWordMode);
        excelPreviewArea.setVisible(!isWordMode);
        excelPreviewArea.setManaged(!isWordMode);
        
        // 清空当前模板和数据
        clearAll();
        
        AppLogger.info("切换模板类型为: " + selectedType);
    }
    
    /**
     * 清空所有数据和UI
     */
    private void clearAll() {
        // 清空编辑器内容
        wordEditor.clear();
        excelEditor.getItems().clear();
        excelEditor.getColumns().clear();
        
        // 清空预览内容
        wordPreviewArea.clear();
        excelPreviewArea.getItems().clear();
        excelPreviewArea.getColumns().clear();
        
        // 清空字段容器
        objectFieldItemsContainer.getChildren().clear();
        listFieldItemsContainer.getChildren().clear();
        
        // 清空数据容器
        objectDataItemsContainer.getChildren().clear();
        listDataItemsContainer.getChildren().clear();
        
        // 重置数据
        fieldDataMap.clear();
        listFieldDataMap.clear();
        currentTemplateFile = null;
        templateNameLabel.setText("未选择模板");
    }
    
    /**
     * 设置Excel编辑器
     */
    private void setupExcelEditor() {
        // 清除现有列
        excelEditor.getColumns().clear();
        excelEditor.getItems().clear();
        
        // 创建默认Excel表格结构（5列，10行）
        int defaultColumns = 5;
        int defaultRows = 10;
        
        // 创建列
        for (int i = 0; i < defaultColumns; i++) {
            final int colIndex = i;
            String columnLabel = getColumnName(i);
            
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnLabel);
            column.setCellValueFactory(data -> {
                ObservableList<String> row = data.getValue();
                return row.size() > colIndex ? new SimpleStringProperty(row.get(colIndex)) : new SimpleStringProperty("");
            });
            
            column.setPrefWidth(100);
            excelEditor.getColumns().add(column);
        }
        
        // 创建行
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (int i = 0; i < defaultRows; i++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int j = 0; j < defaultColumns; j++) {
                row.add("");
            }
            data.add(row);
        }
        excelEditor.setItems(data);
        
        // 设置表格可编辑
        excelEditor.setEditable(true);
        
        // 同样设置预览表格
        setupExcelPreview();
    }
    
    /**
     * 设置Excel预览表格
     */
    private void setupExcelPreview() {
        // 清除现有列
        excelPreviewArea.getColumns().clear();
        excelPreviewArea.getItems().clear();
        
        // 使用与编辑器相同的列数
        int columnCount = excelEditor.getColumns().size();
        
        // 创建列
        for (int i = 0; i < columnCount; i++) {
            final int colIndex = i;
            String columnLabel = getColumnName(i);
            
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnLabel);
            column.setCellValueFactory(data -> {
                ObservableList<String> row = data.getValue();
                return row.size() > colIndex ? new SimpleStringProperty(row.get(colIndex)) : new SimpleStringProperty("");
            });
            
            column.setPrefWidth(100);
            excelPreviewArea.getColumns().add(column);
        }
        
        // 为预览创建空数据
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        int rowCount = Math.max(excelEditor.getItems().size(), 3);
        
        for (int i = 0; i < rowCount; i++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int j = 0; j < columnCount; j++) {
                row.add("");
            }
            data.add(row);
        }
        
        excelPreviewArea.setItems(data);
        
        // 预览不可编辑
        excelPreviewArea.setEditable(false);
    }
    
    /**
     * 创建必要的目录
     */
    private void createDirectories() {
        // 创建模板目录
        File wordTemplateDir = new File(baseDir, "templates/word");
        File excelTemplateDir = new File(baseDir, "templates/excel");
        File documentsDir = new File(baseDir, "documents");
        
        if (!wordTemplateDir.exists()) {
            wordTemplateDir.mkdirs();
        }
        
        if (!excelTemplateDir.exists()) {
            excelTemplateDir.mkdirs();
        }
        
        if (!documentsDir.exists()) {
            documentsDir.mkdirs();
        }
    }
    
    /**
     * 处理添加普通字段
     */
    @FXML
    private void handleAddObjectField() {
        String fieldName = objectFieldInput.getText().trim();
        
        if (fieldName.isEmpty()) {
            UIHelper.showError("添加字段失败", "字段名不能为空");
            return;
        }
        
        // 清除输入
        objectFieldInput.clear();
        
        // 添加到字段列表
        fieldManager.addObjectField(fieldName);
    }
    
    /**
     * 处理添加列表字段
     */
    @FXML
    private void handleAddListField() {
        String listName = listFieldInput.getText().trim();
        
        if (listName.isEmpty()) {
            UIHelper.showError("添加列表失败", "列表名不能为空");
            return;
        }
        
        // 清除输入
        listFieldInput.clear();
        
        // 添加列表字段
        fieldManager.addListField(listName);
    }
    
    /**
     * 加载模板
     */
    private void loadTemplate(File file) {
        if (file == null || !file.exists()) {
            UIHelper.showError("错误", "模板文件不存在");
            return;
        }
        
        currentTemplateFile = file;
        templateNameLabel.setText(file.getName());
        
        try {
            if (isWordMode) {
                // 加载Word模板
                String content = templateHandler.loadWordTemplate(file);
                wordEditor.setText(content);
                analyzeTemplate(content);
                
                // 更新预览 - 使用空值填充所有占位符
                updatePreview();
            } else {
                // 加载Excel模板
                List<List<String>> content = templateHandler.loadExcelTemplate(file);
                loadExcelData(content);
                analyzeExcelTemplate(content);
                
                // 更新预览
                updatePreview();
            }
        } catch (Exception e) {
            AppLogger.info("加载模板失败: " + e.getMessage());
            e.printStackTrace();
            UIHelper.showError("加载失败", "无法加载模板文件: " + e.getMessage());
        }
    }
    
    /**
     * 分析Word模板内容并添加字段
     */
    private void analyzeTemplate(String content) {
        // 清空现有字段
        fieldManager.clearAll();
        
        // 使用TemplateHandler分析模板
        Map<String, Object> analysisResult = templateHandler.analyzeWordTemplate(content);
        
        // 提取字段
        @SuppressWarnings("unchecked")
        Set<String> objectFields = (Set<String>) analysisResult.get("objectFields");
        @SuppressWarnings("unchecked")
        Map<String, List<String>> listFields = (Map<String, List<String>>) analysisResult.get("listFields");
        
        // 添加到UI
        for (String field : objectFields) {
            fieldManager.addObjectField(field);
        }
        
        for (Map.Entry<String, List<String>> entry : listFields.entrySet()) {
            fieldManager.addListField(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * 更新预览内容
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
        String content = wordEditor.getText();
        
        // 使用TemplateHandler处理模板
        String processedContent = templateHandler.processWordTemplate(
            content, 
            fieldManager.getFieldDataMap(), 
            fieldManager.getListFieldDataMap()
        );
        
        wordPreviewArea.setText(processedContent);
    }
    
    /**
     * 处理导出字段按钮点击事件
     */
    @FXML
    private void handleExportFields() {
        try {
            // 获取字段定义
            List<String> objectFields = fieldManager.getObjectFieldNames();
            Map<String, List<String>> listFields = new HashMap<>();
            
            // 收集列表字段
            for (String listName : fieldManager.getListFieldNames()) {
                List<String> fields = new ArrayList<>();
                for (Node node : listFieldItemsContainer.getChildren()) {
                    if (node instanceof VBox && listName.equals(node.getId())) {
                        VBox container = (VBox) node;
                        for (Node child : container.getChildren()) {
                            if (child instanceof TableView) {
                                @SuppressWarnings("unchecked")
                                TableView<String> fieldsTable = (TableView<String>) child;
                                fields.addAll(fieldsTable.getItems());
                                break;
                            }
                        }
                        break;
                    }
                }
                listFields.put(listName, fields);
            }
            
            // 创建文件选择器
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("导出字段定义");
            
            // 设置初始目录
            File exportDir = new File(baseDir);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            fileChooser.setInitialDirectory(exportDir);
            fileChooser.setInitialFileName("fields_" + getCurrentTimeString() + ".xlsx");
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel 文件", "*.xlsx"));
            
            // 显示保存对话框
            File outputFile = fileChooser.showSaveDialog(wordEditor.getScene().getWindow());
            if (outputFile != null) {
                // 使用Excel服务导出字段
                dataHandler.exportFieldsToExcel(objectFields, listFields, outputFile.getAbsolutePath());
                UIHelper.showInfo("导出成功", "字段已导出到: " + outputFile.getName());
            }
        } catch (Exception e) {
            AppLogger.error("导出字段失败", e);
            UIHelper.showError("导出失败", "导出字段时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 调整布局，使其自适应
     */
    public void adjustLayout() {
        // 确保UI元素已初始化
        if (wordEditor == null || wordPreviewArea == null) return;
        
        try {
            // 获取当前场景和窗口大小
            double sceneWidth = wordEditor.getScene().getWidth();
            double sceneHeight = wordEditor.getScene().getHeight();
            
            // 调整编辑器和预览区域的大小
            wordEditor.setPrefWidth(sceneWidth * 0.45);
            wordPreviewArea.setPrefWidth(sceneWidth * 0.45);
            
            // 调整列表容器的大小
            if (objectFieldItemsContainer != null) {
                objectFieldItemsContainer.setPrefWidth(sceneWidth * 0.2);
            }
            
            if (listFieldItemsContainer != null) {
                listFieldItemsContainer.setPrefWidth(sceneWidth * 0.2);
            }
            
            if (objectDataItemsContainer != null) {
                objectDataItemsContainer.setPrefWidth(sceneWidth * 0.2);
            }
            
            if (listDataItemsContainer != null) {
                listDataItemsContainer.setPrefWidth(sceneWidth * 0.2);
            }
        } catch (Exception e) {
            // 忽略调整布局时的异常
            System.err.println("调整布局时出错: " + e.getMessage());
        }
    }

    /**
     * 处理Word模式选择
     */
    @FXML
    private void handleWordModeSelect() {
        isWordMode = true;
        
        // 切换编辑器可见性
        wordEditor.setVisible(true);
        wordEditor.setManaged(true);
        excelEditor.setVisible(false);
        excelEditor.setManaged(false);
        
        // 切换预览区域可见性
        wordPreviewArea.setVisible(true);
        wordPreviewArea.setManaged(true);
        excelPreviewArea.setVisible(false);
        excelPreviewArea.setManaged(false);
        
        // 清空当前模板和数据
        clearAll();
        
        AppLogger.info("切换到Word模式");
    }

    /**
     * 处理Excel模式选择
     */
    @FXML
    private void handleExcelModeSelect() {
        isWordMode = false;
        
        // 切换编辑器可见性
        wordEditor.setVisible(false);
        wordEditor.setManaged(false);
        excelEditor.setVisible(true);
        excelEditor.setManaged(true);
        
        // 切换预览区域可见性
        wordPreviewArea.setVisible(false);
        wordPreviewArea.setManaged(false);
        excelPreviewArea.setVisible(true);
        excelPreviewArea.setManaged(true);
        
        // 清空当前模板和数据
        clearAll();
        
        AppLogger.info("切换到Excel模式");
    }

    /**
     * 处理导入模板
     */
    @FXML
    private void handleImportTemplate() {
        handleSelectTemplate();
    }

    /**
     * 处理添加字段
     */
    @FXML
    private void handleAddField() {
        if (objectFieldInput == null) return;
        
        String fieldName = objectFieldInput.getText().trim();
        if (!fieldName.isEmpty()) {
            fieldManager.addObjectField(fieldName);
            objectFieldInput.clear();
        } else {
            UIHelper.showError("输入错误", "请输入字段名称");
        }
    }

    /**
     * 处理生成文档按钮点击事件
     */
    @FXML
    private void handleGenerateDocument() {
        try {
            // 获取字段数据
            Map<String, String> fieldDataMap = fieldManager.getFieldDataMap();
            Map<String, List<Map<String, String>>> listFieldDataMap = fieldManager.getListFieldDataMap();
            
            // 检查当前是否有模板
            if (currentTemplateFile == null || !currentTemplateFile.exists()) {
                UIHelper.showError("生成失败", "请先导入或生成模板");
                return;
            }
            
            // 创建文件选择器
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存生成的文档");
            
            // 设置初始目录
            File saveDir = new File(baseDir);
            File outputDir = new File(saveDir, "output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            fileChooser.setInitialDirectory(outputDir);
            
            // 设置文件类型过滤器
            if (isWordMode) {
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Word 文档", "*.docx"));
                fileChooser.setInitialFileName("document_" + getCurrentTimeString() + ".docx");
            } else {
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel 电子表格", "*.xlsx"));
                fileChooser.setInitialFileName("document_" + getCurrentTimeString() + ".xlsx");
            }
            
            // 显示保存对话框
            File outputFile = fileChooser.showSaveDialog(wordEditor.getScene().getWindow());
            if (outputFile != null) {
                if (isWordMode) {
                    // 读取模板内容
                    String templateContent = wordEditor.getText();
                    
                    // 处理模板内容，替换所有占位符
                    String processedContent = templateHandler.processWordTemplate(templateContent, fieldDataMap, listFieldDataMap);
                    
                    // 保存生成的文档
                    templateHandler.saveWordDocument(processedContent, outputFile.getAbsolutePath());
                    
                    UIHelper.showInfo("生成成功", "文档已生成: " + outputFile.getName());
                } else {
                    // Excel模式 - 待实现
                    UIHelper.showInfo("功能开发中", "Excel文档生成功能正在开发中");
                }
            }
        } catch (Exception e) {
            AppLogger.error("生成文档失败", e);
            UIHelper.showError("生成失败", "生成文档时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取当前时间字符串，用于文件名
     */
    private String getCurrentTimeString() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return now.format(formatter);
    }

    /**
     * 处理保存数据
     */
    @FXML
    private void handleSaveData() {
        dataHandler.saveData(fieldManager);
    }

    /**
     * 处理选择模板
     */
    @FXML
    private void handleSelectTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择模板文件");
        
        // 设置默认目录为项目的templates目录
        File defaultDir = new File(baseDir);
        File templatesDir = new File(defaultDir, "templates");
        
        // 检查templates目录并选择子目录
        if (templatesDir.exists() && templatesDir.isDirectory()) {
            if (isWordMode) {
                File wordDir = new File(templatesDir, "word");
                if (wordDir.exists() && wordDir.isDirectory()) {
                    fileChooser.setInitialDirectory(wordDir);
                } else {
                    fileChooser.setInitialDirectory(templatesDir);
                }
            } else {
                File excelDir = new File(templatesDir, "excel");
                if (excelDir.exists() && excelDir.isDirectory()) {
                    fileChooser.setInitialDirectory(excelDir);
                } else {
                    fileChooser.setInitialDirectory(templatesDir);
                }
            }
        } else if (defaultDir.exists()) {
            fileChooser.setInitialDirectory(defaultDir);
        }
        
        // 设置文件过滤器
        if (isWordMode) {
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Word文档", "*.docx"));
        } else {
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel表格", "*.xlsx"));
        }
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            loadTemplate(selectedFile);
        }
    }
    
    /**
     * 处理导入数据
     */
    @FXML
    private void handleImportData() {
        if (dataHandler.importData(fieldManager)) {
            updatePreview();
        }
    }
    
    /**
     * 加载Excel数据
     * 
     * @param data Excel数据
     */
    private void loadExcelData(List<List<String>> data) {
        // 清空当前数据
        excelEditor.getItems().clear();
        excelEditor.getColumns().clear();
        
        // 创建列
        for (int i = 0; i < data.get(0).size(); i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(getColumnName(i));
            column.setCellValueFactory(param -> {
                ObservableList<String> row = param.getValue();
                return row.size() > colIndex ? new SimpleStringProperty(row.get(colIndex)) : new SimpleStringProperty("");
            });
            excelEditor.getColumns().add(column);
        }
        
        // 创建数据行
        ObservableList<ObservableList<String>> tableData = FXCollections.observableArrayList();
        for (List<String> row : data) {
            tableData.add(FXCollections.observableArrayList(row));
        }
        excelEditor.setItems(tableData);
    }
    
    /**
     * 获取Excel列名
     * 
     * @param index 列索引
     * @return 列名（A, B, C, ...）
     */
    private String getColumnName(int index) {
        StringBuilder columnName = new StringBuilder();
        int temp = index;
        
        while (temp >= 0) {
            int remainder = temp % 26;
            char letter = (char) ('A' + remainder);
            columnName.insert(0, letter);
            temp = temp / 26 - 1;
            if (temp < 0) break;
        }
        
        return columnName.toString();
    }
    
    /**
     * 分析Excel模板
     * 
     * @param content Excel内容
     */
    private void analyzeExcelTemplate(List<List<String>> content) {
        // 清空现有字段
        fieldManager.clearAll();
        
        if (content == null || content.isEmpty()) {
            return;
        }
        
        // 分析表头行（第一行）
        List<String> headers = content.get(0);
        for (String header : headers) {
            if (header != null && !header.trim().isEmpty()) {
                // 检查是否是列表字段（格式：listName.fieldName）
                if (header.contains(".")) {
                    String[] parts = header.split("\\.", 2);
                    String listName = parts[0];
                    String fieldName = parts[1];
                    
                    // 添加到列表字段
                    List<String> fields = new ArrayList<>();
                    fields.add(fieldName);
                    fieldManager.addListField(listName, fields);
                } else {
                    // 添加为普通字段
                    fieldManager.addObjectField(header);
                }
            }
        }
    }
    
    /**
     * 更新Excel预览
     */
    private void updateExcelPreview() {
        if (!isWordMode) {
            // 获取当前Excel编辑器的内容
            List<List<String>> content = getExcelEditorContent();
            
            // 清空预览表格
            excelPreviewArea.getItems().clear();
            excelPreviewArea.getColumns().clear();
            
            if (content.isEmpty()) {
                return;
            }
            
            // 创建列
            for (int i = 0; i < content.get(0).size(); i++) {
                final int colIndex = i;
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(getColumnName(i));
                column.setCellValueFactory(data -> {
                    ObservableList<String> row = data.getValue();
                    return row.size() > colIndex ? new SimpleStringProperty(row.get(colIndex)) : new SimpleStringProperty("");
                });
                excelPreviewArea.getColumns().add(column);
            }
            
            // 添加数据行
            ObservableList<ObservableList<String>> previewData = FXCollections.observableArrayList();
            for (List<String> row : content) {
                previewData.add(FXCollections.observableArrayList(row));
            }
            excelPreviewArea.setItems(previewData);
        }
    }
    
    /**
     * 获取Excel编辑器的内容
     * 
     * @return Excel内容的二维列表
     */
    private List<List<String>> getExcelEditorContent() {
        List<List<String>> content = new ArrayList<>();
        
        // 获取所有行
        for (ObservableList<String> row : excelEditor.getItems()) {
            List<String> rowContent = new ArrayList<>(row);
            content.add(rowContent);
        }
        
        return content;
    }

    /**
     * 处理保存模板
     */
    @FXML
    private void handleSaveTemplate() {
        if (currentTemplateFile == null) {
            UIHelper.showError("保存失败", "请先选择或导入一个模板");
            return;
        }

        try {
            if (isWordMode) {
                String content = wordEditor.getText();
                templateHandler.saveWordTemplate(currentTemplateFile.getAbsolutePath(), content);
            } else {
                // Excel模式的保存逻辑
                List<List<String>> content = getExcelEditorContent();
                excelTemplateService.saveExcelTemplate(currentTemplateFile.getAbsolutePath(), content);
            }
            
            // 使用模态对话框显示成功消息
            UIHelper.showInfo("保存成功", "模板已保存");
            
        } catch (Exception e) {
            AppLogger.error("保存模板失败", e);
            UIHelper.showError("保存失败", "保存模板时出错: " + e.getMessage());
        }
    }

    /**
     * 处理生成模板按钮点击事件
     */
    @FXML
    private void handleGenerateTemplate() {
        try {
            AppLogger.info("开始生成模板...");
            
            if (isWordMode) {
                // 生成Word模板
                generateWordTemplate();
            } else {
                // 生成Excel模板
                generateExcelTemplate();
            }
        } catch (Exception e) {
            AppLogger.error("生成模板失败: " + e.getMessage(), e);
            UIHelper.showError("错误", "生成模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成Word模板
     */
    private void generateWordTemplate() throws Exception {
        // 检查是否已有字段定义
        List<String> objectFields = fieldManager.getObjectFieldNames();
        List<String> listFields = fieldManager.getListFieldNames();
        
        if (objectFields.isEmpty() && listFields.isEmpty()) {
            UIHelper.showError("错误", "请先添加字段定义！");
            return;
        }
        
        // 创建临时文件名
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File templateFile = new File(baseDir + "/templates/word/自动生成模板_" + timeStamp + ".docx");
        
        // 创建基础模板
        String templateContent = templateHandler.generateWordTemplateContent(objectFields, listFields);
        wordEditor.setText(templateContent);
        handleSaveTemplate(); // 保存模板到文件
        
        // 更新UI
        templateNameLabel.setText(templateFile.getName());
        updatePreview();
    }
    
    /**
     * 生成Excel模板
     */
    private void generateExcelTemplate() throws Exception {
        // 检查是否已有字段定义
        List<String> objectFields = fieldManager.getObjectFieldNames();
        List<String> listFields = fieldManager.getListFieldNames();
        
        if (objectFields.isEmpty() && listFields.isEmpty()) {
            UIHelper.showError("错误", "请先添加字段定义！");
            return;
        }
        
        // 创建临时文件名
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String templateFilePath = baseDir + "/templates/excel/自动生成模板_" + timeStamp + ".xlsx";
        
        // 准备字段列表映射
        Map<String, List<String>> fieldMap = new HashMap<>();
        
        // 添加对象字段
        fieldMap.put("对象字段", objectFields);
        
        // 添加每个列表字段
        for (String listName : listFields) {
            // 为每个列表添加一些默认字段
            List<String> columnNames = new ArrayList<>();
            columnNames.add("ID");
            columnNames.add("名称");
            columnNames.add("描述");
            fieldMap.put(listName, columnNames);
        }
        
        // 调用服务创建Excel模板
        templateHandler.generateExcelTemplate(templateFilePath, fieldMap);
        
        // 加载生成的模板
        loadTemplate(new File(templateFilePath));
        
        // 更新UI
        templateNameLabel.setText(new File(templateFilePath).getName());
        updatePreview();
    }
}