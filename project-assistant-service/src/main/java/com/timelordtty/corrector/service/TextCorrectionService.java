package com.timelordtty.corrector.service;

import com.timelordtty.corrector.model.TextCorrectionResult;

/**
 * 文本纠错服务接口
 */
public interface TextCorrectionService {
    
    /**
     * 纠正文本中的错误
     * 
     * @param text 需要纠正的文本
     * @return 纠错结果
     */
    TextCorrectionResult correctText(String text);
    
} 