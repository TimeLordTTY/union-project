package com.timelordtty.projectcalendar.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 日期计算工具类
 */
public class DateUtil {

    // 节假日列表（可以从配置文件或数据库读取）
    private static final List<LocalDate> HOLIDAYS = new ArrayList<>();
    
    // 工作日（周末可以上班的日期）
    private static final List<LocalDate> WORKDAYS = new ArrayList<>();

    /**
     * 添加节假日
     * 
     * @param date 节假日日期
     */
    public static void addHoliday(LocalDate date) {
        if (!HOLIDAYS.contains(date)) {
            HOLIDAYS.add(date);
        }
    }

    /**
     * 添加工作日
     * 
     * @param date 工作日日期
     */
    public static void addWorkday(LocalDate date) {
        if (!WORKDAYS.contains(date)) {
            WORKDAYS.add(date);
        }
    }

    /**
     * 检查日期是否为工作日
     * 
     * @param date 日期
     * @return 是否为工作日
     */
    public static boolean isWorkingDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        // 如果是周末，但在特殊工作日列表中，则为工作日
        if ((dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) && WORKDAYS.contains(date)) {
            return true;
        }
        
        // 如果是工作日，但在节假日列表中，则不是工作日
        if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY && HOLIDAYS.contains(date)) {
            return false;
        }
        
        // 正常工作日判断（周一至周五）
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * 计算指定日期后的N个工作日
     * 
     * @param startDate 开始日期
     * @param workingDays 工作日天数
     * @return 计算后的日期
     */
    public static LocalDate addWorkingDays(LocalDate startDate, int workingDays) {
        LocalDate result = startDate;
        int addedDays = 0;
        
        while (addedDays < workingDays) {
            result = result.plusDays(1);
            if (isWorkingDay(result)) {
                addedDays++;
            }
        }
        
        return result;
    }

    /**
     * 计算两个日期之间的工作日天数
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工作日天数
     */
    public static int getWorkingDaysBetween(LocalDate startDate, LocalDate endDate) {
        int workingDays = 0;
        LocalDate date = startDate;
        
        while (!date.isAfter(endDate)) {
            if (isWorkingDay(date)) {
                workingDays++;
            }
            date = date.plusDays(1);
        }
        
        return workingDays;
    }

    /**
     * 检查给定月份是否有节假日
     * 
     * @param year 年份
     * @param month 月份
     * @return 节假日列表
     */
    public static List<LocalDate> getHolidaysInMonth(int year, int month) {
        List<LocalDate> result = new ArrayList<>();
        
        for (LocalDate holiday : HOLIDAYS) {
            if (holiday.getYear() == year && holiday.getMonthValue() == month) {
                result.add(holiday);
            }
        }
        
        return result;
    }

    /**
     * 初始化默认节假日（示例）
     * 实际应用中应从配置或数据库读取
     */
    public static void initDefaultHolidays() {
        // 2023年部分法定节假日示例
        List<LocalDate> holidays2023 = Arrays.asList(
            // 元旦
            LocalDate.of(2023, 1, 1),
            // 春节
            LocalDate.of(2023, 1, 21),
            LocalDate.of(2023, 1, 22),
            LocalDate.of(2023, 1, 23),
            LocalDate.of(2023, 1, 24),
            LocalDate.of(2023, 1, 25),
            LocalDate.of(2023, 1, 26),
            LocalDate.of(2023, 1, 27),
            // 清明节
            LocalDate.of(2023, 4, 5),
            // 劳动节
            LocalDate.of(2023, 5, 1),
            LocalDate.of(2023, 5, 2),
            LocalDate.of(2023, 5, 3),
            // 端午节
            LocalDate.of(2023, 6, 22),
            // 中秋节
            LocalDate.of(2023, 9, 29),
            // 国庆节
            LocalDate.of(2023, 10, 1),
            LocalDate.of(2023, 10, 2),
            LocalDate.of(2023, 10, 3),
            LocalDate.of(2023, 10, 4),
            LocalDate.of(2023, 10, 5),
            LocalDate.of(2023, 10, 6)
        );
        HOLIDAYS.addAll(holidays2023);
        
        // 2023年部分调休上班日
        List<LocalDate> workdays2023 = Arrays.asList(
            LocalDate.of(2023, 1, 28),
            LocalDate.of(2023, 1, 29),
            LocalDate.of(2023, 4, 23),
            LocalDate.of(2023, 5, 6),
            LocalDate.of(2023, 6, 25),
            LocalDate.of(2023, 10, 7),
            LocalDate.of(2023, 10, 8)
        );
        WORKDAYS.addAll(workdays2023);
    }
} 