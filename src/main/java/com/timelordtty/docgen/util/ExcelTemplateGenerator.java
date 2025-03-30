package com.timelordtty.docgen.util;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.timelordtty.AppLogger;
import com.timelordtty.docgen.model.TemplateField;

/**
 * Excel模板生成器工具类
 */
public class ExcelTemplateGenerator {
    
    private static final Color PRIMARY_COLOR = new Color(233, 30, 99); // #E91E63 粉色
    private static final Color SECONDARY_COLOR = new Color(255, 208, 224); // #FFD0E0 浅粉色
    private static final Color BORDER_COLOR = new Color(255, 182, 193); // #FFB6C1 淡粉色
    
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
            List<TemplateField> objectFields = new ArrayList<>();
            objectFields.add(new TemplateField("客户.名称", false));
            objectFields.add(new TemplateField("客户.电话", false));
            objectFields.add(new TemplateField("订单.编号", false));
            objectFields.add(new TemplateField("订单.日期", false));
            objectFields.add(new TemplateField("联系人.地址", false));
            objectFields.add(new TemplateField("订单.总金额", false));
            objectFields.add(new TemplateField("订单.备注", false));
            
            List<TemplateField> listFields = new ArrayList<>();
            TemplateField productListField = new TemplateField("商品列表", true);
            productListField.addListItem("序号");
            productListField.addListItem("名称");
            productListField.addListItem("单价");
            productListField.addListItem("数量");
            productListField.addListItem("小计");
            listFields.add(productListField);
            
            // 创建Excel文档
            generateBeautifulOrderTemplate(outputPath, objectFields, listFields);
            
