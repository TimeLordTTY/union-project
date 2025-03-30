package com.timelordtty.docgen.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.timelordtty.AppLogger;

/**
 * Excel测试数据生成器
 * 生成包含客户数据和项目数据的Excel文件
 */
public class ExcelDataGenerator {
    private static final Random random = new Random();
    private static final String[] COMPANY_NAMES = {"领航科技", "智创未来", "云端数据", "星辰技术", "蓝海网络", 
        "盛世信息", "鼎峰科技", "远景数码", "巨天智能", "恒基网络", "鸿运传媒", "太和系统", 
        "金石数据", "华夏网络", "银河科技", "紫光电子", "绿洲软件", "天元信息", "红日科技", "白云网络"};
    
    private static final String[] CONTACT_NAMES = {"张伟", "王芳", "李娜", "赵明", "刘洋", "陈晨", "杨帆", 
        "周静", "吴强", "郑阳", "孙宇", "马超", "朱琳", "胡丽", "林峰", "郭涛", "何雪", "黄海", "徐亮", "高鹏"};
    
    private static final String[] EMAIL_DOMAINS = {"qq.com", "163.com", "126.com", "gmail.com", "outlook.com", 
        "hotmail.com", "sina.com", "sohu.com", "yahoo.com", "foxmail.com"};
    
    private static final String[] CITIES = {"北京", "上海", "广州", "深圳", "南京", "杭州", "成都", "重庆", "武汉", "西安"};
    
    private static final String[] DISTRICTS = {"海淀区", "朝阳区", "静安区", "浦东新区", "天河区", "南山区", "鼓楼区", "西湖区", "武侯区", "江北区"};
    
    private static final String[] STREETS = {"中关村大街", "建国路", "南京西路", "世纪大道", "天河路", "深南大道", "中山路", "西湖大道", "人民南路", "观音桥"};
    
    private static final String[] PROJECT_NAMES = {"智慧城市平台", "云计算基础设施", "大数据分析系统", "AI识别系统", "物联网监控平台",
        "移动支付平台", "客户关系管理系统", "企业资源规划系统", "供应链管理系统", "电子商务平台",
        "内容管理系统", "在线教育平台", "医疗信息系统", "金融交易系统", "视频会议系统"};
    
    private static final String[] PROJECT_MANAGERS = {"张经理", "王总监", "李主管", "赵技术总监", "刘产品经理", 
        "陈项目总监", "杨架构师", "周部门主管", "吴技术专家", "郑项目经理"};
    
    private static final String[] PROJECT_STATUSES = {"未开始", "进行中", "已延期", "已完成", "已取消"};

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
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建客户数据工作表
            Sheet customerSheet = workbook.createSheet("客户数据");
            createCustomerSheet(customerSheet);
            
            // 创建项目数据工作表
            Sheet projectSheet = workbook.createSheet("项目数据");
            createProjectSheet(projectSheet);
            
            // 调整列宽
            for (int i = 0; i < 5; i++) {
                customerSheet.autoSizeColumn(i);
                projectSheet.autoSizeColumn(i);
            }
            
