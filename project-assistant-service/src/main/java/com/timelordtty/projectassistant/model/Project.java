package com.timelordtty.projectassistant.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Data;

/**
 * 项目实体类
 * 
 * @author timelordtty
 */
@Data
public class Project {
    /** 项目唯一标识 */
    private Long id;
    
    /** 项目名称 */
    private String name;
    
    /** 评审周期（单位：自然日） */
    private Integer reviewPeriod;
    
    /** 项目上网日期 */
    private LocalDate onlineDate;
    
    /** 报名期限（单位：工作日） */
    private Integer registrationPeriod;
    
    /** 报名截止日期 */
    private LocalDate registrationEndDate;
    
    /** 最早可评审日期 */
    private LocalDate earliestReviewDate;
    
    /** 预计开标时间 */
    private LocalDateTime expectedReviewTime;
    
    /** 专家评审时间 */
    private LocalDateTime expertReviewTime;
    
    /** 项目备注信息 */
    private String remark;
    
    /** 项目上网日期 - 日期部分 */
    public LocalDate getOnlineDate() {
        return onlineDate;
    }
    
    /** 项目上网日期 - 字符串形式 */
    public String getOnlineDateStr() {
        return onlineDate != null ? onlineDate.toString() : null;
    }
    
    /** 开标时间 - 时间部分 */
    public LocalTime getExpectedReviewTimeOnly() {
        return expectedReviewTime != null ? expectedReviewTime.toLocalTime() : null;
    }
    
    /** 开标时间 - 日期部分 */
    public LocalDate getExpectedReviewDate() {
        return expectedReviewTime != null ? expectedReviewTime.toLocalDate() : null;
    }
    
    /** 专家评审时间 - 时间部分 */
    public LocalTime getExpertReviewTimeOnly() {
        return expertReviewTime != null ? expertReviewTime.toLocalTime() : null;
    }
    
    /** 专家评审时间 - 日期部分 */
    public LocalDate getExpertReviewDate() {
        return expertReviewTime != null ? expertReviewTime.toLocalDate() : null;
    }
    
    /** 获取当前项目状态文本 */
    public String getStatusText() {
        LocalDate now = LocalDate.now();
        
        if (onlineDate != null && now.isBefore(onlineDate)) {
            return "未上网";
        }
        
        if (registrationEndDate != null && now.isBefore(registrationEndDate)) {
            return "报名中";
        }
        
        if (expectedReviewTime != null && now.isBefore(expectedReviewTime.toLocalDate())) {
            return "待开标";
        }
        
        if (expertReviewTime != null && now.isBefore(expertReviewTime.toLocalDate())) {
            return "待评审";
        }
        
        return "已完成";
    }
} 