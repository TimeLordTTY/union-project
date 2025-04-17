package com.timelordtty.corrector.service.impl;

import com.timelordtty.corrector.model.CorrectionDetail;
import com.timelordtty.corrector.model.TextCorrectionResult;
import com.timelordtty.corrector.service.TextCorrectionService;
import com.timelordtty.corrector.util.BaiduTextCorrectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文本纠错服务实现类
 */
@Service
@Slf4j
public class TextCorrectionServiceImpl implements TextCorrectionService {
    
    private final BaiduTextCorrectionUtil baiduTextCorrectionUtil;
    
    @Autowired
    public TextCorrectionServiceImpl(BaiduTextCorrectionUtil baiduTextCorrectionUtil) {
        this.baiduTextCorrectionUtil = baiduTextCorrectionUtil;
    }
    
    @Override
    public TextCorrectionResult correctText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return TextCorrectionResult.error("文本不能为空");
        }
        
        try {
            log.info("开始纠错文本: {}", text);
            
            // 调用百度API进行文本纠错
            List<CorrectionDetail> correctionDetails = baiduTextCorrectionUtil.correctText(text);
            
            // 如果没有纠错结果，则返回原文
            if (correctionDetails.isEmpty()) {
                log.info("文本无需纠错");
                return TextCorrectionResult.noCorrection(text);
            }
            
            // 应用纠错结果
            String correctedText = baiduTextCorrectionUtil.applyCorrections(text, correctionDetails);
            
            log.info("文本纠错完成，共有{}处修改", correctionDetails.size());
            
            return TextCorrectionResult.success(text, correctedText, correctionDetails);
        } catch (Exception e) {
            log.error("文本纠错失败: {}", e.getMessage(), e);
            return TextCorrectionResult.error("文本纠错失败: " + e.getMessage());
        }
    }
} 