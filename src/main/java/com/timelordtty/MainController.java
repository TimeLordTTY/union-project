package com.timelordtty;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;

import com.timelordtty.projectCalendar.ProjectCalendarController;
import com.timelordtty.ui.SimpleTooltip;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 应用程序主控制器
 */
public class MainController {
    
    @FXML private BorderPane root;
    @FXML private VBox toolsContainer;
    @FXML private StackPane toolContentArea;
    @FXML private Label toolTitle;
    @FXML private Button closeToolButton;
    
    // 主题按钮
    @FXML private Button whiteThemeButton;
    @FXML private Button pinkThemeButton;
    @FXML private Button yellowThemeButton;
    @FXML private Button blueThemeButton;
    @FXML private Button cyanThemeButton;
    @FXML private HBox themeButtonContainer;
    
    @FXML private ProjectCalendarController projectCalendarController;
    
    // 动画时长（毫秒）
    private final int ANIMATION_DURATION = 250; // 稍微快一点更自然
    private final int FADE_DURATION = 200; // 淡入淡出可以更快一些
    
    // 按钮当前状态 - true表示展开状态，false表示收起状态
    private boolean isExpanded = false;
    
    // 当前加载的工具
    private String currentTool = null;
    
    // 工作时间提醒定时器
    private Timer workTimeReminderTimer;
    private final long ONE_HOUR_IN_MS = 60 * 60 * 1000; // 一小时的毫秒数
    
    // 测试用短时间
    // private final long ONE_HOUR_IN_MS = 10 * 1000; // 10秒用于测试
    
    // 主舞台引用
    private Stage mainStage;
    
    /**
     * 设置主舞台引用
     * @param stage 主舞台
     */
    public void setMainStage(Stage stage) {
        this.mainStage = stage;
        AppLogger.info("设置了主舞台引用");
    }
    
