package com.timelordtty.projectCalendar;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.service.ProjectService;
import com.timelordtty.projectCalendar.utils.DateCalculator;
import com.timelordtty.projectCalendar.utils.HolidayManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * 项目管理小助手主控制器
 */
public class ProjectCalendarController {
    
    @FXML private TableView<Project> projectTableView;
    @FXML private TableColumn<Project, String> nameColumn;
    @FXML private TableColumn<Project, Integer> reviewPeriodColumn;
    @FXML private TableColumn<Project, String> onlineDateColumn;
    @FXML private TableColumn<Project, String> expectedReviewDateColumn;
    @FXML private TableColumn<Project, String> registrationEndDateColumn;
    @FXML private TableColumn<Project, String> expertReviewDateColumn;
    
    @FXML private Button addProjectButton;
    @FXML private Button prevMonthButton;
    @FXML private Button nextMonthButton;
    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private ScrollPane calendarScrollPane;
    @FXML private VBox calendarContainer;
    @FXML private Label statusLabel;
    @FXML private Button editProjectButton;
    @FXML private Button deleteProjectButton;
    @FXML private HBox reminderScrollArea;
    @FXML private Label scrollingReminderLabel;
    
    private ProjectService projectService;
    private YearMonth currentYearMonth;
    private boolean isMonthView = true; // 默认为月视图
    private Stage currentDatePopup; // 当前日期弹出窗口
    private LocalDate lastClickedDate; // 上次点击的日期
    
    // 节假日管理器
    private HolidayManager holidayManager = HolidayManager.getInstance();
    
    // 滚动提醒相关
    private javafx.animation.Timeline reminderScrollTimeline;
    private List<Project> reminderProjects;
    private int currentReminderIndex = 0;
    private Stage reminderListPopup;
    
    /**
     * 初始化控制器
     */
    @FXML
    private void initialize() {
        try {
            AppLogger.info("初始化项目管理小助手控制器");
            
            // 检查必要组件是否正确注入
            if (calendarGrid == null) {
                AppLogger.error("日历网格(calendarGrid)组件未正确注入");
                return;
            }
            
            if (calendarContainer == null) {
                AppLogger.error("日历容器(calendarContainer)组件未正确注入");
                return;
            }
            
            if (calendarScrollPane == null) {
                AppLogger.error("日历滚动面板(calendarScrollPane)组件未正确注入");
                return;
            }
            
            if (projectTableView == null) {
                AppLogger.error("项目表格(projectTableView)组件未正确注入");
                return;
            }
            
            // 确保calendarGrid是calendarScrollPane的内容
            calendarScrollPane.setContent(calendarGrid);
            calendarScrollPane.setFitToWidth(true);
            calendarScrollPane.setFitToHeight(true);
            
            projectService = ProjectService.getInstance();
            
            // 设置当前月份为系统当前月份
            currentYearMonth = YearMonth.now();
            
            // 初始化表格列
            setupTableColumns();
            
            // 表格双击事件处理
            if (projectTableView != null) {
                projectTableView.setRowFactory(tv -> {
                    TableRow<Project> row = new TableRow<>();
                    row.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && !row.isEmpty()) {
                            showProjectDetail(row.getItem());
                        }
                    });
                    return row;
                });
                
