package com.timelordtty.projectcalendar.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目实体类
 * 
 * @author TimeLordTTY
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    
    private Long id;
    private String name;
    private Integer reviewPeriod; // 评审周期（自然日）
    private LocalDate onlineDate; // 项目上网日期
    private Integer registrationPeriod; // 报名期限（工作日）
    private LocalDate registrationEndDate; // 报名截止日期
    private LocalDate earliestReviewDate; // 最早评审日期
    private LocalDateTime expectedReviewTime; // 开标时间
    private LocalDateTime expertReviewTime; // 专家评审时间
    private String remark; // 项目备注
    private Status status = Status.ACTIVE;
    
    /**
     * 构造一个包含基本信息的项目
     * 
     * @param name 项目名称
     * @param reviewPeriod 评审周期
     * @param onlineDate 上网日期
     * @param registrationPeriod 报名期限
     */
    public Project(String name, Integer reviewPeriod, LocalDate onlineDate, Integer registrationPeriod) {
        this.name = name;
        this.reviewPeriod = reviewPeriod;
        this.onlineDate = onlineDate;
        this.registrationPeriod = registrationPeriod;
    }
} 