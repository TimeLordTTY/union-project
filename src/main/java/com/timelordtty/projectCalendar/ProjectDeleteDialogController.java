package com.timelordtty.projectCalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.timelordtty.AppLogger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * 项目删除确认对话框控制器
 */
public class ProjectDeleteDialogController {
    
    @FXML private Label projectNameLabel;
    @FXML private Label onlineDateLabel;
    @FXML private Label registrationEndDateLabel;
    @FXML private Label expectedReviewDateLabel;
    
    private Project project;
    
    /**
     * 初始化控制器
     */
    @FXML
    private void initialize() {
        AppLogger.info("初始化项目删除确认对话框控制器");
    }
    
    /**
     * 设置要删除的项目
     * @param project 项目对象
     */
    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            // 设置项目名称
            projectNameLabel.setText(project.getName());
            
            // 设置上网日期
            if (project.getOnlineDate() != null) {
                onlineDateLabel.setText(formatDate(project.getOnlineDate()));
            } else {
                onlineDateLabel.setText("未设置");
            }
            
            // 设置报名截止日期
            if (project.getRegistrationEndDate() != null) {
                registrationEndDateLabel.setText(formatDate(project.getRegistrationEndDate()));
            } else {
                registrationEndDateLabel.setText("未设置");
            }
            
            // 设置开标时间
            if (project.getExpectedReviewTime() != null) {
                expectedReviewDateLabel.setText(formatDateTime(project.getExpectedReviewTime()));
            } else if (project.getExpectedReviewDate() != null) {
                expectedReviewDateLabel.setText(formatDate(project.getExpectedReviewDate()));
            } else {
                expectedReviewDateLabel.setText("未设置");
            }
        }
    }
    
    /**
     * 获取当前项目
     * @return 当前项目
     */
    public Project getProject() {
        return project;
    }
    
    /**
     * 格式化日期
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    
    /**
     * 格式化日期时间
     * @param dateTime 日期时间
     * @return 格式化后的日期时间字符串
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
} 