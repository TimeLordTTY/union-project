package com.timelordtty.amountconvert.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 金额转换工具类
 */
public class AmountConvertUtil {

    // 大写数字
    private static final String[] CN_UPPER_NUMBER = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    
    // 整数部分的单位
    private static final String[] CN_UPPER_MONETARY_UNIT = {"元", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "兆"};
    
    // 小数部分的单位
    private static final String[] CN_UPPER_DECIMAL_UNIT = {"角", "分", "厘", "毫"};
    
    // 金额正则表达式
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^(0|[1-9]\\d{0,11})(\\.(\\d{1,4}))?$");
    
    // 中文金额正则表达式
    private static final Pattern CN_AMOUNT_PATTERN = Pattern.compile("^[零壹贰叁肆伍陆柒捌玖元拾佰仟万亿兆角分厘毫整正负]+$");

    /**
     * 将数字金额转换为中文大写金额
     * 
     * @param amount 数字金额（最大支持13位整数）
     * @return 中文大写金额
     */
    public static String convertToChineseAmount(String amount) {
        if (amount == null || amount.isEmpty()) {
            return "零元整";
        }
        
        // 金额超出范围
        Matcher matcher = AMOUNT_PATTERN.matcher(amount);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("金额格式不正确，请输入正确的金额");
        }
        
        // 处理负数
        boolean negative = false;
        if (amount.startsWith("-")) {
            negative = true;
            amount = amount.substring(1);
        }
        
        // 解析金额
        BigDecimal numberOfMoney = new BigDecimal(amount);
        if (numberOfMoney.compareTo(BigDecimal.ZERO) == 0) {
            return "零元整";
        }
        
        // 格式化金额
        DecimalFormat format = new DecimalFormat("0.####");
        String formattedAmount = format.format(numberOfMoney);
        
        // 分离整数和小数部分
        String[] parts = formattedAmount.split("\\.");
        String integerPart = parts[0];
        String decimalPart = parts.length > 1 ? parts[1] : "";
        
        // 转换整数部分
        StringBuilder chineseAmount = new StringBuilder();
        if (negative) {
            chineseAmount.append("负");
        }
        
        // 处理整数部分
        if (Integer.parseInt(integerPart) > 0) {
            chineseAmount.append(convertIntegerPart(integerPart));
        }
        
        // 处理小数部分
        if (decimalPart.length() > 0) {
            chineseAmount.append(convertDecimalPart(decimalPart));
        } else {
            chineseAmount.append("整");
        }
        
