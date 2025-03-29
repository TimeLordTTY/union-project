package com.timelordtty.docgen.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.timelordtty.AppLogger;
import com.timelordtty.docgen.model.TemplateField;

/**
 * Word模板服务
 */
public class WordTemplateService {
    
    /**
     * 读取Word文档内容
     * @param filePath 文件路径
     * @return 文档内容
     * @throws Exception 异常
     */
    public String readDocxContent(String filePath) throws Exception {
        StringBuilder content = new StringBuilder();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            // 读取段落
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
            
            // 读取表格
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        content.append(cell.getText()).append("\t");
                    }
                    content.append("\n");
                }
            }
            
            AppLogger.info("成功读取Word文档: " + filePath);
        } catch (Exception e) {
            AppLogger.error("读取Word文档失败: " + filePath, e);
            throw e;
        }
        
        return content.toString();
    }
    
    /**
     * 保存Word模板
     * @param filePath 文件路径
     * @param content 文档内容
     * @throws Exception 异常
     */
    public void saveDocxTemplate(String filePath, String content) throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {
            // 创建段落
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(content);
            
            // 保存文档
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                document.write(out);
            }
            
            AppLogger.info("成功保存Word模板: " + filePath);
        } catch (Exception e) {
            AppLogger.error("保存Word模板失败: " + filePath, e);
            throw e;
        }
    }
    
    /**
     * 保存Word文档
     * @param filePath 文件路径
     * @param content 文档内容
     * @throws Exception 异常
     */
    public void saveDocxDocument(String filePath, String content) throws Exception {
        saveDocxTemplate(filePath, content);
    }
    
    /**
     * 生成Word模板
     * @param filePath 文件路径
     * @param fields 字段列表
     * @throws Exception 异常
     */
    public void generateWordTemplate(String filePath, List<TemplateField> fields) throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {
            // 创建标题段落
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("文档模板");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            
            // 创建说明段落
            XWPFParagraph descParagraph = document.createParagraph();
            XWPFRun descRun = descParagraph.createRun();
            descRun.setText("本文档包含以下字段：");
            
            // 创建表格
            XWPFTable table = document.createTable(fields.size() + 1, 3);
            table.setWidth("100%");
            
            // 设置表头
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.getCell(0).setText("字段名称");
            headerRow.getCell(1).setText("字段类型");
            headerRow.getCell(2).setText("字段占位符");
            
            // 分离对象字段和列表字段
            List<TemplateField> objectFields = new ArrayList<>();
            List<TemplateField> listFields = new ArrayList<>();
            Map<String, List<TemplateField>> listItemFields = new HashMap<>();
            
            for (TemplateField field : fields) {
                if (field.isList()) {
                    listFields.add(field);
                    listItemFields.put(field.getName(), new ArrayList<>());
                } else if (field.getName().contains(".")) {
                    // 这是列表项字段
                    String listName = field.getName().substring(0, field.getName().indexOf("."));
                    List<TemplateField> itemFields = listItemFields.getOrDefault(listName, new ArrayList<>());
                    itemFields.add(field);
                    listItemFields.put(listName, itemFields);
                } else {
                    objectFields.add(field);
                }
            }
            
            // 填充字段
            int rowIndex = 1;
            
            // 添加对象字段
            for (TemplateField field : objectFields) {
                XWPFTableRow row = table.getRow(rowIndex++);
                row.getCell(0).setText(field.getName());
                row.getCell(1).setText("普通");
                row.getCell(2).setText("{{" + field.getName() + "}}");
            }
            
            // 添加列表字段
            for (TemplateField field : listFields) {
                XWPFTableRow row = table.getRow(rowIndex++);
                row.getCell(0).setText(field.getName());
                row.getCell(1).setText("列表");
                row.getCell(2).setText("{{#" + field.getName() + "}} ... {{/" + field.getName() + "}}");
                
                // 添加列表项字段
                List<TemplateField> itemFields = listItemFields.get(field.getName());
                if (itemFields != null && !itemFields.isEmpty()) {
                    for (TemplateField itemField : itemFields) {
                        if (rowIndex < table.getNumberOfRows()) {
                            row = table.getRow(rowIndex++);
                        } else {
                            row = table.createRow();
                        }
                        String itemName = itemField.getName().substring(itemField.getName().indexOf(".") + 1);
                        row.getCell(0).setText("  " + itemName);
                        row.getCell(1).setText("列表项");
                        row.getCell(2).setText("{{" + itemName + "}}");
                    }
                }
            }
            
            // 如果存在列表字段，添加说明
            if (!listFields.isEmpty()) {
                XWPFParagraph listParagraph = document.createParagraph();
                XWPFRun listRun = listParagraph.createRun();
                listRun.setText("注意：对于列表字段，需要在{{#字段名}}和{{/字段名}}之间添加要循环的内容。");
            }
            
            // 添加示例内容
            XWPFParagraph exampleParagraph = document.createParagraph();
            XWPFRun exampleRun = exampleParagraph.createRun();
            exampleRun.setText("以下是示例内容：");
            
            XWPFParagraph contentParagraph = document.createParagraph();
            XWPFRun contentRun = contentParagraph.createRun();
            
            StringBuilder exampleContent = new StringBuilder();
            exampleContent.append("尊敬的");
            
            // 添加一个客户字段示例
            boolean hasCustomerField = false;
            for (TemplateField field : objectFields) {
                if (field.getName().contains("客户") || field.getName().contains("用户") || 
                    field.getName().contains("姓名") || field.getName().contains("收件人")) {
                    exampleContent.append("{{").append(field.getName()).append("}}");
                    hasCustomerField = true;
                    break;
                }
            }
            
            if (!hasCustomerField && !objectFields.isEmpty()) {
                // 如果没有客户相关字段，使用第一个非列表字段
                exampleContent.append("{{").append(objectFields.get(0).getName()).append("}}");
            }
            
            exampleContent.append("：\n\n");
            exampleContent.append("    感谢您选择我们的服务！以下是您的信息摘要：\n\n");
            
            // 添加非列表字段示例
            for (TemplateField field : objectFields) {
                if (!field.getName().contains("客户") && !field.getName().contains("用户") && 
                    !field.getName().contains("姓名") && !field.getName().contains("收件人")) {
                    exampleContent.append("    ").append(field.getName()).append("：{{").append(field.getName()).append("}}\n");
                }
            }
            
            exampleContent.append("\n");
            
            // 添加列表字段示例
            for (TemplateField field : listFields) {
                exampleContent.append("    ").append(field.getName()).append("明细：\n");
                exampleContent.append("    {{#").append(field.getName()).append("}}\n");
                
                // 添加列表项字段
                List<TemplateField> itemFields = listItemFields.get(field.getName());
                if (itemFields != null && !itemFields.isEmpty()) {
                    for (TemplateField itemField : itemFields) {
                        String itemName = itemField.getName().substring(itemField.getName().indexOf(".") + 1);
                        exampleContent.append("        ").append(itemName).append("：{{").append(itemName).append("}}\n");
                    }
                } else {
                    exampleContent.append("        项目内容\n");
                }
                
                exampleContent.append("    {{/").append(field.getName()).append("}}\n\n");
            }
            
            exampleContent.append("    此致\n");
            exampleContent.append("    敬礼");
            
            contentRun.setText(exampleContent.toString());
            
            // 保存文档
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                document.write(out);
            }
            
            AppLogger.info("成功生成Word模板: " + filePath);
        } catch (Exception e) {
            AppLogger.error("生成Word模板失败: " + filePath, e);
            throw e;
        }
    }
} 