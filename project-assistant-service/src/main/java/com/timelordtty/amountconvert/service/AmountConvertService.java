package com.timelordtty.amountconvert.service;

import com.timelordtty.amountconvert.model.AmountConvertResult;

/**
 * 金额转换服务接口
 */
public interface AmountConvertService {
    
    /**
     * 将数字金额转换为中文大写金额
     * 
     * @param numericAmount 数字金额
     * @return 转换结果
     */
    AmountConvertResult convertToChinese(String numericAmount);
    
    /**
     * 将中文大写金额转换为数字金额
     * 
     * @param chineseAmount 中文大写金额
     * @return 转换结果
     */
    AmountConvertResult convertToNumeric(String chineseAmount);
} 