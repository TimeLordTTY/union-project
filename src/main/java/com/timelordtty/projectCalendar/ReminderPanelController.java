package com.timelordtty.projectCalendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.service.ProjectService;
import com.timelordtty.projectCalendar.utils.DateCalculator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * 提醒面板控制器
 */
public class ReminderPanelController {

    @FXML private Label titleLabel;
    @FXML private VBox remindersContainer;
    
    private ProjectService projectService;
    
    /**
     * 初始化控制器
     */
    @FXML
    private void initialize() {
        try {
            projectService = ProjectService.getInstance();
            titleLabel.setText("项目管理小助手 - 提醒");
            
            // 显示提醒
            showReminders();
        } catch (Exception e) {
            AppLogger.error("初始化提醒面板时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 显示提醒
     */
    public void showReminders() {
        try {
            // 清空容器
            remindersContainer.getChildren().clear();
            
            // 获取当前日期
            LocalDate today = LocalDate.now();
            
            // 计算本周的开始日期和结束日期
            LocalDate weekStart = getStartOfWeek(today);
            LocalDate weekEnd = weekStart.plusDays(6);
            
            // 获取下周的开始和结束
            LocalDate nextWeekStart = weekStart.plusDays(7);
            LocalDate nextWeekEnd = nextWeekStart.plusDays(6);
            
            // 显示提醒标题
            Text titleText = new Text("当前日期: " + formatDate(today) + "\n");
            titleText.setStyle("-fx-font-weight: bold;");
            
            Text weekText = new Text("本周期间: " + formatDate(weekStart) + " 至 " + formatDate(weekEnd) + "\n\n");
            weekText.setStyle("-fx-font-weight: bold;");
            
            TextFlow titleFlow = new TextFlow(titleText, weekText);
            remindersContainer.getChildren().add(titleFlow);
            
            // 获取本周需要提醒的项目（包括报名截止日期、专家评审时间和开标时间）
            List<Project> thisWeekProjects = projectService.getProjectsWithDatesInRange(weekStart, weekEnd);
            
            // 如果今天是周五，也获取下周的项目
            boolean isFriday = today.getDayOfWeek() == DayOfWeek.FRIDAY;
            List<Project> nextWeekProjects = null;
            
            if (isFriday) {
                nextWeekProjects = projectService.getProjectsWithDatesInRange(nextWeekStart, nextWeekEnd);
            }
            
            // 显示本周项目提醒
            if (thisWeekProjects.isEmpty()) {
                Text noProjectsText = new Text("本周没有需要关注的项目\n\n");
                remindersContainer.getChildren().add(new TextFlow(noProjectsText));
            } else {
                Text weekProjectsTitle = new Text("本周需要关注的项目：\n");
                weekProjectsTitle.setStyle("-fx-font-weight: bold;");
                remindersContainer.getChildren().add(new TextFlow(weekProjectsTitle));
                
                for (Project project : thisWeekProjects) {
                    addProjectReminder(project, weekStart, weekEnd, false);
                }
                
                remindersContainer.getChildren().add(new TextFlow(new Text("\n")));
            }
            
            // 如果是周五，显示下周项目提醒
            if (isFriday && nextWeekProjects != null) {
                if (nextWeekProjects.isEmpty()) {
                    Text noNextWeekProjectsText = new Text("下周没有需要关注的项目\n");
                    remindersContainer.getChildren().add(new TextFlow(noNextWeekProjectsText));
                } else {
                    Text nextWeekProjectsTitle = new Text("下周需要关注的项目：\n");
                    nextWeekProjectsTitle.setStyle("-fx-font-weight: bold;");
                    remindersContainer.getChildren().add(new TextFlow(nextWeekProjectsTitle));
                    
                    for (Project project : nextWeekProjects) {
                        addProjectReminder(project, nextWeekStart, nextWeekEnd, true);
                    }
                }
            }
        } catch (Exception e) {
            AppLogger.error("显示提醒时发生异常: " + e.getMessage(), e);
            // 显示错误信息
            Text errorText = new Text("加载提醒时发生错误: " + e.getMessage());
            errorText.setStyle("-fx-fill: red;");
            remindersContainer.getChildren().add(new TextFlow(errorText));
        }
    }
    
    /**
     * 添加项目提醒
     * @param project 项目
     * @param weekStart 周开始日期
     * @param weekEnd 周结束日期
     * @param isNextWeek 是否为下周
     */
    private void addProjectReminder(Project project, LocalDate weekStart, LocalDate weekEnd, boolean isNextWeek) {
        try {
            StringBuilder reminderText = new StringBuilder();
            reminderText.append("● ").append(project.getName()).append("\n");
            
            // 检查报名截止日期
            if (project.getRegistrationEndDate() != null) {
                if (isDateInRange(project.getRegistrationEndDate(), weekStart, weekEnd)) {
                    reminderText.append("   - 报名截止日期: ")
                               .append(formatDate(project.getRegistrationEndDate()))
                               .append("\n");
                }
            }
            
            // 检查专家评审时间
            if (project.getExpertReviewTime() != null) {
                LocalDate expertReviewDate = project.getExpertReviewDate();
                if (isDateInRange(expertReviewDate, weekStart, weekEnd)) {
                    reminderText.append("   - 专家评审时间: ")
                               .append(formatDateTime(project.getExpertReviewTime()))
                               .append("\n");
                }
            }
            
            // 检查开标时间
            if (project.getExpectedReviewTime() != null) {
                LocalDate expectedReviewDate = project.getExpectedReviewDate();
                if (isDateInRange(expectedReviewDate, weekStart, weekEnd)) {
                    reminderText.append("   - 开标时间: ")
                               .append(formatDateTime(project.getExpectedReviewTime()))
                               .append("\n");
                }
            }
            
            // 添加提醒文本
            Text text = new Text(reminderText.toString());
            
            // 如果是下周的项目，使用淡黄色背景
            if (isNextWeek) {
                text.setStyle("-fx-fill: #333333; -fx-background-color: #FFFACD;");
            }
            
            TextFlow textFlow = new TextFlow(text);
            
            // 为下周项目添加淡黄色背景
            if (isNextWeek) {
                textFlow.setStyle("-fx-background-color: #FFFACD; -fx-padding: 5px;");
            }
            
            // 添加工具提示，显示项目备注
            if (project.getRemark() != null && !project.getRemark().trim().isEmpty()) {
                Tooltip tooltip = new Tooltip("备注: " + project.getRemark());
                Tooltip.install(textFlow, tooltip);
            }
            
            remindersContainer.getChildren().add(textFlow);
        } catch (Exception e) {
            AppLogger.error("添加项目提醒时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取一周的开始日期（周一）
     * @param date 日期
     * @return 一周的开始日期
     */
    private LocalDate getStartOfWeek(LocalDate date) {
        // 使用周一作为一周的开始
        TemporalField fieldISO = WeekFields.of(Locale.getDefault()).dayOfWeek();
        return date.with(fieldISO, 1);
    }
    
    /**
     * 检查日期是否在指定范围内
     * @param date 要检查的日期
     * @param start 开始日期
     * @param end 结束日期
     * @return 是否在范围内
     */
    private boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(start) && !date.isAfter(end);
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