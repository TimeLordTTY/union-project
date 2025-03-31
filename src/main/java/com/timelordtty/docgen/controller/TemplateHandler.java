package com.timelordtty.docgen.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.timelordtty.AppLogger;
import com.timelordtty.docgen.service.ExcelTemplateService;
import com.timelordtty.docgen.service.WordTemplateService;

/**
 * 模板处理类，处理模板的加载、分析和生成
 */
public class TemplateHandler {
    private WordTemplateService wordTemplateService;
    private ExcelTemplateService excelTemplateService;
    
    public TemplateHandler() {
        wordTemplateService = new WordTemplateService();
        excelTemplateService = new ExcelTemplateService();
    }
    
    /**
     * 加载Word模板内容
     * 
     * @param file 模板文件
     * @return 模板内容文本
     * @throws Exception 如果加载失败
     */
    public String loadWordTemplate(File file) throws Exception {
        return wordTemplateService.readDocxContent(file.getAbsolutePath());
    }
    
    /**
     * 加载Excel模板内容
     * 
     * @param file 模板文件
     * @return Excel内容（列表的列表）
     * @throws Exception 如果加载失败
     */
    public List<List<String>> loadExcelTemplate(File file) throws Exception {
        return excelTemplateService.readExcelContent(file.getAbsolutePath());
    }
    
    /**
     * 保存Word模板
     * 
     * @param filePath 文件路径
     * @param content 内容
     * @throws Exception 如果保存失败
     */
    public void saveWordTemplate(String filePath, String content) throws Exception {
        wordTemplateService.saveDocxTemplate(filePath, content);
    }
    
    /**
     * 保存Word文档
     * 
     * @param content 文档内容
     * @param filePath 文件路径
     * @throws Exception 如果保存失败
     */
    public void saveWordDocument(String content, String filePath) throws Exception {
        wordTemplateService.saveDocxTemplate(content, filePath);
    }
    
    /**
     * 分析Word模板内容，提取字段和列表
     * 
     * @param content 模板内容
     * @return 包含对象字段和列表字段的映射
     */
    public Map<String, Object> analyzeWordTemplate(String content) {
        AppLogger.info("分析Word模板内容");
        
        Map<String, Object> result = new HashMap<>();
        Set<String> objectFields = new HashSet<>();
        Map<String, List<String>> listFields = new HashMap<>();
        
        // 正则表达式匹配{{字段名}}格式的占位符
        Pattern fieldPattern = Pattern.compile("\\{\\{([^{}]+)\\}\\}");
        Matcher matcher = fieldPattern.matcher(content);
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            
            // 检查是否是列表字段 (格式如: listName.fieldName)
            if (placeholder.contains(".")) {
                String[] parts = placeholder.split("\\.", 2);
                String listName = parts[0];
                String fieldName = parts[1];
                
                if (parts.length == 2 && !listName.trim().isEmpty() && !fieldName.trim().isEmpty()) {
                    // 添加列表字段
                    if (!listFields.containsKey(listName)) {
                        listFields.put(listName, new ArrayList<>());
                    }
                    if (!listFields.get(listName).contains(fieldName)) {
                        listFields.get(listName).add(fieldName);
                    }
                }
            } else if (!placeholder.startsWith("#") && !placeholder.startsWith("/")) {
                // 添加普通字段 (排除#listName和/listName格式)
                objectFields.add(placeholder);
            }
        }
        
        // 查找列表的开始和结束标记 {{#listName}} ... {{/listName}}
        Pattern listPattern = Pattern.compile("\\{\\{#([^{}]+)\\}\\}.*?\\{\\{/\\1\\}\\}", Pattern.DOTALL);
        Matcher listMatcher = listPattern.matcher(content);
        
