package com.timelordtty.projectCalendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.service.ProjectService;
import com.timelordtty.projectCalendar.utils.DateCalculator;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 提醒面板控制器
 */
public class ReminderPanelController {
    
    @FXML private StackPane rootPane;
    @FXML private VBox collapsedTab;
    @FXML private VBox expandedPanel;
    @FXML private Button collapseButton;
    @FXML private VBox reminderContainer;
    @FXML private Separator nextWeekSeparator;
    @FXML private HBox nextWeekHeader;
    @FXML private ScrollPane nextWeekScrollPane;
    @FXML private VBox nextWeekContainer;
    
    private ProjectService projectService;
    private Consumer<LocalDate> onDateSelectedCallback;
    private boolean isCollapsed = false;
    
    /**
     * 初始化控制器
     */
    @FXML
    private void initialize() {
        AppLogger.info("初始化提醒面板控制器");
        projectService = ProjectService.getInstance();
        
        // 确保面板初始化为展开状态
        expandedPanel.setVisible(true);
        collapsedTab.setVisible(false);
    }
    
    /**
     * 初始化提醒数据
     */
    public void initReminderData() {
        // 清空之前的内容
        reminderContainer.getChildren().clear();
        nextWeekContainer.getChildren().clear();
        
        // 获取本周和可能的下周项目
        List<Project> projects = projectService.getThisWeekAndNextWeekProjects();
        
        // 判断是否是周五
        boolean isFriday = LocalDate.now().getDayOfWeek() == DayOfWeek.FRIDAY;
        
        // 跟踪是否有项目添加到本周或下周提醒
        boolean hasThisWeekProjects = false;
        boolean hasNextWeekProjects = false;
        
        for (Project project : projects) {
            boolean isThisWeek = projectService.isProjectInThisWeek(project);
            boolean isNextWeek = projectService.isProjectInNextWeek(project);
            
            if (isThisWeek) {
                // 添加到本周提醒
                addProjectToReminderList(project, reminderContainer);
                hasThisWeekProjects = true;
            } else if (isFriday && isNextWeek) {
                // 如果是周五，显示下周提醒
                nextWeekSeparator.setVisible(true);
                nextWeekHeader.setVisible(true);
                nextWeekScrollPane.setVisible(true);
                
                // 添加到下周提醒
                addProjectToReminderList(project, nextWeekContainer);
                hasNextWeekProjects = true;
            }
        }
        
        // 如果没有本周提醒，显示提示信息
        if (!hasThisWeekProjects) {
            Label emptyLabel = new Label("本周没有需要提醒的项目");
            emptyLabel.setTextFill(Color.GRAY);
            reminderContainer.getChildren().add(emptyLabel);
        }
        
        // 如果是周五但没有下周提醒，显示提示信息
        if (isFriday && !hasNextWeekProjects) {
            Label emptyLabel = new Label("下周没有需要提醒的项目");
            emptyLabel.setTextFill(Color.GRAY);
            nextWeekContainer.getChildren().add(emptyLabel);
        } else if (!isFriday) {
            // 如果不是周五，隐藏下周部分
            nextWeekSeparator.setVisible(false);
            nextWeekHeader.setVisible(false);
            nextWeekScrollPane.setVisible(false);
        }
    }
    
    /**
     * 添加项目到提醒列表
     * @param project 项目
     * @param container 容器
     */
    private void addProjectToReminderList(Project project, VBox container) {
        VBox itemBox = new VBox(5);
        itemBox.setPadding(new Insets(5));
        itemBox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 3;");
        
        // 项目名称
        Label nameLabel = new Label(project.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        // 项目关键日期（预计评审日期和报名截止日期）
        HBox datesBox = new HBox(10);
        
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        LocalDate startOfNextWeek = endOfWeek.plusDays(1);
        LocalDate endOfNextWeek = startOfNextWeek.plusDays(6);
        
        LocalDate regEndDate = project.getRegistrationEndDate();
        if ((regEndDate.isEqual(startOfWeek) || regEndDate.isAfter(startOfWeek)) && 
            (regEndDate.isEqual(endOfNextWeek) || regEndDate.isBefore(endOfNextWeek))) {
            
            Label regEndLabel = new Label("报名截止: " + DateCalculator.formatDate(regEndDate));
            regEndLabel.setStyle("-fx-background-color: #fff8e1; -fx-padding: 2 5 2 5; -fx-background-radius: 3;");
            datesBox.getChildren().add(regEndLabel);
        }
        
        LocalDate reviewDate = project.getExpectedReviewDate();
        if ((reviewDate.isEqual(startOfWeek) || reviewDate.isAfter(startOfWeek)) && 
            (reviewDate.isEqual(endOfNextWeek) || reviewDate.isBefore(endOfNextWeek))) {
            
            Label reviewLabel = new Label("预计评审: " + DateCalculator.formatDate(reviewDate));
            reviewLabel.setStyle("-fx-background-color: #f1f8e9; -fx-padding: 2 5 2 5; -fx-background-radius: 3;");
            datesBox.getChildren().add(reviewLabel);
        }
        
        // 点击跳转功能
        itemBox.setOnMouseClicked(event -> {
            // 如果设置了日期选择回调，调用该回调以跳转到项目日期
            if (onDateSelectedCallback != null) {
                // 跳转到最近的一个关键日期（报名截止或预计评审）
                LocalDate targetDate = reviewDate;
                if (regEndDate.isBefore(reviewDate) && regEndDate.isAfter(today)) {
                    targetDate = regEndDate;
                }
                onDateSelectedCallback.accept(targetDate);
            }
            
            // 折叠提醒面板，但不关闭
            handleCollapsePanel();
        });
        
        itemBox.setStyle(itemBox.getStyle() + "-fx-cursor: hand;");
        
        // 将元素添加到项目条目
        itemBox.getChildren().addAll(nameLabel, datesBox);
        
        // 将项目条目添加到容器
        container.getChildren().add(itemBox);
    }
    
    /**
     * 设置日期选择回调
     * @param callback 回调函数
     */
    public void setOnDateSelectedCallback(Consumer<LocalDate> callback) {
        this.onDateSelectedCallback = callback;
    }
    
    /**
     * 处理折叠面板事件
     */
    @FXML
    private void handleCollapsePanel() {
        expandedPanel.setVisible(false);
        collapsedTab.setVisible(true);
        isCollapsed = true;
        
        // 动画效果：向右侧缩回
        Parent parent = rootPane.getParent();
        if (parent != null) {
            // 获取父容器
            Pane parentPane = (Pane) parent;
            double width = expandedPanel.getPrefWidth();
            
            // 设置向右平移效果
            rootPane.setTranslateX(width);
        }
    }
    
    /**
     * 处理展开面板事件
     */
    @FXML
    private void handleExpandPanel() {
        expandedPanel.setVisible(true);
        collapsedTab.setVisible(false);
        isCollapsed = false;
        
        // 动画效果：向左侧展开
        rootPane.setTranslateX(0);
    }
    
    /**
     * 获取面板是否折叠
     * @return 是否折叠状态
     */
    public boolean isCollapsed() {
        return isCollapsed;
    }
    
    /**
     * 设置面板是否折叠
     * @param collapsed 是否折叠
     */
    public void setCollapsed(boolean collapsed) {
        if (collapsed != isCollapsed) {
            if (collapsed) {
                handleCollapsePanel();
            } else {
                handleExpandPanel();
            }
        }
    }
} 