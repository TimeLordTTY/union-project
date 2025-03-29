package com.timelordtty.docgen.controller;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.timelordtty.AppLogger;
import com.timelordtty.docgen.model.TemplateField;
import com.timelordtty.docgen.service.ExcelTemplateService;
import com.timelordtty.docgen.service.WordTemplateService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * 文档生成器控制器
 */
public class DocumentGeneratorController {

    // FXML控件
    @FXML private TabPane tabPane;
    
    // 模板编辑标签页
    @FXML private RadioButton wordRadioButton;
    @FXML private RadioButton excelRadioButton;
    @FXML private TextField objectFieldInput;
    @FXML private TextField listFieldInput;
    @FXML private TextField listItemFieldInput;
    @FXML private ListView<TemplateField> fieldListView;
    @FXML private TextArea wordEditor;
    @FXML private TableView<ObservableList<String>> excelEditor;
    @FXML private TextArea previewArea;
    
    // 数据录入标签页
    @FXML private ComboBox<String> templateComboBox;
    @FXML private TableView<ObservableList<String>> dataTableView;
    
    // 私有字段
    private ObservableList<TemplateField> fields = FXCollections.observableArrayList();
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
        
        // 初始化字段列表视图
        fieldListView.setItems(fields);
        fieldListView.setCellFactory(param -> new FieldListCell());
        
        // 设置WordEditor
        wordEditor.setWrapText(true);
        
        // 初始化Excel编辑器
        setupExcelEditor();
        
