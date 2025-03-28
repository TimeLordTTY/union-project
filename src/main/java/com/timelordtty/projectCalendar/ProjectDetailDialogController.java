package com.timelordtty.projectCalendar;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.timelordtty.AppLogger;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * 项目详情对话框控制器
 */
public class ProjectDetailDialogController {
    
    @FXML private Label projectNameLabel;
    @FXML private Label onlineDateLabel;
    @FXML private Label registrationPeriodLabel;
    @FXML private Label registrationEndDateLabel;
    @FXML private Label reviewPeriodLabel;
    @FXML private Label earliestReviewDateLabel;
    @FXML private Label expectedReviewDateLabel;
    @FXML private Label expertReviewDateLabel;
    @FXML private Label remarkLabel;
    
    @FXML private Button copyBidTimeBtn;
    @FXML private Button copyExpertTimeBtn;
    @FXML private Button copyExpertMeetingBtn;
    
    private Project currentProject;
    
    /**
     * 初始化控制器
     */
    @FXML
    private void initialize() {
        AppLogger.info("初始化项目详情对话框控制器");
    }
    
    /**
     * 设置当前项目
     * @param project 项目对象
     */
    public void setProject(Project project) {
        this.currentProject = project;
    }
    
    /**
     * 复制开标时间提示语
     */
    @FXML
    private void copyBidTimePrompt() {
        try {
            if (currentProject == null || currentProject.getExpectedReviewTime() == null) {
                AppLogger.warn("无法复制开标时间提示，项目或开标时间为空");
                return;
            }
            
            // 获取项目名称和开标时间
            String projectName = currentProject.getName();
            LocalDateTime bidTime = currentProject.getExpectedReviewTime();
            String timeStr = String.format("%02d:%02d", bidTime.getHour(), bidTime.getMinute());
            
            // 构建提示语
            String promptText = projectName + "（" + timeStr + " 开）侯昱晓\n";
            
            // 复制到剪贴板
            copyToClipboard(promptText);
            AppLogger.info("已复制开标时间提示语到剪贴板");
        } catch (Exception e) {
            AppLogger.error("复制开标时间提示语时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 复制专家评审时间提示语
     */
    @FXML
    private void copyExpertTimePrompt() {
        try {
            if (currentProject == null || currentProject.getExpertReviewTime() == null) {
                AppLogger.warn("无法复制专家评审时间提示，项目或专家评审时间为空");
                return;
            }
            
            // 获取项目名称和专家评审时间
            String projectName = currentProject.getName();
            LocalDateTime expertTime = currentProject.getExpertReviewTime();
            String timeStr = String.format("%02d:%02d", expertTime.getHour(), expertTime.getMinute());
            
            // 构建提示语
            String promptText = projectName + "（" + timeStr + " 评）侯昱晓\n";
            
            // 复制到剪贴板
            copyToClipboard(promptText);
            AppLogger.info("已复制专家评审时间提示语到剪贴板");
        } catch (Exception e) {
            AppLogger.error("复制专家评审时间提示语时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 复制专家会议提示语
     */
    @FXML
    private void copyExpertMeetingPrompt() {
        try {
            if (currentProject == null || currentProject.getExpertReviewTime() == null) {
                AppLogger.warn("无法复制专家会议提示，项目或专家评审时间为空");
                return;
            }
            
            // 获取专家评审时间
            LocalDateTime expertTime = currentProject.getExpertReviewTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String expertTimeStr = expertTime.format(formatter);
            
            // 构建提示语
            String promptText = "请专家老师参加：" + expertTimeStr + "的会议，会议地址位于：上海市黄浦区中山南一路210号北大楼203室（申朋招标）"
                             + "（无停车位，地铁8号线、4号线，西藏南路站3号出口左转100米左右，收到请回复） 项目负责人：侯昱晓";
            
            // 复制到剪贴板
            copyToClipboard(promptText);
            AppLogger.info("已复制专家会议提示语到剪贴板");
        } catch (Exception e) {
            AppLogger.error("复制专家会议提示语时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 复制文本到剪贴板
     * @param text 要复制的文本
     */
    private void copyToClipboard(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }
} 