package com.timelordtty.corrector.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.timelordtty.AppLogger;
import com.timelordtty.corrector.model.ReplaceRule;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * 替换规则对话框控制器
 */
public class ReplaceRuleDialogController implements Initializable {
    
    @FXML
    private TableView<ReplaceRule> ruleTableView;
    
    @FXML
    private TableColumn<ReplaceRule, String> originalColumn;
    
    @FXML
    private TableColumn<ReplaceRule, String> replacementColumn;
    
    @FXML
    private TextField originalTextField;
    
    @FXML
    private TextField replacementTextField;
    
    private ObservableList<ReplaceRule> ruleList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化表格列
        originalColumn.setCellValueFactory(cellData -> cellData.getValue().originalTextProperty());
        replacementColumn.setCellValueFactory(cellData -> cellData.getValue().replacementTextProperty());
        
        // 设置表格选择模式为单选
        ruleTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // 设置表格数据
        ruleTableView.setItems(ruleList);
        
        // 设置表格选择事件
        ruleTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                originalTextField.setText(newSelection.getOriginalText());
                replacementTextField.setText(newSelection.getReplacementText());
            }
        });
        
        AppLogger.info("替换规则对话框已初始化");
    }
    
    /**
     * 添加规则
     */
    @FXML
    private void addRule() {
        String originalText = originalTextField.getText();
        String replacementText = replacementTextField.getText();
        
        if (originalText == null || originalText.trim().isEmpty()) {
            showAlert(AlertType.WARNING, "警告", "请输入要查找的文本");
            return;
        }
        
        if (replacementText == null) {
            replacementText = "";
        }
        
        // 添加规则
        ruleList.add(new ReplaceRule(originalText, replacementText));
        
        // 清空输入框
        originalTextField.clear();
        replacementTextField.clear();
        originalTextField.requestFocus();
        
        AppLogger.info("添加替换规则: " + originalText + " -> " + replacementText);
    }
    
    /**
     * 更新规则
     */
    @FXML
    private void updateRule() {
        ReplaceRule selectedRule = ruleTableView.getSelectionModel().getSelectedItem();
        if (selectedRule == null) {
            showAlert(AlertType.WARNING, "警告", "请先选择要更新的规则");
            return;
        }
        
        String originalText = originalTextField.getText();
        String replacementText = replacementTextField.getText();
        
        if (originalText == null || originalText.trim().isEmpty()) {
            showAlert(AlertType.WARNING, "警告", "请输入要查找的文本");
            return;
        }
        
        if (replacementText == null) {
            replacementText = "";
        }
        
        // 更新规则
        selectedRule.setOriginalText(originalText);
        selectedRule.setReplacementText(replacementText);
        
        // 刷新表格
        ruleTableView.refresh();
        
        // 清空输入框
        originalTextField.clear();
        replacementTextField.clear();
        
        AppLogger.info("更新替换规则: " + originalText + " -> " + replacementText);
    }
    
    /**
     * 删除规则
     */
    @FXML
    private void deleteRule() {
        ReplaceRule selectedRule = ruleTableView.getSelectionModel().getSelectedItem();
        if (selectedRule == null) {
            showAlert(AlertType.WARNING, "警告", "请先选择要删除的规则");
            return;
        }
        
        // 删除规则
        ruleList.remove(selectedRule);
        
        // 清空输入框
        originalTextField.clear();
        replacementTextField.clear();
        
        AppLogger.info("删除替换规则: " + selectedRule.getOriginalText() + " -> " + selectedRule.getReplacementText());
    }
    
    /**
     * 移动规则上移
     */
    @FXML
    private void moveRuleUp() {
        int selectedIndex = ruleTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex <= 0) {
            return;
        }
        
        // 交换位置
        ReplaceRule item = ruleList.remove(selectedIndex);
        ruleList.add(selectedIndex - 1, item);
        
        // 更新选中项
        ruleTableView.getSelectionModel().select(selectedIndex - 1);
        ruleTableView.scrollTo(selectedIndex - 1);
        
        AppLogger.info("规则上移: " + item.getOriginalText());
    }
    
    /**
     * 移动规则下移
     */
    @FXML
    private void moveRuleDown() {
        int selectedIndex = ruleTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= ruleList.size() - 1) {
            return;
        }
        
        // 交换位置
        ReplaceRule item = ruleList.remove(selectedIndex);
        ruleList.add(selectedIndex + 1, item);
        
        // 更新选中项
        ruleTableView.getSelectionModel().select(selectedIndex + 1);
        ruleTableView.scrollTo(selectedIndex + 1);
        
        AppLogger.info("规则下移: " + item.getOriginalText());
    }
    
    /**
     * 清空所有规则
     */
    @FXML
    private void clearRules() {
        if (ruleList.isEmpty()) {
            return;
        }
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("确认");
        alert.setHeaderText(null);
        alert.setContentText("确定要清空所有替换规则吗？");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // 清空规则列表
                ruleList.clear();
                AppLogger.info("清空所有替换规则");
            }
        });
    }
    
    /**
     * 导入规则
     */
    @FXML
    private void importRules() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导入替换规则");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("文本文件", "*.txt")
        );
        
        // 显示打开文件对话框
        Stage stage = (Stage) ruleTableView.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                
                List<ReplaceRule> importedRules = new ArrayList<>();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    
                    // 尝试解析规则，格式: 原文本=替换文本
                    int separatorIndex = line.indexOf("=");
                    if (separatorIndex > 0) {
                        String originalText = line.substring(0, separatorIndex).trim();
                        String replacementText = line.substring(separatorIndex + 1).trim();
                        
                        if (!originalText.isEmpty()) {
                            importedRules.add(new ReplaceRule(originalText, replacementText));
                        }
                    }
                }
                
                if (!importedRules.isEmpty()) {
                    // 添加导入的规则
                    ruleList.addAll(importedRules);
                    
                    showAlert(AlertType.INFORMATION, "导入成功", "成功导入 " + importedRules.size() + " 条替换规则");
                    AppLogger.info("导入 " + importedRules.size() + " 条替换规则从文件: " + file.getName());
                } else {
                    showAlert(AlertType.WARNING, "导入失败", "未能从文件中解析出有效的替换规则");
                }
                
            } catch (Exception e) {
                AppLogger.error("导入替换规则失败: " + e.getMessage(), e);
                showAlert(AlertType.ERROR, "导入失败", "导入替换规则时出错: " + e.getMessage());
            }
        }
    }
    
    /**
     * 导出规则
     */
    @FXML
    private void exportRules() {
        if (ruleList.isEmpty()) {
            showAlert(AlertType.WARNING, "警告", "没有可导出的替换规则");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出替换规则");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("文本文件", "*.txt")
        );
        fileChooser.setInitialFileName("替换规则_" + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
        
        // 显示保存文件对话框
        Stage stage = (Stage) ruleTableView.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                
                // 写入说明
                writer.println("# 文本替换规则");
                writer.println("# 格式: 原文本=替换文本");
                writer.println("# 导出时间: " + java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println();
                
                // 写入规则
                for (ReplaceRule rule : ruleList) {
                    writer.println(rule.getOriginalText() + "=" + rule.getReplacementText());
                }
                
                showAlert(AlertType.INFORMATION, "导出成功", "成功导出 " + ruleList.size() + " 条替换规则");
                AppLogger.info("导出 " + ruleList.size() + " 条替换规则到文件: " + file.getName());
                
            } catch (Exception e) {
                AppLogger.error("导出替换规则失败: " + e.getMessage(), e);
                showAlert(AlertType.ERROR, "导出失败", "导出替换规则时出错: " + e.getMessage());
            }
        }
    }
    
    /**
     * 保存并关闭
     */
    @FXML
    private void saveAndClose() {
        // 获取当前Stage
        Stage stage = (Stage) ruleTableView.getScene().getWindow();
        
        // 返回规则列表并关闭窗口
        stage.close();
    }
    
    /**
     * 显示警告对话框
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 获取规则列表
     */
    public List<ReplaceRule> getRuleList() {
        return new ArrayList<>(ruleList);
    }
    
    /**
     * 设置规则列表
     */
    public void setRuleList(List<ReplaceRule> rules) {
        if (rules != null) {
            ruleList.clear();
            ruleList.addAll(rules);
            AppLogger.info("加载 " + rules.size() + " 条替换规则");
        }
    }
} 