package com.timelordtty.model;

/**
 * 文本纠错模型类
 * 用于存储原文和纠正后的文本信息
 */
public class TextCorrection {
    
    private String original;    // 原始文本
    private String corrected;   // 纠正后的文本
    private String position;    // 位置信息
    
    public TextCorrection() {
    }
    
    public TextCorrection(String original, String corrected, String position) {
        this.original = original;
        this.corrected = corrected;
        this.position = position;
    }
    
    public String getOriginal() {
        return original;
    }
    
    public void setOriginal(String original) {
        this.original = original;
    }
    
    public String getCorrected() {
        return corrected;
    }
    
    public void setCorrected(String corrected) {
        this.corrected = corrected;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
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