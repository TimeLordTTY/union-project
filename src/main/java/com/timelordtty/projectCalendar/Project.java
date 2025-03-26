package com.timelordtty.projectCalendar;

import java.time.LocalDate;

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
    private LocalDate expectedReviewDate; // 预计评审日期
    private String remark; // 项目备注

    public Project() {
        // 默认构造函数
    }

    public Project(String name, int reviewPeriod, LocalDate onlineDate, int registrationPeriod,
                  LocalDate registrationEndDate, LocalDate earliestReviewDate, 
                  LocalDate expectedReviewDate, String remark) {
        this.name = name;
        this.reviewPeriod = reviewPeriod;
        this.onlineDate = onlineDate;
        this.registrationPeriod = registrationPeriod;
        this.registrationEndDate = registrationEndDate;
        this.earliestReviewDate = earliestReviewDate;
        this.expectedReviewDate = expectedReviewDate;
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

    public LocalDate getExpectedReviewDate() {
        return expectedReviewDate;
    }

    public void setExpectedReviewDate(LocalDate expectedReviewDate) {
        this.expectedReviewDate = expectedReviewDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return name;
    }
} 