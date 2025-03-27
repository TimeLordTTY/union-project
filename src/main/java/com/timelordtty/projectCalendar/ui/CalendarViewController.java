package com.timelordtty.projectCalendar.ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.function.Consumer;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.Project;
import com.timelordtty.projectCalendar.service.ProjectService;
import com.timelordtty.projectCalendar.ui.DateCellFactory.DateClickHandler;
import com.timelordtty.projectCalendar.ui.DateCellFactory.ProjectClickHandler;
import com.timelordtty.projectCalendar.utils.HolidayManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

/**
 * 日历视图控制器，负责管理日历视图相关的逻辑
 */
public class CalendarViewController {
    
    private ProjectService projectService;
    private HolidayManager holidayManager;
    private DateCellFactory dateCellFactory;
    private ProjectClickHandler projectClickHandler;
    
    // 日历UI组件
    private GridPane calendarGrid;
    private ScrollPane calendarScrollPane;
    private VBox calendarContainer;
    private Label monthYearLabel;
    
    // 当前显示的年月
    private YearMonth currentYearMonth;
    
    // 回调函数
    private Consumer<Project> onProjectClickedCallback;
    private Consumer<LocalDate> onDateSelectedCallback;
    
    /**
     * 构造函数
     * @param projectService 项目服务
     * @param holidayManager 节假日管理器
     * @param calendarGrid 日历网格
     * @param calendarScrollPane 日历滚动面板
     * @param calendarContainer 日历容器
     * @param monthYearLabel 月份年份标签
     */
    public CalendarViewController(ProjectService projectService, 
                                  HolidayManager holidayManager,
                                  GridPane calendarGrid, 
                                  ScrollPane calendarScrollPane, 
                                  VBox calendarContainer,
                                  Label monthYearLabel) {
        this.projectService = projectService;
        this.holidayManager = holidayManager;
        this.calendarGrid = calendarGrid;
        this.calendarScrollPane = calendarScrollPane;
        this.calendarContainer = calendarContainer;
        this.monthYearLabel = monthYearLabel;
        
        // 创建日期单元格工厂
        this.dateCellFactory = new DateCellFactory(projectService, holidayManager);
        
        // 设置当前月份为系统当前月份
        this.currentYearMonth = YearMonth.now();
        
        // 创建项目点击处理器
        this.projectClickHandler = (project, isDoubleClick) -> {
            if (onProjectClickedCallback != null && project != null) {
                onProjectClickedCallback.accept(project);
            }
        };
        
        // 设置项目点击处理器
        this.dateCellFactory.setProjectClickHandler(projectClickHandler);
    }
    
    /**
     * 构造函数，使用GridPane、ScrollPane和Label
     * @param projectService 项目服务
     * @param holidayManager 节假日管理器
     * @param calendarGrid 日历网格
     * @param calendarScrollPane 日历滚动面板
     * @param monthYearLabel 月份年份标签
     */
    public CalendarViewController(ProjectService projectService, 
                                 HolidayManager holidayManager,
                                 GridPane calendarGrid, 
                                 ScrollPane calendarScrollPane, 
                                 Label monthYearLabel) {
        this(projectService, holidayManager, calendarGrid, calendarScrollPane, null, monthYearLabel);
    }
    
    /**
     * 设置项目点击回调
     * @param callback 项目点击回调
     */
    public void setOnProjectClickedCallback(Consumer<Project> callback) {
        this.onProjectClickedCallback = callback;
    }
    
    /**
     * 设置日期选择回调
     * @param callback 日期选择回调
     */
    public void setOnDateSelectedCallback(Consumer<LocalDate> callback) {
        this.onDateSelectedCallback = callback;
        
        // 创建日期点击处理器
        DateClickHandler dateClickHandler = date -> {
            if (onDateSelectedCallback != null) {
                onDateSelectedCallback.accept(date);
            }
        };
        
        // 设置日期点击处理器
        this.dateCellFactory.setDateClickHandler(dateClickHandler);
    }
    
    /**
     * 初始化日历视图
     */
    public void initialize() {
        try {
            AppLogger.info("初始化日历视图");
            
            // 确保calendarGrid是calendarScrollPane的内容
            calendarScrollPane.setContent(calendarGrid);
            calendarScrollPane.setFitToWidth(true);
            calendarScrollPane.setFitToHeight(true);
            
            // 更新日历视图
            updateCalendarView();
            
            // 更新月份年份标签
            updateMonthYearLabel();
            
            AppLogger.info("日历视图初始化完成");
        } catch (Exception e) {
            AppLogger.error("初始化日历视图失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 更新月份年份标签
     */
    public void updateMonthYearLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月");
        monthYearLabel.setText(currentYearMonth.format(formatter));
    }
    
    /**
     * 切换到上一个月
     */
    public void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateMonthYearLabel();
        updateCalendarView();
    }
    
    /**
     * 切换到下一个月
     */
    public void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateMonthYearLabel();
        updateCalendarView();
    }
    
