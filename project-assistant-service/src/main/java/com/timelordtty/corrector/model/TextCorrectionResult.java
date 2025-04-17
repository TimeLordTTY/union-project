package com.timelordtty.corrector.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文本纠错结果模型类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextCorrectionResult {
    
    private String originalText;  // 原始文本
    private String correctedText; // 纠错后的文本
    private boolean hasCorrection; // 是否有纠错
    private List<CorrectionDetail> details; // 纠错详情
    private boolean success;      // 操作是否成功
    private String message;       // 提示信息
    
    /**
     * 创建成功结果（有纠错）
     */
    public static TextCorrectionResult success(String originalText, String correctedText, List<CorrectionDetail> details) {
        TextCorrectionResult result = new TextCorrectionResult();
        result.setOriginalText(originalText);
        result.setCorrectedText(correctedText);
        result.setHasCorrection(!originalText.equals(correctedText));
        result.setDetails(details);
        result.setSuccess(true);
        result.setMessage("文本纠错完成");
        return result;
    }
    
    /**
     * 创建成功结果（无纠错）
     */
    public static TextCorrectionResult noCorrection(String text) {
        TextCorrectionResult result = new TextCorrectionResult();
        result.setOriginalText(text);
        result.setCorrectedText(text);
        result.setHasCorrection(false);
        result.setSuccess(true);
        result.setMessage("文本无需纠错");
        return result;
    }
    
    /**
     * 创建失败结果
     */
    public static TextCorrectionResult error(String message) {
        TextCorrectionResult result = new TextCorrectionResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
} 