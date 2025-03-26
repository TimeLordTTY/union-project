package com.timelordtty.corrector.model;

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
} 