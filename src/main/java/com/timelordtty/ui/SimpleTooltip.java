package com.timelordtty.ui;

import com.timelordtty.AppLogger;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 简单的气泡提示框组件
 */
public class SimpleTooltip extends StackPane {
    
    private final Label messageLabel;
    private Popup popup;
    
    /**
     * 创建简单气泡组件
     * @param message 显示的消息文本
     */
    public SimpleTooltip(String message) {
        // 设置样式类
        this.getStyleClass().add("simple-tooltip");
        
        // 创建消息标签
        messageLabel = new Label(message);
        messageLabel.getStyleClass().add("tooltip-text");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        
        // 添加到布局中
        this.getChildren().add(messageLabel);
        this.setPadding(new Insets(10));
        this.setAlignment(Pos.CENTER);
        
        // 设置初始不可见（用于动画）
        this.setOpacity(0);
        
        // 初始设置比例缩小
        this.setScaleX(0.8);
        this.setScaleY(0.8);
        
        // 设置容器大小
        this.setPrefWidth(280);
        this.setPrefHeight(80);
        this.setMaxWidth(280);
        this.setMaxHeight(80);
    }
    
    /**
     * 在舞台上以弹出窗口方式显示气泡
     * @param stage 主舞台
     */
    public void showInStage(Stage stage) {
        try {
            // 如果已有弹出窗口，先关闭
            if (popup != null) {
                popup.hide();
            }
            
            // 创建新的弹出窗口
            popup = new Popup();
            popup.setAutoHide(true);  // 点击其他地方自动隐藏
            popup.setHideOnEscape(true);  // 按ESC键隐藏
            popup.getContent().add(this);
            
            // 确保舞台可见
            if (!stage.isShowing()) {
                AppLogger.error("舞台不可见，无法显示气泡");
                return;
            }
            
            // 计算位置，使其在舞台中央显示
            double centerX = stage.getX() + stage.getWidth()/2 - this.getPrefWidth()/2;
            double centerY = stage.getY() + stage.getHeight()/2 - this.getPrefHeight()/2;
            
            // 记录关键信息到日志，帮助调试
            AppLogger.info("舞台位置: X=" + stage.getX() + ", Y=" + stage.getY() + 
                         ", 宽=" + stage.getWidth() + ", 高=" + stage.getHeight());
            AppLogger.info("气泡位置: X=" + centerX + ", Y=" + centerY);
            
            // 设置弹出窗口位置
            popup.setX(centerX);
            popup.setY(centerY);
            
            // 显示弹出窗口
            popup.show(stage);
            AppLogger.info("气泡已显示");
            
            // 开始动画
            playShowAnimation();
        } catch (Exception e) {
            AppLogger.error("显示气泡时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 播放显示动画
     */
    private void playShowAnimation() {
        // 创建淡入动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        // 创建缩放动画
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), this);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        
        // 并行播放淡入和缩放动画
        ParallelTransition showAnimation = new ParallelTransition(fadeIn, scaleIn);
        
        // 暂停3秒，给用户足够时间阅读
        PauseTransition pause = new PauseTransition(Duration.millis(3000));
        
        // 创建淡出动画
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            // 关闭弹出窗口
            if (popup != null) {
                popup.hide();
                AppLogger.info("气泡已隐藏");
            }
        });
        
        // 按顺序播放：显示动画 -> 暂停 -> 淡出
        SequentialTransition sequence = new SequentialTransition(showAnimation, pause, fadeOut);
        sequence.play();
    }
    
    /**
     * 应用主题到气泡
     * @param themeName 主题名称
     */
    public void applyTheme(String themeName) {
        // 移除所有主题样式类
        this.getStyleClass().removeAll(
            "white-theme", "pink-theme", "yellow-theme", "blue-theme", "cyan-theme");
        
        // 添加新的主题样式类
        this.getStyleClass().add(themeName + "-theme");
        
        // 应用CSS
        this.applyCss();
    }
}