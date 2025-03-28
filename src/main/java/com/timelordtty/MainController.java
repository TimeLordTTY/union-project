package com.timelordtty;

import java.io.IOException;

import com.timelordtty.projectCalendar.ProjectCalendarController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * 应用程序主控制器
 */
public class MainController {
    
    @FXML private BorderPane root;
    @FXML private VBox toolsContainer;
    @FXML private StackPane toolContentArea;
    @FXML private Label toolTitle;
    
    // 主题按钮
    @FXML private Button whiteThemeButton;
    @FXML private Button pinkThemeButton;
    @FXML private Button yellowThemeButton;
    @FXML private Button blueThemeButton;
    @FXML private Button cyanThemeButton;
    @FXML private HBox themeButtonContainer;
    
    @FXML private ProjectCalendarController projectCalendarController;
    
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
            }
            
            // 设置白色主题为默认
            applyTheme("white");
            
            AppLogger.info("MainController初始化完成");
        } catch (Exception e) {
            AppLogger.error("初始化MainController时发生异常: " + e.getMessage(), e);
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
        
        // 更新CSS变量
        String rootStyle = "";
        Button activeButton = null;
        
        switch (themeName) {
            case "white":
                rootStyle = "-theme-primary: -white-primary; " +
                           "-theme-light: -white-light; " +
                           "-theme-dark: -white-dark; " +
                           "-theme-accent: -white-accent; " +
                           "-theme-text: -white-text; " +
                           "-theme-border: -white-border; " +
                           "-theme-shadow: -white-shadow; " +
                           "-theme-background: -white-background;";
                activeButton = whiteThemeButton;
                break;
            case "pink":
                rootStyle = "-theme-primary: -pink-primary; " +
                           "-theme-light: -pink-light; " +
                           "-theme-dark: -pink-dark; " +
                           "-theme-accent: -pink-accent; " +
                           "-theme-text: -pink-text; " +
                           "-theme-border: -pink-border; " +
                           "-theme-shadow: -pink-shadow; " +
                           "-theme-background: -pink-background;";
                activeButton = pinkThemeButton;
                break;
            case "yellow":
                rootStyle = "-theme-primary: -yellow-primary; " +
                           "-theme-light: -yellow-light; " +
                           "-theme-dark: -yellow-dark; " +
                           "-theme-accent: -yellow-accent; " +
                           "-theme-text: -yellow-text; " +
                           "-theme-border: -yellow-border; " +
                           "-theme-shadow: -yellow-shadow; " +
                           "-theme-background: -yellow-background;";
                activeButton = yellowThemeButton;
                break;
            case "blue":
                rootStyle = "-theme-primary: -blue-primary; " +
                           "-theme-light: -blue-light; " +
                           "-theme-dark: -blue-dark; " +
                           "-theme-accent: -blue-accent; " +
                           "-theme-text: -blue-text; " +
                           "-theme-border: -blue-border; " +
                           "-theme-shadow: -blue-shadow; " +
                           "-theme-background: -blue-background;";
                activeButton = blueThemeButton;
                break;
            case "cyan":
                rootStyle = "-theme-primary: -cyan-primary; " +
                           "-theme-light: -cyan-light; " +
                           "-theme-dark: -cyan-dark; " +
                           "-theme-accent: -cyan-accent; " +
                           "-theme-text: -cyan-text; " +
                           "-theme-border: -cyan-border; " +
                           "-theme-shadow: -cyan-shadow; " +
                           "-theme-background: -cyan-background;";
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
                
                // 先应用到样式到根节点
                root.setStyle(rootStyle);
                
                try {
                    // 重新加载样式表
                    String cssPath = getClass().getResource("/css/styles.css").toExternalForm();
                    scene.getStylesheets().clear();
                    scene.getStylesheets().add(cssPath);
                    
                    // 尝试强制UI更新
                    root.applyCss();
                    root.layout();
                    
                    // 对每个直接子节点也应用相同的样式
                    applyStyleToChildren(root, rootStyle);
                    
                    AppLogger.info("成功应用样式表: " + cssPath);
                } catch (Exception e) {
                    AppLogger.error("应用样式表时出错: " + e.getMessage(), e);
                }
            } else {
                // 否则先应用到根节点
                root.setStyle(rootStyle);
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
     * 递归地将样式应用到所有子节点
     * @param parent 父节点
     * @param style 要应用的样式
     */
    private void applyStyleToChildren(javafx.scene.Parent parent, String style) {
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) node).setStyle(style);
                node.applyCss();
            }
            
            if (node instanceof javafx.scene.Parent) {
                applyStyleToChildren((javafx.scene.Parent) node, style);
            }
        }
    }
    
    /**
     * 处理白色主题按钮点击
     */
    @FXML
    private void handleWhiteThemeClick() {
        applyTheme("white");
    }
    
    /**
     * 处理粉色主题按钮点击
     */
    @FXML
    private void handlePinkThemeClick() {
        applyTheme("pink");
    }
    
    /**
     * 处理黄色主题按钮点击
     */
    @FXML
    private void handleYellowThemeClick() {
        applyTheme("yellow");
    }
    
    /**
     * 处理蓝色主题按钮点击
     */
    @FXML
    private void handleBlueThemeClick() {
        applyTheme("blue");
    }
    
    /**
     * 处理青色主题按钮点击
     */
    @FXML
    private void handleCyanThemeClick() {
        applyTheme("cyan");
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
        try {
            AppLogger.info("打开文档生成工具");
            loadTool("文档生成", "/fxml/DocumentGeneratorView.fxml");
        } catch (Exception e) {
            AppLogger.error("打开文档生成工具失败: " + e.getMessage(), e);
        }
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
     * 处理关闭工具按钮点击
     */
    @FXML
    private void handleCloseToolClick() {
        // 隐藏工具容器
        toolsContainer.setVisible(false);
    }
    
    /**
     * 加载工具
     * @param title 工具标题
     * @param fxmlPath 工具FXML路径
     */
    private void loadTool(String title, String fxmlPath) {
        try {
            // 首先清空工具内容区域
            toolContentArea.getChildren().clear();
            
            // 加载工具FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent toolView = loader.load();
            
            // 设置工具标题
            toolTitle.setText(title);
            
            // 将工具视图添加到内容区域
            toolContentArea.getChildren().add(toolView);
            
            // 显示工具容器
            toolsContainer.setVisible(true);
        } catch (IOException e) {
            AppLogger.error("加载工具时发生IO异常: " + e.getMessage(), e);
            showError("无法加载工具", "加载工具时发生IO异常: " + e.getMessage());
        } catch (Exception e) {
            AppLogger.error("加载工具时发生未知异常: " + e.getMessage(), e);
            showError("无法加载工具", "加载工具时发生未知异常: " + e.getMessage());
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
} 