        while (listMatcher.find()) {
            String listName = listMatcher.group(1);
            if (!listFields.containsKey(listName)) {
                listFields.put(listName, new ArrayList<>());
            }
            
            // 检查列表内部是否有未识别的字段
            String listContent = listMatcher.group(0);
            Matcher fieldInListMatcher = fieldPattern.matcher(listContent);
            
            while (fieldInListMatcher.find()) {
                String placeholder = fieldInListMatcher.group(1);
                if (placeholder.contains(".")) {
                    String[] parts = placeholder.split("\\.", 2);
                    if (parts.length == 2 && parts[0].equals(listName)) {
                        String fieldName = parts[1];
                        if (!listFields.get(listName).contains(fieldName)) {
                            listFields.get(listName).add(fieldName);
                        }
                    }
                }
            }
        }
        
        result.put("objectFields", objectFields);
        result.put("listFields", listFields);
        return result;
    }
    
    /**
     * 替换Word模板中的占位符
     * 
     * @param content 模板内容
     * @param fieldDataMap 字段数据映射
     * @param listFieldDataMap 列表字段数据映射
     * @return 替换后的内容
     */
    public String processWordTemplate(String content, Map<String, String> fieldDataMap, 
                                     Map<String, List<Map<String, String>>> listFieldDataMap) {
        String result = content;
        
        // 替换普通字段占位符
        for (Map.Entry<String, String> entry : fieldDataMap.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }
        
        // 替换列表字段占位符
        for (Map.Entry<String, List<Map<String, String>>> entry : listFieldDataMap.entrySet()) {
            String listName = entry.getKey();
            List<Map<String, String>> listItems = entry.getValue();
            
            // 查找列表区域
            Pattern listPattern = Pattern.compile("\\{\\{#" + listName + "\\}\\}(.*?)\\{\\{/" + listName + "\\}\\}", Pattern.DOTALL);
            Matcher listMatcher = listPattern.matcher(result);
            
            if (listMatcher.find()) {
                String listTemplate = listMatcher.group(1);
                StringBuilder replacement = new StringBuilder();
                
                // 为列表中的每个项生成内容
                for (Map<String, String> item : listItems) {
                    String itemContent = listTemplate;
                    
                    // 替换该项的所有字段
                    for (Map.Entry<String, String> field : item.entrySet()) {
                        String fieldPlaceholder = "{{" + listName + "." + field.getKey() + "}}";
                        String fieldValue = field.getValue() != null ? field.getValue() : "";
                        itemContent = itemContent.replace(fieldPlaceholder, fieldValue);
                    }
                    
                    replacement.append(itemContent);
                }
                
                // 替换整个列表区域
                result = result.replace(listMatcher.group(0), replacement.toString());
            }
        }
        
        return result;
    }
    
    /**
     * 生成Word模板内容
     * 
     * @param objectFields 对象字段列表
     * @param listFields 列表字段列表
     * @return 生成的模板内容
     */
    public String generateWordTemplateContent(List<String> objectFields, List<String> listFields) {
        StringBuilder builder = new StringBuilder();
        builder.append("==== 自动生成的Word模板 ====\n\n");
        
        // 添加对象字段占位符
        builder.append("【对象字段】\n");
        for (String field : objectFields) {
            builder.append("{{").append(field).append("}}\n");
        }
        builder.append("\n");
        
        // 添加列表字段占位符
        builder.append("【列表字段】\n");
        for (String listName : listFields) {
            builder.append("{{#").append(listName).append("}}\n");
            builder.append("  列表项: {{").append(listName).append(".name}}\n");
            builder.append("{{/").append(listName).append("}}\n\n");
        }
        
        return builder.toString();
    }
    
    /**
     * 生成Excel模板
     * 
     * @param templateFilePath 模板文件路径
     * @param fieldMap 字段映射
     * @throws Exception 如果生成失败
     */
    public void generateExcelTemplate(String templateFilePath, Map<String, List<String>> fieldMap) throws Exception {
        excelTemplateService.createTemplate(templateFilePath, fieldMap);
    }
} 