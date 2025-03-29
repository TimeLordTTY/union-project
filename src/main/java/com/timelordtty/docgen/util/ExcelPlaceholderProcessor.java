package com.timelordtty.docgen.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.timelordtty.AppLogger;

/**
 * Excel占位符处理器
 * 用于处理Excel数据文件中的占位符，以及生成示例Excel数据文件
 */
public class ExcelPlaceholderProcessor {
    
    // 变量占位符正则表达式 ${variable} 或 {{variable}}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}|\\{\\{([^}]+)\\}\\}");
    
    /**
     * 处理文本中的占位符，用数据映射中的值替换
     * 
     * @param text 包含占位符的文本
     * @param dataMap 数据映射
     * @return 替换后的文本
     */
    public static String processPlaceholders(String text, Map<String, Object> dataMap) {
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
    private static Object getNestedValue(Map<String, Object> dataMap, String key) {
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
     * 创建Excel格式的测试数据文件
     * 
     * @param outputPath 输出文件路径
     * @throws IOException 如果创建文件失败
     */
    public static void createExcelTestData(String outputPath) throws IOException {
        try {
            AppLogger.info("开始创建Excel测试数据: " + outputPath);
            
            // 确保输出目录存在
            File outputFile = new File(outputPath);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            
            // 创建Excel工作簿
            try (Workbook workbook = new XSSFWorkbook()) {
                // 创建Sheet
                Sheet sheet = workbook.createSheet("测试数据");
                
                // 设置列宽
                sheet.setColumnWidth(0, 6000);
                sheet.setColumnWidth(1, 8000);
                
                // 创建表头样式
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                
                // 创建标题行
                Row headerRow = sheet.createRow(0);
                Cell placeholderHeader = headerRow.createCell(0);
                placeholderHeader.setCellValue("占位符");
                placeholderHeader.setCellStyle(headerStyle);
                
                Cell valueHeader = headerRow.createCell(1);
                valueHeader.setCellValue("数据值");
                valueHeader.setCellStyle(headerStyle);
                
                // 添加客户数据
                addDataRow(sheet, 1, "客户.姓名", "张三");
                addDataRow(sheet, 2, "客户.电话", "13800138000");
                addDataRow(sheet, 3, "客户.地址", "北京市海淀区中关村大街1号");
                
                // 添加订单基本信息
                addDataRow(sheet, 4, "订单.编号", "ORD-20220401-001");
                addDataRow(sheet, 5, "订单.送达日期", "2022-04-08");
                addDataRow(sheet, 6, "当前日期", "2022-04-01");
                
                // 添加商品列表数据 - 第一个商品
                addDataRow(sheet, 7, "订单.商品[0].序号", "1");
                addDataRow(sheet, 8, "订单.商品[0].名称", "笔记本电脑");
                addDataRow(sheet, 9, "订单.商品[0].数量", "1");
                addDataRow(sheet, 10, "订单.商品[0].单价", "6999");
                addDataRow(sheet, 11, "订单.商品[0].小计", "6999");
                
                // 添加商品列表数据 - 第二个商品
                addDataRow(sheet, 12, "订单.商品[1].序号", "2");
                addDataRow(sheet, 13, "订单.商品[1].名称", "无线鼠标");
                addDataRow(sheet, 14, "订单.商品[1].数量", "2");
                addDataRow(sheet, 15, "订单.商品[1].单价", "99");
                addDataRow(sheet, 16, "订单.商品[1].小计", "198");
                
                // 添加商品列表数据 - 第三个商品
                addDataRow(sheet, 17, "订单.商品[2].序号", "3");
                addDataRow(sheet, 18, "订单.商品[2].名称", "显示器");
                addDataRow(sheet, 19, "订单.商品[2].数量", "1");
                addDataRow(sheet, 20, "订单.商品[2].单价", "1299");
                addDataRow(sheet, 21, "订单.商品[2].小计", "1299");
                
                // 添加总金额和其他信息
                addDataRow(sheet, 22, "订单.总金额", "8496");
                addDataRow(sheet, 23, "公司.名称", "优秀科技有限公司");
                addDataRow(sheet, 24, "公司.客服电话", "400-800-8888");
                addDataRow(sheet, 25, "备注", "如有特殊需求，请提前与客服联系。");
                
                // 写入文件
                try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                    workbook.write(fileOut);
                }
                
                AppLogger.info("Excel测试数据创建成功: " + outputPath);
            }
        } catch (IOException e) {
            AppLogger.error("创建Excel测试数据失败: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 向Excel表格添加一行数据
     * 
     * @param sheet 工作表
     * @param rowIndex 行索引
     * @param placeholder 占位符
     * @param value 值
     */
    private static void addDataRow(Sheet sheet, int rowIndex, String placeholder, String value) {
        Row row = sheet.createRow(rowIndex);
        Cell placeholderCell = row.createCell(0);
        placeholderCell.setCellValue(placeholder);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
    }
    
    /**
     * 用于测试和独立运行
     * 
     * @param args 命令行参数，第一个参数为输出文件路径
     */
    public static void main(String[] args) {
        try {
            String outputPath = args.length > 0 ? args[0] : "templates/excel/订单数据.xlsx";
            createExcelTestData(outputPath);
            System.out.println("成功创建Excel测试数据: " + outputPath);
        } catch (Exception e) {
            AppLogger.error("创建Excel测试数据时发生错误", e);
            e.printStackTrace();
        }
    }
} 