package com.timelordtty.projectCalendar.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期计算工具类，用于处理工作日和节假日计算
 */
public class DateCalculator {
    
    private static final Map<LocalDate, Boolean> workdayCache = new HashMap<>();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // 调休工作日 - 周末需要上班的日期
    private static final Map<String, Boolean> WORKDAYS_MAP = new HashMap<>();
    
    // 初始化调休工作日
    static {
        // 2024年调休
        WORKDAYS_MAP.put("2024-02-04", true); // 春节调休
        WORKDAYS_MAP.put("2024-02-18", true); // 春节调休
        WORKDAYS_MAP.put("2024-04-07", true); // 清明调休
        WORKDAYS_MAP.put("2024-04-28", true); // 劳动节调休
        WORKDAYS_MAP.put("2024-05-11", true); // 劳动节调休
        WORKDAYS_MAP.put("2024-06-02", true); // 端午调休
        WORKDAYS_MAP.put("2024-09-14", true); // 中秋调休
        WORKDAYS_MAP.put("2024-09-29", true); // 国庆调休
        WORKDAYS_MAP.put("2024-10-12", true); // 国庆调休
        
        // 2025年调休
        WORKDAYS_MAP.put("2025-01-26", true); // 春节调休
        WORKDAYS_MAP.put("2025-02-08", true); // 春节调休
        WORKDAYS_MAP.put("2025-04-06", true); // 清明调休
        WORKDAYS_MAP.put("2025-04-27", true); // 劳动节调休
        WORKDAYS_MAP.put("2025-05-10", true); // 劳动节调休
        WORKDAYS_MAP.put("2025-06-29", true); // 端午调休
        WORKDAYS_MAP.put("2025-09-28", true); // 国庆调休
        WORKDAYS_MAP.put("2025-10-11", true); // 国庆调休
    }
    
    /**
     * 检查指定日期是否为工作日
     * @param date 指定日期
     * @return 是否为工作日
     */
    public static boolean isWorkingDay(LocalDate date) {
        // 首先检查缓存
        if (workdayCache.containsKey(date)) {
            return workdayCache.get(date);
        }
        
        boolean result;
        String dateStr = date.format(DATE_FORMATTER);
        
        // 如果是法定节假日，则不是工作日
        if (HolidayManager.getInstance().isHoliday(date)) {
            result = false;
        }
        // 如果是周末，但是是调休工作日，则是工作日
        else if (isWeekend(date) && WORKDAYS_MAP.containsKey(dateStr)) {
            result = true;
        }
        // 如果是周末且不是调休工作日，则不是工作日
        else if (isWeekend(date)) {
            result = false;
        }
        // 否则是普通工作日
        else {
            result = true;
        }
        
        // 将结果放入缓存
        workdayCache.put(date, result);
        return result;
    }
    
    /**
     * 检查指定日期是否为周末
     * @param date 指定日期
     * @return 是否为周末
     */
    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    /**
     * 计算指定日期后的工作日数后的日期，然后再前进一个工作日
     * 例如: 上网日期是3月26日，报名期限是5个工作日
     * 则计算逻辑是：3月27日开始算第1个工作日，到4月2日是第5个工作日，
     * 然后再取之后的第一个工作日，即4月3日
     * 
     * @param startDate 开始日期
     * @param workingDays 工作日数
     * @return 计算后的日期
     */
    public static LocalDate calculateDateAfterWorkingDays(LocalDate startDate, int workingDays) {
        // 先移动到开始日期的下一天，作为第一个计算日
        LocalDate currentDate = startDate.plusDays(1);
        
        // 计算工作日
        int count = 0;
        while (count < workingDays) {
            if (isWorkingDay(currentDate)) {
                count++;
            }
            
            if (count < workingDays) {
                currentDate = currentDate.plusDays(1);
            }
        }
        
        // 再找到下一个工作日
        currentDate = currentDate.plusDays(1);
        while (!isWorkingDay(currentDate)) {
            currentDate = currentDate.plusDays(1);
        }
        
        return currentDate;
    }
    
    /**
     * 计算指定日期后指定自然日数后的第一个工作日
     * 例如: 上网日期是3月26日，评审周期是20个自然日
     * 则计算逻辑是：3月27日开始算第1个自然日，到4月15日是第20个自然日，
     * 然后再取之后的第一个工作日，即4月16日
     * 
     * @param startDate 开始日期
     * @param naturalDays 自然日数
     * @return 计算后的日期
     */
    public static LocalDate calculateFirstWorkingDayAfterNaturalDays(LocalDate startDate, int naturalDays) {
        // 先移动到开始日期的下一天，作为第一个计算日
        LocalDate currentDate = startDate.plusDays(1);
        
        // 再加上剩余的自然日数量(自然日数-1)
        currentDate = currentDate.plusDays(naturalDays - 1);
        
        // 再向后找到第一个工作日
        currentDate = currentDate.plusDays(1);
        while (!isWorkingDay(currentDate)) {
            currentDate = currentDate.plusDays(1);
        }
        
        return currentDate;
    }
    
    /**
     * 格式化日期为中文格式（yyyy年MM月dd日）
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String formatDateCN(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
    }
    
    /**
     * 格式化日期为标准格式（yyyy-MM-dd）
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    /**
     * 清除缓存
     */
    public static void clearCache() {
        workdayCache.clear();
    }
    
    /**
     * 计算从开始日期起指定工作日数后的日期
     * @param startDate 开始日期
     * @param workingDays 工作日数
     * @return 计算后的日期
     */
    public static LocalDate addWorkingDays(LocalDate startDate, int workingDays) {
        if (startDate == null || workingDays <= 0) {
            return startDate;
        }
        
        return calculateDateAfterWorkingDays(startDate, workingDays);
    }
} 