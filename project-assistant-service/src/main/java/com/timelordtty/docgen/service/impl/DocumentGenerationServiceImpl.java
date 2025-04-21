package com.timelordtty.docgen.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

import com.timelordtty.docgen.model.DocumentGenerationResult;
import com.timelordtty.docgen.service.DocumentGenerationService;

import lombok.extern.slf4j.Slf4j;

/**
 * 文档生成服务实现类
 */
@Slf4j
@Service
public class DocumentGenerationServiceImpl implements DocumentGenerationService {

    // 占位符正则表达式
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");
    // 列表占位符开始正则表达式
    private static final Pattern LIST_START_PATTERN = Pattern.compile("\\{\\{#(.*?)\\}\\}");
    // 列表占位符结束正则表达式
    private static final Pattern LIST_END_PATTERN = Pattern.compile("\\{\\{/(.*?)\\}\\}");

    @Override
    public DocumentGenerationResult generateFromTemplate(String templatePath, String outputPath, Map<String, Object> data) {
        return generateFromTemplate(new File(templatePath), new File(outputPath), data);
    }

    @Override
    public DocumentGenerationResult generateFromTemplate(File templateFile, File outputFile, Map<String, Object> data) {
        long startTime = System.currentTimeMillis();
        
        try {
            if (!templateFile.exists()) {
                return DocumentGenerationResult.error("模板文件不存在: " + templateFile.getAbsolutePath());
            }
            
            // 确保输出目录存在
            Path outputDir = Paths.get(outputFile.getParent());
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            
            String fileName = templateFile.getName().toLowerCase();
            
            if (fileName.endsWith(".docx")) {
                return generateWordDocument(templateFile, outputFile, data, startTime);
            } else if (fileName.endsWith(".xlsx")) {
                return generateExcelDocument(templateFile, outputFile, data, startTime);
            } else {
                return DocumentGenerationResult.error("不支持的文件类型: " + fileName);
            }
        } catch (Exception e) {
            log.error("文档生成失败", e);
            return DocumentGenerationResult.error("文档生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成Word文档
     */
    private DocumentGenerationResult generateWordDocument(File templateFile, File outputFile, Map<String, Object> data, long startTime) {
        try (InputStream is = new FileInputStream(templateFile);
             XWPFDocument document = new XWPFDocument(is)) {
            
            // 处理段落
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                processWordParagraph(paragraph, data);
            }
            
            // 处理表格
            for (XWPFTable table : document.getTables()) {
                processWordTable(table, data);
            }
            
            // 保存文档
            try (OutputStream os = new FileOutputStream(outputFile)) {
                document.write(os);
            }
            
            long endTime = System.currentTimeMillis();
            return DocumentGenerationResult.success(outputFile.getAbsolutePath(), endTime - startTime);
            
        } catch (Exception e) {
            log.error("Word文档生成失败", e);
            return DocumentGenerationResult.error("Word文档生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理Word段落
     */
    private void processWordParagraph(XWPFParagraph paragraph, Map<String, Object> data) {
        String text = paragraph.getText();
        
        // 检查是否包含普通占位符
        if (text.contains("{{") && text.contains("}}")) {
            List<XWPFRun> runs = paragraph.getRuns();
            
            if (runs != null && !runs.isEmpty()) {
                // 合并所有runs的文本
                StringBuilder sb = new StringBuilder();
                for (XWPFRun run : runs) {
                    sb.append(run.getText(0));
                }
                
                String combinedText = sb.toString();
                
                // 替换占位符
                String replacedText = replacePlaceholders(combinedText, data);
                
                // 清除所有runs
                for (int i = runs.size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }
                
                // 添加新的run
                XWPFRun newRun = paragraph.createRun();
                newRun.setText(replacedText);
            }
        }
    }
    
    /**
     * 处理Word表格
     */
    private void processWordTable(XWPFTable table, Map<String, Object> data) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    processWordParagraph(paragraph, data);
                }
            }
        }
        
        // 处理表格中的循环数据
        for (int i = 0; i < table.getNumberOfRows(); i++) {
            XWPFTableRow row = table.getRow(i);
            boolean isListRow = false;
            String listField = null;
            
            // 检查是否是列表行
            for (XWPFTableCell cell : row.getTableCells()) {
                String text = cell.getText();
                Matcher startMatcher = LIST_START_PATTERN.matcher(text);
                
                if (startMatcher.find()) {
                    isListRow = true;
                    listField = startMatcher.group(1).trim();
                    break;
                }
            }
            
            if (isListRow && listField != null) {
                // 获取列表数据
                Object listObj = data.get(listField);
                if (listObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> listData = (List<Map<String, Object>>) listObj;
                    
                    if (listData != null && !listData.isEmpty()) {
                        XWPFTableRow templateRow = table.getRow(i);
                        
                        // 处理每个列表项
                        for (int j = 0; j < listData.size(); j++) {
                            Map<String, Object> itemData = listData.get(j);
                            
                            // 第一项直接使用模板行
                            XWPFTableRow newRow = (j == 0) ? templateRow : table.insertNewTableRow(i + j);
                            
                            // 复制单元格
                            if (j > 0) {
                                for (int k = 0; k < templateRow.getTableCells().size(); k++) {
                                    XWPFTableCell oldCell = templateRow.getTableCells().get(k);
                                    XWPFTableCell newCell = newRow.createCell();
                                    
                                    // 复制段落
                                    for (XWPFParagraph oldPara : oldCell.getParagraphs()) {
                                        XWPFParagraph newPara = newCell.addParagraph();
                                        
                                        for (XWPFRun oldRun : oldPara.getRuns()) {
                                            XWPFRun newRun = newPara.createRun();
                                            String oldText = oldRun.getText(0);
                                            
                                            if (oldText != null) {
                                                // 替换列表项中的占位符
                                                String newText = replacePlaceholders(oldText, itemData);
                                                newRun.setText(newText);
                                            }
                                        }
                                    }
                                }
                            } else {
                                // 处理第一项
                                for (XWPFTableCell cell : newRow.getTableCells()) {
                                    for (XWPFParagraph para : cell.getParagraphs()) {
                                        List<XWPFRun> runs = para.getRuns();
                                        
                                        if (runs != null && !runs.isEmpty()) {
                                            for (int k = 0; k < runs.size(); k++) {
                                                XWPFRun run = runs.get(k);
                                                String oldText = run.getText(0);
                                                
                                                if (oldText != null) {
                                                    // 替换列表标记和占位符
                                                    String newText = oldText.replaceAll("\\{\\{#" + listField + "\\}\\}", "")
                                                                    .replaceAll("\\{\\{/" + listField + "\\}\\}", "");
                                                    
                                                    newText = replacePlaceholders(newText, itemData);
                                                    run.setText(newText);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // 跳过已处理的行
                        i += listData.size() - 1;
                    }
                }
            }
        }
    }
    
    /**
     * 生成Excel文档
     */
    private DocumentGenerationResult generateExcelDocument(File templateFile, File outputFile, Map<String, Object> data, long startTime) {
        try (InputStream is = new FileInputStream(templateFile);
             Workbook workbook = WorkbookFactory.create(is)) {
            
            // 处理每个工作表
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                
                // 处理普通单元格
                processExcelSheet(sheet, data);
                
                // 处理列表数据
                processExcelListData(sheet, data);
            }
            
            // 保存工作簿
            try (OutputStream os = new FileOutputStream(outputFile)) {
                workbook.write(os);
            }
            
            long endTime = System.currentTimeMillis();
            return DocumentGenerationResult.success(outputFile.getAbsolutePath(), endTime - startTime);
            
        } catch (Exception e) {
            log.error("Excel文档生成失败", e);
            return DocumentGenerationResult.error("Excel文档生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理Excel工作表
     */
    private void processExcelSheet(Sheet sheet, Map<String, Object> data) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                    String cellValue = cell.getStringCellValue();
                    
                    if (cellValue.contains("{{") && cellValue.contains("}}")) {
                        String newValue = replacePlaceholders(cellValue, data);
                        cell.setCellValue(newValue);
                    }
                }
            }
        }
    }
    
    /**
     * 处理Excel列表数据
     */
    private void processExcelListData(Sheet sheet, Map<String, Object> data) {
        Map<Integer, List<Integer>> listRows = new HashMap<>();
        Map<Integer, String> listFields = new HashMap<>();
        
        // 查找列表开始行
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                    String cellValue = cell.getStringCellValue();
                    Matcher startMatcher = LIST_START_PATTERN.matcher(cellValue);
                    
                    if (startMatcher.find()) {
                        int startRow = row.getRowNum();
                        String listField = startMatcher.group(1).trim();
                        listFields.put(startRow, listField);
                        
                        // 查找结束行
                        for (int i = startRow + 1; i <= sheet.getLastRowNum(); i++) {
                            Row endRow = sheet.getRow(i);
                            if (endRow != null) {
                                for (Cell endCell : endRow) {
                                    if (endCell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                                        String endCellValue = endCell.getStringCellValue();
                                        Matcher endMatcher = LIST_END_PATTERN.matcher(endCellValue);
                                        
                                        if (endMatcher.find() && endMatcher.group(1).trim().equals(listField)) {
                                            // 记录列表范围
                                            List<Integer> range = new ArrayList<>();
                                            range.add(startRow);
                                            range.add(i);
                                            listRows.put(startRow, range);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 处理每个列表
        for (Integer startRow : listRows.keySet()) {
            List<Integer> range = listRows.get(startRow);
            String listField = listFields.get(startRow);
            
            if (range.size() == 2) {
                int endRow = range.get(1);
                
                // 获取列表数据
                Object listObj = data.get(listField);
                if (listObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> listData = (List<Map<String, Object>>) listObj;
                    
                    if (listData != null && !listData.isEmpty()) {
                        int templateRowCount = endRow - startRow - 1;
                        
                        // 处理每个列表项
                        for (int i = 0; i < listData.size(); i++) {
                            Map<String, Object> itemData = listData.get(i);
                            
                            // 计算目标行
                            int targetStartRow = startRow + 1 + i * templateRowCount;
                            
                            // 复制模板行
                            if (i > 0) {
                                // 插入新行
                                sheet.shiftRows(targetStartRow, sheet.getLastRowNum(), templateRowCount);
                                
                                // 复制模板行
                                for (int j = 0; j < templateRowCount; j++) {
                                    Row srcRow = sheet.getRow(startRow + 1 + j);
                                    Row destRow = sheet.createRow(targetStartRow + j);
                                    
                                    // 复制单元格样式和内容
                                    for (Cell srcCell : srcRow) {
                                        Cell destCell = destRow.createCell(srcCell.getColumnIndex());
                                        
                                        // 复制单元格样式
                                        CellStyle newStyle = sheet.getWorkbook().createCellStyle();
                                        newStyle.cloneStyleFrom(srcCell.getCellStyle());
                                        destCell.setCellStyle(newStyle);
                                        
                                        // 复制单元格内容
                                        switch (srcCell.getCellType()) {
                                            case STRING:
                                                String srcValue = srcCell.getStringCellValue();
                                                
                                                // 替换模板标记和占位符
                                                String newValue = srcValue.replaceAll("\\{\\{#" + listField + "\\}\\}", "")
                                                                .replaceAll("\\{\\{/" + listField + "\\}\\}", "");
                                                
                                                newValue = replacePlaceholders(newValue, itemData);
                                                destCell.setCellValue(newValue);
                                                break;
                                            case NUMERIC:
                                                destCell.setCellValue(srcCell.getNumericCellValue());
                                                break;
                                            case BOOLEAN:
                                                destCell.setCellValue(srcCell.getBooleanCellValue());
                                                break;
                                            case FORMULA:
                                                destCell.setCellFormula(srcCell.getCellFormula());
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                }
                            } else {
                                // 处理第一项（直接在原始行上修改）
                                for (int j = 0; j < templateRowCount; j++) {
                                    Row row = sheet.getRow(startRow + 1 + j);
                                    
                                    if (row != null) {
                                        for (Cell cell : row) {
                                            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                                                String cellValue = cell.getStringCellValue();
                                                
                                                // 替换模板标记和占位符
                                                String newValue = cellValue.replaceAll("\\{\\{#" + listField + "\\}\\}", "")
                                                                .replaceAll("\\{\\{/" + listField + "\\}\\}", "");
                                                
                                                newValue = replacePlaceholders(newValue, itemData);
                                                cell.setCellValue(newValue);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // 清除列表结束标记行
                        Row endTagRow = sheet.getRow(endRow);
                        if (endTagRow != null) {
                            for (Cell cell : endTagRow) {
                                if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                                    String cellValue = cell.getStringCellValue();
                                    
                                    if (cellValue.contains("{{/" + listField + "}}")) {
                                        cell.setCellValue("");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 替换字符串中的占位符
     */
    private String replacePlaceholders(String text, Map<String, Object> data) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1).trim();
            Object value = data.get(placeholder);
            String replacement = (value != null) ? value.toString() : "";
            
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 创建新模板
     * 
     * @param templateType 模板类型 (WORD/EXCEL)
     * @param fields 字段列表
     * @param outputPath 输出路径
     * @return 模板生成结果
     */
    public DocumentGenerationResult createTemplate(String templateType, List<String> fields, String outputPath) {
        long startTime = System.currentTimeMillis();
        
        try {
            File outputFile = new File(outputPath);
            
            // 确保输出目录存在
            Path outputDir = Paths.get(outputFile.getParent());
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            
            if ("WORD".equalsIgnoreCase(templateType)) {
                return createWordTemplate(fields, outputFile, startTime);
            } else if ("EXCEL".equalsIgnoreCase(templateType)) {
                return createExcelTemplate(fields, outputFile, startTime);
            } else {
                return DocumentGenerationResult.error("不支持的模板类型: " + templateType);
            }
        } catch (Exception e) {
            log.error("模板创建失败", e);
            return DocumentGenerationResult.error("模板创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建Word模板
     */
    private DocumentGenerationResult createWordTemplate(List<String> fields, File outputFile, long startTime) {
        try (XWPFDocument document = new XWPFDocument()) {
            
            // 创建标题
            XWPFParagraph titlePara = document.createParagraph();
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText("文档模板");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            
            // 创建空行
            document.createParagraph();
            
            // 创建字段表格
            XWPFTable table = document.createTable(fields.size() + 1, 2);
            
            // 设置表头
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.getCell(0).setText("字段名");
            headerRow.getCell(1).setText("值");
            
            // 添加字段
            for (int i = 0; i < fields.size(); i++) {
                XWPFTableRow row = table.getRow(i + 1);
                row.getCell(0).setText(fields.get(i));
                row.getCell(1).setText("{{" + fields.get(i) + "}}");
            }
            
            // 保存文档
            try (FileOutputStream os = new FileOutputStream(outputFile)) {
                document.write(os);
            }
            
            long endTime = System.currentTimeMillis();
            return DocumentGenerationResult.success(outputFile.getAbsolutePath(), endTime - startTime);
            
        } catch (Exception e) {
            log.error("Word模板创建失败", e);
            return DocumentGenerationResult.error("Word模板创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建Excel模板
     */
    private DocumentGenerationResult createExcelTemplate(List<String> fields, File outputFile, long startTime) {
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // 创建工作表
            Sheet sheet = workbook.createSheet("数据模板");
            
            // 创建表头行
            Row headerRow = sheet.createRow(0);
            
            // 添加字段
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(fields.get(i));
                
                // 设置列宽
                sheet.setColumnWidth(i, 15 * 256);
            }
            
            // 创建数据示例行
            Row dataRow = sheet.createRow(1);
            
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = dataRow.createCell(i);
                cell.setCellValue("{{" + fields.get(i) + "}}");
            }
            
            // 冻结首行
            sheet.createFreezePane(0, 1);
            
            // 保存工作簿
            try (FileOutputStream os = new FileOutputStream(outputFile)) {
                workbook.write(os);
            }
            
            long endTime = System.currentTimeMillis();
            return DocumentGenerationResult.success(outputFile.getAbsolutePath(), endTime - startTime);
            
        } catch (Exception e) {
            log.error("Excel模板创建失败", e);
            return DocumentGenerationResult.error("Excel模板创建失败: " + e.getMessage());
        }
    }
} 