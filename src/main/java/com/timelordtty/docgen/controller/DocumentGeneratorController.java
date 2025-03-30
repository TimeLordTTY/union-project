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
import java.util.Optional;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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
    
    // 列表字段表格
    @FXML private TableView<Map<String, String>> listFieldsTable;
    @FXML private TableColumn<Map<String, String>, String> listNameColumn;
    @FXML private TableColumn<Map<String, String>, String> listObjectNameColumn;
    @FXML private TableColumn<Map<String, String>, String> listAddColumn;
    @FXML private TableColumn<Map<String, String>, String> listDeleteColumn;
    
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
        
        // 初始化列表字段表格
        setupListFieldsTable();
        
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
            private final Button deleteButton = new Button("－");
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                deleteButton.setStyle("-fx-background-color: white; -fx-text-fill: #E91E63; -fx-border-color: #E91E63; -fx-border-radius: 50%;");
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
     * 设置列表字段表格
     */
    private void setupListFieldsTable() {
        // 初始化ObservableList
        ObservableList<Map<String, String>> listFields = FXCollections.observableArrayList();
        listFieldsTable.setItems(listFields);
        
        // 设置列表名列
        listNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("listName")));
            
        // 设置对象名列
        listObjectNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("objectName")));
            
        // 添加列
        listAddColumn.setCellFactory(param -> new TableCell<>() {
            private final Button addButton = new Button("＋");
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                Map<String, String> rowData = getTableView().getItems().get(getIndex());
                String listName = rowData.get("listName");
                String objectName = rowData.get("objectName");
                
                // 只有当为列表对象而非列表项时才显示添加按钮
                if (objectName == null || objectName.isEmpty()) {
                    addButton.setStyle("-fx-background-color: white; -fx-text-fill: #E91E63; -fx-border-color: #E91E63; -fx-border-radius: 50%;");
                    setGraphic(addButton);
                    addButton.setOnAction(event -> {
                        TextInputDialog dialog = new TextInputDialog("");
                        dialog.setTitle("添加列表项");
                        dialog.setHeaderText(null);
                        dialog.setContentText("请输入列表项名称:");
                        
                        Optional<String> result = dialog.showAndWait();
                        result.ifPresent(name -> {
                            if (!name.trim().isEmpty()) {
                                // 为列表添加项
                                for (TemplateField field : fields) {
                                    if (field.getName().equals(listName) && field.isList()) {
                                        field.addListItem(name);
                                        
                                        // 更新UI
                                        Map<String, String> newRow = new HashMap<>();
                                        newRow.put("listName", listName);
                                        newRow.put("objectName", name);
                                        listFieldsTable.getItems().add(newRow);
                                        
                                        // 刷新表格
                                        listFieldsTable.refresh();
                                        break;
                                    }
                                }
                            }
                        });
                    });
                } else {
                    setGraphic(null);
                }
            }
        });
        
        // 删除列
        listDeleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("－");
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                Map<String, String> rowData = getTableView().getItems().get(getIndex());
                String listName = rowData.get("listName");
                String objectName = rowData.get("objectName");
                
                deleteButton.setStyle("-fx-background-color: white; -fx-text-fill: #E91E63; -fx-border-color: #E91E63; -fx-border-radius: 50%;");
                setGraphic(deleteButton);
                deleteButton.setOnAction(event -> {
                    if (objectName == null || objectName.isEmpty()) {
                        // 删除整个列表
                        for (TemplateField field : new ArrayList<>(fields)) {
                            if (field.getName().equals(listName) && field.isList()) {
                                fields.remove(field);
                                objectFields.remove(field);
                                
                                // 删除所有相关的UI行
                                listFieldsTable.getItems().removeIf(row -> 
                                    row.get("listName").equals(listName));
                                
                                // 删除列表数据
                                listFieldDataMap.remove(listName);
                                
                                break;
                            }
                        }
                    } else {
                        // 删除列表项
                        for (TemplateField field : fields) {
                            if (field.getName().equals(listName) && field.isList()) {
                                field.removeListItem(objectName);
                                
                                // 删除UI行
                                getTableView().getItems().remove(getIndex());
                                
                                break;
                            }
                        }
                    }
                    
                    // 刷新表格
                    listFieldsTable.refresh();
                    updateDataTables();
                });
            }
        });
        
        // 双击事件，插入列表标记到编辑区
        listFieldsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Map<String, String> selectedField = listFieldsTable.getSelectionModel().getSelectedItem();
                if (selectedField != null) {
                    String listName = selectedField.get("listName");
                    String objectName = selectedField.get("objectName");
                    
                    if (listName != null && !listName.isEmpty()) {
                        if (objectName == null || objectName.isEmpty()) {
                            // 插入列表开始和结束标记
                            if (isWordMode) {
                                String startTag = "{{" + listName + "}}";
                                String endTag = "{{/" + listName + "}}";
                                insertTextAtCursor(wordEditor, startTag + "\n" + endTag);
                            } else {
                                // Excel模式下的处理逻辑
                                int selectedIndex = excelEditor.getSelectionModel().getSelectedIndex();
                                if (selectedIndex >= 0) {
                                    TablePosition pos = excelEditor.getSelectionModel().getSelectedCells().get(0);
                                    int row = pos.getRow();
                                    // 确保有足够的行
                                    while (excelEditor.getItems().size() <= row + 1) {
                                        addExcelRow();
                                    }
                                    // 插入开始和结束标记
                                    excelEditor.getItems().get(row).set(0, "{{" + listName + "}}");
                                    excelEditor.getItems().get(row + 1).set(0, "{{/" + listName + "}}");
                                }
                            }
                        } else {
                            // 插入列表项占位符
                            String placeholder = "{{" + objectName + "}}";
                            if (isWordMode) {
                                insertTextAtCursor(wordEditor, placeholder);
                            } else {
                                // Excel模式下的处理逻辑
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
                    }
                }
            }
        });
    }
    
    /**
     * 在文本区域光标位置插入文本
     * @param textArea 文本区域
     * @param text 要插入的文本
     */
    private void insertTextAtCursor(TextArea textArea, String text) {
        int caretPosition = textArea.getCaretPosition();
        String currentText = textArea.getText();
        
        // 在当前光标位置插入文本
        String newText = currentText.substring(0, caretPosition)
                       + text
                       + currentText.substring(caretPosition);
        textArea.setText(newText);
        
        // 将光标放在插入文本之后
        textArea.positionCaret(caretPosition + text.length());
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
        
        // 清空字段列表和数据
        clearAllFields();
        
        // 清空模板编辑区和预览区
        wordEditor.clear();
        wordPreviewArea.clear();
        excelEditor.getItems().clear();
        excelPreviewArea.getItems().clear();
        
        // 清空当前模板文件
        currentTemplateFile = null;
        
        AppLogger.info("切换模板类型为: " + selectedType);
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
        
        // 设置初始目录
        String dataDir = baseDir + "/ProjectAssistant/templates/data";
        File dataDirFile = new File(dataDir);
        if (!dataDirFile.exists()) {
            dataDirFile.mkdirs();
        }
        fileChooser.setInitialDirectory(dataDirFile);
        
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
        
        // 替换对象字段 - 支持${fieldName}格式
        for (Map.Entry<String, String> entry : fieldDataMap.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (previewText.contains(placeholder)) {
                String value = entry.getValue() != null ? entry.getValue() : "";
                previewText = previewText.replace(placeholder, value);
            }
        }
        
        // 处理列表字段 - 支持{{listName}}...{{/listName}}格式
        for (Map.Entry<String, List<Map<String, String>>> listEntry : listFieldDataMap.entrySet()) {
            String listName = listEntry.getKey();
            List<Map<String, String>> listItems = listEntry.getValue();
            
            // 定义不同格式的开始标签
            String[] startTags = {"{{" + listName + "}}", "{{#" + listName + "}}"};
            String endTag = "{{/" + listName + "}}";
            
            for (String startTag : startTags) {
                // 查找模板中的列表部分
                int startPos = previewText.indexOf(startTag);
                while (startPos != -1) {
                    int endPos = previewText.indexOf(endTag, startPos);
                    if (endPos != -1) {
                        // 提取列表模板内容
                        String listTemplate = previewText.substring(startPos + startTag.length(), endPos);
                        
                        // 生成列表内容
                        StringBuilder listContent = new StringBuilder();
                        if (listItems != null && !listItems.isEmpty()) {
                            for (Map<String, String> item : listItems) {
                                String itemContent = listTemplate;
                                
                                // 替换列表项中的占位符
                                for (Map.Entry<String, String> entry : item.entrySet()) {
                                    String placeholder = "{{" + entry.getKey() + "}}";
                                    String value = entry.getValue() != null ? entry.getValue() : "";
                                    itemContent = itemContent.replace(placeholder, value);
                                }
                                
                                listContent.append(itemContent);
                            }
                        }
                        
                        // 替换列表部分
                        previewText = previewText.substring(0, startPos) + 
                                     listContent.toString() + 
                                     previewText.substring(endPos + endTag.length());
                        
                        // 更新搜索位置
                        startPos = previewText.indexOf(startTag, startPos + listContent.length());
                    } else {
                        break; // 没有找到结束标签
                    }
                }
            }
        }
        
        // 清理任何剩余的未替换占位符 - ${xxx} 格式
        java.util.regex.Pattern dollarPattern = java.util.regex.Pattern.compile("\\$\\{[^{}]+\\}");
        java.util.regex.Matcher dollarMatcher = dollarPattern.matcher(previewText);
        while (dollarMatcher.find()) {
            String placeholder = dollarMatcher.group();
            previewText = previewText.replace(placeholder, "");
        }
        
        // 清理任何剩余的未替换占位符 - {{xxx}} 格式
        java.util.regex.Pattern mustachePattern = java.util.regex.Pattern.compile("\\{\\{[^{}]+\\}\\}");
        java.util.regex.Matcher mustacheMatcher = mustachePattern.matcher(previewText);
        while (mustacheMatcher.find()) {
            String placeholder = mustacheMatcher.group();
            previewText = previewText.replace(placeholder, "");
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
                
                // 替换对象字段占位符 - 支持${fieldName}格式
                for (Map.Entry<String, String> entry : fieldDataMap.entrySet()) {
                    String placeholder = "${" + entry.getKey() + "}";
                    if (processedText.contains(placeholder)) {
                        processedText = processedText.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
                    }
                }
                
                // 替换列表字段占位符
                for (Map.Entry<String, List<Map<String, String>>> listEntry : listFieldDataMap.entrySet()) {
                    String listName = listEntry.getKey();
                    List<Map<String, String>> listItems = listEntry.getValue();
                    
                    // 获取该列表的所有对象字段
                    for (TemplateField field : fields) {
                        if (field.isList() && field.getName().equals(listName)) {
                            for (String itemField : field.getListItems()) {
                                String placeholder = "${" + listName + "." + itemField + "}";
                                if (processedText.contains(placeholder)) {
                                    StringBuilder replacement = new StringBuilder();
                                    for (Map<String, String> item : listItems) {
                                        String value = item.get(itemField);
                                        if (value != null) {
                                            if (replacement.length() > 0) {
                                                replacement.append(", ");
                                            }
                                            replacement.append(value);
                                        }
                                    }
                                    processedText = processedText.replace(placeholder, replacement.toString());
                                }
                            }
                        }
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
        fields.clear();
        
        // 提取字段名
        List<String> foundObjectFields = new ArrayList<>();
        List<String> foundListFields = new ArrayList<>();
        Map<String, List<String>> listItemFields = new HashMap<>();
        
        // 查找对象字段 - ${field}格式
        java.util.regex.Pattern dollarPattern = java.util.regex.Pattern.compile("\\$\\{([^{}]+)\\}");
        java.util.regex.Matcher dollarMatcher = dollarPattern.matcher(content);
        
        while (dollarMatcher.find()) {
            String fieldName = dollarMatcher.group(1);
            foundObjectFields.add(fieldName);
        }
        
        // 查找列表字段 - {{list}}...{{/list}}格式或{{#list}}...{{/list}}格式
        java.util.regex.Pattern listStartPattern = java.util.regex.Pattern.compile("\\{\\{(#?)([^{}\\/#]+)\\}\\}");
        java.util.regex.Matcher listStartMatcher = listStartPattern.matcher(content);
        
        while (listStartMatcher.find()) {
            String listName = listStartMatcher.group(2);
            
            // 检查是否有结束标签
            String listEndTag = "{{/" + listName + "}}";
            if (content.contains(listEndTag)) {
                foundListFields.add(listName);
                
                // 提取列表项字段
                int startPos = content.indexOf("{{" + listName + "}}");
                if (startPos == -1) {
                    startPos = content.indexOf("{{#" + listName + "}}");
                }
                int endPos = content.indexOf(listEndTag);
                
                if (startPos != -1 && endPos != -1 && endPos > startPos) {
                    String listContent = content.substring(startPos + ("{{" + listName + "}}").length(), endPos);
                    if (listContent.isEmpty()) {
                        listContent = content.substring(startPos + ("{{#" + listName + "}}").length(), endPos);
                    }
                    
                    // 提取列表项字段 - {{field}}格式
                    java.util.regex.Pattern itemPattern = java.util.regex.Pattern.compile("\\{\\{([^#/{}]+)\\}\\}");
                    java.util.regex.Matcher itemMatcher = itemPattern.matcher(listContent);
                    
                    List<String> itemFields = new ArrayList<>();
                    while (itemMatcher.find()) {
                        String itemField = itemMatcher.group(1);
                        if (!itemField.equals(listName) && !itemField.startsWith("/")) {
                            itemFields.add(itemField);
                        }
                    }
                    
                    if (!itemFields.isEmpty()) {
                        listItemFields.put(listName, itemFields);
                    }
                }
            }
        }
        
        // 添加对象字段
        for (String fieldName : foundObjectFields) {
            TemplateField field = new TemplateField(fieldName, false);
            if (!containsField(objectFields, field)) {
                objectFields.add(field);
            }
        }
        
        // 添加列表字段
        for (String listName : foundListFields) {
            TemplateField listField = new TemplateField(listName, true);
            
            if (!containsField(fields, listField)) {
                fields.add(listField);
            }
            
            if (!containsField(objectFields, listField)) {
                objectFields.add(listField);
            }
            
            // 添加列表项
            List<String> itemFields = listItemFields.get(listName);
            if (itemFields != null) {
                for (String itemField : itemFields) {
                    listField.addListItem(itemField);
                }
            }
        }
        
        // 在UI上更新字段列表
        Platform.runLater(() -> {
            objectFieldsTable.setItems(FXCollections.observableArrayList(objectFields));
            updateListFieldsUI();
            updateDataTables();
        });
        
        AppLogger.info("从模板中提取了 " + objectFields.size() + " 个字段");
    }
    
    /**
     * 检查列表中是否包含指定字段
     */
    private boolean containsField(List<TemplateField> fields, TemplateField field) {
        for (TemplateField existingField : fields) {
            if (existingField.getName().equals(field.getName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 从Excel中提取字段
     */
    private void extractFieldsFromExcel(List<List<String>> data) {
        // 清空现有字段
        clearAllFields();
        
        // 提取字段的模式
        java.util.regex.Pattern dollarPattern = java.util.regex.Pattern.compile("\\$\\{([^{}]+)\\}");
        
        // 处理所有单元格
        Set<String> foundFields = new HashSet<>();
        Set<String> foundListFields = new HashSet<>();
        Map<String, Set<String>> listItemsMap = new HashMap<>();
        
        for (List<String> row : data) {
            for (String cell : row) {
                if (cell == null || cell.isEmpty()) continue;
                
                java.util.regex.Matcher matcher = dollarPattern.matcher(cell);
                while (matcher.find()) {
                    String fieldPath = matcher.group(1);
                    
                    // 处理列表字段（包含点号的字段）
                    if (fieldPath.contains(".")) {
                        String[] parts = fieldPath.split("\\.", 2);
                        String listName = parts[0];
                        String itemField = parts[1];
                        
                        // 记录列表字段
                        foundListFields.add(listName);
                        
                        // 记录列表项
                        if (!listItemsMap.containsKey(listName)) {
                            listItemsMap.put(listName, new HashSet<>());
                        }
                        listItemsMap.get(listName).add(itemField);
                    } else {
                        // 记录普通字段
                        foundFields.add(fieldPath);
                    }
                }
            }
        }
        
        // 添加普通字段
        for (String fieldName : foundFields) {
            TemplateField field = new TemplateField(fieldName, false);
            objectFields.add(field);
            
            // 初始化字段数据
            fieldDataMap.put(fieldName, "");
        }
        
        // 添加列表字段
        for (String listName : foundListFields) {
            TemplateField listField = new TemplateField(listName, true);
            fields.add(listField);
            objectFields.add(listField);
            
            // 初始化列表数据
            listFieldDataMap.put(listName, new ArrayList<>());
            
            // 添加列表项
            Set<String> listItems = listItemsMap.getOrDefault(listName, new HashSet<>());
            for (String itemName : listItems) {
                listField.addListItem(itemName);
                
                // 添加列表项字段
                String fullName = listName + "." + itemName;
                TemplateField itemField = new TemplateField(fullName, false);
                objectFields.add(itemField);
            }
            
            // 更新列表字段UI
            Map<String, String> row = new HashMap<>();
            row.put("name", listName);
            row.put("objectName", "");
            listFieldsTable.getItems().add(row);
            
            // 为每个列表项添加一行
            for (String itemName : listItems) {
                Map<String, String> itemRow = new HashMap<>();
                itemRow.put("name", listName);
                itemRow.put("objectName", itemName);
                listFieldsTable.getItems().add(itemRow);
            }
        }
        
        // 更新UI
        updateObjectDataTable();
        updateListDataTables();
        
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
        if (fieldName.isEmpty()) {
            showInfo("提示", "请输入字段名称");
            return;
        }
        
        // 检查是否已存在
        for (TemplateField field : objectFields) {
            if (field.getName().equals(fieldName)) {
                showInfo("提示", "字段已存在");
                return;
            }
        }
        
        // 创建新字段
        TemplateField field = new TemplateField(fieldName, false);
        
        // 添加到字段列表
        objectFields.add(field);
        
        // 清空输入框
        objectFieldInput.clear();
        
        // 更新数据填充表格
        updateDataTables();
        
        // 记录日志
        AppLogger.info("添加对象字段: " + fieldName);
        
        // 立即刷新表格UI
        objectFieldsTable.refresh();
        objectDataTable.refresh();
    }
    
    /**
     * 添加列表字段
     */
    @FXML
    private void handleAddListField() {
        String fieldName = listFieldInput.getText().trim();
        if (fieldName.isEmpty()) {
            showInfo("提示", "请输入列表名称");
            return;
        }
        
        // 检查是否已存在
        for (TemplateField field : fields) {
            if (field.getName().equals(fieldName) && field.isList()) {
                showInfo("提示", "列表已存在");
                return;
            }
        }
        
        // 创建新列表字段
        TemplateField field = new TemplateField(fieldName, true);
        
        // 添加到字段列表
        fields.add(field);
        
        // 添加到UI列表
        Map<String, String> row = new HashMap<>();
        row.put("name", fieldName);
        row.put("objectName", "");
        
        // 确保listFieldsTable不为空
        if (listFieldsTable != null) {
            listFieldsTable.getItems().add(row);
            listFieldsTable.refresh();
        }
        
        // 清空输入框
        listFieldInput.clear();
        
        // 记录日志
        AppLogger.info("添加列表字段: " + fieldName);
    }
    
    /**
     * 更新列表字段UI
     */
    private void updateListFieldsUI() {
        // 清空现有列表数据
        ObservableList<Map<String, String>> listFieldsData = FXCollections.observableArrayList();
        
        // 遍历所有列表字段
        for (TemplateField field : fields) {
            if (field.isList()) {
                // 添加列表行
                Map<String, String> listRow = new HashMap<>();
                listRow.put("listName", field.getName());
                listRow.put("objectName", "");
                listFieldsData.add(listRow);
                
                // 添加列表项行
                for (String itemName : field.getListItems()) {
                    Map<String, String> itemRow = new HashMap<>();
                    itemRow.put("listName", field.getName());
                    itemRow.put("objectName", itemName);
                    listFieldsData.add(itemRow);
                }
            }
        }
        
        // 设置列表字段表格数据
        listFieldsTable.setItems(listFieldsData);
        listFieldsTable.refresh();
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
        fileChooser.getExtensionFilters().add(
            new ExtensionFilter("Excel表格", "*.xlsx"));
        
        // 设置初始目录
        String dataDir = baseDir + "/ProjectAssistant/templates/data";
        File dataDirFile = new File(dataDir);
        if (!dataDirFile.exists()) {
            dataDirFile.mkdirs();
        }
        fileChooser.setInitialDirectory(dataDirFile);
        
        // 显示文件选择器
        File file = fileChooser.showOpenDialog(null);
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
                
                // 清空现有数据
                fieldDataMap.clear();
                listFieldDataMap.clear();
                
                // 创建列表字段和列表项的映射
                Map<String, Set<String>> listItemsMap = new HashMap<>();
                
                // 首先解析所有标题，识别对象字段和列表字段
                for (String header : headers) {
                    if (header.contains(".")) {
                        // 这是列表字段
                        String[] parts = header.split("\\.", 2);
                        if (parts.length == 2) {
                            String listName = parts[0];
                            String itemName = parts[1];
                            
                            // 确保在列表映射中有这个列表
                            if (!listItemsMap.containsKey(listName)) {
                                listItemsMap.put(listName, new HashSet<>());
                                // 初始化列表数据
                                listFieldDataMap.put(listName, new ArrayList<>());
                            }
                            
                            // 添加列表项
                            listItemsMap.get(listName).add(itemName);
                        }
                    }
                }
                
                // 创建列表字段和列表项
                for (Map.Entry<String, Set<String>> entry : listItemsMap.entrySet()) {
                    String listName = entry.getKey();
                    Set<String> items = entry.getValue();
                    
                    // 创建列表字段
                    TemplateField listField = null;
                    for (TemplateField field : fields) {
                        if (field.getName().equals(listName) && field.isList()) {
                            listField = field;
                            break;
                        }
                    }
                    
                    if (listField == null) {
                        listField = new TemplateField(listName, true);
                        fields.add(listField);
                        objectFields.add(listField);
                    }
                    
                    // 添加列表项
                    for (String itemName : items) {
                        listField.addListItem(itemName);
                    }
                }
                
                // 处理数据行
                for (int rowIndex = 1; rowIndex < data.size(); rowIndex++) {
                    List<String> rowData = data.get(rowIndex);
                    
                    // 用于记录当前行属于哪个列表对象
                    String currentListKey = ""; 
                    Map<String, String> currentListItem = null;
                    
                    // 处理每一列
                    for (int colIndex = 0; colIndex < headers.size() && colIndex < rowData.size(); colIndex++) {
                        String header = headers.get(colIndex);
                        String value = rowData.get(colIndex);
                        
                        if (value == null || value.isEmpty()) {
                            continue; // 跳过空值
                        }
                        
                        if (header.contains(".")) {
                            // 这是列表字段
                            String[] parts = header.split("\\.", 2);
                            if (parts.length == 2) {
                                String listName = parts[0];
                                String itemName = parts[1];
                                
                                // 如果当前行的列表对象还未确定或者已经变换到新的列表对象
                                if (currentListKey.isEmpty() || !currentListKey.equals(listName)) {
                                    currentListKey = listName;
                                    
                                    // 查找当前列表对象是否已存在（根据主键字段或第一个非空列表项）
                                    boolean found = false;
                                    if (!listFieldDataMap.get(listName).isEmpty()) {
                                        // 尝试根据已有的值找到对应的对象
                                        for (Map<String, String> item : listFieldDataMap.get(listName)) {
                                            boolean isMatch = true;
                                            for (String existingItemName : listItemsMap.get(listName)) {
                                                int existingColIndex = headers.indexOf(listName + "." + existingItemName);
                                                if (existingColIndex >= 0 && existingColIndex < rowData.size() && 
                                                    !rowData.get(existingColIndex).isEmpty() && 
                                                    item.containsKey(existingItemName) && 
                                                    item.get(existingItemName).equals(rowData.get(existingColIndex))) {
                                                    // 找到匹配项，继续检查
                                                } else {
                                                    isMatch = false;
                                                    break;
                                                }
                                            }
                                            
                                            if (isMatch) {
                                                currentListItem = item;
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                    
                                    if (!found) {
                                        // 创建新的列表项
                                        currentListItem = new HashMap<>();
                                        listFieldDataMap.get(listName).add(currentListItem);
                                    }
                                }
                                
                                // 设置列表项的值
                                if (currentListItem != null) {
                                    currentListItem.put(itemName, value);
                                }
                            }
                        } else {
                            // 这是对象字段
                            fieldDataMap.put(header, value);
                            
                            // 确保对象字段存在
                            boolean found = false;
                            for (TemplateField field : objectFields) {
                                if (field.getName().equals(header) && !field.isList()) {
                                    found = true;
                                    break;
                                }
                            }
                            
                            if (!found) {
                                objectFields.add(new TemplateField(header, false));
                            }
                        }
                    }
                }
                
                // 更新UI
                updateListFieldsUI();
                updateObjectDataTable();
                updateListDataTables();
                
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
        
        // 创建表格
        TableView<Map<String, String>> table = new TableView<>();
        
        // 如果列表项数据不存在，则初始化
        if (!listFieldDataMap.containsKey(listField.getName())) {
            listFieldDataMap.put(listField.getName(), new ArrayList<>());
        }
        
        // 查找此列表下的所有列表项字段
        List<String> listItemFields = new ArrayList<>(listField.getListItems());
        
        // 如果没有找到列表项字段，使用默认的字段
        if (listItemFields.isEmpty()) {
            listItemFields.add("value");
        }
        
        // 创建表格列
        for (String itemField : listItemFields) {
            TableColumn<Map<String, String>, String> column = new TableColumn<>(itemField);
            column.setPrefWidth(150); // 设置宽度
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(itemField, "")));
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(event -> {
                Map<String, String> row = event.getRowValue();
                row.put(itemField, event.getNewValue());
                // 更新预览
                updatePreview();
            });
            table.getColumns().add(column);
        }
        
        // 设置表格可编辑
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // 添加操作列
        TableColumn<Map<String, String>, Void> operationColumn = new TableColumn<>("操作");
        operationColumn.setPrefWidth(100);
        
        operationColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("－");
            private final Button addButton = new Button("＋");
            private final HBox buttonBox = new HBox(5);
            
            {
                deleteButton.setStyle("-fx-background-color: white; -fx-text-fill: #E91E63; -fx-border-color: #E91E63; -fx-border-radius: 50%;");
                addButton.setStyle("-fx-background-color: white; -fx-text-fill: #4CAF50; -fx-border-color: #4CAF50; -fx-border-radius: 50%;");
                buttonBox.getChildren().addAll(addButton, deleteButton);
                buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                // 添加行事件
                addButton.setOnAction(event -> {
                    // 创建新行
                    Map<String, String> newRow = new HashMap<>();
                    for (String field : listItemFields) {
                        newRow.put(field, "");
                    }
                    listFieldDataMap.get(listField.getName()).add(newRow);
                    table.setItems(FXCollections.observableArrayList(listFieldDataMap.get(listField.getName())));
                    table.scrollTo(table.getItems().size() - 1);
                    updatePreview();
                });
                
                // 删除行事件
                deleteButton.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < listFieldDataMap.get(listField.getName()).size()) {
                        listFieldDataMap.get(listField.getName()).remove(index);
                        table.setItems(FXCollections.observableArrayList(listFieldDataMap.get(listField.getName())));
                        updatePreview();
                    }
                });
                
                setGraphic(buttonBox);
            }
        });
        
        table.getColumns().add(operationColumn);
        
        // 设置表格数据
        table.setItems(FXCollections.observableArrayList(listFieldDataMap.get(listField.getName())));
        
        // 如果没有数据，添加一个空行
        if (table.getItems().isEmpty()) {
            Map<String, String> newRow = new HashMap<>();
            for (String field : listItemFields) {
                newRow.put(field, "");
            }
            listFieldDataMap.get(listField.getName()).add(newRow);
            table.setItems(FXCollections.observableArrayList(listFieldDataMap.get(listField.getName())));
        }
        
        // 设置表格高度
        table.setPrefHeight(200);
        
        // 添加组件到容器
        container.getChildren().add(table);
        pane.setContent(container);
        
        // 添加到列表数据容器
        listDataContainer.getChildren().add(pane);
    }

    /**
     * 清空所有字段
     */
    private void clearAllFields() {
        // 清空字段列表
        objectFields.clear();
        fields.clear();
        
        // 清空数据映射
        fieldDataMap.clear();
        listFieldDataMap.clear();
        
        // 更新字段表格
        objectFieldsTable.getItems().clear();
        objectFieldsTable.refresh();
        
        // 更新列表字段表格
        if (listFieldsTable != null) {
            listFieldsTable.getItems().clear();
            listFieldsTable.refresh();
        }
        
        // 更新数据填充表格
        objectDataTable.getItems().clear();
        objectDataTable.refresh();
        
        // 清空列表数据容器
        listDataContainer.getChildren().clear();
    }

    /**
     * 更新对象数据表格
     */
    private void updateObjectDataTable() {
        // 更新数据填充表格
        objectDataTable.setItems(FXCollections.observableArrayList(objectFields));
        objectDataTable.refresh();
    }
} 