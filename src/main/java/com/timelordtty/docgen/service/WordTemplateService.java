package com.timelordtty.docgen.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.timelordtty.AppLogger;

/**
 * Word模板服务类，提供Word文档模板的读取、解析和生成功能
 */
public class WordTemplateService {
    
    /**
     * 读取Word文档内容为纯文本
     * 
     * @param filePath Word文档路径
     * @return 文档内容文本
     * @throws IOException IO异常
     */
    public String readDocxContent(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            // 读取段落
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText());
                // 保持段落换行以保留文档结构
                content.append("\n");
            }
            
            // 读取表格
            for (XWPFTable table : document.getTables()) {
                // 添加表格标记，帮助保留文档结构
                content.append("[TABLE_START]\n");
                for (XWPFTableRow row : table.getRows()) {
                    // 行开始
                    content.append("[ROW_START]");
                    for (XWPFTableCell cell : row.getTableCells()) {
                        // 单元格内容
                        content.append("[CELL_START]");
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            content.append(paragraph.getText());
                            content.append(" ");
                        }
                        content.append("[CELL_END]");
                    }
                    // 行结束
                    content.append("[ROW_END]\n");
                }
                content.append("[TABLE_END]\n\n");
            }
        }
        
        return content.toString();
    }
    
    /**
     * 保存Word模板
     * 
     * @param filePath 文件路径
     * @param content 文档内容
     * @throws IOException IO异常
     */
    public void saveDocxTemplate(String filePath, String content) throws IOException {
        // 创建新文档
        try (XWPFDocument document = new XWPFDocument()) {
            // 简化实现：仅创建包含内容的段落
            String[] paragraphs = content.split("\n");
            
            for (String paragraph : paragraphs) {
                XWPFParagraph p = document.createParagraph();
                XWPFRun run = p.createRun();
                run.setText(paragraph);
            }
            
            // 保存文档
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                document.write(out);
            }
        }
        
        AppLogger.info("保存Word模板: " + filePath);
    }
    
    /**
     * 生成模板
     * 
     * @param outputPath 输出路径
     * @param content 模板内容
     * @throws IOException IO异常
     */
    public void generateTemplate(String outputPath, String content) throws IOException {
        saveDocxTemplate(outputPath, content);
    }
    
    /**
     * 根据模板和数据生成文档
     * 
     * @param templatePath 模板路径
     * @param outputPath 输出路径
     * @param fieldDataMap 普通字段数据
     * @param listFieldDataMap 列表字段数据
     * @throws IOException IO异常
     */
    public void generateDocument(
            String templatePath, 
            String outputPath, 
            Map<String, String> fieldDataMap,
            Map<String, List<Map<String, String>>> listFieldDataMap) throws IOException {
        
        // 读取模板
        try (FileInputStream fis = new FileInputStream(templatePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            // 处理段落中的占位符
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                processParagraph(paragraph, fieldDataMap, listFieldDataMap);
            }
            
            // 处理表格中的占位符
            for (XWPFTable table : document.getTables()) {
                processTable(table, fieldDataMap, listFieldDataMap);
            }
            
            // 保存生成的文档
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                document.write(fos);
            }
        }
        
        AppLogger.info("生成Word文档: " + outputPath);
    }
    
    /**
     * 处理表格中的占位符
     */
    private void processTable(XWPFTable table, Map<String, String> fieldDataMap, 
            Map<String, List<Map<String, String>>> listFieldDataMap) {
        
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    processParagraph(paragraph, fieldDataMap, listFieldDataMap);
                }
            }
        }
    }
    
    /**
     * 处理段落中的占位符
     * 
     * @param paragraph 段落
     * @param fieldDataMap 字段数据
     * @param listFieldDataMap 列表数据
     */
    private void processParagraph(
            XWPFParagraph paragraph, 
            Map<String, String> fieldDataMap,
            Map<String, List<Map<String, String>>> listFieldDataMap) {
        
        String text = paragraph.getText();
        
        // 检查段落是否包含占位符
        if (!text.contains("{{")) {
            return; // 不包含占位符，直接返回
        }
        
        // 替换普通字段占位符
        boolean modified = false;
        for (Map.Entry<String, String> entry : fieldDataMap.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            if (text.contains(placeholder)) {
                text = text.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
                modified = true;
            }
        }
        
        // 如果有修改，更新段落内容
        if (modified) {
            // 清除原有内容并保留样式
            XWPFRun firstRun = null;
            if (!paragraph.getRuns().isEmpty()) {
                firstRun = paragraph.getRuns().get(0);
            }
            
            // 清除所有现有Run
            for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }
            
            // 创建新的Run并应用原有样式
            XWPFRun newRun = paragraph.createRun();
            if (firstRun != null) {
                // 复制样式
                newRun.setBold(firstRun.isBold());
                newRun.setItalic(firstRun.isItalic());
                newRun.setUnderline(firstRun.getUnderline());
                newRun.setColor(firstRun.getColor());
                newRun.setFontFamily(firstRun.getFontFamily());
                newRun.setFontSize(firstRun.getFontSize());
            }
            
            // 设置新内容
            newRun.setText(text);
        }
        
        // 查找列表占位符并处理
        processListPlaceholders(paragraph, listFieldDataMap);
    }
    
    /**
     * 处理列表占位符
     */
    private void processListPlaceholders(XWPFParagraph paragraph, Map<String, List<Map<String, String>>> listFieldDataMap) {
        String text = paragraph.getText();
        
        // 查找列表开始标记 {{#listName}}
        for (String listName : listFieldDataMap.keySet()) {
            String startTag = "{{#" + listName + "}}";
            String endTag = "{{/" + listName + "}}";
            
            if (text.contains(startTag) && text.contains(endTag)) {
                // 获取列表数据
                List<Map<String, String>> listData = listFieldDataMap.get(listName);
                if (listData == null || listData.isEmpty()) {
                    continue;
                }
                
                // 提取列表模板内容
                int startIndex = text.indexOf(startTag) + startTag.length();
                int endIndex = text.indexOf(endTag);
                if (startIndex >= endIndex) {
                    continue;
                }
                
                String templateContent = text.substring(startIndex, endIndex);
                
                // 构建列表替换内容
                StringBuilder listContent = new StringBuilder();
                for (Map<String, String> item : listData) {
                    String itemContent = templateContent;
                    for (Map.Entry<String, String> field : item.entrySet()) {
                        String fieldPlaceholder = "{{" + listName + "." + field.getKey() + "}}";
                        itemContent = itemContent.replace(fieldPlaceholder, field.getValue() != null ? field.getValue() : "");
                    }
                    listContent.append(itemContent);
                }
                
                // 替换整个列表内容
                String finalContent = text.substring(0, text.indexOf(startTag)) 
                        + listContent.toString() 
                        + text.substring(text.indexOf(endTag) + endTag.length());
                
                // 清除原有内容
                for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }
                
                // 添加新内容
                XWPFRun newRun = paragraph.createRun();
                newRun.setText(finalContent);
                break; // 一个段落只处理一个列表
            }
        }
    }
} 