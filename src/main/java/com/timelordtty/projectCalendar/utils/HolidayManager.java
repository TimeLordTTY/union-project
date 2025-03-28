package com.timelordtty.projectCalendar.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timelordtty.AppLogger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * 节假日管理器
 * 提供节假日数据的获取、缓存和访问功能
 */
public class HolidayManager {

    private static final String CACHE_DIR = "data/holidays";
    private static final String API_URL = "https://timor.tech/api/holiday/year/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static HolidayManager instance;
    
    // 节假日数据缓存
    private Map<String, String> holidayMap = new HashMap<>();
    // 特殊日期缓存（不作为节假日但需要特殊处理的日期）
    private Map<String, String> specialDateMap = new HashMap<>();
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 获取HolidayManager实例
     * @return HolidayManager实例
     */
    public static HolidayManager getInstance() {
        if (instance == null) {
            instance = new HolidayManager();
        }
        return instance;
    }
    
    /**
     * 私有构造函数
     */
    private HolidayManager() {
        initHolidayData();
    }
    
    /**
     * 初始化节假日数据
     */
    private void initHolidayData() {
        // 首先加载硬编码的基础节假日数据
        loadHardcodedHolidays();
        
        // 尝试从缓存文件加载
        loadHolidaysFromCache();
        
        // 异步更新当前年份和下一年的节假日数据
        int currentYear = LocalDate.now().getYear();
        updateHolidaysAsync(currentYear);
        updateHolidaysAsync(currentYear + 1);
    }
    
    /**
     * 加载硬编码的节假日数据（作为基础数据，确保离线时也能工作）
     */
    private void loadHardcodedHolidays() {
        // 当前年份节假日
        addHoliday("2024-01-01", "元旦");
        addHoliday("2024-02-10", "春节");
        addHoliday("2024-02-11", "春节");
        addHoliday("2024-02-12", "春节");
        addHoliday("2024-02-13", "春节");
        addHoliday("2024-02-14", "春节|情人节");
        addHoliday("2024-02-15", "春节");
        addHoliday("2024-02-16", "春节");
        addHoliday("2024-02-17", "春节");
        addHoliday("2024-04-04", "清明节");
        addHoliday("2024-04-05", "清明节");
        addHoliday("2024-04-06", "清明节");
        addHoliday("2024-05-01", "劳动节");
        addHoliday("2024-05-02", "劳动节");
        addHoliday("2024-05-03", "劳动节");
        addHoliday("2024-05-04", "劳动节");
        addHoliday("2024-05-05", "劳动节");
        addHoliday("2024-06-01", "儿童节");
        addHoliday("2024-06-08", "端午节");
        addHoliday("2024-06-09", "端午节");
        addHoliday("2024-06-10", "端午节");
        addHoliday("2024-08-10", "七夕节");
        addHoliday("2024-09-15", "中秋节");
        addHoliday("2024-09-16", "中秋节");
        addHoliday("2024-09-17", "中秋节");
        addHoliday("2024-10-01", "国庆节");
        addHoliday("2024-10-02", "国庆节");
        addHoliday("2024-10-03", "国庆节");
        addHoliday("2024-10-04", "国庆节");
        addHoliday("2024-10-05", "国庆节");
        addHoliday("2024-10-06", "国庆节");
        addHoliday("2024-10-07", "国庆节");
        
        // 2025年节假日
        addHoliday("2025-01-01", "元旦");
        // 不再将特殊纪念日加为节假日，改为特殊日期
        addSpecialDate("2025-01-18", "first-date");
        addHoliday("2025-01-29", "春节");
        addHoliday("2025-01-30", "春节");
        addHoliday("2025-01-31", "春节");
        // 改成通用的春节，而不用大年初一，避免显示问题
        addHoliday("2025-02-01", "春节");
        addHoliday("2025-02-02", "春节");
        addHoliday("2025-02-03", "春节");
        addHoliday("2025-02-04", "春节");
        // 避免情人节右侧显示方块问题
        addHoliday("2025-02-14", "情人节");
        // 不再将特殊纪念日加为节假日，改为特殊日期
        addSpecialDate("2025-02-16", "second-date");
        addHoliday("2025-03-08", "妇女节");
        addHoliday("2025-04-04", "清明节");
        addHoliday("2025-04-05", "清明节");
        addHoliday("2025-04-06", "清明节");
        addHoliday("2025-05-01", "劳动节");
        addHoliday("2025-05-02", "劳动节");
        addHoliday("2025-05-03", "劳动节");
        addHoliday("2025-05-04", "劳动节");
        addHoliday("2025-05-05", "劳动节");
        addHoliday("2025-06-01", "儿童节");
        addHoliday("2025-06-28", "端午节");
        addHoliday("2025-06-29", "端午节");
        addHoliday("2025-06-30", "端午节");
        addHoliday("2025-08-28", "七夕节");
        addHoliday("2025-10-01", "国庆节");
        addHoliday("2025-10-02", "国庆节");
        addHoliday("2025-10-03", "国庆节");
        addHoliday("2025-10-04", "国庆节|中秋节");
        addHoliday("2025-10-05", "国庆节|中秋节");
        addHoliday("2025-10-06", "国庆节|中秋节");
        addHoliday("2025-10-07", "国庆节");
    }
    
