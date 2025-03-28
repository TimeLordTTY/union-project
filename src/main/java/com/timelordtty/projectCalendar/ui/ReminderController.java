package com.timelordtty.projectCalendar.ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.Project;
import com.timelordtty.projectCalendar.service.ProjectService;
import com.timelordtty.projectCalendar.utils.DateCalculator;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * 提醒控制器，负责管理项目提醒功能
 */
public class ReminderController {
    
    private ProjectService projectService;
    private List<Project> reminderProjects;
    private int currentReminderIndex = 0;
    
    // 提醒UI组件
    private HBox reminderScrollArea;
    private Label scrollingReminderLabel;
    
    // 提醒弹窗
    private Stage reminderListPopup;
    
    // 提醒动画
    private Timeline reminderScrollTimeline;
    
    // 回调函数
    private Consumer<LocalDate> onDateSelectedCallback;
    private Consumer<Project> onProjectSelectedCallback;
    
    /**
     * 构造函数
     * @param projectService 项目服务
     * @param reminderScrollArea 提醒滚动区域
     * @param scrollingReminderLabel 滚动提醒标签
     */
    public ReminderController(ProjectService projectService, 
                             HBox reminderScrollArea, 
                             Label scrollingReminderLabel) {
        this.projectService = projectService;
        this.reminderScrollArea = reminderScrollArea;
        this.scrollingReminderLabel = scrollingReminderLabel;
    }
    
    /**
     * 设置日期选择回调
     * @param callback 回调函数
     */
    public void setOnDateSelectedCallback(Consumer<LocalDate> callback) {
        this.onDateSelectedCallback = callback;
    }
    
    /**
     * 设置项目选择回调
     * @param callback 回调函数
     */
    public void setOnProjectSelectedCallback(Consumer<Project> callback) {
        this.onProjectSelectedCallback = callback;
    }
    
