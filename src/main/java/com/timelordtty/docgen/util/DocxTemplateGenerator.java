package com.timelordtty.docgen.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

import com.timelordtty.AppLogger;

/**
 * Word文档模板生成器
 * 用于生成示例Word文档模板
 */
public class DocxTemplateGenerator {
    
    /**
     * 创建示例Word文档模板
     * 
     * @param outputPath 输出文件路径
     * @throws IOException 如果创建文件失败
     */
    public static void createSampleTemplate(String outputPath) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(outputPath)) {
            
            AppLogger.info("开始创建Word文档模板: " + outputPath);
            
            // 创建标题
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("订单确认单");
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setFontFamily("宋体");
            titleRun.addBreak();
            
            // 创建公司信息段落
            XWPFParagraph companyParagraph = document.createParagraph();
            companyParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun companyRun = companyParagraph.createRun();
            companyRun.setText("公司名称: ${公司名称}");
            companyRun.setFontSize(12);
            companyRun.addBreak();
            companyRun.setText("客服电话: ${公司客服电话}");
            companyRun.setFontSize(12);
            companyRun.addBreak();
            companyRun.setText("日期: ${当前日期}");
            companyRun.setFontSize(12);
            companyRun.addBreak();
            companyRun.addBreak();
            
            // 创建客户信息段落
            XWPFParagraph clientParagraph = document.createParagraph();
            XWPFRun clientRun = clientParagraph.createRun();
            clientRun.setText("客户信息:");
            clientRun.setBold(true);
            clientRun.setFontSize(12);
            clientRun.addBreak();
            clientRun.setText("客户姓名: ${客户姓名}");
            clientRun.setBold(false);
            clientRun.addBreak();
            clientRun.setText("联系电话: ${客户电话}");
            clientRun.addBreak();
            clientRun.setText("送货地址: ${客户地址}");
            clientRun.addBreak();
            clientRun.addBreak();
            
            // 创建订单信息段落
            XWPFParagraph orderParagraph = document.createParagraph();
            XWPFRun orderRun = orderParagraph.createRun();
            orderRun.setText("订单信息:");
            orderRun.setBold(true);
            orderRun.setFontSize(12);
            orderRun.addBreak();
            orderRun.setText("订单编号: ${订单编号}");
            orderRun.setBold(false);
            orderRun.addBreak();
            orderRun.setText("预计送达日期: ${订单送达日期}");
            orderRun.addBreak();
            orderRun.addBreak();
            
            // 创建订单项表格
            XWPFParagraph tableTitleParagraph = document.createParagraph();
            XWPFRun tableTitleRun = tableTitleParagraph.createRun();
            tableTitleRun.setText("订单明细:");
            tableTitleRun.setBold(true);
            tableTitleRun.setFontSize(12);
            
            XWPFTable table = document.createTable(2, 5);
            
            // 设置表头
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.getCell(0).setText("序号");
            headerRow.getCell(1).setText("名称");
            headerRow.getCell(2).setText("数量");
            headerRow.getCell(3).setText("单价");
            headerRow.getCell(4).setText("小计");
            
            // 添加示例行（将在运行时替换）
            XWPFTableRow dataRow = table.getRow(1);
            dataRow.getCell(0).setText("${商品列表.序号}");
            dataRow.getCell(1).setText("${商品列表.名称}");
            dataRow.getCell(2).setText("${商品列表.数量}");
            dataRow.getCell(3).setText("${商品列表.单价}");
            dataRow.getCell(4).setText("${商品列表.小计}");
            
            // 添加合计行
            XWPFParagraph totalParagraph = document.createParagraph();
            totalParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun totalRun = totalParagraph.createRun();
            totalRun.setText("总金额: ${订单总金额} 元");
            totalRun.setBold(true);
            totalRun.setFontSize(12);
            totalRun.addBreak();
            totalRun.addBreak();
            
            // 添加成交记录标题
            XWPFParagraph dealTitleParagraph = document.createParagraph();
            dealTitleParagraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun dealTitleRun = dealTitleParagraph.createRun();
            dealTitleRun.setText("成交记录：");
            dealTitleRun.setBold(true);
            dealTitleRun.setFontSize(12);
            dealTitleRun.addBreak();
            
            // 创建成交记录表格
            XWPFTable dealTable = document.createTable(2, 4);
            dealTable.setWidth("100%");
            
            // 设置成交记录表格样式
            CTTblPr dealTblPr = dealTable.getCTTbl().getTblPr();
            dealTblPr.getTblW().setType(STTblWidth.PCT);
            dealTblPr.getTblW().setW(BigInteger.valueOf(5000));
            
            // 创建成交记录表格头部
            XWPFTableRow dealHeaderRow = dealTable.getRow(0);
            dealHeaderRow.getCell(0).setText("序号");
            dealHeaderRow.getCell(1).setText("合约");
            dealHeaderRow.getCell(2).setText("金额");
            dealHeaderRow.getCell(3).setText("日期");
            
            // 添加成交记录示例行
            XWPFTableRow dealDataRow = dealTable.getRow(1);
            dealDataRow.getCell(0).setText("${成交列表.序号}");
            dealDataRow.getCell(1).setText("${成交列表.合约}");
            dealDataRow.getCell(2).setText("${成交列表.金额}");
            dealDataRow.getCell(3).setText("${成交列表.日期}");
            
            // 添加备注和签名区域
            XWPFParagraph notesParagraph = document.createParagraph();
            notesParagraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun notesRun = notesParagraph.createRun();
            notesRun.setText("备注事项:");
            notesRun.setBold(true);
            notesRun.setFontSize(12);
            notesRun.addBreak();
            notesRun.setText("1. 请在收到货物后仔细检查，如有问题请立即联系客服。");
            notesRun.setBold(false);
            notesRun.addBreak();
            notesRun.setText("2. 本订单自签收之日起7天内可办理退换货手续。");
            notesRun.addBreak();
            notesRun.setText("3. ${备注}");
            notesRun.addBreak();
            notesRun.addBreak();
            notesRun.addBreak();
            
            // 添加签名区域
            XWPFParagraph signatureParagraph = document.createParagraph();
            signatureParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun signatureRun = signatureParagraph.createRun();
            signatureRun.setText("客户签名: ________________        日期: ________________");
            signatureRun.setFontSize(12);
            
            // 保存文档
            document.write(out);
            AppLogger.info("Word文档模板创建成功: " + outputPath);
        } catch (IOException e) {
            AppLogger.error("创建Word文档模板失败: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 用于测试和独立运行
     * 
     * @param args 命令行参数，第一个参数为输出文件路径
     */
    public static void main(String[] args) {
        try {
            String outputPath = args.length > 0 ? args[0] : "doc-generator/src/main/resources/templates/project_report.docx";
            createSampleTemplate(outputPath);
            System.out.println("成功创建Word模板: " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 