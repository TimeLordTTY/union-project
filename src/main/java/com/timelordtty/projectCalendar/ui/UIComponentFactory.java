package com.timelordtty.projectCalendar.ui;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.Project;
import com.timelordtty.projectCalendar.ProjectAddDialogController;
import com.timelordtty.projectCalendar.service.ProjectService;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * UI组件工厂类，负责创建和管理各种UI组件
 */
public class UIComponentFactory {

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 创建导航按钮
     * @param imageUrl 图标URL
     * @param tooltip 提示文本
     * @return 导航按钮
     */
    public static Button createNavigationButton(String imageUrl, String tooltip) {
        try {
            Button button = new Button();
            button.getStyleClass().add("nav-button");
            
            // 加载图标
            Image image = new Image(UIComponentFactory.class.getResourceAsStream(imageUrl));
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(16);
            imageView.setFitWidth(16);
            button.setGraphic(imageView);
            
            // 设置提示
            if (tooltip != null && !tooltip.isEmpty()) {
                button.setTooltip(new Tooltip(tooltip));
            }
            
            return button;
        } catch (Exception e) {
            AppLogger.error("创建导航按钮失败: " + e.getMessage(), e);
            return new Button("错误");
        }
    }
    
    /**
     * 创建月份年份标签
     * @param yearMonth 年月
     * @return 月份年份标签
     */
    public static Label createMonthYearLabel(YearMonth yearMonth) {
        Label label = new Label();
        label.getStyleClass().add("month-year-label");
        label.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        if (yearMonth != null) {
            label.setText(yearMonth.getYear() + "年" + yearMonth.getMonthValue() + "月");
        } else {
            label.setText("未知日期");
        }
        
        return label;
    }
    
    /**
     * 创建日历导航栏
     * @param prevMonthAction 上个月按钮动作
     * @param nextMonthAction 下个月按钮动作
     * @param todayAction 今天按钮动作
     * @param monthYearLabel 月份年份标签
     * @return 日历导航栏
     */
    public static HBox createCalendarNavigationBar(Runnable prevMonthAction, 
                                                  Runnable nextMonthAction, 
                                                  Runnable todayAction,
                                                  Label monthYearLabel) {
        try {
            HBox navigationBar = new HBox(10);
            navigationBar.setAlignment(Pos.CENTER);
            navigationBar.setPadding(new Insets(5, 10, 5, 10));
            
            // 上个月按钮
            Button prevButton = createNavigationButton("/images/prev.png", "上个月");
            prevButton.setOnAction(e -> {
                if (prevMonthAction != null) {
                    prevMonthAction.run();
                }
            });
            
            // 今天按钮
            Button todayButton = new Button("今天");
            todayButton.getStyleClass().add("today-button");
            todayButton.setOnAction(e -> {
                if (todayAction != null) {
                    todayAction.run();
                }
            });
            
            // 下个月按钮
            Button nextButton = createNavigationButton("/images/next.png", "下个月");
            nextButton.setOnAction(e -> {
                if (nextMonthAction != null) {
                    nextMonthAction.run();
                }
            });
            
            // 添加组件
            navigationBar.getChildren().addAll(prevButton, monthYearLabel, nextButton, todayButton);
            
            return navigationBar;
        } catch (Exception e) {
            AppLogger.error("创建日历导航栏失败: " + e.getMessage(), e);
            return new HBox();
        }
    }
    
    /**
     * 创建星期标题行
     * @return 星期标题行
     */
    public static GridPane createWeekdaysHeader() {
        try {
            GridPane header = new GridPane();
            header.getStyleClass().add("calendar-header");
            header.setPadding(new Insets(5));
            
            String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            
            for (int i = 0; i < 7; i++) {
                StackPane dayOfWeekPane = new StackPane();
                dayOfWeekPane.getStyleClass().add("weekday-cell");
                
                // 周末使用不同的样式
                if (i >= 5) {
                    dayOfWeekPane.getStyleClass().add("weekend");
                }
                
                Label dayOfWeekLabel = new Label(weekdays[i]);
                dayOfWeekLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                dayOfWeekLabel.setAlignment(Pos.CENTER);
                
                dayOfWeekPane.getChildren().add(dayOfWeekLabel);
                header.add(dayOfWeekPane, i, 0);
            }
            
            return header;
        } catch (Exception e) {
            AppLogger.error("创建星期标题行失败: " + e.getMessage(), e);
            return new GridPane();
        }
    }
    
