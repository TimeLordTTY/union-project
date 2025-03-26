package com.timelordtty.docgen.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.timelordtty.AppLogger;

/**
 * Excel文档模板生成器
 * 用于生成示例Excel文档模板
 */
public class ExcelTemplateGenerator {
    
    /**
     * 创建示例Excel文档模板
     * 
     * @param outputPath 输出文件路径
     * @throws IOException 如果创建文件失败
     */
    public static void createSampleTemplate(String outputPath) throws IOException {
        // 确保输出目录存在
        File outputFile = new File(outputPath);
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
        
        AppLogger.info("开始创建Excel模板: " + outputPath);
        
        // 使用XSSFWorkbook明确指定为XLSX格式
        XSSFWorkbook workbook = new XSSFWorkbook(); 
        
        try {
            // 创建标题样式
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            
            // 创建普通单元格样式
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            
            // 创建订单信息工作表
            Sheet orderSheet = workbook.createSheet("订单信息");
            
            // 设置列宽
            orderSheet.setColumnWidth(0, 4000);
            orderSheet.setColumnWidth(1, 6000);
            orderSheet.setColumnWidth(2, 4000);
            orderSheet.setColumnWidth(3, 4000);
            orderSheet.setColumnWidth(4, 4000);
            
            // 创建标题行
            Row titleRow = orderSheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("订单信息表");
            titleCell.setCellStyle(titleStyle);
            
            // 合并标题单元格
            orderSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
            
            // 创建基本信息
            Row basicInfoHeaderRow = orderSheet.createRow(1);
            basicInfoHeaderRow.setHeightInPoints(20);
            Cell basicInfoHeader = basicInfoHeaderRow.createCell(0);
            basicInfoHeader.setCellValue("基本信息");
            basicInfoHeader.setCellStyle(headerStyle);
            orderSheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));
            
            // 订单信息
            createLabelValueRow(orderSheet, 2, "订单编号:", "${order.id}", cellStyle);
            createLabelValueRow(orderSheet, 3, "订单日期:", "${today}", cellStyle);
            createLabelValueRow(orderSheet, 4, "预计送达日期:", "${order.delivery_date}", cellStyle);
            
            // 客户信息部分
            Row customerInfoHeaderRow = orderSheet.createRow(5);
            customerInfoHeaderRow.setHeightInPoints(20);
            Cell customerInfoHeader = customerInfoHeaderRow.createCell(0);
            customerInfoHeader.setCellValue("客户信息");
            customerInfoHeader.setCellStyle(headerStyle);
            orderSheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 4));
            
            createLabelValueRow(orderSheet, 6, "客户姓名:", "${client.name}", cellStyle);
            createLabelValueRow(orderSheet, 7, "联系电话:", "${client.phone}", cellStyle);
            createLabelValueRow(orderSheet, 8, "送货地址:", "${client.address}", cellStyle);
            
            // 订单明细表头
            Row detailsHeaderRow = orderSheet.createRow(10);
            detailsHeaderRow.setHeightInPoints(20);
            Cell detailsHeader = detailsHeaderRow.createCell(0);
            detailsHeader.setCellValue("订单明细");
            detailsHeader.setCellStyle(headerStyle);
            orderSheet.addMergedRegion(new CellRangeAddress(10, 10, 0, 4));
            
            // 订单明细列表头
            Row listHeaderRow = orderSheet.createRow(11);
            listHeaderRow.setHeightInPoints(20);
            
            String[] headers = {"序号", "商品名称", "数量", "单价(元)", "小计(元)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = listHeaderRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 订单明细示例行（会在运行时动态替换）
            Row itemRow1 = orderSheet.createRow(12);
            itemRow1.createCell(0).setCellValue("${order.items[0].index}");
            itemRow1.createCell(1).setCellValue("${order.items[0].name}");
            itemRow1.createCell(2).setCellValue("${order.items[0].quantity}");
            itemRow1.createCell(3).setCellValue("${order.items[0].price}");
            itemRow1.createCell(4).setCellValue("${order.items[0].subtotal}");
            
            for (int i = 0; i < 5; i++) {
                itemRow1.getCell(i).setCellStyle(cellStyle);
            }
            
            // 合计行
            Row totalRow = orderSheet.createRow(14);
            totalRow.setHeightInPoints(20);
            
            Cell totalLabelCell = totalRow.createCell(3);
            totalLabelCell.setCellValue("总金额:");
            totalLabelCell.setCellStyle(headerStyle);
            
            Cell totalValueCell = totalRow.createCell(4);
            totalValueCell.setCellValue("${order.total_amount}");
            totalValueCell.setCellStyle(cellStyle);
            
            // 备注信息
            Row notesHeaderRow = orderSheet.createRow(16);
            notesHeaderRow.setHeightInPoints(20);
            Cell notesHeader = notesHeaderRow.createCell(0);
            notesHeader.setCellValue("备注");
            notesHeader.setCellStyle(headerStyle);
            orderSheet.addMergedRegion(new CellRangeAddress(16, 16, 0, 4));
            
            Row notesRow = orderSheet.createRow(17);
            Cell notesCell = notesRow.createCell(0);
            notesCell.setCellValue("${notes}");
            notesCell.setCellStyle(cellStyle);
            orderSheet.addMergedRegion(new CellRangeAddress(17, 17, 0, 4));
            
            // 公司信息
            Row companyHeaderRow = orderSheet.createRow(19);
            companyHeaderRow.setHeightInPoints(20);
            Cell companyHeader = companyHeaderRow.createCell(0);
            companyHeader.setCellValue("公司信息");
            companyHeader.setCellStyle(headerStyle);
            orderSheet.addMergedRegion(new CellRangeAddress(19, 19, 0, 4));
            
            createLabelValueRow(orderSheet, 20, "公司名称:", "${company.name}", cellStyle);
            createLabelValueRow(orderSheet, 21, "客服电话:", "${company.service_phone}", cellStyle);
            
            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
            AppLogger.info("Excel模板创建成功: " + outputPath);
        } finally {
            workbook.close();
        }
    }
    
    /**
     * 创建标签和值的行
     */
    private static void createLabelValueRow(Sheet sheet, int rowIndex, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(20);
        
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);
        
        // 合并值单元格
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 4));
    }
    
    /**
     * 用于测试和独立运行
     * 
     * @param args 命令行参数，第一个参数为输出文件路径
     */
    public static void main(String[] args) {
        try {
            String outputPath = args.length > 0 ? args[0] : "doc-generator/src/main/resources/templates/project_status.xlsx";
            createSampleTemplate(outputPath);
            System.out.println("成功创建Excel模板: " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 