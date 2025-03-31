package com.timelordtty.docgen.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

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
        try {
            AppLogger.info("加载Word模板: " + file.getAbsolutePath());
            
            // 读取模板内容，但保留原始格式
            String content = wordTemplateService.readDocxContent(file.getAbsolutePath());
            
            // 不对内容进行任何格式化处理，直接返回原始内容
            AppLogger.info("Word模板加载成功");
            return content;
        } catch (Exception e) {
            AppLogger.error("加载Word模板失败", e);
            throw e;
        }
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
        try {
            AppLogger.info("保存Word模板: " + filePath);
            
            // 不对内容进行任何格式化处理，直接保存
            wordTemplateService.saveDocxTemplate(filePath, content);
            AppLogger.info("Word模板保存成功");
        } catch (Exception e) {
            AppLogger.error("保存Word模板失败", e);
            throw e;
        }
    }
    
    /**
     * 保存Word文档
     * 
     * @param content 文档内容
     * @param outputPath 输出路径
     * @throws Exception 异常
     */
    public void saveWordDocument(String content, String outputPath) throws Exception {
        try {
            AppLogger.info("保存Word文档到: " + outputPath);
            
            // 创建输出目录
            File outputFile = new File(outputPath);
            File outputDir = outputFile.getParentFile();
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 使用POI API保存Word文档
            XWPFDocument document = new XWPFDocument();
            
            // 按行分割内容
            String[] lines = content.split("\n");
            for (String line : lines) {
                // 对于空行或仅含空格的行，创建空段落
                if (line.trim().isEmpty()) {
                    document.createParagraph();
                    continue;
                }
                
                // 创建段落并添加文本
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(line);
            }
            
            // 保存文档
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                document.write(fos);
            }
            
            AppLogger.info("Word文档保存成功: " + outputPath);
        } catch (Exception e) {
            AppLogger.error("保存Word文档失败", e);
            throw new Exception("保存Word文档失败: " + e.getMessage(), e);
        }
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
        
        // 首先处理表格中的占位符
        int tableStart = -1;
        while ((tableStart = content.indexOf("[TABLE_START]", tableStart + 1)) != -1) {
            int tableEnd = content.indexOf("[TABLE_END]", tableStart);
            if (tableEnd == -1) break;
            
            String tableContent = content.substring(tableStart, tableEnd + "[TABLE_END]".length());
            
            // 分析表格中的所有单元格
            int cellStart = -1;
            while ((cellStart = tableContent.indexOf("[CELL_START]", cellStart + 1)) != -1) {
                int cellEnd = tableContent.indexOf("[CELL_END]", cellStart);
                if (cellEnd == -1) break;
                
                String cellContent = tableContent.substring(cellStart + "[CELL_START]".length(), cellEnd);
                
                // 在单元格内查找占位符
                Pattern pattern = Pattern.compile("\\{\\{([^{}]+)\\}\\}");
                Matcher matcher = pattern.matcher(cellContent);
                
                while (matcher.find()) {
                    String placeholder = matcher.group(1);
                    
                    // 处理普通字段
                    if (!placeholder.contains(".") && !placeholder.startsWith("#") && !placeholder.startsWith("/")) {
                        objectFields.add(placeholder);
                    } 
                    // 处理列表字段
                    else if (placeholder.contains(".")) {
                        String[] parts = placeholder.split("\\.", 2);
                        if (parts.length == 2) {
                            String listName = parts[0];
                            String fieldName = parts[1];
                            
                            if (!listName.trim().isEmpty() && !fieldName.trim().isEmpty()) {
                                if (!listFields.containsKey(listName)) {
                                    listFields.put(listName, new ArrayList<>());
                                }
                                if (!listFields.get(listName).contains(fieldName)) {
                                    listFields.get(listName).add(fieldName);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 正则表达式匹配{{字段名}}格式的占位符 (处理非表格内容)
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
        
        AppLogger.info("分析结果 - 对象字段: " + objectFields.size() + ", 列表字段: " + listFields.size());
        return result;
    }
    
    /**
     * 处理Word模板内容，替换占位符
     * 
     * @param content 模板内容
     * @param fieldDataMap 字段数据映射
     * @param listFieldDataMap 列表字段数据映射
     * @return 处理后的内容
     */
    public String processWordTemplate(String content, Map<String, String> fieldDataMap, Map<String, List<Map<String, String>>> listFieldDataMap) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        AppLogger.info("开始处理Word模板...");
        
        // 复制一份内容用于处理
        String result = content;
        
        try {
            // 处理普通字段
            for (Map.Entry<String, String> entry : fieldDataMap.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue() : "";
                result = result.replace(placeholder, value);
            }
            
            // 处理列表字段
            for (Map.Entry<String, List<Map<String, String>>> entry : listFieldDataMap.entrySet()) {
                String listName = entry.getKey();
                List<Map<String, String>> listItems = entry.getValue();
                
                // 查找列表区域 {{#listName}}...{{/listName}}
                String listBegin = "{{#" + listName + "}}";
                String listEnd = "{{/" + listName + "}}";
                
                if (result.contains(listBegin) && result.contains(listEnd)) {
                    // 提取列表模板部分
                    int beginIndex = result.indexOf(listBegin);
                    int endIndex = result.indexOf(listEnd) + listEnd.length();
                    
                    if (beginIndex != -1 && endIndex != -1 && beginIndex < endIndex) {
                        String listTemplate = result.substring(beginIndex + listBegin.length(), result.indexOf(listEnd));
                        
                        // 生成列表内容
                        StringBuilder listContent = new StringBuilder();
                        
                        // 为每个项目生成内容
                        for (Map<String, String> item : listItems) {
                            String itemContent = listTemplate;
                            
                            // 替换项目中的字段
                            for (Map.Entry<String, String> field : item.entrySet()) {
                                String fieldPlaceholder = "{{" + listName + "." + field.getKey() + "}}";
                                String fieldValue = field.getValue() != null ? field.getValue() : "";
                                itemContent = itemContent.replace(fieldPlaceholder, fieldValue);
                            }
                            
                            listContent.append(itemContent);
                        }
                        
                        // 替换整个列表区域
                        result = result.substring(0, beginIndex) + listContent.toString() + result.substring(endIndex);
                    }
                }
            }
            
            AppLogger.info("Word模板处理完成");
            return result;
        } catch (Exception e) {
            AppLogger.error("处理Word模板时发生错误", e);
            throw e;
        }
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
        
        // 使用更精简的标题
        builder.append("== 文档模板 ==\n\n");
        
        // 添加对象字段表格
        if (!objectFields.isEmpty()) {
            builder.append("【基本信息】\n");
            builder.append("[TABLE_START]\n");
            
            // 添加表头
            builder.append("[ROW_START][CELL_START]字段名称[CELL_END][CELL_START]值[CELL_END][ROW_END]\n");
            
            // 添加字段行
            for (String field : objectFields) {
                builder.append("[ROW_START][CELL_START]").append(field).append("[CELL_END]");
                builder.append("[CELL_START]{{").append(field).append("}}[CELL_END][ROW_END]\n");
            }
            
            builder.append("[TABLE_END]\n\n");
        }
        
        // 添加列表字段表格
        if (!listFields.isEmpty()) {
            for (String listName : listFields) {
                builder.append("【").append(listName).append("】\n");
                builder.append("[TABLE_START]\n");
                
                // 表头行
                builder.append("[ROW_START]");
                // 默认添加ID、名称、描述三个基本字段
                builder.append("[CELL_START]ID[CELL_END]");
                builder.append("[CELL_START]名称[CELL_END]");
                builder.append("[CELL_START]描述[CELL_END]");
                builder.append("[ROW_END]\n");
                
                // 示例数据行
                builder.append("[ROW_START]");
                builder.append("[CELL_START]{{").append(listName).append(".id}}[CELL_END]");
                builder.append("[CELL_START]{{").append(listName).append(".name}}[CELL_END]");
                builder.append("[CELL_START]{{").append(listName).append(".description}}[CELL_END]");
                builder.append("[ROW_END]\n");
                
                builder.append("[TABLE_END]\n\n");
                
                // 添加带循环的列表示例
                builder.append("循环示例:\n");
                builder.append("{{#").append(listName).append("}}\n");
                builder.append("- ID: {{").append(listName).append(".id}}\n");
                builder.append("- 名称: {{").append(listName).append(".name}}\n");
                builder.append("- 描述: {{").append(listName).append(".description}}\n");
                builder.append("{{/").append(listName).append("}}\n\n");
            }
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