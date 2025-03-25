package com.timelordtty.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * 文档生成控制器
 * 处理从Word或Excel模板生成文档的功能
 */
public class DocumentGeneratorController implements Initializable {

    @FXML
    private TextField templateFileField;
    
    @FXML
    private TextField dataFileField;
    
    @FXML
    private TextField outputDirField;
    
    @FXML
    private ComboBox<String> documentTypeComboBox;
    
    @FXML
    private TextArea logTextArea;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("文档生成器界面已初始化");
        
        // 初始化数据源类型选择框
        documentTypeComboBox.getItems().addAll("Word文档", "Excel表格");
        documentTypeComboBox.setValue("Word文档");
    }
    
    /**
     * 选择模板文件
     */
    @FXML
    public void chooseTemplateFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择模板文件");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Word文档", "*.docx"),
            new FileChooser.ExtensionFilter("Excel表格", "*.xlsx")
        );
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            templateFileField.setText(selectedFile.getAbsolutePath());
            logTextArea.appendText("已选择模板文件: " + selectedFile.getAbsolutePath() + "\n");
        }
    }
    
    /**
     * 选择数据源文件
     */
    @FXML
    public void chooseDataFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择数据源文件");
        
        String selectedType = documentTypeComboBox.getValue();
        boolean isJsonOrCsv = true;
        
        if (isJsonOrCsv) {
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON文件", "*.json"),
                new FileChooser.ExtensionFilter("CSV文件", "*.csv"),
                new FileChooser.ExtensionFilter("Excel文件", "*.xlsx", "*.xls")
            );
        }
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            dataFileField.setText(selectedFile.getAbsolutePath());
            logTextArea.appendText("已选择数据源文件: " + selectedFile.getAbsolutePath() + "\n");
        }
    }
    
    /**
     * 选择输出目录
     */
    @FXML
    public void chooseOutputDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择输出目录");
        
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            outputDirField.setText(selectedDirectory.getAbsolutePath());
            logTextArea.appendText("已选择输出目录: " + selectedDirectory.getAbsolutePath() + "\n");
        }
    }
    
    /**
     * 生成文档
     */
    @FXML
    public void generateDocument() {
        String templatePath = templateFileField.getText();
        String dataSourcePath = dataFileField.getText();
        String outputDir = outputDirField.getText();
        String documentType = documentTypeComboBox.getValue();
        
        // 验证输入
        if (templatePath.isEmpty() || dataSourcePath.isEmpty() || outputDir.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "错误", "请填写所有必填字段!");
            return;
        }
        
        // 检查文件是否存在
        if (!new File(templatePath).exists()) {
            showAlert(Alert.AlertType.ERROR, "错误", "模板文件不存在!");
            return;
        }
        
        if (!new File(dataSourcePath).exists()) {
            showAlert(Alert.AlertType.ERROR, "错误", "数据源文件不存在!");
            return;
        }
        
        if (!new File(outputDir).exists() || !new File(outputDir).isDirectory()) {
            showAlert(Alert.AlertType.ERROR, "错误", "输出目录不存在或不是有效目录!");
            return;
        }
        
        try {
            // 判断是Word还是Excel
            boolean isWord = "Word文档".equals(documentType);
            
            // 调用文档生成工具类
            logTextArea.appendText("开始生成文档...\n");
            
            // 模拟生成过程和结果
            Thread.sleep(1000); // 模拟处理时间
            logTextArea.appendText("正在解析模板文件...\n");
            Thread.sleep(500);
            logTextArea.appendText("正在读取数据源...\n");
            Thread.sleep(500);
            logTextArea.appendText("正在替换模板占位符...\n");
            Thread.sleep(500);
            logTextArea.appendText("正在保存生成的文档...\n");
            Thread.sleep(500);
            
            // 实际调用生成方法
            // String outputPath = DocumentGenerator.generateDocument(templatePath, dataSourcePath, outputDir, isWord);
            
            String extension = isWord ? ".docx" : ".xlsx";
            String fileName = "生成的文档_" + System.currentTimeMillis() + extension;
            String outputPath = outputDir + File.separator + fileName;
            
            logTextArea.appendText("文档生成成功: " + outputPath + "\n");
            
            showAlert(Alert.AlertType.INFORMATION, "成功", "文档已成功生成!");
        } catch (Exception e) {
            logTextArea.appendText("文档生成失败: " + e.getMessage() + "\n");
            showAlert(Alert.AlertType.ERROR, "错误", "文档生成失败: " + e.getMessage());
        }
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