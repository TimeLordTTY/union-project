package com.timelordtty;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * 主界面控制器
 */
public class MainController {
    
    @FXML private VBox toolsContainer;
    @FXML private StackPane toolContentArea;
    @FXML private Label toolTitle;
    @FXML private Button closeToolButton;
    @FXML private Button amountToolButton;
    @FXML private Button docGenToolButton;
    @FXML private Button textCorrectionToolButton;
    
    // 跟踪当前工具状态
    private String currentTool = null;
    private Parent amountToolContent = null;
    private Parent docGenToolContent = null;
    private Parent textCorrectionToolContent = null;
    
    @FXML
    private void initialize() {
        // 初始化主界面
        AppLogger.info("初始化主界面控制器");
        
        // 工具栏按钮样式设置
        setupToolbarButtonStyle();
    }
    
    /**
     * 设置工具栏按钮样式
     */
    private void setupToolbarButtonStyle() {
        // 设置工具按钮的鼠标悬停效果
        String defaultStyle = "-fx-background-color: #e3f2fd; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #bbdefb; -fx-cursor: hand;";
        
        setupButtonHoverEffect(amountToolButton, defaultStyle, hoverStyle);
        setupButtonHoverEffect(docGenToolButton, defaultStyle, hoverStyle);
        setupButtonHoverEffect(textCorrectionToolButton, defaultStyle, hoverStyle);
    }
    
    /**
     * 设置按钮悬停效果
     * @param button 按钮
     * @param defaultStyle 默认样式
     * @param hoverStyle 悬停样式
     */
    private void setupButtonHoverEffect(Button button, String defaultStyle, String hoverStyle) {
        button.setStyle(defaultStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(defaultStyle));
    }
    
    /**
     * 处理金额转换工具点击
     */
    @FXML
    private void handleAmountToolClick() {
        if ("amount".equals(currentTool) && toolsContainer.isVisible()) {
            // 如果当前已经显示该工具，则隐藏
            toolsContainer.setVisible(false);
            currentTool = null;
        } else {
            // 显示金额转换工具
            showTool("amount", "金额转换", "/fxml/AmountConverterView.fxml");
        }
    }
    
    /**
     * 处理文档生成工具点击
     */
    @FXML
    private void handleDocGenToolClick() {
        if ("docgen".equals(currentTool) && toolsContainer.isVisible()) {
            // 如果当前已经显示该工具，则隐藏
            toolsContainer.setVisible(false);
            currentTool = null;
        } else {
            // 显示文档生成工具
            showTool("docgen", "文档生成", "/fxml/DocumentGeneratorView.fxml");
        }
    }
    
    /**
     * 处理文本纠错工具点击
     */
    @FXML
    private void handleTextCorrectionToolClick() {
        if ("textcorrection".equals(currentTool) && toolsContainer.isVisible()) {
            // 如果当前已经显示该工具，则隐藏
            toolsContainer.setVisible(false);
            currentTool = null;
        } else {
            // 显示文本纠错工具
            showTool("textcorrection", "文本纠错", "/fxml/TextCorrectorView.fxml");
        }
    }
    
    /**
     * 处理关闭工具点击
     */
    @FXML
    private void handleCloseToolClick() {
        // 关闭小工具面板
        toolsContainer.setVisible(false);
        closeToolButton.setText("收起 ▲");
    }
    
    /**
     * 显示工具
     * @param toolId 工具ID
     * @param title 工具标题
     * @param fxmlPath FXML路径
     */
    private void showTool(String toolId, String title, String fxmlPath) {
        try {
            // 更新工具标题
            toolTitle.setText(title);
            
            // 清空当前内容
            toolContentArea.getChildren().clear();
            
            // 加载工具内容（如果尚未加载）
            Parent toolContent = null;
            
            switch (toolId) {
                case "amount":
                    if (amountToolContent == null) {
                        amountToolContent = loadToolContent(fxmlPath);
                    }
                    toolContent = amountToolContent;
                    break;
                case "docgen":
                    if (docGenToolContent == null) {
                        docGenToolContent = loadToolContent(fxmlPath);
                    }
                    toolContent = docGenToolContent;
                    break;
                case "textcorrection":
                    if (textCorrectionToolContent == null) {
                        textCorrectionToolContent = loadToolContent(fxmlPath);
                    }
                    toolContent = textCorrectionToolContent;
                    break;
            }
            
            // 添加工具内容到容器
            if (toolContent != null) {
                toolContentArea.getChildren().add(toolContent);
            }
            
            // 显示工具容器
            toolsContainer.setVisible(true);
            
            // 更新当前工具ID
            currentTool = toolId;
            
        } catch (Exception e) {
            AppLogger.error("加载工具失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 加载工具内容
     * @param fxmlPath FXML路径
     * @return 工具内容
     * @throws IOException 如果加载失败
     */
    private Parent loadToolContent(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        return loader.load();
    }
} 