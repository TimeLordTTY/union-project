package com.timelordtty.docgen;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * UI帮助工具类
 */
public class UIHelper {
    
    /**
     * 显示信息对话框
     * 
     * @param title 标题
     * @param message 消息
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示错误对话框
     * 
     * @param title 标题
     * @param message 消息
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示警告对话框
     * 
     * @param title 标题
     * @param message 消息
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 