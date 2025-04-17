package com.timelordtty.amountconvert.service.impl;

import com.timelordtty.amountconvert.model.AmountConvertResult;
import com.timelordtty.amountconvert.service.AmountConvertService;
import com.timelordtty.amountconvert.util.AmountConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 金额转换服务实现类
 */
@Service
@Slf4j
public class AmountConvertServiceImpl implements AmountConvertService {

    @Override
    public AmountConvertResult convertToChinese(String numericAmount) {
        try {
            log.info("将数字金额[{}]转换为中文大写金额", numericAmount);
            String chineseAmount = AmountConvertUtil.convertToChineseAmount(numericAmount);
            log.info("转换结果: {}", chineseAmount);
            return AmountConvertResult.success(numericAmount, chineseAmount);
        } catch (Exception e) {
            log.error("数字金额转中文金额失败: {}", e.getMessage(), e);
            return AmountConvertResult.error(e.getMessage());
        }
    }

    @Override
    public AmountConvertResult convertToNumeric(String chineseAmount) {
        try {
            log.info("将中文大写金额[{}]转换为数字金额", chineseAmount);
            String numericAmount = AmountConvertUtil.convertToNumericAmount(chineseAmount);
            log.info("转换结果: {}", numericAmount);
            return AmountConvertResult.success(numericAmount, chineseAmount);
        } catch (Exception e) {
            log.error("中文金额转数字金额失败: {}", e.getMessage(), e);
            return AmountConvertResult.error(e.getMessage());
        }
    }
} 