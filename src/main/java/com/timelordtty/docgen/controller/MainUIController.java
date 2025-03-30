package com.timelordtty.docgen.controller;

import com.timelordtty.corrector.controller.TextCorrectorController;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * 主界面控制器
 * 负责管理主窗口和各个功能模块
 */
public class MainUIController {
    
    @FXML private VBox root;
    @FXML private Pane mainContainer;
    @FXML private TabPane tabPane;
    
    private DocumentGeneratorController documentGeneratorController;
    private TextCorrectorController textCorrectorController;
    
    @FXML
    private void initialize() {
        // 初始化时设置自适应布局
        setupResponsiveLayout();
        
        // 设置选项卡切换事件
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                String tabId = newTab.getId();
                if ("docGenTab".equals(tabId)) {
                    loadDocumentGenerator();
                } else if ("textCorrectorTab".equals(tabId)) {
                    loadTextCorrector();
                }
            }
        });
        
        // 默认加载文档生成器
        loadDocumentGenerator();
    }

    /**
     * 设置自适应布局
     */
    private void setupResponsiveLayout() {
        // 主容器自适应
        mainContainer.prefWidthProperty().bind(root.widthProperty());
        mainContainer.prefHeightProperty().bind(root.heightProperty());
        
        // 标签页自适应
        tabPane.prefWidthProperty().bind(mainContainer.widthProperty());
        tabPane.prefHeightProperty().bind(mainContainer.heightProperty());
        
        // 监听窗口大小变化，调整布局
        root.widthProperty().addListener((obs, oldVal, newVal) -> {
            adjustLayout();
        });
        
        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            adjustLayout();
        });
    }

    /**
     * 调整布局
     */
    private void adjustLayout() {
        // 获取当前选中的标签页
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return;
        
        String tabId = selectedTab.getId();
        if ("docGenTab".equals(tabId)) {
            // 调整文档生成器布局
            if (documentGeneratorController != null) {
                documentGeneratorController.adjustLayout();
            }
        } else if ("textCorrectorTab".equals(tabId)) {
            // 调整文本纠错器布局
            if (textCorrectorController != null) {
                textCorrectorController.adjustLayout();
            }
        }
    }
    
    /**
     * 加载文档生成器
     */
    private void loadDocumentGenerator() {
        // 实现文档生成器加载逻辑
        if (documentGeneratorController == null) {
            documentGeneratorController = new DocumentGeneratorController();
        }
    }
    
    /**
     * 加载文本纠错器
     */
    private void loadTextCorrector() {
        // 实现文本纠错器加载逻辑
        if (textCorrectorController == null) {
            textCorrectorController = new TextCorrectorController();
        }
    }
} 