package com.timelordtty.docgen.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.timelordtty.AppLogger;

/**
 * Excel测试数据生成器
 * 生成可直接用于填充Word和Excel模板的数据文件
 */
public class ExcelDataGenerator {
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("用法: java ExcelDataGenerator <输出文件路径>");
            return;
        }
        
        String outputPath = args[0];
        
        try {
            generateExcelFile(outputPath);
            System.out.println("Excel测试数据已生成: " + outputPath);
        } catch (IOException e) {
            AppLogger.error("生成Excel测试数据失败", e);
            System.err.println("生成Excel数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成Excel测试数据文件
     * 
     * @param outputPath 输出文件路径
     * @throws IOException 如果文件操作失败
     */
    public static void generateExcelFile(String outputPath) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // 创建普通字段工作表
            createBasicInfoSheet(workbook);
            
            // 创建项目团队工作表
            createTeamSheet(workbook);
            
            // 创建里程碑工作表
            createMilestoneSheet(workbook);
            
            // 创建风险点工作表
            createRiskSheet(workbook);
            
            // 调整列宽
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int j = 0; j < sheet.getRow(0).getLastCellNum(); j++) {
                    sheet.autoSizeColumn(j);
                }
            }
            
            // 写入文件
            try (FileOutputStream outputStream = new FileOutputStream(outputPath)) {
                workbook.write(outputStream);
            }
        }
    }
    
    /**
     * 创建基本信息工作表
     */
    private static void createBasicInfoSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("普通字段");
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("字段名");
        headerRow.createCell(1).setCellValue("字段值");
        
        // 添加基本字段
        String[][] basicFields = {
            {"项目名称", "智慧城市平台建设项目"},
            {"项目编号", "P" + String.format("%05d", random.nextInt(100000))},
            {"项目负责人", "张经理"},
            {"开始日期", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))},
            {"结束日期", LocalDate.now().plusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))},
            {"项目目标", "建设智慧城市综合管理平台，提升城市管理效率"},
            {"项目范围", "包括平台架构设计、核心功能开发、系统集成和部署"},
            {"完成百分比", "35"},
            {"本月工作内容", "完成需求分析和系统设计，开始核心模块开发"},
            {"下月工作计划", "继续核心功能开发，开始接口集成测试"},
            {"项目成果", "完成系统架构设计文档和核心功能模块"},
            {"经验与教训", "加强团队沟通，提前识别项目风险"},
            {"下一步计划", "加快开发进度，确保按期交付"},
            {"公司名称", "领航科技有限公司"},
            {"日期", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
        };
        
        int rowNum = 1;
        for (String[] field : basicFields) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(field[0]);
            row.createCell(1).setCellValue(field[1]);
        }
    }
    
    /**
     * 创建团队成员工作表
     */
    private static void createTeamSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("团队成员");
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("姓名");
        headerRow.createCell(1).setCellValue("职位");
        headerRow.createCell(2).setCellValue("联系方式");
        
        // 添加示例数据
        String[][] teamData = {
            {"张三", "项目经理", "13800138001"},
            {"李四", "技术主管", "13800138002"},
            {"王五", "开发工程师", "13800138003"},
            {"赵六", "测试工程师", "13800138004"},
            {"钱七", "UI设计师", "13800138005"}
        };
        
        int rowNum = 1;
        for (String[] member : teamData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(member[0]);
            row.createCell(1).setCellValue(member[1]);
            row.createCell(2).setCellValue(member[2]);
        }
    }
    
    /**
     * 创建里程碑工作表
     */
    private static void createMilestoneSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("里程碑");
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("名称");
        headerRow.createCell(1).setCellValue("计划日期");
        headerRow.createCell(2).setCellValue("实际日期");
        headerRow.createCell(3).setCellValue("完成状态");
        
        // 添加示例数据
        LocalDate startDate = LocalDate.now();
        String[][] milestoneData = {
            {"需求分析完成", startDate.plusWeeks(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
             startDate.plusWeeks(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "已完成"},
            {"系统设计完成", startDate.plusWeeks(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
             startDate.plusWeeks(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "已完成"},
            {"核心功能开发", startDate.plusWeeks(14).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
             "", "进行中"},
            {"系统测试完成", startDate.plusWeeks(20).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
             "", "未开始"},
            {"系统上线", startDate.plusWeeks(24).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
             "", "未开始"}
        };
        
        int rowNum = 1;
        for (String[] milestone : milestoneData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(milestone[0]);
            row.createCell(1).setCellValue(milestone[1]);
            row.createCell(2).setCellValue(milestone[2]);
            row.createCell(3).setCellValue(milestone[3]);
        }
    }
    
    /**
     * 创建风险点工作表
     */
    private static void createRiskSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("风险点");
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("风险描述");
        headerRow.createCell(1).setCellValue("影响程度");
        headerRow.createCell(2).setCellValue("应对措施");
        headerRow.createCell(3).setCellValue("责任人");
        
        // 添加示例数据
        String[][] riskData = {
            {"技术架构复杂度高", "高", "细化技术方案，增加技术评审", "李四"},
            {"项目进度延迟", "中", "调整资源配置，加强进度管理", "张三"},
            {"需求变更频繁", "高", "加强需求管理，控制变更范围", "王五"},
            {"系统性能不达标", "中", "优化系统架构，进行性能测试", "赵六"},
            {"团队协作效率低", "低", "改进沟通机制，定期团队会议", "张三"}
        };
        
        int rowNum = 1;
        for (String[] risk : riskData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(risk[0]);
            row.createCell(1).setCellValue(risk[1]);
            row.createCell(2).setCellValue(risk[2]);
            row.createCell(3).setCellValue(risk[3]);
        }
    }
} 