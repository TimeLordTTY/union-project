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
        // 初始化一个简单的Excel编辑器布局
        TableColumn<ObservableList<String>, String> col1 = new TableColumn<>("A");
        TableColumn<ObservableList<String>, String> col2 = new TableColumn<>("B");
        TableColumn<ObservableList<String>, String> col3 = new TableColumn<>("C");
        
        col1.setCellValueFactory(data -> {
            ObservableList<String> row = data.getValue();
            return row.size() > 0 ? new SimpleStringProperty(row.get(0)) : new SimpleStringProperty("");
        });
        
        col2.setCellValueFactory(data -> {
            ObservableList<String> row = data.getValue();
            return row.size() > 1 ? new SimpleStringProperty(row.get(1)) : new SimpleStringProperty("");
        });
        
        col3.setCellValueFactory(data -> {
            ObservableList<String> row = data.getValue();
            return row.size() > 2 ? new SimpleStringProperty(row.get(2)) : new SimpleStringProperty("");
        });
        
        excelEditor.getColumns().addAll(col1, col2, col3);
        
        // 添加一些默认行
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        data.add(FXCollections.observableArrayList("", "", ""));
        data.add(FXCollections.observableArrayList("", "", ""));
        data.add(FXCollections.observableArrayList("", "", ""));
        
        excelEditor.setItems(data);
        
        // 同样设置预览表格
        setupExcelPreview();
    }
    
    /**
     * 设置Excel预览表格
     */
    private void setupExcelPreview() {
        TableColumn<ObservableList<String>, String> col1 = new TableColumn<>("A");
        TableColumn<ObservableList<String>, String> col2 = new TableColumn<>("B");
        TableColumn<ObservableList<String>, String> col3 = new TableColumn<>("C");
        
        col1.setCellValueFactory(data -> {
            ObservableList<String> row = data.getValue();
            return row.size() > 0 ? new SimpleStringProperty(row.get(0)) : new SimpleStringProperty("");
        });
        
        col2.setCellValueFactory(data -> {
            ObservableList<String> row = data.getValue();
            return row.size() > 1 ? new SimpleStringProperty(row.get(1)) : new SimpleStringProperty("");
        });
        
        col3.setCellValueFactory(data -> {
            ObservableList<String> row = data.getValue();
            return row.size() > 2 ? new SimpleStringProperty(row.get(2)) : new SimpleStringProperty("");
        });
        
        excelPreviewArea.getColumns().addAll(col1, col2, col3);
        
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        data.add(FXCollections.observableArrayList("", "", ""));
        data.add(FXCollections.observableArrayList("", "", ""));
        data.add(FXCollections.observableArrayList("", "", ""));
        
        excelPreviewArea.setItems(data);
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
     * 处理导出字段
     */
    @FXML
    private void handleExportFields() {
        try {
            if (isWordMode && wordEditor.getText().isEmpty()) {
                UIHelper.showError("导出失败", "请先导入或编辑模板");
                return;
            }
            
            // 使用DataHandler导出字段定义
            dataHandler.exportFieldDefinitions(objectFieldItemsContainer, listFieldItemsContainer);
        } catch (Exception e) {
            UIHelper.showError("导出失败", "导出字段定义时出错: " + e.getMessage());
            e.printStackTrace();
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
     * 处理生成文档
     */
    @FXML
    private void handleGenerateDocument() {
        try {
            if (currentTemplateFile == null) {
                UIHelper.showError("错误", "请先选择模板文件");
                return;
            }
            
            // 创建文件选择器
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存生成的文档");
            
            // 设置默认输出目录
            File defaultDir = new File(baseDir);
            fileChooser.setInitialDirectory(defaultDir);
            
            // 设置默认文件名
            String defaultFileName = "生成文档_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            
            // 设置文件类型
            if (isWordMode) {
                fileChooser.setInitialFileName(defaultFileName + ".docx");
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Word文档", "*.docx"));
            } else {
                fileChooser.setInitialFileName(defaultFileName + ".xlsx");
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel表格", "*.xlsx"));
            }
            
            // 显示保存对话框
            File outputFile = fileChooser.showSaveDialog(null);
            if (outputFile != null) {
                if (isWordMode) {
                    // 生成Word文档
                    String content = wordPreviewArea.getText();
                    templateHandler.saveWordDocument(content, outputFile.getAbsolutePath());
                } else {
                    // 生成Excel文档 - 这里留给后续实现
                }
                
                UIHelper.showInfo("成功", "文档已生成: " + outputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            UIHelper.showError("生成失败", "生成文档时出错: " + e.getMessage());
            e.printStackTrace();
        }
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
     * 获取Excel列名（A, B, C, ...）
     * 
     * @param index 列索引
     * @return 列名
     */
    private String getColumnName(int index) {
        StringBuilder name = new StringBuilder();
        while (index >= 0) {
            name.insert(0, (char) ('A' + index % 26));
            index = index / 26 - 1;
        }
        return name.toString();
    }
    
    /**
     * 分析Excel模板
     * 
     * @param content Excel内容
     */
    private void analyzeExcelTemplate(List<List<String>> content) {
        // 此处留给后续实现
    }
    
    /**
     * 更新Excel预览
     */
    private void updateExcelPreview() {
        // 此处留给后续实现
    }
    
    /**
     * 处理保存模板
     */
    @FXML
    private void handleSaveTemplate() {
        try {
            if (isWordMode && wordEditor.getText().isEmpty()) {
                UIHelper.showError("保存失败", "请先导入或编辑模板");
                return;
            }
            
            // 创建文件选择器
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存模板");
            
            // 设置默认输出目录
            File defaultDir = new File(baseDir);
            File templatesDir = new File(defaultDir, "templates");
            
            if (isWordMode) {
                File wordDir = new File(templatesDir, "word");
                if (wordDir.exists() && wordDir.isDirectory()) {
                    fileChooser.setInitialDirectory(wordDir);
                } else {
                    fileChooser.setInitialDirectory(templatesDir);
                }
                fileChooser.setInitialFileName("模板.docx");
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Word文档", "*.docx"));
            } else {
                File excelDir = new File(templatesDir, "excel");
                if (excelDir.exists() && excelDir.isDirectory()) {
                    fileChooser.setInitialDirectory(excelDir);
                } else {
                    fileChooser.setInitialDirectory(templatesDir);
                }
                fileChooser.setInitialFileName("模板.xlsx");
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel表格", "*.xlsx"));
            }
            
            // 显示保存对话框
            File outputFile = fileChooser.showSaveDialog(null);
            if (outputFile != null) {
                if (isWordMode) {
                    // 保存Word模板
                    String content = wordEditor.getText();
                    templateHandler.saveWordTemplate(outputFile.getAbsolutePath(), content);
                } else {
                    // 保存Excel模板
                    // TODO: 实现Excel模板保存
                    UIHelper.showError("未实现", "Excel模板保存功能尚未实现");
                    return;
                }
                
                UIHelper.showInfo("成功", "模板已保存: " + outputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            UIHelper.showError("保存失败", "保存模板时出错: " + e.getMessage());
            e.printStackTrace();
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
            
            UIHelper.showInfo("成功", "模板生成成功！");
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