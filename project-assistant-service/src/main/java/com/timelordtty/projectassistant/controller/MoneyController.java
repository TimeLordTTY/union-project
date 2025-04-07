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
 * 提供数字金额与中文金额互相转换的API
 */
@RestController
@RequestMapping("/api/money")
public class MoneyController {

    /**
     * 将数字金额转换为中文金额
     * @param requestBody 包含amount字段的请求体
     * @return 中文金额
     */
    @PostMapping("/toChineseAmount")
    public ApiResponse<Map<String, String>> toChineseAmount(@RequestBody Map<String, Object> requestBody) {
        try {
            // 获取请求中的金额参数
            String amountStr = String.valueOf(requestBody.get("amount"));
            
            // 检查特殊金额，增加彩蛋
            String specialResult = checkSpecialAmount(amountStr);
            if (specialResult != null) {
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("result", specialResult);
                return ApiResponse.success(resultMap);
            }
            
            // 转换为BigDecimal以保证精度
            BigDecimal amount = new BigDecimal(amountStr);
            
            // 调用工具类进行转换
            String chineseAmount = MoneyConvertUtil.toChineseAmount(amount);
            
            // 构建响应
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("result", chineseAmount);
            
            return ApiResponse.success(resultMap);
        } catch (NumberFormatException e) {
            return ApiResponse.error("金额格式不正确");
        } catch (Exception e) {
            return ApiResponse.error("转换失败: " + e.getMessage());
        }
    }

    /**
     * 将中文金额转换为数字金额
     * @param requestBody 包含chineseAmount字段的请求体
     * @return 数字金额
     */
    @PostMapping("/toDigitalAmount")
    public ApiResponse<Map<String, String>> toDigitalAmount(@RequestBody Map<String, Object> requestBody) {
        try {
            // 获取请求中的中文金额参数
            String chineseAmount = String.valueOf(requestBody.get("chineseAmount"));
            
            // 调用工具类进行转换
            BigDecimal amount = MoneyConvertUtil.toDigitalAmount(chineseAmount);
            
            // 格式化金额，保留两位小数
            String formattedAmount = amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            
            // 构建响应
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("result", formattedAmount);
            
            return ApiResponse.success(resultMap);
        } catch (Exception e) {
            return ApiResponse.error("转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查特殊金额，用于实现彩蛋功能
     * @param amountStr 金额字符串
     * @return 特殊响应，如果不是特殊金额则返回null
     */
    private String checkSpecialAmount(String amountStr) {
        // 移除非数字字符
        String cleanAmount = amountStr.replaceAll("[^\\d]", "");
        
        // 检查特殊数字
        switch (cleanAmount) {
            case "520":
                return "伍佰贰拾元整 ❤️ 我爱你";
            case "1314":
                return "壹仟叁佰壹拾肆元整 ❤️ 一生一世";
            case "5201314":
                return "伍佰贰拾万壹仟叁佰壹拾肆元整 ❤️ 我爱你一生一世";
            case "9999":
                return "玖仟玖佰玖拾玖元整 ❤️ 天长地久";
            default:
                return null;
        }
    }
} 