    /**
     * 创建日历图例
     * @return 日历图例
     */
    public static FlowPane createCalendarLegend() {
        try {
            FlowPane legend = new FlowPane();
            legend.setHgap(10);
            legend.setVgap(5);
            legend.setPadding(new Insets(5));
            legend.getStyleClass().add("calendar-legend");
            
            // 今天
            HBox todayLegend = createLegendItem("今天", "today-legend");
            
            // 周末
            HBox weekendLegend = createLegendItem("周末", "weekend-legend");
            
            // 节假日
            HBox holidayLegend = createLegendItem("节假日", "holiday-legend");
            
            // 上网日期
            HBox onlineDateLegend = createLegendItem("上网日期", "online-date-legend");
            
            // 报名截止日期
            HBox registrationEndLegend = createLegendItem("报名截止", "registration-end-legend");
            
            // 评审日期
            HBox reviewDateLegend = createLegendItem("评审日期", "review-date-legend");
            
            // 添加所有图例项
            legend.getChildren().addAll(
                todayLegend, weekendLegend, holidayLegend, 
                onlineDateLegend, registrationEndLegend, reviewDateLegend
            );
            
            return legend;
        } catch (Exception e) {
            AppLogger.error("创建日历图例失败: " + e.getMessage(), e);
            return new FlowPane();
        }
    }
    
