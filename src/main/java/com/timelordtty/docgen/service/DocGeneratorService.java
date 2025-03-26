package com.timelordtty.docgen.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.timelordtty.AppLogger;

/**
 * 文档生成服务类，处理基于Word和Excel模板生成文档的核心逻辑
 */
public class DocGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(DocGeneratorService.class);
    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";
    
    // 变量占位符正则表达式 ${variable} 或 {{variable}}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}|\\{\\{([^}]+)\\}\\}");
    
    private final ObjectMapper objectMapper;
    
    /**
     * 构造函数
     */
    public DocGeneratorService() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 生成文档的主方法
     * 
     * @param templatePath 模板文件路径
     * @param outputDir 输出目录
     * @param dataFilePath 数据文件路径
     * @param isWordTemplate 是否为Word模板
     * @return 生成的文件路径
     * @throws Exception 处理过程中的异常
     */
    public String generateDocument(String templatePath, String outputDir, String dataFilePath, boolean isWordTemplate) throws Exception {
        // 验证文件是否存在
        File templateFile = new File(templatePath);
        File dataFile = new File(dataFilePath);
        File outputDirFile = new File(outputDir);
        
        if (!templateFile.exists()) {
            throw new FileNotFoundException("模板文件不存在: " + templatePath);
        }
        
        if (!dataFile.exists()) {
            throw new FileNotFoundException("数据文件不存在: " + dataFilePath);
        }
        
        if (!outputDirFile.exists()) {
            if (!outputDirFile.mkdirs()) {
                throw new IOException("无法创建输出目录: " + outputDir);
            }
        }
        
        // 读取数据文件内容
        Map<String, Object> dataMap = readDataFile(dataFilePath);
        AppLogger.info("已读取数据: " + dataMap.size() + " 条记录");
        
        // 生成输出文件名（添加时间戳）
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Path templatePath1 = Paths.get(templatePath);
        String fileName = templatePath1.getFileName().toString();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        String outputFileName = baseName + "_" + timestamp + extension;
        String outputFilePath = Paths.get(outputDir, outputFileName).toString();
        
        // 根据文件类型调用相应的处理方法
        if (isWordTemplate) {
            generateWordDocument(templatePath, outputFilePath, dataMap);
        } else {
            generateExcelDocument(templatePath, outputFilePath, dataMap);
        }
        
        AppLogger.info("文档生成成功: " + outputFilePath);
        return outputFilePath;
    }
    
    /**
     * 读取JSON数据文件
     * 
     * @param dataFilePath 数据文件路径
     * @return 包含数据的Map对象
     * @throws IOException IO异常
     */
    private Map<String, Object> readDataFile(String dataFilePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(dataFilePath)) {
            Map<String, Object> dataMap = objectMapper.readValue(fis, new TypeReference<Map<String, Object>>() {});
            return dataMap;
        }
    }
    
    /**
     * 生成Word文档
     * 
     * @param templatePath 模板文件路径
     * @param outputPath 输出文件路径
     * @param dataMap 数据映射
     * @throws IOException IO异常
     */
    private void generateWordDocument(String templatePath, String outputPath, Map<String, Object> dataMap) throws IOException {
        try (FileInputStream fis = new FileInputStream(templatePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            // 处理文档中的段落和表格
            processParagraphs(document.getParagraphs(), dataMap);
            processTables(document.getTables(), dataMap);
            
            // 保存生成的文档
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                document.write(fos);
            }
        }
    }
    
    /**
     * 处理Word文档中的段落
     * 
     * @param paragraphs 段落列表
     * @param dataMap 数据映射
     */
    private void processParagraphs(List<XWPFParagraph> paragraphs, Map<String, Object> dataMap) {
        for (XWPFParagraph paragraph : paragraphs) {
            String paragraphText = paragraph.getText();
            
            if (paragraphText != null && paragraphText.contains(PLACEHOLDER_PREFIX)) {
                AppLogger.debug("处理包含占位符的段落: " + paragraphText);
                
                // 创建替换后的文本
                String processedText = processPlaceholders(paragraphText, dataMap);
                
                if (!processedText.equals(paragraphText)) {
                    AppLogger.debug("替换后的文本: " + processedText);
                    
                    // 清除现有的runs
                    int runsCount = paragraph.getRuns().size();
                    for (int i = runsCount - 1; i >= 0; i--) {
                        paragraph.removeRun(i);
                    }
                    
                    // 添加新的run，包含替换后的文本
                    XWPFRun newRun = paragraph.createRun();
                    newRun.setText(processedText, 0);
                    
                    // 尝试保留原格式（加粗、斜体等）
                    // 这里简化处理，实际使用可能需要更复杂的格式保留逻辑
                    try {
                        if (runsCount > 0 && paragraph.getRuns().size() > 0) {
                            XWPFRun originalRun = paragraph.getRuns().get(0);
                            newRun.setBold(originalRun.isBold());
                            newRun.setItalic(originalRun.isItalic());
                            newRun.setFontFamily(originalRun.getFontFamily());
                            if (originalRun.getFontSize() > 0) {
                                newRun.setFontSize(originalRun.getFontSize());
                            }
                        }
                    } catch (Exception e) {
                        AppLogger.warning("复制格式时出错: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * 处理Word文档中的表格
     * 
     * @param tables 表格列表
     * @param dataMap 数据映射
     */
    private void processTables(List<XWPFTable> tables, Map<String, Object> dataMap) {
        for (XWPFTable table : tables) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    processParagraphs(cell.getParagraphs(), dataMap);
                }
            }
        }
    }
    
    /**
     * 生成Excel文档
     * 
     * @param templatePath 模板文件路径
     * @param outputPath 输出文件路径
     * @param dataMap 数据映射
     * @throws IOException IO异常
     */
    private void generateExcelDocument(String templatePath, String outputPath, Map<String, Object> dataMap) throws IOException {
        try (FileInputStream fis = new FileInputStream(templatePath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            
            // 遍历工作簿中的所有工作表
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                processSheet(sheet, dataMap);
            }
            
            // 保存生成的Excel文件
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        } catch (Exception e) {
            AppLogger.error("Excel文档生成失败: " + e.getMessage(), e);
            throw new IOException("无法处理Excel文档: " + e.getMessage(), e);
        }
    }
    
    /**
     * 处理Excel工作表
     * 
     * @param sheet 工作表
     * @param dataMap 数据映射
     */
    private void processSheet(Sheet sheet, Map<String, Object> dataMap) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue();
                    
                    // 检查单元格是否包含占位符
                    if (cellValue.contains(PLACEHOLDER_PREFIX)) {
                        // 处理单元格内的所有占位符
                        String processedValue = processPlaceholders(cellValue, dataMap);
                        
                        // 更新单元格值
                        cell.setCellValue(processedValue);
                        
                        // 如果处理后的值是纯数字或日期，尝试转换单元格类型
                        try {
                            if (processedValue.matches("^\\d+$")) {
                                // 整数
                                cell.setCellValue(Long.parseLong(processedValue));
                            } else if (processedValue.matches("^\\d+\\.\\d+$")) {
                                // 小数
                                cell.setCellValue(Double.parseDouble(processedValue));
                            }
                        } catch (NumberFormatException e) {
                            // 忽略转换错误，保持为字符串
                            AppLogger.debug("无法将值转换为数字: " + processedValue);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 处理文本中的占位符，用数据映射中的值替换
     * 
     * @param text 包含占位符的文本
     * @param dataMap 数据映射
     * @return 替换后的文本
     */
    private String processPlaceholders(String text, Map<String, Object> dataMap) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            // 获取占位符中的键 - 支持两种格式
            String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            
            if (key != null) {
                // 处理嵌套路径，如 user.name
                Object value = getNestedValue(dataMap, key);
                String replacement = (value != null) ? value.toString() : "";
                AppLogger.debug("替换变量：" + matcher.group() + " -> " + replacement);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 获取嵌套数据中的值，支持点号分隔的路径，如 user.address.city
     * 
     * @param dataMap 数据映射
     * @param key 键路径
     * @return 找到的值，未找到时返回null
     */
    @SuppressWarnings("unchecked")
    private Object getNestedValue(Map<String, Object> dataMap, String key) {
        if (key == null || key.isEmpty() || dataMap == null) {
            return null;
        }
        
        // 直接获取简单键
        if (!key.contains(".")) {
            return dataMap.get(key);
        }
        
        // 处理嵌套路径
        String[] parts = key.split("\\.");
        Object current = dataMap;
        
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null; // 路径中断
            }
            
            if (current == null) {
                return null;
            }
        }
        
        return current;
    }

    /**
     * 用于测试的主方法
     * 
     * @param args 命令行参数，"word"表示测试Word文档，"excel"表示测试Excel文档
     */
    public static void main(String[] args) {
        boolean isWordTemplate = true;
        
        // 解析命令行参数
        if (args.length > 0 && "excel".equalsIgnoreCase(args[0])) {
            isWordTemplate = false;
        }
        
        try {
            // 设置测试模板和数据文件路径
            String templatePath = isWordTemplate 
                ? "doc-generator/src/main/resources/templates/project_report.docx"
                : "doc-generator/src/main/resources/templates/project_status.xlsx";
            
            String outputDir = "doc-generator/target/test-output";
            String dataFilePath = "doc-generator/src/main/resources/templates/sample_data.json";
            
            // 创建输出目录（如果不存在）
            File outputDirFile = new File(outputDir);
            if (!outputDirFile.exists()) {
                outputDirFile.mkdirs();
            }
            
            // 检查文件是否存在
            File templateFile = new File(templatePath);
            File dataFile = new File(dataFilePath);
            
            if (!templateFile.exists()) {
                System.err.println("错误: 模板文件不存在: " + templatePath);
                return;
            }
            
            if (!dataFile.exists()) {
                System.err.println("错误: 数据文件不存在: " + dataFilePath);
                return;
            }
            
            System.out.println("测试参数:");
            System.out.println("- 模板类型: " + (isWordTemplate ? "Word" : "Excel"));
            System.out.println("- 模板文件: " + templatePath);
            System.out.println("- 数据文件: " + dataFilePath);
            System.out.println("- 输出目录: " + outputDir);
            
            // 创建服务实例并调用生成方法
            DocGeneratorService service = new DocGeneratorService();
            String outputPath = service.generateDocument(templatePath, outputDir, dataFilePath, isWordTemplate);
            
            System.out.println("文档生成成功: " + outputPath);
        } catch (Exception e) {
            System.err.println("文档生成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 