package com.timelordtty.projectassistant.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 金额转换工具类
 */
public class MoneyConvertUtil {
    
    // 中文数字
    private static final String[] CN_NUMS = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    
    // 中文单位
    private static final String[] CN_UNITS = {"分", "角", "元", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟"};
    
    // 中文数字映射表
    private static final Map<Character, Integer> CN_NUM_MAP = new HashMap<>();
    
    // 中文单位映射表
    private static final Map<Character, BigDecimal> CN_UNIT_MAP = new HashMap<>();
    
    static {
        // 初始化中文数字映射
        CN_NUM_MAP.put('零', 0);
        CN_NUM_MAP.put('壹', 1);
        CN_NUM_MAP.put('贰', 2);
        CN_NUM_MAP.put('叁', 3);
        CN_NUM_MAP.put('肆', 4);
        CN_NUM_MAP.put('伍', 5);
        CN_NUM_MAP.put('陆', 6);
        CN_NUM_MAP.put('柒', 7);
        CN_NUM_MAP.put('捌', 8);
        CN_NUM_MAP.put('玖', 9);
        CN_NUM_MAP.put('〇', 0);
        CN_NUM_MAP.put('一', 1);
        CN_NUM_MAP.put('二', 2);
        CN_NUM_MAP.put('三', 3);
        CN_NUM_MAP.put('四', 4);
        CN_NUM_MAP.put('五', 5);
        CN_NUM_MAP.put('六', 6);
        CN_NUM_MAP.put('七', 7);
        CN_NUM_MAP.put('八', 8);
        CN_NUM_MAP.put('九', 9);
        
        // 初始化中文单位映射
        CN_UNIT_MAP.put('分', new BigDecimal("0.01"));
        CN_UNIT_MAP.put('角', new BigDecimal("0.1"));
        CN_UNIT_MAP.put('元', new BigDecimal("1"));
        CN_UNIT_MAP.put('圆', new BigDecimal("1"));
        CN_UNIT_MAP.put('块', new BigDecimal("1"));
        CN_UNIT_MAP.put('拾', new BigDecimal("10"));
        CN_UNIT_MAP.put('十', new BigDecimal("10"));
        CN_UNIT_MAP.put('佰', new BigDecimal("100"));
        CN_UNIT_MAP.put('百', new BigDecimal("100"));
        CN_UNIT_MAP.put('仟', new BigDecimal("1000"));
        CN_UNIT_MAP.put('千', new BigDecimal("1000"));
        CN_UNIT_MAP.put('万', new BigDecimal("10000"));
        CN_UNIT_MAP.put('亿', new BigDecimal("100000000"));
    }
    
    /**
     * 将数字金额转换为中文大写金额
     * 
     * @param amount 数字金额
     * @return 中文大写金额
     */
    public static String toChineseAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额不能为空或负数");
        }
        
        if (amount.compareTo(new BigDecimal("1000000000000")) >= 0) {
            throw new IllegalArgumentException("金额不能超过万亿");
        }
        
        // 四舍五入到分
        amount = amount.setScale(2, BigDecimal.ROUND_HALF_UP);
        
        // 转为字符串处理
        String strAmount = amount.toString();
        String intPart = strAmount.contains(".") ? strAmount.split("\\.")[0] : strAmount;
        String decPart = strAmount.contains(".") ? strAmount.split("\\.")[1] : "00";
        
        if (decPart.length() == 1) {
            decPart = decPart + "0";
        }
        
        // 整数部分处理
        StringBuilder sb = new StringBuilder();
        boolean isZero = true; // 标记是否全为零
        
        // 处理整数部分
        for (int i = 0; i < intPart.length(); i++) {
            int digit = intPart.charAt(i) - '0';
            int unitIndex = intPart.length() - i + 1; // 单位索引
            
            if (digit == 0) {
                if (unitIndex == 3 || unitIndex == 7 || unitIndex == 11) {
                    // 遇到元、万、亿时，需要加单位
                    if (!isZero) {
                        sb.append(CN_UNITS[unitIndex]);
                        isZero = true;
                    }
                } else if (!isZero) {
                    // 非连续零才加"零"
                    sb.append(CN_NUMS[0]);
                    isZero = true;
                }
            } else {
                sb.append(CN_NUMS[digit]).append(CN_UNITS[unitIndex]);
                isZero = false;
            }
        }
        
        // 如果整数部分全为零，则加"零元"
        if (isZero) {
            sb.append("零元");
        }
        
        // 处理小数部分
        if ("00".equals(decPart)) {
            sb.append("整");
        } else {
            int jiao = decPart.charAt(0) - '0';
            int fen = decPart.charAt(1) - '0';
            
            if (jiao > 0) {
                sb.append(CN_NUMS[jiao]).append("角");
            } else if (fen > 0) {
                sb.append("零");
            }
            
            if (fen > 0) {
                sb.append(CN_NUMS[fen]).append("分");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 将数字金额转换为中文大写金额
     * 
     * @param amount 数字金额
     * @return 中文大写金额
     */
    public static String toChineseAmount(double amount) {
        return toChineseAmount(new BigDecimal(String.valueOf(amount)));
    }
    
    /**
     * 将中文金额转换为数字金额
     * 
     * @param chineseAmount 中文金额
     * @return 数字金额
     */
    public static BigDecimal toDigitalAmount(String chineseAmount) {
        if (chineseAmount == null || chineseAmount.isEmpty()) {
            throw new IllegalArgumentException("中文金额不能为空");
        }
        
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal section = BigDecimal.ZERO;
        BigDecimal temp = BigDecimal.ZERO;
        
        for (int i = 0; i < chineseAmount.length(); i++) {
            char c = chineseAmount.charAt(i);
            
            // 处理数字
            if (CN_NUM_MAP.containsKey(c)) {
                temp = new BigDecimal(CN_NUM_MAP.get(c));
            } 
            // 处理单位
            else if (CN_UNIT_MAP.containsKey(c)) {
                BigDecimal unit = CN_UNIT_MAP.get(c);
                
                // 万和亿需要累加
                if (unit.compareTo(new BigDecimal("10000")) >= 0) {
                    section = section.add(temp).multiply(unit);
                    result = result.add(section);
                    section = BigDecimal.ZERO;
                    temp = BigDecimal.ZERO;
                } 
                // 元以下的单位直接累加结果
                else if (unit.compareTo(BigDecimal.ONE) <= 0) {
                    temp = temp.multiply(unit);
                    result = result.add(temp);
                    temp = BigDecimal.ZERO;
                } 
                // 其他单位累加到临时结果
                else {
                    temp = temp.multiply(unit);
                    section = section.add(temp);
                    temp = BigDecimal.ZERO;
                }
            } 
            // 忽略"整"、"正"等词
            else if (c != '整' && c != '正' && c != '整' && c != '数' && c != '点') {
                // 其他字符忽略
            }
        }
        
        // 处理剩余数值
        result = result.add(section).add(temp);
        
        return result.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
} 