package com.timelordtty.docgen.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
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
 * Excel数据生成器工具类
 */
public class ExcelDataGenerator {
    
    /**
     * 生成Excel数据示例
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 设置基础目录
        String baseDir = System.getProperty("user.dir");
        String dataDir = baseDir + "/ProjectAssistant/templates/data";
        File dataDirFile = new File(dataDir);
        if (!dataDirFile.exists()) {
            dataDirFile.mkdirs();
        }
        
        String outputPath = dataDir + "/测试数据.xlsx";
        
        // 如果有命令行参数，则使用第一个参数作为输出路径
        if (args.length > 0) {
            outputPath = args[0];
        }
        
        try {
            // 创建Excel文档
            generateSampleData(outputPath);
            
            System.out.println("Excel测试数据已生成：" + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Excel测试数据生成失败：" + e.getMessage());
        }
    }
    
    /**
     * 生成示例数据
     * @param outputPath 输出路径
     * @throws Exception 异常
     */
    private static void generateSampleData(String outputPath) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("测试数据");
            
            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            
            // 定义表头（包含对象字段和列表字段）
            String[] headers = new String[] {
                "客户.名称", "客户.电话", "订单.编号", "订单.日期", "联系人.地址", "订单.总金额", "订单.备注", 
                "商品列表.序号", "商品列表.名称", "商品列表.单价", "商品列表.数量", "商品列表.小计"
            };
            
            // 创建表头行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 创建数据
            String[][] data = new String[][] {
                {"张三", "13800138001", "ORD2023001", "2023-01-01", "北京市海淀区", "7297.00", "请尽快发货", "1", "笔记本电脑", "6999.00", "1", "6999.00"},
                {"", "", "", "", "", "", "", "2", "电脑包", "199.00", "1", "199.00"},
                {"", "", "", "", "", "", "", "3", "鼠标", "99.00", "1", "99.00"},
                {"李四", "13800138002", "ORD2023002", "2023-01-02", "上海市浦东新区", "3297.00", "工作日送货", "1", "手机", "2999.00", "1", "2999.00"},
                {"", "", "", "", "", "", "", "2", "手机壳", "99.00", "1", "99.00"},
                {"", "", "", "", "", "", "", "3", "耳机", "199.00", "1", "199.00"},
                {"王五", "13800138003", "ORD2023003", "2023-01-03", "广州市天河区", "598.00", "周末送货", "1", "键盘", "299.00", "2", "598.00"},
                {"赵六", "13800138004", "ORD2023004", "2023-01-04", "深圳市南山区", "1397.00", "电话联系", "1", "鼠标", "99.00", "3", "297.00"},
                {"", "", "", "", "", "", "", "2", "显示器", "1100.00", "1", "1100.00"},
            };
            
            // 添加数据行
            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < data[i].length; j++) {
                    Cell cell = row.createCell(j);
                    
                    // 根据数据类型设置单元格值
                    if (j == 8 || j == 9 || j == 10 || j == 11) {
                        // 金额和数量列设置为数字
                        try {
                            double value = Double.parseDouble(data[i][j]);
                            cell.setCellValue(value);
                        } catch (NumberFormatException e) {
                            cell.setCellValue(data[i][j]);
                        }
                    } else {
                        cell.setCellValue(data[i][j]);
                    }
                }
            }
            
            // 冻结首行
            sheet.createFreezePane(0, 1);
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 保存文件
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                workbook.write(out);
            }
        } catch (Exception e) {
            AppLogger.error("生成Excel测试数据失败: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 从表头定义中提取字段
     * @param headerDefinitions 表头定义
     * @return 字段列表
     */
    private List<Field> extractFieldsFromHeaders(List<String> headerDefinitions) {
        List<Field> fields = new ArrayList<>();
        
        // 正则表达式用于匹配${字段名}格式
        Pattern pattern = Pattern.compile("\\$\\{([^{}]+)\\}");
        
        for (String header : headerDefinitions) {
            Matcher matcher = pattern.matcher(header);
            if (matcher.find()) {
                String fieldName = matcher.group(1);
                fields.add(new Field(fieldName, header));
            } else {
                // 如果没有占位符，就用原始标题作为字段名和标题
                fields.add(new Field(header, header));
            }
        }
        
        return fields;
    }

    /**
     * 创建简单的Excel数据文件作为示例
     * @param outputPath 输出路径
     */
    public static void createSampleData(String outputPath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("示例数据");
            
            // 设置列宽
            sheet.setColumnWidth(0, 20 * 256);
            sheet.setColumnWidth(1, 20 * 256);
            sheet.setColumnWidth(2, 15 * 256);
            sheet.setColumnWidth(3, 15 * 256);
            
            // 创建样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = new String[] {"客户.名称", "联系人.电话", "商品列表.名称", "商品列表.数量"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 创建数据行
            String[][] data = new String[][] {
                {"公司A", "13911112222", "产品1", "10"},
                {"", "", "产品2", "5"},
                {"公司B", "13933334444", "产品3", "20"},
                {"", "", "产品4", "15"},
                {"公司C", "13955556666", "产品5", "30"},
                {"", "", "产品6", "25"}
            };
            
            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < data[i].length; j++) {
                    row.createCell(j).setCellValue(data[i][j]);
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 保存工作簿
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
            
            System.out.println("示例数据文件创建成功: " + outputPath);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("创建示例数据文件失败: " + e.getMessage());
        }
    }

    /**
     * 字段类，用于保存字段名和表头
     */
    private static class Field {
        private String name;      // 字段名
        private String header;    // 表头
        
        public Field(String name, String header) {
            this.name = name;
            this.header = header;
        }
        
        public String getName() {
            return name;
        }
        
        public String getHeader() {
            return header;
        }
    }
} 