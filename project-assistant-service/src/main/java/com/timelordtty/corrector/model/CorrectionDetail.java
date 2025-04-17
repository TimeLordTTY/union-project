package com.timelordtty.corrector.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 纠错详情模型类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrectionDetail {
    
    private String original; // 原文本
    private String corrected; // 纠正后的文本
    private int position; // 在原文中的位置
    private int length; // 原文的长度
    
} 