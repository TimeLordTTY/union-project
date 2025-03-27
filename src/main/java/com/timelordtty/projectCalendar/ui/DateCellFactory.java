package com.timelordtty.projectCalendar.ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.Project;
import com.timelordtty.projectCalendar.service.ProjectService;
import com.timelordtty.projectCalendar.utils.DateCalculator;
import com.timelordtty.projectCalendar.utils.HolidayManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * 日期单元格工厂类，负责创建和管理日历单元格
 */
public class DateCellFactory {
    
    private ProjectService projectService;
    private HolidayManager holidayManager;
    private ProjectClickHandler projectClickHandler;
    private DateClickHandler dateClickHandler;
    
    public DateCellFactory(ProjectService projectService, HolidayManager holidayManager) {
        this.projectService = projectService;
        this.holidayManager = holidayManager;
    }
    
    /**
     * 设置项目点击处理器
     * @param handler 项目点击处理器
     */
    public void setProjectClickHandler(ProjectClickHandler handler) {
        this.projectClickHandler = handler;
    }
    
    /**
     * 设置日期点击处理器
     * @param handler 日期点击处理器
     */
    public void setDateClickHandler(DateClickHandler handler) {
        this.dateClickHandler = handler;
    }
    
    /**
     * 创建日期单元格
     * @param date 日期
     * @param isCurrentMonth 是否为当前月日期
     * @return 日期单元格
     */
    public VBox createDateCell(LocalDate date, boolean isCurrentMonth) {
        try {
            AppLogger.debug("创建日期单元格: " + date + ", isCurrentMonth: " + isCurrentMonth);
            
            // 创建日期单元格容器
            VBox dateCell = new VBox(5);
            dateCell.setPadding(new Insets(5));
            dateCell.getStyleClass().add("project-date-cell");
            
            // 设置单元格属性，确保自适应窗口大小并且可见
            dateCell.setMinHeight(80);
            dateCell.setPrefHeight(100);
            dateCell.setPrefWidth(100);
            dateCell.setMaxWidth(Double.MAX_VALUE);
            dateCell.setMaxHeight(Double.MAX_VALUE);
            
            // 检查是否为非工作日（周末或法定假日）
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
            boolean isHoliday = holidayManager.isHoliday(date);
            boolean isNonWorkingDay = isWeekend || isHoliday;
            
            // 设置单元格背景色
            String backgroundColor;
            if (!isCurrentMonth) {
                // 区分上个月和下个月的日期
                if (date.isBefore(date.withDayOfMonth(1))) {
                    backgroundColor = "#F0F0F0"; // 上个月日期 - 浅灰色
                } else {
                    backgroundColor = "#F8F8F8"; // 下个月日期 - 更浅的灰色
                }
            } else if (isNonWorkingDay) {
                backgroundColor = "#EEEEEE"; // 非工作日 - 中等灰色
            } else {
                backgroundColor = "white"; // 普通工作日 - 白色
            }
            
            // 设置明显的单元格边框和背景色
            dateCell.setStyle("-fx-border-color: #CCCCCC; -fx-border-width: 0.5; -fx-background-color: " + backgroundColor + ";");
            
            // 日期标签和今日标记HBox，水平布局
            HBox dateHeader = new HBox(5);
            dateHeader.setPadding(new Insets(0, 0, 5, 0));
            dateHeader.setAlignment(Pos.CENTER_LEFT);
            
            // 日期数字标签
            Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
            dateLabel.getStyleClass().add("date-number");
            
            // 设置日期字体和颜色
            if (!isCurrentMonth) {
                // 非当月日期显示为灰色
                dateLabel.setStyle("-fx-text-fill: #BDBDBD; -fx-font-size: 14px;");
            } else {
                // 当月日期显示为黑色
                dateLabel.setStyle("-fx-text-fill: #212121; -fx-font-weight: bold; -fx-font-size: 14px;");
            }
            
            // 添加日期标签到dateHeader
            dateHeader.getChildren().add(dateLabel);
            
            // 检查是否为今天，添加特殊标记（只有绿色小方块，不改变背景色）
            if (date.equals(LocalDate.now())) {
                // 创建今日标记（绿色小方块）
                Rectangle todayMarker = new Rectangle(10, 10);
                todayMarker.setFill(Color.web("#4CAF50"));
                todayMarker.setStroke(Color.web("#388E3C"));
                todayMarker.setStrokeWidth(0.5);
                
                // 添加今日标记到dateHeader
                dateHeader.getChildren().add(todayMarker);
            }
            
            // 如果是节假日，添加节假日名称标签
            if (isHoliday) {
                String holidayName = holidayManager.getHolidayName(date);
                if (holidayName != null && !holidayName.isEmpty()) {
                    Label holidayLabel = new Label(holidayName);
                    holidayLabel.setStyle("-fx-text-fill: #E53935; -fx-font-size: 11px;");
                    holidayLabel.setAlignment(Pos.CENTER_RIGHT);
                    // 使用Pane作为填充，让节假日标签靠右显示
                    Pane spacer = new Pane();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    dateHeader.getChildren().addAll(spacer, holidayLabel);
                }
            }
            
            // 添加日期标题到单元格
            dateCell.getChildren().add(dateHeader);
            
            // 项目标记容器（可滚动）
            VBox projectContainer = new VBox(3);
            projectContainer.setSpacing(3);
            projectContainer.setFillWidth(true);

            // 设置项目容器填充整个可用空间
            projectContainer.prefWidthProperty().bind(dateCell.widthProperty().subtract(10)); // 减去内边距
            VBox.setVgrow(projectContainer, Priority.ALWAYS);

            ScrollPane scrollPane = new ScrollPane(projectContainer);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // 始终不显示垂直滚动条，但仍可滚动
            scrollPane.setFitToWidth(true); // 适应宽度
            scrollPane.setMinHeight(40);
            scrollPane.setFitToHeight(false); // 不适应高度，允许内容超出时滚动

            // 绑定滚动面板高度到单元格剩余空间
            scrollPane.prefHeightProperty().bind(dateCell.heightProperty().subtract(dateHeader.heightProperty()).subtract(15)); // 减去日期头部高度和内边距
            
            // 设置强制隐藏滚动条的样式
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;" +
                               "-fx-padding: 0; -fx-background-insets: 0;");
            
            // 防止任何情况下滚动条被显示
            scrollPane.getStyleClass().addAll("project-cell-scroll-pane", "no-scroll-bar");
            scrollPane.setPannable(true); // 允许用户拖动滚动

            // 移除原有的鼠标进入/离开事件，不再切换滚动条的显示策略
            scrollPane.setOnMouseEntered(null);
            scrollPane.setOnMouseExited(null);

            // 查找该日期的所有项目并添加标记（无论是否为当前月份）
            List<Project> projectsForDate = projectService.getProjectsForDate(date);
            if (projectsForDate != null && !projectsForDate.isEmpty()) {
                // 按项目分组，记录每个项目的所有日期类型
                Map<Project, List<String>> projectDateTypes = new HashMap<>();
                
                // 遍历所有项目，收集每个项目的所有日期类型
                for (Project project : projectsForDate) {
                    List<String> dateTypes = new ArrayList<>();
                    
                    // 检查项目的各个日期是否匹配当前日期，并且日期不为null
                    if (project.getOnlineDate() != null && date.equals(project.getOnlineDate())) {
                        dateTypes.add("上网");
                    }
                    if (project.getRegistrationEndDate() != null && date.equals(project.getRegistrationEndDate())) {
                        dateTypes.add("报名截止");
                    }
                    if (project.getEarliestReviewDate() != null && date.equals(project.getEarliestReviewDate())) {
                        dateTypes.add("最早评审");
                    }
                    if (project.getExpectedReviewTime() != null && date.equals(project.getExpectedReviewTime().toLocalDate())) {
                        dateTypes.add("开标");
                    }
                    if (project.getExpertReviewTime() != null && date.equals(project.getExpertReviewTime().toLocalDate())) {
                        dateTypes.add("专家评审");
                    }
                    
                    // 只有当项目在当前日期有关联的日期类型时才添加到列表
                    if (!dateTypes.isEmpty()) {
                        projectDateTypes.put(project, dateTypes);
                    }
                }
                
                // 项目数量
                int projectCount = projectDateTypes.size();
                boolean hasMultipleProjects = projectCount > 1;
                
                // 为每个项目创建标记
                for (Map.Entry<Project, List<String>> entry : projectDateTypes.entrySet()) {
                    Project project = entry.getKey();
                    List<String> dateTypes = entry.getValue();
                    
                    // 如果一个项目有多个日期类型，则为每个日期类型创建单独的行
                    if (dateTypes.size() > 1) {
                        for (String dateType : dateTypes) {
                            List<String> singleType = new ArrayList<>();
                            singleType.add(dateType);
                            HBox projectMarkerBox = createProjectMarkerWithDateTypes(project, date, singleType);
                            
                            // 添加点击和双击事件
                            setupProjectMarkerEvents(projectMarkerBox, project);
                            
                            // 添加到项目容器
                            projectContainer.getChildren().add(projectMarkerBox);
                        }
                    } else {
                        // 创建项目标记
                        HBox projectMarkerBox = createProjectMarkerWithDateTypes(project, date, dateTypes);
                        
                        // 添加点击和双击事件
                        setupProjectMarkerEvents(projectMarkerBox, project);
                        
                        // 添加到项目容器
                        projectContainer.getChildren().add(projectMarkerBox);
                    }
                }
                
                // 如果单元格中的项目很多，设置更紧凑的布局
                if (projectContainer.getChildren().size() > 3) {
                    projectContainer.setSpacing(1);
                    for (javafx.scene.Node node : projectContainer.getChildren()) {
                        if (node instanceof HBox) {
                            ((HBox) node).setPadding(new Insets(0, 1, 0, 1));
                        }
                    }
                }
                
                // 如果当前单元格中恰好有1个项目但是需要显示2行以上（多个日期类型），确保布局合理
                if (projectContainer.getChildren().size() > 0 && projectContainer.getChildren().size() <= 2) {
                    VBox.setMargin(projectContainer.getChildren().get(0), new Insets(2, 0, 2, 0));
                    if (projectContainer.getChildren().size() == 2) {
                        VBox.setMargin(projectContainer.getChildren().get(1), new Insets(2, 0, 2, 0));
                    }
                }
            }
            
            // 添加项目容器到单元格
            dateCell.getChildren().add(scrollPane);
            
            return dateCell;
        } catch (Exception e) {
            // 记录错误并返回默认单元格
            AppLogger.error("创建日期单元格时出错: " + date, e);
            e.printStackTrace(); // 打印堆栈跟踪以便调试
            
            // 创建一个简单的备用单元格，确保它是可见的
            VBox fallbackCell = new VBox();
            fallbackCell.setStyle("-fx-border-color: red; -fx-border-width: 1; -fx-background-color: #FFEBEE;");
            fallbackCell.setPadding(new Insets(5));
            Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
            dateLabel.setStyle("-fx-font-weight: bold;");
            fallbackCell.getChildren().add(dateLabel);
            return fallbackCell;
        }
    }
    
    /**
     * 设置项目标记的点击和双击事件
     * @param projectMarkerBox 项目标记HBox
     * @param project 项目对象
     */
    private void setupProjectMarkerEvents(HBox projectMarkerBox, Project project) {
        // 设置手型光标
        projectMarkerBox.setCursor(Cursor.HAND);
        
        // 添加点击事件，点击项目标记时只选中该项目
        projectMarkerBox.setOnMouseClicked(event -> {
            if (projectClickHandler != null) {
                projectClickHandler.onProjectClicked(project, event.getClickCount() == 2);
            }
            
            // 阻止事件继续传播
            event.consume();
        });
    }
    
    /**
     * 创建带日期类型的项目标记
     * @param project 项目
     * @param date 日期
     * @param dateTypes 日期类型列表
     * @return 项目标记HBox
     */
    private HBox createProjectMarkerWithDateTypes(Project project, LocalDate date, List<String> dateTypes) {
        HBox projectMarkerBox = new HBox(3);
        projectMarkerBox.setAlignment(Pos.CENTER_LEFT);
        projectMarkerBox.setPadding(new Insets(1));
        projectMarkerBox.getStyleClass().add("project-marker-row");
        projectMarkerBox.setMaxWidth(Double.MAX_VALUE);
        
        // 项目名称标签
        Label projectNameLabel = new Label(project.getName());
        projectNameLabel.getStyleClass().add("project-marker");
        projectNameLabel.setPadding(new Insets(1, 2, 1, 2));
        
        // 设置项目名称标签样式
        String bgColor = "#F5F5F5"; // 默认背景色
        String textColor = "#212121"; // 默认文字颜色
        
        // 根据第一个日期类型设置背景色和文字颜色
        if (!dateTypes.isEmpty()) {
            String firstType = dateTypes.get(0);
            switch (firstType) {
                case "上网":
                    bgColor = "#E3F2FD";
                    textColor = "#1976D2";
                    break;
                case "报名截止":
                    bgColor = "#FFF8E1";
                    textColor = "#FF8F00";
                    break;
                case "最早评审":
                    bgColor = "#E8F5E9";
                    textColor = "#388E3C";
                    break;
                case "开标":
                    bgColor = "#FFEBEE";
                    textColor = "#D32F2F";
                    break;
                case "专家评审":
                    bgColor = "#E1BEE7";
                    textColor = "#9C27B0";
                    break;
            }
        }
        
        projectNameLabel.setStyle("-fx-background-color: " + bgColor + "; " +
                               "-fx-background-radius: 3; " +
                               "-fx-text-fill: " + textColor + ";");
        
        // 设置最大宽度并处理文本溢出
        projectNameLabel.setMaxWidth(85);
        projectNameLabel.setMinWidth(50);
        projectNameLabel.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        HBox.setHgrow(projectNameLabel, Priority.ALWAYS);
        
        // 添加项目名称标签
        projectMarkerBox.getChildren().add(projectNameLabel);
        
        // 创建日期类型标签
        if (!dateTypes.isEmpty()) {
            // 将所有日期类型合并为一个字符串
            String dateTypeText = String.join("/", dateTypes);
            
            Label dateTypeLabel = new Label(dateTypeText);
            dateTypeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + textColor + ";");
            dateTypeLabel.getStyleClass().add("date-type-label");
            dateTypeLabel.setMaxWidth(55);
            dateTypeLabel.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
            
            // 添加日期类型标签
            projectMarkerBox.getChildren().add(dateTypeLabel);
        }
        
        // 设置悬浮提示，显示完整项目信息
        StringBuilder tooltipText = new StringBuilder(project.getName());
        
        // 添加日期类型
        if (!dateTypes.isEmpty()) {
            tooltipText.append("\n日期类型: ").append(String.join(", ", dateTypes));
        }
        
        // 添加其他日期信息，仅展示非空日期
        if (project.getOnlineDate() != null) {
            tooltipText.append("\n上网日期: ").append(DateCalculator.formatDate(project.getOnlineDate()));
        }
        if (project.getRegistrationEndDate() != null) {
            tooltipText.append("\n报名截止: ").append(DateCalculator.formatDate(project.getRegistrationEndDate()));
        }
        if (project.getEarliestReviewDate() != null) {
            tooltipText.append("\n最早评审: ").append(DateCalculator.formatDate(project.getEarliestReviewDate()));
        }
        if (project.getExpectedReviewTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            tooltipText.append("\n开标时间: ").append(project.getExpectedReviewTime().format(formatter));
        }
        if (project.getExpertReviewTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            tooltipText.append("\n专家评审时间: ").append(project.getExpertReviewTime().format(formatter));
        }
        
        Tooltip tooltip = new Tooltip(tooltipText.toString());
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setShowDuration(Duration.seconds(20));
        tooltip.setStyle("-fx-background-color: #424242; -fx-text-fill: white;");
        Tooltip.install(projectMarkerBox, tooltip);
        
        // 设置手型光标
        projectMarkerBox.setCursor(Cursor.HAND);
        
        return projectMarkerBox;
    }
    
    /**
     * 检查项目是否过期5天以上
     * @param project 项目
     * @param today 今天的日期
     * @return 是否过期5天以上
     */
    private boolean isProjectExpired(Project project, LocalDate today) {
        // 检查项目的所有关键日期是否都已过期5天以上
        LocalDate fiveDaysAgo = today.minusDays(5);
        
        // 如果有任何一个日期未设置或未过期5天，则项目未过期
        if (project.getOnlineDate() == null || !project.getOnlineDate().isBefore(fiveDaysAgo)) {
            return false;
        }
        
        if (project.getRegistrationEndDate() == null || !project.getRegistrationEndDate().isBefore(fiveDaysAgo)) {
            return false;
        }
        
        if (project.getEarliestReviewDate() == null || !project.getEarliestReviewDate().isBefore(fiveDaysAgo)) {
            return false;
        }
        
        if (project.getExpectedReviewTime() == null || 
            !project.getExpectedReviewTime().toLocalDate().isBefore(fiveDaysAgo)) {
            return false;
        }
        
        // 所有设置了的日期都过期5天以上，项目才算过期
        return true;
    }
    
    /**
     * 创建日历图例项
     * @param color 颜色
     * @param text 图例文字
     * @param isWhiteText 是否使用白色文本
     * @return 图例项
     */
    public HBox createLegendItem(String color, String text, boolean isWhiteText) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Rectangle colorBox = new Rectangle(12, 12);
        colorBox.setFill(Color.web(color));
        colorBox.setStroke(Color.GRAY);
        colorBox.setStrokeWidth(0.5);
        
        Label label = new Label(text);
        label.setFont(Font.font("System", 10));
        
        // 如果需要白色文本（深色背景），则设置白色文本
        if (isWhiteText) {
            label.setStyle("-fx-text-fill: white; -fx-background-color: " + color + "; -fx-padding: 2 5 2 5; -fx-background-radius: 3;");
        } else {
            label.setStyle("-fx-text-fill: #333333;");
        }
        
        item.getChildren().addAll(colorBox, label);
        return item;
    }
    
    /**
     * 项目点击处理器接口
     */
    public interface ProjectClickHandler {
        /**
         * 当项目被点击时调用
         * @param project 被点击的项目
         * @param isDoubleClick 是否为双击
         */
        void onProjectClicked(Project project, boolean isDoubleClick);
    }
    
    /**
     * 日期点击处理器接口
     */
    public interface DateClickHandler {
        /**
         * 当日期被点击时调用
         * @param date 被点击的日期
         */
        void onDateClicked(LocalDate date);
    }
    
    // 添加辅助方法用于格式化日期时间
    private String formatDateTime(LocalDate date) {
        if (date == null) {
            return "";
        }
        return DateCalculator.formatDate(date) + " 00:00";
    }
    
    private String formatDateTime(java.util.Date date) {
        if (date == null) {
            return "";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(date);
    }
    
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }
} 