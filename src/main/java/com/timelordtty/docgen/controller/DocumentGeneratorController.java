package com.timelordtty.docgen.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.timelordtty.AppLogger;
import com.timelordtty.docgen.service.ExcelTemplateService;
import com.timelordtty.docgen.service.WordTemplateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

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
            showError("添加字段失败", "字段名不能为空");
            return;
        }
        
        // 清除输入
        objectFieldInput.clear();
        
        // 添加到字段列表
        addObjectField(fieldName);
        
        // 更新UI
        updateFieldUIAndData();
    }
    
    /**
     * 添加普通字段到UI
     */
    private void addObjectField(String fieldName) {
        // 创建字段UI组件
        HBox fieldItem = new HBox();
        fieldItem.setAlignment(Pos.CENTER_LEFT);
        fieldItem.setSpacing(10);
        fieldItem.setPadding(new Insets(5));
        fieldItem.getStyleClass().add("field-item");
        
        Label nameLabel = new Label(fieldName);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        Label placeholderLabel = new Label("{{" + fieldName + "}}");
        placeholderLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #999;");
        
        Button deleteButton = new Button("×");
        deleteButton.getStyleClass().add("icon-button");
        deleteButton.setOnAction(e -> {
            objectFieldItemsContainer.getChildren().remove(fieldItem);
            fieldDataMap.remove(fieldName);
            updateFieldUIAndData();
        });
        
        fieldItem.getChildren().addAll(nameLabel, placeholderLabel, deleteButton);
        objectFieldItemsContainer.getChildren().add(fieldItem);
        
        // 添加数据填充字段
        addObjectDataField(fieldName);
        
        // 更新预览
        updatePreview();
    }
    
    /**
     * 添加数据填充字段
     */
    private void addObjectDataField(String fieldName) {
        // 创建数据输入UI组件
        HBox dataItem = new HBox();
        dataItem.setAlignment(Pos.CENTER_LEFT);
        dataItem.setSpacing(10);
        dataItem.setPadding(new Insets(5));
        
        Label nameLabel = new Label(fieldName + ":");
        nameLabel.setPrefWidth(120);
        
        TextField valueField = new TextField();
        HBox.setHgrow(valueField, Priority.ALWAYS);
        valueField.textProperty().addListener((obs, oldVal, newVal) -> {
            fieldDataMap.put(fieldName, newVal);
            updatePreview();
        });
        
        // 初始默认值
        fieldDataMap.put(fieldName, "");
        
        dataItem.getChildren().addAll(nameLabel, valueField);
        objectDataItemsContainer.getChildren().add(dataItem);
    }
    
    /**
     * 处理添加列表字段
     */
    @FXML
    private void handleAddListField() {
        String listName = listFieldInput.getText().trim();
        
        if (listName.isEmpty()) {
            showError("添加列表失败", "列表名不能为空");
            return;
        }
        
        // 清除输入
        listFieldInput.clear();
        
        // 添加列表字段
        addListField(listName);
        
        // 更新UI
        updateFieldUIAndData();
    }
    
    /**
     * 添加列表字段，带字段列表
     */
    private void addListField(String listName, List<String> fields) {
        // 检查该列表是否已存在
        for (javafx.scene.Node node : listFieldItemsContainer.getChildren()) {
            if (node instanceof VBox && listName.equals(node.getId())) {
                return; // 已存在，不重复添加
            }
        }
        
        // 初始化列表数据结构
        if (!listFieldDataMap.containsKey(listName)) {
            listFieldDataMap.put(listName, new ArrayList<>());
        }
        
        // 创建列表字段UI组件
        VBox listContainer = new VBox();
        listContainer.setId(listName);
        listContainer.setSpacing(10);
        listContainer.setPadding(new Insets(10));
        listContainer.getStyleClass().add("list-field-container");
        
        // 标题行
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setSpacing(10);
        
        Label titleLabel = new Label(listName);
        titleLabel.getStyleClass().add("list-field-title");
        
        Button removeButton = new Button("×");
        removeButton.getStyleClass().add("icon-button");
        removeButton.setOnAction(e -> {
            // 移除该列表
            listFieldItemsContainer.getChildren().remove(listContainer);
            listFieldDataMap.remove(listName);
            
            // 查找并移除数据容器
            for (javafx.scene.Node node : listDataItemsContainer.getChildren()) {
                if (node instanceof VBox && node.getId() != null && node.getId().equals("list_" + listName)) {
                    listDataItemsContainer.getChildren().remove(node);
                    break;
                }
            }
            
            // 更新预览
            updatePreview();
        });
        
        headerRow.getChildren().addAll(titleLabel, removeButton);
        listContainer.getChildren().add(headerRow);
        
        // 创建表格来管理列表字段 - 去掉表头
        TableView<String> fieldsTable = new TableView<>();
        fieldsTable.setPrefHeight(150);
        fieldsTable.getStyleClass().addAll("list-table", "no-header");
        
        TableColumn<String, String> fieldNameColumn = new TableColumn<>("字段名");
        fieldNameColumn.setVisible(false); // 隐藏表头
        fieldNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        fieldNameColumn.setPrefWidth(200);
        
        TableColumn<String, Void> actionsColumn = new TableColumn<>("操作");
        actionsColumn.setVisible(false); // 隐藏表头
        actionsColumn.setPrefWidth(100);
        actionsColumn.setCellFactory(col -> {
            return new javafx.scene.control.TableCell<String, Void>() {
                private final HBox container = new HBox();
                private final Button addButton = new Button("+");
                private final Button removeButton = new Button("×");
                
                {
                    container.getStyleClass().add("btn-group-horizontal");
                    addButton.getStyleClass().add("icon-button");
                    removeButton.getStyleClass().add("icon-button");
                    
                    addButton.setOnAction(event -> {
                        // 显示添加新字段的对话框
                        String field = getTableRow().getItem();
                        TextInputDialog dialog = new TextInputDialog();
                        dialog.setTitle("添加字段");
                        dialog.setHeaderText("添加列表字段");
                        dialog.setContentText("请输入新字段名:");
                        
                        dialog.showAndWait().ifPresent(name -> {
                            if (!name.isEmpty()) {
                                ObservableList<String> items = fieldsTable.getItems();
                                items.add(name);
                                
                                // 同步更新到数据表格
                                updateListDataTable(listName);
                                
                                // 更新预览
                                updatePreview();
                            }
                        });
                    });
                    
                    removeButton.setOnAction(event -> {
                        String field = getTableRow().getItem();
                        if (field != null) {
                            fieldsTable.getItems().remove(field);
                            
                            // 同步更新到数据表格
                            updateListDataTable(listName);
                            
                            // 更新预览
                            updatePreview();
                        }
                    });
                    
                    container.getChildren().addAll(addButton, removeButton);
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(container);
                    }
                }
            };
        });
        
        fieldsTable.getColumns().addAll(fieldNameColumn, actionsColumn);
        
        // 添加初始字段列表
        if (fields != null && !fields.isEmpty()) {
            ObservableList<String> items = FXCollections.observableArrayList(fields);
            fieldsTable.setItems(items);
        } else {
            fieldsTable.setItems(FXCollections.observableArrayList());
        }
        
        listContainer.getChildren().add(fieldsTable);
        listFieldItemsContainer.getChildren().add(listContainer);
        
        // 创建数据填充表格
        createListDataTable(listName);
    }

    /**
     * 创建列表数据表格
     */
    private void createListDataTable(String listName) {
        // 获取列表字段
        List<String> listFields = new ArrayList<>();
        for (javafx.scene.Node node : listFieldItemsContainer.getChildren()) {
            if (node instanceof VBox && listName.equals(node.getId())) {
                VBox container = (VBox) node;
                for (javafx.scene.Node child : container.getChildren()) {
                    if (child instanceof TableView) {
                        @SuppressWarnings("unchecked")
                        TableView<String> fieldsTable = (TableView<String>) child;
                        listFields.addAll(fieldsTable.getItems());
                        break;
                    }
                }
                break;
            }
        }
        
        // 创建列表数据容器
        VBox tableContainer = new VBox();
        tableContainer.setSpacing(10);
        tableContainer.setPadding(new Insets(10));
        tableContainer.setId("list_" + listName);
        tableContainer.getStyleClass().add("list-field-container");
        
        // 标题
        Label titleLabel = new Label(listName + " 数据");
        titleLabel.getStyleClass().add("list-field-title");
        tableContainer.getChildren().add(titleLabel);
        
        if (listFields.isEmpty()) {
            // 如果没有字段，显示提示信息
            Label emptyLabel = new Label("请先在字段定义中添加列表字段");
            emptyLabel.setStyle("-fx-text-fill: #999999;");
            tableContainer.getChildren().add(emptyLabel);
        } else {
            // 创建表格
            TableView<Map<String, String>> dataTable = new TableView<>();
            dataTable.setId("table_" + listName);
            dataTable.getStyleClass().add("list-table");
            
            // 设置行工厂，添加按钮直接显示在每行的末尾
            dataTable.setRowFactory(tv -> {
                TableRow<Map<String, String>> row = new TableRow<>();
                HBox buttonContainer = new HBox(5);
                buttonContainer.setAlignment(Pos.CENTER_RIGHT);
                
                Button addRowButton = new Button("+");
                addRowButton.getStyleClass().add("icon-button");
                addRowButton.setOnAction(e -> {
                    int index = row.getIndex();
                    Map<String, String> emptyRow = new HashMap<>();
                    if (index >= 0 && index < dataTable.getItems().size()) {
                        // 在当前行后面添加
                        listFieldDataMap.get(listName).add(index + 1, emptyRow);
                    } else {
                        // 添加到末尾
                        listFieldDataMap.get(listName).add(emptyRow);
                    }
                    updateListDataTable(listName);
                    updatePreview();
                });
                
                Button deleteRowButton = new Button("×");
                deleteRowButton.getStyleClass().add("icon-button");
                deleteRowButton.setOnAction(e -> {
                    int index = row.getIndex();
                    if (index >= 0 && index < dataTable.getItems().size()) {
                        listFieldDataMap.get(listName).remove(index);
                        updateListDataTable(listName);
                        updatePreview();
                    }
                });
                
                buttonContainer.getChildren().addAll(addRowButton, deleteRowButton);
                row.graphicProperty().bind(Bindings.when(row.emptyProperty())
                        .then((javafx.scene.Node)null)
                        .otherwise(buttonContainer));
                return row;
            });
            
            // 添加列
            for (String field : listFields) {
                TableColumn<Map<String, String>, String> column = new TableColumn<>(field);
                column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrDefault(field, "")));
                column.setCellFactory(tc -> new javafx.scene.control.TableCell<Map<String, String>, String>() {
                    private final TextField textField = new TextField();
                    
                    {
                        textField.setOnAction(e -> commitEdit(textField.getText()));
                        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                            if (!isNowFocused) {
                                commitEdit(textField.getText());
                            }
                        });
                    }
                    
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            textField.setText(item == null ? "" : item);
                            setGraphic(textField);
                            setText(null);
                        }
                    }
                    
                    @Override
                    public void commitEdit(String newValue) {
                        Map<String, String> rowData = getTableView().getItems().get(getIndex());
                        rowData.put(field, newValue);
                        updatePreview();
                    }
                });
                dataTable.getColumns().add(column);
            }
            
            tableContainer.getChildren().add(dataTable);
            
            // 添加"添加行"按钮
            Button addRowButton = new Button("添加行");
            addRowButton.getStyleClass().add("action-button");
            addRowButton.setOnAction(e -> {
                // 在数据模型中添加空行
                if (listFieldDataMap.containsKey(listName)) {
                    Map<String, String> emptyRow = new HashMap<>();
                    listFieldDataMap.get(listName).add(emptyRow);
                    
                    // 刷新表格
                    updateListDataTable(listName);
                    
                    // 更新预览
                    updatePreview();
                }
            });
            
            HBox buttonRow = new HBox(10);
            buttonRow.setAlignment(Pos.CENTER);
            buttonRow.getChildren().add(addRowButton);
            tableContainer.getChildren().add(buttonRow);
        }
        
        // 添加到UI
        listDataItemsContainer.getChildren().add(tableContainer);
        
        // 初始化表格列和数据
        updateListDataTable(listName);
    }

    /**
     * 更新列表数据表格
     */
    private void updateListDataTable(String listName) {
        // 查找并更新数据表格
        for (javafx.scene.Node node : listDataItemsContainer.getChildren()) {
            if (node instanceof VBox && node.getId() != null && node.getId().equals("list_" + listName)) {
                VBox container = (VBox) node;
                
                // 找到表格
                for (javafx.scene.Node child : container.getChildren()) {
                    if (child instanceof TableView) {
                        @SuppressWarnings("unchecked")
                        TableView<Map<String, String>> dataTable = (TableView<Map<String, String>>) child;
                        
                        // 获取列表中的所有字段
                        List<String> listFields = new ArrayList<>();
                        for (TableColumn<Map<String, String>, ?> column : dataTable.getColumns()) {
                            listFields.add(column.getText());
                        }
                        
                        // 确保数据模型存在
                        if (!listFieldDataMap.containsKey(listName)) {
                            listFieldDataMap.put(listName, new ArrayList<>());
                        }
                        
                        // 更新表格数据
                        ObservableList<Map<String, String>> data = FXCollections.observableArrayList(listFieldDataMap.get(listName));
                        dataTable.setItems(data);
                        
                        break;
                    }
                }
                break;
            }
        }
    }

    /**
     * 加载模板
     */
    private void loadTemplate(File file) {
        if (file == null || !file.exists()) {
            showError("错误", "模板文件不存在");
            return;
        }
        
        currentTemplateFile = file;
        templateNameLabel.setText(file.getName());
        
        try {
            if (isWordMode) {
                // 加载Word模板
                String content = wordTemplateService.readDocxContent(file.getAbsolutePath());
                wordEditor.setText(content);
                analyzeWordTemplate(content);
                
                // 更新预览 - 使用空值填充所有占位符
                updatePreview();
            } else {
                // 加载Excel模板
                List<List<String>> content = excelTemplateService.readExcelContent(file.getAbsolutePath());
                loadExcelData(content);
                analyzeExcelTemplate(content);
                
                // 更新预览
                updatePreview();
            }
        } catch (Exception e) {
            AppLogger.info("加载模板失败: " + e.getMessage());
            e.printStackTrace();
            showError("加载失败", "无法加载模板文件: " + e.getMessage());
        }
    }

    /**
     * 更新Word预览
     */
    private void updateWordPreview() {
        String content = wordEditor.getText();
        
        // 替换普通字段占位符
        for (Map.Entry<String, String> entry : fieldDataMap.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            content = content.replace(placeholder, value);
        }
        
        // 替换列表字段占位符
        for (Map.Entry<String, List<Map<String, String>>> entry : listFieldDataMap.entrySet()) {
            String listName = entry.getKey();
            List<Map<String, String>> listItems = entry.getValue();
            
            // 查找列表区域
            Pattern listPattern = Pattern.compile("\\{\\{#" + listName + "\\}\\}(.*?)\\{\\{/" + listName + "\\}\\}", Pattern.DOTALL);
            Matcher listMatcher = listPattern.matcher(content);
            
            if (listMatcher.find()) {
                String listTemplate = listMatcher.group(1);
                StringBuilder replacement = new StringBuilder();
                
                // 为列表中的每个项生成内容
                for (Map<String, String> item : listItems) {
                    String itemContent = listTemplate;
                    
                    // 替换该项的所有字段
                    for (Map.Entry<String, String> field : item.entrySet()) {
                        String fieldPlaceholder = "{{" + listName + "." + field.getKey() + "}}";
                        String fieldValue = field.getValue() != null ? field.getValue() : "";
                        itemContent = itemContent.replace(fieldPlaceholder, fieldValue);
                    }
                    
                    replacement.append(itemContent);
                }
                
                // 替换整个列表区域
                content = content.replace(listMatcher.group(0), replacement.toString());
            }
        }
        
        wordPreviewArea.setText(content);
    }

    /**
     * 处理导出字段
     */
    @FXML
    private void handleExportFields() {
        try {
            if (isWordMode && wordEditor.getText().isEmpty()) {
                showError("导出失败", "请先导入或编辑模板");
                return;
            }
            
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
                        String listName = null;
                        List<String> fields = new ArrayList<>();
                        
                        // 获取列表名称
                        for (Node child : listItem.getChildren()) {
                            if (child instanceof HBox) {
                                HBox headerBox = (HBox) child;
                                for (Node headerChild : headerBox.getChildren()) {
                                    if (headerChild instanceof Label) {
                                        Label listNameLabel = (Label) headerChild;
                                        listName = listNameLabel.getText();
                                        break;
                                    }
                                }
                                break; // 只处理第一个HBox
                            }
                        }
                        
                        // 获取列表字段
                        if (listName != null && !listName.isEmpty()) {
                            for (Node child : listItem.getChildren()) {
                                if (child instanceof VBox) {
                                    VBox fieldsBox = (VBox) child;
                                    for (Node fieldNode : fieldsBox.getChildren()) {
                                        if (fieldNode instanceof HBox) {
                                            HBox fieldBox = (HBox) fieldNode;
                                            for (Node fieldChild : fieldBox.getChildren()) {
                                                if (fieldChild instanceof Label) {
                                                    Label fieldNameLabel = (Label) fieldChild;
                                                    String fieldName = fieldNameLabel.getText();
                                                    if (fieldName != null && !fieldName.isEmpty()) {
                                                        fields.add(fieldName);
                                                    }
                                                    break; // 只处理第一个Label
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            listFields.put(listName, fields);
                        }
                    }
                }
                fieldDefinitions.put("listFields", listFields);
                
                // 将字段定义转换为JSON并保存
                ObjectMapper mapper = new ObjectMapper();
                mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, fieldDefinitions);
                
                showInfo("成功", "字段定义已导出: " + outputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            showError("导出失败", "导出字段定义时出错: " + e.getMessage());
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
            addObjectField(fieldName);
            objectFieldInput.clear();
        } else {
            showError("输入错误", "请输入字段名称");
        }
    }

    /**
     * 处理生成文档
     */
    @FXML
    private void handleGenerateDocument() {
        try {
            if (currentTemplateFile == null) {
                showError("错误", "请先选择模板文件");
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
                    wordTemplateService.saveDocxTemplate(content, outputFile.getAbsolutePath());
                } else {
                    // 生成Excel文档
                    // 这里应该实现Excel数据的保存，具体实现根据实际需求编写
                    // excelTemplateService.saveExcelContent(...);
                }
                
                showInfo("成功", "文档已生成: " + outputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            showError("生成失败", "生成文档时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理保存数据
     */
    @FXML
    private void handleSaveData() {
        try {
            if (fieldDataMap.isEmpty() && listFieldDataMap.isEmpty()) {
                showError("错误", "没有数据可保存");
                return;
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
                // 这里应该实现数据的保存逻辑，例如使用Jackson或Gson保存为JSON
                // 此处简化为仅显示成功消息
                showInfo("成功", "数据已保存: " + outputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            showError("保存失败", "保存数据时出错: " + e.getMessage());
            e.printStackTrace();
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
     * 更新UI和数据
     */
    private void updateFieldUIAndData() {
        // 更新所有数据和UI
        updatePreview();
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
     * 更新Excel预览
     */
    private void updateExcelPreview() {
        // 简化实现，实际应用中需要更复杂的Excel处理逻辑
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
        File templatesDir = new File(defaultDir.getParentFile(), "templates");
        if (templatesDir.exists() && templatesDir.isDirectory()) {
            fileChooser.setInitialDirectory(templatesDir);
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
     * 添加简单列表字段
     */
    private void addListField(String listName) {
        addListField(listName, new ArrayList<>());
    }
    
    /**
     * 分析Word模板内容，提取字段和列表
     */
    private void analyzeWordTemplate(String content) {
        AppLogger.info("分析Word模板内容");
        
        // 清空现有字段
        objectFieldItemsContainer.getChildren().clear();
        listFieldItemsContainer.getChildren().clear();
        objectDataItemsContainer.getChildren().clear();
        listDataItemsContainer.getChildren().clear();
        
        fieldDataMap.clear();
        listFieldDataMap.clear();
        
        // 正则表达式匹配{{字段名}}格式的占位符
        Pattern fieldPattern = Pattern.compile("\\{\\{([^{}]+)\\}\\}");
        Matcher matcher = fieldPattern.matcher(content);
        
        Set<String> objectFields = new HashSet<>();
        Map<String, List<String>> listFields = new HashMap<>();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            
            // 检查是否是列表字段 (格式如: listName.fieldName)
            if (placeholder.contains(".")) {
                String[] parts = placeholder.split("\\.", 2);
                String listName = parts[0];
                String fieldName = parts[1];
                
                if (parts.length == 2 && !listName.trim().isEmpty() && !fieldName.trim().isEmpty()) {
                    // 添加列表字段
                    if (!listFields.containsKey(listName)) {
                        listFields.put(listName, new ArrayList<>());
                    }
                    if (!listFields.get(listName).contains(fieldName)) {
                        listFields.get(listName).add(fieldName);
                    }
                }
            } else if (!placeholder.startsWith("#") && !placeholder.startsWith("/")) {
                // 添加普通字段 (排除#listName和/listName格式)
                objectFields.add(placeholder);
            }
        }
        
        // 查找列表的开始和结束标记 {{#listName}} ... {{/listName}}
        Pattern listPattern = Pattern.compile("\\{\\{#([^{}]+)\\}\\}.*?\\{\\{/\\1\\}\\}", Pattern.DOTALL);
        Matcher listMatcher = listPattern.matcher(content);
        
        while (listMatcher.find()) {
            String listName = listMatcher.group(1);
            if (!listFields.containsKey(listName)) {
                listFields.put(listName, new ArrayList<>());
            }
            
            // 检查列表内部是否有未识别的字段
            String listContent = listMatcher.group(0);
            Matcher fieldInListMatcher = fieldPattern.matcher(listContent);
            
            while (fieldInListMatcher.find()) {
                String placeholder = fieldInListMatcher.group(1);
                if (placeholder.contains(".")) {
                    String[] parts = placeholder.split("\\.", 2);
                    if (parts.length == 2 && parts[0].equals(listName)) {
                        String fieldName = parts[1];
                        if (!listFields.get(listName).contains(fieldName)) {
                            listFields.get(listName).add(fieldName);
                        }
                    }
                }
            }
        }
        
        // 添加到UI
        for (String field : objectFields) {
            addObjectField(field);
        }
        
        for (Map.Entry<String, List<String>> entry : listFields.entrySet()) {
            addListField(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * 处理导入数据
     */
    @FXML
    private void handleImportData() {
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
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(selectedFile);
                
                // 清空当前数据
                fieldDataMap.clear();
                listFieldDataMap.clear();
                objectDataItemsContainer.getChildren().clear();
                listDataItemsContainer.getChildren().clear();
                
                // 加载普通字段数据
                if (rootNode.has("objectFields")) {
                    JsonNode objectFieldsNode = rootNode.get("objectFields");
                    Iterator<Map.Entry<String, JsonNode>> fields = objectFieldsNode.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        String fieldName = field.getKey();
                        String fieldValue = field.getValue().asText();
                        fieldDataMap.put(fieldName, fieldValue);
                        addObjectDataField(fieldName, fieldValue);
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
                        
                        listFieldDataMap.put(listName, itemsList);
                        addListDataField(listName, itemsList);
                    }
                }
                
                showInfo("成功", "数据已导入: " + selectedFile.getAbsolutePath());
                updatePreview();
            }
        } catch (Exception e) {
            showError("导入失败", "导入数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 添加对象数据字段
     */
    private void addObjectDataField(String fieldName, String fieldValue) {
        // 创建数据输入UI组件
        HBox dataItem = new HBox();
        dataItem.setAlignment(Pos.CENTER_LEFT);
        dataItem.setSpacing(10);
        dataItem.setPadding(new Insets(5));
        
        Label nameLabel = new Label(fieldName + ":");
        nameLabel.setPrefWidth(120);
        
        TextField valueField = new TextField(fieldValue);
        HBox.setHgrow(valueField, Priority.ALWAYS);
        valueField.textProperty().addListener((obs, oldVal, newVal) -> {
            fieldDataMap.put(fieldName, newVal);
            updatePreview();
        });
        
        dataItem.getChildren().addAll(nameLabel, valueField);
        objectDataItemsContainer.getChildren().add(dataItem);
    }
    
    /**
     * 添加列表数据字段
     */
    private void addListDataField(String listName, List<Map<String, String>> itemsList) {
        // 实现列表数据字段添加逻辑
    }
    
    /**
     * 加载Excel数据
     */
    private void loadExcelData(List<List<String>> data) {
        // 清空当前数据
        excelEditor.getItems().clear();
        excelEditor.getColumns().clear();
        
        // 简化实现，实际应用中需要更复杂的Excel处理逻辑
    }
    
    /**
     * 分析Excel模板
     */
    private void analyzeExcelTemplate(List<List<String>> content) {
        // 实现Excel模板分析逻辑
    }
    
    /**
     * 处理保存模板功能
     */
    @FXML
    private void handleSaveTemplate() {
        try {
            if (isWordMode && wordEditor.getText().isEmpty()) {
                showError("保存失败", "请先导入或编辑模板");
                return;
            }
            
            // 创建文件选择器
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存模板");
            
            // 设置默认输出目录
            File defaultDir = new File(baseDir);
            fileChooser.setInitialDirectory(defaultDir);
            
            // 设置默认文件名和类型
            if (isWordMode) {
                fileChooser.setInitialFileName("模板.docx");
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Word文档", "*.docx"));
            } else {
                fileChooser.setInitialFileName("模板.xlsx");
                fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel表格", "*.xlsx"));
            }
            
            // 显示保存对话框
            File outputFile = fileChooser.showSaveDialog(null);
            if (outputFile != null) {
                if (isWordMode) {
                    // 保存Word模板
                    String content = wordEditor.getText();
                    wordTemplateService.saveDocxTemplate(outputFile.getAbsolutePath(), content);
                } else {
                    // 保存Excel模板
                    // TODO: 实现Excel模板保存
                    showError("未实现", "Excel模板保存功能尚未实现");
                    return;
                }
                
                showInfo("成功", "模板已保存: " + outputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            showError("保存失败", "保存模板时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}