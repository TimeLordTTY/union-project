package com.timelordtty.docgen.controller;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * UI辅助类，提供通用UI操作
 */
public class UIHelper {
    
    /**
     * 显示信息对话框
     * 
     * @param title 标题
     * @param message 消息内容
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // 确保对话框是模态的，并显示在当前窗口之上
        Window activeWindow = getActiveWindow();
        if (activeWindow != null) {
            alert.initOwner(activeWindow);
        }
        alert.initModality(Modality.APPLICATION_MODAL);
        
        alert.showAndWait();
    }
    
    /**
     * 显示错误对话框
     * 
     * @param title 标题
     * @param message 错误消息
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // 确保对话框是模态的，并显示在当前窗口之上
        Window activeWindow = getActiveWindow();
        if (activeWindow != null) {
            alert.initOwner(activeWindow);
        }
        alert.initModality(Modality.APPLICATION_MODAL);
        
        alert.showAndWait();
    }
    
    /**
     * 显示确认对话框
     * 
     * @param title 标题
     * @param message 确认消息
     * @return 是否确认
     */
    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // 确保对话框是模态的，并显示在当前窗口之上
        Window activeWindow = getActiveWindow();
        if (activeWindow != null) {
            alert.initOwner(activeWindow);
        }
        alert.initModality(Modality.APPLICATION_MODAL);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * 获取当前活动的窗口
     * 
     * @return 当前活动窗口
     */
    private static Window getActiveWindow() {
        try {
            return javafx.stage.Stage.getWindows().stream()
                    .filter(Window::isFocused)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 调整表格列宽以适应内容
     * 
     * @param tableView 表格视图
     */
    public static void autoResizeColumns(javafx.scene.control.TableView<?> tableView) {
        for (javafx.scene.control.TableColumn<?, ?> column : tableView.getColumns()) {
            column.setPrefWidth(column.getText().length() * 10 + 20); // 简单估算宽度
        }
    }
} 