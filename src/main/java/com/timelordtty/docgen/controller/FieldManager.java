package com.timelordtty.docgen.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.timelordtty.AppLogger;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;

/**
 * 字段管理器，负责管理文档生成器的字段和数据
 */
public class FieldManager {
    // 数据存储
    private Map<String, String> fieldDataMap = new HashMap<>();
    private Map<String, List<Map<String, String>>> listFieldDataMap = new HashMap<>();
    
    // UI容器
    private VBox objectFieldItemsContainer;
    private VBox listFieldItemsContainer;
    private VBox objectDataItemsContainer;
    private VBox listDataItemsContainer;
    
    // 更新预览回调
    private Runnable updatePreviewCallback;
    
    /**
     * 构造函数
     * 
     * @param objectFieldItemsContainer 对象字段容器
     * @param listFieldItemsContainer 列表字段容器
     * @param objectDataItemsContainer 对象数据容器
     * @param listDataItemsContainer 列表数据容器
     * @param updatePreviewCallback 更新预览的回调函数
     */
    public FieldManager(VBox objectFieldItemsContainer, VBox listFieldItemsContainer,
                        VBox objectDataItemsContainer, VBox listDataItemsContainer,
                        Runnable updatePreviewCallback) {
        this.objectFieldItemsContainer = objectFieldItemsContainer;
        this.listFieldItemsContainer = listFieldItemsContainer;
        this.objectDataItemsContainer = objectDataItemsContainer;
        this.listDataItemsContainer = listDataItemsContainer;
        this.updatePreviewCallback = updatePreviewCallback;
    }
    
    /**
     * 清空所有字段和数据
     */
    public void clearAll() {
        // 清空字段容器
        objectFieldItemsContainer.getChildren().clear();
        listFieldItemsContainer.getChildren().clear();
        
        // 清空数据容器
        objectDataItemsContainer.getChildren().clear();
        listDataItemsContainer.getChildren().clear();
        
        // 重置数据
        fieldDataMap.clear();
        listFieldDataMap.clear();
    }
    
    /**
     * 添加普通字段
     * 
     * @param fieldName 字段名称
     */
    public void addObjectField(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            UIHelper.showError("添加字段失败", "字段名不能为空");
            return;
        }
        
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
            
