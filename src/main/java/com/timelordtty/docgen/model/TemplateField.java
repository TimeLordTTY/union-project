package com.timelordtty.docgen.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

/**
 * 模板字段类
 */
public class TemplateField {
    
    private SimpleStringProperty name;        // 字段名称
    private SimpleStringProperty placeholder; // 占位符
    private SimpleBooleanProperty isList;     // 是否为列表字段
    private List<String> listItems = new ArrayList<>(); // 列表项

    /**
     * 构造函数
     * @param name 字段名称
     * @param isList 是否为列表字段
     */
    public TemplateField(String name, boolean isList) {
        this.name = new SimpleStringProperty(name);
        this.isList = new SimpleBooleanProperty(isList);
        
        // 生成占位符 - 根据字段类型使用不同格式
        if (isList) {
            // 列表字段使用 {{#name}} 格式
            this.placeholder = new SimpleStringProperty("{{#" + name + "}}");
        } else {
            // 对象字段使用 ${name} 格式
            this.placeholder = new SimpleStringProperty("${" + name + "}");
        }
    }

    /**
     * 获取字段名称
     * @return 字段名称
     */
    public String getName() {
        return name.get();
    }

    /**
     * 获取字段名称属性
     * @return 字段名称属性
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * 设置字段名称
     * @param name 字段名称
     */
    public void setName(String name) {
        this.name.set(name);
        
        // 更新占位符
        updatePlaceholder();
    }

    /**
     * 获取占位符
     * @return 占位符
     */
    public String getPlaceholder() {
        return placeholder.get();
    }

    /**
     * 获取占位符属性
     * @return 占位符属性
     */
    public StringProperty placeholderProperty() {
        return placeholder;
    }

    /**
     * 设置占位符
     * @param placeholder 占位符
     */
    public void setPlaceholder(String placeholder) {
        this.placeholder.set(placeholder);
    }

    /**
     * 是否为列表字段
     * @return 是否为列表字段
     */
    public boolean isList() {
        return isList.get();
    }

    /**
     * 获取是否为列表字段属性
     * @return 是否为列表字段属性
     */
    public SimpleBooleanProperty isListProperty() {
        return isList;
    }

    /**
     * 设置是否为列表字段
     * @param isList 是否为列表字段
     */
    public void setIsList(boolean isList) {
        this.isList.set(isList);
        // 更新占位符格式
        updatePlaceholder();
    }
    
    /**
     * 更新占位符
     */
    private void updatePlaceholder() {
        if (isList.get()) {
            // 列表字段使用 {{#name}} 格式
            this.placeholder.set("{{#" + getName() + "}}");
        } else {
            // 对象字段使用 ${name} 格式
            this.placeholder.set("${" + getName() + "}");
        }
    }
    
    /**
     * 获取列表项
     * @return 列表项
     */
    public List<String> getListItems() {
        return listItems;
    }
    
    /**
     * 设置列表项
     * @param listItems 列表项
     */
    public void setListItems(List<String> listItems) {
        this.listItems = listItems;
    }
    
    /**
     * 添加列表项
     * @param itemName 列表项名称
     */
    public void addListItem(String itemName) {
        if (listItems == null) {
            listItems = FXCollections.observableArrayList();
        }
        
        // 避免重复添加
        if (!listItems.contains(itemName)) {
            listItems.add(itemName);
        }
    }
    
    /**
     * 移除列表项
     * @param itemName 列表项名称
     * @return 是否成功移除
     */
    public boolean removeListItem(String itemName) {
        if (listItems != null) {
            return listItems.remove(itemName);
        }
        return false;
    }
    
    /**
     * 获取指定列表项的占位符
     * @param itemName 列表项名称
     * @return 占位符
     */
    public String getListItemPlaceholder(String itemName) {
        return "{{" + itemName + "}}";
    }
    
    /**
     * 获取列表的结束占位符
     * @return 列表结束占位符
     */
    public String getListEndPlaceholder() {
        return "{{/" + getName() + "}}";
    }
    
    @Override
    public String toString() {
        return name.get();
    }
} 