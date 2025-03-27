package com.timelordtty.projectCalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import com.timelordtty.projectCalendar.model.Status;

/**
 * 项目模型类
 */
public class Project {
    private Long id;
    private String name;
    private int reviewPeriod; // 评审周期（自然日）
    private LocalDate onlineDate; // 项目上网日期
    private int registrationPeriod; // 报名期限（工作日）
    private LocalDate registrationEndDate; // 报名截止日期
    private LocalDate earliestReviewDate; // 最早评审日期
    private LocalDateTime expectedReviewTime; // 开标时间
    private LocalDateTime expertReviewTime; // 专家评审时间
    private String remark; // 项目备注

    public Project() {
        // 默认构造函数
    }

    public Project(String name, int reviewPeriod, LocalDate onlineDate, int registrationPeriod,
                  LocalDate registrationEndDate, LocalDate earliestReviewDate, 
                  LocalDateTime expectedReviewTime, LocalDateTime expertReviewTime, String remark) {
        this.name = name;
        this.reviewPeriod = reviewPeriod;
        this.onlineDate = onlineDate;
        this.registrationPeriod = registrationPeriod;
        this.registrationEndDate = registrationEndDate;
        this.earliestReviewDate = earliestReviewDate;
        this.expectedReviewTime = expectedReviewTime;
        this.expertReviewTime = expertReviewTime;
        this.remark = remark;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getReviewPeriod() {
        return reviewPeriod;
    }

    public void setReviewPeriod(int reviewPeriod) {
        this.reviewPeriod = reviewPeriod;
    }

    public LocalDate getOnlineDate() {
        return onlineDate;
    }

    public void setOnlineDate(LocalDate onlineDate) {
        this.onlineDate = onlineDate;
    }

    public int getRegistrationPeriod() {
        return registrationPeriod;
    }

    public void setRegistrationPeriod(int registrationPeriod) {
        this.registrationPeriod = registrationPeriod;
    }

    public LocalDate getRegistrationEndDate() {
        return registrationEndDate;
    }

    public void setRegistrationEndDate(LocalDate registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
    }

    public LocalDate getEarliestReviewDate() {
        return earliestReviewDate;
    }

    public void setEarliestReviewDate(LocalDate earliestReviewDate) {
        this.earliestReviewDate = earliestReviewDate;
    }

    public LocalDateTime getExpectedReviewTime() {
        return expectedReviewTime;
    }

    public void setExpectedReviewTime(LocalDateTime expectedReviewTime) {
        this.expectedReviewTime = expectedReviewTime;
    }
    
    // 为了兼容现有代码，提供日期版本的getter和setter
    public LocalDate getExpectedReviewDate() {
        return expectedReviewTime != null ? expectedReviewTime.toLocalDate() : null;
    }

    public void setExpectedReviewDate(LocalDate expectedReviewDate) {
        if (expectedReviewDate != null) {
            // 设置日期时，如果已有时间则保留，否则默认9:00
            if (this.expectedReviewTime != null) {
                this.expectedReviewTime = LocalDateTime.of(
                    expectedReviewDate, 
                    this.expectedReviewTime.toLocalTime()
                );
            } else {
                this.expectedReviewTime = LocalDateTime.of(
                    expectedReviewDate, 
                    java.time.LocalTime.of(9, 0)
                );
            }
        } else {
            this.expectedReviewTime = null;
        }
    }

    public LocalDateTime getExpertReviewTime() {
        return expertReviewTime;
    }

    public void setExpertReviewTime(LocalDateTime expertReviewTime) {
        this.expertReviewTime = expertReviewTime;
    }
    
    // 提供日期版本的专家评审时间getter和setter
    public LocalDate getExpertReviewDate() {
        return expertReviewTime != null ? expertReviewTime.toLocalDate() : null;
    }

    public void setExpertReviewDate(LocalDate expertReviewDate) {
        if (expertReviewDate != null) {
            // 设置日期时，如果已有时间则保留，否则默认9:00
            if (this.expertReviewTime != null) {
                this.expertReviewTime = LocalDateTime.of(
                    expertReviewDate, 
                    this.expertReviewTime.toLocalTime()
                );
            } else {
                this.expertReviewTime = LocalDateTime.of(
                    expertReviewDate, 
                    java.time.LocalTime.of(9, 0)
                );
            }
        } else {
            this.expertReviewTime = null;
        }
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取项目当前状态文本
     * @return 状态文本
     */
    public String getStatusText() {
        LocalDate today = LocalDate.now();
        LocalDate expectedReviewDate = getExpectedReviewDate();
        
        // 如果开标时间已过，则已完成
        if (expectedReviewDate != null && expectedReviewDate.isBefore(today)) {
            return Status.COMPLETED.getText();
        }
        
        // 如果上网日期已过，报名截止日期未到，则进行中
        if (onlineDate != null && onlineDate.isBefore(today) || onlineDate.isEqual(today)) {
            if (registrationEndDate != null && (registrationEndDate.isAfter(today) || registrationEndDate.isEqual(today))) {
                return Status.IN_PROGRESS.getText();
            }
        }
        
        // 如果上网日期未到，则即将开始
        if (onlineDate != null && onlineDate.isAfter(today)) {
            return Status.UPCOMING.getText();
        }
        
        // 如果报名截止日期已过，开标时间未到，则进行中
        if (registrationEndDate != null && registrationEndDate.isBefore(today)) {
            if (expectedReviewDate != null && expectedReviewDate.isAfter(today)) {
                return Status.IN_PROGRESS.getText();
            }
        }
        
        // 如果报名截止日期已过，但没有设置开标时间，则已过期
        if (registrationEndDate != null && registrationEndDate.isBefore(today) && expectedReviewDate == null) {
            return Status.EXPIRED.getText();
        }
        
        // 默认为进行中
        return Status.IN_PROGRESS.getText();
    }
    
    /**
     * 获取项目状态
     * @return 状态枚举
     */
    public Status getStatus() {
        return Status.fromText(getStatusText());
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return id != null && id.equals(project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 