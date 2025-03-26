package com.timelordtty.docgen.util;

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
import com.timelordtty.AppLogger;

/**
 * 文档生成工具类
 * 根据模板和数据生成Word和Excel文档
 */
public class DocumentGenerator {

    // 变量占位符正则表达式 ${variable} 或 {{variable}}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}|\\{\\{([^}]+)\\}\\}");
    
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
        String documentType = isWord ? "Word" : "Excel";
        String traceId = AppLogger.setTrackingId();
        try {
            AppLogger.info("开始生成" + documentType + "文档，使用模板：" + templateFilePath);
            AppLogger.documentGeneration(documentType, templateFilePath, dataFilePath);
            
            // 解析数据文件
            AppLogger.info("开始解析数据文件：" + dataFilePath);
            Map<String, Object> dataMap = parseDataFile(dataFilePath);
            AppLogger.info("成功解析数据文件，共有" + dataMap.size() + "个变量");
            
            // 生成文件名
            String outputFileName = generateOutputFileName(templateFilePath, isWord);
            String outputFilePath = Paths.get(outputDirPath, outputFileName).toString();
            AppLogger.info("输出文件路径：" + outputFilePath);
            
            // 根据文档类型生成文档
            if (isWord) {
                AppLogger.info("开始生成Word文档");
                generateWordDocument(templateFilePath, outputFilePath, dataMap);
                AppLogger.info("Word文档生成成功");
            } else {
                AppLogger.info("开始生成Excel文档");
                generateExcelDocument(templateFilePath, outputFilePath, dataMap);
                AppLogger.info("Excel文档生成成功");
            }
            
            AppLogger.documentGeneration(documentType, templateFilePath, outputFilePath);
            AppLogger.info(documentType + "文档生成完成：" + outputFilePath);
            return outputFilePath;
        } catch (Exception e) {
            AppLogger.documentError(documentType, "生成文档", "文档生成过程中发生异常", e);
            throw e;
        } finally {
            AppLogger.clearTrackingId();
        }
    }
    
    /**
     * 解析数据文件
     * @param dataFilePath 数据文件路径
     * @return 数据映射表
     * @throws Exception 解析异常
     */
    private static Map<String, Object> parseDataFile(String dataFilePath) throws Exception {
        Map<String, Object> dataMap = new HashMap<>();
        
        try {
            if (dataFilePath.toLowerCase().endsWith(".json")) {
                // 解析JSON文件
                AppLogger.debug("从JSON文件加载数据");
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(new File(dataFilePath));
                
                // 将JsonNode转换为Map
                if (rootNode.isObject()) {
                    rootNode.fields().forEachRemaining(entry -> {
                        dataMap.put(entry.getKey(), parseJsonValue(entry.getValue()));
                    });
                }
                AppLogger.debug("已从JSON解析" + dataMap.size() + "个变量");
            } else if (dataFilePath.toLowerCase().endsWith(".csv")) {
                // 解析CSV文件
                AppLogger.debug("从CSV文件加载数据");
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
                AppLogger.debug("已从CSV解析" + dataMap.size() + "个变量");
            } else {
                AppLogger.warning("不支持的数据文件格式：" + dataFilePath);
                throw new IllegalArgumentException("不支持的数据文件格式：" + dataFilePath);
            }
            return dataMap;
        } catch (Exception e) {
            AppLogger.documentError("DataParser", "解析数据文件", "无法解析数据文件：" + dataFilePath, e);
            throw e;
        }
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
        
        String fileName = baseName + "_" + timestamp + extension;
        AppLogger.debug("生成的文件名：" + fileName);
        return fileName;
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
        AppLogger.debug("正在生成Word文档，替换变量...");
        int replacementCount = 0;
        
        try (FileInputStream fis = new FileInputStream(templateFilePath);
             XWPFDocument document = new XWPFDocument(fis);
             FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            
            // 替换段落中的占位符
            AppLogger.debug("处理文档段落...");
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                int count = replacePlaceholdersInParagraph(paragraph, dataMap);
                replacementCount += count;
            }
            
            // 替换表格中的占位符
            AppLogger.debug("处理文档表格...");
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            int count = replacePlaceholdersInParagraph(paragraph, dataMap);
                            replacementCount += count;
                        }
                    }
                }
            }
            
            // 保存文档
            document.write(fos);
            AppLogger.debug("完成Word文档生成，共替换" + replacementCount + "个变量");
        } catch (Exception e) {
            AppLogger.documentError("Word", "生成文档", "生成Word文档失败", e);
            throw e;
        }
    }
    
    /**
     * 替换段落中的占位符
     * @param paragraph 段落对象
     * @param dataMap 数据映射表
     * @return 替换的占位符数量
     */
    private static int replacePlaceholdersInParagraph(XWPFParagraph paragraph, Map<String, Object> dataMap) {
        int replacementCount = 0;
        String text = paragraph.getText();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        
        if (matcher.find()) {
            // 获取段落中的所有文本运行
            List<XWPFRun> runs = paragraph.getRuns();
            if (runs != null && !runs.isEmpty()) {
                // 将所有运行合并为一个运行
                String combinedText = "";
                for (int i = 0; i < runs.size(); i++) {
                    combinedText += runs.get(i).getText(0);
                    if (i > 0) {
                        runs.get(0).setText(combinedText, 0);
                        paragraph.removeRun(i);
                        i--;
                    }
                }
                
                // 对合并后的文本进行替换
                XWPFRun mainRun = runs.get(0);
                String runText = mainRun.getText(0);
                if (runText != null) {
                    matcher = PLACEHOLDER_PATTERN.matcher(runText);
                    
                    StringBuffer sb = new StringBuffer();
                    while (matcher.find()) {
                        replacementCount++;
                        // 获取占位符中的键 - 支持两种格式
                        String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                        Object value = key != null ? dataMap.get(key) : null;
                        String replacement = (value != null) ? value.toString() : "";
                        AppLogger.debug("替换变量：" + matcher.group() + " -> " + replacement);
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                    }
                    matcher.appendTail(sb);
                    
                    mainRun.setText(sb.toString(), 0);
                }
            }
        }
        
        return replacementCount;
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
        AppLogger.debug("正在生成Excel文档，替换变量...");
        int replacementCount = 0;
        
        try (FileInputStream fis = new FileInputStream(templateFilePath);
             Workbook workbook = WorkbookFactory.create(fis);
             FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            
            // 处理所有的工作表
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                AppLogger.debug("处理工作表：" + sheet.getSheetName());
                
                // 遍历所有行
                for (Row row : sheet) {
                    // 遍历所有单元格
                    for (Cell cell : row) {
                        if (cell.getCellType() == CellType.STRING) {
                            String cellValue = cell.getStringCellValue();
                            Matcher matcher = PLACEHOLDER_PATTERN.matcher(cellValue);
                            
                            if (matcher.find()) {
                                StringBuffer sb = new StringBuffer();
                                matcher.reset();
                                
                                while (matcher.find()) {
                                    replacementCount++;
                                    // 获取占位符中的键 - 支持两种格式
                                    String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                                    Object value = key != null ? dataMap.get(key) : null;
                                    
                                    String replacement = "";
                                    if (value != null) {
                                        replacement = value.toString();
                                        AppLogger.debug("替换变量：" + matcher.group() + " -> " + replacement);
                                    } else {
                                        AppLogger.debug("未找到变量：" + matcher.group());
                                    }
                                    
                                    matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                                }
                                matcher.appendTail(sb);
                                
                                cell.setCellValue(sb.toString());
                            }
                        }
                    }
                }
            }
            
            // 保存Excel文档
            workbook.write(fos);
            AppLogger.debug("完成Excel文档生成，共替换" + replacementCount + "个变量");
        } catch (Exception e) {
            AppLogger.documentError("Excel", "生成文档", "生成Excel文档失败", e);
            throw e;
        }
    }

    /**
     * 生成文档（直接指定输出文件路径）
     * @param templateFilePath 模板文件路径
     * @param dataFilePath 数据文件路径
     * @param outputFilePath 输出文件路径
     * @throws Exception 生成文档过程中的异常
     */
    public static void generateDocument(String templateFilePath, String dataFilePath, 
                                        String outputFilePath) throws Exception {
        String documentType = templateFilePath.toLowerCase().endsWith(".docx") ? "Word" : "Excel";
        boolean isWord = documentType.equals("Word");
        String traceId = AppLogger.setTrackingId();
        
        try {
            AppLogger.info("开始生成" + documentType + "文档，使用模板：" + templateFilePath);
            AppLogger.documentGeneration(documentType, templateFilePath, dataFilePath);
            
            // 解析数据文件
            AppLogger.info("开始解析数据文件：" + dataFilePath);
            Map<String, Object> dataMap = parseDataFile(dataFilePath);
            AppLogger.info("成功解析数据文件，共有" + dataMap.size() + "个变量");
            
            AppLogger.info("输出文件路径：" + outputFilePath);
            
            // 确保输出目录存在
            File outputFile = new File(outputFilePath);
            if (!outputFile.getParentFile().exists()) {
                boolean created = outputFile.getParentFile().mkdirs();
                if (created) {
                    AppLogger.info("已创建输出目录：" + outputFile.getParentFile().getAbsolutePath());
                }
            }
            
            // 根据文档类型生成文档
            if (isWord) {
                AppLogger.info("开始生成Word文档");
                generateWordDocument(templateFilePath, outputFilePath, dataMap);
                AppLogger.info("Word文档生成成功");
            } else {
                AppLogger.info("开始生成Excel文档");
                generateExcelDocument(templateFilePath, outputFilePath, dataMap);
                AppLogger.info("Excel文档生成成功");
            }
            
            AppLogger.documentGeneration(documentType, templateFilePath, outputFilePath);
            AppLogger.info(documentType + "文档生成完成：" + outputFilePath);
        } catch (Exception e) {
            AppLogger.documentError(documentType, "生成文档", "文档生成过程中发生异常", e);
            throw e;
        } finally {
            AppLogger.clearTrackingId();
        }
    }
} 