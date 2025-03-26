package com.timelordtty.replace.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 文本替换规则模型类
 */
public class ReplacementRule {
    
    private final StringProperty from = new SimpleStringProperty();
    private final StringProperty to = new SimpleStringProperty();
    
    public ReplacementRule() {
    }
    
    public ReplacementRule(String from, String to) {
        setFrom(from);
        setTo(to);
    }
    
    public String getFrom() {
        return from.get();
    }
    
    public StringProperty fromProperty() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from.set(from);
    }
    
    public String getTo() {
        return to.get();
    }
    
    public StringProperty toProperty() {
        return to;
    }
    
    public void setTo(String to) {
        this.to.set(to);
    }
    
    @Override
    public String toString() {
        return "ReplacementRule{" +
                "from='" + getFrom() + '\'' +
                ", to='" + getTo() + '\'' +
                '}';
    }
} 