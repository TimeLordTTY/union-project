package com.timelordtty.docgen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板字段类
 */
public class TemplateField {
    
    private String name;        // 字段名称
    private String placeholder; // 占位符
    private boolean isList;     // 是否为列表字段
    private List<String> listItems = new ArrayList<>(); // 列表项

    /**
     * 构造函数
     * @param name 字段名称
     * @param isList 是否为列表字段
     */
    public TemplateField(String name, boolean isList) {
        this.name = name;
        this.isList = isList;
        
        // 生成占位符
        this.placeholder = "${" + name + "}";
    }

    /**
     * 获取字段名称
     * @return 字段名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置字段名称
     * @param name 字段名称
     */
    public void setName(String name) {
        this.name = name;
        
        // 更新占位符
        this.placeholder = "${" + name + "}";
    }

    /**
     * 获取占位符
     * @return 占位符
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * 设置占位符
     * @param placeholder 占位符
     */
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    /**
     * 是否为列表字段
     * @return 是否为列表字段
     */
    public boolean isList() {
        return isList;
    }

    /**
     * 设置是否为列表字段
     * @param isList 是否为列表字段
     */
    public void setList(boolean isList) {
        this.isList = isList;
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
        if (!listItems.contains(itemName)) {
            listItems.add(itemName);
        }
    }
    
    /**
     * 移除列表项
     * @param itemName 列表项名称
     */
    public void removeListItem(String itemName) {
        listItems.remove(itemName);
    }
    
    /**
     * 获取指定列表项的占位符
     * @param itemName 列表项名称
     * @return 占位符
     */
    public String getListItemPlaceholder(String itemName) {
        return "${" + name + "." + itemName + "}";
    }
    
    @Override
    public String toString() {
        return name + (isList ? " (列表)" : "");
    }
} 