        return chineseAmount.toString();
    }
    
    /**
     * 将中文大写金额转换为数字金额
     * 
     * @param chineseAmount 中文大写金额
     * @return 数字金额
     */
    public static String convertToNumericAmount(String chineseAmount) {
        if (chineseAmount == null || chineseAmount.isEmpty()) {
            return "0";
        }
        
        // 校验中文金额格式
        if (!CN_AMOUNT_PATTERN.matcher(chineseAmount).matches()) {
            throw new IllegalArgumentException("中文金额格式不正确");
        }
        
        // 特殊情况处理
        if ("零元整".equals(chineseAmount)) {
            return "0";
        }
        
        // 处理负数
        boolean negative = false;
        if (chineseAmount.startsWith("负")) {
            negative = true;
            chineseAmount = chineseAmount.substring(1);
        }
        
        // 拆分整数和小数部分
        String integerPart = "0";
        String decimalPart = "";
        
        int yuanIndex = chineseAmount.indexOf("元");
        if (yuanIndex > 0) {
            integerPart = parseIntegerPart(chineseAmount.substring(0, yuanIndex));
            if (yuanIndex + 1 < chineseAmount.length() && !"整".equals(chineseAmount.substring(yuanIndex + 1))) {
                decimalPart = parseDecimalPart(chineseAmount.substring(yuanIndex + 1));
            }
        } else {
            // 没有"元"，可能只有小数部分
            decimalPart = parseDecimalPart(chineseAmount);
        }
        
        // 组合结果
        String result = decimalPart.isEmpty() ? integerPart : integerPart + "." + decimalPart;
        return negative ? "-" + result : result;
    }
    
    /**
     * 转换整数部分
     */
    private static String convertIntegerPart(String integerPart) {
        StringBuilder result = new StringBuilder();
        int length = integerPart.length();
        
        // 不足13位前面补0
        String temp = String.format("%13s", integerPart).replace(' ', '0');
        
        // 从高位到低位转换
        for (int i = 0; i < temp.length(); i++) {
            int digit = temp.charAt(i) - '0';
            
            // 跳过前导零
            if (i < temp.length() - length && digit == 0) {
                continue;
            }
            
            if (digit != 0) {
                result.append(CN_UPPER_NUMBER[digit]).append(CN_UPPER_MONETARY_UNIT[temp.length() - 1 - i]);
            } else {
                // 处理连续多个零
                if (result.length() > 0 && result.charAt(result.length() - 1) != '零') {
                    result.append(CN_UPPER_NUMBER[0]);
                }
                
                // 对于万、亿等单位，即使值为0也要保留单位
                if ((temp.length() - 1 - i) == 4 || (temp.length() - 1 - i) == 8) {
                    result.append(CN_UPPER_MONETARY_UNIT[temp.length() - 1 - i]);
                }
            }
        }
        
        // 处理元
        if (result.charAt(result.length() - 1) != '元') {
            result.append("元");
        }
        
        return result.toString().replace("零零", "零");
    }
    
    /**
     * 转换小数部分
     */
    private static String convertDecimalPart(String decimalPart) {
        StringBuilder result = new StringBuilder();
        int length = Math.min(decimalPart.length(), 4); // 最多处理到毫
        
        for (int i = 0; i < length; i++) {
            int digit = decimalPart.charAt(i) - '0';
            if (digit != 0) {
                result.append(CN_UPPER_NUMBER[digit]).append(CN_UPPER_DECIMAL_UNIT[i]);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 解析中文整数部分
     */
    private static String parseIntegerPart(String chineseInteger) {
        // 处理中文整数部分的逻辑
        BigDecimal result = BigDecimal.ZERO;
        
        // 处理兆单位
        int zhaoIndex = chineseInteger.indexOf("兆");
        if (zhaoIndex > 0) {
            String zhaoValue = chineseInteger.substring(0, zhaoIndex);
            result = result.add(parseLowerUnit(zhaoValue).multiply(new BigDecimal("1000000000000")));
            chineseInteger = chineseInteger.substring(zhaoIndex + 1);
        }
        
        // 处理亿单位
        int yiIndex = chineseInteger.indexOf("亿");
        if (yiIndex > 0) {
            String yiValue = chineseInteger.substring(0, yiIndex);
            result = result.add(parseLowerUnit(yiValue).multiply(new BigDecimal("100000000")));
            chineseInteger = chineseInteger.substring(yiIndex + 1);
        }
        
        // 处理万单位
        int wanIndex = chineseInteger.indexOf("万");
        if (wanIndex > 0) {
            String wanValue = chineseInteger.substring(0, wanIndex);
            result = result.add(parseLowerUnit(wanValue).multiply(new BigDecimal("10000")));
            chineseInteger = chineseInteger.substring(wanIndex + 1);
        }
        
        // 处理剩余部分
        if (!chineseInteger.isEmpty()) {
            result = result.add(parseLowerUnit(chineseInteger));
        }
        
        return result.toString();
    }
    
    /**
     * 解析中文小数部分
     */
    private static String parseDecimalPart(String chineseDecimal) {
        StringBuilder result = new StringBuilder();
        
        // 处理角
        int jiaoIndex = chineseDecimal.indexOf("角");
        if (jiaoIndex > 0) {
            result.append(getNumFromChinese(chineseDecimal.substring(jiaoIndex - 1, jiaoIndex)));
            chineseDecimal = chineseDecimal.substring(jiaoIndex + 1);
        } else {
            result.append("0");
        }
        
        // 处理分
        int fenIndex = chineseDecimal.indexOf("分");
        if (fenIndex > 0) {
            result.append(getNumFromChinese(chineseDecimal.substring(fenIndex - 1, fenIndex)));
            chineseDecimal = chineseDecimal.substring(fenIndex + 1);
        } else {
            result.append("0");
        }
        
        // 处理厘
        int liIndex = chineseDecimal.indexOf("厘");
        if (liIndex > 0) {
            result.append(getNumFromChinese(chineseDecimal.substring(liIndex - 1, liIndex)));
            chineseDecimal = chineseDecimal.substring(liIndex + 1);
        }
        
        // 处理毫
        int haoIndex = chineseDecimal.indexOf("毫");
        if (haoIndex > 0) {
            result.append(getNumFromChinese(chineseDecimal.substring(haoIndex - 1, haoIndex)));
        }
        
        // 去除尾部的0
        String decimalStr = result.toString();
        while (decimalStr.endsWith("0") && decimalStr.length() > 1) {
            decimalStr = decimalStr.substring(0, decimalStr.length() - 1);
        }
        
        return decimalStr;
    }
    
    /**
     * 解析较低单位的中文数字（千、百、十、个位）
     */
    private static BigDecimal parseLowerUnit(String chinese) {
        BigDecimal result = BigDecimal.ZERO;
        
        // 处理千位
        int qianIndex = chinese.indexOf("仟");
        if (qianIndex > 0) {
            result = result.add(new BigDecimal(getNumFromChinese(chinese.substring(qianIndex - 1, qianIndex)) + "000"));
            chinese = chinese.replace(chinese.substring(qianIndex - 1, qianIndex + 1), "");
        }
        
        // 处理百位
        int baiIndex = chinese.indexOf("佰");
        if (baiIndex > 0) {
            result = result.add(new BigDecimal(getNumFromChinese(chinese.substring(baiIndex - 1, baiIndex)) + "00"));
            chinese = chinese.replace(chinese.substring(baiIndex - 1, baiIndex + 1), "");
        }
        
        // 处理十位
        int shiIndex = chinese.indexOf("拾");
        if (shiIndex > 0) {
            result = result.add(new BigDecimal(getNumFromChinese(chinese.substring(shiIndex - 1, shiIndex)) + "0"));
            chinese = chinese.replace(chinese.substring(shiIndex - 1, shiIndex + 1), "");
        } else if (chinese.startsWith("拾")) {
            // 特殊处理"拾"开头的情况，如"拾元"表示10元
            result = result.add(new BigDecimal("10"));
            chinese = chinese.substring(1);
        }
        
        // 处理个位
        if (!chinese.isEmpty() && !"零".equals(chinese)) {
            for (int i = 0; i < chinese.length(); i++) {
                String ch = chinese.substring(i, i + 1);
                if (!"零".equals(ch)) {
                    result = result.add(new BigDecimal(getNumFromChinese(ch)));
                }
            }
        }
        
        return result;
    }
    
    /**
     * 获取中文数字对应的阿拉伯数字
     */
    private static String getNumFromChinese(String chinese) {
        for (int i = 0; i < CN_UPPER_NUMBER.length; i++) {
            if (CN_UPPER_NUMBER[i].equals(chinese)) {
                return String.valueOf(i);
            }
        }
        return "0";
    }
} 