            System.out.println("Excel模板已成功生成：" + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Excel模板生成失败：" + e.getMessage());
        }
    }
    
    /**
     * 生成美观的订单模板
     * @param outputPath 输出路径
     * @param objectFields 对象字段列表
     * @param listFields 列表字段列表
     * @throws Exception 异常
     */
    private static void generateBeautifulOrderTemplate(String outputPath, 
                                                    List<TemplateField> objectFields, 
                                                    List<TemplateField> listFields) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // 创建样式
            CellStyle[] styles = createStyles(workbook);
            
            // 创建工作表
            Sheet sheet = workbook.createSheet("订单模板");
            
            // 设置列宽
            sheet.setColumnWidth(0, 20 * 256);
            sheet.setColumnWidth(1, 20 * 256);
            sheet.setColumnWidth(2, 20 * 256);
            sheet.setColumnWidth(3, 20 * 256);
            sheet.setColumnWidth(4, 20 * 256);
            sheet.setColumnWidth(5, 20 * 256);
            
            int rowNum = 0;
            
            // 1. 创建标题行
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("✨ 专业订单信息 ✨");
            titleCell.setCellStyle(styles[0]); // 标题样式
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            
            // 空行
            sheet.createRow(rowNum++);
            
            // 2. 客户信息部分
            Row customerTitle = sheet.createRow(rowNum++);
            Cell customerTitleCell = customerTitle.createCell(0);
            customerTitleCell.setCellValue("📋 客户信息");
            customerTitleCell.setCellStyle(styles[1]); // 小标题样式
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));
            
            // 客户信息表格
            Row customerRow1 = sheet.createRow(rowNum++);
            
            Cell customerLabel1 = customerRow1.createCell(0);
            customerLabel1.setCellValue("客户名称");
            customerLabel1.setCellStyle(styles[2]); // 表头样式
            
            Cell customerValue1 = customerRow1.createCell(1);
            customerValue1.setCellValue("${客户.名称}");
            customerValue1.setCellStyle(styles[3]); // 单元格样式
            
            Cell customerLabel2 = customerRow1.createCell(2);
            customerLabel2.setCellValue("联系电话");
            customerLabel2.setCellStyle(styles[2]); // 表头样式
            
            Cell customerValue2 = customerRow1.createCell(3);
            customerValue2.setCellValue("${客户.电话}");
            customerValue2.setCellStyle(styles[3]); // 单元格样式
            
            Cell customerLabel3 = customerRow1.createCell(4);
            customerLabel3.setCellValue("订单编号");
            customerLabel3.setCellStyle(styles[2]); // 表头样式
            
            Cell customerValue3 = customerRow1.createCell(5);
            customerValue3.setCellValue("${订单.编号}");
            customerValue3.setCellStyle(styles[3]); // 单元格样式
            
            Row customerRow2 = sheet.createRow(rowNum++);
            
            Cell dateLabel = customerRow2.createCell(0);
            dateLabel.setCellValue("下单日期");
            dateLabel.setCellStyle(styles[2]); // 表头样式
            
            Cell dateValue = customerRow2.createCell(1);
            dateValue.setCellValue("${订单.日期}");
            dateValue.setCellStyle(styles[3]); // 单元格样式
            
            Cell addressLabel = customerRow2.createCell(2);
            addressLabel.setCellValue("收货地址");
            addressLabel.setCellStyle(styles[2]); // 表头样式
            
            Cell addressValue = customerRow2.createCell(3);
            addressValue.setCellValue("${联系人.地址}");
            addressValue.setCellStyle(styles[3]); // 单元格样式
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 3, 5));
            
            // 空行
            sheet.createRow(rowNum++);
            
            // 3. 商品信息部分
            Row productTitle = sheet.createRow(rowNum++);
            Cell productTitleCell = productTitle.createCell(0);
            productTitleCell.setCellValue("🛒 商品列表");
            productTitleCell.setCellStyle(styles[1]); // 小标题样式
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));
            
            // 插入列表开始标记（隐藏）
            Row listStartRow = sheet.createRow(rowNum++);
            Cell listStartCell = listStartRow.createCell(0);
            listStartCell.setCellValue("{{#商品列表}}");
            CellStyle hiddenStyle = workbook.createCellStyle();
            hiddenStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            hiddenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font hiddenFont = workbook.createFont();
            hiddenFont.setColor(IndexedColors.WHITE.getIndex());
            hiddenFont.setFontHeightInPoints((short)1);
            hiddenStyle.setFont(hiddenFont);
            listStartCell.setCellStyle(hiddenStyle);
            
            // 商品表格头
            Row productHeader = sheet.createRow(rowNum++);
            
            String[] headers = new String[]{"序号", "商品名称", "单价", "数量", "小计", "备注"};
            for (int i = 0; i < headers.length; i++) {
                Cell headerCell = productHeader.createCell(i);
                headerCell.setCellValue(headers[i]);
                headerCell.setCellStyle(styles[2]); // 表头样式
            }
            
            // 商品表格数据行
            Row productRow = sheet.createRow(rowNum++);
            
            Cell productCell1 = productRow.createCell(0);
            productCell1.setCellValue("{{序号}}");
            productCell1.setCellStyle(styles[3]); // 单元格样式
            
            Cell productCell2 = productRow.createCell(1);
            productCell2.setCellValue("{{名称}}");
            productCell2.setCellStyle(styles[3]); // 单元格样式
            
            Cell productCell3 = productRow.createCell(2);
            productCell3.setCellValue("{{单价}}");
            productCell3.setCellStyle(styles[3]); // 单元格样式
            
            Cell productCell4 = productRow.createCell(3);
            productCell4.setCellValue("{{数量}}");
            productCell4.setCellStyle(styles[3]); // 单元格样式
            
            Cell productCell5 = productRow.createCell(4);
            productCell5.setCellValue("{{小计}}");
            productCell5.setCellStyle(styles[3]); // 单元格样式
            
            Cell productCell6 = productRow.createCell(5);
            productCell6.setCellValue("");
            productCell6.setCellStyle(styles[3]); // 单元格样式
            
            // 插入列表结束标记（隐藏）
            Row listEndRow = sheet.createRow(rowNum++);
            Cell listEndCell = listEndRow.createCell(0);
            listEndCell.setCellValue("{{/商品列表}}");
            listEndCell.setCellStyle(hiddenStyle);
            
            // 空行
            sheet.createRow(rowNum++);
            
            // 4. 合计信息
            Row totalRow = sheet.createRow(rowNum++);
            
            Cell totalLabel = totalRow.createCell(3);
            totalLabel.setCellValue("总金额：");
            totalLabel.setCellStyle(styles[4]); // 强调样式
            
            Cell totalValue = totalRow.createCell(4);
            totalValue.setCellValue("${订单.总金额}");
            totalValue.setCellStyle(styles[4]); // 强调样式
            
            Cell totalUnit = totalRow.createCell(5);
            totalUnit.setCellValue("元");
            totalUnit.setCellStyle(styles[4]); // 强调样式
            
            // 5. 备注信息
            Row remarkRow = sheet.createRow(rowNum++);
            
            Cell remarkLabel = remarkRow.createCell(0);
            remarkLabel.setCellValue("📝 备注：");
            remarkLabel.setCellStyle(styles[1]); // 小标题样式
            
            Cell remarkValue = remarkRow.createCell(1);
            remarkValue.setCellValue("${订单.备注}");
            remarkValue.setCellStyle(styles[3]); // 单元格样式
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 1, 5));
            
            // 空行
            sheet.createRow(rowNum++);
            sheet.createRow(rowNum++);
            
            // 6. 签名行
            Row signatureRow = sheet.createRow(rowNum++);
            
            Cell signatureLabel = signatureRow.createCell(3);
            signatureLabel.setCellValue("客户签名：");
            signatureLabel.setCellStyle(styles[3]); // 单元格样式
            
            Cell signatureLine = signatureRow.createCell(4);
            signatureLine.setCellValue("__________________");
            signatureLine.setCellStyle(styles[3]); // 单元格样式
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 4, 5));
            
            // 保存文件
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                workbook.write(out);
            }
            
        } catch (Exception e) {
            AppLogger.error("生成Excel订单模板失败: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 创建样式数组
     * @param workbook 工作簿
     * @return 样式数组
     */
    private static CellStyle[] createStyles(Workbook workbook) {
        CellStyle[] styles = new CellStyle[5];
        
        // 标题样式
        XSSFCellStyle titleStyle = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont titleFont = (XSSFFont) workbook.createFont();
        titleFont.setFontHeightInPoints((short)16);
        titleFont.setBold(true);
        titleFont.setColor(new XSSFColor(PRIMARY_COLOR, null));
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styles[0] = titleStyle;
        
        // 小标题样式
        XSSFCellStyle subTitleStyle = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont subTitleFont = (XSSFFont) workbook.createFont();
        subTitleFont.setFontHeightInPoints((short)12);
        subTitleFont.setBold(true);
        subTitleFont.setColor(new XSSFColor(PRIMARY_COLOR, null));
        subTitleStyle.setFont(subTitleFont);
        subTitleStyle.setAlignment(HorizontalAlignment.LEFT);
        subTitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styles[1] = subTitleStyle;
        
        // 表头样式
        XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
        headerStyle.setFillForegroundColor(new XSSFColor(SECONDARY_COLOR, null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setTopBorderColor(new XSSFColor(BORDER_COLOR, null));
        headerStyle.setBottomBorderColor(new XSSFColor(BORDER_COLOR, null));
        headerStyle.setLeftBorderColor(new XSSFColor(BORDER_COLOR, null));
        headerStyle.setRightBorderColor(new XSSFColor(BORDER_COLOR, null));
        XSSFFont headerFont = (XSSFFont) workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        styles[2] = headerStyle;
        
        // 单元格样式
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setTopBorderColor(new XSSFColor(BORDER_COLOR, null));
        cellStyle.setBottomBorderColor(new XSSFColor(BORDER_COLOR, null));
        cellStyle.setLeftBorderColor(new XSSFColor(BORDER_COLOR, null));
        cellStyle.setRightBorderColor(new XSSFColor(BORDER_COLOR, null));
        styles[3] = cellStyle;
        
        // 强调样式
        XSSFCellStyle emphasisStyle = (XSSFCellStyle) workbook.createCellStyle();
        emphasisStyle.setAlignment(HorizontalAlignment.RIGHT);
        emphasisStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont emphasisFont = (XSSFFont) workbook.createFont();
        emphasisFont.setBold(true);
        emphasisFont.setColor(new XSSFColor(PRIMARY_COLOR, null));
        emphasisStyle.setFont(emphasisFont);
        styles[4] = emphasisStyle;
        
        return styles;
    }
    
    /**
     * 创建Excel模板
     * @param outputPath 输出路径
     */
    public static void createExcelTemplate(String outputPath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建工作表
            Sheet sheet = workbook.createSheet("模板");
            
            // 创建标题样式
            CellStyle titleStyle = createTitleStyle(workbook);
            
            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // 创建普通单元格样式
            CellStyle normalStyle = createNormalStyle(workbook);
            
            // 创建标题行
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleCell = titleRow.createCell(0);
            titleCell.setCellValue("订单信息模板");
            titleCell.setCellStyle(titleStyle);
            
            // 合并标题单元格
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            
            // 创建客户信息部分
            Row customerHeaderRow = sheet.createRow(2);
            Cell customerHeaderCell = customerHeaderRow.createCell(0);
            customerHeaderCell.setCellValue("客户信息");
            customerHeaderCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));
            
            // 客户信息表格
            String[][] customerInfo = {
                {"客户名称", "${客户.名称}", "联系电话", "${客户.电话}"},
                {"订单编号", "${订单.编号}", "下单日期", "${订单.日期}"},
                {"收货地址", "${联系人.地址}", "", ""}
            };
            
            for (int i = 0; i < customerInfo.length; i++) {
                Row row = sheet.createRow(i + 3);
                for (int j = 0; j < customerInfo[i].length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(customerInfo[i][j]);
                    cell.setCellStyle(normalStyle);
                }
                // 合并地址单元格
                if (i == 2) {
                    sheet.addMergedRegion(new CellRangeAddress(i + 3, i + 3, 1, 3));
                }
            }
            
            // 创建商品列表部分
            Row productHeaderRow = sheet.createRow(7);
            Cell productHeaderCell = productHeaderRow.createCell(0);
            productHeaderCell.setCellValue("商品列表");
            productHeaderCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(7, 7, 0, 5));
            
            // 插入列表开始标记（隐藏）
            Row listStartRow = sheet.createRow(8);
            Cell listStartCell = listStartRow.createCell(0);
            listStartCell.setCellValue("{{#商品列表}}");
            listStartCell.setCellStyle(normalStyle);
            
            // 商品表格头
            String[] productHeaders = {"序号", "商品名称", "单价", "数量", "小计", "备注"};
            Row productTableHeader = sheet.createRow(9);
            for (int i = 0; i < productHeaders.length; i++) {
                Cell cell = productTableHeader.createCell(i);
                cell.setCellValue(productHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 商品数据行
            Row productRow = sheet.createRow(10);
            String[] productData = {"{{序号}}", "{{名称}}", "{{单价}}", "{{数量}}", "{{小计}}", ""};
            for (int i = 0; i < productData.length; i++) {
                Cell cell = productRow.createCell(i);
                cell.setCellValue(productData[i]);
                cell.setCellStyle(normalStyle);
            }
            
            // 插入列表结束标记（隐藏）
            Row listEndRow = sheet.createRow(11);
            Cell listEndCell = listEndRow.createCell(0);
            listEndCell.setCellValue("{{/商品列表}}");
            listEndCell.setCellStyle(normalStyle);
            
            // 创建合计行
            Row totalRow = sheet.createRow(13);
            Cell totalLabelCell = totalRow.createCell(3);
            totalLabelCell.setCellValue("总金额：");
            totalLabelCell.setCellStyle(normalStyle);
            
            Cell totalValueCell = totalRow.createCell(4);
            totalValueCell.setCellValue("${订单.总金额}");
            totalValueCell.setCellStyle(normalStyle);
            
            Cell totalUnitCell = totalRow.createCell(5);
            totalUnitCell.setCellValue("元");
            totalUnitCell.setCellStyle(normalStyle);
            
            // 创建备注行
            Row remarkRow = sheet.createRow(15);
            Cell remarkLabelCell = remarkRow.createCell(0);
            remarkLabelCell.setCellValue("备注：");
            remarkLabelCell.setCellStyle(normalStyle);
            
            Cell remarkValueCell = remarkRow.createCell(1);
            remarkValueCell.setCellValue("${订单.备注}");
            remarkValueCell.setCellStyle(normalStyle);
            sheet.addMergedRegion(new CellRangeAddress(15, 15, 1, 5));
            
            // 设置列宽
            for (int i = 0; i < 6; i++) {
                sheet.setColumnWidth(i, 15 * 256);
            }
            
            // 保存文件
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                workbook.write(out);
            }
            
            System.out.println("Excel模板创建成功: " + outputPath);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("创建Excel模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建标题样式
     * @param workbook 工作簿
     * @return 标题样式
     */
    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 设置字体
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short)16);
        font.setBold(true);
        font.setColor(IndexedColors.PINK.getIndex());
        style.setFont(font);
        
        return style;
    }
    
    /**
     * 创建表头样式
     * @param workbook 工作簿
     * @return 表头样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        
        // 设置边框
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        
        return style;
    }
    
    /**
     * 创建普通单元格样式
     * @param workbook 工作簿
     * @return 普通单元格样式
     */
    private static CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 设置边框
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        
        return style;
    }
} 