    /**
     * 导航到今天
     */
    public void navigateToToday() {
        LocalDate today = LocalDate.now();
        navigateToDate(today);
    }
    
    /**
     * 导航到指定日期
     * @param date 目标日期
     */
    public void navigateToDate(LocalDate date) {
        if (date == null) {
            return;
        }
        
        currentYearMonth = YearMonth.from(date);
        updateMonthYearLabel();
        updateCalendarView();
        
        // 通知选中日期
        if (onDateSelectedCallback != null) {
            onDateSelectedCallback.accept(date);
        }
    }
    
    /**
     * 刷新日历
     */
    public void refreshCalendar() {
        updateCalendarView();
    }
    
    /**
     * 更新日历视图
     */
    public void updateCalendarView() {
        try {
            AppLogger.debug("更新日历视图: " + currentYearMonth);
            
            // 检查日历网格和滚动面板是否已正确注入
            if (calendarGrid == null || calendarScrollPane == null) {
                AppLogger.error("无法更新日历视图，UI组件为空");
                return;
            }
            
            // 绘制月视图
            drawMonthView();
            
            // 更新月份标题
            monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")));
            
            // 设置日历图例
            setupCalendarLegend();
            
            AppLogger.debug("日历视图更新完成，当前月份: " + currentYearMonth);
        } catch (Exception e) {
            AppLogger.error("更新日历视图时发生错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 绘制月视图
     */
    private void drawMonthView() {
        try {
            AppLogger.debug("开始绘制月视图：" + currentYearMonth);
            
            // 确保GridPane可见并具有边框
            calendarGrid.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-grid-lines-visible: true;");
            calendarGrid.setVisible(true);
            calendarGrid.setGridLinesVisible(true);
            
            // 清空网格
            calendarGrid.getChildren().clear();
            calendarGrid.getColumnConstraints().clear();
            calendarGrid.getRowConstraints().clear();
            
            // 设置列约束：7列等宽
            for (int i = 0; i < 7; i++) {
                ColumnConstraints column = new ColumnConstraints();
                column.setPercentWidth(100.0 / 7);
                column.setHgrow(Priority.ALWAYS);
                column.setMinWidth(100);
                calendarGrid.getColumnConstraints().add(column);
            }
            
            // 添加星期标题行
            String[] weekDays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            for (int i = 0; i < 7; i++) {
                Label dayLabel = new Label(weekDays[i]);
                dayLabel.getStyleClass().add("calendar-day-header");
                dayLabel.setAlignment(Pos.CENTER);
                dayLabel.setPrefWidth(Double.MAX_VALUE);
                dayLabel.setPrefHeight(40);
                dayLabel.setMaxWidth(Double.MAX_VALUE);
                dayLabel.setMaxHeight(Double.MAX_VALUE);
                // 设置明显的边框和背景色使标题更明显
                dayLabel.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #BDBDBD; -fx-border-width: 0 0 1 0; -fx-font-weight: bold;");
                
                // 将星期标题添加到第一行
                calendarGrid.add(dayLabel, i, 0);
            }
            
            // 获取当前月份的第一天
            LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
            
            // 确定视图的起始日期（从第一个周一开始）
            LocalDate startDate = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            
            // 计算需要的总行数
            // 计算当前月份有多少天
            int daysInMonth = currentYearMonth.lengthOfMonth();
            // 再加上第一天之前需要显示的上个月的天数
            int daysBeforeMonth = firstDayOfMonth.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            if (daysBeforeMonth < 0) daysBeforeMonth += 7; // 调整为正确的天数
            
            // 计算总天数
            int totalDays = daysInMonth + daysBeforeMonth;
            // 计算需要的总行数（每行7天）
            int totalRows = (int) Math.ceil(totalDays / 7.0);
            // 确保至少显示5行，最多显示6行
            totalRows = Math.max(5, Math.min(6, totalRows));
            
            AppLogger.debug("月视图参数：firstDayOfMonth=" + firstDayOfMonth + 
                       ", startDate=" + startDate + 
                       ", daysInMonth=" + daysInMonth + 
                       ", daysBeforeMonth=" + daysBeforeMonth + 
                       ", totalDays=" + totalDays + 
                       ", totalRows=" + totalRows);
            
            // 设置行约束（标题行 + 日期行）
            RowConstraints headerRow = new RowConstraints();
            headerRow.setMinHeight(40);
            headerRow.setPrefHeight(40);
            headerRow.setMaxHeight(40);
            headerRow.setVgrow(Priority.NEVER);
            calendarGrid.getRowConstraints().add(headerRow);
            
            for (int i = 0; i < totalRows; i++) {
                RowConstraints row = new RowConstraints();
                // 日期单元格行高根据总行数动态计算，确保占满剩余空间
                row.setMinHeight(80); // 最小高度保证内容显示
                row.setPrefHeight(100);
                row.setVgrow(Priority.ALWAYS);
                calendarGrid.getRowConstraints().add(row);
            }

            // 开始填充日历网格（从第二行开始，因为第一行是星期标题）
            LocalDate date = startDate;
            for (int i = 0; i < totalRows; i++) {
                for (int j = 0; j < 7; j++) {
                    // 确定此日期是否属于当前月
                    boolean isCurrentMonth = date.getMonth() == currentYearMonth.getMonth();
                    
                    // 创建并添加日期单元格
                    VBox dateCell = dateCellFactory.createDateCell(date, isCurrentMonth);
                    calendarGrid.add(dateCell, j, i + 1); // 注意这里是i+1，因为第0行是星期标题
                    
                    // 设置单元格大小和布局约束
                    GridPane.setFillWidth(dateCell, true);
                    GridPane.setFillHeight(dateCell, true);
                    GridPane.setHgrow(dateCell, Priority.ALWAYS);
                    GridPane.setVgrow(dateCell, Priority.ALWAYS);
                    
                    // 移至下一天
                    date = date.plusDays(1);
                }
            }
            
            // 刷新布局
            calendarGrid.requestLayout();
            
            AppLogger.debug("月视图绘制完成");
        } catch (Exception e) {
            AppLogger.error("绘制月视图时发生错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建日历图例
     */
    private void setupCalendarLegend() {
        try {
            if (calendarContainer == null) {
                AppLogger.error("无法创建日历图例，calendarContainer为空");
                return;
            }
            
            HBox legendContainer = new HBox(10);
            legendContainer.setPadding(new Insets(5));
            legendContainer.setAlignment(Pos.CENTER_LEFT);
            legendContainer.setStyle("-fx-background-color: white; -fx-border-color: #BDBDBD; -fx-border-width: 0.5 0 0 0;");
            legendContainer.setMinHeight(30);
            legendContainer.setMaxHeight(30);
            
            // 今天的图例
            HBox todayLegend = dateCellFactory.createLegendItem("#4caf50", "今天", true);
            
            // 上网日期图例
            HBox onlineLegend = dateCellFactory.createLegendItem("#e3f2fd", "上网日期", false);
            
            // 报名截止图例
            HBox regEndLegend = dateCellFactory.createLegendItem("#fff8e1", "报名截止", false);
            
            // 最早评审图例
            HBox earliestLegend = dateCellFactory.createLegendItem("#e8f5e9", "最早评审", false);
            
            // 预计评审图例
            HBox expectedLegend = dateCellFactory.createLegendItem("#ffebee", "预计评审", false);
            
            legendContainer.getChildren().addAll(
                todayLegend, onlineLegend, regEndLegend, earliestLegend, 
                expectedLegend
            );
            
            // 清除之前的图例（如果有）
            calendarContainer.getChildren().removeIf(node -> node.getId() != null && node.getId().equals("legend"));
            
            legendContainer.setId("legend");
            
            // 确保图例始终显示在ScrollPane之后、提醒区域之前
            int scrollPaneIndex = -1;
            for (int i = 0; i < calendarContainer.getChildren().size(); i++) {
                if (calendarContainer.getChildren().get(i) == calendarScrollPane) {
                    scrollPaneIndex = i;
                    break;
                }
            }
            
            if (scrollPaneIndex >= 0) {
                calendarContainer.getChildren().add(scrollPaneIndex + 1, legendContainer);
            } else {
                calendarContainer.getChildren().add(legendContainer);
            }
            
            // 设置为不随窗口大小变化而被隐藏
            VBox.setVgrow(legendContainer, Priority.NEVER);
        } catch (Exception e) {
            AppLogger.error("创建日历图例时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取当前显示的年月
     * @return 当前年月
     */
    public YearMonth getCurrentYearMonth() {
        return currentYearMonth;
    }
} 