    /**
     * 创建图例项
     * @param text 图例文本
     * @param styleClass 样式类
     * @return 图例项
     */
    private static HBox createLegendItem(String text, String styleClass) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER_LEFT);
        
        // 创建图例颜色标记
        StackPane colorMark = new StackPane();
        colorMark.getStyleClass().add(styleClass);
        colorMark.setPrefSize(15, 15);
        
        // 创建图例文本
        Label label = new Label(text);
        label.setFont(Font.font("System", 12));
        
        item.getChildren().addAll(colorMark, label);
        
        return item;
    }
    
    /**
     * 创建项目标记
     * @param project 项目
     * @param markType 标记类型（"online", "registration", "review"）
     * @param onProjectClicked 项目点击回调
     * @return 项目标记
     */
    public static StackPane createProjectMarker(Project project, String markType, Consumer<Project> onProjectClicked) {
        try {
            // 创建项目标记容器
            StackPane marker = new StackPane();
            marker.getStyleClass().add("project-marker");
            
            // 根据类型设置不同的样式
            if ("online".equals(markType)) {
                marker.getStyleClass().add("online-date-marker");
            } else if ("registration".equals(markType)) {
                marker.getStyleClass().add("registration-end-marker");
            } else if ("review".equals(markType)) {
                marker.getStyleClass().add("review-date-marker");
            }
            
            // 设置大小
            marker.setPrefSize(12, 12);
            
            // 创建提示文本
            String dateStr = "";
            String tipText = project.getName();
            
            if ("online".equals(markType)) {
                dateStr = formatDate(project.getOnlineDate());
                tipText += " (上网日期: " + dateStr + ")";
            } else if ("registration".equals(markType)) {
                dateStr = formatDate(project.getRegistrationEndDate());
                tipText += " (报名截止: " + dateStr + ")";
            } else if ("review".equals(markType)) {
                if (project.getExpectedReviewTime() != null) {
                    dateStr = formatDate(project.getExpectedReviewDate()) + " " +
                    String.format("%02d:%02d", project.getExpectedReviewTime().getHour(), project.getExpectedReviewTime().getMinute());
                    tipText += " (开标时间: " + dateStr + ")";
                }
            }
            
            // 设置提示
            Tooltip tooltip = new Tooltip(tipText);
            Tooltip.install(marker, tooltip);
            
            // 设置点击事件
            marker.setOnMouseClicked(event -> {
                if (onProjectClicked != null) {
                    onProjectClicked.accept(project);
                }
            });
            
            // 设置鼠标样式
            marker.setCursor(javafx.scene.Cursor.HAND);
            
            return marker;
        } catch (Exception e) {
            AppLogger.error("创建项目标记失败: " + e.getMessage(), e);
            return new StackPane();
        }
    }
    
    /**
     * 格式化日期
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    private static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "未知日期";
    }
    
    /**
     * 创建项目信息面板
     * @param project 项目
     * @return 项目信息面板
     */
    public static VBox createProjectInfoPanel(Project project) {
        try {
            VBox infoPanel = new VBox(5);
            infoPanel.setPadding(new Insets(10));
            infoPanel.getStyleClass().add("project-info-panel");
            
            // 项目名称
            Label nameLabel = new Label(project.getName());
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            
            // 创建信息网格
            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(10);
            infoGrid.setVgap(5);
            
            // 上网日期
            addInfoRow(infoGrid, 0, "上网日期:", formatDate(project.getOnlineDate()));
            
            // 报名期限
            addInfoRow(infoGrid, 1, "报名期限:", project.getRegistrationPeriod() + " 工作日");
            
            // 报名截止日期
            addInfoRow(infoGrid, 2, "报名截止:", formatDate(project.getRegistrationEndDate()));
            
            // 评审周期
            addInfoRow(infoGrid, 3, "评审周期:", project.getReviewPeriod() + " 自然日");
            
            // 最早评审日期
            addInfoRow(infoGrid, 4, "最早评审:", formatDate(project.getEarliestReviewDate()));
            
            // 开标时间
            String expectedReviewTimeStr = "";
            if (project.getExpectedReviewTime() != null) {
                expectedReviewTimeStr = project.getExpectedReviewTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
            addInfoRow(infoGrid, 5, "开标时间:", expectedReviewTimeStr);
            
            // 专家评审时间
            String expertReviewTimeStr = "";
            if (project.getExpertReviewTime() != null) {
                expertReviewTimeStr = project.getExpertReviewTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
            addInfoRow(infoGrid, 6, "专家评审:", expertReviewTimeStr);
            
            // 备注
            Label remarkLabel = new Label("备注:");
            remarkLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            
            Text remarkText = new Text(project.getRemark());
            remarkText.setWrappingWidth(200);
            
            // 添加所有组件
            infoPanel.getChildren().addAll(nameLabel, infoGrid, remarkLabel, remarkText);
            
            return infoPanel;
        } catch (Exception e) {
            AppLogger.error("创建项目信息面板失败: " + e.getMessage(), e);
            return new VBox();
        }
    }
    
    /**
     * 添加信息行
     * @param grid 网格
     * @param row 行索引
     * @param labelText 标签文本
     * @param valueText 值文本
     */
    private static void addInfoRow(GridPane grid, int row, String labelText, String valueText) {
        Label label = new Label(labelText);
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Label value = new Label(valueText);
        value.setFont(Font.font("System", 12));
        
        grid.add(label, 0, row);
        grid.add(value, 1, row);
    }
    
    /**
     * 创建项目详情面板
     * @param project 项目
     * @param onEditProjectAction 编辑项目动作
     * @param onDeleteProjectAction 删除项目动作
     * @return 项目详情面板
     */
    public static ScrollPane createProjectDetailPanel(Project project, 
                                                     Consumer<Project> onEditProjectAction,
                                                     Consumer<Project> onDeleteProjectAction) {
        try {
            // 创建项目信息面板
            VBox infoPanel = createProjectInfoPanel(project);
            
            // 创建操作按钮容器
            HBox buttonsBox = new HBox(10);
            buttonsBox.setPadding(new Insets(10, 0, 0, 0));
            buttonsBox.setAlignment(Pos.CENTER_RIGHT);
            
            // 编辑按钮
            Button editButton = new Button("编辑");
            editButton.getStyleClass().add("action-button");
            editButton.setOnAction(e -> {
                if (onEditProjectAction != null) {
                    onEditProjectAction.accept(project);
                }
            });
            
            // 删除按钮
            Button deleteButton = new Button("删除");
            deleteButton.getStyleClass().add("delete-button");
            deleteButton.setOnAction(e -> {
                if (onDeleteProjectAction != null) {
                    onDeleteProjectAction.accept(project);
                }
            });
            
            // 添加按钮
            buttonsBox.getChildren().addAll(editButton, deleteButton);
            
            // 添加按钮到信息面板
            infoPanel.getChildren().add(buttonsBox);
            
            // 创建滚动面板
            ScrollPane scrollPane = new ScrollPane(infoPanel);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setPannable(true);
            
            return scrollPane;
        } catch (Exception e) {
            AppLogger.error("创建项目详情面板失败: " + e.getMessage(), e);
            return new ScrollPane();
        }
    }
    
    /**
     * 创建项目添加对话框
     * @param owner 父窗口
     * @param projectService 项目服务
     * @param initialDate 初始日期
     * @return 项目添加对话框
     */
    public static Dialog<Project> createProjectAddDialog(Window owner, ProjectService projectService, LocalDate initialDate) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(UIComponentFactory.class.getResource("/fxml/ProjectAddDialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            ProjectAddDialogController controller = loader.getController();
            controller.setProjectService(projectService);
            
            if (initialDate != null) {
                controller.setInitialDate(initialDate);
            }
            
            Dialog<Project> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("添加项目");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            
            // 设置结果转换器
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getProject();
                }
                return null;
            });
            
            return dialog;
            
        } catch (IOException e) {
            AppLogger.error("创建项目添加对话框时发生IO异常: " + e.getMessage(), e);
            showErrorAlert("创建对话框失败", "无法加载项目添加对话框: " + e.getMessage());
            return null;
        } catch (Exception e) {
            AppLogger.error("创建项目添加对话框时发生未知异常: " + e.getMessage(), e);
            showErrorAlert("创建对话框失败", "创建项目添加对话框时发生未知错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 创建项目编辑对话框
     * @param owner 父窗口
     * @param projectService 项目服务
     * @param project 要编辑的项目
     * @return 项目编辑对话框
     */
    public static Dialog<Project> createProjectEditDialog(Window owner, ProjectService projectService, Project project) {
        if (project == null) {
            AppLogger.error("无法编辑空项目");
            return null;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(UIComponentFactory.class.getResource("/fxml/ProjectAddDialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            ProjectAddDialogController controller = loader.getController();
            controller.setProjectService(projectService);
            controller.setProject(project);
            
            Dialog<Project> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("编辑项目");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            
            // 设置结果转换器
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getProject();
                }
                return null;
            });
            
            return dialog;
            
        } catch (IOException e) {
            AppLogger.error("创建项目编辑对话框时发生IO异常: " + e.getMessage(), e);
            showErrorAlert("创建对话框失败", "无法加载项目编辑对话框: " + e.getMessage());
            return null;
        } catch (Exception e) {
            AppLogger.error("创建项目编辑对话框时发生未知异常: " + e.getMessage(), e);
            showErrorAlert("创建对话框失败", "创建项目编辑对话框时发生未知错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 创建项目详情对话框
     * @param owner 父窗口
     * @param project 项目
     * @return 项目详情对话框
     */
    public static Stage createProjectDetailsStage(Window owner, Project project) {
        if (project == null) {
            AppLogger.error("无法显示空项目详情");
            return null;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(UIComponentFactory.class.getResource("/fxml/ProjectDetailDialog.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("项目详情");
            stage.initOwner(owner);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // 设置项目数据
            ProjectAddDialogController controller = loader.getController();
            controller.setProject(project);
            controller.setReadOnly(true);
            
            return stage;
            
        } catch (IOException e) {
            AppLogger.error("创建项目详情对话框时发生IO异常: " + e.getMessage(), e);
            showErrorAlert("显示项目详情失败", "无法加载项目详情对话框: " + e.getMessage());
            return null;
        } catch (Exception e) {
            AppLogger.error("创建项目详情对话框时发生未知异常: " + e.getMessage(), e);
            showErrorAlert("显示项目详情失败", "创建项目详情对话框时发生未知错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 创建自定义提示框
     * @param text 提示文本
     * @param showDuration 显示时间（秒）
     * @return Tooltip对象
     */
    public static Tooltip createCustomTooltip(String text, double showDuration) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDuration(Duration.seconds(showDuration));
        tooltip.setShowDelay(Duration.millis(100));
        return tooltip;
    }
    
    /**
     * 显示错误提示对话框
     * @param title 标题
     * @param message 消息内容
     */
    public static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示信息提示对话框
     * @param title 标题
     * @param message 消息内容
     */
    public static void showInfoAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示确认对话框
     * @param title 标题
     * @param message 消息内容
     * @return 用户选择结果
     */
    public static boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
} 