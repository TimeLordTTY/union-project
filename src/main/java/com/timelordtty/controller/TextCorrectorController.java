package com.timelordtty.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import com.timelordtty.model.TextCorrection;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * 文本校正控制器
 * 使用百度API进行文本校正功能
 */
public class TextCorrectorController implements Initializable {

    @FXML
    private TextArea inputTextArea;
    
    @FXML
    private TextFlow correctedTextFlow;
    
    @FXML
    private TextArea correctionDetailsTextArea;
    
    @FXML
    private Button correctButton;
    
    @FXML
    private ProgressIndicator progressIndicator;
    
    @FXML
    private TextArea resultTextArea;
    
    @FXML
    private TableView<TextCorrection> correctionTableView;
    
    @FXML
    private TableColumn<TextCorrection, String> originalColumn;
    
    @FXML
    private TableColumn<TextCorrection, String> correctedColumn;
    
    @FXML
    private TableColumn<TextCorrection, String> positionColumn;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("文本校正器界面已初始化");
        
        // 添加null检查以避免NullPointerException
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
        
        // 初始化TableView列 - 添加null检查
        if (originalColumn != null && correctedColumn != null && positionColumn != null) {
            originalColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOriginal()));
            correctedColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCorrected()));
            positionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPosition()));
        }
    }
    
    /**
     * 执行文本校正
     */
    @FXML
    private void correctText() {
        if (inputTextArea == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "文本输入区域未初始化!");
            return;
        }
        
        String text = inputTextArea.getText();
        if (text.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "错误", "请输入需要校正的文本!");
            return;
        }
        
        // 清空之前的结果
        if (correctedTextFlow != null) {
            correctedTextFlow.getChildren().clear();
        }
        if (correctionDetailsTextArea != null) {
            correctionDetailsTextArea.clear();
        }
        if (resultTextArea != null) {
            resultTextArea.clear();
        }
        
        // 显示进度指示器
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }
        
        // 异步执行校正，避免UI冻结
        CompletableFuture.runAsync(() -> {
            try {
                // 此处应调用实际的API服务进行文本校正
                // TextCorrectionResult result = TextCorrectionService.correct(text);
                
                // 模拟API调用延迟
                Thread.sleep(1500);
                
                // 模拟校正结果
                String correctedText = simulateTextCorrection(text);
                String[] corrections = {
                    "将'收到'修改为'收到'",
                    "将'解决问题'修改为'解决问题'",
                    "将'有意义'修改为'有意义'"
                };
                
                // 创建模拟的TextCorrection对象列表
                java.util.List<TextCorrection> correctionList = new java.util.ArrayList<>();
                correctionList.add(new TextCorrection("收到", "收到", "位置: 0-2"));
                correctionList.add(new TextCorrection("解决问题", "解决问题", "位置: 3-7"));
                correctionList.add(new TextCorrection("有意义", "有意义", "位置: 8-11"));
                
                // 在UI线程中更新界面
                Platform.runLater(() -> {
                    // 更新校正后文本
                    displayCorrectedText(text, correctedText);
                    
                    // 更新结果文本区域
                    if (resultTextArea != null) {
                        resultTextArea.setText(correctedText);
                    }
                    
                    // 更新TableView
                    if (correctionTableView != null) {
                        correctionTableView.getItems().clear();
                        correctionTableView.getItems().addAll(correctionList);
                    }
                    
                    // 隐藏进度指示器
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    
                    showAlert(Alert.AlertType.INFORMATION, "完成", "文本校正已完成!");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "错误", "文本校正失败: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * 清空文本
     */
    @FXML
    private void clearText() {
        if (inputTextArea != null) {
            inputTextArea.clear();
        }
        if (resultTextArea != null) {
            resultTextArea.clear();
        }
        if (correctedTextFlow != null) {
            correctedTextFlow.getChildren().clear();
        }
        if (correctionDetailsTextArea != null) {
            correctionDetailsTextArea.clear();
        }
    }
    
    /**
     * 复制校正结果
     */
    @FXML
    private void copyResult() {
        String content = "";
        
        // 尝试从resultTextArea获取内容
        if (resultTextArea != null && !resultTextArea.getText().isEmpty()) {
            content = resultTextArea.getText();
        } 
        // 如果没有内容，从correctedTextFlow获取
        else if (correctedTextFlow != null && !correctedTextFlow.getChildren().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (javafx.scene.Node node : correctedTextFlow.getChildren()) {
                if (node instanceof Text) {
                    sb.append(((Text) node).getText());
                }
            }
            content = sb.toString();
        }
        
        // 复制到剪贴板
        if (!content.isEmpty()) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(content);
            clipboard.setContent(clipboardContent);
            
            showAlert(Alert.AlertType.INFORMATION, "成功", "已复制到剪贴板!");
        } else {
            showAlert(Alert.AlertType.WARNING, "警告", "没有可复制的内容!");
        }
    }
    
    /**
     * 模拟文本校正过程
     * 实际应用中应替换为真正的API调用
     */
    private String simulateTextCorrection(String text) {
        // 这里只是简单模拟一些常见错误的校正
        return text.replace("收到", "收到")
                  .replace("解决问题", "解决问题")
                  .replace("有意义", "有意义");
    }
    
    /**
     * 在TextFlow中显示校正后的文本，并高亮显示修改的部分
     */
    private void displayCorrectedText(String original, String corrected) {
        // 如果correctedTextFlow为null，直接返回
        if (correctedTextFlow == null) {
            return;
        }
        
        // 实际应用中应该进行差异比较，这里仅作示例
        Text text = new Text(corrected);
        text.setFill(Color.BLACK);
        
        // 假设有修改的部分，可以用不同颜色标记
        // 这里简化处理，实际应用中需要更复杂的差异算法
        if (!original.equals(corrected)) {
            int diffIndex = findFirstDifferenceIndex(original, corrected);
            if (diffIndex >= 0 && diffIndex < corrected.length()) {
                Text beforeDiff = new Text(corrected.substring(0, diffIndex));
                Text diffPart = new Text(corrected.substring(diffIndex, diffIndex + 1));
                diffPart.setFill(Color.RED);
                Text afterDiff = new Text(corrected.substring(diffIndex + 1));
                
                correctedTextFlow.getChildren().addAll(beforeDiff, diffPart, afterDiff);
                return;
            }
        }
        
        // 如果没有差异或处理差异失败，显示完整文本
        correctedTextFlow.getChildren().add(text);
    }
    
    /**
     * 查找两个字符串的第一个不同字符的索引
     */
    private int findFirstDifferenceIndex(String s1, String s2) {
        int minLength = Math.min(s1.length(), s2.length());
        for (int i = 0; i < minLength; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return i;
            }
        }
        
        if (s1.length() != s2.length()) {
            return minLength;
        }
        
        return -1; // 两个字符串相同
    }
    
    /**
     * 显示警告对话框
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 