        // 初始化数据表格
        setupDataTableView();
        
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
     * 设置数据表格视图
     */
    private void setupDataTableView() {
        // 设置可编辑
        dataTableView.setEditable(true);
        
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addRowItem = new MenuItem("添加行");
        addRowItem.setOnAction(e -> handleAddRow());
        MenuItem deleteRowItem = new MenuItem("删除选中行");
        deleteRowItem.setOnAction(e -> handleDeleteRow());
        MenuItem copyRowItem = new MenuItem("复制行");
        copyRowItem.setOnAction(e -> copyRow());
        MenuItem pasteRowItem = new MenuItem("粘贴行");
        pasteRowItem.setOnAction(e -> pasteRow());
        
        contextMenu.getItems().addAll(addRowItem, deleteRowItem, copyRowItem, pasteRowItem);
        dataTableView.setContextMenu(contextMenu);
        
        // 添加键盘快捷键
        dataTableView.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                copyRow();
            } else if (event.isControlDown() && event.getCode() == KeyCode.V) {
                pasteRow();
            } else if (event.getCode() == KeyCode.DELETE) {
                handleDeleteRow();
            }
        });
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
     * 复制选中的行
     */
    private void copyRow() {
        ObservableList<String> selectedRow = dataTableView.getSelectionModel().getSelectedItem();
        if (selectedRow != null) {
            StringBuilder content = new StringBuilder();
            for (String cell : selectedRow) {
                content.append(cell).append("\t");
            }
            
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(content.toString().trim());
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        }
    }
    
    /**
     * 粘贴行
     */
    private void pasteRow() {
        String content = Clipboard.getSystemClipboard().getString();
        if (content != null && !content.isEmpty()) {
            String[] cells = content.split("\t");
            ObservableList<String> newRow = FXCollections.observableArrayList(cells);
            
            // 确保有足够的列
            for (int i = 0; i < dataTableView.getColumns().size(); i++) {
                if (i >= newRow.size()) {
                    newRow.add("");
                }
            }
            
            dataTableView.getItems().add(newRow);
        }
    }
    
    /**
     * 处理模板类型变更
     */
    @FXML
    private void handleTemplateTypeChange() {
        isWordMode = wordRadioButton.isSelected();
        
        // 切换编辑器可见性
        wordEditor.setVisible(isWordMode);
        excelEditor.setVisible(!isWordMode);
        
        // 清空当前编辑内容
        if (isWordMode) {
            wordEditor.clear();
        } else {
            excelEditor.getItems().clear();
            addExcelRow();
        }
        
        AppLogger.info("模板类型切换为: " + (isWordMode ? "Word" : "Excel"));
    }
    
    /**
     * 处理上传模板
     */
    @FXML
    private void handleUploadTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择模板文件");
        
        // 根据当前选择的模板类型设置默认目录
        File defaultDir;
        if (isWordMode) {
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Word文档", "*.docx"));
            defaultDir = new File(baseDir, "templates/word");
        } else {
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel表格", "*.xlsx"));
            defaultDir = new File(baseDir, "templates/excel");
        }
        
        if (!defaultDir.exists()) {
            defaultDir.mkdirs();
        }
        fileChooser.setInitialDirectory(defaultDir);
        
        File file = fileChooser.showOpenDialog(wordEditor.getScene().getWindow());
        if (file != null) {
            try {
                currentTemplateFile = file;
                
                if (isWordMode) {
                    String content = wordTemplateService.readDocxContent(file.getPath());
                    wordEditor.setText(content);
                    // 提取字段
                    extractFields(content);
                } else {
                    List<List<String>> data = excelTemplateService.readExcelContent(file.getPath());
                    loadExcelData(data);
                    // 从Excel提取可能的字段
                    extractFieldsFromExcel(data);
                }
                
                // 更新数据表格
                updateDataTableView();
                
                // 更新预览
                updatePreview();
                
                AppLogger.info("成功上传模板: " + file.getName());
            } catch (Exception e) {
                AppLogger.error("上传模板失败: " + e.getMessage(), e);
                showError("上传失败", "无法读取所选文件: " + e.getMessage());
            }
        }
    }
    
    /**
     * 从文本中提取字段
     */
    private void extractFields(String content) {
        // 清空现有字段
        fields.clear();
        
        // 查找所有{{字段}}模式
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{([^{}]+)\\}\\}");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            // 忽略#和/开头的字段，这些是列表的开始和结束标记
            if (!fieldName.startsWith("#") && !fieldName.startsWith("/")) {
                // 检查是否是列表字段
                boolean isList = content.contains("{{#" + fieldName + "}}") && content.contains("{{/" + fieldName + "}}");
                fields.add(new TemplateField(fieldName, isList));
            }
        }
        
        AppLogger.info("从模板中提取了 " + fields.size() + " 个字段");
    }
    
    /**
     * 从Excel中提取字段
     */
    private void extractFieldsFromExcel(List<List<String>> data) {
        // 清空现有字段
        fields.clear();
        
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
                        fields.add(new TemplateField(fieldName, false));
                    } else {
                        // 没有{{}}格式，将整个标题作为字段名
                        fields.add(new TemplateField(header, false));
                    }
                }
            }
        }
        
        AppLogger.info("从Excel模板中提取了 " + fields.size() + " 个字段");
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
            for (TemplateField field : fields) {
                if (field.getName().equals(fieldName)) {
                    exists = true;
                    break;
                }
            }
            
            if (exists) {
                showError("添加失败", "字段 '" + fieldName + "' 已存在");
                return;
            }
            
            fields.add(new TemplateField(fieldName, false));
            objectFieldInput.clear();
            
            // 更新数据表格
            updateDataTableView();
            
            AppLogger.info("添加对象字段: " + fieldName);
        }
    }
    
    /**
     * 添加列表字段
     */
    @FXML
    private void handleAddListField() {
        String fieldName = listFieldInput.getText().trim();
        String itemFieldName = listItemFieldInput.getText().trim();
        
        if (fieldName.isEmpty()) {
            showError("添加失败", "请输入列表字段名称");
            return;
        }
        
        // 检查字段是否已存在
        boolean exists = false;
        for (TemplateField field : fields) {
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
        fields.add(new TemplateField(fieldName, true));
        
        // 如果有列表项字段，也添加
        if (!itemFieldName.isEmpty()) {
            // 添加列表项字段，格式为：listName.itemName
            String combinedName = fieldName + "." + itemFieldName;
            fields.add(new TemplateField(combinedName, false));
        }
        
        listFieldInput.clear();
        listItemFieldInput.clear();
        
        // 更新数据表格
        updateDataTableView();
        
        AppLogger.info("添加列表字段: " + fieldName);
    }
    
    /**
     * 删除字段
     */
    private void deleteField(TemplateField field) {
        String fieldName = field.getName();
        
        // 从字段列表中移除
        fields.remove(field);
        
        // 从数据映射中移除
        fieldDataMap.remove(fieldName);
        
        // 如果是列表字段，还要移除相关的列表数据
        if (field.isList()) {
            listFieldDataMap.remove(fieldName);
            
            // 还需要移除列表相关的子字段
            List<TemplateField> fieldsToRemove = new ArrayList<>();
            for (TemplateField f : fields) {
                if (f.getName().startsWith(fieldName + ".")) {
                    fieldsToRemove.add(f);
                }
            }
            fields.removeAll(fieldsToRemove);
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
        
        // 从模板中移除占位符
        if (isWordMode) {
            String text = wordEditor.getText();
            String placeholder;
            
            if (field.isList()) {
                // 删除列表字段的开始和结束标记
                String startMarker = "{{#" + fieldName + "}}";
                String endMarker = "{{/" + fieldName + "}}";
                
                int startIndex = text.indexOf(startMarker);
                int endIndex = text.indexOf(endMarker);
                
                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    // 删除整个列表段落，包括标记
                    text = text.substring(0, startIndex) + text.substring(endIndex + endMarker.length());
                }
            } else {
                // 删除对象字段占位符
                placeholder = "{{" + fieldName + "}}";
                text = text.replace(placeholder, "");
            }
            
            wordEditor.setText(text);
        } else {
            // 从Excel中移除占位符
            for (ObservableList<String> row : excelEditor.getItems()) {
                for (int i = 0; i < row.size(); i++) {
                    String cellText = row.get(i);
                    String placeholder = "{{" + fieldName + "}}";
                    if (cellText.contains(placeholder)) {
                        row.set(i, cellText.replace(placeholder, ""));
                    }
                }
            }
        }
        
        // 更新数据表格
        updateDataTableView();
        
        // 更新预览
        updatePreview();
        
        AppLogger.info("删除字段: " + fieldName);
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
                    wordTemplateService.generateWordTemplate(file.getPath(), fields);
                    // 更新编辑器内容
                    String content = wordTemplateService.readDocxContent(file.getPath());
                    wordEditor.setText(content);
                } else {
                    // 生成Excel模板
                    excelTemplateService.generateExcelTemplate(file.getPath(), fields);
                    // 更新Excel编辑器
                    List<List<String>> data = excelTemplateService.readExcelContent(file.getPath());
                    loadExcelData(data);
                }
                
                // 更新当前模板文件
                currentTemplateFile = file;
                
                // 更新数据表格
                updateDataTableView();
                
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
                    wordTemplateService.saveDocxDocument(file.getPath(), previewArea.getText());
                } else {
                    // 保存Excel文档，暂时保存编辑器中的内容
                    List<List<String>> data = new ArrayList<>();
                    for (ObservableList<String> row : excelEditor.getItems()) {
                        List<String> rowData = new ArrayList<>();
                        for (String cell : row) {
                            // 替换所有字段占位符
                            String processedCell = cell;
                            for (Map.Entry<String, String> entry : fieldDataMap.entrySet()) {
                                String placeholder = "{{" + entry.getKey() + "}}";
                                if (processedCell.contains(placeholder)) {
                                    processedCell = processedCell.replace(placeholder, entry.getValue());
                                }
                            }
                            rowData.add(processedCell);
                        }
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
     * 刷新模板列表
     */
    @FXML
    private void handleRefreshTemplates() {
        refreshTemplateList();
    }
    
    /**
     * 刷新模板列表
     */
    private void refreshTemplateList() {
        ObservableList<String> templates = FXCollections.observableArrayList();
        
        // 加载templates目录下的模板文件
        File templatesDir = new File(baseDir, "templates");
        if (templatesDir.exists() && templatesDir.isDirectory()) {
            File[] files = templatesDir.listFiles((dir, name) -> name.endsWith(".docx") || name.endsWith(".xlsx"));
            if (files != null) {
                for (File file : files) {
                    templates.add(file.getName());
                }
            }
        }
        
        templateComboBox.setItems(templates);
        
        if (!templates.isEmpty()) {
            templateComboBox.getSelectionModel().selectFirst();
            handleTemplateSelected();
        }
    }
    
    /**
     * 处理模板选择
     */
    @FXML
    private void handleTemplateSelected() {
        String selectedTemplate = templateComboBox.getSelectionModel().getSelectedItem();
        if (selectedTemplate != null) {
            try {
                File templatesDir = new File(baseDir, "templates");
                File templateFile = new File(templatesDir, selectedTemplate);
                
                if (templateFile.exists()) {
                    // 确定文件类型
                    boolean isWordFile = selectedTemplate.endsWith(".docx");
                    
                    // 提取字段并设置数据表格列
                    if (isWordFile) {
                        String content = wordTemplateService.readDocxContent(templateFile.getPath());
                        extractFields(content);
                    } else {
                        List<List<String>> data = excelTemplateService.readExcelContent(templateFile.getPath());
                        extractFieldsFromExcel(data);
                    }
                    
                    // 设置数据表格列
                    setupDataTableColumns();
                    
                    AppLogger.info("加载模板: " + selectedTemplate);
                }
            } catch (Exception e) {
                AppLogger.error("加载模板失败: " + e.getMessage(), e);
                showError("加载失败", "无法加载所选模板: " + e.getMessage());
            }
        }
    }
    
    /**
     * 设置数据表格列
     */
    private void setupDataTableColumns() {
        dataTableView.getColumns().clear();
        dataTableView.getItems().clear();
        
        // 为每个字段创建一列
        for (TemplateField field : fields) {
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(field.getName());
            final int colIndex = dataTableView.getColumns().size();
            
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
            
            dataTableView.getColumns().add(column);
        }
        
        // 添加一行空数据
        addDataRow();
    }
    
    /**
     * 添加数据行
     */
    @FXML
    private void handleAddRow() {
        addDataRow();
    }
    
    /**
     * 添加数据行
     */
    private void addDataRow() {
        ObservableList<String> row = FXCollections.observableArrayList();
        // 为每个字段添加一个空单元格
        for (int i = 0; i < dataTableView.getColumns().size(); i++) {
            row.add("");
        }
        dataTableView.getItems().add(row);
    }
    
    /**
     * 删除选中行
     */
    @FXML
    private void handleDeleteRow() {
        int selectedIndex = dataTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            dataTableView.getItems().remove(selectedIndex);
        }
    }
    
    /**
     * 保存数据
     */
    @FXML
    private void handleSaveData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存数据");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel表格", "*.xlsx"));
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        fileChooser.setInitialFileName("数据_" + timestamp + ".xlsx");
        
        // 设置初始目录
        File dataDir = new File(baseDir, "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        fileChooser.setInitialDirectory(dataDir);
        
        File file = fileChooser.showSaveDialog(dataTableView.getScene().getWindow());
        if (file != null) {
            try {
                // 准备数据
                List<List<String>> data = new ArrayList<>();
                
                // 添加标题行
                List<String> headers = new ArrayList<>();
                for (TableColumn<?, ?> column : dataTableView.getColumns()) {
                    headers.add(column.getText());
                }
                data.add(headers);
                
                // 添加数据行
                for (ObservableList<String> row : dataTableView.getItems()) {
                    data.add(new ArrayList<>(row));
                }
                
                // 保存Excel文件
                excelTemplateService.saveExcelDocument(file.getPath(), data);
                
                AppLogger.info("成功保存数据: " + file.getName());
                showInfo("保存成功", "数据已保存到: " + file.getPath());
            } catch (Exception e) {
                AppLogger.error("保存数据失败: " + e.getMessage(), e);
                showError("保存失败", "无法保存数据: " + e.getMessage());
            }
        }
    }
    
    /**
     * 字段列表单元格
     */
    private class FieldListCell extends ListCell<TemplateField> {
        @Override
        protected void updateItem(TemplateField item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox hbox = new HBox(5);
                Label typeLabel = new Label(item.isList() ? "▣" : "◉");
                typeLabel.setStyle("-fx-text-fill: -pink-text; -fx-font-weight: bold;");
                
                Label nameLabel = new Label(item.getName());
                
                Button deleteButton = new Button("×");
                deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-cursor: hand;");
                deleteButton.setOnAction(e -> deleteField(item));
                
                hbox.getChildren().addAll(typeLabel, nameLabel, deleteButton);
                hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                
                setGraphic(hbox);
            }
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
     * 导入Excel数据
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
                        if (!header.contains("#")) {
                            // 移除可能的占位符符号
                            String fieldName = header.replace("{{", "").replace("}}", "").trim();
                            fieldDataMap.put(fieldName, firstDataRow.get(i));
                        } else {
                            // 处理列表字段
                            String listFieldName = header.replace("{{#", "").replace("}}", "").trim();
                            processListFieldData(listFieldName, headers, data);
                        }
                    }
                }
                
                // 更新数据表格
                updateDataTableView();
                
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
     * 处理列表字段数据
     */
    private void processListFieldData(String listFieldName, List<String> headers, List<List<String>> data) {
        List<Map<String, String>> listItems = new ArrayList<>();
        List<String> listItemFields = new ArrayList<>();
        
        // 找出属于这个列表的所有字段
        for (String header : headers) {
            if (header.contains(listFieldName + ".")) {
                String itemField = header.substring(header.indexOf(".") + 1).replace("{{", "").replace("}}", "").trim();
                listItemFields.add(itemField);
            }
        }
        
        // 从第二行开始遍历数据（跳过标题行）
        for (int i = 1; i < data.size(); i++) {
            List<String> row = data.get(i);
            Map<String, String> item = new HashMap<>();
            
            boolean hasData = false;
            for (int j = 0; j < headers.size() && j < row.size(); j++) {
                String header = headers.get(j);
                if (header.contains(listFieldName + ".")) {
                    String itemField = header.substring(header.indexOf(".") + 1).replace("{{", "").replace("}}", "").trim();
                    String value = row.get(j);
                    if (value != null && !value.trim().isEmpty()) {
                        item.put(itemField, value);
                        hasData = true;
                    }
                }
            }
            
            if (hasData) {
                listItems.add(item);
            }
        }
        
        if (!listItems.isEmpty()) {
            listFieldDataMap.put(listFieldName, listItems);
        }
    }
    
    /**
     * 更新数据表格视图
     */
    private void updateDataTableView() {
        dataTableView.getItems().clear();
        dataTableView.getColumns().clear();
        
        // 添加对象字段列
        TableColumn<ObservableList<String>, String> fieldNameColumn = new TableColumn<>("字段名称");
        fieldNameColumn.setPrefWidth(150);
        fieldNameColumn.setCellValueFactory(data -> {
            if (data.getValue().size() > 0) {
                return new SimpleStringProperty(data.getValue().get(0));
            }
            return new SimpleStringProperty("");
        });
        
        TableColumn<ObservableList<String>, String> fieldValueColumn = new TableColumn<>("字段值");
        fieldValueColumn.setPrefWidth(250);
        fieldValueColumn.setCellValueFactory(data -> {
            if (data.getValue().size() > 1) {
                return new SimpleStringProperty(data.getValue().get(1));
            }
            return new SimpleStringProperty("");
        });
        fieldValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        fieldValueColumn.setOnEditCommit(event -> {
            ObservableList<String> row = event.getRowValue();
            String fieldName = row.get(0);
            String newValue = event.getNewValue();
            
            // 更新字段数据
            row.set(1, newValue);
            fieldDataMap.put(fieldName, newValue);
            
            // 更新预览
            updatePreview();
        });
        
        dataTableView.getColumns().addAll(fieldNameColumn, fieldValueColumn);
        
        // 添加对象字段数据
        for (TemplateField field : fields) {
            if (!field.isList()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(field.getName());
                row.add(fieldDataMap.getOrDefault(field.getName(), ""));
                dataTableView.getItems().add(row);
            }
        }
        
        dataTableView.setEditable(true);
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
        previewArea.setText(previewText);
    }
    
    /**
     * 更新Excel预览
     */
    private void updateExcelPreview() {
        // Excel预览暂时使用文本形式显示
        StringBuilder preview = new StringBuilder();
        
        // 获取表格数据
        for (int i = 0; i < excelEditor.getItems().size(); i++) {
            ObservableList<String> row = excelEditor.getItems().get(i);
            StringBuilder rowText = new StringBuilder();
            
            for (int j = 0; j < row.size(); j++) {
                String cellText = row.get(j);
                
                // 替换对象字段
                for (Map.Entry<String, String> entry : fieldDataMap.entrySet()) {
                    String placeholder = "{{" + entry.getKey() + "}}";
                    if (cellText.contains(placeholder)) {
                        cellText = cellText.replace(placeholder, entry.getValue());
                    }
                }
                
                rowText.append(cellText).append("\t");
            }
            
            preview.append(rowText).append("\n");
        }
        
        previewArea.setText(preview.toString());
    }
    
    /**
     * 处理字段列表点击事件
     */
    @FXML
    private void handleFieldListClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            TemplateField selectedField = fieldListView.getSelectionModel().getSelectedItem();
            if (selectedField != null) {
                // 在编辑器当前光标位置插入字段
                if (isWordMode) {
                    int caretPosition = wordEditor.getCaretPosition();
                    String fieldText;
                    
                    if (selectedField.isList()) {
                        fieldText = "{{#" + selectedField.getName() + "}}\n{{/" + selectedField.getName() + "}}";
                    } else {
                        fieldText = "{{" + selectedField.getName() + "}}";
                    }
                    
                    wordEditor.insertText(caretPosition, fieldText);
                } else {
                    // 在Excel编辑器中处理
                    ObservableList<TablePosition> selectedCells = excelEditor.getSelectionModel().getSelectedCells();
                    if (!selectedCells.isEmpty()) {
                        TablePosition pos = selectedCells.get(0);
                        int row = pos.getRow();
                        int col = pos.getColumn();
                        
                        if (row >= 0 && col >= 0 && row < excelEditor.getItems().size()) {
                            ObservableList<String> rowData = excelEditor.getItems().get(row);
                            if (col < rowData.size()) {
                                String fieldText = "{{" + selectedField.getName() + "}}";
                                rowData.set(col, fieldText);
                            }
                        }
                    }
                }
                
                // 更新预览
                updatePreview();
            }
        }
    }
} 