            // 移除数据字段
            for (Node node : objectDataItemsContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox box = (HBox) node;
                    for (Node child : box.getChildren()) {
                        if (child instanceof Label && ((Label) child).getText().equals(fieldName + ":")) {
                            objectDataItemsContainer.getChildren().remove(node);
                            break;
                        }
                    }
                }
            }
            
            if (updatePreviewCallback != null) {
                updatePreviewCallback.run();
            }
        });
        
        fieldItem.getChildren().addAll(nameLabel, placeholderLabel, deleteButton);
        objectFieldItemsContainer.getChildren().add(fieldItem);
        
        // 添加数据填充字段
        addObjectDataField(fieldName);
        
        // 更新预览
        if (updatePreviewCallback != null) {
            updatePreviewCallback.run();
        }
    }
    
    /**
     * 添加数据填充字段
     * 
     * @param fieldName 字段名称
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
            if (updatePreviewCallback != null) {
                updatePreviewCallback.run();
            }
        });
        
        // 初始默认值
        fieldDataMap.put(fieldName, "");
        
        dataItem.getChildren().addAll(nameLabel, valueField);
        objectDataItemsContainer.getChildren().add(dataItem);
    }
    
    /**
     * 添加数据填充字段，带初始值
     * 
     * @param fieldName 字段名称
     * @param fieldValue 字段初始值
     */
    public void addObjectDataField(String fieldName, String fieldValue) {
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
            if (updatePreviewCallback != null) {
                updatePreviewCallback.run();
            }
        });
        
        // 设置初始值
        fieldDataMap.put(fieldName, fieldValue);
        
        dataItem.getChildren().addAll(nameLabel, valueField);
        objectDataItemsContainer.getChildren().add(dataItem);
    }
    
    /**
     * 添加列表字段
     * 
     * @param listName 列表名称
     */
    public void addListField(String listName) {
        addListField(listName, new ArrayList<>());
    }
    
    /**
     * 添加列表字段，带字段列表
     * 
     * @param listName 列表名称
     * @param fields 字段列表
     */
    public void addListField(String listName, List<String> fields) {
        if (listName == null || listName.trim().isEmpty()) {
            UIHelper.showError("添加列表失败", "列表名不能为空");
            return;
        }
        
        // 检查该列表是否已存在
        for (Node node : listFieldItemsContainer.getChildren()) {
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
            for (Node node : listDataItemsContainer.getChildren()) {
                if (node instanceof VBox && node.getId() != null && node.getId().equals("list_" + listName)) {
                    listDataItemsContainer.getChildren().remove(node);
                    break;
                }
            }
            
            // 更新预览
            if (updatePreviewCallback != null) {
                updatePreviewCallback.run();
            }
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
                                if (updatePreviewCallback != null) {
                                    updatePreviewCallback.run();
                                }
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
                            if (updatePreviewCallback != null) {
                                updatePreviewCallback.run();
                            }
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
     * 
     * @param listName 列表名称
     */
    private void createListDataTable(String listName) {
        // 获取列表字段
        List<String> listFields = new ArrayList<>();
        for (Node node : listFieldItemsContainer.getChildren()) {
            if (node instanceof VBox && listName.equals(node.getId())) {
                VBox container = (VBox) node;
                for (Node child : container.getChildren()) {
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
                    if (updatePreviewCallback != null) {
                        updatePreviewCallback.run();
                    }
                });
                
                Button deleteRowButton = new Button("×");
                deleteRowButton.getStyleClass().add("icon-button");
                deleteRowButton.setOnAction(e -> {
                    int index = row.getIndex();
                    if (index >= 0 && index < dataTable.getItems().size()) {
                        listFieldDataMap.get(listName).remove(index);
                        updateListDataTable(listName);
                        if (updatePreviewCallback != null) {
                            updatePreviewCallback.run();
                        }
                    }
                });
                
                buttonContainer.getChildren().addAll(addRowButton, deleteRowButton);
                row.graphicProperty().bind(Bindings.when(row.emptyProperty())
                        .then((Node)null)
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
                        if (updatePreviewCallback != null) {
                            updatePreviewCallback.run();
                        }
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
                    if (updatePreviewCallback != null) {
                        updatePreviewCallback.run();
                    }
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
     * 
     * @param listName 列表名称
     */
    private void updateListDataTable(String listName) {
        // 查找并更新数据表格
        for (Node node : listDataItemsContainer.getChildren()) {
            if (node instanceof VBox && node.getId() != null && node.getId().equals("list_" + listName)) {
                VBox container = (VBox) node;
                
                // 找到表格
                for (Node child : container.getChildren()) {
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
     * 添加列表数据字段
     * 
     * @param listName 列表名称
     * @param itemsList 数据项列表
     */
    public void addListDataField(String listName, List<Map<String, String>> itemsList) {
        if (listFieldDataMap.containsKey(listName)) {
            listFieldDataMap.get(listName).addAll(itemsList);
        } else {
            listFieldDataMap.put(listName, new ArrayList<>(itemsList));
        }
        
        // 更新UI
        updateListDataTable(listName);
    }
    
    /**
     * 获取所有普通字段名称
     * 
     * @return 字段名称列表
     */
    public List<String> getObjectFieldNames() {
        List<String> fieldNames = new ArrayList<>();
        for (Node node : objectFieldItemsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                for (Node child : hbox.getChildren()) {
                    if (child instanceof Label) {
                        fieldNames.add(((Label) child).getText());
                        break;
                    }
                }
            }
        }
        return fieldNames;
    }
    
    /**
     * 获取所有列表字段名称
     * 
     * @return 列表字段名称
     */
    public List<String> getListFieldNames() {
        List<String> fieldNames = new ArrayList<>();
        for (Node node : listFieldItemsContainer.getChildren()) {
            if (node instanceof VBox) {
                VBox vbox = (VBox) node;
                fieldNames.add(vbox.getId());
            }
        }
        return fieldNames;
    }
    
    /**
     * 获取字段数据映射
     * 
     * @return 字段数据映射
     */
    public Map<String, String> getFieldDataMap() {
        return fieldDataMap;
    }
    
    /**
     * 获取列表字段数据映射
     * 
     * @return 列表字段数据映射
     */
    public Map<String, List<Map<String, String>>> getListFieldDataMap() {
        return listFieldDataMap;
    }
    
    /**
     * 设置字段数据映射
     * 
     * @param fieldDataMap 字段数据映射
     */
    public void setFieldDataMap(Map<String, String> fieldDataMap) {
        this.fieldDataMap = fieldDataMap;
    }
    
    /**
     * 设置列表字段数据映射
     * 
     * @param listFieldDataMap 列表字段数据映射
     */
    public void setListFieldDataMap(Map<String, List<Map<String, String>>> listFieldDataMap) {
        this.listFieldDataMap = listFieldDataMap;
    }
} 