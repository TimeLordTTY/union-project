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
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.timelordtty.AppLogger;
import com.timelordtty.docgen.model.TemplateField;

/**
 * Excel模板生成器工具类 - 基于HTML模板重新设计美化版
 */
public class ExcelTemplateGenerator {
    
    // 主题色定义
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
            // 创建模板字段 - 使用对象和列表字段的形式
            List<TemplateField> objectFields = new ArrayList<>();
            objectFields.add(new TemplateField("客户.名称", false));
            objectFields.add(new TemplateField("客户.电话", false));
            objectFields.add(new TemplateField("客户.邮箱", false));
            objectFields.add(new TemplateField("订单.编号", false));
            objectFields.add(new TemplateField("订单.日期", false));
            objectFields.add(new TemplateField("订单.总金额", false));
            objectFields.add(new TemplateField("备注", false));
            
            List<TemplateField> listFields = new ArrayList<>();
            listFields.add(new TemplateField("商品列表.序号", true));
            listFields.add(new TemplateField("商品列表.名称", true));
            listFields.add(new TemplateField("商品列表.型号", true));
            listFields.add(new TemplateField("商品列表.单价", true));
            listFields.add(new TemplateField("商品列表.数量", true));
            listFields.add(new TemplateField("商品列表.小计", true));
            
            // 创建Excel文档
            generateBeautifulOrderTemplate(outputPath, objectFields, listFields);
            
