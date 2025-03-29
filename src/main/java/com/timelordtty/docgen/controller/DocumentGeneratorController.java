package com.timelordtty.docgen.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.timelordtty.AppLogger;
import com.timelordtty.docgen.service.DocGeneratorService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * 文档生成控制器
 * 处理模板编辑和文档生成的功能
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
    private Button saveTemplateButton;
    
    @FXML
    private Button generateTemplateButton;
    
    @FXML
    private Button saveDocumentButton;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private ListView<String> availableFieldsListView;
    
    @FXML
    private TextArea templateContentTextArea;
    
    @FXML
    private GridPane dataInputGridPane;
    
    @FXML
    private ScrollPane previewScrollPane;
    
    @FXML
    private VBox previewContentVBox;
    
    // 新增的UI组件
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
    
    // 默认模板目录（相对路径）
    private final String DEFAULT_TEMPLATES_DIR = "templates";
    
    // 文档生成服务
    private DocGeneratorService docService;
    
    // 占位符正则
    private final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}|\\{\\{([^}]+)\\}\\}");
    
    // 模板中检测到的字段列表
    private ObservableList<String> templateFields = FXCollections.observableArrayList();
    
    // 填写的数据
    private Map<String, Object> dataValues = new HashMap<>();
    
    @FXML
    private VBox fieldsListVBox;
    
    @FXML
    private VBox dataInputVBox;
    
    @FXML
    private void initialize() {
        // 初始化文档生成服务
        docService = new DocGeneratorService();
        
        // 初始化文档类型下拉框
        documentTypeComboBox.getItems().addAll("Word", "Excel");
        documentTypeComboBox.setValue("Word");
        
        documentTypeComboBox.setOnAction(event -> {
            updateTemplateFileExtensions();
        });
        
        // 设置状态标签初始文本
        statusLabel.setText("就绪");
        
        // 初始化可用字段列表
        templateFields = FXCollections.observableArrayList();
        
        // 初始化按钮事件
        saveTemplateButton.setOnAction(event -> saveTemplate());
        generateTemplateButton.setOnAction(event -> generateTemplate());
        saveDocumentButton.setOnAction(event -> saveDocument());
        
        // 初始化新增按钮的事件处理
        addNormalPlaceholderButton.setOnAction(event -> addNormalPlaceholder());
        addListPlaceholderButton.setOnAction(event -> addListPlaceholder());
        
        // 回车键提交
        normalPlaceholderTextField.setOnAction(event -> addNormalPlaceholder());
        listPropertyTextField.setOnAction(event -> addListPlaceholder());
        
        updateTemplateFileExtensions();
    }
    
    /**
     * 更新字段列表显示
     */
    private void updateFieldsList() {
        fieldsListVBox.getChildren().clear();
        
        for (String fieldName : templateFields) {
            HBox fieldItem = createFieldListItem(fieldName);
            fieldsListVBox.getChildren().add(fieldItem);
        }
        
        // 更新数据输入表单
        updateDataInputForm();
    }
    
    /**
     * 创建字段列表项
     * @param fieldName 字段名称
     * @return 包含字段名和删除按钮的HBox
     */
    private HBox createFieldListItem(String fieldName) {
        HBox fieldItem = new HBox();
        fieldItem.setSpacing(5);
        fieldItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        fieldItem.setPadding(new Insets(3, 5, 3, 5));
        fieldItem.setStyle("-fx-border-color: #EEEEEE; -fx-border-radius: 3; -fx-background-color: white;");
        
        Label fieldLabel = new Label(fieldName);
        fieldLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(fieldLabel, Priority.ALWAYS);
        fieldLabel.setTooltip(new Tooltip("双击插入占位符"));
        
        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff6b6b; -fx-cursor: hand; -fx-padding: 0 5;");
        deleteButton.setTooltip(new Tooltip("删除字段"));
        deleteButton.setOnAction(e -> {
            templateFields.remove(fieldName);
            updateFieldsList();
            statusLabel.setText("已删除字段: " + fieldName);
        });
        
        // 双击字段插入占位符
        fieldLabel.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                insertFieldToTemplate(fieldName);
            }
        });
        
        fieldItem.getChildren().addAll(fieldLabel, deleteButton);
        return fieldItem;
    }
    
    /**
     * 添加普通占位符
     */
    private void addNormalPlaceholder() {
        String placeholderName = normalPlaceholderTextField.getText().trim();
        if (!placeholderName.isEmpty()) {
            if (!templateFields.contains(placeholderName)) {
                templateFields.add(placeholderName);
                normalPlaceholderTextField.clear();
                updateFieldsList();
                statusLabel.setText("已添加占位符: " + placeholderName);
            } else {
                showAlert(AlertType.WARNING, "警告", "重复的占位符", "占位符 '" + placeholderName + "' 已存在");
            }
        }
    }
    
    /**
     * 添加列表占位符
     */
    private void addListPlaceholder() {
        String listName = listNameTextField.getText().trim();
        String propertyName = listPropertyTextField.getText().trim();
        
        if (!listName.isEmpty() && !propertyName.isEmpty()) {
            String placeholderName = listName + "." + propertyName;
            if (!templateFields.contains(placeholderName)) {
                templateFields.add(placeholderName);
                listPropertyTextField.clear();
                updateFieldsList();
                statusLabel.setText("已添加列表占位符: " + placeholderName);
            } else {
                showAlert(AlertType.WARNING, "警告", "重复的占位符", "占位符 '" + placeholderName + "' 已存在");
            }
        } else {
            if (listName.isEmpty()) {
                showAlert(AlertType.WARNING, "警告", "列表名称为空", "请输入列表名称");
            } else {
                showAlert(AlertType.WARNING, "警告", "属性名称为空", "请输入属性名称");
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
    
    @FXML
    private void chooseTemplateFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择模板文件");
        
        // 获取模板目录
        String templatesPath = getTemplatesDirectory();
        File defaultDir = new File(templatesPath);
        if (!defaultDir.exists()) {
            defaultDir.mkdirs();
            AppLogger.info("已创建默认模板目录：" + templatesPath);
        }
        
        String documentType = documentTypeComboBox.getValue();
        String subDir = null;
        if ("Word".equals(documentType)) {
            subDir = "word";
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Word 文档", "*.docx"));
        } else if ("Excel".equals(documentType)) {
            subDir = "excel";
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel 表格", "*.xlsx"));
        }
        
        // 如果有子目录且存在，则打开子目录
        if (subDir != null) {
            File typeDir = new File(defaultDir, subDir);
            if (typeDir.exists() && typeDir.isDirectory()) {
                fileChooser.setInitialDirectory(typeDir);
            } else {
                fileChooser.setInitialDirectory(defaultDir);
            }
        } else {
            fileChooser.setInitialDirectory(defaultDir);
        }
        
        File selectedFile = fileChooser.showOpenDialog(chooseTemplateFileButton.getScene().getWindow());
        if (selectedFile != null) {
            templateFilePathTextField.setText(selectedFile.getAbsolutePath());
            loadTemplateContent(selectedFile);
            statusLabel.setText("已选择模板文件: " + selectedFile.getName());
            AppLogger.info("已选择模板文件：" + selectedFile.getAbsolutePath());
        }
    }
    
    @FXML
    private void chooseDataFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择数据文件");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Excel 文件", "*.xlsx"),
            new ExtensionFilter("所有文件", "*.*")
        );
        
        // 设置默认目录为templates目录
        String templatesPath = getTemplatesDirectory();
        File defaultDir = new File(templatesPath);
        if (defaultDir.exists() && defaultDir.isDirectory()) {
            fileChooser.setInitialDirectory(defaultDir);
        } else {
            defaultDir.mkdirs();
            AppLogger.info("已创建默认模板目录：" + templatesPath);
            fileChooser.setInitialDirectory(defaultDir);
        }
        
        File selectedFile = fileChooser.showOpenDialog(chooseDataFileButton.getScene().getWindow());
        if (selectedFile != null) {
            dataFilePathTextField.setText(selectedFile.getAbsolutePath());
            loadDataFile(selectedFile);
            statusLabel.setText("已选择数据文件: " + selectedFile.getName());
            AppLogger.info("已选择数据文件：" + selectedFile.getAbsolutePath());
        }
    }
    
    /**
     * 将选中的字段插入到模板内容中
     * @param fieldName 字段名
     */
    private void insertFieldToTemplate(String fieldName) {
        String placeholder = "${" + fieldName + "}";
        int caretPosition = templateContentTextArea.getCaretPosition();
        String currentText = templateContentTextArea.getText();
        
        // 在光标位置插入占位符
        String newText = currentText.substring(0, caretPosition) + placeholder + currentText.substring(caretPosition);
        templateContentTextArea.setText(newText);
        templateContentTextArea.positionCaret(caretPosition + placeholder.length());
        
        // 更新状态
        statusLabel.setText("已插入字段: " + fieldName);
    }
    
    /**
     * 从模板文件中提取占位符
     * @param templateText 模板文本内容
     * @return 占位符列表
     */
    private List<String> extractPlaceholders(String templateText) {
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateText);
        
        while (matcher.find()) {
            String placeholder = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (!placeholders.contains(placeholder)) {
                placeholders.add(placeholder);
            }
        }
        
        return placeholders;
    }
    
    /**
     * 加载模板内容
     * @param templateFile 模板文件
     */
    private void loadTemplateContent(File templateFile) {
        try {
            String documentType = documentTypeComboBox.getValue();
            String templateText = "";
            List<String> placeholders = new ArrayList<>();
            
            if ("Word".equals(documentType)) {
                // 读取Word文档内容
                try (FileInputStream fis = new FileInputStream(templateFile);
                     XWPFDocument document = new XWPFDocument(fis)) {
                    
                    StringBuilder content = new StringBuilder();
                    for (XWPFParagraph paragraph : document.getParagraphs()) {
                        content.append(paragraph.getText()).append("\n");
                    }
                    
                    templateText = content.toString();
                    placeholders = extractPlaceholders(templateText);
                }
            } else if ("Excel".equals(documentType)) {
                // 读取Excel文件内容
                try (FileInputStream fis = new FileInputStream(templateFile);
                     Workbook workbook = new XSSFWorkbook(fis)) {
                    
                    StringBuilder content = new StringBuilder();
                    Sheet sheet = workbook.getSheetAt(0);
                    
                    for (Row row : sheet) {
                        StringBuilder rowContent = new StringBuilder();
                        for (Cell cell : row) {
                            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                                String cellValue = cell.getStringCellValue();
                                rowContent.append(cellValue).append("\t");
                                
                                // 提取占位符
                                Matcher matcher = PLACEHOLDER_PATTERN.matcher(cellValue);
                                while (matcher.find()) {
                                    String placeholder = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                                    if (!placeholders.contains(placeholder)) {
                                        placeholders.add(placeholder);
                                    }
                                }
                            } else {
                                rowContent.append("\t");
                            }
                        }
                        content.append(rowContent.toString().trim()).append("\n");
                    }
                    
                    templateText = content.toString();
                }
            }
            
            // 更新UI
            templateContentTextArea.setText(templateText);
            templateFields.clear();
            templateFields.addAll(placeholders);
            
            // 加载模板时，自动设置列表名称为发现的第一个列表名称
            for (String field : placeholders) {
                if (field.contains(".")) {
                    String listName = field.split("\\.")[0];
                    listNameTextField.setText(listName);
                    break;
                }
            }
            
            // 更新字段列表和数据输入表单
            updateFieldsList();
            
            // 更新状态
            statusLabel.setText("已加载模板文件: " + templateFile.getName());
            
        } catch (Exception e) {
            AppLogger.error("加载模板文件失败", e);
            showAlert(AlertType.ERROR, "错误", "加载模板失败", "无法读取模板文件: " + e.getMessage());
            statusLabel.setText("错误: 加载模板失败");
        }
    }
    
    /**
     * 更新数据输入表单
     */
    private void updateDataInputForm() {
        dataInputVBox.getChildren().clear();
        
        int row = 0;
        for (String fieldName : templateFields) {
            HBox inputRow = new HBox();
            inputRow.setSpacing(10);
            inputRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            // 字段名标签
            Label fieldLabel = new Label(fieldName + ":");
            fieldLabel.setPrefWidth(200);
            fieldLabel.setMinWidth(150);
            
            // 文本输入框
            TextField valueField = new TextField();
            valueField.setPrefWidth(250);
            HBox.setHgrow(valueField, Priority.ALWAYS);
            
            // 如果已经有值，填充进去
            if (dataValues.containsKey(fieldName)) {
                valueField.setText(dataValues.get(fieldName).toString());
            }
            
            // 监听值变化
            valueField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.isEmpty()) {
                    dataValues.remove(fieldName);
                } else {
                    dataValues.put(fieldName, newVal);
                }
                updatePreview();
            });
            
            inputRow.getChildren().addAll(fieldLabel, valueField);
            dataInputVBox.getChildren().add(inputRow);
            row++;
        }
        
        // 更新预览
        updatePreview();
    }
    
    /**
     * 更新预览内容
     */
    private void updatePreview() {
        previewContentVBox.getChildren().clear();
        
        if (templateContentTextArea.getText().isEmpty()) {
            Label emptyLabel = new Label("请编辑模板内容");
            emptyLabel.setStyle("-fx-text-fill: #999999; -fx-font-style: italic;");
            previewContentVBox.getChildren().add(emptyLabel);
            return;
        }
        
        String templateContent = templateContentTextArea.getText();
        String previewContent = replaceTemplatePlaceholders(templateContent, dataValues);
        
        // 分行显示
        String[] lines = previewContent.split("\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                Label lineLabel = new Label(line);
                lineLabel.setWrapText(true);
                lineLabel.setPrefWidth(previewScrollPane.getWidth() - 20);
                lineLabel.setPadding(new Insets(2, 0, 2, 0));
                previewContentVBox.getChildren().add(lineLabel);
            }
        }
    }
    
    /**
     * 替换模板中的占位符
     * @param template 模板内容
     * @param dataMap 数据映射
     * @return 替换后的内容
     */
    private String replaceTemplatePlaceholders(String template, Map<String, Object> dataMap) {
        String result = template;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            String replacement = "";
            
            // 处理对象列表形式的占位符 (如: 列表.属性)
            if (placeholder.contains(".")) {
                String[] parts = placeholder.split("\\.", 2);
                String listName = parts[0];
                String propertyName = parts[1];
                
                // 检查是否有相应的列表数据
                if (isListTypePlaceholder(listName)) {
                    Object listValue = getListItemValue(listName, propertyName);
                    if (listValue != null) {
                        replacement = listValue.toString();
                    }
                } else {
                    Object value = dataMap.get(placeholder);
                    if (value != null) {
                        replacement = value.toString();
                    }
                }
            } else {
                // 普通占位符
                Object value = dataMap.get(placeholder);
                if (value != null) {
                    replacement = value.toString();
                }
            }
            
            // 执行替换
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    /**
     * 检查是否是列表类型的占位符
     * @param listName 列表名
     * @return 是否是列表类型
     */
    private boolean isListTypePlaceholder(String listName) {
        // 检查是否有以listName为前缀的占位符
        int count = 0;
        for (String field : templateFields) {
            if (field.startsWith(listName + ".")) {
                count++;
            }
        }
        return count > 1;
    }
    
    /**
     * 从列表中获取对应属性的值
     * @param listName 列表名
     * @param propertyName 属性名
     * @return 属性值
     */
    private Object getListItemValue(String listName, String propertyName) {
        // 实现列表处理逻辑
        // 这里简化处理，实际应根据数据结构调整
        String listKey = listName + "." + propertyName;
        if (dataValues.containsKey(listKey)) {
            return dataValues.get(listKey);
        }
        return null;
    }
    
    /**
     * 从Excel数据文件中加载数据
     * @param dataFile 数据文件
     */
    private void loadDataFile(File dataFile) {
        try {
            try (FileInputStream fis = new FileInputStream(dataFile);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                Sheet sheet = workbook.getSheetAt(0);
                Map<String, String> excelData = new HashMap<>();
                
                // 遍历所有行
                for (Row row : sheet) {
                    // 第一列是占位符名，其余列是值
                    if (row.getCell(0) != null && row.getCell(0).getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                        String placeholder = row.getCell(0).getStringCellValue().trim();
                        
                        // 第二列是值
                        if (row.getCell(1) != null) {
                            String value = "";
                            if (row.getCell(1).getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                                value = row.getCell(1).getStringCellValue();
                            } else if (row.getCell(1).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                                value = String.valueOf(row.getCell(1).getNumericCellValue());
                            }
                            
                            if (!value.isEmpty()) {
                                // 将数据添加到Map中
                                excelData.put(placeholder, value);
                                
                                // 如果是列表数据，确保占位符存在于字段列表中
                                if (placeholder.contains(".") && !templateFields.contains(placeholder)) {
                                    templateFields.add(placeholder);
                                    
                                    // 提取列表名
                                    String listName = placeholder.split("\\.")[0];
                                    listNameTextField.setText(listName);
                                }
                            }
                        }
                    }
                }
                
                // 更新数据值
                dataValues.clear();
                for (Map.Entry<String, String> entry : excelData.entrySet()) {
                    dataValues.put(entry.getKey(), entry.getValue());
                }
                
                // 更新UI
                updateFieldsList();
                
                // 更新状态
                statusLabel.setText("已加载数据文件: " + dataFile.getName());
            }
            
        } catch (Exception e) {
            AppLogger.error("加载数据文件失败", e);
            showAlert(AlertType.ERROR, "错误", "加载数据失败", "无法读取数据文件: " + e.getMessage());
            statusLabel.setText("错误: 加载数据失败");
        }
    }
    
    /**
     * 生成模板文件
     */
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
    
    /**
     * 保存模板
     */
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
    
    /**
     * 保存生成的文档
     */
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
                    // 创建Word文档
                    try (XWPFDocument document = new XWPFDocument()) {
                        // 获取预览内容并分行添加段落
                        StringBuilder previewContent = new StringBuilder();
                        for (javafx.scene.Node node : previewContentVBox.getChildren()) {
                            if (node instanceof Label) {
                                previewContent.append(((Label) node).getText()).append("\n");
                            }
                        }
                        
                        String[] lines = previewContent.toString().split("\n");
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
                        
                        // 获取预览内容并分行添加单元格
                        StringBuilder previewContent = new StringBuilder();
                        for (javafx.scene.Node node : previewContentVBox.getChildren()) {
                            if (node instanceof Label) {
                                previewContent.append(((Label) node).getText()).append("\n");
                            }
                        }
                        
                        String[] lines = previewContent.toString().split("\n");
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
    
    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 