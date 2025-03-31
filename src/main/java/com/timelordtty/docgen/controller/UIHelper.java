package com.timelordtty.docgen.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * UI辅助工具类，提供通用UI操作功能
 */
public class UIHelper {
    
    /**
     * 显示错误对话框
     * 
     * @param title 标题
     * @param message 错误信息
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示信息对话框
     * 
     * @param title 标题
     * @param message 信息内容
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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