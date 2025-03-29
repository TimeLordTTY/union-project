package com.timelordtty.docgen.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;

import com.timelordtty.AppLogger;
import com.timelordtty.docgen.service.DocGeneratorService;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * 文档生成控制器
 * 处理从Word或Excel模板生成文档的功能
 */
public class DocumentGeneratorController {
    
    @FXML
    private ComboBox<String> documentTypeComboBox;
    
    @FXML
    private TextField templateFilePathTextField;
    
    @FXML
    private Button chooseTemplateFileButton;
    
    @FXML
    private TextField dataFilePathTextField;
    
    @FXML
    private Button chooseDataFileButton;
    
    @FXML
    private TextField outputDirectoryTextField;
    
    @FXML
    private Button chooseOutputDirectoryButton;
    
    @FXML
    private Button generateButton;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private TextArea templateContentTextArea;
    
    @FXML
    private VBox fieldsListVBox;
    
    @FXML
    private VBox dataInputVBox;
    
    @FXML
    private VBox previewContentVBox;
    
    @FXML
    private ScrollPane previewScrollPane;
    
    @FXML
    private TextField normalPlaceholderTextField;
    
    @FXML
    private Button addNormalPlaceholderButton;
    
    @FXML
    private TextField listNameTextField;
    
    @FXML
    private TextField listPropertyTextField;
    
    @FXML
    private Button addListPlaceholderButton;
    
    @FXML
    private Button saveTemplateButton;
    
    @FXML
    private Button generateTemplateButton;
    
    @FXML
    private Button saveDocumentButton;
    
    // 默认模板目录（相对路径）
    private final String DEFAULT_TEMPLATES_DIR = "templates";
    
    // 文档生成服务
    private DocGeneratorService docService;
    
    // 数据映射
    private Map<String, String> dataMap = new HashMap<>();
    
