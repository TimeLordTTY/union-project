package com.timelordtty.docgen.utils;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.timelordtty.AppLogger;

/**
 * Excel项目模板生成器
 * 创建标准的项目管理Excel模板，包含项目基本信息、任务分配、进度跟踪等工作表
 */
public class ExcelTemplateGenerator {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("用法: java ExcelTemplateGenerator <输出文件路径>");
            return;
        }
        
        String outputPath = args[0];
        
        try {
            generateExcelTemplate(outputPath);
            System.out.println("Excel模板已生成: " + outputPath);
        } catch (IOException e) {
            AppLogger.error("生成Excel模板失败", e);
            System.err.println("生成Excel模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成Excel项目管理模板
     * 
     * @param outputPath 输出文件路径
     * @throws IOException 如果文件操作失败
     */
    public static void generateExcelTemplate(String outputPath) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // 创建项目信息工作表
            createProjectInfoSheet(workbook);
            
            // 创建任务列表工作表
            createTaskListSheet(workbook);
            
            // 创建资源分配工作表
            createResourceAllocationSheet(workbook);
            
            // 创建进度跟踪工作表
            createProgressTrackingSheet(workbook);
            
            // 创建风险管理工作表
            createRiskManagementSheet(workbook);
            
            // 创建财务预算工作表
            createBudgetSheet(workbook);
            
            // 设置默认打开第一个工作表
            workbook.setActiveSheet(0);
            
            // 写入文件
            try (FileOutputStream outputStream = new FileOutputStream(outputPath)) {
                workbook.write(outputStream);
            }
        }
    }
    
    /**
     * 创建项目信息工作表
     */
    private static void createProjectInfoSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("项目基本信息");
        
        // 设置列宽
        sheet.setColumnWidth(0, 20 * 256);
        sheet.setColumnWidth(1, 30 * 256);
        
        // 标题行
        XSSFRow titleRow = sheet.createRow(0);
        XSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("项目基本信息");
        XSSFCellStyle titleStyle = workbook.createCellStyle();
        XSSFFont titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        
        // 项目信息字段行
        String[][] fields = {
            {"项目名称", "{{项目名称}}"},
            {"项目编号", "{{项目编号}}"},
            {"项目负责人", "{{项目负责人}}"},
            {"开始日期", "{{开始日期}}"},
            {"结束日期", "{{结束日期}}"},
            {"项目目标", "{{项目目标}}"},
            {"关键利益相关方", "{{关键利益相关方}}"},
            {"项目描述", "{{项目描述}}"}
        };
        
        // 设置字段样式
        XSSFCellStyle labelStyle = createHeaderStyle(workbook);
        XSSFCellStyle fieldStyle = workbook.createCellStyle();
        fieldStyle.setBorderBottom(BorderStyle.THIN);
        fieldStyle.setBorderTop(BorderStyle.THIN);
        fieldStyle.setBorderLeft(BorderStyle.THIN);
        fieldStyle.setBorderRight(BorderStyle.THIN);
        
        // 添加字段
        for (int i = 0; i < fields.length; i++) {
            XSSFRow row = sheet.createRow(i + 2);
            XSSFCell labelCell = row.createCell(0);
            labelCell.setCellValue(fields[i][0]);
            labelCell.setCellStyle(labelStyle);
            
            XSSFCell valueCell = row.createCell(1);
            valueCell.setCellValue(fields[i][1]);
            valueCell.setCellStyle(fieldStyle);
        }
    }
    
    /**
     * 创建任务列表工作表
     */
    private static void createTaskListSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("任务清单");
        
        // 设置列宽
        sheet.setColumnWidth(0, 10 * 256); // 任务ID
        sheet.setColumnWidth(1, 30 * 256); // 任务名称
        sheet.setColumnWidth(2, 15 * 256); // 开始日期
        sheet.setColumnWidth(3, 15 * 256); // 结束日期
        sheet.setColumnWidth(4, 15 * 256); // 负责人
        sheet.setColumnWidth(5, 15 * 256); // 状态
        sheet.setColumnWidth(6, 30 * 256); // 备注
        
        // 创建标题
        XSSFRow titleRow = sheet.createRow(0);
        XSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("项目任务清单");
        XSSFCellStyle titleStyle = workbook.createCellStyle();
        XSSFFont titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        
        // 创建表头
        XSSFRow headerRow = sheet.createRow(2);
        String[] headers = {"任务ID", "任务名称", "开始日期", "结束日期", "负责人", "状态", "备注"};
        XSSFCellStyle headerStyle = createHeaderStyle(workbook);
        
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 添加示例数据和占位符
        String[][] exampleData = {
            {"TASK-001", "{{任务名称}}", "{{开始日期}}", "{{结束日期}}", "{{负责人}}", "{{状态}}", "{{备注}}"},
            {"TASK-002", "需求分析", "2023-06-01", "2023-06-15", "张三", "已完成", "初步需求已确认"},
            {"TASK-003", "系统设计", "2023-06-16", "2023-06-30", "李四", "进行中", "架构设计中"}
        };
        
        XSSFCellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        
        // 添加示例数据行
        for (int i = 0; i < exampleData.length; i++) {
            XSSFRow row = sheet.createRow(i + 3);
            for (int j = 0; j < exampleData[i].length; j++) {
                XSSFCell cell = row.createCell(j);
                cell.setCellValue(exampleData[i][j]);
                cell.setCellStyle(dataStyle);
            }
        }
        
        // 创建状态的数据验证
        XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(sheet);
        XSSFDataValidationConstraint constraint = (XSSFDataValidationConstraint)
                validationHelper.createExplicitListConstraint(new String[]{"未开始", "进行中", "已完成", "延期", "已取消"});
        
        CellRangeAddressList addressList = new CellRangeAddressList(3, 100, 5, 5);
        XSSFDataValidation validation = (XSSFDataValidation) validationHelper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }
    
    /**
     * 创建资源分配工作表
     */
    private static void createResourceAllocationSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("资源分配");
        
        // 设置列宽
        sheet.setColumnWidth(0, 15 * 256); // 资源ID
        sheet.setColumnWidth(1, 20 * 256); // 资源名称
        sheet.setColumnWidth(2, 15 * 256); // 类型
        sheet.setColumnWidth(3, 15 * 256); // 分配日期
        sheet.setColumnWidth(4, 15 * 256); // 分配人
        sheet.setColumnWidth(5, 15 * 256); // 任务ID
        sheet.setColumnWidth(6, 25 * 256); // 备注
        
        // 创建标题
        XSSFRow titleRow = sheet.createRow(0);
        XSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("项目资源分配");
        XSSFCellStyle titleStyle = workbook.createCellStyle();
        XSSFFont titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        
        // 创建表头
        XSSFRow headerRow = sheet.createRow(2);
        String[] headers = {"资源ID", "资源名称", "类型", "分配日期", "分配人", "任务ID", "备注"};
        XSSFCellStyle headerStyle = createHeaderStyle(workbook);
        
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 添加示例数据和占位符
        String[][] exampleData = {
            {"RES-001", "{{资源名称}}", "{{类型}}", "{{分配日期}}", "{{分配人}}", "{{任务ID}}", "{{备注}}"},
            {"RES-002", "开发服务器", "硬件", "2023-06-01", "王五", "TASK-002", "开发环境搭建"},
            {"RES-003", "设计团队", "人力", "2023-06-16", "赵六", "TASK-003", "UI设计分配"}
        };
        
        XSSFCellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        
        // 添加示例数据行
        for (int i = 0; i < exampleData.length; i++) {
            XSSFRow row = sheet.createRow(i + 3);
            for (int j = 0; j < exampleData[i].length; j++) {
                XSSFCell cell = row.createCell(j);
                cell.setCellValue(exampleData[i][j]);
                cell.setCellStyle(dataStyle);
            }
        }
        
        // 创建类型的数据验证
        XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(sheet);
        XSSFDataValidationConstraint constraint = (XSSFDataValidationConstraint)
                validationHelper.createExplicitListConstraint(new String[]{"人力", "硬件", "软件", "资金", "场地", "其他"});
        
        CellRangeAddressList addressList = new CellRangeAddressList(3, 100, 2, 2);
        XSSFDataValidation validation = (XSSFDataValidation) validationHelper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }
    
    /**
     * 创建进度跟踪工作表
     */
    private static void createProgressTrackingSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("进度跟踪");
        
        // 设置列宽
        sheet.setColumnWidth(0, 15 * 256); // 日期
        sheet.setColumnWidth(1, 25 * 256); // 里程碑名称
        sheet.setColumnWidth(2, 15 * 256); // 计划完成日期
        sheet.setColumnWidth(3, 15 * 256); // 实际完成日期
        sheet.setColumnWidth(4, 10 * 256); // 完成状态
        sheet.setColumnWidth(5, 20 * 256); // 备注
        
        // 创建表头
        XSSFRow headerRow = sheet.createRow(0);
        String[] headers = {"日期", "里程碑名称", "计划完成日期", "实际完成日期", "完成状态", "备注"};
        
        XSSFCellStyle headerStyle = createHeaderStyle(workbook);
        
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 添加一个示例行，包含占位符
        XSSFRow exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("{{里程碑.日期}}");
        exampleRow.createCell(1).setCellValue("{{里程碑.名称}}");
        exampleRow.createCell(2).setCellValue("{{里程碑.计划完成日期}}");
        exampleRow.createCell(3).setCellValue("{{里程碑.实际完成日期}}");
        exampleRow.createCell(4).setCellValue("{{里程碑.完成状态}}");
        exampleRow.createCell(5).setCellValue("{{里程碑.备注}}");
        
        // 创建数据有效性列表（状态列）
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint)
                dvHelper.createExplicitListConstraint(new String[]{"未开始", "进行中", "已完成", "已延期"});
        
        CellRangeAddressList addressList = new CellRangeAddressList(1, 100, 4, 4);
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }
    
    /**
     * 创建风险管理工作表
     */
    private static void createRiskManagementSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("风险管理");
        
        // 设置列宽
        int[] columnWidths = {10, 30, 15, 15, 30, 15, 15};
        for (int i = 0; i < columnWidths.length; i++) {
            sheet.setColumnWidth(i, columnWidths[i] * 256);
        }
        
        // 创建表头
        XSSFRow headerRow = sheet.createRow(0);
        String[] headers = {"风险ID", "风险描述", "可能性", "影响程度", "应对措施", "责任人", "状态"};
        
        XSSFCellStyle headerStyle = createHeaderStyle(workbook);
        
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 添加一个示例行，包含占位符
        XSSFRow exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("{{风险.ID}}");
        exampleRow.createCell(1).setCellValue("{{风险.描述}}");
        exampleRow.createCell(2).setCellValue("{{风险.可能性}}");
        exampleRow.createCell(3).setCellValue("{{风险.影响程度}}");
        exampleRow.createCell(4).setCellValue("{{风险.应对措施}}");
        exampleRow.createCell(5).setCellValue("{{风险.责任人}}");
        exampleRow.createCell(6).setCellValue("{{风险.状态}}");
        
        // 添加一些空行
        for (int i = 2; i <= 10; i++) {
            sheet.createRow(i);
        }
        
        // 创建数据有效性列表（可能性列）
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        XSSFDataValidationConstraint possibilityConstraint = (XSSFDataValidationConstraint)
                dvHelper.createExplicitListConstraint(new String[]{"低", "中", "高"});
        
        CellRangeAddressList possibilityAddressList = new CellRangeAddressList(1, 100, 2, 2);
        XSSFDataValidation possibilityValidation = (XSSFDataValidation) dvHelper.createValidation(
                possibilityConstraint, possibilityAddressList);
        possibilityValidation.setShowErrorBox(true);
        sheet.addValidationData(possibilityValidation);
        
        // 创建数据有效性列表（影响程度列）
        XSSFDataValidationConstraint impactConstraint = (XSSFDataValidationConstraint)
                dvHelper.createExplicitListConstraint(new String[]{"低", "中", "高"});
        
        CellRangeAddressList impactAddressList = new CellRangeAddressList(1, 100, 3, 3);
        XSSFDataValidation impactValidation = (XSSFDataValidation) dvHelper.createValidation(
                impactConstraint, impactAddressList);
        impactValidation.setShowErrorBox(true);
        sheet.addValidationData(impactValidation);
        
        // 创建数据有效性列表（状态列）
        XSSFDataValidationConstraint statusConstraint = (XSSFDataValidationConstraint)
                dvHelper.createExplicitListConstraint(new String[]{"已识别", "正在监控", "已解决", "已关闭"});
        
        CellRangeAddressList statusAddressList = new CellRangeAddressList(1, 100, 6, 6);
        XSSFDataValidation statusValidation = (XSSFDataValidation) dvHelper.createValidation(
                statusConstraint, statusAddressList);
        statusValidation.setShowErrorBox(true);
        sheet.addValidationData(statusValidation);
    }
    
    /**
     * 创建财务预算工作表
     */
    private static void createBudgetSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("财务预算");
        
        // 设置列宽
        int[] columnWidths = {30, 15, 15, 15, 20};
        for (int i = 0; i < columnWidths.length; i++) {
            sheet.setColumnWidth(i, columnWidths[i] * 256);
        }
        
        // 创建表头
        XSSFRow headerRow = sheet.createRow(0);
        String[] headers = {"费用项目", "预算金额", "实际支出", "剩余金额", "备注"};
        
        XSSFCellStyle headerStyle = createHeaderStyle(workbook);
        
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 添加费用类别，使用占位符
        String[] expenseCategories = {
            "人力成本",
            "设备采购",
            "软件许可",
            "培训费用",
            "差旅费用",
            "会议费用",
            "外包服务",
            "其他费用"
        };
        
        XSSFCellStyle categoryStyle = workbook.createCellStyle();
        XSSFFont categoryFont = workbook.createFont();
        categoryFont.setBold(true);
        categoryStyle.setFont(categoryFont);
        
        // 添加费用类别和计算公式，使用占位符
        for (int i = 0; i < expenseCategories.length; i++) {
            XSSFRow row = sheet.createRow(i + 1);
            
            XSSFCell categoryCell = row.createCell(0);
            categoryCell.setCellValue(expenseCategories[i]);
            categoryCell.setCellStyle(categoryStyle);
            
            // 添加占位符
            row.createCell(1).setCellValue("{{预算." + expenseCategories[i] + ".预算}}");
            row.createCell(2).setCellValue("{{预算." + expenseCategories[i] + ".支出}}");
            
            // 设置公式计算剩余金额 (预算-支出)
            XSSFCell remainingCell = row.createCell(3);
            remainingCell.setCellFormula(String.format("B%d-C%d", i + 2, i + 2));
            
            // 添加备注占位符
            row.createCell(4).setCellValue("{{预算." + expenseCategories[i] + ".备注}}");
        }
        
        // 添加合计行
        XSSFRow totalRow = sheet.createRow(expenseCategories.length + 2);
        XSSFCell totalLabelCell = totalRow.createCell(0);
        totalLabelCell.setCellValue("合计");
        totalLabelCell.setCellStyle(categoryStyle);
        
        // 设置合计公式
        XSSFCell totalBudgetCell = totalRow.createCell(1);
        totalBudgetCell.setCellFormula(String.format("SUM(B2:B%d)", expenseCategories.length + 1));
        
        XSSFCell totalActualCell = totalRow.createCell(2);
        totalActualCell.setCellFormula(String.format("SUM(C2:C%d)", expenseCategories.length + 1));
        
        XSSFCell totalRemainingCell = totalRow.createCell(3);
        totalRemainingCell.setCellFormula(String.format("SUM(D2:D%d)", expenseCategories.length + 1));
        
        // 设置数字格式为货币
        XSSFCellStyle currencyStyle = workbook.createCellStyle();
        XSSFDataFormat format = workbook.createDataFormat();
        currencyStyle.setDataFormat(format.getFormat("¥#,##0.00"));
        
        // 应用货币格式到金额列
        for (int i = 1; i <= expenseCategories.length + 3; i++) {
            XSSFRow row = sheet.getRow(i);
            if (row != null) {
                for (int j = 1; j <= 3; j++) {
                    XSSFCell cell = row.getCell(j);
                    if (cell != null) {
                        cell.setCellStyle(currencyStyle);
                    }
                }
            }
        }
    }
    
    /**
     * 创建表头样式
     */
    private static XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
} 