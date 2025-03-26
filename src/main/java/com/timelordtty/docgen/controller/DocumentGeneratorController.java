package com.timelordtty.docgen.controller;

import java.io.File;

import com.timelordtty.AppLogger;
import com.timelordtty.docgen.service.DocGeneratorService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * 文档生成控制器
 * 处理从Word或Excel模板生成文档的功能
 */
public class DocumentGeneratorController {
    
    @FXML
    private ComboBox<String> documentTypeComboBox;
    
    @FXML
    private TextField templateFilePathTextField;
    
    @FXML
    private Button chooseTemplateFileButton;
    
    @FXML
    private TextField dataFilePathTextField;
    
    @FXML
    private Button chooseDataFileButton;
    
    @FXML
    private TextField outputDirectoryTextField;
    
    @FXML
    private Button chooseOutputDirectoryButton;
    
    @FXML
    private Button generateButton;
    
    @FXML
    private Label statusLabel;
    
    // 默认模板目录
    private final String DEFAULT_TEMPLATES_DIR = "D:\\Project\\union-project\\PersonalApps\\多功能工具集\\templates";
    
    // 文档生成服务
    private DocGeneratorService docService;
    
    @FXML
    private void initialize() {
        // 初始化文档生成服务
        docService = new DocGeneratorService();
        
        // 初始化文档类型下拉框
        documentTypeComboBox.getItems().addAll("Word", "Excel");
        documentTypeComboBox.setValue("Word");
        
        documentTypeComboBox.setOnAction(event -> {
            updateTemplateFileExtensions();
        });
        
        // 设置状态标签初始文本
        statusLabel.setText("就绪");
        
        updateTemplateFileExtensions();
    }
    
    private void updateTemplateFileExtensions() {
        // 根据选择的文档类型更新文件过滤器
        if (documentTypeComboBox.getValue() != null) {
            String documentType = documentTypeComboBox.getValue();
            if ("Word".equals(documentType)) {
                templateFilePathTextField.setPromptText("选择Word模板文件(.docx)");
            } else if ("Excel".equals(documentType)) {
                templateFilePathTextField.setPromptText("选择Excel模板文件(.xlsx)");
            }
        }
    }
    
    @FXML
    private void chooseTemplateFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择模板文件");
        
        // 设置默认目录
        File defaultDir = new File(DEFAULT_TEMPLATES_DIR);
        if (!defaultDir.exists()) {
            defaultDir.mkdirs();
            AppLogger.info("已创建默认模板目录：" + DEFAULT_TEMPLATES_DIR);
        }
        
        String documentType = documentTypeComboBox.getValue();
        String subDir = null;
        if ("Word".equals(documentType)) {
            subDir = "word";
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Word 文档", "*.docx"));
        } else if ("Excel".equals(documentType)) {
            subDir = "excel";
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel 表格", "*.xlsx"));
        }
        
        // 如果有子目录且存在，则打开子目录
        if (subDir != null) {
            File typeDir = new File(defaultDir, subDir);
            if (typeDir.exists() && typeDir.isDirectory()) {
                fileChooser.setInitialDirectory(typeDir);
            } else {
                fileChooser.setInitialDirectory(defaultDir);
            }
        } else {
            fileChooser.setInitialDirectory(defaultDir);
        }
        
        File selectedFile = fileChooser.showOpenDialog(chooseTemplateFileButton.getScene().getWindow());
        if (selectedFile != null) {
            templateFilePathTextField.setText(selectedFile.getAbsolutePath());
            statusLabel.setText("已选择模板文件: " + selectedFile.getName());
            AppLogger.info("已选择模板文件：" + selectedFile.getAbsolutePath());
        }
    }
    
    @FXML
    private void chooseDataFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择数据文件");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("JSON 文件", "*.json"),
            new ExtensionFilter("所有文件", "*.*")
        );
        
        // 设置默认目录为templates/json目录
        File defaultDir = new File(DEFAULT_TEMPLATES_DIR);
        File jsonDir = new File(defaultDir, "json");
        
        if (jsonDir.exists() && jsonDir.isDirectory()) {
            fileChooser.setInitialDirectory(jsonDir);
        } else if (defaultDir.exists() && defaultDir.isDirectory()) {
            fileChooser.setInitialDirectory(defaultDir);
        } else {
            defaultDir.mkdirs();
            AppLogger.info("已创建默认模板目录：" + DEFAULT_TEMPLATES_DIR);
            fileChooser.setInitialDirectory(defaultDir);
        }
        
        File selectedFile = fileChooser.showOpenDialog(chooseDataFileButton.getScene().getWindow());
        if (selectedFile != null) {
            dataFilePathTextField.setText(selectedFile.getAbsolutePath());
            statusLabel.setText("已选择数据文件: " + selectedFile.getName());
            AppLogger.info("已选择数据文件：" + selectedFile.getAbsolutePath());
        }
    }
    
    @FXML
    private void chooseOutputDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择输出目录");
        
        // 设置默认目录
        File defaultDir = new File(DEFAULT_TEMPLATES_DIR);
        if (defaultDir.exists() && defaultDir.isDirectory()) {
            directoryChooser.setInitialDirectory(defaultDir);
        } else {
            defaultDir.mkdirs();
            AppLogger.info("已创建默认模板目录：" + DEFAULT_TEMPLATES_DIR);
            directoryChooser.setInitialDirectory(defaultDir);
        }
        
        File selectedDirectory = directoryChooser.showDialog(chooseOutputDirectoryButton.getScene().getWindow());
        if (selectedDirectory != null) {
            outputDirectoryTextField.setText(selectedDirectory.getAbsolutePath());
            statusLabel.setText("已选择输出目录: " + selectedDirectory.getName());
            AppLogger.info("已选择输出目录：" + selectedDirectory.getAbsolutePath());
        }
    }
    
    @FXML
    private void generateDocument() {
        statusLabel.setText("正在处理...");
        AppLogger.info("开始生成文档");
        
        // 验证是否所有必填字段都已填写
        if (templateFilePathTextField.getText().isEmpty() || 
            outputDirectoryTextField.getText().isEmpty() || 
            dataFilePathTextField.getText().isEmpty()) {
            
            showAlert(AlertType.ERROR, "错误", "请填写所有字段", 
                     "模板文件、输出目录和数据文件均为必填项。");
            statusLabel.setText("错误：请填写所有必填字段");
            return;
        }
        
        try {
            // 获取选择的模板类型
            String documentType = documentTypeComboBox.getValue();
            boolean isWordTemplate = "Word".equals(documentType);
            
            // 调用服务生成文档
            String templatePath = templateFilePathTextField.getText();
            String outputDir = outputDirectoryTextField.getText();
            String dataFilePath = dataFilePathTextField.getText();
            
            String outputFilePath = docService.generateDocument(
                templatePath, 
                outputDir, 
                dataFilePath, 
                isWordTemplate
            );
            
            // 显示成功消息
            statusLabel.setText("文档生成成功: " + outputFilePath);
            showAlert(AlertType.INFORMATION, "成功", "文档生成成功", 
                     "文档已生成至: " + outputFilePath);
            
        } catch (Exception e) {
            AppLogger.error("生成文档失败", e);
            statusLabel.setText("错误: " + e.getMessage());
            showAlert(AlertType.ERROR, "错误", "生成文档失败", 
                     "原因: " + e.getMessage());
        }
    }
    
    /**
     * 显示警告对话框
     * @param type 警告类型
     * @param title 标题
     * @param header 头部信息
     * @param content 内容
     */
    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 