            // 写入文件
            try (FileOutputStream outputStream = new FileOutputStream(outputPath)) {
                workbook.write(outputStream);
            }
        }
    }
    
    /**
     * 创建客户数据工作表
     * 
     * @param sheet 工作表对象
     */
    private static void createCustomerSheet(Sheet sheet) {
        // 创建表头样式
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"客户ID", "公司名称", "联系人", "邮箱", "地址", "电话"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 创建数据行
        for (int i = 0; i < 10; i++) {
            Row dataRow = sheet.createRow(i + 1);
            
            // 客户ID：C开头加4位数字
            dataRow.createCell(0).setCellValue("C" + String.format("%04d", (i + 1) * 10));
            
            // 公司名称
            String companyName = COMPANY_NAMES[i];
            dataRow.createCell(1).setCellValue(companyName);
            
            // 联系人
            String contactName = CONTACT_NAMES[i];
            dataRow.createCell(2).setCellValue(contactName);
            
            // 邮箱：拼音加域名
            String email = getPinyin(contactName) + "@" + EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];
            dataRow.createCell(3).setCellValue(email);
            
            // 地址
            String address = CITIES[i] + DISTRICTS[random.nextInt(DISTRICTS.length)] + 
                            STREETS[random.nextInt(STREETS.length)] + 
                            (random.nextInt(200) + 1) + "号";
            dataRow.createCell(4).setCellValue(address);
            
            // 电话：手机号码
            String phone = "1" + (3 + random.nextInt(7)) + 
                           String.format("%09d", random.nextInt(1000000000));
            dataRow.createCell(5).setCellValue(phone);
        }
    }
    
    /**
     * 创建项目数据工作表
     * 
     * @param sheet 工作表对象
     */
    private static void createProjectSheet(Sheet sheet) {
        // 创建表头样式
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        
        // 创建数据样式
        CellStyle dateStyle = sheet.getWorkbook().createCellStyle();
        CreationHelper createHelper = sheet.getWorkbook().getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"项目ID", "项目名称", "开始日期", "项目经理", "状态", "客户ID", "预计结束日期", "实际结束日期", "项目描述"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 创建数据行
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 0; i < 15; i++) {
            Row dataRow = sheet.createRow(i + 1);
            
            // 项目ID：P开头加5位数字
            dataRow.createCell(0).setCellValue("P" + String.format("%05d", (i + 1) * 100));
            
            // 项目名称
            String projectName = PROJECT_NAMES[i % PROJECT_NAMES.length];
            dataRow.createCell(1).setCellValue(projectName);
            
            // 开始日期：近3个月内的随机日期
            LocalDate startDate = now.minusDays(random.nextInt(90));
            Cell dateCell = dataRow.createCell(2);
            dateCell.setCellValue(startDate.format(formatter));
            dateCell.setCellStyle(dateStyle);
            
            // 项目经理
            String manager = PROJECT_MANAGERS[i % PROJECT_MANAGERS.length];
            dataRow.createCell(3).setCellValue(manager);
            
            // 状态
            String status = PROJECT_STATUSES[random.nextInt(PROJECT_STATUSES.length)];
            dataRow.createCell(4).setCellValue(status);
            
            // 客户ID：关联到客户表
            dataRow.createCell(5).setCellValue("C" + String.format("%04d", (random.nextInt(10) + 1) * 10));
            
            // 预计结束日期
            Cell expectedEndCell = dataRow.createCell(6);
            LocalDate expectedEndDate = startDate.plusDays(random.nextInt(90));
            expectedEndCell.setCellValue(expectedEndDate.format(formatter));
            expectedEndCell.setCellStyle(dateStyle);
            
            // 实际结束日期
            Cell actualEndCell = dataRow.createCell(7);
            if (status.equals("已完成")) {
                actualEndCell.setCellValue(expectedEndDate.format(formatter));
            } else {
                actualEndCell.setCellValue("");
            }
            actualEndCell.setCellStyle(dateStyle);
            
            // 项目描述
            Cell descCell = dataRow.createCell(8);
            descCell.setCellValue("这是一个" + projectName + "项目，由" + manager + "负责管理，计划于" + expectedEndDate.toString() + "完成。");
        }
    }
    
    /**
     * 创建表头样式
     * 
     * @param workbook 工作簿对象
     * @return 单元格样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置背景颜色
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置文字居中
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        
        return style;
    }
    
    /**
     * 简单的拼音转换方法
     * 注：这是一个非常简化的版本，仅用于演示
     * 
     * @param name 中文名字
     * @return 拼音结果
     */
    private static String getPinyin(String name) {
        String[] pinyinMap = {
            "zhangwei", "wangfang", "lina", "zhaoming", "liuyang", "chenchan", "yangfan", 
            "zhoujing", "wuqiang", "zhengyang", "sunyu", "machao", "zhulin", "huli", 
            "linfeng", "guotao", "hexue", "huanghai", "xuliang", "gaopeng"
        };
        
        for (int i = 0; i < CONTACT_NAMES.length; i++) {
            if (CONTACT_NAMES[i].equals(name)) {
                return pinyinMap[i];
            }
        }
        
        // 默认返回
        return "user" + random.nextInt(1000);
    }
} 