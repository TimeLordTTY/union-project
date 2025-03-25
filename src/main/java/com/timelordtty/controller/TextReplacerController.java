package com.timelordtty.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * 文本替换控制器
 * 处理批量文本替换功能
 */
public class TextReplacerController implements Initializable {

    @FXML
    private TextArea inputTextArea;
    
    @FXML
    private TextArea outputTextArea;
    
    @FXML
    private TextArea resultTextArea;
    
    @FXML
    private Button addRuleButton;
    
    @FXML
    private Button replaceButton;
    
    @FXML
    private Button clearRulesButton;
    
    @FXML
    private VBox rulesContainer;
    
    @FXML
    private CheckBox useRegexCheckBox;
    
    @FXML
    private TableView<ReplacementRule> replacementRuleTableView;
    
    @FXML
    private TableColumn<ReplacementRule, String> fromColumn;
    
    @FXML
    private TableColumn<ReplacementRule, String> toColumn;
    
    // 存储所有替换规则
    private List<ReplacementRule> replacementRules = new ArrayList<>();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("文本替换器界面已初始化");
        
        // 初始化TableView列
        if (fromColumn != null && toColumn != null) {
            fromColumn.setCellValueFactory(cellData -> cellData.getValue().findTextProperty());
            toColumn.setCellValueFactory(cellData -> cellData.getValue().replaceTextProperty());
        }
        
        // 初始化按钮事件 - 添加null检查
        if (addRuleButton != null) {
            addRuleButton.setOnAction(event -> addNewRule());
        }
        if (replaceButton != null) {
            replaceButton.setOnAction(event -> performReplace());
        }
        if (clearRulesButton != null) {
            clearRulesButton.setOnAction(event -> clearRules());
        }
        
