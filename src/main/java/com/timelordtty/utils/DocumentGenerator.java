package com.timelordtty.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 文档生成工具类
 * 根据模板和数据生成Word和Excel文档
 */
public class DocumentGenerator {

    // 变量占位符正则表达式 ${variable}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    /**
     * 生成文档
     * @param templateFilePath 模板文件路径
     * @param dataFilePath 数据文件路径
     * @param outputDirPath 输出目录路径
     * @param isWord 是否是Word文档
     * @return 生成的文档路径
     * @throws Exception 生成文档过程中的异常
     */
    public static String generateDocument(String templateFilePath, String dataFilePath, 
                                          String outputDirPath, boolean isWord) throws Exception {
        // 解析数据文件
        Map<String, Object> dataMap = parseDataFile(dataFilePath);
        
        // 生成文件名
        String outputFileName = generateOutputFileName(templateFilePath, isWord);
        String outputFilePath = Paths.get(outputDirPath, outputFileName).toString();
        
        // 根据文档类型生成文档
        if (isWord) {
            generateWordDocument(templateFilePath, outputFilePath, dataMap);
        } else {
            generateExcelDocument(templateFilePath, outputFilePath, dataMap);
        }
        
        return outputFilePath;
    }
    
    /**
     * 解析数据文件
     * @param dataFilePath 数据文件路径
     * @return 数据映射表
     * @throws Exception 解析异常
     */
    private static Map<String, Object> parseDataFile(String dataFilePath) throws Exception {
        Map<String, Object> dataMap = new HashMap<>();
        
        if (dataFilePath.toLowerCase().endsWith(".json")) {
            // 解析JSON文件
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(dataFilePath));
            
            // 将JsonNode转换为Map
            if (rootNode.isObject()) {
                rootNode.fields().forEachRemaining(entry -> {
                    dataMap.put(entry.getKey(), parseJsonValue(entry.getValue()));
                });
            }
        } else if (dataFilePath.toLowerCase().endsWith(".csv")) {
            // 解析CSV文件
            List<String> lines = Files.readAllLines(Paths.get(dataFilePath));
            if (!lines.isEmpty()) {
                String[] headers = lines.get(0).split(",");
                
                if (lines.size() > 1) {
                    String[] values = lines.get(1).split(",");
                    for (int i = 0; i < Math.min(headers.length, values.length); i++) {
                        dataMap.put(headers[i].trim(), values[i].trim());
                    }
                }
            }
        } else if (dataFilePath.toLowerCase().endsWith(".xlsx") || dataFilePath.toLowerCase().endsWith(".xls")) {
            // 解析Excel文件
            try (Workbook workbook = WorkbookFactory.create(new File(dataFilePath))) {
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);
                Row dataRow = sheet.getRow(1);
                
                if (headerRow != null && dataRow != null) {
                    for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                        Cell headerCell = headerRow.getCell(i);
                        Cell dataCell = dataRow.getCell(i);
                        
                        if (headerCell != null && dataCell != null) {
                            String key = headerCell.toString().trim();
                            String value = dataCell.toString().trim();
                            dataMap.put(key, value);
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("不支持的数据文件格式");
        }
        
        return dataMap;
    }
    
    /**
     * 解析JSON值
     * @param node JSON节点
     * @return 解析后的对象
     */
    private static Object parseJsonValue(JsonNode node) {
        if (node.isTextual()) {
            return node.asText();
        } else if (node.isNumber()) {
            return node.isInt() ? node.asInt() : node.asDouble();
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            node.forEach(item -> list.add(parseJsonValue(item)));
            return list;
        } else if (node.isObject()) {
            Map<String, Object> map = new HashMap<>();
            node.fields().forEachRemaining(entry -> {
                map.put(entry.getKey(), parseJsonValue(entry.getValue()));
            });
            return map;
        }
        return null;
    }
    
    /**
     * 生成输出文件名
     * @param templateFilePath 模板文件路径
     * @param isWord 是否是Word文档
     * @return 生成的文件名
     */
    private static String generateOutputFileName(String templateFilePath, boolean isWord) {
        String baseName = new File(templateFilePath).getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = isWord ? ".docx" : ".xlsx";
        
        return baseName + "_" + timestamp + extension;
    }
    
    /**
     * 生成Word文档
     * @param templateFilePath 模板文件路径
     * @param outputFilePath 输出文件路径
     * @param dataMap 数据映射表
     * @throws Exception 生成异常
     */
    private static void generateWordDocument(String templateFilePath, String outputFilePath, 
                                             Map<String, Object> dataMap) throws Exception {
        try (FileInputStream fis = new FileInputStream(templateFilePath);
             XWPFDocument document = new XWPFDocument(fis);
             FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            
            // 替换段落中的占位符
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replacePlaceholdersInParagraph(paragraph, dataMap);
            }
            
            // 替换表格中的占位符
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            replacePlaceholdersInParagraph(paragraph, dataMap);
                        }
                    }
                }
            }
            
            // 保存文档
            document.write(fos);
        }
    }
    
    /**
     * 替换段落中的占位符
     * @param paragraph 段落对象
     * @param dataMap 数据映射表
     */
    private static void replacePlaceholdersInParagraph(XWPFParagraph paragraph, Map<String, Object> dataMap) {
        String text = paragraph.getText();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        
        // 如果段落中没有占位符，直接返回
        if (!matcher.find()) {
            return;
        }
        
        // 重置匹配器，从头开始匹配
        matcher.reset();
        
        // 获取段落中的所有文本运行
        List<XWPFRun> runs = paragraph.getRuns();
        
        // 如果没有任何文本运行，直接返回
        if (runs.isEmpty()) {
            return;
        }
        
        // 合并所有文本运行的内容
        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            fullText.append(run.getText(0));
        }
        
        // 清空第一个文本运行之外的所有内容
        for (int i = 1; i < runs.size(); i++) {
            runs.get(i).setText("", 0);
        }
        
        // 替换占位符
        String replacedText = fullText.toString();
        matcher = PLACEHOLDER_PATTERN.matcher(replacedText);
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = dataMap.get(key);
            String replacement = value != null ? value.toString() : "";
            replacedText = replacedText.replace(matcher.group(), replacement);
        }
        
        // 将替换后的文本设置到第一个文本运行
        runs.get(0).setText(replacedText, 0);
    }
    
    /**
     * 生成Excel文档
     * @param templateFilePath 模板文件路径
     * @param outputFilePath 输出文件路径
     * @param dataMap 数据映射表
     * @throws Exception 生成异常
     */
    private static void generateExcelDocument(String templateFilePath, String outputFilePath, 
                                              Map<String, Object> dataMap) throws Exception {
        try (FileInputStream fis = new FileInputStream(templateFilePath);
             Workbook workbook = WorkbookFactory.create(fis);
             FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            
            // 处理所有Sheet
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                processSheet(sheet, dataMap);
            }
            
            // 保存文档
            workbook.write(fos);
        }
    }
    
    /**
     * 处理Excel工作表
     * @param sheet 工作表对象
     * @param dataMap 数据映射表
     */
    private static void processSheet(Sheet sheet, Map<String, Object> dataMap) {
        // 遍历所有行
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            
            // 遍历行中的所有单元格
            for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                Cell cell = row.getCell(colIndex);
                if (cell == null) {
                    continue;
                }
                
                // 只处理文本类型单元格
                if (cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue();
                    Matcher matcher = PLACEHOLDER_PATTERN.matcher(cellValue);
                    
                    if (matcher.find()) {
                        // 替换占位符
                        String newValue = cellValue;
                        matcher.reset();
                        
                        while (matcher.find()) {
                            String key = matcher.group(1);
                            Object value = dataMap.get(key);
                            String replacement = value != null ? value.toString() : "";
                            newValue = newValue.replace(matcher.group(), replacement);
                        }
                        
                        // 设置新值
                        cell.setCellValue(newValue);
                    }
                }
            }
        }
    }
} 