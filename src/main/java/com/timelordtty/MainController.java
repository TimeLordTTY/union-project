package com.timelordtty;

import java.io.IOException;

import com.timelordtty.projectCalendar.ProjectCalendarController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
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
            
            AppLogger.info("MainController初始化完成");
        } catch (Exception e) {
            AppLogger.error("初始化MainController时发生异常: " + e.getMessage(), e);
        }
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