    /**
     * 初始化控制器
     */
    @FXML
    private void initialize() {
        try {
            AppLogger.info("初始化MainController");
            
            // 初始化工具容器
            if (toolsContainer != null) {
                toolsContainer.setVisible(false);
                isExpanded = false;
            }
            
            // 高亮粉色主题按钮
            if (pinkThemeButton != null) {
                pinkThemeButton.setStyle(pinkThemeButton.getStyle().replace("-fx-border-color: transparent;", "-fx-border-color: #424242; -fx-border-width: 2px;"));
            }
            
            AppLogger.info("MainController初始化完成");
        } catch (Exception e) {
            AppLogger.error("初始化MainController时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 启动工作时间提醒计时器
     * 每隔一小时发送一次喝水休息的提醒
     */
    public void startWorkTimeReminder() {
        try {
            AppLogger.info("启动工作时间提醒计时器");
            
            // 创建定时器线程
            Thread reminderThread = new Thread(() -> {
                try {
                    // 开始计时循环
                    while (true) {
                        // 等待一小时
                        Thread.sleep(60 * 60 * 1000); // 60分钟 * 60秒 * 1000毫秒
                        
                        // 在JavaFX应用线程中显示提醒
                        javafx.application.Platform.runLater(() -> {
                            showWorkTimeReminder();
                        });
                    }
                } catch (InterruptedException e) {
                    AppLogger.warning("工作时间提醒线程被中断: " + e.getMessage());
                }
            });
            
            // 设置为守护线程，不阻止JVM退出
            reminderThread.setDaemon(true);
            reminderThread.start();
            
            AppLogger.info("工作时间提醒计时器已启动");
        } catch (Exception e) {
            AppLogger.error("启动工作时间提醒计时器时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 显示工作时间提醒气泡
     */
    private void showWorkTimeReminder() {
        try {
            AppLogger.info("显示工作时间提醒气泡");
            
            // 创建带提醒消息的气泡
            SimpleTooltip tooltip = new SimpleTooltip("宝宝已经工作一个小时啦，要站起来活动活动喝点水哦💖💖💖~");
            
            // 应用当前主题
            applyCurrentThemeToTooltip(tooltip);
            
            // 优先使用主舞台引用
            if (mainStage != null) {
                tooltip.showInStage(mainStage);
                AppLogger.info("成功使用主舞台引用显示工作时间提醒气泡");
                return;
            }
            
            // 如果没有主舞台引用，尝试从root获取
            if (root != null && root.getScene() != null && root.getScene().getWindow() != null) {
                Stage stage = (Stage) root.getScene().getWindow();
                tooltip.showInStage(stage);
                AppLogger.info("成功显示工作时间提醒气泡");
            } else {
                AppLogger.warning("无法获取舞台，无法显示工作时间提醒");
            }
        } catch (Exception e) {
            AppLogger.error("显示工作时间提醒时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 显示欢迎气泡
     * 由MainApp在启动完成后调用
     */
    public void showWelcomeBubble() {
        try {
            AppLogger.info("准备显示欢迎气泡");
            
            // 创建气泡
            SimpleTooltip tooltip = new SimpleTooltip("欢迎回来，最棒的宝宝~~");
            
            // 应用当前主题
            applyCurrentThemeToTooltip(tooltip);
            
            // 首先尝试使用保存的主舞台引用
            if (mainStage != null) {
                tooltip.showInStage(mainStage);
                AppLogger.info("成功使用主舞台引用显示欢迎气泡");
                return;
            }
            
            // 如果没有主舞台引用，尝试从root获取
            if (root != null && root.getScene() != null && root.getScene().getWindow() != null) {
                Stage stage = (Stage) root.getScene().getWindow();
                tooltip.showInStage(stage);
                AppLogger.info("成功显示欢迎气泡");
            } else {
                AppLogger.warning("root或scene或window为null，延迟显示欢迎气泡");
                
                // 使用延迟检查方法
                delayedShowBubble(20, 200); // 增加尝试次数和延迟
            }
        } catch (Exception e) {
            AppLogger.error("显示欢迎气泡时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 延迟显示欢迎气泡的方法
     * @param maxAttempts 最大尝试次数
     * @param delayMs 每次尝试间隔（毫秒）
     */
    private void delayedShowBubble(int maxAttempts, int delayMs) {
        AppLogger.info("开始延迟尝试显示欢迎气泡，最多尝试" + maxAttempts + "次");
        
        final int[] attempts = {0};
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(delayMs), event -> {
                attempts[0]++;
                AppLogger.info("尝试显示欢迎气泡：第" + attempts[0] + "次");
                
                // 首先尝试使用主舞台引用
                if (mainStage != null) {
                    SimpleTooltip tooltip = new SimpleTooltip("欢迎回来，最棒的宝宝~~");
                    applyCurrentThemeToTooltip(tooltip);
                    tooltip.showInStage(mainStage);
                    AppLogger.info("延迟后使用主舞台引用成功显示欢迎气泡");
                    return;
                }
                
                if (root != null && root.getScene() != null && root.getScene().getWindow() != null) {
                    Stage stage = (Stage) root.getScene().getWindow();
                    SimpleTooltip tooltip = new SimpleTooltip("欢迎回来，最棒的宝宝~~");
                    applyCurrentThemeToTooltip(tooltip);
                    tooltip.showInStage(stage);
                    AppLogger.info("延迟后成功显示欢迎气泡");
                    return;
                }
                
                if (attempts[0] >= maxAttempts) {
                    AppLogger.warning("尝试" + maxAttempts + "次后仍无法显示欢迎气泡");
                }
            })
        );
        
        timeline.setCycleCount(maxAttempts);
        timeline.play();
    }
    
    /**
     * 应用当前主题到气泡
     * @param tooltip 气泡对象
     */
    private void applyCurrentThemeToTooltip(SimpleTooltip tooltip) {
        // 获取根节点的样式类列表，确定当前使用的主题
        String currentTheme = "pink"; // 默认为粉色主题
        
        if (root != null) {
            if (root.getStyleClass().contains("white-theme")) {
                currentTheme = "white";
            } else if (root.getStyleClass().contains("yellow-theme")) {
                currentTheme = "yellow";
            } else if (root.getStyleClass().contains("blue-theme")) {
                currentTheme = "blue";
            } else if (root.getStyleClass().contains("cyan-theme")) {
                currentTheme = "cyan";
            }
        }
        
        // 应用主题
        tooltip.applyTheme(currentTheme + "-theme");
        AppLogger.info("应用" + currentTheme + "主题到气泡");
    }
    
    /**
     * 创建并显示气泡在场景上
     * @param tooltip 气泡对象
     * @param scene 当前场景
     * @param stage 当前舞台
     */
    private void createAndShowBubble(SimpleTooltip tooltip, Scene scene, Stage stage) {
        try {
            // 直接在舞台上显示气泡，无需修改场景
            tooltip.showInStage(stage);
            AppLogger.info("气泡显示成功");
        } catch (Exception e) {
            AppLogger.error("创建并显示气泡时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 应用主题样式
     * @param themeName 主题名称（white/pink/yellow/blue/cyan）
     */
    private void applyTheme(String themeName) {
        // 首先重置所有主题按钮的样式
        if (whiteThemeButton != null) whiteThemeButton.setStyle("-fx-min-width: 25px; -fx-max-width: 25px; -fx-min-height: 25px; -fx-max-height: 25px; -fx-background-radius: 50%; -fx-border-radius: 50%; -fx-background-color: #F5F5F5; -fx-border-color: transparent; -fx-border-width: 1px; -fx-cursor: hand;");
        if (pinkThemeButton != null) pinkThemeButton.setStyle("-fx-min-width: 25px; -fx-max-width: 25px; -fx-min-height: 25px; -fx-max-height: 25px; -fx-background-radius: 50%; -fx-border-radius: 50%; -fx-background-color: #FFCDD2; -fx-border-color: transparent; -fx-border-width: 1px; -fx-cursor: hand;");
        if (yellowThemeButton != null) yellowThemeButton.setStyle("-fx-min-width: 25px; -fx-max-width: 25px; -fx-min-height: 25px; -fx-max-height: 25px; -fx-background-radius: 50%; -fx-border-radius: 50%; -fx-background-color: #FFF9C4; -fx-border-color: transparent; -fx-border-width: 1px; -fx-cursor: hand;");
        if (blueThemeButton != null) blueThemeButton.setStyle("-fx-min-width: 25px; -fx-max-width: 25px; -fx-min-height: 25px; -fx-max-height: 25px; -fx-background-radius: 50%; -fx-border-radius: 50%; -fx-background-color: #BBDEFB; -fx-border-color: transparent; -fx-border-width: 1px; -fx-cursor: hand;");
        if (cyanThemeButton != null) cyanThemeButton.setStyle("-fx-min-width: 25px; -fx-max-width: 25px; -fx-min-height: 25px; -fx-max-height: 25px; -fx-background-radius: 50%; -fx-border-radius: 50%; -fx-background-color: #B2EBF2; -fx-border-color: transparent; -fx-border-width: 1px; -fx-cursor: hand;");
        
        // 高亮当前活动主题按钮
        Button activeButton = null;
        switch (themeName) {
            case "white":
                activeButton = whiteThemeButton;
                break;
            case "pink":
                activeButton = pinkThemeButton;
                break;
            case "yellow":
                activeButton = yellowThemeButton;
                break;
            case "blue":
                activeButton = blueThemeButton;
                break;
            case "cyan":
                activeButton = cyanThemeButton;
                break;
        }
        
        // 高亮当前活动主题按钮
        if (activeButton != null) {
            activeButton.setStyle(activeButton.getStyle().replace("-fx-border-color: transparent;", "-fx-border-color: #424242; -fx-border-width: 2px;"));
        }
        
        // 应用样式到整个场景树
        AppLogger.info("应用" + themeName + "主题");
        
        if (root != null) {
            // 如果场景已经存在，直接应用到场景
            if (root.getScene() != null) {
                Scene scene = root.getScene();
                
                // 设置CSS变量
                switch (themeName) {
                    case "white":
                        root.setStyle("-theme-primary: -white-primary; -theme-light: -white-light; -theme-dark: -white-dark; -theme-accent: -white-accent; -theme-text: -white-text; -theme-border: -white-border; -theme-shadow: -white-shadow; -theme-background: -white-background;");
                        break;
                    case "pink":
                        root.setStyle("-theme-primary: -pink-primary; -theme-light: -pink-light; -theme-dark: -pink-dark; -theme-accent: -pink-accent; -theme-text: -pink-text; -theme-border: -pink-border; -theme-shadow: -pink-shadow; -theme-background: -pink-background;");
                        break;
                    case "yellow":
                        root.setStyle("-theme-primary: -yellow-primary; -theme-light: -yellow-light; -theme-dark: -yellow-dark; -theme-accent: -yellow-accent; -theme-text: -yellow-text; -theme-border: -yellow-border; -theme-shadow: -yellow-shadow; -theme-background: -yellow-background;");
                        break;
                    case "blue":
                        root.setStyle("-theme-primary: -blue-primary; -theme-light: -blue-light; -theme-dark: -blue-dark; -theme-accent: -blue-accent; -theme-text: -blue-text; -theme-border: -blue-border; -theme-shadow: -blue-shadow; -theme-background: -blue-background;");
                        break;
                    case "cyan":
                        root.setStyle("-theme-primary: -cyan-primary; -theme-light: -cyan-light; -theme-dark: -cyan-dark; -theme-accent: -cyan-accent; -theme-text: -cyan-text; -theme-border: -cyan-border; -theme-shadow: -cyan-shadow; -theme-background: -cyan-background;");
                        break;
                }
                
                // 移除所有主题样式类
                root.getStyleClass().removeAll(
                    "white-theme", "pink-theme", "yellow-theme", "blue-theme", "cyan-theme");
                
                // 添加新的主题样式类
                root.getStyleClass().add(themeName + "-theme");
                
                try {
                    // 重新加载样式表
                    String cssPath = getClass().getResource("/css/styles.css").toExternalForm();
                    if (!scene.getStylesheets().contains(cssPath)) {
                        scene.getStylesheets().add(cssPath);
                    } else {
                        // 移除并重新添加样式表，强制刷新
                        scene.getStylesheets().remove(cssPath);
                        scene.getStylesheets().add(cssPath);
                    }
                    
                    // 尝试强制UI更新
                    root.applyCss();
                    root.layout();
                    
                    // 递归应用主题样式到子节点
                    applyThemeToChildren(root, themeName);
                    
                    AppLogger.info("成功应用样式表: " + cssPath);
                } catch (Exception e) {
                    AppLogger.error("应用样式表时出错: " + e.getMessage(), e);
                }
            } else {
                // 设置CSS变量
                switch (themeName) {
                    case "white":
                        root.setStyle("-theme-primary: -white-primary; -theme-light: -white-light; -theme-dark: -white-dark; -theme-accent: -white-accent; -theme-text: -white-text; -theme-border: -white-border; -theme-shadow: -white-shadow; -theme-background: -white-background;");
                        break;
                    case "pink":
                        root.setStyle("-theme-primary: -pink-primary; -theme-light: -pink-light; -theme-dark: -pink-dark; -theme-accent: -pink-accent; -theme-text: -pink-text; -theme-border: -pink-border; -theme-shadow: -pink-shadow; -theme-background: -pink-background;");
                        break;
                    case "yellow":
                        root.setStyle("-theme-primary: -yellow-primary; -theme-light: -yellow-light; -theme-dark: -yellow-dark; -theme-accent: -yellow-accent; -theme-text: -yellow-text; -theme-border: -yellow-border; -theme-shadow: -yellow-shadow; -theme-background: -yellow-background;");
                        break;
                    case "blue":
                        root.setStyle("-theme-primary: -blue-primary; -theme-light: -blue-light; -theme-dark: -blue-dark; -theme-accent: -blue-accent; -theme-text: -blue-text; -theme-border: -blue-border; -theme-shadow: -blue-shadow; -theme-background: -blue-background;");
                        break;
                    case "cyan":
                        root.setStyle("-theme-primary: -cyan-primary; -theme-light: -cyan-light; -theme-dark: -cyan-dark; -theme-accent: -cyan-accent; -theme-text: -cyan-text; -theme-border: -cyan-border; -theme-shadow: -cyan-shadow; -theme-background: -cyan-background;");
                        break;
                }
                
                // 移除所有主题样式类
                root.getStyleClass().removeAll(
                    "white-theme", "pink-theme", "yellow-theme", "blue-theme", "cyan-theme");
                
                // 添加新的主题样式类
                root.getStyleClass().add(themeName + "-theme");
            }
        }
        
        // 刷新项目日历视图
        if (projectCalendarController != null) {
            try {
                projectCalendarController.refreshAllViews();
                AppLogger.info("已刷新项目日历视图");
            } catch (Exception e) {
                AppLogger.error("刷新项目日历视图时出错: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 递归地将主题应用到所有子节点
     * @param parent 父节点
     * @param themeName 主题名称
     */
    private void applyThemeToChildren(javafx.scene.Parent parent, String themeName) {
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            try {
                // 如果节点是BorderPane、VBox、HBox等容器组件
                if (node instanceof javafx.scene.layout.Region) {
                    javafx.scene.layout.Region region = (javafx.scene.layout.Region) node;
                    
                    // 设置CSS变量
                    String existingStyle = region.getStyle();
                    
                    // 移除所有主题样式类
                    node.getStyleClass().removeAll(
                        "white-theme", "pink-theme", "yellow-theme", "blue-theme", "cyan-theme");
                    
                    // 添加新的主题样式类
                    node.getStyleClass().add(themeName + "-theme");
                    
                    // 强制更新CSS样式
                    region.applyCss();
                }
                
                // 递归处理子节点
                if (node instanceof javafx.scene.Parent) {
                    applyThemeToChildren((javafx.scene.Parent) node, themeName);
                }
            } catch (Exception e) {
                AppLogger.error("应用主题到子节点时出错: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 处理白色主题按钮点击 - 已禁用
     */
    @FXML
    private void handleWhiteThemeClick() {
        // 已禁用主题切换
    }
    
    /**
     * 处理粉色主题按钮点击 - 已禁用
     */
    @FXML
    private void handlePinkThemeClick() {
        // 已禁用主题切换
    }
    
    /**
     * 处理黄色主题按钮点击 - 已禁用
     */
    @FXML
    private void handleYellowThemeClick() {
        // 已禁用主题切换
    }
    
    /**
     * 处理蓝色主题按钮点击 - 已禁用
     */
    @FXML
    private void handleBlueThemeClick() {
        // 已禁用主题切换
    }
    
    /**
     * 处理青色主题按钮点击 - 已禁用
     */
    @FXML
    private void handleCyanThemeClick() {
        // 已禁用主题切换
    }
    
    /**
     * 处理关闭工具按钮点击
     */
    @FXML
    private void handleCloseToolClick() {
        if (isExpanded) {
            // 当前是展开状态，收起
            closeToolButton.setText("展开 ▼");
            animateClose();
            isExpanded = false;
        } else {
            // 当前是收起状态，展开
            closeToolButton.setText("收起 ▲");
            animateOpen();
            isExpanded = true;
        }
    }
    
    /**
     * 收起动画
     */
    private void animateClose() {
        // 创建向上移动动画
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(ANIMATION_DURATION), toolsContainer);
        translateTransition.setFromY(0);
        translateTransition.setToY(-50); // 增大移动距离让效果更明显
        
        // 创建淡出动画
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(FADE_DURATION), toolsContainer);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        
        // 同时播放两个动画
        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(translateTransition, fadeTransition);
        
        // 动画结束后隐藏工具容器
        parallelTransition.setOnFinished(event -> {
            toolsContainer.setVisible(false);
            // 重置Y位置，以便下次动画
            toolsContainer.setTranslateY(0);
        });
        
        // 开始播放动画
        parallelTransition.play();
    }
    
    /**
     * 展开动画
     */
    private void animateOpen() {
        // 先设置初始状态
        toolsContainer.setOpacity(0.0);
        toolsContainer.setTranslateY(-50); // 与收起动画保持一致
        toolsContainer.setVisible(true);
        
        // 创建向下移动动画
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(ANIMATION_DURATION), toolsContainer);
        translateTransition.setFromY(-50);
        translateTransition.setToY(0);
        
        // 创建淡入动画
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(FADE_DURATION), toolsContainer);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        
        // 同时播放两个动画
        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(translateTransition, fadeTransition);
        
        // 开始播放动画
        parallelTransition.play();
    }
    
    /**
     * 工具内容切换动画
     * @param toolView 新的工具视图
     */
    private void animateToolChange(javafx.scene.Parent toolView, String title) {
        // 创建当前内容淡出动画
        FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_DURATION), toolContentArea);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        // 淡出完成后切换内容，然后淡入
        fadeOut.setOnFinished(e -> {
            // 清空并设置新内容
            toolContentArea.getChildren().clear();
            toolContentArea.getChildren().add(toolView);
            toolTitle.setText(title);
            
            // 创建新内容淡入动画
            FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION), toolContentArea);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        // 开始淡出动画
        fadeOut.play();
    }
    
    /**
     * 处理金额转换工具按钮点击
     */
    @FXML
    private void handleAmountToolClick() {
        try {
            AppLogger.info("打开金额转换工具");
            loadTool("金额转换", "/fxml/AmountConverterView.fxml");
        } catch (Exception e) {
            AppLogger.error("打开金额转换工具失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 处理文档生成工具按钮点击
     */
    @FXML
    private void handleDocGenToolClick() {
        AppLogger.info("点击文档生成工具按钮");
        // 切换到文档生成器
        loadTool("文档生成", "/fxml/DocumentGeneratorView.fxml");
        // 直接将扩展状态设为true，确保总是以全窗口模式展示
        isExpanded = true;
        // 调用animateOpen确保UI状态与isExpanded一致
        animateOpen();
    }
    
    /**
     * 处理文本纠错工具按钮点击
     */
    @FXML
    private void handleTextCorrectionToolClick() {
        try {
            AppLogger.info("打开文本纠错工具");
            loadTool("文本纠错", "/fxml/TextCorrectorView.fxml");
        } catch (Exception e) {
            AppLogger.error("打开文本纠错工具失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 加载工具
     * @param title 工具标题
     * @param fxmlPath 工具FXML路径
     */
    private void loadTool(String title, String fxmlPath) {
        try {
            // 记录正在尝试加载的路径
            AppLogger.info("尝试加载工具: " + title + ", 路径: " + fxmlPath);
            
            // 记录类加载器信息
            ClassLoader cl = getClass().getClassLoader();
            AppLogger.info("使用类加载器: " + cl);
            
            // 加载方法1: 使用URL
            java.net.URL url = getClass().getResource(fxmlPath);
            if (url != null) {
                AppLogger.info("使用URL加载FXML: " + url);
                // 创建FXMLLoader并设置location
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(url);
                
                try {
                    // 加载FXML内容
                    javafx.scene.Parent toolView = loader.load();
                    AppLogger.info("URL方式成功加载FXML, 控制器: " + loader.getController());
                    
                    // 处理加载后的视图
                    handleToolViewAfterLoad(toolView, title, fxmlPath);
                    return;
                } catch (Exception e) {
                    AppLogger.error("URL方式加载FXML失败: " + e.getMessage(), e);
                    AppLogger.error("异常类型: " + e.getClass().getName());
                }
            } else {
                AppLogger.info("URL为null，尝试其他加载方式");
            }

            // 加载方法2: 使用类加载器
            url = getClass().getClassLoader().getResource(fxmlPath.substring(1));
            if (url != null) {
                AppLogger.info("使用类加载器URL加载FXML: " + url);
                // 创建FXMLLoader并设置location
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(url);
                
                try {
                    // 加载FXML内容
                    javafx.scene.Parent toolView = loader.load();
                    AppLogger.info("类加载器方式成功加载FXML, 控制器: " + loader.getController());
                    
                    // 处理加载后的视图
                    handleToolViewAfterLoad(toolView, title, fxmlPath);
                    return;
                } catch (Exception e) {
                    AppLogger.error("类加载器方式加载FXML失败: " + e.getMessage(), e);
                    AppLogger.error("异常类型: " + e.getClass().getName());
                }
            } else {
                AppLogger.info("类加载器URL为null，尝试其他加载方式");
            }

            // 加载方法3: 使用InputStream
            try (InputStream fxmlStream = getClass().getResourceAsStream(fxmlPath)) {
                if (fxmlStream != null) {
                    // 创建临时字节数组来存储FXML内容
                    byte[] fxmlBytes = fxmlStream.readAllBytes();
                    AppLogger.info("使用输入流加载FXML，流大小: " + fxmlBytes.length + " 字节");
                    
                    // 使用新的输入流，避免流已关闭的问题
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(fxmlBytes)) {
                        // 创建FXMLLoader
                        FXMLLoader loader = new FXMLLoader();
                        
                        // 确保设置正确的URL，避免"Location is not set"错误
                        url = getClass().getResource(fxmlPath);
                        if (url != null) {
                            loader.setLocation(url);
                        }
                        
                        // 确保CSS样式表能被正确加载
                        loader.setClassLoader(getClass().getClassLoader());
                        
                        try {
                            // 加载FXML内容
                            javafx.scene.Parent toolView = loader.load(bais);
                            AppLogger.info("输入流方式成功加载FXML, 控制器: " + loader.getController());
                            
                            // 手动加载CSS样式
                            String cssPath = "/css/styles.css";
                            url = getClass().getResource(cssPath);
                            if (url != null && toolView.getStylesheets().isEmpty()) {
                                toolView.getStylesheets().add(url.toExternalForm());
                                AppLogger.info("手动添加样式表: " + url.toExternalForm());
                            }
                            
                            // 处理加载后的视图
                            handleToolViewAfterLoad(toolView, title, fxmlPath);
                            return;
                        } catch (Exception e) {
                            AppLogger.error("输入流方式加载FXML失败: " + e.getMessage(), e);
                            AppLogger.error("异常类型: " + e.getClass().getName());
                            AppLogger.error("异常堆栈: ", e);
                        }
                    }
                } else {
                    AppLogger.info("输入流为null，尝试其他加载方式");
                }
            } catch (Exception e) {
                AppLogger.error("创建输入流时出错: " + e.getMessage(), e);
            }

            // 加载方法4: 尝试从文件系统加载
            String[] paths = {
                "target/classes" + fxmlPath,
                "resources" + fxmlPath,
                "src/main/resources" + fxmlPath
            };
            
            for (String path : paths) {
                java.io.File file = new java.io.File(path);
                AppLogger.info("检查文件系统路径: " + file.getAbsolutePath() + ", 存在: " + file.exists());
                
                if (file.exists()) {
                    try (java.io.FileInputStream fxmlStream = new java.io.FileInputStream(file)) {
                        // 读取文件内容到字节数组
                        byte[] fileBytes = fxmlStream.readAllBytes();
                        AppLogger.info("从文件系统加载FXML: " + file.getAbsolutePath() + ", 大小: " + fileBytes.length + " 字节");
                        
                        // 使用字节数组创建新的输入流
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes)) {
                            // 创建FXMLLoader
                            FXMLLoader loader = new FXMLLoader();
                            
                            // 设置location，使用file:URL协议
                            try {
                                java.net.URL fileUrl = file.toURI().toURL();
                                loader.setLocation(fileUrl);
                                AppLogger.info("设置FXMLLoader location为文件URL: " + fileUrl);
                            } catch (Exception e) {
                                AppLogger.error("无法将文件转换为URL: " + e.getMessage(), e);
                            }
                            
                            try {
                                // 加载FXML
                                javafx.scene.Parent toolView = loader.load(bais);
                                AppLogger.info("文件系统方式成功加载FXML, 控制器: " + loader.getController());
                                
                                // 手动加载CSS样式
                                String cssFile = "src/main/resources/css/styles.css";
                                if (new java.io.File(cssFile).exists()) {
                                    String cssUrl = new java.io.File(cssFile).toURI().toURL().toExternalForm();
                                    toolView.getStylesheets().add(cssUrl);
                                    AppLogger.info("手动添加样式表: " + cssUrl);
                                }
                                
                                // 处理加载后的视图
                                handleToolViewAfterLoad(toolView, title, fxmlPath);
                                return;
                            } catch (Exception e) {
                                AppLogger.error("文件系统方式加载FXML失败: " + e.getMessage(), e);
                                AppLogger.error("异常类型: " + e.getClass().getName());
                                AppLogger.error("异常堆栈: ", e);
                            }
                        }
                    } catch (Exception e) {
                        AppLogger.error("创建文件输入流失败: " + e.getMessage(), e);
                    }
                }
            }
            
            // 如果所有方法都失败，抛出异常
            throw new java.io.IOException("无法加载FXML文件: " + fxmlPath + " (未知路径)");
        } catch (IOException e) {
            AppLogger.error("加载工具时发生IO异常: " + fxmlPath + " - " + e.getMessage(), e);
            AppLogger.error("异常堆栈: ", e);
            showError("无法加载工具", "加载工具失败: " + e.getMessage());
        } catch (Exception e) {
            AppLogger.error("加载工具时发生异常: " + e.getMessage(), e);
            AppLogger.error("异常类型: " + e.getClass().getName());
            AppLogger.error("异常堆栈: ", e);
            showError("无法加载工具", "加载工具时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 显示错误对话框
     * @param title 标题
     * @param message 消息
     */
    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 刷新项目管理小助手
     */
    public void refreshProjectCalendar() {
        if (projectCalendarController != null) {
            projectCalendarController.refreshAllViews();
        } else {
            AppLogger.warning("无法刷新项目管理小助手，未找到控制器");
        }
    }
    
    /**
     * 处理工具视图加载后的通用逻辑
     */
    private void handleToolViewAfterLoad(javafx.scene.Parent toolView, String title, String fxmlPath) {
        // 检查是否只是切换工具
        if (toolsContainer.isVisible()) {
            // 如果是切换到相同的工具，不做任何操作
            if (fxmlPath.equals(currentTool)) {
                return;
            }
            
            // 使用淡入淡出动画切换工具内容
            animateToolChange(toolView, title);
            currentTool = fxmlPath;
            
            // 根据不同工具调整大小
            if (fxmlPath.equals("/fxml/AmountConverterView.fxml")) {
                // 金额转换小工具 - 小尺寸
                toolsContainer.setPrefWidth(400);
                toolsContainer.setPrefHeight(350);
                toolsContainer.setMinWidth(400);
                toolsContainer.setMinHeight(350);
                // 限制最大尺寸，防止占满整个窗口
                toolsContainer.setMaxWidth(500);
                toolsContainer.setMaxHeight(450);
            } else if (fxmlPath.equals("/fxml/DocumentGeneratorView.fxml")) {
                // 文档生成小工具需要与日历相同大小
                toolsContainer.setPrefWidth(1000);
                toolsContainer.setPrefHeight(680);
                toolsContainer.setMinWidth(800);
                toolsContainer.setMinHeight(600);
                toolsContainer.setMaxWidth(Double.MAX_VALUE);
                toolsContainer.setMaxHeight(Double.MAX_VALUE);
            } else if (fxmlPath.equals("/fxml/TextCorrectorView.fxml")) {
                // 文本处理小工具 - 中等尺寸
                toolsContainer.setPrefWidth(600);
                toolsContainer.setPrefHeight(450);
                toolsContainer.setMinWidth(600);
                toolsContainer.setMinHeight(450);
                // 限制最大尺寸，防止占满整个窗口
                toolsContainer.setMaxWidth(700);
                toolsContainer.setMaxHeight(550);
            }
            
            // 确保小工具可调整大小
            setupResizableToolContainer();
            
            // 添加代码以在工具加载完成后根据实际内容调整尺寸
            javafx.application.Platform.runLater(() -> {
                if (toolView.prefWidth(-1) > 0 && toolView.prefHeight(-1) > 0) {
                    // 如果工具视图有指定的首选尺寸，使用它
                    double prefWidth = toolView.prefWidth(-1) + 20; // 加一些边距
                    double prefHeight = toolView.prefHeight(-1) + 60; // 加一些边距给标题栏和底部
                    
                    // 检查是否需要限制尺寸
                    if (fxmlPath.equals("/fxml/DocumentGeneratorView.fxml")) {
                        // 文档生成器允许占满窗口
                        toolsContainer.setPrefWidth(prefWidth);
                        toolsContainer.setPrefHeight(prefHeight);
                    } else {
                        // 其他工具限制大小
                        double maxWidth = fxmlPath.equals("/fxml/AmountConverterView.fxml") ? 500 : 700;
                        double maxHeight = fxmlPath.equals("/fxml/AmountConverterView.fxml") ? 450 : 550;
                        
                        toolsContainer.setPrefWidth(Math.min(prefWidth, maxWidth));
                        toolsContainer.setPrefHeight(Math.min(prefHeight, maxHeight));
                    }
                    
                    // 确保最小宽高不小于当前值
                    if (toolsContainer.getMinWidth() < 400) 
                        toolsContainer.setMinWidth(400);
                    if (toolsContainer.getMinHeight() < 300) 
                        toolsContainer.setMinHeight(300);
                } else {
                    // 根据内容计算大小
                    toolView.applyCss();
                    toolView.layout();
                    double width = toolView.getBoundsInLocal().getWidth() + 20;
                    double height = toolView.getBoundsInLocal().getHeight() + 60;
                    
                    // 检查是否需要限制尺寸
                    if (fxmlPath.equals("/fxml/DocumentGeneratorView.fxml")) {
                        // 文档生成器允许占满窗口
                        toolsContainer.setPrefWidth(width);
                        toolsContainer.setPrefHeight(height);
                    } else {
                        // 其他工具限制大小
                        double maxWidth = fxmlPath.equals("/fxml/AmountConverterView.fxml") ? 500 : 700;
                        double maxHeight = fxmlPath.equals("/fxml/AmountConverterView.fxml") ? 450 : 550;
                        
                        toolsContainer.setPrefWidth(Math.min(width, maxWidth));
                        toolsContainer.setPrefHeight(Math.min(height, maxHeight));
                    }
                }
                
                // 防止过小
                if (toolsContainer.getPrefWidth() < toolsContainer.getMinWidth()) 
                    toolsContainer.setPrefWidth(toolsContainer.getMinWidth());
                if (toolsContainer.getPrefHeight() < toolsContainer.getMinHeight()) 
                    toolsContainer.setPrefHeight(toolsContainer.getMinHeight());
                
                // 防止过大超出屏幕
                if (toolsContainer.getScene() != null) {
                    double sceneWidth = toolsContainer.getScene().getWidth();
                    double sceneHeight = toolsContainer.getScene().getHeight();
                    
                    if (toolsContainer.getPrefWidth() > sceneWidth * 0.9) {
                        toolsContainer.setPrefWidth(sceneWidth * 0.9);
                    }
                    
                    if (toolsContainer.getPrefHeight() > sceneHeight * 0.9) {
                        toolsContainer.setPrefHeight(sceneHeight * 0.9);
                    }
                }
            });
            
            return;
        }
        
        // 记录当前工具
        currentTool = fxmlPath;
        
        // 设置工具标题
        toolTitle.setText(title);
        
        // 将工具视图添加到内容区域
        toolContentArea.getChildren().clear();
        toolContentArea.getChildren().add(toolView);
        
        // 根据内容调整大小
        if (fxmlPath.equals("/fxml/AmountConverterView.fxml")) {
            // 金额转换小工具 - 小尺寸
            toolsContainer.setPrefWidth(400);
            toolsContainer.setPrefHeight(350);
            toolsContainer.setMinWidth(400);
            toolsContainer.setMinHeight(350);
            toolsContainer.setMaxWidth(500);
            toolsContainer.setMaxHeight(450);
        } else if (fxmlPath.equals("/fxml/DocumentGeneratorView.fxml")) {
            // 文档生成小工具需要与日历相同大小
            toolsContainer.setPrefWidth(1000);
            toolsContainer.setPrefHeight(680);
            toolsContainer.setMinWidth(800);
            toolsContainer.setMinHeight(600);
            toolsContainer.setMaxWidth(Double.MAX_VALUE);
            toolsContainer.setMaxHeight(Double.MAX_VALUE);
        } else if (fxmlPath.equals("/fxml/TextCorrectorView.fxml")) {
            // 文本处理小工具 - 中等尺寸
            toolsContainer.setPrefWidth(600);
            toolsContainer.setPrefHeight(450);
            toolsContainer.setMinWidth(600);
            toolsContainer.setMinHeight(450);
            toolsContainer.setMaxWidth(700);
            toolsContainer.setMaxHeight(550);
        } else {
            // 其他小工具使用自适应大小
            toolsContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
            toolsContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
            toolsContainer.setMinWidth(400);
            toolsContainer.setMinHeight(300);
            toolsContainer.setMaxWidth(Region.USE_COMPUTED_SIZE);
            toolsContainer.setMaxHeight(Region.USE_COMPUTED_SIZE);
        }
        
        // 确保小工具可调整大小
        setupResizableToolContainer();
        
        // 更新状态和按钮文本
        closeToolButton.setText("收起 ▲");
        isExpanded = true;
        
        // 在下一个UI刷新周期计算并应用实际尺寸
        javafx.application.Platform.runLater(() -> {
            // 计算工具视图的尺寸
            toolView.applyCss();
            toolView.layout();
            
            double width = toolView.prefWidth(-1);
            double height = toolView.prefHeight(-1);
            
            if (width <= 0 || height <= 0) {
                // 没有明确的首选尺寸，使用计算的边界尺寸
                width = toolView.getBoundsInLocal().getWidth() + 20; // 添加边距
                height = toolView.getBoundsInLocal().getHeight() + 60; // 添加边距给标题栏和底部
            } else {
                // 有明确的首选尺寸，添加一些边距
                width += 20;
                height += 60;
            }
            
            // 防止过小
            if (width < toolsContainer.getMinWidth()) width = toolsContainer.getMinWidth();
            if (height < toolsContainer.getMinHeight()) height = toolsContainer.getMinHeight();
            
            // 防止过大超出屏幕
            if (toolsContainer.getScene() != null) {
                double sceneWidth = toolsContainer.getScene().getWidth();
                double sceneHeight = toolsContainer.getScene().getHeight();
                
                if (width > sceneWidth * 0.9) {
                    width = sceneWidth * 0.9;
                }
                
                if (height > sceneHeight * 0.9) {
                    height = sceneHeight * 0.9;
                }
            }
            
            // 设置容器大小
            toolsContainer.setPrefWidth(width);
            toolsContainer.setPrefHeight(height);
            
            // 展示工具容器（带动画）
            animateOpen();
        });
    }
    
    /**
     * 设置工具容器可调整大小
     */
    private void setupResizableToolContainer() {
        final javafx.scene.Cursor defaultCursor = toolsContainer.getCursor();
        final double[] dragStartX = new double[1];
        final double[] dragStartY = new double[1];
        final double[] initWidth = new double[1];
        final double[] initHeight = new double[1];
        final boolean[] resizing = new boolean[1];
        final int RESIZE_BORDER = 8; // 边缘大小
        
        // 鼠标移动监听器
        toolsContainer.setOnMouseMoved(e -> {
            double x = e.getX();
            double y = e.getY();
            double width = toolsContainer.getWidth();
            double height = toolsContainer.getHeight();
            
            if (x >= width - RESIZE_BORDER && y >= height - RESIZE_BORDER) {
                // 右下角 - 对角线调整大小
                toolsContainer.setCursor(javafx.scene.Cursor.SE_RESIZE);
            } else if (x >= width - RESIZE_BORDER) {
                // 右边框 - 水平调整大小
                toolsContainer.setCursor(javafx.scene.Cursor.E_RESIZE);
            } else if (y >= height - RESIZE_BORDER) {
                // 下边框 - 垂直调整大小
                toolsContainer.setCursor(javafx.scene.Cursor.S_RESIZE);
            } else {
                // 恢复默认光标
                toolsContainer.setCursor(defaultCursor);
            }
        });
        
        // 鼠标按下监听器
        toolsContainer.setOnMousePressed(e -> {
            double x = e.getX();
            double y = e.getY();
            double width = toolsContainer.getWidth();
            double height = toolsContainer.getHeight();
            
            if (x >= width - RESIZE_BORDER || y >= height - RESIZE_BORDER) {
                dragStartX[0] = e.getScreenX();
                dragStartY[0] = e.getScreenY();
                initWidth[0] = toolsContainer.getWidth();
                initHeight[0] = toolsContainer.getHeight();
                resizing[0] = true;
                e.consume();
            }
        });
        
        // 鼠标拖动监听器
        toolsContainer.setOnMouseDragged(e -> {
            if (resizing[0]) {
                double xDiff = e.getScreenX() - dragStartX[0];
                double yDiff = e.getScreenY() - dragStartY[0];
                
                double newWidth = initWidth[0] + xDiff;
                double newHeight = initHeight[0] + yDiff;
                
                // 确保不小于最小尺寸
                newWidth = Math.max(newWidth, toolsContainer.getMinWidth());
                newHeight = Math.max(newHeight, toolsContainer.getMinHeight());
                
                // 考虑屏幕边界
                if (toolsContainer.getScene() != null) {
                    double sceneWidth = toolsContainer.getScene().getWidth();
                    double sceneHeight = toolsContainer.getScene().getHeight();
                    
                    // 限制最大尺寸，避免超出屏幕
                    newWidth = Math.min(newWidth, sceneWidth * 0.95);
                    newHeight = Math.min(newHeight, sceneHeight * 0.95);
                }
                
                // 应用新的尺寸
                if (toolsContainer.getCursor() == javafx.scene.Cursor.E_RESIZE || 
                    toolsContainer.getCursor() == javafx.scene.Cursor.SE_RESIZE) {
                    toolsContainer.setPrefWidth(newWidth);
                }
                
                if (toolsContainer.getCursor() == javafx.scene.Cursor.S_RESIZE || 
                    toolsContainer.getCursor() == javafx.scene.Cursor.SE_RESIZE) {
                    toolsContainer.setPrefHeight(newHeight);
                }
                
                e.consume();
            }
        });
        
        // 鼠标释放监听器
        toolsContainer.setOnMouseReleased(e -> {
            resizing[0] = false;
            e.consume();
        });
        
        // 鼠标退出监听器
        toolsContainer.setOnMouseExited(e -> {
            if (!resizing[0]) {
                toolsContainer.setCursor(defaultCursor);
            }
        });
    }
    
    /**
     * 显示信息对话框
     * @param title 标题
     * @param message 消息
     */
    private void showInfo(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 