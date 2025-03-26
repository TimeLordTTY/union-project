package com.timelordtty.corrector.model;

/**
 * 文本纠正项模型类
 * 表示一个需要修正的文本片段
 * 
 * @author tianyu.tang
 */
public class TextCorrection {
    private String original;  // 原始文本
    private String corrected; // 纠正后的文本
    private String position;  // 位置信息，格式为"位置: startPos-endPos"
    private String errorType; // 错误类型，例如"拼写错误"、"语法错误"等
    
    /**
     * 构造函数
     * @param original 原始文本
     * @param corrected 纠正后的文本
     * @param position 位置信息
     */
    public TextCorrection(String original, String corrected, String position) {
        this.original = original;
        this.corrected = corrected;
        this.position = position;
    }
    
    /**
     * 获取原始文本
     * @return 原始文本
     */
    public String getOriginal() {
        return original;
    }
    
    /**
     * 设置原始文本
     * @param original 原始文本
     */
    public void setOriginal(String original) {
        this.original = original;
    }
    
    /**
     * 获取纠正后的文本
     * @return 纠正后的文本
     */
    public String getCorrected() {
        return corrected;
    }
    
    /**
     * 设置纠正后的文本
     * @param corrected 纠正后的文本
     */
    public void setCorrected(String corrected) {
        this.corrected = corrected;
    }
    
    /**
     * 获取位置信息
     * @return 位置信息，格式为"位置: startPos-endPos"
     */
    public String getPosition() {
        return position;
    }
    
    /**
     * 设置位置信息
     * @param position 位置信息
     */
    public void setPosition(String position) {
        this.position = position;
    }
    
    /**
     * 设置错误类型
     * @param errorType 错误类型
     */
    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
    
    /**
     * 获取错误类型
     * @return 错误类型
     */
    public String getErrorType() {
        return errorType;
    }
    
    @Override
    public String toString() {
        return "TextCorrection{" +
                "original='" + original + '\'' +
                ", corrected='" + corrected + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
} 