package com.timelordtty.utils;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 金额转换工具类，用于在数字金额和中文大写金额之间进行转换
 * 
 * @author tianyu.tang
 * @version 1.0
 */
public class AmountConverter {
    
    // 大写中文数字
    private static final String[] CN_UPPER_NUMBER = {"零", "壹", "贰", "叁", "肆",
            "伍", "陆", "柒", "捌", "玖"};
    
    // 整数单位
    private static final String[] CN_INTEGER_UNIT = {"", "拾", "佰", "仟", "万",
            "拾", "佰", "仟", "亿", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿"};
    
    // 小数单位
    private static final String[] CN_DECIMAL_UNIT = {"角", "分", "厘", "毫"};
    
    // 金额单位
    private static final String CN_INTEGER = "整";
    private static final String CN_FULL = "元";
    private static final String CN_NEGATIVE = "负";
    private static final String CN_ZERO_FULL = "零元整";
    
    /**
     * 将数字金额转换为中文大写金额
     *
     * @param money 金额数值
     * @return 中文大写金额
     */
    public static String convertToChineseAmount(BigDecimal money) {
        LogUtils.info("开始将数字转换为中文大写金额: " + money.toPlainString());
        
        StringBuilder sb = new StringBuilder();
        
        // 零元整的情况
        if (money.compareTo(BigDecimal.ZERO) == 0) {
            LogUtils.info("数字为0，返回\"零元整\"");
            return CN_ZERO_FULL;
        }
        
        // 负数处理
        if (money.compareTo(BigDecimal.ZERO) < 0) {
            LogUtils.info("处理负数: " + money.toPlainString());
            sb.append(CN_NEGATIVE);
            money = money.abs();
        }
        
        // 格式化金额 - 使用setScale保留4位小数，确保精度一致
        money = money.setScale(4, java.math.RoundingMode.HALF_UP);
        String strNum = money.toPlainString();
        LogUtils.info("格式化金额（保留4位小数）: " + strNum);
        
        String[] numParts = strNum.split("\\.");
        
        // 处理整数部分
        String integerPart = numParts[0];
        
        // 前置零处理
        while (integerPart.startsWith("0")) {
            integerPart = integerPart.substring(1);
        }
        
        // 重写整数部分逻辑，完全改变处理方式
        if (integerPart.length() > 0) {
            // 按4位分组，每组最大单位是仟
            // 1-4位：个位组
            // 5-8位：万位组
            // 9-12位：亿位组
            // 万亿及以上暂不处理
            LogUtils.info("整数部分: " + integerPart + ", 长度: " + integerPart.length());
            
            int length = integerPart.length();
            
            // 处理亿位组（最多4位）
            if (length > 8) {
                String yiSection = integerPart.substring(0, length - 8);
                LogUtils.info("处理亿位组: " + yiSection);
                sb.append(sectionToChinese(yiSection)).append("亿");
            }
            
            // 处理万位组（最多4位）
            if (length > 4 && length <= 8) {
                String wanSection = integerPart.substring(0, length - 4);
                LogUtils.info("处理万位组: " + wanSection);
                sb.append(sectionToChinese(wanSection)).append("万");
            } else if (length > 8) {
                String wanSection = integerPart.substring(length - 8, length - 4);
                if (!wanSection.equals("0000")) {
                    LogUtils.info("处理万位组: " + wanSection);
                    if (wanSection.charAt(0) == '0') {
                        sb.append("零");
                    }
                    sb.append(sectionToChinese(wanSection)).append("万");
                }
            }
            
            // 处理个位组（最后4位）
            String geSection = integerPart.substring(Math.max(length - 4, 0));
            if (!geSection.equals("0000")) {
                LogUtils.info("处理个位组: " + geSection);
                if (length > 4 && geSection.charAt(0) == '0') {
                    sb.append("零");
                }
                sb.append(sectionToChinese(geSection));
            }
            
            sb.append(CN_FULL);
        }
        
        // 处理小数部分
        String decimalPart = numParts[1];
        int decimalPartLength = decimalPart.length();
        
        if (decimalPartLength > 0) {
            LogUtils.info("处理小数部分: " + decimalPart);
            boolean hasDecimal = false;
            for (int i = 0; i < decimalPartLength; i++) {
                if (i >= CN_DECIMAL_UNIT.length) {
                    break; // 只处理到毫
                }
                int n = decimalPart.charAt(i) - '0';
                if (n != 0) {
                    hasDecimal = true;
                    sb.append(CN_UPPER_NUMBER[n]).append(CN_DECIMAL_UNIT[i]);
                    LogUtils.info("添加小数位: " + CN_UPPER_NUMBER[n] + CN_DECIMAL_UNIT[i]);
                }
            }
            if (!hasDecimal) {
                sb.append(CN_INTEGER);
                LogUtils.info("小数部分全为0，添加\"整\"");
            }
        } else {
            sb.append(CN_INTEGER);
            LogUtils.info("没有小数部分，添加\"整\"");
        }
        
        String result = sb.toString();
        LogUtils.info("数字转换为大写金额完成: " + result);
        LogUtils.logAmountConversion(money.toPlainString(), result, true);
        
        return result;
    }
    
    /**
     * 将数字字符串转换为中文（仅处理一个节，即4位以内）
     * 
     * @param section 数字字符串，长度不超过4
     * @return 中文大写
     */
    private static String sectionToChinese(String section) {
        StringBuilder sb = new StringBuilder();
        int length = section.length();
        
        // 前导零处理
        while (section.startsWith("0")) {
            section = section.substring(1);
        }
        
        if (section.isEmpty()) {
            return "";
        }
        
        length = section.length();
        LogUtils.info("处理4位以内数字节: " + section + ", 长度: " + length);
        
        for (int i = 0; i < length; i++) {
            int digit = section.charAt(i) - '0';
            if (digit == 0) {
                // 当前数位是0的情况
                if (i < length - 1 && section.charAt(i + 1) != '0') {
                    // 当前位是0，下一位不是0，需要加"零"
                    sb.append(CN_UPPER_NUMBER[0]);
                    LogUtils.info("位置" + i + "为0，下一位不为0，添加\"零\"");
                }
            } else {
                // 非0数字
                sb.append(CN_UPPER_NUMBER[digit]);
                if (i < length - 1) {
                    // 不是最后一位，需要加单位（仟、佰、拾）
                    sb.append(CN_INTEGER_UNIT[length - i - 1]);
                    LogUtils.info("位置" + i + "添加: " + CN_UPPER_NUMBER[digit] + CN_INTEGER_UNIT[length - i - 1]);
                } else {
                    LogUtils.info("位置" + i + "添加: " + CN_UPPER_NUMBER[digit]);
                }
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 将中文大写金额转换为数字金额
     *
     * @param chineseAmount 中文大写金额
     * @return 数字金额
     */
    public static BigDecimal convertToNumber(String chineseAmount) {
        LogUtils.info("开始将中文大写金额转换为数字: " + chineseAmount);
        
        if (chineseAmount == null || chineseAmount.isEmpty()) {
            LogUtils.warning("输入为空，返回0");
            return BigDecimal.ZERO;
        }
        
        // 去除空格和特殊字符
        chineseAmount = chineseAmount.replaceAll("[,，　 ]", "");
        LogUtils.info("去除空格和特殊字符后: " + chineseAmount);
        
        boolean negative = false;
        if (chineseAmount.startsWith(CN_NEGATIVE)) {
            negative = true;
            chineseAmount = chineseAmount.substring(1);
            LogUtils.info("检测到负数，移除负号: " + chineseAmount);
        }
        
        if (chineseAmount.equals(CN_ZERO_FULL)) {
            LogUtils.info("检测到\"零元整\"，返回0");
            return BigDecimal.ZERO;
        }
        
        BigDecimal result = BigDecimal.ZERO;
        
        // 匹配万亿级别的金额
        Pattern patternYi = Pattern.compile("([零壹贰叁肆伍陆柒捌玖拾佰仟]+)亿");
        Matcher matcherYi = patternYi.matcher(chineseAmount);
        if (matcherYi.find()) {
            String yiPart = matcherYi.group(1);
            LogUtils.info("检测到亿位: " + yiPart);
            BigDecimal yiValue = convertSection(yiPart).multiply(new BigDecimal("100000000"));
            result = result.add(yiValue);
            LogUtils.info("亿位部分值: " + yiValue);
            chineseAmount = chineseAmount.substring(matcherYi.end());
        }
        
        // 匹配万级别的金额
        Pattern patternWan = Pattern.compile("([零壹贰叁肆伍陆柒捌玖拾佰仟]+)万");
        Matcher matcherWan = patternWan.matcher(chineseAmount);
        if (matcherWan.find()) {
            String wanPart = matcherWan.group(1);
            LogUtils.info("检测到万位: " + wanPart);
            BigDecimal wanValue = convertSection(wanPart).multiply(new BigDecimal("10000"));
            result = result.add(wanValue);
            LogUtils.info("万位部分值: " + wanValue);
            chineseAmount = chineseAmount.substring(matcherWan.end());
        }
        
        // 匹配元级别的金额
        Pattern patternYuan = Pattern.compile("([零壹贰叁肆伍陆柒捌玖拾佰仟]+)元");
        Matcher matcherYuan = patternYuan.matcher(chineseAmount);
        if (matcherYuan.find()) {
            String yuanPart = matcherYuan.group(1);
            LogUtils.info("检测到元位: " + yuanPart);
            BigDecimal yuanValue = convertSection(yuanPart);
            result = result.add(yuanValue);
            LogUtils.info("元位部分值: " + yuanValue);
            chineseAmount = chineseAmount.substring(matcherYuan.end());
        }
        
        // 处理小数部分
        BigDecimal decimalResult = BigDecimal.ZERO;
        
        if (chineseAmount.contains("角")) {
            int index = chineseAmount.indexOf("角");
            char c = chineseAmount.charAt(index - 1);
            int num = getChineseNumber(c);
            BigDecimal value = new BigDecimal("0.1").multiply(new BigDecimal(num));
            decimalResult = decimalResult.add(value);
            LogUtils.info("检测到角位: " + c + ", 值: " + value);
            chineseAmount = chineseAmount.substring(index + 1);
        }
        
        if (chineseAmount.contains("分")) {
            int index = chineseAmount.indexOf("分");
            char c = chineseAmount.charAt(index - 1);
            int num = getChineseNumber(c);
            BigDecimal value = new BigDecimal("0.01").multiply(new BigDecimal(num));
            decimalResult = decimalResult.add(value);
            LogUtils.info("检测到分位: " + c + ", 值: " + value);
            chineseAmount = chineseAmount.substring(index + 1);
        }
        
        if (chineseAmount.contains("厘")) {
            int index = chineseAmount.indexOf("厘");
            char c = chineseAmount.charAt(index - 1);
            int num = getChineseNumber(c);
            BigDecimal value = new BigDecimal("0.001").multiply(new BigDecimal(num));
            decimalResult = decimalResult.add(value);
            LogUtils.info("检测到厘位: " + c + ", 值: " + value);
            chineseAmount = chineseAmount.substring(index + 1);
        }
        
        if (chineseAmount.contains("毫")) {
            int index = chineseAmount.indexOf("毫");
            char c = chineseAmount.charAt(index - 1);
            int num = getChineseNumber(c);
            BigDecimal value = new BigDecimal("0.0001").multiply(new BigDecimal(num));
            decimalResult = decimalResult.add(value);
            LogUtils.info("检测到毫位: " + c + ", 值: " + value);
        }
        
        result = result.add(decimalResult);
        LogUtils.info("小数部分合计: " + decimalResult);
        
        // 处理整字符
        if (chineseAmount.contains("整") && decimalResult.compareTo(BigDecimal.ZERO) == 0) {
            LogUtils.info("检测到\"整\"字，无小数部分");
        }
        
        if (negative) {
            result = result.negate();
            LogUtils.info("应用负号，最终结果: " + result);
        }
        
        // 设置精度为4位小数，与转换为中文时保持一致
        BigDecimal finalResult = result.setScale(4, java.math.RoundingMode.HALF_UP);
        LogUtils.info("中文大写金额转换为数字完成: " + finalResult.toPlainString());
        LogUtils.logAmountConversion(chineseAmount, finalResult.toPlainString(), false);
        
        return finalResult;
    }
    
    /**
     * 转换中文数字到阿拉伯数字
     *
     * @param c 中文数字字符
     * @return 阿拉伯数字
     */
    private static int getChineseNumber(char c) {
        for (int i = 0; i < CN_UPPER_NUMBER.length; i++) {
            if (CN_UPPER_NUMBER[i].charAt(0) == c) {
                return i;
            }
        }
        LogUtils.warning("无法识别的中文数字: " + c + ", 返回0");
        return 0;
    }
    
    /**
     * 转换中文数字节到数值
     *
     * @param section 中文数字节
     * @return 对应的数值
     */
    private static BigDecimal convertSection(String section) {
        LogUtils.info("转换中文数字节: " + section);
        BigDecimal result = BigDecimal.ZERO;
        int number = 0;
        boolean hasNumber = false;
        
        for (int i = 0; i < section.length(); i++) {
            char c = section.charAt(i);
            
            // 处理数字
            int digit = getChineseNumber(c);
            if (digit > 0) {
                number = digit;
                hasNumber = true;
                LogUtils.info("位置" + i + "识别到数字: " + c + " -> " + digit);
                continue;
            }
            
            // 处理单位
            String unit = String.valueOf(c);
            if ("拾".equals(unit)) {
                if (hasNumber) {
                    BigDecimal value = new BigDecimal(number * 10);
                    result = result.add(value);
                    LogUtils.info("处理十位: " + number + "拾 -> " + value);
                } else {
                    BigDecimal value = new BigDecimal(10);
                    result = result.add(value);
                    LogUtils.info("处理十位: 拾 -> " + value);
                }
                number = 0;
                hasNumber = false;
            } else if ("佰".equals(unit)) {
                if (hasNumber) {
                    BigDecimal value = new BigDecimal(number * 100);
                    result = result.add(value);
                    LogUtils.info("处理百位: " + number + "佰 -> " + value);
                } else {
                    BigDecimal value = new BigDecimal(100);
                    result = result.add(value);
                    LogUtils.info("处理百位: 佰 -> " + value);
                }
                number = 0;
                hasNumber = false;
            } else if ("仟".equals(unit)) {
                if (hasNumber) {
                    BigDecimal value = new BigDecimal(number * 1000);
                    result = result.add(value);
                    LogUtils.info("处理千位: " + number + "仟 -> " + value);
                } else {
                    BigDecimal value = new BigDecimal(1000);
                    result = result.add(value);
                    LogUtils.info("处理千位: 仟 -> " + value);
                }
                number = 0;
                hasNumber = false;
            }
        }
        
        // 处理最后一个数字
        if (hasNumber) {
            BigDecimal value = new BigDecimal(number);
            result = result.add(value);
            LogUtils.info("处理末尾数字: " + number + " -> " + value);
        }
        
        LogUtils.info("中文数字节转换结果: " + section + " -> " + result);
        return result;
    }
    
    /**
     * 快速将数字转换为中文大写金额
     *
     * @param amount 数字金额字符串
     * @return 中文大写金额
     */
    public static String convertToChinese(String amount) {
        LogUtils.info("快速数字转中文: " + amount);
        try {
            BigDecimal money = new BigDecimal(amount);
            String result = convertToChineseAmount(money);
            return result;
        } catch (Exception e) {
            LogUtils.error("数字转中文出错: " + e.getMessage(), e);
            LogUtils.logAmountError(amount, e.getMessage(), true);
            return "无效金额";
        }
    }
    
    /**
     * 快速将中文大写金额转换为数字
     *
     * @param chineseAmount 中文大写金额
     * @return 数字金额字符串
     */
    public static String convertToNumeric(String chineseAmount) {
        LogUtils.info("快速中文转数字: " + chineseAmount);
        try {
            BigDecimal number = convertToNumber(chineseAmount);
            // 使用toPlainString确保不使用科学计数法
            String result = number.toPlainString();
            return result;
        } catch (Exception e) {
            LogUtils.error("中文转数字出错: " + e.getMessage(), e);
            LogUtils.logAmountError(chineseAmount, e.getMessage(), false);
            return "无效金额";
        }
    }
} 