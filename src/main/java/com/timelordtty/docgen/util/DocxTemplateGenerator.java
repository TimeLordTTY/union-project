package com.timelordtty.docgen.util;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.timelordtty.AppLogger;
import com.timelordtty.docgen.model.TemplateField;

/**
 * Word模板生成器工具类
 */
public class DocxTemplateGenerator {
    
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
            fields.add(new TemplateField("客户名称", false));
            fields.add(new TemplateField("订单编号", false));
            fields.add(new TemplateField("下单日期", false));
            fields.add(new TemplateField("联系电话", false));
            fields.add(new TemplateField("收货地址", false));
            fields.add(new TemplateField("商品列表", true));
            fields.add(new TemplateField("总金额", false));
            fields.add(new TemplateField("备注", false));
            
            // 创建Word文档
            generateOrderTemplate(outputPath, fields);
            
            System.out.println("Word模板已生成：" + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Word模板生成失败：" + e.getMessage());
        }
    }
    
    /**
     * 生成订单模板
     * @param outputPath 输出路径
     * @param fields 字段列表
     * @throws Exception 异常
     */
    private static void generateOrderTemplate(String outputPath, List<TemplateField> fields) throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {
            // 1. 创建标题
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("订单信息");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            
            // 2. 创建客户信息表格
            XWPFTable customerTable = document.createTable(3, 4);
            customerTable.setWidth("100%");
            
            // 表格第一行：标题行
            XWPFTableRow headerRow = customerTable.getRow(0);
            headerRow.getCell(0).setText("客户信息表");
            headerRow.getCell(0).getCTTc().addNewTcPr().addNewGridSpan().setVal(BigInteger.valueOf(4));
            
            // 表格第二行：客户信息第一行
            XWPFTableRow customerRow1 = customerTable.getRow(1);
            customerRow1.getCell(0).setText("客户名称");
            customerRow1.getCell(1).setText("{{客户名称}}");
            customerRow1.getCell(2).setText("订单编号");
            customerRow1.getCell(3).setText("{{订单编号}}");
            
            // 表格第三行：客户信息第二行
            XWPFTableRow customerRow2 = customerTable.getRow(2);
            customerRow2.getCell(0).setText("下单日期");
            customerRow2.getCell(1).setText("{{下单日期}}");
            customerRow2.getCell(2).setText("联系电话");
            customerRow2.getCell(3).setText("{{联系电话}}");
            
            // 3. 创建收货地址段落
            XWPFParagraph addressParagraph = document.createParagraph();
            XWPFRun addressRun = addressParagraph.createRun();
            addressRun.setText("收货地址：{{收货地址}}");
            
            // 4. 创建商品列表
            XWPFParagraph listHeaderParagraph = document.createParagraph();
            XWPFRun listHeaderRun = listHeaderParagraph.createRun();
            listHeaderRun.setText("商品列表：");
            listHeaderRun.setBold(true);
            
            // 示例表格
            XWPFTable productTable = document.createTable(1, 5);
            productTable.setWidth("100%");
            
            // 表头
            XWPFTableRow productHeaderRow = productTable.getRow(0);
            productHeaderRow.getCell(0).setText("序号");
            productHeaderRow.getCell(1).setText("商品名称");
            productHeaderRow.getCell(2).setText("单价");
            productHeaderRow.getCell(3).setText("数量");
            productHeaderRow.getCell(4).setText("小计");
            
            // 商品列表循环
            XWPFParagraph loopStartParagraph = document.createParagraph();
            XWPFRun loopStartRun = loopStartParagraph.createRun();
            loopStartRun.setText("{{#商品列表}}");
            
            // 添加示例行
            XWPFTable itemTable = document.createTable(1, 5);
            itemTable.setWidth("100%");
            XWPFTableRow itemRow = itemTable.getRow(0);
            itemRow.getCell(0).setText("{{序号}}");
            itemRow.getCell(1).setText("{{商品名称}}");
            itemRow.getCell(2).setText("{{单价}}");
            itemRow.getCell(3).setText("{{数量}}");
            itemRow.getCell(4).setText("{{小计}}");
            
            XWPFParagraph loopEndParagraph = document.createParagraph();
            XWPFRun loopEndRun = loopEndParagraph.createRun();
            loopEndRun.setText("{{/商品列表}}");
            
            // 5. 创建总金额
            XWPFParagraph totalParagraph = document.createParagraph();
            totalParagraph.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.RIGHT);
            XWPFRun totalRun = totalParagraph.createRun();
            totalRun.setText("总金额：{{总金额}} 元");
            totalRun.setBold(true);
            
            // 6. 创建备注
            XWPFParagraph remarkParagraph = document.createParagraph();
            XWPFRun remarkRun = remarkParagraph.createRun();
            remarkRun.setText("备注：{{备注}}");
            
            // 7. 创建落款
            XWPFParagraph endingParagraph = document.createParagraph();
            endingParagraph.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.RIGHT);
            XWPFRun endingRun = endingParagraph.createRun();
            endingRun.setText("签字：____________");
            
            // 保存文档
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                document.write(out);
            }
        } catch (Exception e) {
            AppLogger.error("生成订单模板失败: " + e.getMessage(), e);
            throw e;
        }
    }
} 