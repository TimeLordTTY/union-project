package com.timelordtty.docgen.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.BorderStyle;
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
 * Excel测试数据生成器
 * 用于创建与模板对应的Excel格式测试数据
 */
public class ExcelDataGenerator {

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
                
                // 设置边框
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);
                
                // 创建数据单元格样式
                CellStyle dataStyle = workbook.createCellStyle();
                dataStyle.setBorderBottom(BorderStyle.THIN);
                dataStyle.setBorderLeft(BorderStyle.THIN);
                dataStyle.setBorderRight(BorderStyle.THIN);
                dataStyle.setBorderTop(BorderStyle.THIN);
                
                // 创建标题行
                Row headerRow = sheet.createRow(0);
                Cell placeholderHeader = headerRow.createCell(0);
                placeholderHeader.setCellValue("占位符");
                placeholderHeader.setCellStyle(headerStyle);
                
                Cell valueHeader = headerRow.createCell(1);
                valueHeader.setCellValue("数据值");
                valueHeader.setCellStyle(headerStyle);
                
                // 添加客户数据
                addDataRow(sheet, 1, "${客户姓名}", "张三", dataStyle);
                addDataRow(sheet, 2, "${客户电话}", "13800138000", dataStyle);
                addDataRow(sheet, 3, "${客户地址}", "北京市海淀区中关村大街1号", dataStyle);
                
                // 添加订单基本信息
                addDataRow(sheet, 4, "${订单编号}", "ORD-20220401-001", dataStyle);
                addDataRow(sheet, 5, "${订单送达日期}", "2022-04-08", dataStyle);
                addDataRow(sheet, 6, "${当前日期}", "2022-04-01", dataStyle);
                
                // 添加列表数据 - 第一个商品
                addDataRow(sheet, 7, "${商品列表.序号}", "1", dataStyle);
                addDataRow(sheet, 8, "${商品列表.名称}", "笔记本电脑", dataStyle);
                addDataRow(sheet, 9, "${商品列表.数量}", "1", dataStyle);
                addDataRow(sheet, 10, "${商品列表.单价}", "6999", dataStyle);
                addDataRow(sheet, 11, "${商品列表.小计}", "6999", dataStyle);
                
                // 添加列表数据 - 第二个商品
                addDataRow(sheet, 12, "${商品列表.序号}", "2", dataStyle);
                addDataRow(sheet, 13, "${商品列表.名称}", "无线鼠标", dataStyle);
                addDataRow(sheet, 14, "${商品列表.数量}", "2", dataStyle);
                addDataRow(sheet, 15, "${商品列表.单价}", "99", dataStyle);
                addDataRow(sheet, 16, "${商品列表.小计}", "198", dataStyle);
                
                // 添加列表数据 - 第三个商品
                addDataRow(sheet, 17, "${商品列表.序号}", "3", dataStyle);
                addDataRow(sheet, 18, "${商品列表.名称}", "显示器", dataStyle);
                addDataRow(sheet, 19, "${商品列表.数量}", "1", dataStyle);
                addDataRow(sheet, 20, "${商品列表.单价}", "1299", dataStyle);
                addDataRow(sheet, 21, "${商品列表.小计}", "1299", dataStyle);
                
                // 添加成交列表数据 - 第一个成交
                addDataRow(sheet, 22, "${成交列表.序号}", "1", dataStyle);
                addDataRow(sheet, 23, "${成交列表.合约}", "A001", dataStyle);
                addDataRow(sheet, 24, "${成交列表.金额}", "10000", dataStyle);
                addDataRow(sheet, 25, "${成交列表.日期}", "2022-03-15", dataStyle);
                
                // 添加成交列表数据 - 第二个成交
                addDataRow(sheet, 26, "${成交列表.序号}", "2", dataStyle);
                addDataRow(sheet, 27, "${成交列表.合约}", "B002", dataStyle);
                addDataRow(sheet, 28, "${成交列表.金额}", "5000", dataStyle);
                addDataRow(sheet, 29, "${成交列表.日期}", "2022-03-20", dataStyle);
                
                // 添加总金额和其他信息
                addDataRow(sheet, 30, "${订单总金额}", "8496", dataStyle);
                addDataRow(sheet, 31, "${公司名称}", "优秀科技有限公司", dataStyle);
                addDataRow(sheet, 32, "${公司客服电话}", "400-800-8888", dataStyle);
                addDataRow(sheet, 33, "${备注}", "如有特殊需求，请提前与客服联系。", dataStyle);
                
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
     * @param style 单元格样式
     */
    private static void addDataRow(Sheet sheet, int rowIndex, String placeholder, String value, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        Cell placeholderCell = row.createCell(0);
        placeholderCell.setCellValue(placeholder);
        placeholderCell.setCellStyle(style);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);
    }
    
    /**
     * 用于测试和独立运行
     * 
     * @param args 命令行参数，第一个参数为输出文件路径
     */
    public static void main(String[] args) {
        try {
            String outputPath = args.length > 0 ? args[0] : "templates/excel/测试数据.xlsx";
            createExcelTestData(outputPath);
            System.out.println("成功创建Excel测试数据: " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 