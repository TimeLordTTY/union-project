package com.timelordtty.projectassistant.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timelordtty.projectassistant.common.ApiResponse;
import com.timelordtty.projectassistant.util.MoneyConvertUtil;

/**
 * 金额转换控制器
 */
@RestController
@RequestMapping("/money")
public class MoneyConvertController {
    
    /**
     * 数字金额转中文金额
     */
    @PostMapping("/toChineseAmount")
    public ApiResponse<Map<String, String>> toChineseAmount(@RequestBody Map<String, Object> params) {
        try {
            String amount = params.get("amount").toString();
            BigDecimal value = new BigDecimal(amount);
            
            String chineseAmount = MoneyConvertUtil.toChineseAmount(value);
            
            Map<String, String> result = new HashMap<>();
            result.put("amount", amount);
            result.put("chineseAmount", chineseAmount);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 中文金额转数字金额
     */
    @PostMapping("/toDigitalAmount")
    public ApiResponse<Map<String, String>> toDigitalAmount(@RequestBody Map<String, Object> params) {
        try {
            String chineseAmount = params.get("chineseAmount").toString();
            
            BigDecimal amount = MoneyConvertUtil.toDigitalAmount(chineseAmount);
            
            Map<String, String> result = new HashMap<>();
            result.put("chineseAmount", chineseAmount);
            result.put("amount", amount.toString());
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("转换失败: " + e.getMessage());
        }
    }
} 