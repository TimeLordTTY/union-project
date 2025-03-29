package com.timelordtty.docgen.util;

import java.io.FileOutputStream;

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
        String outputPath = "templates/excel/测试数据.xlsx";
        
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
                "客户名称", "联系电话", "订单编号", "下单日期", "收货地址", "总金额", 
                "#商品列表.序号", "#商品列表.商品名称", "#商品列表.单价", "#商品列表.数量", "#商品列表.小计"
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
                {"张三", "13800138001", "ORD2023001", "2023-01-01", "北京市海淀区", "6999.00", "1", "笔记本电脑", "6999.00", "1", "6999.00"},
                {"", "", "", "", "", "", "2", "电脑包", "199.00", "1", "199.00"},
                {"", "", "", "", "", "", "3", "鼠标", "99.00", "1", "99.00"},
                {"李四", "13800138002", "ORD2023002", "2023-01-02", "上海市浦东新区", "3297.00", "1", "手机", "2999.00", "1", "2999.00"},
                {"", "", "", "", "", "", "2", "手机壳", "99.00", "1", "99.00"},
                {"", "", "", "", "", "", "3", "耳机", "199.00", "1", "199.00"},
                {"王五", "13800138003", "ORD2023003", "2023-01-03", "广州市天河区", "598.00", "1", "键盘", "299.00", "2", "598.00"},
                {"赵六", "13800138004", "ORD2023004", "2023-01-04", "深圳市南山区", "1397.00", "1", "鼠标", "99.00", "3", "297.00"},
                {"", "", "", "", "", "", "2", "显示器", "1100.00", "1", "1100.00"},
            };
            
            // 添加数据行
            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < data[i].length; j++) {
                    Cell cell = row.createCell(j);
                    
                    // 根据数据类型设置单元格值
                    if (j == 6 || j == 8 || j == 9 || j == 10) {
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
} 