        // 添加一个默认规则
        addNewRule();
    }
    
    /**
     * 添加替换规则（与FXML中的onAction="addReplacementRule"对应）
     */
    @FXML
    private void addReplacementRule() {
        addNewRule();
    }
    
    /**
     * 添加新的替换规则
     */
    @FXML
    private void addNewRule() {
        // 如果控件未初始化，则直接返回
        if (rulesContainer == null) {
            return;
        }
        
        ReplacementRule rule = new ReplacementRule();
        replacementRules.add(rule);
        
        // 更新TableView
        if (replacementRuleTableView != null) {
            replacementRuleTableView.getItems().add(rule);
        }
        
        // 创建规则UI组件
        HBox ruleBox = createRuleBox(rule);
        rulesContainer.getChildren().add(ruleBox);
    }
    
    /**
     * 移除选定的替换规则
     */
    @FXML
    private void removeReplacementRule() {
        if (replacementRules.size() <= 1) {
            showAlert(Alert.AlertType.WARNING, "警告", "至少需要保留一条替换规则!");
            return;
        }
        
        // 移除最后一条规则
        int lastIndex = replacementRules.size() - 1;
        replacementRules.remove(lastIndex);
        rulesContainer.getChildren().remove(lastIndex);
        
        // 更新TableView
        if (replacementRuleTableView != null) {
            replacementRuleTableView.getItems().remove(lastIndex);
        }
    }
    
    /**
     * 清空输入和输出文本
     */
    @FXML
    private void clearText() {
        if (inputTextArea != null) {
            inputTextArea.clear();
        }
        if (outputTextArea != null) {
            outputTextArea.clear();
        }
        if (resultTextArea != null) {
            resultTextArea.clear();
        }
    }
    
    /**
     * 复制替换结果
     */
    @FXML
    private void copyResult() {
        String content = "";
        
        // 首先尝试从resultTextArea获取内容
        if (resultTextArea != null && !resultTextArea.getText().isEmpty()) {
            content = resultTextArea.getText();
        } 
        // 如果resultTextArea为空，尝试从outputTextArea获取
        else if (outputTextArea != null && !outputTextArea.getText().isEmpty()) {
            content = outputTextArea.getText();
        }
        
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
     * 为替换规则创建UI组件
     */
    private HBox createRuleBox(ReplacementRule rule) {
        HBox hbox = new HBox(5);
        hbox.setStyle("-fx-padding: 5; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        
        TextField findField = new TextField();
        findField.setPromptText("查找内容");
        findField.setPrefWidth(200);
        findField.textProperty().bindBidirectional(rule.findTextProperty());
        
        TextField replaceField = new TextField();
        replaceField.setPromptText("替换为");
        replaceField.setPrefWidth(200);
        replaceField.textProperty().bindBidirectional(rule.replaceTextProperty());
        
        Button removeButton = new Button("删除");
        removeButton.setOnAction(event -> {
            replacementRules.remove(rule);
            rulesContainer.getChildren().remove(hbox);
        });
        
        hbox.getChildren().addAll(new Label("查找:"), findField, new Label("替换为:"), replaceField, removeButton);
        return hbox;
    }
    
    /**
     * 执行替换（与FXML中的onAction="replaceText"对应）
     */
    @FXML
    private void replaceText() {
        performReplace();
    }
    
    /**
     * 执行批量替换
     */
    @FXML
    private void performReplace() {
        String inputText = inputTextArea.getText();
        if (inputText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "错误", "请输入需要替换的文本!");
            return;
        }
        
        if (replacementRules.isEmpty() || allRulesEmpty()) {
            showAlert(Alert.AlertType.ERROR, "错误", "请添加至少一条有效的替换规则!");
            return;
        }
        
        String result = inputText;
        boolean isRegex = useRegexCheckBox.isSelected();
        int replacementCount = 0;
        
        try {
            for (ReplacementRule rule : replacementRules) {
                String findText = rule.getFindText();
                String replaceText = rule.getReplaceText();
                
                if (findText.isEmpty()) {
                    continue;
                }
                
                if (isRegex) {
                    // 使用正则表达式替换
                    Pattern pattern = Pattern.compile(findText);
                    Matcher matcher = pattern.matcher(result);
                    int count = 0;
                    while (matcher.find()) {
                        count++;
                    }
                    replacementCount += count;
                    result = matcher.replaceAll(replaceText);
                } else {
                    // 普通文本替换
                    int index = 0;
                    int count = 0;
                    while ((index = result.indexOf(findText, index)) != -1) {
                        count++;
                        index += findText.length();
                    }
                    replacementCount += count;
                    result = result.replace(findText, replaceText);
                }
            }
            
            // 使用resultTextArea替代outputTextArea，并确保非空检查
            if (outputTextArea != null) {
                outputTextArea.setText(result);
            }
            if (resultTextArea != null) {
                resultTextArea.setText(result);
            }
            
            showAlert(Alert.AlertType.INFORMATION, "完成", "替换完成! 共执行了 " + replacementCount + " 处替换。");
        } catch (PatternSyntaxException e) {
            showAlert(Alert.AlertType.ERROR, "错误", "正则表达式语法错误: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "错误", "替换过程发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否所有规则都为空
     */
    private boolean allRulesEmpty() {
        for (ReplacementRule rule : replacementRules) {
            if (!rule.getFindText().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 清除所有替换规则（与FXML中的onAction="clearReplacementRules"对应）
     */
    @FXML
    private void clearReplacementRules() {
        clearRules();
    }
    
    /**
     * 清除所有替换规则
     */
    @FXML
    private void clearRules() {
        replacementRules.clear();
        rulesContainer.getChildren().clear();
        
        // 清空TableView
        if (replacementRuleTableView != null) {
            replacementRuleTableView.getItems().clear();
        }
        
        addNewRule(); // 添加一个默认规则
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
    
    /**
     * 替换规则内部类
     */
    public static class ReplacementRule {
        private StringProperty findText = new SimpleStringProperty("");
        private StringProperty replaceText = new SimpleStringProperty("");
        
        public String getFindText() {
            return findText.get();
        }
        
        public StringProperty findTextProperty() {
            return findText;
        }
        
        public void setFindText(String findText) {
            this.findText.set(findText);
        }
        
        public String getReplaceText() {
            return replaceText.get();
        }
        
        public StringProperty replaceTextProperty() {
            return replaceText;
        }
        
        public void setReplaceText(String replaceText) {
            this.replaceText.set(replaceText);
        }
    }
} 