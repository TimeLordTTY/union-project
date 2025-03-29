package com.timelordtty.docgen.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Excel模板服务
 */
public class ExcelTemplateService {
    
    /**
     * 读取Excel内容
     * @param filePath 文件路径
     * @return Excel内容，按行列表形式返回
     * @throws Exception 异常
     */
    public List<List<String>> readExcelContent(String filePath) throws Exception {
        List<List<String>> data = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // 读取所有行
            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            rowData.add(cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            rowData.add(String.valueOf(cell.getNumericCellValue()));
                            break;
                        case BOOLEAN:
                            rowData.add(String.valueOf(cell.getBooleanCellValue()));
                            break;
                        case FORMULA:
                            try {
                                rowData.add(cell.getStringCellValue());
                            } catch (IllegalStateException e) {
                                rowData.add(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        default:
                            rowData.add("");
                    }
                }
                data.add(rowData);
            }
            
            AppLogger.info("成功读取Excel文件: " + filePath);
        } catch (Exception e) {
            AppLogger.error("读取Excel文件失败: " + filePath, e);
            throw e;
        }
        
        return data;
    }
    
    /**
     * 保存Excel模板
     * @param filePath 文件路径
     * @param data Excel内容
     * @throws Exception 异常
     */
    public void saveExcelTemplate(String filePath, List<List<String>> data) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("模板");
            
            // 创建行和单元格
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i);
                List<String> rowData = data.get(i);
                
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(rowData.get(j));
                }
            }
            
            // 冻结首行
            if (!data.isEmpty()) {
                sheet.createFreezePane(0, 1);
            }
            
            // 自动调整列宽
            for (int i = 0; i < getMaxColumnCount(data); i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 保存文件
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
            
            AppLogger.info("成功保存Excel模板: " + filePath);
        } catch (Exception e) {
            AppLogger.error("保存Excel模板失败: " + filePath, e);
            throw e;
        }
    }
    
    /**
     * 保存Excel文档
     * @param filePath 文件路径
     * @param data Excel内容
     * @throws Exception 异常
     */
    public void saveExcelDocument(String filePath, List<List<String>> data) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("数据");
            
            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // 创建行和单元格
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i);
                List<String> rowData = data.get(i);
                
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    String value = rowData.get(j);
                    
                    // 尝试将数字字符串转换为数字
                    try {
                        double numValue = Double.parseDouble(value);
                        cell.setCellValue(numValue);
                    } catch (NumberFormatException e) {
                        cell.setCellValue(value);
                    }
                    
                    // 为表头设置样式
                    if (i == 0) {
                        cell.setCellStyle(headerStyle);
                    }
                }
            }
            
            // 冻结首行
            if (!data.isEmpty()) {
                sheet.createFreezePane(0, 1);
            }
            
            // 自动调整列宽
            for (int i = 0; i < getMaxColumnCount(data); i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 保存文件
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
            
            AppLogger.info("成功保存Excel文档: " + filePath);
        } catch (Exception e) {
            AppLogger.error("保存Excel文档失败: " + filePath, e);
            throw e;
        }
    }
    
    /**
     * 生成Excel模板
     * @param filePath 文件路径
     * @param fields 字段列表
     * @throws Exception 异常
     */
    public void generateExcelTemplate(String filePath, List<TemplateField> fields) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("模板");
            
            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // 分离对象字段和列表字段
            List<TemplateField> objectFields = new ArrayList<>();
            List<TemplateField> listFields = new ArrayList<>();
            Map<String, List<TemplateField>> listItemFields = new HashMap<>();
            
            for (TemplateField field : fields) {
                if (field.isList()) {
                    listFields.add(field);
                    listItemFields.put(field.getName(), new ArrayList<>());
                } else if (field.getName().contains(".")) {
                    // 这是列表项字段
                    String listName = field.getName().substring(0, field.getName().indexOf("."));
                    List<TemplateField> itemFields = listItemFields.getOrDefault(listName, new ArrayList<>());
                    itemFields.add(field);
                    listItemFields.put(listName, itemFields);
                } else {
                    objectFields.add(field);
                }
            }
            
            // 创建说明工作表
            Sheet infoSheet = workbook.createSheet("字段说明");
            Row infoTitleRow = infoSheet.createRow(0);
            Cell infoTitleCell = infoTitleCell = infoTitleRow.createCell(0);
            infoTitleCell.setCellValue("字段说明");
            infoTitleCell.setCellStyle(headerStyle);
            
            Row infoHeaderRow = infoSheet.createRow(1);
            infoHeaderRow.createCell(0).setCellValue("字段名称");
            infoHeaderRow.createCell(1).setCellValue("字段类型");
            infoHeaderRow.createCell(2).setCellValue("字段占位符");
            
            int infoRowIndex = 2;
            
            // 添加对象字段说明
            for (TemplateField field : objectFields) {
                Row row = infoSheet.createRow(infoRowIndex++);
                row.createCell(0).setCellValue(field.getName());
                row.createCell(1).setCellValue("普通");
                row.createCell(2).setCellValue("{{" + field.getName() + "}}");
            }
            
            // 添加列表字段说明
            for (TemplateField field : listFields) {
                Row row = infoSheet.createRow(infoRowIndex++);
                row.createCell(0).setCellValue(field.getName());
                row.createCell(1).setCellValue("列表");
                row.createCell(2).setCellValue("{{#" + field.getName() + "}} ... {{/" + field.getName() + "}}");
                
                // 添加列表项字段说明
                List<TemplateField> itemFields = listItemFields.get(field.getName());
                if (itemFields != null && !itemFields.isEmpty()) {
                    for (TemplateField itemField : itemFields) {
                        row = infoSheet.createRow(infoRowIndex++);
                        String itemName = itemField.getName().substring(itemField.getName().indexOf(".") + 1);
                        row.createCell(0).setCellValue("  " + itemName);
                        row.createCell(1).setCellValue("列表项");
                        row.createCell(2).setCellValue("{{" + itemName + "}}");
                    }
                }
            }
            
            // 自动调整说明表格列宽
            for (int i = 0; i < 3; i++) {
                infoSheet.autoSizeColumn(i);
            }
            
            // 创建表头行
            Row headerRow = sheet.createRow(0);
            int columnIndex = 0;
            
            // 添加对象字段列
            for (TemplateField field : objectFields) {
                Cell cell = headerRow.createCell(columnIndex++);
                cell.setCellValue("{{" + field.getName() + "}}");
                cell.setCellStyle(headerStyle);
            }
            
            // 添加列表字段列
            for (TemplateField field : listFields) {
                List<TemplateField> itemFields = listItemFields.get(field.getName());
                if (itemFields != null && !itemFields.isEmpty()) {
                    for (TemplateField itemField : itemFields) {
                        Cell cell = headerRow.createCell(columnIndex++);
                        String itemName = itemField.getName().substring(itemField.getName().indexOf(".") + 1);
                        cell.setCellValue("{{#" + field.getName() + "}}{{" + itemName + "}}{{/" + field.getName() + "}}");
                        cell.setCellStyle(headerStyle);
                    }
                } else {
                    // 如果没有子字段，创建一个列表字段列
                    Cell cell = headerRow.createCell(columnIndex++);
                    cell.setCellValue("{{#" + field.getName() + "}}{{item}}{{/" + field.getName() + "}}");
                    cell.setCellStyle(headerStyle);
                }
            }
            
            // 添加样例数据行
            Row dataRow = sheet.createRow(1);
            columnIndex = 0;
            
            // 添加对象字段样例数据
            for (TemplateField field : objectFields) {
                Cell cell = dataRow.createCell(columnIndex++);
                
                // 根据字段名称生成样例数据
                String fieldName = field.getName().toLowerCase();
                if (fieldName.contains("日期") || fieldName.contains("时间") || fieldName.contains("date") || fieldName.contains("time")) {
                    cell.setCellValue("2023-01-01");
                } else if (fieldName.contains("金额") || fieldName.contains("价格") || fieldName.contains("费用") || 
                           fieldName.contains("amount") || fieldName.contains("price") || fieldName.contains("cost")) {
                    cell.setCellValue(1000.00);
                } else if (fieldName.contains("数量") || fieldName.contains("个数") || fieldName.contains("count") || 
                         fieldName.contains("quantity") || fieldName.contains("number")) {
                    cell.setCellValue(10);
                } else if (fieldName.contains("电话") || fieldName.contains("手机") || fieldName.contains("phone") || 
                         fieldName.contains("mobile") || fieldName.contains("tel")) {
                    cell.setCellValue("13800138000");
                } else if (fieldName.contains("邮箱") || fieldName.contains("email")) {
                    cell.setCellValue("example@example.com");
                } else if (fieldName.contains("地址") || fieldName.contains("address")) {
                    cell.setCellValue("北京市朝阳区");
                } else if (fieldName.contains("姓名") || fieldName.contains("name") || fieldName.contains("客户") || 
                         fieldName.contains("用户") || fieldName.contains("customer") || fieldName.contains("user")) {
                    cell.setCellValue("张三");
                } else {
                    cell.setCellValue("样例数据");
                }
            }
            
            // 添加列表字段样例数据
            for (TemplateField field : listFields) {
                List<TemplateField> itemFields = listItemFields.get(field.getName());
                if (itemFields != null && !itemFields.isEmpty()) {
                    for (TemplateField itemField : itemFields) {
                        Cell cell = dataRow.createCell(columnIndex++);
                        String itemName = itemField.getName().substring(itemField.getName().indexOf(".") + 1).toLowerCase();
                        
                        // 根据字段名称生成样例数据
                        if (itemName.contains("日期") || itemName.contains("时间")) {
                            cell.setCellValue("2023-01-01");
                        } else if (itemName.contains("金额") || itemName.contains("价格") || itemName.contains("费用")) {
                            cell.setCellValue(200.00);
                        } else if (itemName.contains("数量") || itemName.contains("个数")) {
                            cell.setCellValue(2);
                        } else if (itemName.contains("名称") || itemName.contains("标题")) {
                            cell.setCellValue("样例项目");
                        } else {
                            cell.setCellValue("样例数据");
                        }
                    }
                } else {
                    Cell cell = dataRow.createCell(columnIndex++);
                    cell.setCellValue("样例项目");
                }
            }
            
            // 冻结首行
            sheet.createFreezePane(0, 1);
            
            // 自动调整列宽
            for (int i = 0; i < columnIndex; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 保存文件
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
            
            AppLogger.info("成功生成Excel模板: " + filePath);
        } catch (Exception e) {
            AppLogger.error("生成Excel模板失败: " + filePath, e);
            throw e;
        }
    }
    
    /**
     * 获取最大列数
     * @param data 数据
     * @return 最大列数
     */
    private int getMaxColumnCount(List<List<String>> data) {
        int maxColumns = 0;
        for (List<String> row : data) {
            maxColumns = Math.max(maxColumns, row.size());
        }
        return maxColumns;
    }
} 