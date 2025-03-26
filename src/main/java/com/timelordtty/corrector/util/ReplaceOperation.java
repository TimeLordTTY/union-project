package com.timelordtty.corrector.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.timelordtty.AppLogger;
import com.timelordtty.corrector.model.ReplaceRule;
import com.timelordtty.corrector.model.TextCorrection;

/**
 * 文本替换操作工具类
 */
public class ReplaceOperation {
    
    /**
     * 执行文本替换操作
     * @param originalText 原始文本
     * @param rules 替换规则列表
     * @return 替换结果，包含替换后文本和替换项列表
     */
    public static ReplaceResult replace(String originalText, List<ReplaceRule> rules) {
        if (originalText == null || originalText.isEmpty() || rules == null || rules.isEmpty()) {
            return new ReplaceResult(originalText, new ArrayList<>());
        }
        
        AppLogger.info("开始执行文本替换，文本长度: " + originalText.length() + "，规则数量: " + rules.size());
        
        String replacedText = originalText;
        List<TextCorrection> replacements = new ArrayList<>();
        
        for (ReplaceRule rule : rules) {
            String originalPattern = rule.getOriginalText();
            String replacement = rule.getReplacementText();
            
            if (originalPattern == null || originalPattern.isEmpty()) {
                continue;
            }
            
            try {
                // 使用Pattern检查是否是正则表达式模式
                Pattern pattern = Pattern.compile(originalPattern);
                Matcher matcher = pattern.matcher(replacedText);
                
                StringBuffer sb = new StringBuffer();
                int position = 0;
                
                // 循环查找每个匹配项
                while (matcher.find()) {
                    position = matcher.start();
                    String matchedText = matcher.group();
                    
                    // 记录替换项
                    String positionStr = "位置: " + position + "-" + (position + matchedText.length());
                    TextCorrection correction = new TextCorrection(matchedText, replacement, positionStr);
                    replacements.add(correction);
                    
                    // 执行替换
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                }
                matcher.appendTail(sb);
                
                // 更新已替换的文本
                replacedText = sb.toString();
                
            } catch (Exception e) {
                // 如果正则表达式无效，则使用普通的字符串替换
                AppLogger.warn("正则表达式无效或执行错误: " + e.getMessage() + "，尝试使用普通文本替换");
                
                try {
                    // 使用普通文本替换
                    if (replacedText.contains(originalPattern)) {
                        int position = replacedText.indexOf(originalPattern);
                        while (position >= 0) {
                            // 记录替换项
                            String positionStr = "位置: " + position + "-" + (position + originalPattern.length());
                            TextCorrection correction = new TextCorrection(originalPattern, replacement, positionStr);
                            replacements.add(correction);
                            
                            position = replacedText.indexOf(originalPattern, position + 1);
                        }
                        
                        // 执行替换
                        replacedText = replacedText.replace(originalPattern, replacement);
                    }
                } catch (Exception ex) {
                    AppLogger.error("执行普通文本替换时出错: " + ex.getMessage(), ex);
                }
            }
        }
        
        AppLogger.info("文本替换完成，共执行了 " + replacements.size() + " 处替换");
        
        return new ReplaceResult(replacedText, replacements);
    }
    
    /**
     * 替换结果类
     */
    public static class ReplaceResult {
        private final String replacedText;
        private final List<TextCorrection> replacements;
        
        public ReplaceResult(String replacedText, List<TextCorrection> replacements) {
            this.replacedText = replacedText;
            this.replacements = replacements;
        }
        
        public String getReplacedText() {
            return replacedText;
        }
        
        public List<TextCorrection> getReplacements() {
            return replacements;
        }
    }
} 