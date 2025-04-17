package com.timelordtty.amountconvert.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 金额转换结果模型类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmountConvertResult {
    
    private String numericAmount;  // 数字金额
    private String chineseAmount;  // 中文大写金额
    private boolean success;       // 转换是否成功
    private String message;        // 提示信息
    
    /**
     * 创建成功结果
     * 
     * @param numericAmount 数字金额
     * @param chineseAmount 中文大写金额
     * @return 转换结果
     */
    public static AmountConvertResult success(String numericAmount, String chineseAmount) {
        AmountConvertResult result = new AmountConvertResult();
        result.setNumericAmount(numericAmount);
        result.setChineseAmount(chineseAmount);
        result.setSuccess(true);
        result.setMessage("转换成功");
        return result;
    }
    
    /**
     * 创建失败结果
     * 
     * @param message 错误信息
     * @return 转换结果
     */
    public static AmountConvertResult error(String message) {
        AmountConvertResult result = new AmountConvertResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
} 