    @FXML
    private void initialize() {
        // 初始化文档类型下拉框
        documentTypeComboBox.getItems().addAll("Word", "Excel");
        documentTypeComboBox.setValue("Word");
        
        // 文档类型切换监听器
        documentTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            // 清空所有内容
            clearAllContent();
            // 更新模板文件扩展名
            updateTemplateFileExtensions();
        });
        
        // 初始化默认值
        templateFilePathTextField.setText(getTemplatesDirectory() + "/word/订单模板.docx");
        dataFilePathTextField.setText(getTemplatesDirectory() + "/excel/测试数据.xlsx");
        outputDirectoryTextField.setText(System.getProperty("user.dir") + "/output");
        
        // 设置左右两侧对齐
        setupLayoutAlignment();
        
        // 初始化状态标签
        statusLabel.setText("准备就绪");
        
        // 检查POI依赖
        if (!checkPOIDependencies()) {
            showMissingDependenciesAlert();
        }
        
        // 初始化文档生成服务
        docService = new DocGeneratorService();
        
        // 初始化数据映射
        dataMap = new HashMap<>();
        
        // 设置按钮事件
        saveTemplateButton.setOnAction(event -> saveTemplate());
        generateTemplateButton.setOnAction(event -> generateTemplate());
        saveDocumentButton.setOnAction(event -> saveDocument());
        addNormalPlaceholderButton.setOnAction(event -> addNormalPlaceholder());
        addListPlaceholderButton.setOnAction(event -> addListPlaceholder());
        
        // 设置Enter键提交
        normalPlaceholderTextField.setOnAction(event -> addNormalPlaceholder());
        listPropertyTextField.setOnAction(event -> addListPlaceholder());
    }
    
    /**
     * 检查POI依赖是否完整
     * 
     * @return 是否所有必要的POI依赖都已加载
     */
    private boolean checkPOIDependencies() {
        try {
            // 尝试加载POI OOXML类
            Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook");
            Class.forName("org.apache.poi.ss.usermodel.WorkbookFactory");
            Class.forName("org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr");
            return true;
        } catch (ClassNotFoundException e) {
            AppLogger.error("缺少必要的POI依赖: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 显示缺少依赖的警告对话框
     */
    private void showMissingDependenciesAlert() {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("依赖缺失");
        alert.setHeaderText("缺少必要的POI OOXML依赖");
        
        String message = "程序检测到缺少处理Excel文件所需的依赖库。\n\n" +
                        "请确保以下JAR文件已正确添加到项目的lib目录：\n" +
                        "- poi-ooxml-5.2.2.jar\n" +
                        "- poi-ooxml-schemas-4.1.2.jar\n" +
                        "- xmlbeans-5.1.1.jar\n\n" +
                        "选择Excel模板可能会导致应用程序错误。";
        
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 清空所有内容
     */
    private void clearAllContent() {
        // 清空模板内容
        templateContentTextArea.clear();
        
        // 清空字段列表
        fieldsListVBox.getChildren().clear();
        
        // 清空数据输入区域
        dataInputVBox.getChildren().clear();
        
        // 清空预览区域
        previewContentVBox.getChildren().clear();
        
        // 清空数据映射
        dataMap.clear();
        
        // 重置状态
        statusLabel.setText("已切换文档类型，请选择模板文件");
        
        // 清空文件路径
        String docType = documentTypeComboBox.getValue();
        String fileExt = "Word".equals(docType) ? "docx" : "xlsx";
        templateFilePathTextField.setText(getTemplatesDirectory() + "/" + docType.toLowerCase() + "/订单模板." + fileExt);
    }
    
    /**
     * 设置布局对齐
     */
    private void setupLayoutAlignment() {
        // 确保模板内容和预览区域高度一致
        templateContentTextArea.prefHeightProperty().bind(previewScrollPane.heightProperty());
        
        // 字段列表和数据输入区域高度一致
        fieldsListVBox.prefHeightProperty().bind(dataInputVBox.heightProperty());
        
        // 设置分割面板位置
        // 这里通过CSS来控制布局，确保左右两侧均匀分布
        String css = ".split-pane > .split-pane-divider { -fx-padding: 0 0.5em 0 0.5em; }";
        templateContentTextArea.getScene().getStylesheets().add(css);
        
        // 为模板内容文本区域添加文本变化监听
        templateContentTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            // 当模板内容变化时，更新蓝色占位符
            highlightPlaceholders();
            // 更新字段列表
            extractAndAddPlaceholders(newValue);
        });
    }
    
    /**
     * 高亮显示模板内容中的占位符
     */
    private void highlightPlaceholders() {
        String documentType = documentTypeComboBox.getValue();
        
        if ("Word".equals(documentType)) {
            highlightWordPlaceholders();
        } else if ("Excel".equals(documentType)) {
            // Excel模板在表格中已经有高亮处理
        }
    }
    
    /**
     * 高亮显示Word模板中的占位符
     */
    private void highlightWordPlaceholders() {
        // 获取文本内容
        String content = templateContentTextArea.getText();
        
        // 清空原有样式
        templateContentTextArea.setStyle("");
        
        // 创建富文本区域来替换原来的文本区域
        // 由于JavaFX TextArea不支持部分文本着色，这里使用其他方法实现
        
        // 方法1: 使用内联CSS来修改整个TextArea的样式
        templateContentTextArea.setStyle("-fx-highlight-fill: #CCCCFF; -fx-highlight-text-fill: #0066cc;");
        
        // 如果有更好的方法，如使用RichTextFX库，可以替换此处代码
    }
    
    /**
     * 加载模板内容
     * 
     * @param templateFile 模板文件
     */
    private void loadTemplateContent(File templateFile) {
        try {
            String documentType = documentTypeComboBox.getValue();
            
            // 清空之前的内容
            templateContentTextArea.clear();
            fieldsListVBox.getChildren().clear();
            dataInputVBox.getChildren().clear();
            previewContentVBox.getChildren().clear();
            
            AppLogger.info("加载模板内容: " + templateFile.getAbsolutePath());
            
            if ("Word".equals(documentType)) {
                loadWordTemplateContent(templateFile);
            } else if ("Excel".equals(documentType)) {
                loadExcelTemplateContent(templateFile);
            }
            
            // 高亮占位符
            highlightPlaceholders();
            
            // 更新状态
            statusLabel.setText("模板加载成功: " + templateFile.getName());
        } catch (Exception e) {
            AppLogger.error("加载模板内容失败: " + e.getMessage(), e);
            statusLabel.setText("加载模板失败: " + e.getMessage());
            showAlert(AlertType.ERROR, "错误", "加载模板内容失败", e.getMessage());
        }
    }
    
    /**
     * 加载Word文档模板内容
     * 
     * @param templateFile Word模板文件
     * @throws Exception 如果加载失败
     */
    private void loadWordTemplateContent(File templateFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(templateFile);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
            
            // 设置内容到文本框
            templateContentTextArea.setText(content.toString());
            
            // 提取占位符并添加到字段列表
            extractAndAddPlaceholders(content.toString());
            
            // 更新预览区域
            updatePreview(content.toString());
        }
    }
    
    /**
     * 加载Excel文档模板内容
     * 
     * @param templateFile Excel模板文件
     * @throws Exception 如果加载失败
     */
    private void loadExcelTemplateContent(File templateFile) throws Exception {
        try {
            // 使用FileInputStream而不是直接传递File对象
            try (FileInputStream fis = new FileInputStream(templateFile);
                 Workbook workbook = WorkbookFactory.create(fis)) {
                
                // 先确保UI清空
                templateContentTextArea.clear();
                previewContentVBox.getChildren().clear();
                
                // 将Excel内容转换为字符串表示
                StringBuilder content = new StringBuilder();
                
                // 获取第一个工作表
                if (workbook.getNumberOfSheets() > 0) {
                    Sheet sheet = workbook.getSheetAt(0);
                    
                    // 遍历行和单元格，构建内容
                    for (Row row : sheet) {
                        StringBuilder rowContent = new StringBuilder();
                        
                        for (Cell cell : row) {
                            String cellValue = getCellValueAsString(cell);
                            rowContent.append(cellValue).append("\t");
                        }
                        
                        // 添加行内容和换行符
                        content.append(rowContent).append("\n");
                    }
                }
                
                // 设置内容到文本区域
                templateContentTextArea.setText(content.toString());
                
                // 提取占位符并添加到字段列表
                extractAndAddPlaceholders(content.toString());
                
                // 使用表格显示Excel内容
                displayExcelInTable(workbook, templateContentTextArea, previewContentVBox);
                
                AppLogger.info("Excel模板内容加载成功");
            } catch (Exception e) {
                AppLogger.error("加载Excel模板失败: " + e.getMessage(), e);
                throw new Exception("加载Excel模板失败: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            // 捕获并提供更多信息
            AppLogger.error("处理Excel模板错误: " + e.getMessage(), e);
            String message = "处理Excel模板错误: " + e.getMessage() + 
                           "\n请确保已添加所有必要的POI依赖，包括poi-ooxml*.jar";
            throw new Exception(message, e);
        }
    }
    
    /**
     * 在表格中显示Excel内容
     * 
     * @param workbook Excel工作簿
     * @param contentArea 内容文本区域
     * @param previewContainer 预览容器
     */
    private void displayExcelInTable(Workbook workbook, TextArea contentArea, VBox previewContainer) {
        // 清空预览容器
        previewContainer.getChildren().clear();
        
        // 为预览区域添加标题
        Label titleLabel = new Label("Excel表格预览");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0 0 5 0;");
        previewContainer.getChildren().add(titleLabel);
        
        try {
            // 获取第一个工作表
            if (workbook.getNumberOfSheets() > 0) {
                Sheet sheet = workbook.getSheetAt(0);
                
                // 创建一个GridPane来显示Excel表格
                GridPane gridPane = new GridPane();
                gridPane.setGridLinesVisible(true);
                gridPane.setStyle("-fx-background-color: white; -fx-border-color: #CCCCCC;");
                
                // 添加列约束，使表格可以适应宽度
                int maxColumns = 0;
                for (Row row : sheet) {
                    maxColumns = Math.max(maxColumns, row.getLastCellNum());
                }
                
                for (int i = 0; i < maxColumns; i++) {
                    ColumnConstraints columnConstraints = new ColumnConstraints();
                    columnConstraints.setHgrow(Priority.SOMETIMES);
                    columnConstraints.setFillWidth(true);
                    gridPane.getColumnConstraints().add(columnConstraints);
                }
                
                // 遍历行和单元格，添加到GridPane
                int rowIndex = 0;
                for (Row row : sheet) {
                    int cellIndex = 0;
                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        
                        Label cellLabel = new Label(cellValue);
                        cellLabel.setPadding(new Insets(5));
                        
                        // 高亮显示占位符
                        if (cellValue.contains("${")) {
                            cellLabel.setStyle("-fx-text-fill: #0066cc; -fx-font-weight: bold;");
                        }
                        
                        gridPane.add(cellLabel, cellIndex, rowIndex);
                        cellIndex++;
                    }
                    rowIndex++;
                }
                
                // 将表格添加到预览容器
                previewContainer.getChildren().add(gridPane);
                
                // 添加表格内容到模板内容编辑区
                createExcelEditorTable(sheet, contentArea);
            }
        } catch (Exception e) {
            AppLogger.error("创建Excel表格预览失败: " + e.getMessage(), e);
            Label errorLabel = new Label("表格预览加载失败: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            previewContainer.getChildren().add(errorLabel);
        }
    }
    
    /**
     * 为模板内容编辑器创建Excel表格编辑组件
     * 
     * @param sheet Excel工作表
     * @param contentArea 原始文本区域
     */
    private void createExcelEditorTable(Sheet sheet, TextArea contentArea) {
        // 保存原始文本内容
        String originalText = contentArea.getText();
        
        // 创建一个新的VBox来替换原始文本区域
        VBox editorContainer = new VBox(10);
        editorContainer.setPadding(new Insets(10));
        
        // 添加表格编辑提示
        Label editorLabel = new Label("Excel模板编辑区 (占位符以${xxx}格式编写)");
        editorLabel.setStyle("-fx-font-weight: bold;");
        editorContainer.getChildren().add(editorLabel);
        
        // 以表格形式显示的编辑内容已在显示方法中处理
        // 我们保留文本区域用于底层数据存储
        
        // 设置文本区域样式，使占位符显示为蓝色
        contentArea.setStyle("-fx-highlight-fill: #CCCCFF; -fx-highlight-text-fill: #0066cc;");
    }
    
    @FXML
    private void chooseTemplateFile() {
        // 获取当前文档类型
        String documentType = documentTypeComboBox.getValue();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择" + documentType + "模板文件");
        
        // 根据文档类型设置不同的文件扩展名过滤器
        if ("Word".equals(documentType)) {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Word文档", "*.docx")
            );
        } else if ("Excel".equals(documentType)) {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel工作簿", "*.xlsx")
            );
        }
        
        // 设置初始目录
        String initialDirectory = getTemplatesDirectory() + "/" + documentType.toLowerCase();
        fileChooser.setInitialDirectory(new File(initialDirectory));
        
        // 显示文件选择器对话框
        File selectedFile = fileChooser.showOpenDialog(templateFilePathTextField.getScene().getWindow());
        if (selectedFile != null) {
            // 更新文本字段显示选中的文件路径
            templateFilePathTextField.setText(selectedFile.getAbsolutePath());
            AppLogger.info("已选择模板文件：" + selectedFile.getAbsolutePath());
            
            // 加载模板内容
            loadTemplateContent(selectedFile);
        }
    }
    
    @FXML
    private void chooseDataFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择数据文件");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("JSON 文件", "*.json"),
            new ExtensionFilter("所有文件", "*.*")
        );
        
        // 设置默认目录为templates/json目录
        String templatesPath = getTemplatesDirectory();
        File defaultDir = new File(templatesPath);
        File jsonDir = new File(defaultDir, "json");
        
        if (jsonDir.exists() && jsonDir.isDirectory()) {
            fileChooser.setInitialDirectory(jsonDir);
        } else if (defaultDir.exists() && defaultDir.isDirectory()) {
            fileChooser.setInitialDirectory(defaultDir);
        } else {
            defaultDir.mkdirs();
            AppLogger.info("已创建默认模板目录：" + templatesPath);
            fileChooser.setInitialDirectory(defaultDir);
        }
        
        File selectedFile = fileChooser.showOpenDialog(chooseDataFileButton.getScene().getWindow());
        if (selectedFile != null) {
            dataFilePathTextField.setText(selectedFile.getAbsolutePath());
            statusLabel.setText("已选择数据文件: " + selectedFile.getName());
            AppLogger.info("已选择数据文件：" + selectedFile.getAbsolutePath());
        }
    }
    
    @FXML
    private void chooseOutputDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择输出目录");
        
        // 设置默认目录
        String templatesPath = getTemplatesDirectory();
        File defaultDir = new File(templatesPath);
        if (defaultDir.exists() && defaultDir.isDirectory()) {
            directoryChooser.setInitialDirectory(defaultDir);
        } else {
            defaultDir.mkdirs();
            AppLogger.info("已创建默认模板目录：" + templatesPath);
            directoryChooser.setInitialDirectory(defaultDir);
        }
        
        File selectedDirectory = directoryChooser.showDialog(chooseOutputDirectoryButton.getScene().getWindow());
        if (selectedDirectory != null) {
            outputDirectoryTextField.setText(selectedDirectory.getAbsolutePath());
            statusLabel.setText("已选择输出目录: " + selectedDirectory.getName());
            AppLogger.info("已选择输出目录：" + selectedDirectory.getAbsolutePath());
        }
    }
    
    @FXML
    private void generateDocument() {
        statusLabel.setText("正在处理...");
        AppLogger.info("开始生成文档");
        
        // 验证是否所有必填字段都已填写
        if (templateFilePathTextField.getText().isEmpty() || 
            outputDirectoryTextField.getText().isEmpty() || 
            dataFilePathTextField.getText().isEmpty()) {
            
            showAlert(AlertType.ERROR, "错误", "请填写所有字段", 
                     "模板文件、输出目录和数据文件均为必填项。");
            statusLabel.setText("错误：请填写所有必填字段");
            return;
        }
        
        try {
            // 获取选择的模板类型
            String documentType = documentTypeComboBox.getValue();
            boolean isWordTemplate = "Word".equals(documentType);
            
            // 调用服务生成文档
            String templatePath = templateFilePathTextField.getText();
            String outputDir = outputDirectoryTextField.getText();
            String dataFilePath = dataFilePathTextField.getText();
            
            String outputFilePath = docService.generateDocument(
                templatePath, 
                outputDir, 
                dataFilePath, 
                isWordTemplate
            );
            
            // 显示成功消息
            statusLabel.setText("文档生成成功: " + outputFilePath);
            showAlert(AlertType.INFORMATION, "成功", "文档生成成功", 
                     "文档已生成至: " + outputFilePath);
            
        } catch (Exception e) {
            AppLogger.error("生成文档失败", e);
            statusLabel.setText("错误: " + e.getMessage());
            showAlert(AlertType.ERROR, "错误", "生成文档失败", 
                     "原因: " + e.getMessage());
        }
    }
    
    /**
     * 显示警告对话框
     * @param type 警告类型
     * @param title 标题
     * @param header 头部信息
     * @param content 内容
     */
    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // 保存模板
    private void saveTemplate() {
        try {
            String templateFilePath = templateFilePathTextField.getText();
            
            if (templateFilePath.isEmpty()) {
                // 如果未指定模板文件，调用生成模板功能
                generateTemplate();
                return;
            }
            
            File templateFile = new File(templateFilePath);
            if (!templateFile.exists()) {
                showAlert(AlertType.ERROR, "错误", "模板文件不存在", "请先选择或生成模板文件");
                return;
            }
            
            String documentType = documentTypeComboBox.getValue();
            String templateContent = templateContentTextArea.getText();
            
            if ("Word".equals(documentType)) {
                // 保存Word模板
                try (XWPFDocument document = new XWPFDocument()) {
                    // 分行添加段落
                    String[] lines = templateContent.split("\n");
                    for (String line : lines) {
                        XWPFParagraph paragraph = document.createParagraph();
                        XWPFRun run = paragraph.createRun();
                        run.setText(line);
                    }
                    
                    // 保存文档
                    try (FileOutputStream fos = new FileOutputStream(templateFile)) {
                        document.write(fos);
                    }
                }
            } else if ("Excel".equals(documentType)) {
                // 保存Excel模板
                try (Workbook workbook = new XSSFWorkbook()) {
                    Sheet sheet = workbook.createSheet("Template");
                    
                    // 分行添加单元格
                    String[] lines = templateContent.split("\n");
                    for (int i = 0; i < lines.length; i++) {
                        Row row = sheet.createRow(i);
                        String[] cells = lines[i].split("\t");
                        
                        for (int j = 0; j < cells.length; j++) {
                            Cell cell = row.createCell(j);
                            cell.setCellValue(cells[j]);
                        }
                    }
                    
                    // 保存文档
                    try (FileOutputStream fos = new FileOutputStream(templateFile)) {
                        workbook.write(fos);
                    }
                }
            }
            
            // 更新状态
            statusLabel.setText("模板已保存: " + templateFile.getName());
            
        } catch (Exception e) {
            AppLogger.error("保存模板失败", e);
            showAlert(AlertType.ERROR, "错误", "保存模板失败", e.getMessage());
            statusLabel.setText("错误: 保存模板失败");
        }
    }
    
    // 生成模板
    private void generateTemplate() {
        try {
            String documentType = documentTypeComboBox.getValue();
            String templateContent = templateContentTextArea.getText();
            
            if (templateContent.isEmpty()) {
                showAlert(AlertType.ERROR, "错误", "模板内容为空", "请先编辑模板内容");
                return;
            }
            
            // 创建文件选择器
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存模板文件");
            
            // 设置默认路径和文件名
            String templatesPath = getTemplatesDirectory();
            File defaultDir = new File(templatesPath);
            
            if (!defaultDir.exists()) {
                defaultDir.mkdirs();
            }
            
            fileChooser.setInitialDirectory(defaultDir);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            
            if ("Word".equals(documentType)) {
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Word 文档", "*.docx"));
                fileChooser.setInitialFileName("template_" + timestamp + ".docx");
                
                File file = fileChooser.showSaveDialog(templateContentTextArea.getScene().getWindow());
                if (file != null) {
                    // 创建Word文档
                    try (XWPFDocument document = new XWPFDocument()) {
                        // 分行添加段落
                        String[] lines = templateContent.split("\n");
                        for (String line : lines) {
                            XWPFParagraph paragraph = document.createParagraph();
                            XWPFRun run = paragraph.createRun();
                            run.setText(line);
                        }
                        
                        // 保存文档
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            document.write(fos);
                        }
                        
                        // 更新状态
                        statusLabel.setText("模板已保存: " + file.getName());
                        templateFilePathTextField.setText(file.getAbsolutePath());
                    }
                }
            } else if ("Excel".equals(documentType)) {
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel 表格", "*.xlsx"));
                fileChooser.setInitialFileName("template_" + timestamp + ".xlsx");
                
                File file = fileChooser.showSaveDialog(templateContentTextArea.getScene().getWindow());
                if (file != null) {
                    // 创建Excel文档
                    try (Workbook workbook = new XSSFWorkbook()) {
                        Sheet sheet = workbook.createSheet("Template");
                        
                        // 分行添加单元格
                        String[] lines = templateContent.split("\n");
                        for (int i = 0; i < lines.length; i++) {
                            Row row = sheet.createRow(i);
                            String[] cells = lines[i].split("\t");
                            
                            for (int j = 0; j < cells.length; j++) {
                                Cell cell = row.createCell(j);
                                cell.setCellValue(cells[j]);
                            }
                        }
                        
                        // 保存文档
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            workbook.write(fos);
                        }
                        
                        // 更新状态
                        statusLabel.setText("模板已保存: " + file.getName());
                        templateFilePathTextField.setText(file.getAbsolutePath());
                    }
                }
            }
            
        } catch (Exception e) {
            AppLogger.error("生成模板失败", e);
            showAlert(AlertType.ERROR, "错误", "生成模板失败", e.getMessage());
            statusLabel.setText("错误: 生成模板失败");
        }
    }
    
    // 保存生成的文档
    private void saveDocument() {
        try {
            String documentType = documentTypeComboBox.getValue();
            
            // 创建文件选择器
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存生成的文档");
            
            // 设置默认路径和文件名
            String templatesPath = getTemplatesDirectory();
            File defaultDir = new File(templatesPath);
            
            if (!defaultDir.exists()) {
                defaultDir.mkdirs();
            }
            
            fileChooser.setInitialDirectory(defaultDir);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            
            if ("Word".equals(documentType)) {
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Word 文档", "*.docx"));
                fileChooser.setInitialFileName("document_" + timestamp + ".docx");
                
                File file = fileChooser.showSaveDialog(previewContentVBox.getScene().getWindow());
                if (file != null) {
                    // 获取预览内容
                    String previewContent = extractTextFromPreview();
                    
                    // 创建Word文档
                    try (XWPFDocument document = new XWPFDocument()) {
                        // 分行添加段落
                        String[] lines = previewContent.split("\n");
                        for (String line : lines) {
                            XWPFParagraph paragraph = document.createParagraph();
                            XWPFRun run = paragraph.createRun();
                            run.setText(line);
                        }
                        
                        // 保存文档
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            document.write(fos);
                        }
                        
                        // 更新状态
                        statusLabel.setText("文档已保存: " + file.getName());
                    }
                }
            } else if ("Excel".equals(documentType)) {
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel 表格", "*.xlsx"));
                fileChooser.setInitialFileName("document_" + timestamp + ".xlsx");
                
                File file = fileChooser.showSaveDialog(previewContentVBox.getScene().getWindow());
                if (file != null) {
                    // 创建Excel文档
                    try (Workbook workbook = new XSSFWorkbook()) {
                        Sheet sheet = workbook.createSheet("Document");
                        
                        // 创建表头
                        Row headerRow = sheet.createRow(0);
                        headerRow.createCell(0).setCellValue("占位符");
                        headerRow.createCell(1).setCellValue("值");
                        
                        // 添加数据
                        int rowIndex = 1;
                        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                            Row row = sheet.createRow(rowIndex++);
                            row.createCell(0).setCellValue(entry.getKey());
                            row.createCell(1).setCellValue(entry.getValue());
                        }
                        
                        // 自动调整列宽
                        for (int i = 0; i < 2; i++) {
                            sheet.autoSizeColumn(i);
                        }
                        
                        // 保存文档
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            workbook.write(fos);
                        }
                        
                        // 更新状态
                        statusLabel.setText("文档已保存: " + file.getName());
                    }
                }
            }
            
        } catch (Exception e) {
            AppLogger.error("保存文档失败", e);
            showAlert(AlertType.ERROR, "错误", "保存文档失败", e.getMessage());
            statusLabel.setText("错误: 保存文档失败");
        }
    }
    
    /**
     * 提取并添加占位符到字段列表
     *
     * @param content 模板内容
     */
    private void extractAndAddPlaceholders(String content) {
        // 清空当前字段列表
        fieldsListVBox.getChildren().clear();
        
        // 使用正则表达式提取占位符 - 匹配${xxx}和${xxx.yyy}两种格式
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(content);
        
        // 用于避免重复添加
        Set<String> addedPlaceholders = new HashSet<>();
        
        while (matcher.find()) {
            // 获取占位符名称（移除${}）
            String placeholder = matcher.group(1);
            
            // 如果已经添加过该占位符，则跳过
            if (addedPlaceholders.contains(placeholder)) {
                continue;
            }
            
            addedPlaceholders.add(placeholder);
            addPlaceholderToList(placeholder);
        }
        
        // 如果没有找到占位符，显示提示信息
        if (addedPlaceholders.isEmpty()) {
            Label noFieldsLabel = new Label("未找到占位符");
            noFieldsLabel.setStyle("-fx-text-fill: #999999; -fx-font-style: italic;");
            fieldsListVBox.getChildren().add(noFieldsLabel);
        }
        
        // 更新数据输入区域
        updateDataInputs();
    }
    
    /**
     * 向字段列表添加占位符项
     * 
     * @param placeholder 占位符
     */
    private void addPlaceholderToList(String placeholder) {
        // 为Lambda表达式创建一个final副本
        final String finalPlaceholder = placeholder;
        
        // 创建HBox作为字段列表项的容器
        HBox fieldItem = new HBox();
        fieldItem.setAlignment(Pos.CENTER_LEFT);
        fieldItem.setPadding(new Insets(5, 10, 5, 10));
        fieldItem.setSpacing(10);
        
        // 为字段项添加背景色和鼠标悬停效果
        fieldItem.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 4px;");
        fieldItem.setOnMouseEntered(e -> fieldItem.setStyle("-fx-background-color: #e8e8e8; -fx-border-radius: 4px;"));
        fieldItem.setOnMouseExited(e -> fieldItem.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 4px;"));
        
        // 创建标签显示占位符名称
        Label placeholderLabel = new Label(finalPlaceholder);
        placeholderLabel.setMaxWidth(Double.MAX_VALUE);
        placeholderLabel.setCursor(javafx.scene.Cursor.HAND);
        HBox.setHgrow(placeholderLabel, Priority.ALWAYS);
        
        // 检查是否为列表类型的占位符（包含点号）
        if (finalPlaceholder.contains(".")) {
            placeholderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0066cc;");
        } else {
            placeholderLabel.setStyle("-fx-text-fill: #2c3e50;");
        }
        
        // 双击事件 - 插入到编辑区
        placeholderLabel.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                insertFieldToTemplate(finalPlaceholder);
            }
        });
        
        // 创建删除按钮
        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff6b6b; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5;");
        deleteButton.setTooltip(new Tooltip("删除此占位符"));
        
        // 删除按钮点击事件
        deleteButton.setOnAction(e -> {
            // 从字段列表移除
            fieldsListVBox.getChildren().remove(fieldItem);
            
            // 从输入区域移除
            updateDataInputs();
            
            // 从模板中移除占位符
            removeFieldFromTemplate(finalPlaceholder);
        });
        
        // 将组件添加到字段项
        fieldItem.getChildren().addAll(placeholderLabel, deleteButton);
        
        // 添加到字段列表
        fieldsListVBox.getChildren().add(fieldItem);
    }
    
    /**
     * 从模板中移除字段
     * 
     * @param fieldName 要移除的字段名
     */
    private void removeFieldFromTemplate(String fieldName) {
        String placeholder = "${" + fieldName + "}";
        String content = templateContentTextArea.getText();
        
        // 替换掉所有匹配的占位符
        String newContent = content.replace(placeholder, "");
        
        // 更新模板内容
        templateContentTextArea.setText(newContent);
        
        // 更新状态
        statusLabel.setText("已移除占位符: " + fieldName);
    }
    
    /**
     * 更新数据输入区域
     */
    private void updateDataInputs() {
        // 清空当前内容
        dataInputVBox.getChildren().clear();
        dataMap.clear();
        
        // 创建标题
        Label titleLabel = new Label("数据输入：");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0 0 5 0;");
        dataInputVBox.getChildren().add(titleLabel);
        
        // 获取所有字段
        for (javafx.scene.Node node : fieldsListVBox.getChildren()) {
            if (node instanceof HBox) {
                HBox fieldItem = (HBox) node;
                
                // 获取标签节点（第一个子节点）
                javafx.scene.Node labelNode = fieldItem.getChildren().size() > 0 ? fieldItem.getChildren().get(0) : null;
                
                if (labelNode instanceof Label) {
                    String placeholder = ((Label) labelNode).getText();
                    
                    // 创建标签和输入字段
                    HBox inputRow = new HBox(10);
                    inputRow.setPadding(new Insets(5, 0, 5, 0));
                    inputRow.setAlignment(Pos.CENTER_LEFT);
                    
                    Label fieldLabel = new Label(placeholder + ":");
                    fieldLabel.setPrefWidth(150);
                    
                    TextField inputField = new TextField();
                    inputField.setPromptText("输入" + placeholder + "的值");
                    HBox.setHgrow(inputField, Priority.ALWAYS);
                    
                    // 文本变化监听
                    final String fieldName = placeholder;
                    inputField.textProperty().addListener((obs, oldVal, newVal) -> {
                        dataMap.put(fieldName, newVal);
                        updatePreviewWithData();
                    });
                    
                    inputRow.getChildren().addAll(fieldLabel, inputField);
                    dataInputVBox.getChildren().add(inputRow);
                    
                    // 初始化数据映射
                    dataMap.put(placeholder, "");
                }
            }
        }
        
        // 如果没有字段，显示提示信息
        if (dataMap.isEmpty() && fieldsListVBox.getChildren().size() <= 1) {
            Label noInputLabel = new Label("请先从模板中提取字段或手动添加字段");
            noInputLabel.setStyle("-fx-text-fill: #999999; -fx-font-style: italic;");
            dataInputVBox.getChildren().add(noInputLabel);
        }
        
        // 更新预览
        updatePreviewWithData();
    }
    
    /**
     * 更新预览内容，使用数据填充占位符
     */
    private void updatePreviewWithData() {
        String documentType = documentTypeComboBox.getValue();
        
        if ("Word".equals(documentType)) {
            updateWordPreview();
        } else if ("Excel".equals(documentType)) {
            // Excel预览在loadExcelTemplateContent方法中已处理
            // 这里可以添加更新Excel预览的逻辑
        }
    }
    
    /**
     * 更新Word文档预览
     */
    private void updateWordPreview() {
        // 获取模板内容
        String templateText = templateContentTextArea.getText();
        
        // 清空预览区域
        previewContentVBox.getChildren().clear();
        
        // 创建富文本流
        TextFlow textFlow = new TextFlow();
        
        // 解析内容，替换占位符
        int lastIndex = 0;
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(templateText);
        
        while (matcher.find()) {
            // 添加占位符前的普通文本
            if (matcher.start() > lastIndex) {
                Text normalText = new Text(templateText.substring(lastIndex, matcher.start()));
                textFlow.getChildren().add(normalText);
            }
            
            // 获取占位符名称
            String placeholder = matcher.group(1);
            
            // 获取占位符对应的数据值，如果没有则用空字符串
            String value = dataMap.getOrDefault(placeholder, "");
            
            if (!value.isEmpty()) {
                // 如果有数据，显示数据值
                Text valueText = new Text(value);
                valueText.setStyle("-fx-fill: #2c3e50;");
                textFlow.getChildren().add(valueText);
            } else {
                // 如果没有数据，显示占位符（灰色斜体）
                Text emptyText = new Text("(" + placeholder + ")");
                emptyText.setStyle("-fx-fill: #999999; -fx-font-style: italic;");
                textFlow.getChildren().add(emptyText);
            }
            
            lastIndex = matcher.end();
        }
        
        // 添加最后一部分普通文本
        if (lastIndex < templateText.length()) {
            Text normalText = new Text(templateText.substring(lastIndex));
            textFlow.getChildren().add(normalText);
        }
        
        // 添加到预览区域
        previewContentVBox.getChildren().add(textFlow);
    }
    
    /**
     * 插入字段到模板方法
     *
     * @param placeholder 占位符
     */
    private void insertFieldToTemplate(String placeholder) {
        // 生成占位符格式
        String formattedPlaceholder = "${" + placeholder + "}";
        
        // 在当前光标位置插入占位符
        int caretPosition = templateContentTextArea.getCaretPosition();
        templateContentTextArea.insertText(caretPosition, formattedPlaceholder);
        
        // 重新设置光标位置到插入后的位置
        templateContentTextArea.positionCaret(caretPosition + formattedPlaceholder.length());
        
        // 更新状态
        statusLabel.setText("已插入占位符: " + placeholder);
        
        // 重新提取占位符以更新字段列表
        extractAndAddPlaceholders(templateContentTextArea.getText());
    }
    
    /**
     * 添加普通占位符
     */
    private void addNormalPlaceholder() {
        String placeholder = normalPlaceholderTextField.getText().trim();
        
        if (placeholder.isEmpty()) {
            showAlert(AlertType.WARNING, "警告", "占位符为空", "请输入占位符名称");
            return;
        }
        
        // 如果包含.，则应使用列表占位符
        if (placeholder.contains(".")) {
            showAlert(AlertType.WARNING, "警告", "格式错误", "普通占位符不应包含点号(.)，请使用列表占位符");
            return;
        }
        
        // 检查是否已存在
        boolean exists = false;
        for (javafx.scene.Node node : fieldsListVBox.getChildren()) {
            if (node instanceof HBox) {
                HBox fieldItem = (HBox) node;
                for (javafx.scene.Node child : fieldItem.getChildren()) {
                    if (child instanceof Label && ((Label) child).getText().equals(placeholder)) {
                        exists = true;
                        break;
                    }
                }
            }
            if (exists) break;
        }
        
        if (exists) {
            showAlert(AlertType.WARNING, "警告", "占位符已存在", "此占位符已在列表中");
            return;
        }
        
        // 创建字段项
        HBox fieldItem = new HBox(5);
        fieldItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        fieldItem.setPadding(new Insets(3, 5, 3, 5));
        fieldItem.setStyle("-fx-border-color: #EEEEEE; -fx-border-radius: 3; -fx-background-color: white;");
        
        Label fieldLabel = new Label(placeholder);
        fieldLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(fieldLabel, Priority.ALWAYS);
        fieldLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        // 双击可以插入到模板
        fieldLabel.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                insertFieldToTemplate(placeholder);
            }
        });
        
        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff6b6b; -fx-cursor: hand; -fx-padding: 0 5;");
        deleteButton.setTooltip(new Tooltip("删除字段"));
        deleteButton.setOnAction(e -> {
            fieldsListVBox.getChildren().remove(fieldItem);
            updateDataInputs();
        });
        
        fieldItem.getChildren().addAll(fieldLabel, deleteButton);
        fieldsListVBox.getChildren().add(fieldItem);
        
        // 清空输入框
        normalPlaceholderTextField.clear();
        
        // 更新数据输入区域
        updateDataInputs();
        
        // 更新状态
        statusLabel.setText("已添加占位符: " + placeholder);
    }
    
    /**
     * 添加列表占位符
     */
    private void addListPlaceholder() {
        String listName = listNameTextField.getText().trim();
        String propertyName = listPropertyTextField.getText().trim();
        
        if (listName.isEmpty() || propertyName.isEmpty()) {
            showAlert(AlertType.WARNING, "警告", "占位符不完整", "列表名称和属性名称均不能为空");
            return;
        }
        
        String placeholder = listName + "." + propertyName;
        
        // 检查是否已存在
        boolean exists = false;
        for (javafx.scene.Node node : fieldsListVBox.getChildren()) {
            if (node instanceof HBox) {
                HBox fieldItem = (HBox) node;
                for (javafx.scene.Node child : fieldItem.getChildren()) {
                    if (child instanceof Label && ((Label) child).getText().equals(placeholder)) {
                        exists = true;
                        break;
                    }
                }
            }
            if (exists) break;
        }
        
        if (exists) {
            showAlert(AlertType.WARNING, "警告", "占位符已存在", "此占位符已在列表中");
            return;
        }
        
        // 创建字段项
        HBox fieldItem = new HBox(5);
        fieldItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        fieldItem.setPadding(new Insets(3, 5, 3, 5));
        fieldItem.setStyle("-fx-border-color: #EEEEEE; -fx-border-radius: 3; -fx-background-color: white;");
        
        Label fieldLabel = new Label(placeholder);
        fieldLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(fieldLabel, Priority.ALWAYS);
        fieldLabel.setStyle("-fx-text-fill: #3498db;"); // 列表占位符用蓝色
        
        // 双击可以插入到模板
        fieldLabel.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                insertFieldToTemplate(placeholder);
            }
        });
        
        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff6b6b; -fx-cursor: hand; -fx-padding: 0 5;");
        deleteButton.setTooltip(new Tooltip("删除字段"));
        deleteButton.setOnAction(e -> {
            fieldsListVBox.getChildren().remove(fieldItem);
            updateDataInputs();
        });
        
        fieldItem.getChildren().addAll(fieldLabel, deleteButton);
        fieldsListVBox.getChildren().add(fieldItem);
        
        // 清空属性名称输入框
        listPropertyTextField.clear();
        
        // 更新数据输入区域
        updateDataInputs();
        
        // 更新状态
        statusLabel.setText("已添加列表占位符: " + placeholder);
    }
    
    /**
     * 从预览中提取文本
     */
    private String extractTextFromPreview() {
        StringBuilder content = new StringBuilder();
        for (javafx.scene.Node node : previewContentVBox.getChildren()) {
            if (node instanceof TextFlow) {
                TextFlow textFlow = (TextFlow) node;
                for (javafx.scene.Node child : textFlow.getChildren()) {
                    if (child instanceof Text) {
                        Text text = (Text) child;
                        content.append(text.getText()).append("\n");
                    }
                }
            }
        }
        return content.toString();
    }
    
    /**
     * 保留原有的updateTemplateFileExtensions方法
     */
    private void updateTemplateFileExtensions() {
        // 根据选择的文档类型更新文件过滤器
        if (documentTypeComboBox.getValue() != null) {
            String documentType = documentTypeComboBox.getValue();
            if ("Word".equals(documentType)) {
                templateFilePathTextField.setPromptText("选择Word模板文件(.docx)");
            } else if ("Excel".equals(documentType)) {
                templateFilePathTextField.setPromptText("选择Excel模板文件(.xlsx)");
            }
        }
    }
    
    /**
     * 获取应用程序当前目录下的模板文件夹
     * @return 模板文件夹的完整路径
     */
    private String getTemplatesDirectory() {
        // 获取应用程序所在目录
        String appDir = System.getProperty("user.dir");
        
        // 如果是在IDE中运行，可能需要向上一级目录查找templates
        File templatesDir = new File(appDir, DEFAULT_TEMPLATES_DIR);
        if (!templatesDir.exists()) {
            // 尝试在上一级目录查找
            templatesDir = new File(new File(appDir).getParent(), DEFAULT_TEMPLATES_DIR);
        }

        // 如果仍然找不到，尝试创建目录
        if (!templatesDir.exists()) {
            templatesDir.mkdirs();
            AppLogger.info("已创建模板目录: " + templatesDir.getAbsolutePath());
        }
        
        return templatesDir.getAbsolutePath();
    }
    
    /**
     * 更新预览内容
     * 
     * @param content 模板内容
     */
    private void updatePreview(String content) {
        // 清空现有预览
        previewContentVBox.getChildren().clear();
        
        // 处理预览 - 根据文档类型有不同的显示方式
        String documentType = documentTypeComboBox.getValue();
        
        if ("Word".equals(documentType)) {
            // 使用TextFlow以保持格式
            TextFlow textFlow = new TextFlow();
            
            // 解析内容，高亮显示占位符
            int lastIndex = 0;
            
            // 使用正则表达式匹配${xxx}格式的占位符
            Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
            Matcher matcher = pattern.matcher(content);
            
            while (matcher.find()) {
                // 添加占位符前的普通文本
                if (matcher.start() > lastIndex) {
                    Text normalText = new Text(content.substring(lastIndex, matcher.start()));
                    normalText.setStyle("-fx-fill: black;");
                    textFlow.getChildren().add(normalText);
                }
                
                // 添加高亮的占位符
                Text placeholderText = new Text(matcher.group());
                placeholderText.setStyle("-fx-fill: #0066cc; -fx-font-weight: bold;");
                textFlow.getChildren().add(placeholderText);
                
                lastIndex = matcher.end();
            }
            
            // 添加最后一部分普通文本
            if (lastIndex < content.length()) {
                Text normalText = new Text(content.substring(lastIndex));
                normalText.setStyle("-fx-fill: black;");
                textFlow.getChildren().add(normalText);
            }
            
            // 添加到预览区域
            previewContentVBox.getChildren().add(textFlow);
        }
    }
    
    /**
     * 获取单元格值为字符串
     *
     * @param cell Excel单元格
     * @return 单元格的字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        return sdf.format(cell.getDateCellValue());
                    } catch (Exception e) {
                        return String.valueOf(cell.getNumericCellValue());
                    }
                } else {
                    // 对于数字，避免显示科学计数法
                    DecimalFormat df = new DecimalFormat("0.####"); // 保留最多4位小数
                    return df.format(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception ex) {
                        return "#ERROR!";
                    }
                }
            default:
                return "";
        }
    }
} 