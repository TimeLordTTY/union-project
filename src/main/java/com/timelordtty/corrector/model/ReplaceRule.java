package com.timelordtty.corrector.model;

import java.util.Objects;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 文本替换规则模型
 */
public class ReplaceRule {
    private final StringProperty originalText;
    private final StringProperty replacementText;

    /**
     * 创建一个替换规则
     * @param originalText 原始文本
     * @param replacementText 替换后文本
     */
    public ReplaceRule(String originalText, String replacementText) {
        this.originalText = new SimpleStringProperty(originalText);
        this.replacementText = new SimpleStringProperty(replacementText);
    }

    /**
     * 获取原始文本
     */
    public String getOriginalText() {
        return originalText.get();
    }

    /**
     * 设置原始文本
     */
    public void setOriginalText(String originalText) {
        this.originalText.set(originalText);
    }

    /**
     * 获取原始文本属性
     */
    public StringProperty originalTextProperty() {
        return originalText;
    }

    /**
     * 获取替换后文本
     */
    public String getReplacementText() {
        return replacementText.get();
    }

    /**
     * 设置替换后文本
     */
    public void setReplacementText(String replacementText) {
        this.replacementText.set(replacementText);
    }

    /**
     * 获取替换后文本属性
     */
    public StringProperty replacementTextProperty() {
        return replacementText;
    }
    
    /**
     * 比较两个规则是否相同
     * 只要原文本相同即认为是相同的规则
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplaceRule that = (ReplaceRule) o;
        return Objects.equals(getOriginalText(), that.getOriginalText());
    }
    
    /**
     * 生成哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(getOriginalText());
    }
} 