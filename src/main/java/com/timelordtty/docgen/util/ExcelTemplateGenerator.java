package com.timelordtty.docgen.util;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

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
import com.timelordtty.docgen.model.TemplateField;

/**
 * Excel模板生成器工具类
 */
public class ExcelTemplateGenerator {
    
    /**
     * 生成Excel模板示例
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        String outputPath = "templates/excel/订单模板.xlsx";
        
        // 如果有命令行参数，则使用第一个参数作为输出路径
        if (args.length > 0) {
            outputPath = args[0];
        }
        
        try {
            // 创建模板字段
            List<TemplateField> fields = new ArrayList<>();
            fields.add(new TemplateField("客户名称", false));
            fields.add(new TemplateField("订单编号", false));
            fields.add(new TemplateField("下单日期", false));
            fields.add(new TemplateField("联系电话", false));
            fields.add(new TemplateField("商品名称", false));
            fields.add(new TemplateField("单价", false));
            fields.add(new TemplateField("数量", false));
            fields.add(new TemplateField("总金额", false));
            
            // 创建Excel文档
            generateOrderTemplate(outputPath, fields);
            
            System.out.println("Excel模板已生成：" + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Excel模板生成失败：" + e.getMessage());
        }
    }
    
    /**
     * 生成订单模板
     * @param outputPath 输出路径
     * @param fields 字段列表
     * @throws Exception 异常
     */
    private static void generateOrderTemplate(String outputPath, List<TemplateField> fields) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("订单模板");
            
            // 创建标题样式
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            
            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            
            // 创建标题行
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("订单信息表");
            titleCell.setCellStyle(titleStyle);
            
            // 合并标题单元格
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, fields.size() - 1));
            
            // 创建表头行
            Row headerRow = sheet.createRow(1);
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue("{{" + fields.get(i).getName() + "}}");
                cell.setCellStyle(headerStyle);
            }
            
            // 添加示例数据行
            Row dataRow = sheet.createRow(2);
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = dataRow.createCell(i);
                
                // 根据字段名称生成样例数据
                String fieldName = fields.get(i).getName().toLowerCase();
                if (fieldName.contains("日期") || fieldName.contains("时间")) {
                    cell.setCellValue("2023-01-01");
                } else if (fieldName.contains("金额") || fieldName.contains("价格") || fieldName.contains("单价")) {
                    cell.setCellValue(1000.00);
                } else if (fieldName.contains("数量")) {
                    cell.setCellValue(10);
                } else if (fieldName.contains("电话") || fieldName.contains("手机")) {
                    cell.setCellValue("13800138000");
                } else if (fieldName.contains("编号") || fieldName.contains("订单")) {
                    cell.setCellValue("ORD" + System.currentTimeMillis() % 10000);
                } else if (fieldName.contains("商品") || fieldName.contains("产品")) {
                    cell.setCellValue("示例商品名称");
                } else if (fieldName.contains("客户") || fieldName.contains("姓名")) {
                    cell.setCellValue("张三");
                } else {
                    cell.setCellValue("示例数据" + (i + 1));
                }
            }
            
            // 冻结首行
            sheet.createFreezePane(0, 2);
            
            // 自动调整列宽
            for (int i = 0; i < fields.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 保存文件
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                workbook.write(out);
            }
        } catch (Exception e) {
            AppLogger.error("生成Excel订单模板失败: " + e.getMessage(), e);
            throw e;
        }
    }
} 