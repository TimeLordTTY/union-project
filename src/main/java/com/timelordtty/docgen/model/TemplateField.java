package com.timelordtty.docgen.model;

/**
 * 模板字段实体类
 */
public class TemplateField {
    private String name;
    private boolean isList;
    
    /**
     * 构造函数
     * @param name 字段名称
     * @param isList 是否为列表字段
     */
    public TemplateField(String name, boolean isList) {
        this.name = name;
        this.isList = isList;
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
    }
    
    /**
     * 判断是否为列表字段
     * @return 如果是列表字段则为true，否则为false
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
    
    @Override
    public String toString() {
        return (isList ? "▣ " : "◉ ") + name;
    }
} 