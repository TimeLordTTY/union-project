package com.timelordtty.docgen.util;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

import com.timelordtty.AppLogger;
import com.timelordtty.docgen.model.TemplateField;

/**
 * Word模板生成器工具类 - 基于HTML模板重新设计
 */
public class DocxTemplateGenerator {
    
    // 粉色主题色值
    private static final String PINK_COLOR = "E91E63";
    private static final String LIGHT_PINK = "FFD0E0";
    
    /**
     * 生成Word模板示例
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        String outputPath = "templates/word/订单模板.docx";
        
        // 如果有命令行参数，则使用第一个参数作为输出路径
        if (args.length > 0) {
            outputPath = args[0];
        }
        
        try {
            // 创建模板字段
            List<TemplateField> fields = new ArrayList<>();
            fields.add(new TemplateField("客户", false));  // 对象字段
            fields.add(new TemplateField("订单", false));  // 对象字段
            fields.add(new TemplateField("联系人", false)); // 对象字段
            fields.add(new TemplateField("商品列表", true)); // 列表字段
            
            // 创建Word文档
            generateOrderTemplate(outputPath, fields);
            
            System.out.println("Word模板已成功生成：" + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Word模板生成失败：" + e.getMessage());
        }
    }
    
    /**
     * 生成订单模板 - 美化版
     * @param outputPath 输出路径
     * @param fields 字段列表
     * @throws Exception 异常
     */
    private static void generateOrderTemplate(String outputPath, List<TemplateField> fields) throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {
            
            // ======================= 1. 创建文档标题 =======================
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            titleParagraph.setSpacingAfter(200);
            
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("✨ 专业订单信息 ✨");
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setColor(PINK_COLOR);
            titleRun.setFontFamily("微软雅黑");
            
            // ======================= 2. 创建订单基本信息部分 =======================
            XWPFParagraph orderInfoTitle = document.createParagraph();
            orderInfoTitle.setSpacingBefore(200);
            
            XWPFRun orderInfoTitleRun = orderInfoTitle.createRun();
            orderInfoTitleRun.setText("📋 订单基本信息");
            orderInfoTitleRun.setBold(true);
            orderInfoTitleRun.setFontSize(14);
            orderInfoTitleRun.setColor(PINK_COLOR);
            orderInfoTitleRun.setFontFamily("微软雅黑");
            
            // 创建客户信息表格 - 使用3x4表格布局
            XWPFTable infoTable = document.createTable(3, 4);
            infoTable.setWidth("100%");
            
            // 设置表格边框和样式
            setTableBorders(infoTable, PINK_COLOR);
            
            // 第一行：标题行
            XWPFTableRow headerRow = infoTable.getRow(0);
            headerRow.getCell(0).setText("客户信息");
            mergeAndStyleCell(headerRow.getCell(0), 4, PINK_COLOR, true);
            
            // 第二行：客户信息第一行
            XWPFTableRow row1 = infoTable.getRow(1);
            styleCellWithLabel(row1.getCell(0), "客户名称", LIGHT_PINK);
            styleCellWithValue(row1.getCell(1), "${客户.名称}");
            styleCellWithLabel(row1.getCell(2), "订单编号", LIGHT_PINK);
            styleCellWithValue(row1.getCell(3), "${订单.编号}");
            
            // 第三行：客户信息第二行
            XWPFTableRow row2 = infoTable.getRow(2);
            styleCellWithLabel(row2.getCell(0), "下单日期", LIGHT_PINK);
            styleCellWithValue(row2.getCell(1), "${订单.日期}");
            styleCellWithLabel(row2.getCell(2), "联系电话", LIGHT_PINK);
            styleCellWithValue(row2.getCell(3), "${联系人.电话}");
            
            // 地址段落
            XWPFParagraph addressParagraph = document.createParagraph();
            addressParagraph.setSpacingBefore(200);
            addressParagraph.setSpacingAfter(200);
            
            XWPFRun addressLabelRun = addressParagraph.createRun();
            addressLabelRun.setText("📍 收货地址：");
            addressLabelRun.setBold(true);
            addressLabelRun.setColor(PINK_COLOR);
            addressLabelRun.setFontFamily("微软雅黑");
            
            XWPFRun addressValueRun = addressParagraph.createRun();
            addressValueRun.setText("${联系人.地址}");
            addressValueRun.setFontFamily("微软雅黑");
            
            // ======================= 3. 创建商品列表部分 =======================
            XWPFParagraph productListTitle = document.createParagraph();
            productListTitle.setSpacingBefore(200);
            
            XWPFRun productListTitleRun = productListTitle.createRun();
            productListTitleRun.setText("🛒 商品列表");
            productListTitleRun.setBold(true);
            productListTitleRun.setFontSize(14);
            productListTitleRun.setColor(PINK_COLOR);
            productListTitleRun.setFontFamily("微软雅黑");
            
            // 创建商品表格
            XWPFTable productTable = document.createTable(2, 5);
            productTable.setWidth("100%");
            setTableBorders(productTable, PINK_COLOR);
            
            // 表头
            XWPFTableRow productHeaderRow = productTable.getRow(0);
            styleCellWithLabel(productHeaderRow.getCell(0), "序号", LIGHT_PINK);
            styleCellWithLabel(productHeaderRow.getCell(1), "商品名称", LIGHT_PINK);
            styleCellWithLabel(productHeaderRow.getCell(2), "单价", LIGHT_PINK);
            styleCellWithLabel(productHeaderRow.getCell(3), "数量", LIGHT_PINK);
            styleCellWithLabel(productHeaderRow.getCell(4), "小计", LIGHT_PINK);
            
            // 创建商品行示例 - 使用${商品列表.xxx}格式
            XWPFTableRow productRow = productTable.getRow(1);
            styleCellWithValue(productRow.getCell(0), "${商品列表.序号}");
            styleCellWithValue(productRow.getCell(1), "${商品列表.名称}");
            styleCellWithValue(productRow.getCell(2), "${商品列表.单价}");
            styleCellWithValue(productRow.getCell(3), "${商品列表.数量}");
            styleCellWithValue(productRow.getCell(4), "${商品列表.小计}");
            
            // ======================= 4. 创建合计部分 =======================
            XWPFParagraph totalParagraph = document.createParagraph();
            totalParagraph.setAlignment(ParagraphAlignment.RIGHT);
            totalParagraph.setSpacingBefore(200);
            totalParagraph.setSpacingAfter(200);
            
            XWPFRun totalLabelRun = totalParagraph.createRun();
            totalLabelRun.setText("💰 总金额：");
            totalLabelRun.setBold(true);
            totalLabelRun.setColor(PINK_COLOR);
            totalLabelRun.setFontSize(12);
            totalLabelRun.setFontFamily("微软雅黑");
            
            XWPFRun totalValueRun = totalParagraph.createRun();
            totalValueRun.setText("${订单.总金额}");
            totalValueRun.setBold(true);
            totalValueRun.setFontFamily("微软雅黑");
            totalValueRun.setFontSize(12);
            
            XWPFRun totalUnitRun = totalParagraph.createRun();
            totalUnitRun.setText(" 元");
            totalUnitRun.setBold(true);
            totalUnitRun.setFontFamily("微软雅黑");
            totalUnitRun.setFontSize(12);
            
            // ======================= 5. 创建备注部分 =======================
            XWPFParagraph remarkParagraph = document.createParagraph();
            remarkParagraph.setSpacingBefore(200);
            
            XWPFRun remarkLabelRun = remarkParagraph.createRun();
            remarkLabelRun.setText("📝 备注：");
            remarkLabelRun.setBold(true);
            remarkLabelRun.setColor(PINK_COLOR);
            remarkLabelRun.setFontFamily("微软雅黑");
            
            XWPFRun remarkValueRun = remarkParagraph.createRun();
            remarkValueRun.setText("${订单.备注}");
            remarkValueRun.setFontFamily("微软雅黑");
            
            // ======================= 6. 创建签名和日期行 =======================
            XWPFParagraph signatureParagraph = document.createParagraph();
            signatureParagraph.setAlignment(ParagraphAlignment.RIGHT);
            signatureParagraph.setSpacingBefore(600);
            
            XWPFRun signatureRun = signatureParagraph.createRun();
            signatureRun.setText("客户签名：________________        日期：________________");
            signatureRun.setFontFamily("微软雅黑");
            
            // ======================= 7. 隐藏的爱心彩蛋 =======================
            XWPFParagraph easterEggParagraph = document.createParagraph();
            easterEggParagraph.setAlignment(ParagraphAlignment.CENTER);
            easterEggParagraph.setSpacingBefore(1000);
            
            // 保存文档
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                document.write(out);
            }
        } catch (Exception e) {
            AppLogger.error("生成Word订单模板失败: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 设置表格的边框样式
     * @param table 表格
     * @param borderColor 边框颜色(RGB格式)
     */
    private static void setTableBorders(XWPFTable table, String borderColor) {
        // 使用正确的边框设置方法
        table.setBottomBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, borderColor);
        table.setTopBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, borderColor);
        table.setLeftBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, borderColor);
        table.setRightBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, borderColor);
        table.setInsideHBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, borderColor);
        table.setInsideVBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, borderColor);
    }
    
    /**
     * 合并并设置单元格样式
     * @param cell 单元格
     * @param colSpan 跨列数
     * @param bgColor 背景颜色
     * @param isBold 是否加粗
     */
    private static void mergeAndStyleCell(XWPFTableCell cell, int colSpan, String bgColor, boolean isBold) {
        // 设置单元格合并
        cell.getCTTc().addNewTcPr().addNewGridSpan().setVal(BigInteger.valueOf(colSpan));
        
        // 设置单元格样式
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        
        XWPFRun run = paragraph.createRun();
        run.setColor(bgColor);
        run.setBold(isBold);
        run.setFontFamily("微软雅黑");
    }
    
    /**
     * 设置带标签的单元格样式(表头)
     * @param cell 单元格
     * @param text 文本内容
     * @param bgColor 背景颜色
     */
    private static void styleCellWithLabel(XWPFTableCell cell, String text, String bgColor) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        
        // 清除现有内容
        cell.removeParagraph(0);
        paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontFamily("微软雅黑");
        
        // 设置单元格背景色
        cell.setColor(bgColor);
    }
    
    /**
     * 设置带值的单元格样式(数据行)
     * @param cell 单元格
     * @param text 文本内容
     */
    private static void styleCellWithValue(XWPFTableCell cell, String text) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        
        // 清除现有内容
        cell.removeParagraph(0);
        paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontFamily("微软雅黑");
    }

    /**
     * 向Word文档模板中添加占位符
     */
    public static void createWordTemplate(String outputPath) {
        try (XWPFDocument document = new XWPFDocument()) {
            
            // 添加标题
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("文档模板示例");
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            
            // 添加客户信息部分
            XWPFParagraph customerInfoTitle = document.createParagraph();
            customerInfoTitle.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun customerTitleRun = customerInfoTitle.createRun();
            customerTitleRun.setText("客户信息");
            customerTitleRun.setBold(true);
            customerTitleRun.setFontSize(14);
            
            // 创建客户信息表格
            XWPFTable customerTable = document.createTable(3, 2);
            setTableWidth(customerTable, "100%");
            
            // 设置表格内容
            setCellValue(customerTable, 0, 0, "客户名称:");
            setCellValue(customerTable, 0, 1, "${customerName}");
            setCellValue(customerTable, 1, 0, "联系人:");
            setCellValue(customerTable, 1, 1, "${contactPerson}");
            setCellValue(customerTable, 2, 0, "联系电话:");
            setCellValue(customerTable, 2, 1, "${contactPhone}");
            
            // 添加产品信息部分
            XWPFParagraph productInfoTitle = document.createParagraph();
            productInfoTitle.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun productTitleRun = productInfoTitle.createRun();
            productTitleRun.setText("产品信息");
            productTitleRun.setBold(true);
            productTitleRun.setFontSize(14);
            
            // 创建产品信息表格
            XWPFTable productTable = document.createTable(4, 4);
            setTableWidth(productTable, "100%");
            
            // 设置产品表头
            setCellValue(productTable, 0, 0, "产品名称");
            setCellValue(productTable, 0, 1, "数量");
            setCellValue(productTable, 0, 2, "单价");
            setCellValue(productTable, 0, 3, "总价");
            
            // 添加列表数据占位符
            setCellValue(productTable, 1, 0, "${products.name}");
            setCellValue(productTable, 1, 1, "${products.quantity}");
            setCellValue(productTable, 1, 2, "${products.price}");
            setCellValue(productTable, 1, 3, "${products.total}");
            
            // 添加结束文字
            XWPFParagraph endParagraph = document.createParagraph();
            endParagraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun endRun = endParagraph.createRun();
            endRun.setText("总金额: ${totalAmount}");
            endRun.setFontSize(12);
            
            // 添加日期
            XWPFParagraph dateParagraph = document.createParagraph();
            dateParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun dateRun = dateParagraph.createRun();
            dateRun.setText("日期: ${currentDate}");
            dateRun.setFontSize(12);
            
            // 保存文档
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                document.write(out);
            }
            
            System.out.println("Word模板创建成功: " + outputPath);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("创建Word模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置表格宽度
     * @param table 表格对象
     * @param width 宽度值（如"100%"）
     */
    private static void setTableWidth(XWPFTable table, String width) {
        CTTblWidth tblWidth = table.getCTTbl().addNewTblPr().addNewTblW();
        tblWidth.setW(BigInteger.valueOf(10000)); // 10000 = 100%
        tblWidth.setType(STTblWidth.PCT);
    }
    
    /**
     * 设置单元格内容
     * @param table 表格对象
     * @param row 行索引
     * @param col 列索引
     * @param text 文本内容
     */
    private static void setCellValue(XWPFTable table, int row, int col, String text) {
        XWPFTableCell cell = table.getRow(row).getCell(col);
        XWPFParagraph paragraph = cell.getParagraphArray(0);
        if (paragraph == null) {
            paragraph = cell.addParagraph();
        }
        XWPFRun run = paragraph.createRun();
        run.setText(text);
    }
} 