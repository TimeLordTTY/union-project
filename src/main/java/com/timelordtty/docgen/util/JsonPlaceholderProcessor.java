package com.timelordtty.docgen.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.timelordtty.AppLogger;

/**
 * JSON占位符处理器
 * 用于处理JSON数据文件中的占位符，以及生成示例JSON数据文件
 */
public class JsonPlaceholderProcessor {
    
    // 变量占位符正则表达式 ${variable} 或 {{variable}}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}|\\{\\{([^}]+)\\}\\}");
    
    /**
     * 处理文本中的占位符，用数据映射中的值替换
     * 
     * @param text 包含占位符的文本
     * @param dataMap 数据映射
     * @return 替换后的文本
     */
    public static String processPlaceholders(String text, Map<String, Object> dataMap) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            // 获取占位符中的键 - 支持两种格式
            String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            
            if (key != null) {
                // 处理嵌套路径，如 user.name
                Object value = getNestedValue(dataMap, key);
                String replacement = (value != null) ? value.toString() : "";
                AppLogger.debug("替换变量：" + matcher.group() + " -> " + replacement);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 获取嵌套数据中的值，支持点号分隔的路径，如 user.address.city
     * 
     * @param dataMap 数据映射
     * @param key 键路径
     * @return 找到的值，未找到时返回null
     */
    @SuppressWarnings("unchecked")
    private static Object getNestedValue(Map<String, Object> dataMap, String key) {
        if (key == null || key.isEmpty() || dataMap == null) {
            return null;
        }
        
        // 直接获取简单键
        if (!key.contains(".")) {
            return dataMap.get(key);
        }
        
        // 处理嵌套路径
        String[] parts = key.split("\\.");
        Object current = dataMap;
        
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null; // 路径中断
            }
            
            if (current == null) {
                return null;
            }
        }
        
        return current;
    }
    
    /**
     * 创建示例JSON数据文件
     * 
     * @param outputPath 输出文件路径
     * @throws IOException 如果创建文件失败
     */
    public static void createSampleJsonData(String outputPath) throws IOException {
        try {
            AppLogger.info("开始创建JSON示例数据: " + outputPath);
            
            // 创建示例数据
            Map<String, Object> sampleData = new HashMap<>();
            
            // 客户信息
            Map<String, Object> client = new HashMap<>();
            client.put("name", "张三");
            client.put("phone", "13800138000");
            client.put("address", "北京市海淀区中关村大街1号");
            sampleData.put("client", client);
            
            // 订单信息
            Map<String, Object> order = new HashMap<>();
            order.put("id", "ORD-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-001");
            order.put("delivery_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))); // 7天后
            
            // 订单明细
            Map<String, Object> item1 = new HashMap<>();
            item1.put("index", 1);
            item1.put("name", "笔记本电脑");
            item1.put("quantity", 1);
            item1.put("price", 6999);
            item1.put("subtotal", 6999);
            
            Map<String, Object> item2 = new HashMap<>();
            item2.put("index", 2);
            item2.put("name", "无线鼠标");
            item2.put("quantity", 2);
            item2.put("price", 99);
            item2.put("subtotal", 198);
            
            Map<String, Object> item3 = new HashMap<>();
            item3.put("index", 3);
            item3.put("name", "显示器");
            item3.put("quantity", 1);
            item3.put("price", 1299);
            item3.put("subtotal", 1299);
            
            Object[] items = {item1, item2, item3};
            order.put("items", items);
            order.put("total_amount", 8496);
            
            sampleData.put("order", order);
            
            // 公司信息
            Map<String, Object> company = new HashMap<>();
            company.put("name", "优秀科技有限公司");
            company.put("service_phone", "400-800-8888");
            sampleData.put("company", company);
            
            // 其他信息
            sampleData.put("today", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            sampleData.put("notes", "如有特殊需求，请提前与客服联系。");
            
            // 将数据写入JSON文件
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // 美化输出
            
            // 确保输出目录存在
            File outputFile = new File(outputPath);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            
            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                String jsonContent = objectMapper.writeValueAsString(sampleData);
                fos.write(jsonContent.getBytes(StandardCharsets.UTF_8));
                AppLogger.info("JSON示例数据创建成功: " + outputPath);
            }
        } catch (IOException e) {
            AppLogger.error("创建JSON示例数据失败: " + e.getMessage(), e);
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
            String outputPath = args.length > 0 ? args[0] : "doc-generator/src/main/resources/templates/sample_data.json";
            createSampleJsonData(outputPath);
            System.out.println("成功创建JSON示例数据: " + outputPath);
        } catch (Exception e) {
            AppLogger.error("测试占位符处理时发生错误", e);
            e.printStackTrace();
        }
    }
} 