                // 绑定项目列表数据
                projectTableView.setItems(projectService.getProjects());
            }
            
            // 初始化日历视图
            updateCalendarView();
            
            // 显示初始月份
            updateMonthYearLabel();
            
            // 初始化底部滚动提醒
            initScrollingReminders();
            
            AppLogger.info("项目管理小助手控制器初始化完成");
        } catch (Exception e) {
            AppLogger.error("初始化项目管理小助手控制器时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 设置表格列
     */
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
        
        // 报名截止日期列
        registrationEndDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(DateCalculator.formatDate(cellData.getValue().getRegistrationEndDate())));
            
        onlineDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(DateCalculator.formatDate(cellData.getValue().getOnlineDate())));
        
        // 开标时间列
        expectedReviewDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime expectedTime = cellData.getValue().getExpectedReviewTime();
            if (expectedTime != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                return new SimpleStringProperty(expectedTime.format(formatter));
            } else {
                return new SimpleStringProperty(DateCalculator.formatDate(cellData.getValue().getExpectedReviewDate()));
            }
        });
        
        // 专家评审列
        expertReviewDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime expertTime = cellData.getValue().getExpertReviewTime();
            if (expertTime != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                return new SimpleStringProperty(expertTime.format(formatter));
            } else {
                return new SimpleStringProperty("");
            }
        });
    }
    
    /**
     * 处理添加项目按钮点击事件
     */
    @FXML
    private void handleAddProject() {
        try {
            AppLogger.info("开始处理添加项目事件");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/ProjectAddDialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            ProjectAddDialogController controller = loader.getController();
            
            // 创建对话框
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("新增项目");
            
            // 确保对话框大小合适
            dialog.getDialogPane().setPrefSize(450, 550);
            dialog.getDialogPane().setMinHeight(550);
            dialog.getDialogPane().setMinWidth(450);
            
            // 确保对话框位于正确的位置
            dialog.initOwner(addProjectButton.getScene().getWindow());
            
            // 给控制器一点时间初始化
            Platform.runLater(() -> {
                try {
                    AppLogger.info("确保控制器完全初始化");
                    if (controller.getDialogPane() == null) {
                        controller.setDialogPane(dialogPane);
                    }
                    
                    // 添加项目时设置初始日期
                    controller.setInitialDate(LocalDate.now());
                    
                    // 确保名称字段获得焦点
                    Platform.runLater(() -> {
                        dialog.getDialogPane().getScene().getWindow().requestFocus();
                    });
                } catch (Exception e) {
                    AppLogger.error("初始化对话框控制器时发生异常: " + e.getMessage(), e);
                }
            });
            
            AppLogger.info("显示项目添加对话框");
            // 显示对话框并等待直到用户关闭
            Optional<ButtonType> result = dialog.showAndWait();
            
            // 如果用户点击了确定按钮
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // 获取新项目
                Project newProject = controller.getProject();
                if (newProject != null) {
                    // 保存项目
                    boolean saved = projectService.saveProject(newProject);
                    if (saved) {
                        AppLogger.info("成功添加项目: " + newProject.getName());
                        statusLabel.setText("项目 '" + newProject.getName() + "' 添加成功");
                        refreshAllViews();
                    } else {
                        AppLogger.error("添加项目失败: " + newProject.getName());
                        showAlert("添加项目失败", "无法保存新项目，请稍后再试。", javafx.scene.control.Alert.AlertType.ERROR);
                    }
                }
            } else {
                AppLogger.info("用户取消了添加项目");
            }
        } catch (IOException e) {
            AppLogger.error("打开项目添加对话框时发生IO异常: " + e.getMessage(), e);
            showAlert("无法添加项目", "打开项目添加对话框时发生异常: " + e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
        } catch (Exception e) {
            AppLogger.error("添加项目时发生未知异常: " + e.getMessage(), e);
            showAlert("无法添加项目", "添加项目时发生未知异常: " + e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
        }
    }
    
    /**
     * 显示项目详情
     * @param project 要显示详情的项目
     */
    private void showProjectDetail(Project project) {
        try {
            if (project == null) {
                AppLogger.error("无法显示项目详情，project为空");
                return;
            }
            
            AppLogger.info("显示项目详情: " + project.getName());
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/ProjectDetailDialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            // 获取控制器并设置项目
            ProjectDetailDialogController controller = loader.getController();
            controller.setProject(project);
            
            // 设置标签文本
            Label projectNameLabel = (Label) dialogPane.lookup("#projectNameLabel");
            Label onlineDateLabel = (Label) dialogPane.lookup("#onlineDateLabel");
            Label registrationPeriodLabel = (Label) dialogPane.lookup("#registrationPeriodLabel");
            Label registrationEndDateLabel = (Label) dialogPane.lookup("#registrationEndDateLabel");
            Label reviewPeriodLabel = (Label) dialogPane.lookup("#reviewPeriodLabel");
            Label earliestReviewDateLabel = (Label) dialogPane.lookup("#earliestReviewDateLabel");
            Label expectedReviewDateLabel = (Label) dialogPane.lookup("#expectedReviewDateLabel");
            Label expertReviewDateLabel = (Label) dialogPane.lookup("#expertReviewDateLabel");
            Label remarkLabel = (Label) dialogPane.lookup("#remarkLabel");
            
            // 检查是否成功找到所有标签
            if (projectNameLabel == null || onlineDateLabel == null || registrationPeriodLabel == null || 
                registrationEndDateLabel == null || reviewPeriodLabel == null || earliestReviewDateLabel == null || 
                expectedReviewDateLabel == null || expertReviewDateLabel == null || remarkLabel == null) {
                AppLogger.error("项目详情对话框的某些标签元素未找到");
            }
            
            // 设置标签文本
            if (projectNameLabel != null) projectNameLabel.setText(project.getName());
            if (onlineDateLabel != null) onlineDateLabel.setText(DateCalculator.formatDate(project.getOnlineDate()));
            if (registrationPeriodLabel != null) registrationPeriodLabel.setText(project.getRegistrationPeriod() + " 个工作日");
            if (registrationEndDateLabel != null) registrationEndDateLabel.setText(DateCalculator.formatDate(project.getRegistrationEndDate()));
            if (reviewPeriodLabel != null) reviewPeriodLabel.setText(project.getReviewPeriod() + " 个自然日");
            if (earliestReviewDateLabel != null) earliestReviewDateLabel.setText(DateCalculator.formatDate(project.getEarliestReviewDate()));
            
            // 设置开标时间
            if (expectedReviewDateLabel != null) {
                LocalDateTime expectedTime = project.getExpectedReviewTime();
                if (expectedTime != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    expectedReviewDateLabel.setText(expectedTime.format(formatter));
                } else {
                    expectedReviewDateLabel.setText(DateCalculator.formatDate(project.getExpectedReviewDate()));
                }
            }
            
            // 设置专家评审时间
            if (expertReviewDateLabel != null) {
                LocalDateTime expertTime = project.getExpertReviewTime();
                if (expertTime != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    expertReviewDateLabel.setText(expertTime.format(formatter));
                } else {
                    expertReviewDateLabel.setText("未设置");
                }
            }
            
            if (remarkLabel != null) remarkLabel.setText(project.getRemark() != null ? project.getRemark() : "");
            
            // 设置对话框样式
            dialogPane.setStyle("-fx-background-color: #8D6E63; -fx-padding: 10;");
            if (projectNameLabel != null) projectNameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
            
            // 确保dialogPane的内容区域使用更好看的样式
            if (dialogPane.getContent() instanceof GridPane) {
                ((GridPane) dialogPane.getContent()).setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 15;");
            }
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("项目详情");
            dialog.initModality(Modality.APPLICATION_MODAL);
            
            AppLogger.info("显示项目详情对话框: " + project.getName());
            dialog.showAndWait();
            
        } catch (IOException e) {
            AppLogger.error("加载项目详情失败: " + e.getMessage(), e);
        } catch (Exception e) {
            AppLogger.error("显示项目详情时发生未预期异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 编辑项目
     * @param project 要编辑的项目
     */
    private void editProject(Project project) {
        try {
            AppLogger.info("开始编辑项目: " + project.getName());
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/ProjectAddDialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            ProjectAddDialogController controller = loader.getController();
            
            // 创建对话框
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("编辑项目");
            
            // 确保对话框大小合适
            dialog.getDialogPane().setPrefSize(450, 550);
            dialog.getDialogPane().setMinHeight(550);
            dialog.getDialogPane().setMinWidth(450);
            
            // 确保对话框位于正确的位置
            dialog.initOwner(projectTableView.getScene().getWindow());
            
            // 给控制器一点时间初始化
            Platform.runLater(() -> {
                try {
                    AppLogger.info("确保控制器完全初始化");
                    if (controller.getDialogPane() == null) {
                        controller.setDialogPane(dialogPane);
                    }
                    
                    // 设置项目数据（放在这里确保对话框已初始化）
                    controller.setProject(project);
                    
                    // 确保对话框中按钮状态正确
                    javafx.scene.Node okButton = dialogPane.lookupButton(ButtonType.OK);
                    if (okButton != null && project.getName() != null && !project.getName().trim().isEmpty()) {
                        okButton.setDisable(false);
                        AppLogger.info("确保编辑项目对话框确定按钮已启用");
                    }
                } catch (Exception e) {
                    AppLogger.error("初始化对话框控制器时发生异常: " + e.getMessage(), e);
                }
            });
            
            AppLogger.info("显示项目编辑对话框");
            // 显示对话框并等待直到用户关闭
            Optional<ButtonType> result = dialog.showAndWait();
            
            // 如果用户点击了确定按钮
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // 获取编辑后的项目
                Project editedProject = controller.getProject();
                if (editedProject != null) {
                    // 保存项目
                    boolean saved = projectService.saveProject(editedProject);
                    if (saved) {
                        AppLogger.info("成功更新项目: " + editedProject.getName());
                        refreshAllViews();
                    } else {
                        AppLogger.error("更新项目失败: " + editedProject.getName());
                        showAlert("更新项目失败", "无法保存编辑后的项目，请稍后再试。", javafx.scene.control.Alert.AlertType.ERROR);
                    }
                }
            } else {
                AppLogger.info("用户取消了项目编辑");
            }
        } catch (IOException e) {
            AppLogger.error("打开项目编辑对话框时发生IO异常: " + e.getMessage(), e);
            showAlert("无法编辑项目", "打开项目编辑对话框时发生异常: " + e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
        } catch (Exception e) {
            AppLogger.error("编辑项目时发生未知异常: " + e.getMessage(), e);
            showAlert("无法编辑项目", "编辑项目时发生未知异常: " + e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
        }
    }
    
    /**
     * 删除项目
     * @param project 要删除的项目
     */
    private void deleteProject(Project project) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("确认删除");
        confirmDialog.setHeaderText("宝宝不再需要'" + project.getName() + "'这个项目了吗？");
        confirmDialog.setContentText("删除以后就真的没有了哦~");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleteResult = projectService.deleteProject(project);
            if (deleteResult) {
                statusLabel.setText("项目 '" + project.getName() + "' 已删除");
                updateCalendarView(); // 更新日历视图
                refreshReminders(); // 刷新提醒
            } else {
                statusLabel.setText("删除项目失败");
            }
        }
    }
    
    /**
     * 处理上一月按钮点击事件
     */
    @FXML
    private void handlePrevMonth() {
        AppLogger.info("切换到上一月");
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateMonthYearLabel();
        updateCalendarView();
    }
    
    /**
     * 处理下一月按钮点击事件
     */
    @FXML
    private void handleNextMonth() {
        AppLogger.info("切换到下一月");
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateMonthYearLabel();
        updateCalendarView();
    }
    
    /**
     * 更新月份年份标签
     */
    private void updateMonthYearLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月");
        monthYearLabel.setText(currentYearMonth.format(formatter));
    }
    
    /**
     * 更新日历视图
     */
    private void updateCalendarView() {
        try {
            AppLogger.debug("更新日历视图: " + currentYearMonth);
            
            // 检查日历网格和滚动面板是否已正确注入
            if (calendarGrid == null) {
                AppLogger.error("无法更新日历视图，calendarGrid为空");
                return;
            }
            
            if (calendarScrollPane == null) {
                AppLogger.error("无法更新日历视图，calendarScrollPane为空");
                return;
            }
            
            // 确保calendarGrid是calendarScrollPane的内容
            calendarScrollPane.setContent(calendarGrid);
            calendarScrollPane.setFitToWidth(true);
            calendarScrollPane.setFitToHeight(true);
            
            // 绘制月视图
            drawMonthView();
            
            // 更新月份标题
            monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")));
            
            // 设置日历图例
            setupCalendarLegend();
            
            // 输出日志确认视图已更新
            AppLogger.debug("日历视图更新完成，当前月份: " + currentYearMonth);
            
        } catch (Exception e) {
            AppLogger.error("更新日历视图时发生错误", e);
            e.printStackTrace(); // 打印堆栈跟踪以便调试
            Alert alert = new Alert(Alert.AlertType.ERROR, "更新日历视图时发生错误：" + e.getMessage());
            alert.showAndWait();
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
                    VBox dateCell = createDateCell(date, isCurrentMonth);
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
            AppLogger.error("绘制月视图时发生错误", e);
            e.printStackTrace(); // 打印堆栈跟踪以便调试
            Alert alert = new Alert(Alert.AlertType.ERROR, "绘制日历视图时发生错误：" + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * 创建日期单元格
     * @param date 日期
     * @param isCurrentMonth 是否为当前月日期
     * @return 日期单元格
     */
    private VBox createDateCell(LocalDate date, boolean isCurrentMonth) {
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
                if (date.isBefore(currentYearMonth.atDay(1))) {
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
            // 滚动条通过CSS样式设置为透明，保留滚动功能但不可见
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
                    if (project.getExpectedReviewDate() != null && date.equals(project.getExpectedReviewDate())) {
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
            // 只选中当前点击的项目
            projectTableView.getSelectionModel().clearSelection();
            projectTableView.getSelectionModel().select(project);
            projectTableView.scrollTo(project);
            
            // 如果是双击，则显示项目详情
            if (event.getClickCount() == 2) {
                AppLogger.info("双击项目标记，显示项目详情: " + project.getName());
                showProjectDetail(project);
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
            tooltipText.append("\n开标时间: ").append(
                DateCalculator.formatDate(project.getExpectedReviewDate()) + " " +
                String.format("%02d:%02d", project.getExpectedReviewTime().getHour(), project.getExpectedReviewTime().getMinute())
            );
        }
        if (project.getExpertReviewTime() != null) {
            tooltipText.append("\n专家评审: ").append(
                DateCalculator.formatDate(project.getExpertReviewDate()) + " " +
                String.format("%02d:%02d", project.getExpertReviewTime().getHour(), project.getExpertReviewTime().getMinute())
            );
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
     * 创建项目标记 - 水平布局，支持截断
     * @param project 项目
     * @param date 日期
     * @return 项目标记
     */
    private HBox createProjectMarker(Project project, LocalDate date) {
        HBox marker = new HBox(3);
        marker.setAlignment(Pos.CENTER_LEFT);
        marker.setPadding(new Insets(2, 3, 2, 3));
        marker.setMaxWidth(Double.MAX_VALUE);
        
        // 检查项目是否过期5天以上
        boolean isExpired = isProjectExpired(project, LocalDate.now());
        
        // 根据不同类型的日期确定样式
        String markerStyle = "";
        String dateType = getDateType(project, date);
        Color textColor = Color.BLACK;
        
        // 根据日期类型设置不同的背景色
        if (project.getOnlineDate() != null && date.equals(project.getOnlineDate())) {
            markerStyle = "-fx-background-color: #E3F2FD; -fx-background-radius: 3;";
            textColor = Color.BLUE;
        } else if (project.getRegistrationEndDate() != null && date.equals(project.getRegistrationEndDate())) {
            markerStyle = "-fx-background-color: #FFF8E1; -fx-background-radius: 3;";
            textColor = Color.ORANGE;
        } else if (project.getEarliestReviewDate() != null && date.equals(project.getEarliestReviewDate())) {
            markerStyle = "-fx-background-color: #E8F5E9; -fx-background-radius: 3;";
            textColor = Color.GREEN;
        } else if (project.getExpectedReviewDate() != null && date.equals(project.getExpectedReviewDate())) {
            markerStyle = "-fx-background-color: #FFEBEE; -fx-background-radius: 3;";
            textColor = Color.RED;
        } else if (project.getExpertReviewTime() != null && date.equals(project.getExpertReviewTime().toLocalDate())) {
            markerStyle = "-fx-background-color: #E1BEE7; -fx-background-radius: 3;";
            textColor = Color.PURPLE;
        }
        
        // 判断是否需要高亮显示（本周或下周的重要日期）
        LocalDate dateToCheck = null;
        if ((project.getRegistrationEndDate() != null && date.equals(project.getRegistrationEndDate())) || 
            (project.getExpectedReviewDate() != null && date.equals(project.getExpectedReviewDate())) ||
            (project.getExpertReviewTime() != null && date.equals(project.getExpertReviewTime().toLocalDate()))) {
            dateToCheck = date;
        }
        
        boolean isHighlighted = false;
        if (dateToCheck != null) {
            boolean dateInCurrentWeek = isDateInCurrentWeek(dateToCheck);
            boolean dateInNextWeek = isDateInNextWeek(dateToCheck);
            LocalDate today = LocalDate.now();
            boolean isFriday = today.getDayOfWeek() == DayOfWeek.FRIDAY;
            
            // 本周内的重要日期或当天是周五时下周的重要日期需要高亮
            isHighlighted = dateInCurrentWeek || (isFriday && dateInNextWeek);
        }
        
        // 设置不同的样式
        if (isExpired) {
            marker.setStyle("-fx-background-color: #BDBDBD; -fx-background-radius: 3;");
            textColor = Color.WHITE;
        } else if (isHighlighted) {
            // 淡黄色背景高亮显示
            marker.setStyle("-fx-background-color: #FFFDE7; -fx-background-radius: 3; -fx-border-color: #FFD600; -fx-border-radius: 3; -fx-border-width: 1;");
        } else {
            marker.setStyle(markerStyle);
        }
        
        // 项目名称标签 - 截断显示
        Label nameLabel = new Label(project.getName());
        nameLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));
        nameLabel.setTextFill(textColor);
        nameLabel.setMaxWidth(70); // 限制宽度
        nameLabel.setMinWidth(10);
        nameLabel.setEllipsisString(".."); // 截断时显示..
        nameLabel.setWrapText(false); // 不换行
        nameLabel.getStyleClass().add("text-overflow-ellipsis"); // 确保CSS样式支持截断
        nameLabel.setStyle(nameLabel.getStyle() + "-fx-text-overflow: ellipsis;");
        HBox.setHgrow(nameLabel, Priority.SOMETIMES);
        
        // 日期类型标签 - 截断显示
        Label typeLabel = new Label(dateType);
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 9));
        typeLabel.setTextFill(textColor);
        typeLabel.setMaxWidth(40); // 限制宽度
        typeLabel.setMinWidth(10);
        typeLabel.setEllipsisString("."); // 截断时显示.
        typeLabel.setWrapText(false); // 不换行
        typeLabel.getStyleClass().add("text-overflow-ellipsis"); // 确保CSS样式支持截断
        typeLabel.setStyle(typeLabel.getStyle() + "-fx-text-overflow: ellipsis;");
        
        marker.getChildren().addAll(nameLabel, typeLabel);
        marker.setCursor(Cursor.HAND);
        
        // 鼠标点击事件 - 在左侧列表中选中项目
        marker.setOnMouseClicked(event -> {
            event.consume(); // 防止事件冒泡到日期单元格
            
            // 选中项目
            projectTableView.getSelectionModel().clearSelection();
            projectTableView.getSelectionModel().select(project);
            projectTableView.scrollTo(project);
            
            // 如果是双击，显示详情
            if (event.getClickCount() == 2) {
                showProjectDetail(project);
            }
        });
        
        return marker;
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
        
        if (project.getExpectedReviewDate() == null || !project.getExpectedReviewDate().isBefore(fiveDaysAgo)) {
            return false;
        }
        
        // 所有设置了的日期都过期5天以上，项目才算过期
        return true;
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
            HBox todayLegend = createLegendItem("#4caf50", "今天", true);
            
            // 上网日期图例
            HBox onlineLegend = createLegendItem("#e3f2fd", "上网日期", false);
            
            // 报名截止图例
            HBox regEndLegend = createLegendItem("#fff8e1", "报名截止", false);
            
            // 最早评审图例
            HBox earliestLegend = createLegendItem("#e8f5e9", "最早评审", false);
            
            // 开标时间图例
            HBox expectedLegend = createLegendItem("#ffebee", "开标时间", false);
            
            // 专家评审图例
            HBox expertLegend = createLegendItem("#e1bee7", "专家评审", false);
            
            // 添加图例到容器
            legendContainer.getChildren().addAll(
                todayLegend, onlineLegend, regEndLegend, earliestLegend, expectedLegend, expertLegend
            );
            
            // 调整calendarContainer的孩子节点，如果图例已存在则替换，否则添加
            if (calendarContainer.getChildren().size() > 1 && 
                calendarContainer.getChildren().get(calendarContainer.getChildren().size() - 1) instanceof HBox) {
                calendarContainer.getChildren().set(calendarContainer.getChildren().size() - 1, legendContainer);
            } else {
                calendarContainer.getChildren().add(legendContainer);
            }
        } catch (Exception e) {
            AppLogger.error("创建日历图例时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建图例项
     * @param color 颜色
     * @param text 图例文字
     * @param isWhiteText 是否使用白色文本
     * @return 图例项
     */
    private HBox createLegendItem(String color, String text, boolean isWhiteText) {
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
     * 显示提醒面板
     */
    private void showReminderPanel() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/ReminderPanel.fxml"));
            Parent reminderPane = loader.load();
            
            ReminderPanelController controller = loader.getController();
            controller.showReminders();
            
            // 不再支持日期回调
            // controller.setOnDateSelectedCallback(this::navigateToMonth);
            
            // 获取主界面的BorderPane
            BorderPane mainPane = (BorderPane) calendarGrid.getScene().getRoot();
            
            // 将提醒面板添加到主界面右侧（使用浮动面板）
            // 创建一个浮动面板容器
            Pane floatingPane = new Pane();
            floatingPane.getChildren().add(reminderPane);
            
            // 设置提醒面板的位置（右侧外部）
            // 先添加到主布局，稍后在Platform.runLater中调整位置
            mainPane.setRight(null); // 确保右侧没有其他内容
            
            // 添加到主舞台的场景图
            StackPane root = new StackPane();
            mainPane.getScene().setRoot(root);
            root.getChildren().addAll(mainPane, floatingPane);
            
            // 在下一个UI周期调整位置
            Platform.runLater(() -> {
                // 设置提醒面板位置在主面板右侧外部
                double width = reminderPane.prefWidth(-1);
                reminderPane.setTranslateX(mainPane.getWidth());
                
                // 确保提醒面板在垂直居中位置
                double centerY = mainPane.getHeight() / 2;
                double panelHeight = reminderPane.prefHeight(-1);
                reminderPane.setTranslateY(centerY - panelHeight / 2);
                
                // 让提醒面板在主窗口右侧
                StackPane.setAlignment(floatingPane, Pos.CENTER_RIGHT);
            });
            
        } catch (IOException e) {
            AppLogger.error("加载提醒面板失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 导航到指定月份
     * @param targetDate 目标日期
     */
    public void navigateToMonth(LocalDate targetDate) {
        try {
            if (targetDate == null) {
                AppLogger.error("无法导航到指定月份，targetDate为空");
                return;
            }
            
            AppLogger.info("导航到指定月份: " + DateCalculator.formatDate(targetDate));
            currentYearMonth = YearMonth.from(targetDate);
            updateMonthYearLabel();
            updateCalendarView();
        } catch (Exception e) {
            AppLogger.error("导航到指定月份时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 显示日期项目弹出窗口
     * @param projects 项目列表
     * @param date 日期
     * @param x 屏幕X坐标
     * @param y 屏幕Y坐标
     * @return 创建的弹出窗口Stage
     */
    private Stage showDateProjectsPopup(List<Project> projects, LocalDate date, double x, double y) {
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
        
        Label titleLabel = new Label(DateCalculator.formatDateCN(date) + " 的项目");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        content.getChildren().add(titleLabel);
        
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
        content.getChildren().add(separator);
        
        for (Project project : projects) {
            HBox projectRow = new HBox(10);
            projectRow.setPadding(new Insets(5));
            
            String dateType = "";
            if (project.getOnlineDate().equals(date)) {
                dateType = "上网日期";
            } else if (project.getRegistrationEndDate().equals(date)) {
                dateType = "报名截止";
            } else if (project.getEarliestReviewDate().equals(date)) {
                dateType = "最早评审";
            } else if (project.getExpectedReviewDate().equals(date)) {
                dateType = "预计评审";
            }
            
            // 检查项目是否过期
            boolean isExpired = isProjectExpired(project, LocalDate.now());
            String style = isExpired ? 
                "-fx-background-color: #9e9e9e; -fx-text-fill: white;" : 
                "-fx-background-color: #f5f5f5;";
            
            Label nameLabel = new Label(project.getName());
            nameLabel.setFont(Font.font("System", 12));
            
            Label typeLabel = new Label(dateType);
            typeLabel.setFont(Font.font("System", 12));
            typeLabel.setStyle(style + " -fx-padding: 2 5 2 5; -fx-background-radius: 3;");
            
            projectRow.getChildren().addAll(nameLabel, typeLabel);
            content.getChildren().add(projectRow);
            
            // 添加点击事件，选中对应项目并显示详情
            projectRow.setOnMouseClicked(event -> {
                // 在左侧表格中选择该项目
                projectTableView.getSelectionModel().select(project);
                projectTableView.scrollTo(project);
                
                // 如果是双击，显示详情
                if (event.getClickCount() == 2) {
                    if (currentDatePopup != null) {
                        currentDatePopup.close();
                    }
                    showProjectDetail(project);
                }
            });
            projectRow.setCursor(Cursor.HAND);
        }
        
        // 创建弹出窗口
        Stage popup = new Stage();
        popup.initStyle(StageStyle.UNDECORATED);
        popup.setX(x);
        popup.setY(y);
        
        Scene scene = new Scene(content);
        popup.setScene(scene);
        
        // 点击场景外关闭弹窗
        Stage mainStage = (Stage) calendarGrid.getScene().getWindow();
        mainStage.getScene().setOnMousePressed(event -> {
            if (popup.isShowing()) {
                // 检查点击是否在弹窗外
                Point2D screenPoint = new Point2D(event.getScreenX(), event.getScreenY());
                if (!isPointInWindow(popup, screenPoint)) {
                    popup.close();
                    currentDatePopup = null;
                    lastClickedDate = null;
                }
            }
        });
        
        popup.show();
        return popup;
    }
    
    /**
     * 检查点是否在窗口内
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
     * 检查日期是否是节假日
     * @param date 日期
     * @return 是否是节假日
     */
    private boolean isHoliday(LocalDate date) {
        return holidayManager.isHoliday(date);
    }
    
    /**
     * 获取节假日名称
     * @param date 日期
     * @return 节假日名称
     */
    private String getHolidayName(LocalDate date) {
        return holidayManager.getHolidayName(date);
    }
    
    /**
     * 处理编辑项目按钮点击事件
     */
    @FXML
    private void handleEditProject() {
        Project selectedProject = projectTableView.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            editProject(selectedProject);
        } else {
            statusLabel.setText("请先选择一个项目");
        }
    }
    
    /**
     * 处理删除项目按钮点击事件
     */
    @FXML
    private void handleDeleteProject() {
        Project selectedProject = projectTableView.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            deleteProject(selectedProject);
        } else {
            statusLabel.setText("请先选择一个项目");
        }
    }
    
    /**
     * 初始化底部滚动提醒
     */
    private void initScrollingReminders() {
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
        reminderScrollTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(5),
                event -> showNextReminder()
            )
        );
        reminderScrollTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
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
            LocalDate reviewDate = project.getExpectedReviewDate();
            LocalDate expertReviewDate = project.getExpertReviewTime() != null ? 
                project.getExpertReviewTime().toLocalDate() : null;
            
            StringBuilder sb = new StringBuilder(project.getName());
            
            // 检查日期是否在时间范围内
            boolean isRegEndDateInRange = isDateInCurrentWeek(regEndDate) || 
                (today.getDayOfWeek() == DayOfWeek.FRIDAY && isDateInNextWeek(regEndDate));
            
            boolean isReviewDateInRange = isDateInCurrentWeek(reviewDate) || 
                (today.getDayOfWeek() == DayOfWeek.FRIDAY && isDateInNextWeek(reviewDate));
                
            boolean isExpertReviewDateInRange = expertReviewDate != null && 
                (isDateInCurrentWeek(expertReviewDate) || 
                (today.getDayOfWeek() == DayOfWeek.FRIDAY && isDateInNextWeek(expertReviewDate)));
            
            // 添加日期信息
            if (isRegEndDateInRange) {
                sb.append(" - 报名截止: ").append(DateCalculator.formatDate(regEndDate));
            }
            
            if (isReviewDateInRange) {
                String timeStr = "";
                if (project.getExpectedReviewTime() != null) {
                    timeStr = DateCalculator.formatDate(reviewDate) + " " + 
                        String.format("%02d:%02d", project.getExpectedReviewTime().getHour(), project.getExpectedReviewTime().getMinute());
                } else {
                    timeStr = DateCalculator.formatDate(reviewDate);
                }
                
                sb.append(" - 开标时间: ").append(timeStr);
            }
            
            if (isExpertReviewDateInRange && project.getExpertReviewTime() != null) {
                String timeStr = "";
                if (project.getExpertReviewTime() != null) {
                    timeStr = DateCalculator.formatDate(expertReviewDate) + " " + 
                        String.format("%02d:%02d", project.getExpertReviewTime().getHour(), project.getExpertReviewTime().getMinute());
                } else {
                    timeStr = DateCalculator.formatDate(expertReviewDate);
                }
                
                sb.append(" - 专家评审时间: ").append(timeStr);
            }
            
            // 更新提醒文本
            scrollingReminderLabel.setText(sb.toString());
            
            // 更新当前索引
            currentReminderIndex = (currentReminderIndex + 1) % reminderProjects.size();
        } catch (Exception e) {
            AppLogger.error("显示下一条提醒时发生异常: " + e.getMessage(), e);
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
        if (reminderScrollTimeline != null && !reminderScrollTimeline.getStatus().equals(javafx.animation.Animation.Status.RUNNING)) {
            reminderScrollTimeline.play();
        }
    }
    
    /**
     * 显示提醒列表弹出窗口
     */
    private void showReminderListPopup() {
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
            LocalDate expertReviewDate = project.getExpertReviewTime() != null ? 
                project.getExpertReviewTime().toLocalDate() : null;
            
            VBox datesBox = new VBox(2);
            
            // 检查报名截止日期是否在时间范围内
            boolean isRegEndDateInRange = isDateInCurrentWeek(regEndDate) || (today.getDayOfWeek() == DayOfWeek.FRIDAY && isDateInNextWeek(regEndDate));
            
            // 检查开标时间是否在时间范围内
            boolean isReviewDateInRange = isDateInCurrentWeek(reviewDate) || (today.getDayOfWeek() == DayOfWeek.FRIDAY && isDateInNextWeek(reviewDate));
            
            // 检查专家评审时间是否在时间范围内
            boolean isExpertReviewDateInRange = expertReviewDate != null && 
                (isDateInCurrentWeek(expertReviewDate) || (today.getDayOfWeek() == DayOfWeek.FRIDAY && isDateInNextWeek(expertReviewDate)));
            
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
                    
                    // 跳转到对应月份
                    navigateToMonth(regEndDate);
                    
                    // 选中项目
                    Platform.runLater(() -> {
                        projectTableView.getSelectionModel().clearSelection();
                        projectTableView.getSelectionModel().select(project);
                        projectTableView.scrollTo(project);
                    });
                    
                    event.consume();
                });
            }
            
            if (isReviewDateInRange) {
                String timeStr = "";
                if (project.getExpectedReviewTime() != null) {
                    timeStr = DateCalculator.formatDate(reviewDate) + " " + 
                        String.format("%02d:%02d", project.getExpectedReviewTime().getHour(), project.getExpectedReviewTime().getMinute());
                } else {
                    timeStr = DateCalculator.formatDate(reviewDate);
                }
                
                Label reviewLabel = new Label("开标时间: " + timeStr);
                reviewLabel.setStyle("-fx-text-fill: #D32F2F;");
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
                    
                    // 跳转到对应月份
                    navigateToMonth(reviewDate);
                    
                    // 选中项目
                    Platform.runLater(() -> {
                        projectTableView.getSelectionModel().clearSelection();
                        projectTableView.getSelectionModel().select(project);
                        projectTableView.scrollTo(project);
                    });
                    
                    event.consume();
                });
            }
            
            if (isExpertReviewDateInRange) {
                String timeStr = "";
                if (project.getExpertReviewTime() != null) {
                    timeStr = DateCalculator.formatDate(expertReviewDate) + " " + 
                        String.format("%02d:%02d", project.getExpertReviewTime().getHour(), project.getExpertReviewTime().getMinute());
                } else {
                    timeStr = DateCalculator.formatDate(expertReviewDate);
                }
                
                Label expertReviewLabel = new Label("专家评审时间: " + timeStr);
                expertReviewLabel.setStyle("-fx-text-fill: #9C27B0;");
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
                    
                    // 跳转到对应月份
                    navigateToMonth(expertReviewDate);
                    
                    // 选中项目
                    Platform.runLater(() -> {
                        projectTableView.getSelectionModel().clearSelection();
                        projectTableView.getSelectionModel().select(project);
                        projectTableView.scrollTo(project);
                    });
                    
                    event.consume();
                });
            }
            
            projectRow.getChildren().addAll(nameLabel, datesBox);
            HBox.setHgrow(datesBox, Priority.ALWAYS);
            
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
    }
    
    /**
     * 刷新提醒区域
     */
    private void refreshReminders() {
        try {
            AppLogger.info("开始刷新提醒区域");
            // 重新获取提醒项目
            List<Project> oldProjects = reminderProjects;
            reminderProjects = projectService.getThisWeekAndNextWeekProjects();
            
            int oldCount = oldProjects != null ? oldProjects.size() : 0;
            int newCount = reminderProjects != null ? reminderProjects.size() : 0;
            AppLogger.info("提醒项目更新: " + oldCount + " -> " + newCount + " 个项目");
            
            // 如果当前没有项目在显示，立即开始显示
            if (reminderScrollTimeline == null || !reminderScrollTimeline.getStatus().equals(javafx.animation.Animation.Status.RUNNING)) {
                startReminderScroll();
            } else {
                // 否则仅更新当前显示的项目文本
                showNextReminder();
            }
            AppLogger.info("提醒区域刷新完成");
        } catch (Exception e) {
            AppLogger.error("刷新提醒区域时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据项目和日期获取日期类型
     */
    private String getDateType(Project project, LocalDate date) {
        if (project.getOnlineDate() != null && date.equals(project.getOnlineDate())) {
            return "上网";
        }
        if (project.getRegistrationEndDate() != null && date.equals(project.getRegistrationEndDate())) {
            return "报名截止";
        }
        if (project.getEarliestReviewDate() != null && date.equals(project.getEarliestReviewDate())) {
            return "最早评审";
        }
        if (project.getExpectedReviewDate() != null && date.equals(project.getExpectedReviewDate())) {
            return "开标";
        }
        if (project.getExpertReviewTime() != null && date.equals(project.getExpertReviewTime().toLocalDate())) {
            return "专家评审";
        }
        return "其他";
    }
    
    /**
     * 刷新所有视图
     * 供MainController调用，刷新项目表、日历视图和提醒区域
     */
    public void refreshAllViews() {
        try {
            AppLogger.info("开始刷新所有视图");
            
            // 刷新项目列表
            projectService.refreshProjects();
            
            // 刷新项目表格
            projectTableView.setItems(projectService.getProjects());
            projectTableView.refresh();
            
            // 刷新日历视图
            updateCalendarView();
            
            // 刷新提醒区域
            refreshReminders();
            
            AppLogger.info("所有视图刷新完成");
        } catch (Exception e) {
            AppLogger.error("刷新所有视图时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 显示提醒对话框
     * @param title 标题
     * @param message 消息内容
     * @param alertType 对话框类型
     */
    private void showAlert(String title, String message, javafx.scene.control.Alert.AlertType alertType) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}