    /**
     * 添加一个节假日
     */
    private void addHoliday(String date, String name) {
        holidayMap.put(date, name);
    }
    
    /**
     * 添加一个特殊日期（不是节假日但需要特殊处理）
     */
    private void addSpecialDate(String date, String type) {
        specialDateMap.put(date, type);
    }
    
    /**
     * 从本地缓存加载节假日数据
     */
    private void loadHolidaysFromCache() {
        try {
            // 确保缓存目录存在
            Path cacheDir = Paths.get(CACHE_DIR);
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
                return;
            }
            
            // 读取所有缓存文件
            Files.list(cacheDir)
                 .filter(path -> path.toString().endsWith(".json"))
                 .forEach(this::loadHolidayFile);
                 
            AppLogger.info("从缓存加载节假日数据完成");
        } catch (Exception e) {
            AppLogger.error("从缓存加载节假日数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 加载单个节假日缓存文件
     */
    private void loadHolidayFile(Path path) {
        try {
            Map<String, Object> data = objectMapper.readValue(path.toFile(), 
                                       new TypeReference<Map<String, Object>>() {});
            
            // 解析API返回的节假日数据结构
            if (data.containsKey("holiday") && data.get("holiday") instanceof Map) {
                Map<String, Object> holidays = (Map<String, Object>) data.get("holiday");
                
                for (Map.Entry<String, Object> entry : holidays.entrySet()) {
                    if (entry.getValue() instanceof Map) {
                        Map<String, Object> holiday = (Map<String, Object>) entry.getValue();
                        if (holiday.containsKey("name")) {
                            String date = entry.getKey();
                            String name = holiday.get("name").toString();
                            holidayMap.put(date, name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            AppLogger.error("加载节假日文件失败: " + path.toString() + ", " + e.getMessage(), e);
        }
    }
    
    /**
     * 异步更新指定年份的节假日数据
     */
    private void updateHolidaysAsync(int year) {
        CompletableFuture.runAsync(() -> {
            try {
                // 检查是否已有缓存
                Path cachePath = Paths.get(CACHE_DIR, year + ".json");
                if (Files.exists(cachePath)) {
                    // 如果文件修改时间在30天内，不更新
                    long lastModified = Files.getLastModifiedTime(cachePath).toMillis();
                    long now = System.currentTimeMillis();
                    if ((now - lastModified) < 30 * 24 * 60 * 60 * 1000L) {
                        AppLogger.info("节假日数据缓存仍然有效: " + year);
                        return;
                    }
                }
                
                // 从API获取数据
                String apiUrl = API_URL + year;
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    // 读取响应
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        
                        // 保存到缓存
                        Files.createDirectories(Paths.get(CACHE_DIR));
                        try (FileWriter writer = new FileWriter(cachePath.toFile())) {
                            writer.write(response.toString());
                        }
                        
                        // 解析并加载数据
                        loadHolidayFile(cachePath);
                        AppLogger.info("成功更新节假日数据: " + year);
                    }
                } else {
                    AppLogger.warn("获取节假日数据失败，响应码: " + responseCode);
                }
            } catch (Exception e) {
                AppLogger.error("更新节假日数据失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 检查日期是否是节假日
     */
    public boolean isHoliday(LocalDate date) {
        String dateStr = date.format(DATE_FORMATTER);
        return holidayMap.containsKey(dateStr);
    }
    
    /**
     * 获取节假日名称
     */
    public String getHolidayName(LocalDate date) {
        String dateStr = date.format(DATE_FORMATTER);
        return holidayMap.getOrDefault(dateStr, "");
    }
    
    /**
     * 检查日期是否是特殊日期
     */
    public boolean isSpecialDate(LocalDate date) {
        String dateStr = date.format(DATE_FORMATTER);
        return specialDateMap.containsKey(dateStr);
    }
    
    /**
     * 获取特殊日期类型
     */
    public String getSpecialDateType(LocalDate date) {
        String dateStr = date.format(DATE_FORMATTER);
        return specialDateMap.getOrDefault(dateStr, "");
    }
} 