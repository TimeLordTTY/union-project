package com.timelordtty.docgen.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.Modality;
import javafx.stage.Window;

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
            listFieldItemsContainer.getChildren().remove(listContainer);
            listFieldDataMap.remove(listName);
            
            // 移除数据填充中对应的列表
            for (Node node : listDataItemsContainer.getChildren()) {
                if (node instanceof VBox && ("list_" + listName).equals(node.getId())) {
                    listDataItemsContainer.getChildren().remove(node);
                    break;
                }
            }
            
            if (updatePreviewCallback != null) {
                updatePreviewCallback.run();
            }
        });
        
        // 创建添加字段按钮
        Button addFieldButton = new Button("添加属性");
        addFieldButton.getStyleClass().add("small-button");
        addFieldButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("添加列表属性");
            dialog.setHeaderText(null);
            dialog.setContentText("请输入属性名称:");
            
            // 确保对话框是模态的
            Window activeWindow = null;
            try {
                activeWindow = javafx.stage.Stage.getWindows().stream()
                    .filter(Window::isFocused)
                    .findFirst()
                    .orElse(null);
            } catch (Exception ex) {
                // 忽略异常
            }
            
            if (activeWindow != null) {
                dialog.initOwner(activeWindow);
            }
            dialog.initModality(Modality.APPLICATION_MODAL);
            
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String fieldName = result.get().trim();
                
                // 获取列表的字段表格
                TableView<String> fieldsTable = null;
                for (Node child : listContainer.getChildren()) {
                    if (child instanceof TableView) {
                        @SuppressWarnings("unchecked")
                        TableView<String> table = (TableView<String>) child;
                        fieldsTable = table;
                        break;
                    }
                }
                
                if (fieldsTable != null) {
                    // 检查字段是否已存在
                    if (!fieldsTable.getItems().contains(fieldName)) {
                        fieldsTable.getItems().add(fieldName);
                        
                        // 更新数据表格
                        updateListDataTable(listName);
                    }
                }
            }
        });
        
        headerRow.getChildren().addAll(titleLabel, addFieldButton, removeButton);
        listContainer.getChildren().add(headerRow);
        
        // 创建字段列表表格
        TableView<String> fieldsTable = new TableView<>();
        fieldsTable.setEditable(true);
        fieldsTable.setPrefHeight(150); // 设置合适的高度
        
        TableColumn<String, String> fieldColumn = new TableColumn<>("属性名称");
        fieldColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        fieldsTable.getColumns().add(fieldColumn);
        
        // 添加字段删除列
        TableColumn<String, Void> deleteColumn = new TableColumn<>("");
        deleteColumn.setPrefWidth(50);
        deleteColumn.setCellFactory(param -> new javafx.scene.control.TableCell<String, Void>() {
            private final Button deleteButton = new Button("×");
            
            {
                deleteButton.getStyleClass().add("icon-button");
                deleteButton.setOnAction(event -> {
                    String fieldName = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(getIndex());
                    
                    // 更新数据表格
                    updateListDataTable(listName);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
        fieldsTable.getColumns().add(deleteColumn);
        
        // 添加初始字段
        ObservableList<String> fieldItems = FXCollections.observableArrayList(fields);
        fieldsTable.setItems(fieldItems);
        
        listContainer.getChildren().add(fieldsTable);
        listFieldItemsContainer.getChildren().add(listContainer);
        
        // 创建数据填充表格
        createListDataTable(listName);
        
        // 更新预览
        if (updatePreviewCallback != null) {
            updatePreviewCallback.run();
        }
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
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setSpacing(10);
        
        Label titleLabel = new Label(listName);
        titleLabel.getStyleClass().add("list-field-title");
        
        headerRow.getChildren().add(titleLabel);
        tableContainer.getChildren().add(headerRow);
        
        // 创建数据表格
        TableView<Map<String, String>> dataTable = new TableView<>();
        dataTable.setEditable(true);
        dataTable.setId("data_table_" + listName);
        
        // 根据字段创建列
        for (String field : listFields) {
            TableColumn<Map<String, String>, String> column = new TableColumn<>(field);
            column.setCellValueFactory(data -> {
                Map<String, String> rowData = data.getValue();
                String value = rowData.get(field);
                return new SimpleStringProperty(value);
            });
            
            // 设置单元格编辑工厂
            column.setCellFactory(col -> new javafx.scene.control.TableCell<Map<String, String>, String>() {
                private final TextField textField = new TextField();
                
                {
                    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal) {
                            commitEdit(textField.getText());
                        }
                    });
                    
                    textField.setOnKeyPressed(e -> {
                        if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                            commitEdit(textField.getText());
                        }
                    });
                }
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else if (isEditing()) {
                        setText(null);
                        textField.setText(item == null ? "" : item);
                        setGraphic(textField);
                    } else {
                        setText(item == null ? "" : item);
                        setGraphic(null);
                    }
                }
                
                @Override
                public void startEdit() {
                    super.startEdit();
                    textField.setText(getItem() == null ? "" : getItem());
                    setText(null);
                    setGraphic(textField);
                    textField.requestFocus();
                }
                
                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }
                
                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    int row = getTableRow().getIndex();
                    if (row >= 0 && row < getTableView().getItems().size()) {
                        Map<String, String> rowData = getTableView().getItems().get(row);
                        rowData.put(field, newValue);
                        
                        // 更新模型数据
                        if (listFieldDataMap.containsKey(listName) && 
                            row < listFieldDataMap.get(listName).size()) {
                            listFieldDataMap.get(listName).get(row).put(field, newValue);
                            
                            if (updatePreviewCallback != null) {
                                updatePreviewCallback.run();
                            }
                        }
                    }
                }
            });
            
            dataTable.getColumns().add(column);
        }
        
        // 添加操作列
        TableColumn<Map<String, String>, Void> actionColumn = new TableColumn<>("");
        actionColumn.setPrefWidth(50);
        actionColumn.setCellFactory(param -> new javafx.scene.control.TableCell<Map<String, String>, Void>() {
            private final Button addButton = new Button("+");
            
            {
                addButton.getStyleClass().add("icon-button");
                
                addButton.setOnAction(event -> {
                    int rowIndex = getIndex();
                    
                    // 创建新行（复制当前行的数据）
                    Map<String, String> newRowData = new HashMap<>();
                    Map<String, String> currentRowData = getTableView().getItems().get(rowIndex);
                    
                    // 初始化所有字段为空
                    for (String field : listFields) {
                        newRowData.put(field, "");
                    }
                    
                    // 在当前行之后插入新行
                    listFieldDataMap.get(listName).add(rowIndex + 1, newRowData);
                    updateListDataTable(listName);
                    
                    if (updatePreviewCallback != null) {
                        updatePreviewCallback.run();
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(addButton);
                }
            }
        });
        dataTable.getColumns().add(actionColumn);
        
        dataTable.setPrefHeight(200);
        tableContainer.getChildren().add(dataTable);
        listDataItemsContainer.getChildren().add(tableContainer);
        
        // 添加一行默认数据
        if (listFields.size() > 0) {
            Map<String, String> rowData = new HashMap<>();
            // 为每个字段创建默认空值
            for (String field : listFields) {
                rowData.put(field, "");
            }
            
            if (!listFieldDataMap.containsKey(listName)) {
                listFieldDataMap.put(listName, new ArrayList<>());
            }
            
            listFieldDataMap.get(listName).clear(); // 清除现有数据
            listFieldDataMap.get(listName).add(rowData); // 只添加一行
        }
        
        // 更新数据表格
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