package com.timelordtty.projectCalendar.ui;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.Project;
import com.timelordtty.projectCalendar.ProjectAddDialogController;
import com.timelordtty.projectCalendar.service.ProjectService;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * 项目对话框辅助类，负责处理项目相关对话框的显示和管理
 */
public class ProjectDialogHelper {

    private ProjectService projectService;
    private Window ownerWindow;
    private Consumer<Project> onProjectChangedCallback;

    /**
     * 构造函数
     * @param projectService 项目服务
     * @param ownerWindow 父窗口
     */
    public ProjectDialogHelper(ProjectService projectService, Window ownerWindow) {
        this.projectService = projectService;
        this.ownerWindow = ownerWindow;
    }

    /**
     * 设置项目变更回调函数
     * @param callback 项目变更回调函数
     */
    public void setOnProjectChangedCallback(Consumer<Project> callback) {
        this.onProjectChangedCallback = callback;
    }

    /**
     * 显示添加项目对话框
     * @param startDate 初始开始日期，如果为null则使用当天
     */
    public void showAddProjectDialog(LocalDate startDate) {
        try {
            AppLogger.info("显示添加项目对话框");
            
            // 加载FXML
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/timelordtty/projectCalendar/ProjectAddDialog.fxml"));
            AnchorPane page = loader.load();

            // 创建对话框
            Stage dialogStage = new Stage();
            dialogStage.setTitle("添加项目");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(ownerWindow);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // 设置控制器
            ProjectAddDialogController controller = loader.getController();
            
            // 如果指定了开始日期，则设置
            if (startDate != null) {
                // 通过设置上网日期实现开始日期的设置
                // controller.setStartDate(startDate); // 该方法不存在，使用其他方式
                Project tempProject = new Project();
                tempProject.setOnlineDate(startDate);
                controller.setProject(tempProject);
            }

            // 显示对话框并等待直到用户关闭
            dialogStage.showAndWait();

            // 如果有项目被添加并且回调不为空，则调用回调
            Project addedProject = controller.getProject();
            if (addedProject != null && onProjectChangedCallback != null) {
                // 保存新项目
                projectService.saveProject(addedProject);
                onProjectChangedCallback.accept(addedProject);
            }
            
            AppLogger.info("添加项目对话框关闭");
        } catch (IOException e) {
            AppLogger.error("显示添加项目对话框时发生异常: " + e.getMessage(), e);
            showErrorAlert("无法打开添加项目对话框", "系统错误", e.getMessage());
        }
    }

    /**
     * 显示编辑项目对话框
     * @param project 要编辑的项目
     */
    public void showEditProjectDialog(Project project) {
        try {
            if (project == null) {
                AppLogger.warn("尝试编辑空项目");
                return;
            }
            
            AppLogger.info("显示编辑项目对话框: " + project.getName());
            
            // 加载FXML
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/timelordtty/projectCalendar/ProjectAddDialog.fxml"));
            AnchorPane page = loader.load();

            // 创建对话框
            Stage dialogStage = new Stage();
            dialogStage.setTitle("编辑项目");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(ownerWindow);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // 设置控制器
            ProjectAddDialogController controller = loader.getController();
            controller.setProject(project);

            // 显示对话框并等待直到用户关闭
            dialogStage.showAndWait();

            // 如果项目被更新并且回调不为空，则调用回调
            Project updatedProject = controller.getProject();
            if (updatedProject != null && onProjectChangedCallback != null) {
                // 保存更新后的项目
                projectService.saveProject(updatedProject);
                onProjectChangedCallback.accept(updatedProject);
            }
            
            AppLogger.info("编辑项目对话框关闭");
        } catch (IOException e) {
            AppLogger.error("显示编辑项目对话框时发生异常: " + e.getMessage(), e);
            showErrorAlert("无法打开编辑项目对话框", "系统错误", e.getMessage());
        }
    }

    /**
     * 显示删除项目确认对话框
     * @param project 要删除的项目
     * @return 是否确认删除
     */
    public boolean showDeleteProjectConfirmation(Project project) {
        try {
            if (project == null) {
                AppLogger.warn("尝试删除空项目");
                return false;
            }
            
            AppLogger.info("显示删除项目确认对话框: " + project.getName());
            
            // 创建确认对话框
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("确认删除");
            alert.setHeaderText("删除项目");
            alert.setContentText("确定要删除项目 \"" + project.getName() + "\" 吗？此操作不可撤销。");
            alert.initOwner(ownerWindow);

            // 显示对话框并等待用户响应
            Optional<ButtonType> result = alert.showAndWait();
            
            // 如果用户点击了确认
            if (result.isPresent() && result.get() == ButtonType.OK) {
                AppLogger.info("用户确认删除项目: " + project.getName());
                
                // 删除项目
                projectService.deleteProject(project);
                
                // 调用回调（传递null表示删除操作）
                if (onProjectChangedCallback != null) {
                    onProjectChangedCallback.accept(null);
                }
                
                return true;
            } else {
                AppLogger.info("用户取消删除项目: " + project.getName());
                return false;
            }
        } catch (Exception e) {
            AppLogger.error("删除项目时发生异常: " + e.getMessage(), e);
            showErrorAlert("无法删除项目", "系统错误", e.getMessage());
            return false;
        }
    }

    /**
     * 显示错误提示对话框
     * @param title 标题
     * @param header 头部文本
     * @param content 内容文本
     */
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(ownerWindow);
        alert.showAndWait();
    }
} 