package com.timelordtty.amountConvert.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.timelordtty.amountConvert.util.AmountConverter;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * 金额转换功能控制器
 */
public class AmountConverterController implements Initializable {

    @FXML
    private TextField numericAmountField;
    
    @FXML
    private TextField chineseAmountField;
    
    @FXML
    private TextField chineseInputField;
    
    @FXML
    private TextField numericResultField;
    
    private final Clipboard clipboard = Clipboard.getSystemClipboard();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化控件
    }
    
    /**
     * 将数字金额转换为中文大写
     */
    @FXML
    private void convertToChineseAmount() {
        try {
            String numericAmount = numericAmountField.getText().trim();
            if (numericAmount.isEmpty()) {
                showAlert("请输入数字金额");
                return;
            }
            
            java.math.BigDecimal amount = new java.math.BigDecimal(numericAmount);
            String chineseAmount = AmountConverter.convertToChineseAmount(amount);
            chineseAmountField.setText(chineseAmount);
        } catch (NumberFormatException e) {
            showAlert("请输入有效的数字金额");
        } catch (Exception e) {
            showAlert("转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 将中文大写金额转换为数字
     */
    @FXML
    private void convertToNumericAmount() {
        try {
            String chineseAmount = chineseInputField.getText().trim();
            if (chineseAmount.isEmpty()) {
                showAlert("请输入中文金额");
                return;
            }
            
            java.math.BigDecimal numericAmount = AmountConverter.convertToNumber(chineseAmount);
            numericResultField.setText(numericAmount.toPlainString());
        } catch (Exception e) {
            showAlert("转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 复制中文金额到剪贴板
     */
    @FXML
    private void copyChineseAmount() {
        String content = chineseAmountField.getText();
        if (content != null && !content.isEmpty()) {
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(content);
            clipboard.setContent(clipboardContent);
            showAlert("已复制到剪贴板", Alert.AlertType.INFORMATION);
        }
    }
    
    /**
     * 复制数字金额到剪贴板
     */
    @FXML
    private void copyNumericAmount() {
        String content = numericResultField.getText();
        if (content != null && !content.isEmpty()) {
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(content);
            clipboard.setContent(clipboardContent);
            showAlert("已复制到剪贴板", Alert.AlertType.INFORMATION);
        }
    }
    
    /**
     * 显示警告对话框
     */
    private void showAlert(String message) {
        showAlert(message, Alert.AlertType.WARNING);
    }
    
    /**
     * 显示对话框
     */
    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(alertType == Alert.AlertType.WARNING ? "警告" : "信息");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 