package com.timelordtty.docgen.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.timelordtty.AppLogger;

/**
 * Word项目报告模板生成器
 * 创建标准的项目报告模板，包含基本信息、项目概述、进度情况和总结等部分
 */
public class DocxTemplateGenerator {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("用法: java DocxTemplateGenerator <输出文件路径>");
            return;
        }
        
        String outputPath = args[0];
        
        try {
            generateDocxTemplate(outputPath);
            System.out.println("Word模板已生成: " + outputPath);
        } catch (IOException e) {
            AppLogger.error("生成Word模板失败", e);
            System.err.println("生成Word模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成Word项目报告模板
     * 
     * @param outputPath 输出文件路径
     * @throws IOException 如果文件操作失败
     */
    public static void generateDocxTemplate(String outputPath) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            // 添加封面
            createCoverPage(document);
            
            // 添加目录
            createTableOfContents(document);
            
            // 添加项目基本信息部分
            createBasicInfoSection(document);
            
            // 添加项目概述部分
            createProjectOverviewSection(document);
            
            // 添加项目进度情况部分
            createProgressSection(document);
            
            // 添加项目风险管理部分
            createRiskManagementSection(document);
            
            // 添加项目总结部分
            createSummarySection(document);
            
            // 写入文件
            try (FileOutputStream outputStream = new FileOutputStream(outputPath)) {
                document.write(outputStream);
            }
        }
    }
    
    /**
     * 创建封面
     */
    private static void createCoverPage(XWPFDocument document) {
        // 添加标题
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        titleParagraph.setSpacingAfter(500);
        
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(24);
        titleRun.setText("{{项目名称}}");
        titleRun.addBreak();
        titleRun.addBreak();
        titleRun.setFontSize(18);
        titleRun.setText("项目进展报告");
        
        // 添加报告信息
        XWPFParagraph infoParagraph = document.createParagraph();
        infoParagraph.setAlignment(ParagraphAlignment.CENTER);
        infoParagraph.setSpacingBefore(800);
        
        XWPFRun infoRun = infoParagraph.createRun();
        infoRun.setFontSize(12);
        infoRun.setText("报告日期：{{日期}}");
        infoRun.addBreak();
        infoRun.setText("报告人：{{负责人}}");
        infoRun.addBreak();
        infoRun.setText("{{公司名称}}");
        
        // 添加分页符
        XWPFParagraph pageBreak = document.createParagraph();
        pageBreak.setPageBreak(true);
    }
    
    /**
     * 创建目录
     */
    private static void createTableOfContents(XWPFDocument document) {
        XWPFParagraph tocTitle = document.createParagraph();
        tocTitle.setAlignment(ParagraphAlignment.CENTER);
        
        XWPFRun tocTitleRun = tocTitle.createRun();
        tocTitleRun.setBold(true);
        tocTitleRun.setFontSize(16);
        tocTitleRun.setText("目 录");
        
        // 模拟目录项
        List<String> tocItems = Arrays.asList(
            "1. 项目基本信息",
            "2. 项目概述",
            "3. 项目进度情况",
            "4. 项目风险管理",
            "5. 项目总结"
        );
        
        for (String item : tocItems) {
            XWPFParagraph tocItem = document.createParagraph();
            tocItem.setAlignment(ParagraphAlignment.LEFT);
            tocItem.setIndentationLeft(500);
            
            XWPFRun tocItemRun = tocItem.createRun();
            tocItemRun.setFontSize(12);
            tocItemRun.setText(item);
        }
        
        // 添加分页符
        XWPFParagraph pageBreak = document.createParagraph();
        pageBreak.setPageBreak(true);
    }
    
    /**
     * 创建项目基本信息部分
     */
    private static void createBasicInfoSection(XWPFDocument document) {
        // 添加部分标题
        XWPFParagraph sectionTitle = document.createParagraph();
        sectionTitle.setStyle("Heading1");
        
        XWPFRun titleRun = sectionTitle.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.setText("1. 项目基本信息");
        
        // 创建项目信息表格
        XWPFTable table = document.createTable(5, 2);
        table.setWidth("100%");
        
        // 设置表格样式
        setTableCellsStyle(table);
        
        // 填充表格内容
        setCellText(table.getRow(0).getCell(0), "项目名称");
        setCellText(table.getRow(0).getCell(1), "{{项目名称}}");
        
        setCellText(table.getRow(1).getCell(0), "项目编号");
        setCellText(table.getRow(1).getCell(1), "{{项目编号}}");
        
        setCellText(table.getRow(2).getCell(0), "开始日期");
        setCellText(table.getRow(2).getCell(1), "{{开始日期}}");
        
        setCellText(table.getRow(3).getCell(0), "计划结束日期");
        setCellText(table.getRow(3).getCell(1), "{{结束日期}}");
        
        setCellText(table.getRow(4).getCell(0), "项目负责人");
        setCellText(table.getRow(4).getCell(1), "{{负责人}}");
        
        // 添加额外空白行
        document.createParagraph();
    }
    
    /**
     * 创建项目概述部分
     */
    private static void createProjectOverviewSection(XWPFDocument document) {
        // 添加部分标题
        XWPFParagraph sectionTitle = document.createParagraph();
        sectionTitle.setStyle("Heading1");
        
        XWPFRun titleRun = sectionTitle.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.setText("2. 项目概述");
        
        // 添加项目目标段落
        XWPFParagraph objectivePara = document.createParagraph();
        XWPFRun objectiveRun = objectivePara.createRun();
        objectiveRun.setBold(true);
        objectiveRun.setFontSize(12);
        objectiveRun.setText("2.1 项目目标");
        
        XWPFParagraph objectiveContentPara = document.createParagraph();
        XWPFRun objectiveContentRun = objectiveContentPara.createRun();
        objectiveContentRun.setFontSize(11);
        objectiveContentRun.setText("{{项目目标}}");
        
        // 添加项目范围段落
        XWPFParagraph scopePara = document.createParagraph();
        XWPFRun scopeRun = scopePara.createRun();
        scopeRun.setBold(true);
        scopeRun.setFontSize(12);
        scopeRun.setText("2.2 项目范围");
        
        XWPFParagraph scopeContentPara = document.createParagraph();
        XWPFRun scopeContentRun = scopeContentPara.createRun();
        scopeContentRun.setFontSize(11);
        scopeContentRun.setText("{{项目范围}}");
        
        // 添加项目关键干系人段落
        XWPFParagraph stakeholdersPara = document.createParagraph();
        XWPFRun stakeholdersRun = stakeholdersPara.createRun();
        stakeholdersRun.setBold(true);
        stakeholdersRun.setFontSize(12);
        stakeholdersRun.setText("2.3 项目干系人");
        
        // 添加团队成员表格
        XWPFParagraph teamTitle = document.createParagraph();
        XWPFRun teamTitleRun = teamTitle.createRun();
        teamTitleRun.setItalic(true);
        teamTitleRun.setFontSize(11);
        teamTitleRun.setText("项目团队成员：");
        
        XWPFTable teamTable = document.createTable(1, 3);
        teamTable.setWidth("100%");
        
        // 设置表头
        XWPFTableRow headerRow = teamTable.getRow(0);
        setCellText(headerRow.getCell(0), "姓名");
        setCellText(headerRow.getCell(1), "职位");
        setCellText(headerRow.getCell(2), "联系方式");
        
        // 添加列表模板标记
        XWPFParagraph teamListPara = document.createParagraph();
        XWPFRun teamListRun = teamListPara.createRun();
        teamListRun.setFontSize(11);
        teamListRun.setText("{{#团队成员}}{{团队成员.姓名}} {{团队成员.职位}} {{团队成员.联系方式}}{{/团队成员}}");
        
        // 添加额外空白行
        document.createParagraph();
    }
    
    /**
     * 创建项目进度情况部分
     */
    private static void createProgressSection(XWPFDocument document) {
        // 添加部分标题
        XWPFParagraph sectionTitle = document.createParagraph();
        sectionTitle.setStyle("Heading1");
        
        XWPFRun titleRun = sectionTitle.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.setText("3. 项目进度情况");
        
        // 添加总体进度描述
        XWPFParagraph overallProgressPara = document.createParagraph();
        XWPFRun overallProgressRun = overallProgressPara.createRun();
        overallProgressRun.setBold(true);
        overallProgressRun.setFontSize(12);
        overallProgressRun.setText("3.1 总体进度");
        
        XWPFParagraph progressContentPara = document.createParagraph();
        XWPFRun progressContentRun = progressContentPara.createRun();
        progressContentRun.setFontSize(11);
        progressContentRun.setText("截至{{日期}}，项目总体完成度：{{完成百分比}}%");
        
        // 添加里程碑进展表格
        XWPFParagraph milestonePara = document.createParagraph();
        XWPFRun milestoneRun = milestonePara.createRun();
        milestoneRun.setBold(true);
        milestoneRun.setFontSize(12);
        milestoneRun.setText("3.2 里程碑完成情况");
        
        XWPFTable milestoneTable = document.createTable(1, 4);
        milestoneTable.setWidth("100%");
        
        // 设置表头
        XWPFTableRow headerRow = milestoneTable.getRow(0);
        setCellText(headerRow.getCell(0), "里程碑名称");
        setCellText(headerRow.getCell(1), "计划日期");
        setCellText(headerRow.getCell(2), "实际日期");
        setCellText(headerRow.getCell(3), "完成状态");
        
        // 添加列表模板标记
        XWPFParagraph milestoneListPara = document.createParagraph();
        XWPFRun milestoneListRun = milestoneListPara.createRun();
        milestoneListRun.setFontSize(11);
        milestoneListRun.setText("{{#里程碑}}{{里程碑.名称}} {{里程碑.计划日期}} {{里程碑.实际日期}} {{里程碑.完成状态}}{{/里程碑}}");
        
        // 添加本月工作内容
        XWPFParagraph thisMonthPara = document.createParagraph();
        XWPFRun thisMonthRun = thisMonthPara.createRun();
        thisMonthRun.setBold(true);
        thisMonthRun.setFontSize(12);
        thisMonthRun.setText("3.3 本月工作内容");
        
        XWPFParagraph thisMonthContentPara = document.createParagraph();
        XWPFRun thisMonthContentRun = thisMonthContentPara.createRun();
        thisMonthContentRun.setFontSize(11);
        thisMonthContentRun.setText("{{本月工作内容}}");
        
        // 添加下月工作计划
        XWPFParagraph nextMonthPara = document.createParagraph();
        XWPFRun nextMonthRun = nextMonthPara.createRun();
        nextMonthRun.setBold(true);
        nextMonthRun.setFontSize(12);
        nextMonthRun.setText("3.4 下月工作计划");
        
        XWPFParagraph nextMonthContentPara = document.createParagraph();
        XWPFRun nextMonthContentRun = nextMonthContentPara.createRun();
        nextMonthContentRun.setFontSize(11);
        nextMonthContentRun.setText("{{下月工作计划}}");
        
        // 添加额外空白行
        document.createParagraph();
    }
    
    /**
     * 创建项目风险管理部分
     */
    private static void createRiskManagementSection(XWPFDocument document) {
        // 添加部分标题
        XWPFParagraph sectionTitle = document.createParagraph();
        sectionTitle.setStyle("Heading1");
        
        XWPFRun titleRun = sectionTitle.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.setText("4. 项目风险管理");
        
        // 添加风险清单
        XWPFParagraph riskPara = document.createParagraph();
        XWPFRun riskRun = riskPara.createRun();
        riskRun.setBold(true);
        riskRun.setFontSize(12);
        riskRun.setText("4.1 项目风险清单");
        
        XWPFTable riskTable = document.createTable(1, 4);
        riskTable.setWidth("100%");
        
        // 设置表头
        XWPFTableRow headerRow = riskTable.getRow(0);
        setCellText(headerRow.getCell(0), "风险描述");
        setCellText(headerRow.getCell(1), "影响程度");
        setCellText(headerRow.getCell(2), "应对措施");
        setCellText(headerRow.getCell(3), "责任人");
        
        // 添加列表模板标记
        XWPFParagraph riskListPara = document.createParagraph();
        XWPFRun riskListRun = riskListPara.createRun();
        riskListRun.setFontSize(11);
        riskListRun.setText("{{#风险点}}{{风险点.风险描述}} {{风险点.影响程度}} {{风险点.应对措施}} {{风险点.责任人}}{{/风险点}}");
        
        // 添加风险管理建议
        XWPFParagraph riskAdvicePara = document.createParagraph();
        XWPFRun riskAdviceRun = riskAdvicePara.createRun();
        riskAdviceRun.setBold(true);
        riskAdviceRun.setFontSize(12);
        riskAdviceRun.setText("4.2 风险管理建议");
        
        XWPFParagraph riskAdviceContentPara = document.createParagraph();
        XWPFRun riskAdviceContentRun = riskAdviceContentPara.createRun();
        riskAdviceContentRun.setFontSize(11);
        riskAdviceContentRun.setText("{{风险管理建议}}");
        
        // 添加额外空白行
        document.createParagraph();
    }
    
    /**
     * 创建项目总结部分
     */
    private static void createSummarySection(XWPFDocument document) {
        // 添加部分标题
        XWPFParagraph sectionTitle = document.createParagraph();
        sectionTitle.setStyle("Heading1");
        
        XWPFRun titleRun = sectionTitle.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.setText("5. 项目总结");
        
        // 添加项目成果
        XWPFParagraph achievementsPara = document.createParagraph();
        XWPFRun achievementsRun = achievementsPara.createRun();
        achievementsRun.setBold(true);
        achievementsRun.setFontSize(12);
        achievementsRun.setText("5.1 项目成果");
        
        XWPFParagraph achievementsContentPara = document.createParagraph();
        XWPFRun achievementsContentRun = achievementsContentPara.createRun();
        achievementsContentRun.setFontSize(11);
        achievementsContentRun.setText("{{项目成果}}");
        
        // 添加经验与教训
        XWPFParagraph lessonsPara = document.createParagraph();
        XWPFRun lessonsRun = lessonsPara.createRun();
        lessonsRun.setBold(true);
        lessonsRun.setFontSize(12);
        lessonsRun.setText("5.2 经验与教训");
        
        XWPFParagraph lessonsContentPara = document.createParagraph();
        XWPFRun lessonsContentRun = lessonsContentPara.createRun();
        lessonsContentRun.setFontSize(11);
        lessonsContentRun.setText("{{经验与教训}}");
        
        // 添加下一步计划
        XWPFParagraph nextStepsPara = document.createParagraph();
        XWPFRun nextStepsRun = nextStepsPara.createRun();
        nextStepsRun.setBold(true);
        nextStepsRun.setFontSize(12);
        nextStepsRun.setText("5.3 下一步计划");
        
        XWPFParagraph nextStepsContentPara = document.createParagraph();
        XWPFRun nextStepsContentRun = nextStepsContentPara.createRun();
        nextStepsContentRun.setFontSize(11);
        nextStepsContentRun.setText("{{下一步计划}}");
    }
    
    /**
     * 设置表格单元格样式
     */
    private static void setTableCellsStyle(XWPFTable table) {
        for (XWPFTableRow row : table.getRows()) {
            for (int i = 0; i < row.getTableCells().size(); i++) {
                XWPFTableCell cell = row.getCell(i);
                
                // 设置单元格样式
                cell.setWidth("50%");
                cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                
                // 设置单元格段落样式
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    paragraph.setSpacingBefore(60);
                    paragraph.setSpacingAfter(60);
                    if (i == 0) { // 第一列（标题列）
                        paragraph.setAlignment(ParagraphAlignment.RIGHT);
                    } else {
                        paragraph.setAlignment(ParagraphAlignment.LEFT);
                    }
                }
            }
        }
    }
    
    /**
     * 设置单元格文本内容
     */
    private static void setCellText(XWPFTableCell cell, String text) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();
        run.setFontSize(11);
        
        // 第一列（标题列）加粗
        if (cell.getTableRow().getTableCells().get(0) == cell) {
            run.setBold(true);
        }
        
        run.setText(text);
    }
} 