package com.timelordtty.docgen.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.timelordtty.AppLogger;

/**
 * Excel模板服务类，提供Excel表格模板的读取、解析和生成功能
 */
public class ExcelTemplateService {

    /**
     * 读取Excel表格内容
     * 
     * @param filePath Excel文件路径
     * @return 表格内容列表
     * @throws IOException IO异常
     */
    public List<List<String>> readExcelContent(String filePath) throws IOException {
        List<List<String>> result = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {
            
            // 读取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            
            // 遍历行
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    result.add(new ArrayList<>());
                    continue;
                }
                
                List<String> rowData = new ArrayList<>();
                
                // 遍历列
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) {
                        rowData.add("");
                        continue;
                    }
                    
                    // 根据单元格类型获取值
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
                            rowData.add(cell.getCellFormula());
                            break;
                        default:
                            rowData.add("");
                    }
                }
                
                result.add(rowData);
            }
        }
        
        return result;
    }
    
    /**
     * 保存Excel模板
     * 
     * @param filePath 文件路径
     * @param data 表格数据
     * @throws IOException IO异常
     */
    public void saveExcelTemplate(String filePath, List<List<String>> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            
            // 写入数据
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i);
                List<String> rowData = data.get(i);
                
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(rowData.get(j));
                }
            }
            
            // 保存文件
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
        
        AppLogger.info("保存Excel模板: " + filePath);
    }
    
    /**
     * 生成模板
     * 
     * @param outputPath 输出路径
     * @param data 模板数据
     * @throws IOException IO异常
     */
    public void generateTemplate(String outputPath, List<List<String>> data) throws IOException {
        saveExcelTemplate(outputPath, data);
    }
    
    /**
     * 根据模板和数据生成文档
     * 
     * @param templatePath 模板路径
     * @param outputPath 输出路径
     * @param fieldDataMap 普通字段数据
     * @param listFieldDataMap 列表字段数据
     * @throws IOException IO异常
     */
    public void generateDocument(
            String templatePath, 
            String outputPath, 
            Map<String, String> fieldDataMap,
            Map<String, List<Map<String, String>>> listFieldDataMap) throws IOException {
        
        try (FileInputStream fis = new FileInputStream(templatePath);
             Workbook workbook = WorkbookFactory.create(fis)) {
            
            // 处理所有工作表
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                processSheet(sheet, fieldDataMap, listFieldDataMap);
            }
            
            // 保存生成的文档
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        }
        
        AppLogger.info("生成Excel文档: " + outputPath);
    }
    
    /**
     * 处理工作表中的占位符
     * 
     * @param sheet 工作表
     * @param fieldDataMap 字段数据
     * @param listFieldDataMap 列表数据
     */
    private void processSheet(
            Sheet sheet, 
            Map<String, String> fieldDataMap,
            Map<String, List<Map<String, String>>> listFieldDataMap) {
        
        // 遍历所有行
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            
            // 遍历所有列
            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    continue;
                }
                
                // 只处理字符串类型的单元格
                if (cell.getCellType() != CellType.STRING) {
                    continue;
                }
                
                String cellValue = cell.getStringCellValue();
                if (cellValue == null || cellValue.isEmpty()) {
                    continue;
                }
                
                // 替换普通字段占位符
                boolean modified = false;
                String newValue = cellValue;
                
                // 处理普通字段
                for (Map.Entry<String, String> entry : fieldDataMap.entrySet()) {
                    String placeholder = "{{" + entry.getKey() + "}}";
                    if (newValue.contains(placeholder)) {
                        String value = entry.getValue() != null ? entry.getValue() : "";
                        newValue = newValue.replace(placeholder, value);
                        modified = true;
                    }
                }
                
                // 处理列表字段 (单元格中的列表字段，不是整行的列表)
                for (Map.Entry<String, List<Map<String, String>>> entry : listFieldDataMap.entrySet()) {
                    String listName = entry.getKey();
                    for (Map<String, String> item : entry.getValue()) {
                        for (Map.Entry<String, String> field : item.entrySet()) {
                            String placeholder = "{{" + listName + "." + field.getKey() + "}}";
                            if (newValue.contains(placeholder)) {
                                String value = field.getValue() != null ? field.getValue() : "";
                                newValue = newValue.replace(placeholder, value);
                                modified = true;
                            }
                        }
                    }
                }
                
                // 更新单元格值
                if (modified) {
                    cell.setCellValue(newValue);
                }
            }
        }
        
        // 查找并处理列表行区域
        processListSections(sheet, listFieldDataMap);
    }

    /**
     * 处理列表行区域
     * 
     * @param sheet 工作表
     * @param listFieldDataMap 列表数据
     */
    private void processListSections(Sheet sheet, Map<String, List<Map<String, String>>> listFieldDataMap) {
        // 遍历查找列表开始和结束标记
        List<ListSectionInfo> listSections = findListSections(sheet);
        
        // 从后往前处理列表部分，避免插入行时影响后面的行索引
        for (int i = listSections.size() - 1; i >= 0; i--) {
            ListSectionInfo section = listSections.get(i);
            String listName = section.getListName();
            
            // 检查列表数据是否存在
            if (!listFieldDataMap.containsKey(listName)) {
                continue;
            }
            
            List<Map<String, String>> listItems = listFieldDataMap.get(listName);
            
            // 这里需要从后往前删除，否则会影响行索引
            // 先删除模板行（包括开始和结束标记行）
            for (int rowIndex = section.getEndRow(); rowIndex >= section.getStartRow(); rowIndex--) {
                sheet.removeRow(sheet.getRow(rowIndex));
                if (rowIndex < sheet.getLastRowNum()) {
                    sheet.shiftRows(rowIndex + 1, sheet.getLastRowNum(), -1);
                }
            }
            
            // 记录模板行的样式
            int templateRowIndex = section.getTemplateRow();
            
            // 获取模板行
            Row templateRow = sheet.getRow(templateRowIndex);
            if (templateRow == null) {
                continue;
            }
            
            // 对每个列表项插入新行
            int currentRowIndex = section.getStartRow();
            
            // 插入列表数据行
            for (Map<String, String> item : listItems) {
                // 创建新行
                Row newRow = sheet.createRow(currentRowIndex);
                
                // 复制模板行的单元格
                for (int cellIndex = 0; cellIndex < templateRow.getLastCellNum(); cellIndex++) {
                    Cell templateCell = templateRow.getCell(cellIndex);
                    if (templateCell == null) {
                        continue;
                    }
                    
                    // 创建新单元格
                    Cell newCell = newRow.createCell(cellIndex);
                    
                    // 复制样式
                    CellStyle newStyle = sheet.getWorkbook().createCellStyle();
                    newStyle.cloneStyleFrom(templateCell.getCellStyle());
                    newCell.setCellStyle(newStyle);
                    
                    // 获取模板单元格的值并替换占位符
                    if (templateCell.getCellType() == CellType.STRING) {
                        String value = templateCell.getStringCellValue();
                        
                        // 替换列表字段占位符
                        for (Map.Entry<String, String> entry : item.entrySet()) {
                            String placeholder = "{{" + listName + "." + entry.getKey() + "}}";
                            if (value.contains(placeholder)) {
                                String fieldValue = entry.getValue() != null ? entry.getValue() : "";
                                value = value.replace(placeholder, fieldValue);
                            }
                        }
                        
                        newCell.setCellValue(value);
                    } else {
                        // 复制其他类型的单元格值
                        switch (templateCell.getCellType()) {
                            case NUMERIC:
                                newCell.setCellValue(templateCell.getNumericCellValue());
                                break;
                            case BOOLEAN:
                                newCell.setCellValue(templateCell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                newCell.setCellFormula(templateCell.getCellFormula());
                                break;
                            default:
                                newCell.setCellValue("");
                        }
                    }
                }
                
                currentRowIndex++;
            }
        }
    }

    /**
     * 查找工作表中的列表区域
     * 
     * @param sheet 工作表
     * @return 列表区域信息
     */
    private List<ListSectionInfo> findListSections(Sheet sheet) {
        List<ListSectionInfo> result = new ArrayList<>();
        
        // 遍历工作表的所有行
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            
            // 查找列表开始标记 {{#listName}}
            String listName = findListStartMarker(row);
            if (listName != null) {
                int startRow = i;
                int templateRow = i + 1; // 模板行通常在开始标记的下一行
                
                // 查找列表结束标记 {{/listName}}
                int endRow = findListEndMarker(sheet, i + 1, listName);
                
                if (endRow > startRow) {
                    // 找到了完整的列表区域
                    result.add(new ListSectionInfo(listName, startRow, templateRow, endRow));
                    
                    // 跳过当前列表区域
                    i = endRow;
                }
            }
        }
        
        return result;
    }

    /**
     * 在行中查找列表开始标记
     * 
     * @param row 行
     * @return 列表名称，如果没有找到则返回null
     */
    private String findListStartMarker(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue();
                // 匹配模式 {{#listName}}
                if (value.matches(".*\\{\\{#([^}]+)\\}\\}.*")) {
                    return value.replaceAll(".*\\{\\{#([^}]+)\\}\\}.*", "$1");
                }
            }
        }
        return null;
    }

    /**
     * 从指定行开始查找列表结束标记
     * 
     * @param sheet 工作表
     * @param startRow 开始行索引
     * @param listName 列表名称
     * @return 结束行索引，如果没有找到则返回-1
     */
    private int findListEndMarker(Sheet sheet, int startRow, String listName) {
        for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            
            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String value = cell.getStringCellValue();
                    // 匹配模式 {{/listName}}
                    if (value.contains("{{/" + listName + "}}")) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 列表区域信息类
     */
    private static class ListSectionInfo {
        private final String listName;
        private final int startRow;
        private final int templateRow;
        private final int endRow;
        
        public ListSectionInfo(String listName, int startRow, int templateRow, int endRow) {
            this.listName = listName;
            this.startRow = startRow;
            this.templateRow = templateRow;
            this.endRow = endRow;
        }
        
        public String getListName() {
            return listName;
        }
        
        public int getStartRow() {
            return startRow;
        }
        
        public int getTemplateRow() {
            return templateRow;
        }
        
        public int getEndRow() {
            return endRow;
        }
    }

    /**
     * 创建一个新的Excel模板
     * 
     * @param outputPath 输出路径
     * @param sheets 工作表信息，键为工作表名称，值为列名列表
     * @throws IOException IO异常
     */
    public void createTemplate(String outputPath, Map<String, List<String>> sheets) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建工作表和表头
            for (Map.Entry<String, List<String>> entry : sheets.entrySet()) {
                String sheetName = entry.getKey();
                List<String> columns = entry.getValue();
                
                Sheet sheet = workbook.createSheet(sheetName);
                
                // 创建表头样式
                CellStyle headerStyle = createHeaderStyle(workbook);
                
                // 创建表头行
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < columns.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns.get(i));
                    cell.setCellStyle(headerStyle);
                    sheet.setColumnWidth(i, 256 * 15); // 设置列宽
                }
                
                // 设置过滤区域
                if (!columns.isEmpty()) {
                    sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, columns.size() - 1));
                }
            }
            
            // 保存模板
            try (FileOutputStream outputStream = new FileOutputStream(outputPath)) {
                workbook.write(outputStream);
            }
            
            AppLogger.info("创建Excel模板: " + outputPath);
        }
    }
    
    /**
     * 创建表头单元格样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置对齐方式
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
     * 根据模板填充数据
     * 
     * @param templatePath 模板路径
     * @param outputPath 输出路径
     * @param sheetData 工作表数据，键为工作表名称，值为行数据列表
     * @throws IOException IO异常
     */
    public void fillTemplate(String templatePath, String outputPath, 
            Map<String, List<Map<String, Object>>> sheetData) throws IOException {
        
        try (FileInputStream fis = new FileInputStream(templatePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            // 创建单元格样式
            CellStyle dateCellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));
            
            // 处理每个工作表
            for (Map.Entry<String, List<Map<String, Object>>> entry : sheetData.entrySet()) {
                String sheetName = entry.getKey();
                List<Map<String, Object>> rowDataList = entry.getValue();
                
                // 获取工作表
                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    AppLogger.warning("工作表不存在: " + sheetName);
                    continue;
                }
                
                // 获取表头行和列名
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    AppLogger.warning("表头行不存在: " + sheetName);
                    continue;
                }
                
                // 填充数据行
                int rowNum = 1; // 从第二行开始（第一行是表头）
                for (Map<String, Object> rowData : rowDataList) {
                    Row dataRow = sheet.createRow(rowNum++);
                    
                    // 遍历表头列，获取列名并填充数据
                    for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                        Cell headerCell = headerRow.getCell(i);
                        if (headerCell == null) continue;
                        
                        // 获取列名和相应的数据
                        String columnName = headerCell.getStringCellValue();
                        Object value = rowData.get(columnName);
                        if (value == null) continue;
                        
                        // 创建数据单元格
                        Cell dataCell = dataRow.createCell(i);
                        
                        // 根据数据类型设置单元格值
                        setCellValueByType(dataCell, value, dateCellStyle);
                    }
                }
                
                // 调整列宽以适应内容
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    sheet.autoSizeColumn(i);
                }
            }
            
            // 保存填充后的工作簿
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
            
            AppLogger.info("填充Excel模板: " + outputPath);
        }
    }
    
    /**
     * 根据值类型设置单元格值
     */
    private void setCellValueByType(Cell cell, Object value, CellStyle dateCellStyle) {
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
            cell.setCellStyle(dateCellStyle);
        } else {
            cell.setCellValue(value.toString());
        }
    }
    
    /**
     * 读取Excel数据
     * 
     * @param filePath Excel文件路径
     * @return 工作表数据，键为工作表名称，值为行数据列表
     * @throws IOException IO异常
     */
    public Map<String, List<Map<String, Object>>> readExcelData(String filePath) throws IOException {
        Map<String, List<Map<String, Object>>> result = new java.util.HashMap<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {
            
            // 遍历工作表
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                
                List<Map<String, Object>> sheetData = new java.util.ArrayList<>();
                
                // 获取表头行和列名
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) continue;
                
                // 读取表头列名
                List<String> headers = new java.util.ArrayList<>();
                for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                    Cell cell = headerRow.getCell(j);
                    headers.add(cell != null ? cell.getStringCellValue() : "列" + (j + 1));
                }
                
                // 读取数据行
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row dataRow = sheet.getRow(rowIndex);
                    if (dataRow == null) continue;
                    
                    Map<String, Object> rowData = new java.util.HashMap<>();
                    
                    // 读取每个单元格的值
                    for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
                        Cell cell = dataRow.getCell(colIndex);
                        if (cell == null) continue;
                        
                        String header = headers.get(colIndex);
                        Object value = getCellValue(cell);
                        
                        rowData.put(header, value);
                    }
                    
                    // 添加行数据
                    if (!rowData.isEmpty()) {
                        sheetData.add(rowData);
                    }
                }
                
                // 添加工作表数据
                if (!sheetData.isEmpty()) {
                    result.put(sheetName, sheetData);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 获取单元格值
     */
    private Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double value = cell.getNumericCellValue();
                    // 如果是整数，返回Integer
                    if (value == Math.floor(value)) {
                        return (int) value;
                    }
                    return value;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                // 获取公式计算结果
                switch (cell.getCachedFormulaResultType()) {
                    case STRING:
                        return cell.getStringCellValue();
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            return cell.getDateCellValue();
                        } else {
                            return cell.getNumericCellValue();
                        }
                    case BOOLEAN:
                        return cell.getBooleanCellValue();
                    default:
                        return null;
                }
            default:
                return null;
        }
    }
    
    /**
     * 将列表数据转换为Map格式的数据，以便于填充模板
     * 
     * @param data 列表数据
     * @param keyField 作为键的字段名称
     * @return 转换后的Map格式数据
     */
    public Map<String, Object> convertToMap(List<Map<String, Object>> data, String keyField) {
        Map<String, Object> result = new java.util.HashMap<>();
        
        for (Map<String, Object> item : data) {
            Object key = item.get(keyField);
            if (key != null) {
                result.put(key.toString(), item);
            }
        }
        
        return result;
    }
} 