            System.out.println("Excel模板已成功生成：" + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Excel模板生成失败：" + e.getMessage());
        }
    }
    
    /**
     * 生成美化版订单模板
     * @param outputPath 输出路径
     * @param objectFields 对象字段列表
     * @param listFields 列表字段列表
     * @throws Exception 异常
     */
    private static void generateBeautifulOrderTemplate(String outputPath, 
                                                    List<TemplateField> objectFields, 
                                                    List<TemplateField> listFields) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("订单模板");
            
            // 创建样式集合
            CellStyle[] styles = createStyles(workbook);
            
            // 设置列宽
            sheet.setColumnWidth(0, 15 * 256); // A列
            sheet.setColumnWidth(1, 20 * 256); // B列
            sheet.setColumnWidth(2, 15 * 256); // C列
            sheet.setColumnWidth(3, 20 * 256); // D列
            sheet.setColumnWidth(4, 15 * 256); // E列
            sheet.setColumnWidth(5, 15 * 256); // F列
            
            int rowIndex = 0;
            
            // ===================== 1. 创建标题行 =====================
            Row titleRow = sheet.createRow(rowIndex++);
            titleRow.setHeightInPoints(30); // 设置行高
            
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("✨ 专业订单模板 ✨");
            titleCell.setCellStyle(styles[0]); // 标题样式
            
            // 合并标题单元格从A1到F1
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            
            // 空行
            rowIndex++;
            
            // ===================== 2. 创建客户信息部分 =====================
            Row customerHeaderRow = sheet.createRow(rowIndex++);
            customerHeaderRow.setHeightInPoints(22);
            
            Cell customerHeaderCell = customerHeaderRow.createCell(0);
            customerHeaderCell.setCellValue("📋 客户信息");
            customerHeaderCell.setCellStyle(styles[1]); // 分组标题样式
            
            // 合并分组标题单元格
            sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 5));
            
            // 客户基本信息表头行
            Row customerLabelRow = sheet.createRow(rowIndex++);
            customerLabelRow.setHeightInPoints(20);
            
            String[] customerLabels = {"客户名称", "联系电话", "客户邮箱", "订单编号", "下单日期", "订单金额"};
            for (int i = 0; i < customerLabels.length; i++) {
                Cell cell = customerLabelRow.createCell(i);
                cell.setCellValue(customerLabels[i]);
                cell.setCellStyle(styles[2]); // 表头样式
            }
            
            // 客户信息数据行
            Row customerDataRow = sheet.createRow(rowIndex++);
            customerDataRow.setHeightInPoints(20);
            
            Cell nameCell = customerDataRow.createCell(0);
            nameCell.setCellValue("${客户.名称}");
            nameCell.setCellStyle(styles[3]); // 数据样式
            
            Cell phoneCell = customerDataRow.createCell(1);
            phoneCell.setCellValue("${客户.电话}");
            phoneCell.setCellStyle(styles[3]);
            
            Cell emailCell = customerDataRow.createCell(2);
            emailCell.setCellValue("${客户.邮箱}");
            emailCell.setCellStyle(styles[3]);
            
            Cell orderIdCell = customerDataRow.createCell(3);
            orderIdCell.setCellValue("${订单.编号}");
            orderIdCell.setCellStyle(styles[3]);
            
            Cell dateCell = customerDataRow.createCell(4);
            dateCell.setCellValue("${订单.日期}");
            dateCell.setCellStyle(styles[3]);
            
            Cell amountCell = customerDataRow.createCell(5);
            amountCell.setCellValue("${订单.总金额}");
            amountCell.setCellStyle(styles[3]);
            
            // 空行
            rowIndex++;
            
            // ===================== 3. 创建商品信息部分 =====================
            Row productHeaderRow = sheet.createRow(rowIndex++);
            productHeaderRow.setHeightInPoints(22);
            
            Cell productHeaderCell = productHeaderRow.createCell(0);
            productHeaderCell.setCellValue("🛒 商品列表");
            productHeaderCell.setCellStyle(styles[1]); // 分组标题样式
            
            // 合并分组标题单元格
            sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 5));
            
            // 商品列表表头行
            Row productLabelRow = sheet.createRow(rowIndex++);
            productLabelRow.setHeightInPoints(20);
            
            String[] productLabels = {"序号", "商品名称", "型号", "单价", "数量", "小计"};
            for (int i = 0; i < productLabels.length; i++) {
                Cell cell = productLabelRow.createCell(i);
                cell.setCellValue(productLabels[i]);
                cell.setCellStyle(styles[2]); // 表头样式
            }
            
            // 商品列表数据行1
            Row productDataRow1 = sheet.createRow(rowIndex++);
            productDataRow1.setHeightInPoints(20);
            
            Cell seqCell = productDataRow1.createCell(0);
            seqCell.setCellValue("${商品列表.序号}");
            seqCell.setCellStyle(styles[3]);
            
            Cell productNameCell = productDataRow1.createCell(1);
            productNameCell.setCellValue("${商品列表.名称}");
            productNameCell.setCellStyle(styles[3]);
            
            Cell modelCell = productDataRow1.createCell(2);
            modelCell.setCellValue("${商品列表.型号}");
            modelCell.setCellStyle(styles[3]);
            
            Cell priceCell = productDataRow1.createCell(3);
            priceCell.setCellValue("${商品列表.单价}");
            priceCell.setCellStyle(styles[3]);
            
            Cell qtyCell = productDataRow1.createCell(4);
            qtyCell.setCellValue("${商品列表.数量}");
            qtyCell.setCellStyle(styles[3]);
            
            Cell subtotalCell = productDataRow1.createCell(5);
            subtotalCell.setCellValue("${商品列表.小计}");
            subtotalCell.setCellStyle(styles[3]);
            
            // 添加几个示例行，以便用户明白如何填写
            for (int i = 0; i < 5; i++) {
                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(20);
                for (int j = 0; j < 6; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(styles[3]);
                }
            }
            
            // ===================== 4. 创建备注部分 =====================
            Row remarkHeaderRow = sheet.createRow(rowIndex++);
            remarkHeaderRow.setHeightInPoints(22);
            
            Cell remarkHeaderCell = remarkHeaderRow.createCell(0);
            remarkHeaderCell.setCellValue("📝 备注");
            remarkHeaderCell.setCellStyle(styles[1]);
            
            // 合并备注标题单元格
            sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 5));
            
            // 备注内容行
            Row remarkContentRow = sheet.createRow(rowIndex++);
            remarkContentRow.setHeightInPoints(40); // 设置较高的行高
            
            Cell remarkCell = remarkContentRow.createCell(0);
            remarkCell.setCellValue("${备注}");
            remarkCell.setCellStyle(styles[3]);
            
            // 合并备注内容单元格
            sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 5));
            
            // ===================== 5. 创建隐藏的爱心彩蛋 =====================
            Row easterEggRow = sheet.createRow(rowIndex + 2);
            
            // 冻结前两行，方便查看
            sheet.createFreezePane(0, 5);
            
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
     * 创建Excel样式集合
     * @param workbook 工作簿
     * @return 样式数组 [0]:标题样式 [1]:分组标题样式 [2]:表头样式 [3]:数据单元格样式 [4]:彩蛋样式
     */
    private static CellStyle[] createStyles(Workbook workbook) {
        CellStyle[] styles = new CellStyle[5];
        
        // 创建颜色
        XSSFColor primaryColor = new XSSFColor(PRIMARY_COLOR, new DefaultIndexedColorMap());
        XSSFColor secondaryColor = new XSSFColor(SECONDARY_COLOR, new DefaultIndexedColorMap());
        XSSFColor borderColor = new XSSFColor(BORDER_COLOR, new DefaultIndexedColorMap());
        
        // ========== 1. 标题样式 ==========
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.PINK.getIndex());
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置边框
        titleStyle.setBorderBottom(BorderStyle.MEDIUM);
        titleStyle.setBorderTop(BorderStyle.MEDIUM);
        titleStyle.setBorderLeft(BorderStyle.MEDIUM);
        titleStyle.setBorderRight(BorderStyle.MEDIUM);
        titleStyle.setBottomBorderColor(IndexedColors.PINK.getIndex());
        titleStyle.setTopBorderColor(IndexedColors.PINK.getIndex());
        titleStyle.setLeftBorderColor(IndexedColors.PINK.getIndex());
        titleStyle.setRightBorderColor(IndexedColors.PINK.getIndex());
        // 设置背景色
        titleStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles[0] = titleStyle;
        
        // ========== 2. 分组标题样式 ==========
        CellStyle groupStyle = workbook.createCellStyle();
        Font groupFont = workbook.createFont();
        groupFont.setBold(true);
        groupFont.setFontHeightInPoints((short) 12);
        groupFont.setColor(IndexedColors.PINK.getIndex());
        groupStyle.setFont(groupFont);
        groupStyle.setAlignment(HorizontalAlignment.LEFT);
        groupStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置背景色（浅色）
        groupStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        groupStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles[1] = groupStyle;
        
        // ========== 3. 表头样式 ==========
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置边框
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        // 设置浅粉色背景
        headerStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles[2] = headerStyle;
        
        // ========== 4. 数据单元格样式 ==========
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置边框
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        // 设置自动换行
        dataStyle.setWrapText(true);
        styles[3] = dataStyle;
        
        // ========== 5. 彩蛋样式 ==========
        CellStyle easterEggStyle = workbook.createCellStyle();
        Font easterEggFont = workbook.createFont();
        easterEggFont.setColor(IndexedColors.PINK.getIndex());
        easterEggFont.setFontHeightInPoints((short) 8);
        easterEggStyle.setFont(easterEggFont);
        easterEggStyle.setAlignment(HorizontalAlignment.CENTER);
        styles[4] = easterEggStyle;
        
        return styles;
    }

    /**
     * 创建Excel模板
     * @param outputPath 输出路径
     */
    public static void createExcelTemplate(String outputPath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("订单模板");
            
            // 设置列宽
            sheet.setColumnWidth(0, 20 * 256);
            sheet.setColumnWidth(1, 20 * 256);
            sheet.setColumnWidth(2, 15 * 256);
            sheet.setColumnWidth(3, 15 * 256);
            
            // 创建标题行
            Row titleRow = sheet.createRow(0);
            CellStyle titleStyle = createTitleStyle(workbook);
            
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("订单信息");
            titleCell.setCellStyle(titleStyle);
            
            // 合并标题单元格
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
            
            // 创建客户信息
            Row customerRow1 = sheet.createRow(2);
            Row customerRow2 = sheet.createRow(3);
            Row customerRow3 = sheet.createRow(4);
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle normalStyle = createNormalStyle(workbook);
            
            // 客户名称
            Cell customerLabelCell = customerRow1.createCell(0);
            customerLabelCell.setCellValue("客户名称:");
            customerLabelCell.setCellStyle(headerStyle);
            
            Cell customerValueCell = customerRow1.createCell(1);
            customerValueCell.setCellValue("${customerName}");
            customerValueCell.setCellStyle(normalStyle);
            
            // 联系人
            Cell contactLabelCell = customerRow2.createCell(0);
            contactLabelCell.setCellValue("联系人:");
            contactLabelCell.setCellStyle(headerStyle);
            
            Cell contactValueCell = customerRow2.createCell(1);
            contactValueCell.setCellValue("${contactPerson}");
            contactValueCell.setCellStyle(normalStyle);
            
            // 联系电话
            Cell phoneLabelCell = customerRow3.createCell(0);
            phoneLabelCell.setCellValue("联系电话:");
            phoneLabelCell.setCellStyle(headerStyle);
            
            Cell phoneValueCell = customerRow3.createCell(1);
            phoneValueCell.setCellValue("${contactPhone}");
            phoneValueCell.setCellStyle(normalStyle);
            
            // 日期
            Cell dateLabelCell = customerRow1.createCell(2);
            dateLabelCell.setCellValue("日期:");
            dateLabelCell.setCellStyle(headerStyle);
            
            Cell dateValueCell = customerRow1.createCell(3);
            dateValueCell.setCellValue("${orderDate}");
            dateValueCell.setCellStyle(normalStyle);
            
            // 订单号
            Cell orderNoLabelCell = customerRow2.createCell(2);
            orderNoLabelCell.setCellValue("订单号:");
            orderNoLabelCell.setCellStyle(headerStyle);
            
            Cell orderNoValueCell = customerRow2.createCell(3);
            orderNoValueCell.setCellValue("${orderNumber}");
            orderNoValueCell.setCellStyle(normalStyle);
            
            // 创建订单表头
            Row headerRow = sheet.createRow(6);
            
            Cell productHeaderCell = headerRow.createCell(0);
            productHeaderCell.setCellValue("产品名称");
            productHeaderCell.setCellStyle(headerStyle);
            
            Cell quantityHeaderCell = headerRow.createCell(1);
            quantityHeaderCell.setCellValue("数量");
            quantityHeaderCell.setCellStyle(headerStyle);
            
            Cell priceHeaderCell = headerRow.createCell(2);
            priceHeaderCell.setCellValue("单价");
            priceHeaderCell.setCellStyle(headerStyle);
            
            Cell totalHeaderCell = headerRow.createCell(3);
            totalHeaderCell.setCellValue("总价");
            totalHeaderCell.setCellStyle(headerStyle);
            
            // 创建订单数据行示例
            Row dataRow = sheet.createRow(7);
            
            Cell productCell = dataRow.createCell(0);
            productCell.setCellValue("${products.name}");
            productCell.setCellStyle(normalStyle);
            
            Cell quantityCell = dataRow.createCell(1);
            quantityCell.setCellValue("${products.quantity}");
            quantityCell.setCellStyle(normalStyle);
            
            Cell priceCell = dataRow.createCell(2);
            priceCell.setCellValue("${products.price}");
            priceCell.setCellStyle(normalStyle);
            
            Cell totalCell = dataRow.createCell(3);
            totalCell.setCellValue("${products.total}");
            totalCell.setCellStyle(normalStyle);
            
            // 创建总计行
            Row totalRow = sheet.createRow(9);
            
            Cell totalLabelCell = totalRow.createCell(2);
            totalLabelCell.setCellValue("总计:");
            totalLabelCell.setCellStyle(headerStyle);
            
            Cell totalValueCell = totalRow.createCell(3);
            totalValueCell.setCellValue("${totalAmount}");
            totalValueCell.setCellStyle(headerStyle);
            
            // 保存工作簿
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
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
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    /**
     * 创建表头样式
     * @param workbook 工作簿
     * @return 表头样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    /**
     * 创建普通单元格样式
     * @param workbook 工作簿
     * @return 普通单元格样式
     */
    private static CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
} 