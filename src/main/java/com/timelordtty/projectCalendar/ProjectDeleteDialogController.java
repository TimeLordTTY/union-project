package com.timelordtty.projectCalendar;

import com.timelordtty.AppLogger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * 项目删除确认对话框控制器
 */
public class ProjectDeleteDialogController {
    
    @FXML private Label projectNameLabel;
    
    private Project project;
    
    /**
     * 初始化控制器
     */
    @FXML
    private void initialize() {
        AppLogger.info("初始化项目删除确认对话框控制器");
    }
    
    /**
     * 设置要删除的项目
     * @param project 项目对象
     */
    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            projectNameLabel.setText(project.getName());
        }
    }
    
    /**
     * 获取当前项目
     * @return 当前项目
     */
    public Project getProject() {
        return project;
    }
} 