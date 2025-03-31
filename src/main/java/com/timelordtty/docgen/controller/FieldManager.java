package com.timelordtty.docgen.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.timelordtty.docgen.UIHelper;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
        fieldItem.setSpacing(2);
        fieldItem.setPadding(new Insets(0, 2, 0, 2)); // 极小的内边距
        fieldItem.getStyleClass().addAll("field-item", "compact-hbox");
        fieldItem.setMinHeight(20);
        fieldItem.setMaxHeight(20);
        fieldItem.setPrefHeight(20);
        
        Label nameLabel = new Label(fieldName);
        nameLabel.getStyleClass().add("field-label");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        // 为nameLabel添加悬浮提示
        Tooltip nameTooltip = new Tooltip("字段名: " + fieldName + "\n占位符: {{" + fieldName + "}}");
        Tooltip.install(nameLabel, nameTooltip);
        
        Label placeholderLabel = new Label("{{" + fieldName + "}}");
        placeholderLabel.getStyleClass().add("field-label");
        placeholderLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #999;");
        
        // 为占位符标签添加悬浮提示
        Tooltip placeholderTooltip = new Tooltip("{{" + fieldName + "}}");
        Tooltip.install(placeholderLabel, placeholderTooltip);
        
        Button deleteButton = new Button("×");
        deleteButton.getStyleClass().add("operation-delete-button");
        deleteButton.setMinSize(18, 18);
        deleteButton.setMaxSize(18, 18);
        deleteButton.setPrefSize(18, 18);
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
        dataItem.setSpacing(2);
        dataItem.setPadding(new Insets(0, 2, 0, 2)); // 极小的内边距
        dataItem.getStyleClass().addAll("data-field-container", "compact-hbox");
        dataItem.setMinHeight(20);
        dataItem.setMaxHeight(20);
        dataItem.setPrefHeight(20);
        
        Label nameLabel = new Label(fieldName + ":");
        nameLabel.getStyleClass().add("field-label");
        nameLabel.setPrefWidth(100);
        
        // 添加悬浮提示
        Tooltip fieldTooltip = new Tooltip("字段名: " + fieldName);
        Tooltip.install(nameLabel, fieldTooltip);
        
        TextField valueField = new TextField();
        valueField.getStyleClass().add("reduced-height-field");
        HBox.setHgrow(valueField, Priority.ALWAYS);
        
        // 添加失焦保存功能
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
        dataItem.setSpacing(2);
        dataItem.setPadding(new Insets(0, 2, 0, 2)); // 极小的内边距
        dataItem.getStyleClass().addAll("data-field-container", "compact-hbox");
        dataItem.setMinHeight(20);
        dataItem.setMaxHeight(20);
        dataItem.setPrefHeight(20);
        
        Label nameLabel = new Label(fieldName + ":");
        nameLabel.getStyleClass().add("field-label");
        nameLabel.setPrefWidth(100);
        
        TextField valueField = new TextField(fieldValue);
        valueField.getStyleClass().add("reduced-height-field");
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
        
        //
        /* 第1部分: 创建列表字段容器 */
        
        // 初始化列表数据结构
        if (!listFieldDataMap.containsKey(listName)) {
            listFieldDataMap.put(listName, new ArrayList<>());
        }
        
        // 创建列表字段UI组件
        VBox listContainer = new VBox();
        listContainer.setId(listName);
        listContainer.setSpacing(3); // 减少间距，使显示更紧凑
        listContainer.setPadding(new Insets(3)); // 减少内边距
        listContainer.getStyleClass().add("list-field-container");
        
        // 标题行
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setSpacing(10);
        
        Label titleLabel = new Label(listName);
        titleLabel.getStyleClass().add("list-field-title");
        
        // 为列表标题添加悬浮提示
        Tooltip listTooltip = new Tooltip("列表名: " + listName + "\n占位符: {{#" + listName + "}} {{/" + listName + "}}");
        Tooltip.install(titleLabel, listTooltip);
        
        // 添加列表占位符
        Label placeholderLabel = new Label("{{#" + listName + "}} {{/" + listName + "}}");
        placeholderLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #999;");
        HBox.setHgrow(placeholderLabel, Priority.ALWAYS);
        
        // 为占位符标签添加悬浮提示
        Tooltip placeholderTooltip = new Tooltip("{{#" + listName + "}} {{/" + listName + "}}");
        Tooltip.install(placeholderLabel, placeholderTooltip);
        
        Button removeListButton = new Button("×");
        removeListButton.getStyleClass().add("operation-delete-button");
        removeListButton.setOnAction(e -> {
            // 移除该列表的所有相关组件
            listFieldItemsContainer.getChildren().remove(listContainer);
            
            // 从数据容器中移除对应的表格
            for (int i = 0; i < listDataItemsContainer.getChildren().size(); i++) {
                Node node = listDataItemsContainer.getChildren().get(i);
                if (node instanceof VBox && node.getId() != null && node.getId().equals(listName + "_data")) {
                    listDataItemsContainer.getChildren().remove(i);
                    break;
                }
            }
            
            // 清除列表数据
            listFieldDataMap.remove(listName);
            
            if (updatePreviewCallback != null) {
                updatePreviewCallback.run();
            }
        });
        
        headerRow.getChildren().addAll(titleLabel, placeholderLabel, removeListButton);
        listContainer.getChildren().add(headerRow);
        
        // 添加字段输入功能
        TableView<String> fieldsTable = new TableView<>();
        fieldsTable.setEditable(true);
        fieldsTable.setPrefHeight(120); // 减少表格高度
        fieldsTable.setFixedCellSize(24); // 设置单元格固定高度
        
        // 隐藏表头
        fieldsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        fieldsTable.getStyleClass().add("no-header");
        fieldsTable.setStyle("-fx-border-color: #ddd; -fx-background-color: white; "
                          + "-fx-control-inner-background: white; "
                          + "-fx-table-header-border-color: transparent; "
                          + "-fx-table-cell-border-color: transparent; "
                          + "-fx-scroll-bar-visible: false;");
        
        // 使用CSS样式隐藏表头
        fieldsTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (fieldsTable.lookup(".column-header-background") != null) {
                fieldsTable.lookup(".column-header-background").setStyle("-fx-max-height: 0; -fx-pref-height: 0; -fx-min-height: 0;");
            }
        });
        
        TableColumn<String, String> fieldNameColumn = new TableColumn<>("字段名");
        fieldNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        fieldNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        fieldNameColumn.setOnEditCommit(event -> {
            int index = event.getTablePosition().getRow();
            String oldValue = event.getOldValue();
            String newValue = event.getNewValue();
            
            // 更新表格数据
            fieldsTable.getItems().set(index, newValue);
            
            // 更新列表数据中的字段名
            updateListFieldName(listName, oldValue, newValue);
            
            if (updatePreviewCallback != null) {
                updatePreviewCallback.run();
            }
        });
        fieldNameColumn.setPrefWidth(150);
        
        TableColumn<String, String> placeholderColumn = new TableColumn<>("占位符");
        placeholderColumn.setCellValueFactory(data -> new SimpleStringProperty("{{" + listName + "." + data.getValue() + "}}"));
        placeholderColumn.setCellFactory(param -> new javafx.scene.control.TableCell<String, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // 使用与其他占位符相同的灰色斜体样式
                    setText(item);
                    setStyle("-fx-font-style: italic; -fx-text-fill: #999;");
                    
                    // 为占位符单元格添加工具提示
                    Tooltip tooltip = new Tooltip(item);
                    Tooltip.install(this, tooltip);
                }
            }
        });
        placeholderColumn.setPrefWidth(150);
        
        TableColumn<String, Void> actionColumn = new TableColumn<>("操作");
        actionColumn.setPrefWidth(80);
        actionColumn.setCellFactory(param -> new javafx.scene.control.TableCell<String, Void>() {
            private final Button addButton = new Button("+");
            private final Button deleteButton = new Button("×");
            private final HBox actionBox = new HBox(5, addButton, deleteButton);
            
            {
                addButton.getStyleClass().add("operation-add-button");
                deleteButton.getStyleClass().add("operation-delete-button");
                
                // 添加按钮动作：在当前行后添加新字段
                addButton.setOnAction(event -> {
                    int rowIndex = getIndex();
                    String newField = "新字段" + (fieldsTable.getItems().size() + 1);
                    
                    // 在指定位置插入新行
                    fieldsTable.getItems().add(rowIndex + 1, newField);
                    
                    // 更新列表数据表格
                    updateListDataTableFields(listName);
                    
                    if (updatePreviewCallback != null) {
                        updatePreviewCallback.run();
                    }
                });
                
                // 删除按钮动作
                deleteButton.setOnAction(event -> {
                    int rowIndex = getIndex();
                    String fieldToRemove = fieldsTable.getItems().get(rowIndex);
                    
                    // 检查是否至少保留一个字段
                    if (fieldsTable.getItems().size() > 1) {
                        // 移除字段
                        fieldsTable.getItems().remove(rowIndex);
                        
                        // 更新列表数据表格
                        removeFieldFromListData(listName, fieldToRemove);
                        
                        if (updatePreviewCallback != null) {
                            updatePreviewCallback.run();
                        }
                    } else {
                        UIHelper.showWarning("警告", "至少需要保留一个字段");
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });
        
        fieldsTable.getColumns().addAll(fieldNameColumn, placeholderColumn, actionColumn);
        
        // 添加默认字段
        fieldsTable.setItems(FXCollections.observableArrayList("id", "name", "description"));
        
        listContainer.getChildren().add(fieldsTable);
        listFieldItemsContainer.getChildren().add(listContainer);
        
        // 创建列表数据表格
        createListDataTable(listName);
        
        // 更新预览
        if (updatePreviewCallback != null) {
            updatePreviewCallback.run();
        }
    }
    
    /**
     * 从列表数据中移除字段
     * 
     * @param listName 列表名称
     * @param fieldName 字段名称
     */
    private void removeFieldFromListData(String listName, String fieldName) {
        // 从数据中移除该字段
        if (listFieldDataMap.containsKey(listName)) {
            for (Map<String, String> item : listFieldDataMap.get(listName)) {
                item.remove(fieldName);
            }
        }
        
        // 更新数据表格
        updateListDataTableFields(listName);
    }
    
    /**
     * 更新列表字段名称
     * 
     * @param listName 列表名称
     * @param oldName 旧字段名
     * @param newName 新字段名
     */
    private void updateListFieldName(String listName, String oldName, String newName) {
        // 更新数据中的字段名
        if (listFieldDataMap.containsKey(listName)) {
            for (Map<String, String> item : listFieldDataMap.get(listName)) {
                if (item.containsKey(oldName)) {
                    String value = item.remove(oldName);
                    item.put(newName, value);
                }
            }
        }
        
        // 更新数据表格
        updateListDataTableFields(listName);
    }
    
    /**
     * 更新列表数据表格字段
     * 
     * @param listName 列表名称
     */
    private void updateListDataTableFields(String listName) {
        // 查找数据表格容器
        for (Node node : listDataItemsContainer.getChildren()) {
            if (node instanceof VBox && node.getId() != null && node.getId().equals(listName + "_data")) {
                VBox tableContainer = (VBox) node;
                
                // 查找字段表格
                for (Node listNode : listFieldItemsContainer.getChildren()) {
                    if (listNode instanceof VBox && listNode.getId() != null && listNode.getId().equals(listName)) {
                        VBox listContainer = (VBox) listNode;
                        List<String> fields = new ArrayList<>();
                        
                        // 获取所有字段
                        for (Node child : listContainer.getChildren()) {
                            if (child instanceof TableView) {
                                @SuppressWarnings("unchecked")
                                TableView<String> fieldsTable = (TableView<String>) child;
                                fields.addAll(fieldsTable.getItems());
                                break;
                            }
                        }
                        
                        // 重建数据表格
                        tableContainer.getChildren().clear();
                        
                        // 重新添加标题
                        Label titleLabel = new Label(listName);
                        titleLabel.getStyleClass().add("list-field-title");
                        
                        // 为标题添加悬浮提示
                        Tooltip titleTooltip = new Tooltip("列表名: " + listName + "\n占位符示例: {{#" + listName + "}} {{" + listName + ".字段名}} {{/" + listName + "}}");
                        Tooltip.install(titleLabel, titleTooltip);
                        
                        tableContainer.getChildren().add(titleLabel);
                        
                        // 创建数据表格
                        TableView<Map<String, String>> dataTable = new TableView<>();
                        dataTable.setEditable(true);
                        dataTable.getStyleClass().add("list-data-table"); // 添加样式类以便CSS选择器能识别
                        
                        // 显示表头
                        dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                        
                        // 为每个字段创建一个列
                        for (String field : fields) {
                            TableColumn<Map<String, String>, String> column = new TableColumn<>(field);
                            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(field)));
                            
                            // 使用支持失焦保存的TextFieldTableCell
                            column.setCellFactory(col -> new javafx.scene.control.TableCell<Map<String, String>, String>() {
                                private final TextField textField = new TextField();
                                
                                {
                                    // 失焦时保存
                                    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                                        if (!newVal) {
                                            commitEdit(textField.getText());
                                        }
                                    });
                                    
                                    // 回车键保存
                                    textField.setOnAction(e -> commitEdit(textField.getText()));
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
                                    Map<String, String> rowData = getTableView().getItems().get(getIndex());
                                    rowData.put(column.getText(), newValue);
                                    
                                    if (updatePreviewCallback != null) {
                                        updatePreviewCallback.run();
                                    }
                                }
                            });
                            
                            column.setPrefWidth(100);
                            dataTable.getColumns().add(column);
                        }
                        
                        // 添加操作列
                        TableColumn<Map<String, String>, Void> actionColumn = new TableColumn<>("操作");
                        actionColumn.setPrefWidth(60);
                        
                        actionColumn.setCellFactory(param -> new javafx.scene.control.TableCell<Map<String, String>, Void>() {
                            private final Button addButton = new Button("+");
                            private final Button deleteButton = new Button("×");
                            private final HBox actionBox = new HBox(5, addButton, deleteButton);
                            
                            {
                                addButton.getStyleClass().add("operation-add-button");
                                deleteButton.getStyleClass().add("operation-delete-button");
                                
                                // 添加按钮动作
                                addButton.setOnAction(event -> {
                                    int rowIndex = getIndex();
                                    
                                    // 创建新行
                                    Map<String, String> newRowData = new HashMap<>();
                                    
                                    // 初始化所有字段为空
                                    for (String fieldName : fields) {
                                        newRowData.put(fieldName, "");
                                    }
                                    
                                    // 在当前行之后插入新行
                                    listFieldDataMap.get(listName).add(rowIndex + 1, newRowData);
                                    dataTable.setItems(FXCollections.observableArrayList(listFieldDataMap.get(listName)));
                                    
                                    if (updatePreviewCallback != null) {
                                        updatePreviewCallback.run();
                                    }
                                });
                                
                                // 删除按钮动作
                                deleteButton.setOnAction(event -> {
                                    int rowIndex = getIndex();
                                    
                                    // 检查是否是最后一行
                                    if (dataTable.getItems().size() > 1) {
                                        listFieldDataMap.get(listName).remove(rowIndex);
                                        dataTable.setItems(FXCollections.observableArrayList(listFieldDataMap.get(listName)));
                                        
                                        if (updatePreviewCallback != null) {
                                            updatePreviewCallback.run();
                                        }
                                    } else {
                                        UIHelper.showWarning("警告", "至少需要保留一条数据");
                                    }
                                });
                            }
                            
                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(actionBox);
                                }
                            }
                        });
                        dataTable.getColumns().add(actionColumn);
                        
                        // 设置表格样式和高度
                        dataTable.setPrefHeight(120);
                        dataTable.setFixedCellSize(24);
                        
                        // 设置表格数据
                        dataTable.setItems(FXCollections.observableArrayList(listFieldDataMap.get(listName)));
                        tableContainer.getChildren().add(dataTable);
                        
                        break;
                    }
                }
                break;
            }
        }
    }
    
    /**
     * 创建列表数据表格
     * 
     * @param listName 列表名称
     */
    private void createListDataTable(String listName) {
        // 检查数据容器中是否已有该列表的表格
        for (Node node : listDataItemsContainer.getChildren()) {
            if (node instanceof VBox && node.getId() != null && node.getId().equals(listName + "_data")) {
                return; // 已存在，不重复创建
            }
        }
        
        // 创建新的表格容器
        VBox tableContainer = new VBox();
        tableContainer.setId(listName + "_data");
        tableContainer.setSpacing(3); // 减少间距
        tableContainer.setPadding(new Insets(3)); // 减少内边距
        
        // 创建标题
        Label titleLabel = new Label(listName);
        titleLabel.getStyleClass().add("list-field-title"); // 确保和字段定义部分使用相同的样式
        
        // 为标题添加悬浮提示
        Tooltip titleTooltip = new Tooltip("列表名: " + listName + "\n占位符示例: {{#" + listName + "}} {{" + listName + ".字段名}} {{/" + listName + "}}");
        Tooltip.install(titleLabel, titleTooltip);
        
        tableContainer.getChildren().add(titleLabel);
        
        // 获取列表字段
        List<String> listFields = new ArrayList<>();
        for (Node node : listFieldItemsContainer.getChildren()) {
            if (node instanceof VBox && node.getId() != null && node.getId().equals(listName)) {
                for (Node child : ((VBox) node).getChildren()) {
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
        
        // 如果没有找到字段，添加一些默认字段
        if (listFields.isEmpty()) {
            listFields.add("id");
            listFields.add("name");
            listFields.add("description");
        }
        
        // 创建数据表格
        TableView<Map<String, String>> dataTable = new TableView<>();
        dataTable.setEditable(true);
        dataTable.getStyleClass().add("list-data-table"); // 添加样式类以便CSS选择器能识别
        
        // 显示表头
        dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // 为每个字段创建一个列
        for (String field : listFields) {
            TableColumn<Map<String, String>, String> column = new TableColumn<>(field);
            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(field)));
            
            // 使用支持失焦保存的TextFieldTableCell
            column.setCellFactory(col -> new javafx.scene.control.TableCell<Map<String, String>, String>() {
                private final TextField textField = new TextField();
                
                {
                    // 失焦时保存
                    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal) {
                            commitEdit(textField.getText());
                        }
                    });
                    
                    // 回车键保存
                    textField.setOnAction(e -> commitEdit(textField.getText()));
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
                    Map<String, String> rowData = getTableView().getItems().get(getIndex());
                    rowData.put(column.getText(), newValue);
                    
                    if (updatePreviewCallback != null) {
                        updatePreviewCallback.run();
                    }
                }
            });
            
            column.setPrefWidth(100);
            dataTable.getColumns().add(column);
        }
        
        // 添加操作列
        TableColumn<Map<String, String>, Void> actionColumn = new TableColumn<>("操作");
        actionColumn.setPrefWidth(60);
        
        actionColumn.setCellFactory(param -> new javafx.scene.control.TableCell<Map<String, String>, Void>() {
            private final Button addButton = new Button("+");
            private final Button deleteButton = new Button("×");
            private final HBox actionBox = new HBox(5, addButton, deleteButton);
            
            {
                addButton.getStyleClass().add("operation-add-button");
                deleteButton.getStyleClass().add("operation-delete-button");
                
                // 添加按钮动作
                addButton.setOnAction(event -> {
                    int rowIndex = getIndex();
                    
                    // 创建新行
                    Map<String, String> newRowData = new HashMap<>();
                    
                    // 初始化所有字段为空
                    for (String fieldName : listFields) {
                        newRowData.put(fieldName, "");
                    }
                    
                    // 在当前行之后插入新行
                    listFieldDataMap.get(listName).add(rowIndex + 1, newRowData);
                    dataTable.setItems(FXCollections.observableArrayList(listFieldDataMap.get(listName)));
                    
                    if (updatePreviewCallback != null) {
                        updatePreviewCallback.run();
                    }
                });
                
                // 删除按钮动作
                deleteButton.setOnAction(event -> {
                    int rowIndex = getIndex();
                    
                    // 检查是否是最后一行
                    if (dataTable.getItems().size() > 1) {
                        listFieldDataMap.get(listName).remove(rowIndex);
                        dataTable.setItems(FXCollections.observableArrayList(listFieldDataMap.get(listName)));
                        
                        if (updatePreviewCallback != null) {
                            updatePreviewCallback.run();
                        }
                    } else {
                        UIHelper.showWarning("警告", "至少需要保留一条数据");
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });
        dataTable.getColumns().add(actionColumn);
        
        // 设置表格样式和高度
        dataTable.setPrefHeight(120);
        dataTable.setFixedCellSize(24);
        
        // 如果数据列表为空，初始化一行空数据
        if (!listFieldDataMap.containsKey(listName) || listFieldDataMap.get(listName).isEmpty()) {
            List<Map<String, String>> emptyList = new ArrayList<>();
            Map<String, String> emptyRow = new HashMap<>();
            
            // 初始化所有字段为空
            for (String field : listFields) {
                emptyRow.put(field, "");
            }
            
            emptyList.add(emptyRow);
            listFieldDataMap.put(listName, emptyList);
        } else {
            // 确保每个字段都在数据中存在
            for (Map<String, String> item : listFieldDataMap.get(listName)) {
                for (String field : listFields) {
                    if (!item.containsKey(field)) {
                        item.put(field, "");
                    }
                }
            }
        }
        
        // 设置表格数据
        dataTable.setItems(FXCollections.observableArrayList(listFieldDataMap.get(listName)));
        
        tableContainer.getChildren().add(dataTable);
        listDataItemsContainer.getChildren().add(tableContainer);
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

    /**
     * 添加列表字段，带字段列表
     * 
     * @param listName 列表名称
     * @param fields 字段列表
     */
    public void addListField(String listName, List<String> fields) {
        // 先添加列表
        addListField(listName);
        
        // 更新列表的字段
        for (Node node : listFieldItemsContainer.getChildren()) {
            if (node instanceof VBox && listName.equals(node.getId())) {
                VBox listContainer = (VBox) node;
                
                // 查找字段表格
                for (Node child : listContainer.getChildren()) {
                    if (child instanceof TableView) {
                        @SuppressWarnings("unchecked")
                        TableView<String> fieldsTable = (TableView<String>) child;
                        
                        // 清空默认字段并添加新字段
                        fieldsTable.getItems().clear();
                        fieldsTable.getItems().addAll(fields);
                        
                        // 更新数据表格
                        updateListDataTableFields(listName);
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
            listFieldDataMap.get(listName).clear();
            listFieldDataMap.get(listName).addAll(itemsList);
        } else {
            listFieldDataMap.put(listName, new ArrayList<>(itemsList));
        }
        
        // 更新数据表格
        updateListDataTableFields(listName);
    }
} 