    /**
     * 初始化提醒功能
     */
    public void initialize() {
        try {
            AppLogger.info("初始化提醒功能");
            
            // 获取需要提醒的项目列表
            reminderProjects = projectService.getThisWeekAndNextWeekProjects();
            
            // 设置鼠标事件
            reminderScrollArea.setOnMouseEntered(e -> {
                pauseReminderScroll();
                // 鼠标悬浮时显示提醒列表
                showReminderListPopup();
            });
            
            // 移出时关闭列表
            reminderScrollArea.setOnMouseExited(e -> {
                // 检查鼠标是否移到了提醒列表上，如果没有，则隐藏列表
                if (reminderListPopup != null && reminderListPopup.isShowing()) {
                    Point2D mousePoint = new Point2D(e.getScreenX(), e.getScreenY());
                    if (!isPointInWindow(reminderListPopup, mousePoint)) {
                        reminderListPopup.hide();
                        reminderListPopup = null;
                        resumeReminderScroll();
                    }
                } else {
                    resumeReminderScroll();
                }
            });
            
            // 开始滚动显示
            startReminderScroll();
            
            AppLogger.info("提醒功能初始化完成");
        } catch (Exception e) {
            AppLogger.error("初始化提醒功能失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 刷新提醒
     */
    public void refreshReminders() {
        try {
            AppLogger.info("开始刷新提醒");
            
            // 重新获取提醒项目
            List<Project> oldProjects = reminderProjects;
            reminderProjects = projectService.getThisWeekAndNextWeekProjects();
            
            int oldCount = oldProjects != null ? oldProjects.size() : 0;
            int newCount = reminderProjects != null ? reminderProjects.size() : 0;
            AppLogger.info("提醒项目更新: " + oldCount + " -> " + newCount + " 个项目");
            
            // 如果当前没有项目在显示，立即开始显示
            if (reminderScrollTimeline == null || !reminderScrollTimeline.getStatus().equals(Animation.Status.RUNNING)) {
                startReminderScroll();
            } else {
                // 否则仅更新当前显示的项目文本
                showNextReminder();
            }
            
            AppLogger.info("提醒刷新完成");
        } catch (Exception e) {
            AppLogger.error("刷新提醒时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 开始滚动提醒显示
     */
    private void startReminderScroll() {
        // 如果没有需要提醒的项目，显示默认文本并返回
        if (reminderProjects == null || reminderProjects.isEmpty()) {
            scrollingReminderLabel.setText("暂无需要提醒的项目");
            return;
        }
        
        // 停止之前的滚动（如果有）
        if (reminderScrollTimeline != null) {
            reminderScrollTimeline.stop();
        }
        
        // 创建定时器，每5秒切换一个提醒
        reminderScrollTimeline = new Timeline(
            new KeyFrame(
                Duration.seconds(5),
                event -> showNextReminder()
            )
        );
        reminderScrollTimeline.setCycleCount(Animation.INDEFINITE);
        reminderScrollTimeline.play();
        
        // 立即显示第一个提醒
        showNextReminder();
    }
    
    /**
     * 显示下一个提醒
     */
    private void showNextReminder() {
        try {
            if (scrollingReminderLabel == null) {
                AppLogger.error("无法显示提醒，scrollingReminderLabel为空");
                return;
            }
            
            if (reminderProjects == null || reminderProjects.isEmpty()) {
                scrollingReminderLabel.setText("暂无需要提醒的项目");
                return;
            }
            
            // 获取当前要显示的项目
            Project project = reminderProjects.get(currentReminderIndex);
            
            // 构建提醒文本
            LocalDate today = LocalDate.now();
            LocalDate regEndDate = project.getRegistrationEndDate();
            LocalDate expertReviewDate = project.getExpertReviewDate();
            LocalDate reviewDate = project.getExpectedReviewDate();
            
            StringBuilder sb = new StringBuilder(project.getName());
            
            // 检查报名截止日期是否在时间范围内
            boolean isRegEndDateInRange = isDateInCurrentWeek(regEndDate) || 
                                         (today.getDayOfWeek() == DayOfWeek.FRIDAY && isDateInNextWeek(regEndDate));
            
            // 检查专家评审日期是否在时间范围内
            boolean isExpertReviewDateInRange = isDateInCurrentWeek(expertReviewDate) || 
                                            (today.getDayOfWeek() == DayOfWeek.FRIDAY && isDateInNextWeek(expertReviewDate));
            
            // 检查开标时间是否在时间范围内
            boolean isReviewDateInRange = isDateInCurrentWeek(reviewDate) || 
                                         (today.getDayOfWeek() == DayOfWeek.FRIDAY && isDateInNextWeek(reviewDate));
            
            // 添加日期信息，优先级：报名截止 > 专家评审 > 开标时间
            if (isRegEndDateInRange) {
                sb.append(" - 报名截止: ").append(DateCalculator.formatDate(regEndDate));
            } else if (isExpertReviewDateInRange) {
                LocalDateTime expertReviewTime = project.getExpertReviewTime();
                sb.append(" - 专家评审: ").append(
                    DateCalculator.formatDate(expertReviewDate) + " " + 
                    String.format("%02d:%02d", expertReviewTime.getHour(), expertReviewTime.getMinute())
                );
            } else if (isReviewDateInRange) {
                LocalDateTime reviewTime = project.getExpectedReviewTime();
                sb.append(" - 开标时间: ").append(
                    DateCalculator.formatDate(reviewDate) + " " + 
                    String.format("%02d:%02d", reviewTime.getHour(), reviewTime.getMinute())
                );
            }
            
            // 设置文本
            scrollingReminderLabel.setText(sb.toString());
            
            // 更新索引
            currentReminderIndex = (currentReminderIndex + 1) % reminderProjects.size();
        } catch (Exception e) {
            AppLogger.error("显示下一个提醒时发生异常: " + e.getMessage(), e);
            if (scrollingReminderLabel != null) {
                scrollingReminderLabel.setText("提醒加载失败");
            }
        }
    }
    
    /**
     * 暂停提醒滚动
     */
    private void pauseReminderScroll() {
        if (reminderScrollTimeline != null) {
            reminderScrollTimeline.pause();
        }
    }
    
    /**
     * 恢复提醒滚动
     */
    private void resumeReminderScroll() {
        if (reminderScrollTimeline != null && !reminderScrollTimeline.getStatus().equals(Animation.Status.RUNNING)) {
            reminderScrollTimeline.play();
        }
    }
    
    /**
     * 显示提醒列表弹出窗口
     */
    private void showReminderListPopup() {
        try {
            // 如果弹窗已存在且显示中，则不做任何操作
            if (reminderListPopup != null && reminderListPopup.isShowing()) {
                return;
            }
            
            // 如果没有提醒项目，不显示弹窗
            if (reminderProjects == null || reminderProjects.isEmpty()) {
                return;
            }
            
            // 创建弹窗内容
            VBox content = new VBox(5);
            content.setPadding(new Insets(10));
            content.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
            content.setMaxHeight(400);
            
            Label titleLabel = new Label("宝宝将要处理的所有项目事项");
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            content.getChildren().add(titleLabel);
            
            Separator separator = new Separator();
            content.getChildren().add(separator);
            
            // 创建滚动面板
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");
            VBox itemsContainer = new VBox(5);
            itemsContainer.setPadding(new Insets(5));
            
            LocalDate today = LocalDate.now();
            
            // 添加提醒项目
            for (Project project : reminderProjects) {
                HBox projectRow = new HBox(10);
                projectRow.setPadding(new Insets(5));
                projectRow.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand;");
                
                Label nameLabel = new Label(project.getName());
                nameLabel.setFont(Font.font("System", 12));
                
                LocalDate regEndDate = project.getRegistrationEndDate();
                LocalDate reviewDate = project.getExpectedReviewDate();
                
                VBox datesBox = new VBox(2);
                
                // 检查报名截止日期是否在时间范围内
                boolean isRegEndDateInRange = isDateInCurrentWeek(regEndDate) || 
                                            (today.getDayOfWeek() == DayOfWeek.FRIDAY && isDateInNextWeek(regEndDate));
                
                // 检查专家评审日期是否在时间范围内
                LocalDate expertReviewDate = project.getExpertReviewDate();
                boolean isExpertReviewDateInRange = isDateInCurrentWeek(expertReviewDate) || 
                                                 (today.getDayOfWeek() == DayOfWeek.FRIDAY && isDateInNextWeek(expertReviewDate));
                
                // 检查开标时间是否在时间范围内
                boolean isReviewDateInRange = isDateInCurrentWeek(reviewDate) || 
                                            (today.getDayOfWeek() == DayOfWeek.FRIDAY && isDateInNextWeek(reviewDate));
                
                // 添加符合条件的日期
                if (isRegEndDateInRange) {
                    Label regEndLabel = new Label("报名截止: " + DateCalculator.formatDate(regEndDate));
                    regEndLabel.setStyle("-fx-text-fill: #FF9800;");
                    regEndLabel.setFont(Font.font("System", 10));
                    datesBox.getChildren().add(regEndLabel);
                    
                    // 添加点击日期跳转事件
                    regEndLabel.setCursor(Cursor.HAND);
                    regEndLabel.setOnMouseClicked(event -> {
                        // 关闭提醒列表弹窗
                        if (reminderListPopup != null) {
                            reminderListPopup.close();
                            reminderListPopup = null;
                        }
                        
                        // 跳转到对应日期
                        if (onDateSelectedCallback != null) {
                            onDateSelectedCallback.accept(regEndDate);
                        }
                        
                        // 选中项目
                        if (onProjectSelectedCallback != null) {
                            onProjectSelectedCallback.accept(project);
                        }
                        
                        event.consume();
                    });
                }
                
                if (isExpertReviewDateInRange) {
                    Label expertReviewLabel = new Label("专家评审: " + DateCalculator.formatDate(expertReviewDate));
                    expertReviewLabel.setStyle("-fx-text-fill: #4CAF50;");
                    expertReviewLabel.setFont(Font.font("System", 10));
                    datesBox.getChildren().add(expertReviewLabel);
                    
                    // 添加点击日期跳转事件
                    expertReviewLabel.setCursor(Cursor.HAND);
                    expertReviewLabel.setOnMouseClicked(event -> {
                        // 关闭提醒列表弹窗
                        if (reminderListPopup != null) {
                            reminderListPopup.close();
                            reminderListPopup = null;
                        }
                        
                        // 跳转到对应日期
                        if (onDateSelectedCallback != null) {
                            onDateSelectedCallback.accept(expertReviewDate);
                        }
                        
                        // 选中项目
                        if (onProjectSelectedCallback != null) {
                            onProjectSelectedCallback.accept(project);
                        }
                        
                        event.consume();
                    });
                }
                
                if (isReviewDateInRange) {
                    Label reviewLabel = new Label("开标时间: " + DateCalculator.formatDate(reviewDate));
                    reviewLabel.setStyle("-fx-text-fill: #4CAF50;");
                    reviewLabel.setFont(Font.font("System", 10));
                    datesBox.getChildren().add(reviewLabel);
                    
                    // 添加点击日期跳转事件
                    reviewLabel.setCursor(Cursor.HAND);
                    reviewLabel.setOnMouseClicked(event -> {
                        // 关闭提醒列表弹窗
                        if (reminderListPopup != null) {
                            reminderListPopup.close();
                            reminderListPopup = null;
                        }
                        
                        // 跳转到对应日期
                        if (onDateSelectedCallback != null) {
                            onDateSelectedCallback.accept(reviewDate);
                        }
                        
                        // 选中项目
                        if (onProjectSelectedCallback != null) {
                            onProjectSelectedCallback.accept(project);
                        }
                        
                        event.consume();
                    });
                }
                
                projectRow.getChildren().addAll(nameLabel, datesBox);
                HBox.setHgrow(datesBox, Priority.ALWAYS);
                
                // 添加点击事件
                projectRow.setOnMouseClicked(event -> {
                    // 在左侧表格中选择该项目
                    if (onProjectSelectedCallback != null) {
                        onProjectSelectedCallback.accept(project);
                    }
                    
                    // 关闭弹窗
                    reminderListPopup.close();
                    reminderListPopup = null;
                    
                    event.consume();
                });
                
                itemsContainer.getChildren().add(projectRow);
            }
            
            scrollPane.setContent(itemsContainer);
            content.getChildren().add(scrollPane);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            
            // 创建弹出窗口
            reminderListPopup = new Stage();
            reminderListPopup.initStyle(StageStyle.UNDECORATED);
            reminderListPopup.initOwner(reminderScrollArea.getScene().getWindow());
            reminderListPopup.setWidth(350);
            
            Scene scene = new Scene(content);
            reminderListPopup.setScene(scene);
            
            // 定位弹窗（在滚动提醒区域的正上方）
            Point2D point = reminderScrollArea.localToScreen(0, 0);
            reminderListPopup.setX(point.getX());
            
            // 添加鼠标事件处理
            scene.setOnMouseEntered(event -> {
                // 鼠标进入列表内部，保持列表显示
                pauseReminderScroll();
            });
            
            scene.setOnMouseExited(event -> {
                // 鼠标离开列表，关闭列表
                reminderListPopup.close();
                reminderListPopup = null;
                resumeReminderScroll();
            });
            
            // 设置弹窗位置
            reminderListPopup.setOnShown(e -> {
                pauseReminderScroll();
                // 调整弹窗位置（在窗口上方弹出）
                reminderListPopup.setY(point.getY() - reminderListPopup.getHeight());
            });
            
            reminderListPopup.show();
        } catch (Exception e) {
            AppLogger.error("显示提醒列表弹窗时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 判断点是否在窗口内
     * @param window 窗口
     * @param screenPoint 屏幕坐标点
     * @return 点是否在窗口内
     */
    private boolean isPointInWindow(Stage window, Point2D screenPoint) {
        // 检查窗口是否为null
        if (window == null) {
            return false;
        }
        
        try {
            double x = window.getX();
            double y = window.getY();
            double width = window.getWidth();
            double height = window.getHeight();
            
            return screenPoint.getX() >= x && 
                  screenPoint.getX() <= x + width && 
                  screenPoint.getY() >= y && 
                  screenPoint.getY() <= y + height;
        } catch (Exception e) {
            AppLogger.error("检查点是否在窗口内时发生异常: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 判断日期是否在当前周内
     * @param date 要检查的日期
     * @return 是否在当前周内
     */
    private boolean isDateInCurrentWeek(LocalDate date) {
        if (date == null) {
            return false;
        }
        
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        return !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek);
    }
    
    /**
     * 判断日期是否在下一周内
     * @param date 要检查的日期
     * @return 是否在下一周内
     */
    private boolean isDateInNextWeek(LocalDate date) {
        if (date == null) {
            return false;
        }
        
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate startOfNextWeek = startOfWeek.plusDays(7);
        LocalDate endOfNextWeek = startOfNextWeek.plusDays(6);
        
        return !date.isBefore(startOfNextWeek) && !date.isAfter(endOfNextWeek);
    }
} 