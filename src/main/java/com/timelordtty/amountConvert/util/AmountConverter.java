package com.timelordtty.amountConvert.util;

import com.timelordtty.AppLogger;

import java.math.BigDecimal;

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
        AppLogger.info("开始将数字转换为中文大写金额: " + money.toPlainString());
        
        StringBuilder sb = new StringBuilder();
        
        // 零元整的情况
        if (money.compareTo(BigDecimal.ZERO) == 0) {
            AppLogger.info("数字为0，返回\"零元整\"");
            return CN_ZERO_FULL;
        }
        
        // 负数处理
        if (money.compareTo(BigDecimal.ZERO) < 0) {
            AppLogger.info("处理负数: " + money.toPlainString());
            sb.append(CN_NEGATIVE);
            money = money.abs();
        }
        
        // 格式化金额 - 使用setScale保留4位小数，确保精度一致
        money = money.setScale(4, java.math.RoundingMode.HALF_UP);
        String strNum = money.toPlainString();
        AppLogger.info("格式化金额（保留4位小数）: " + strNum);
        
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
            AppLogger.info("整数部分: " + integerPart + ", 长度: " + integerPart.length());
            
            int length = integerPart.length();
            
            // 处理亿位组（最多4位）
            if (length > 8) {
                String yiSection = integerPart.substring(0, length - 8);
                AppLogger.info("处理亿位组: " + yiSection);
                sb.append(sectionToChinese(yiSection)).append("亿");
            }
            
            // 处理万位组（最多4位）
            if (length > 4 && length <= 8) {
                String wanSection = integerPart.substring(0, length - 4);
                AppLogger.info("处理万位组: " + wanSection);
                sb.append(sectionToChinese(wanSection)).append("万");
            } else if (length > 8) {
                String wanSection = integerPart.substring(length - 8, length - 4);
                if (!wanSection.equals("0000")) {
                    AppLogger.info("处理万位组: " + wanSection);
                    if (wanSection.charAt(0) == '0') {
                        sb.append("零");
                    }
                    sb.append(sectionToChinese(wanSection)).append("万");
                }
            }
            
            // 处理个位组（最后4位）
            String geSection = integerPart.substring(Math.max(length - 4, 0));
            if (!geSection.equals("0000")) {
                AppLogger.info("处理个位组: " + geSection);
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
            AppLogger.info("处理小数部分: " + decimalPart);
            boolean hasDecimal = false;
            for (int i = 0; i < decimalPartLength; i++) {
                if (i >= CN_DECIMAL_UNIT.length) {
                    break; // 只处理到毫
                }
                int n = decimalPart.charAt(i) - '0';
                if (n != 0) {
                    hasDecimal = true;
                    sb.append(CN_UPPER_NUMBER[n]).append(CN_DECIMAL_UNIT[i]);
                    AppLogger.info("添加小数位: " + CN_UPPER_NUMBER[n] + CN_DECIMAL_UNIT[i]);
                }
            }
            if (!hasDecimal) {
                sb.append(CN_INTEGER);
                AppLogger.info("小数部分全为0，添加\"整\"");
            }
        } else {
            sb.append(CN_INTEGER);
            AppLogger.info("没有小数部分，添加\"整\"");
        }
        
        String result = sb.toString();
        AppLogger.info("数字转换为大写金额完成: " + result);
        AppLogger.amountConversion(money.toPlainString(), result, true);
        
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
        AppLogger.info("处理4位以内数字节: " + section + ", 长度: " + length);
        
        for (int i = 0; i < length; i++) {
            int digit = section.charAt(i) - '0';
            if (digit == 0) {
                // 当前数位是0的情况
                if (i < length - 1 && section.charAt(i + 1) != '0') {
                    // 当前位是0，下一位不是0，需要加"零"
                    sb.append(CN_UPPER_NUMBER[0]);
                    AppLogger.info("位置" + i + "为0，下一位不为0，添加\"零\"");
                }
            } else {
                // 非0数字
                sb.append(CN_UPPER_NUMBER[digit]);
                if (i < length - 1) {
                    // 不是最后一位，需要加单位（仟、佰、拾）
                    sb.append(CN_INTEGER_UNIT[length - i - 1]);
                    AppLogger.info("位置" + i + "添加: " + CN_UPPER_NUMBER[digit] + CN_INTEGER_UNIT[length - i - 1]);
                } else {
                    AppLogger.info("位置" + i + "添加: " + CN_UPPER_NUMBER[digit]);
                }
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 将中文大写金额转换为数字
     *
     * @param chineseAmount 中文大写金额
     * @return 数字金额
     */
    public static BigDecimal convertToNumber(String chineseAmount) {
        AppLogger.info("开始将中文大写金额转换为数字: " + chineseAmount);
        
        if (chineseAmount == null || chineseAmount.isEmpty()) {
            AppLogger.warning("输入为空，返回0");
            return BigDecimal.ZERO;
        }
        
        // 去除空格和特殊字符
        chineseAmount = chineseAmount.replaceAll("[,，　 ]", "");
        AppLogger.info("去除空格和特殊字符后: " + chineseAmount);
        
        boolean negative = false;
        if (chineseAmount.startsWith(CN_NEGATIVE)) {
            negative = true;
            chineseAmount = chineseAmount.substring(1);
            AppLogger.info("检测到负数，移除负号: " + chineseAmount);
        }
        
        if (chineseAmount.equals(CN_ZERO_FULL)) {
            AppLogger.info("检测到\"零元整\"，返回0");
            return BigDecimal.ZERO;
        }
        
        // 移除"整"和"元"字符，便于后续解析
        chineseAmount = chineseAmount.replace("整", "").replace("元", "");
        AppLogger.info("移除'整'和'元'后的金额: " + chineseAmount);
        
        BigDecimal result = BigDecimal.ZERO;
        
        // 完全重写解析逻辑，采用更清晰的分段处理方式
        String[] sections = new String[] {"", "", ""}; // 亿以上，万到亿，个位到万
        
        // 分割不同数量级
        if (chineseAmount.contains("亿")) {
            String[] parts = chineseAmount.split("亿");
            sections[0] = parts[0]; // 亿以上部分
            
            if (parts.length > 1) {
                String remainingPart = parts[1];
                
                if (remainingPart.contains("万")) {
                    String[] wanParts = remainingPart.split("万");
                    sections[1] = wanParts[0]; // 万到亿部分
                    
                    if (wanParts.length > 1) {
                        sections[2] = wanParts[1]; // 个位到万部分
                    }
                } else {
                    sections[2] = remainingPart; // 不包含万，全是个位部分
                }
            }
        } else if (chineseAmount.contains("万")) {
            String[] parts = chineseAmount.split("万");
            sections[1] = parts[0]; // 万部分
            
            if (parts.length > 1) {
                sections[2] = parts[1]; // 个位部分
            }
        } else {
            sections[2] = chineseAmount; // 全是个位部分
        }
        
        AppLogger.info("分段后: 亿以上=[" + sections[0] + "], 万到亿=[" + sections[1] + "], 个位到万=[" + sections[2] + "]");
        
        // 处理亿以上部分
        if (!sections[0].isEmpty()) {
            BigDecimal value = parseChineseNumber(sections[0]);
            value = value.multiply(new BigDecimal("100000000"));
            result = result.add(value);
            AppLogger.info("亿以上部分计算结果: " + value);
        }
        
        // 处理万到亿部分
        if (!sections[1].isEmpty()) {
            BigDecimal value = parseChineseNumber(sections[1]);
            value = value.multiply(new BigDecimal("10000"));
            result = result.add(value);
            AppLogger.info("万到亿部分计算结果: " + value);
        }
        
        // 处理个位部分
        if (!sections[2].isEmpty()) {
            BigDecimal value = parseChineseNumber(sections[2]);
            result = result.add(value);
            AppLogger.info("个位部分计算结果: " + value);
        }
        
        // 处理小数部分 (角、分等)
        String[] decimalParts = {"角", "分", "厘", "毫"};
        BigDecimal[] decimalValues = {
            new BigDecimal("0.1"), 
            new BigDecimal("0.01"), 
            new BigDecimal("0.001"), 
            new BigDecimal("0.0001")
        };
        
        // 最初的字符串可能包含小数部分
        for (int i = 0; i < decimalParts.length; i++) {
            if (chineseAmount.contains(decimalParts[i])) {
                int index = chineseAmount.indexOf(decimalParts[i]);
                if (index > 0) {
                    char c = chineseAmount.charAt(index - 1);
                    int num = getChineseNumber(c);
                    if (num >= 0) {
                        BigDecimal value = decimalValues[i].multiply(new BigDecimal(num));
                        result = result.add(value);
                        AppLogger.info("小数部分(" + decimalParts[i] + ")计算结果: " + value);
                    }
                }
            }
        }
        
        if (negative) {
            result = result.negate();
            AppLogger.info("应用负号，最终结果: " + result);
        }
        
        // 设置精度为4位小数，与转换为中文时保持一致
        BigDecimal finalResult = result.setScale(4, java.math.RoundingMode.HALF_UP);
        AppLogger.info("中文大写金额转换为数字完成: " + finalResult.toPlainString());
        AppLogger.amountConversion(chineseAmount, finalResult.toPlainString(), false);
        
        return finalResult;
    }
    
    /**
     * 解析不带单位的中文数字（不含"万"、"亿"等单位）
     * 例如"一千二百三十四"解析为1234
     *
     * @param chineseNumber 不带单位的中文数字
     * @return 解析结果
     */
    private static BigDecimal parseChineseNumber(String chineseNumber) {
        AppLogger.info("解析中文数字: " + chineseNumber);
        
        if (chineseNumber == null || chineseNumber.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 如果以"零"开头，移除
        if (chineseNumber.startsWith("零")) {
            chineseNumber = chineseNumber.substring(1);
        }
        
        BigDecimal result = BigDecimal.ZERO;
        
        // 记录当前数字
        int number = 0;
        
        // 处理特殊情况"壹拾"开头应该是10而不是1拾
        if (chineseNumber.startsWith("壹拾") || chineseNumber.startsWith("一拾")) {
            chineseNumber = "拾" + chineseNumber.substring(2);
        }
        
        for (int i = 0; i < chineseNumber.length(); i++) {
            char c = chineseNumber.charAt(i);
            
            // 获取数字的值
            int value = getChineseNumber(c);
            
            // 如果是数字
            if (value >= 0) {
                number = value;
                // 如果是最后一个字符，直接加入结果
                if (i == chineseNumber.length() - 1) {
                    result = result.add(new BigDecimal(number));
                    AppLogger.debug("解析最后数字字符 " + c + " -> " + number + ", 当前结果 = " + result);
                }
                continue;
            }
            
            // 处理单位
            if (c == '拾' || c == '十') {
                // 如果前面没有数字，默认为1
                if (number == 0) {
                    number = 1;
                }
                result = result.add(new BigDecimal(number * 10));
                AppLogger.debug("解析拾位单位, 数字=" + number + ", 添加=" + (number * 10) + ", 当前结果=" + result);
                number = 0;
            } else if (c == '佰' || c == '百') {
                // 如果前面没有数字，默认为1
                if (number == 0) {
                    number = 1;
                }
                result = result.add(new BigDecimal(number * 100));
                AppLogger.debug("解析佰位单位, 数字=" + number + ", 添加=" + (number * 100) + ", 当前结果=" + result);
                number = 0;
            } else if (c == '仟' || c == '千') {
                // 如果前面没有数字，默认为1
                if (number == 0) {
                    number = 1;
                }
                result = result.add(new BigDecimal(number * 1000));
                AppLogger.debug("解析仟位单位, 数字=" + number + ", 添加=" + (number * 1000) + ", 当前结果=" + result);
                number = 0;
            }
        }
        
        AppLogger.info("中文数字 '" + chineseNumber + "' 解析结果: " + result);
        return result;
    }
    
    /**
     * 将中文数字字符转换为阿拉伯数字
     *
     * @param c 中文数字字符
     * @return 阿拉伯数字值，如果字符不是中文数字则返回-1
     */
    private static int getChineseNumber(char c) {
        switch (c) {
            case '零': case '〇': return 0;
            case '一': case '壹': return 1;
            case '二': case '贰': case '两': case '貳': return 2;
            case '三': case '叁': case '參': return 3;
            case '四': case '肆': case '䦉': return 4;
            case '五': case '伍': return 5;
            case '六': case '陆': case '陸': return 6;
            case '七': case '柒': case '漆': return 7;
            case '八': case '捌': return 8;
            case '九': case '玖': return 9;
            default: return -1;
        }
    }
    
    /**
     * 快速将数字转换为中文大写金额
     *
     * @param amount 数字金额字符串
     * @return 中文大写金额
     */
    public static String convertToChinese(String amount) {
        AppLogger.info("快速数字转中文: " + amount);
        try {
            BigDecimal money = new BigDecimal(amount);
            String result = convertToChineseAmount(money);
            return result;
        } catch (Exception e) {
            AppLogger.error("数字转中文出错: " + e.getMessage(), e);
            AppLogger.amountError(amount, e.getMessage(), true);
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
        AppLogger.info("快速中文转数字: " + chineseAmount);
        try {
            BigDecimal number = convertToNumber(chineseAmount);
            // 使用toPlainString确保不使用科学计数法
            String result = number.toPlainString();
            return result;
        } catch (Exception e) {
            AppLogger.error("中文转数字出错: " + e.getMessage(), e);
            AppLogger.amountError(chineseAmount, e.getMessage(), false);
            return "无效金额";
        }
    }
} 