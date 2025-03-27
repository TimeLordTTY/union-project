package com.timelordtty.projectCalendar;

import java.time.LocalDate;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.service.ProjectService;
import com.timelordtty.projectCalendar.utils.DateCalculator;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.time.format.DateTimeFormatter;

/**
 * 项目添加对话框控制器
 */
public class ProjectAddDialogController {
    
    @FXML private TextField projectNameField;
    @FXML private DatePicker onlineDatePicker;
    @FXML private TextField registrationPeriodField;
    @FXML private TextField reviewPeriodField;
    @FXML private DatePicker expectedReviewDatePicker;
    @FXML private TextArea remarkArea;
    @FXML private Label registrationEndDateLabel;
    @FXML private Label earliestReviewDateLabel;
    @FXML private Label validationLabel;
    @FXML private DialogPane dialogPane;
    
    private ProjectService projectService;
    private Project editingProject;
    private boolean isEditing = false;
    
    /**
     * 初始化控制器
     */
    @FXML
    private void initialize() {
        try {
            AppLogger.info("初始化项目添加对话框控制器");
            projectService = ProjectService.getInstance();
            
            // 设置日期选择器格式
            setupDatePickers();
            
            // 初始化校验标签
            validationLabel.setTextFill(Color.RED);
            validationLabel.setText("");
            
            // 设置默认值
            onlineDatePicker.setValue(LocalDate.now());
            registrationPeriodField.setText("5");
            reviewPeriodField.setText("20");
            
            // 注册文本字段监听
            setupTextFieldListeners();
            
            // 触发自动计算
            calculateDates();
            
            // 配置按钮事件
            setupButtonActions();
            
            // 初始默认禁用确定按钮，直到输入项目名称
            if (dialogPane != null) {
                dialogPane.lookupButton(ButtonType.OK).setDisable(true);
            }
        } catch (Exception e) {
            AppLogger.error("初始化项目添加对话框控制器时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 设置日期选择器格式
     */
    private void setupDatePickers() {
        // 设置日期格式
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };
        
        onlineDatePicker.setConverter(converter);
        expectedReviewDatePicker.setConverter(converter);
    }
    
    /**
     * 设置文本字段监听器
     */
    private void setupTextFieldListeners() {
        // 项目名称变更监听，实时验证并控制按钮状态
        projectNameField.textProperty().addListener((obs, oldValue, newValue) -> {
            validateProjectName();
        });
        
        // 上网日期变更监听
        onlineDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            calculateDates();
        });
        
        // 报名期限变更监听
        registrationPeriodField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                registrationPeriodField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                calculateDates();
            }
        });
        
        // 评审周期变更监听
        reviewPeriodField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                reviewPeriodField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                calculateDates();
            }
        });
        
        // 预计评审日期变更监听
        expectedReviewDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            validateReviewDates();
        });
    }
    
    /**
     * 验证项目名称并控制确定按钮状态
     */
    private void validateProjectName() {
        String name = projectNameField.getText().trim();
        
        if (name.isEmpty()) {
            // 项目名称为空，禁用确定按钮并显示错误提示
            projectNameField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            validationLabel.setText("请输入项目名称");
            validationLabel.setVisible(true);
            
            // 禁用确定按钮
            if (dialogPane != null) {
                dialogPane.lookupButton(ButtonType.OK).setDisable(true);
            }
        } else {
            // 项目名称有效，启用确定按钮并清除错误提示
            projectNameField.setStyle("");
            validationLabel.setText("");
            validationLabel.setVisible(false);
            
            // 启用确定按钮
            if (dialogPane != null) {
                dialogPane.lookupButton(ButtonType.OK).setDisable(false);
            }
        }
    }
    
    /**
     * 计算相关日期
     */
    private void calculateDates() {
        LocalDate onlineDate = onlineDatePicker.getValue();
        String regPeriodText = registrationPeriodField.getText().trim();
        String reviewPeriodText = reviewPeriodField.getText().trim();
        
        // 清除之前的提示信息
        validationLabel.setText("");
        
        // 计算报名截止日期，但只在上网日期和报名期限都有值时进行
        if (onlineDate != null && !regPeriodText.isEmpty()) {
            try {
                int regPeriod = Integer.parseInt(regPeriodText);
                if (regPeriod > 0) {
                    LocalDate regEndDate = DateCalculator.calculateDateAfterWorkingDays(onlineDate, regPeriod);
                    registrationEndDateLabel.setText(DateCalculator.formatDate(regEndDate));
                } else {
                    registrationEndDateLabel.setText("");
                }
            } catch (NumberFormatException e) {
                registrationEndDateLabel.setText("无效的输入");
            }
        } else {
            registrationEndDateLabel.setText("");
        }
        
        // 计算最早评审日期，但只在上网日期和评审周期都有值时进行
        if (onlineDate != null && !reviewPeriodText.isEmpty()) {
            try {
                int reviewPeriod = Integer.parseInt(reviewPeriodText);
                if (reviewPeriod > 0) {
                    LocalDate earliestReviewDate = DateCalculator.calculateFirstWorkingDayAfterNaturalDays(onlineDate, reviewPeriod);
                    earliestReviewDateLabel.setText(DateCalculator.formatDate(earliestReviewDate));
                    
                    // 验证预计评审日期是否晚于最早评审日期，但只在预计评审日期已设置时验证
                    if (expectedReviewDatePicker.getValue() != null) {
                        validateReviewDates();
                    }
                } else {
                    earliestReviewDateLabel.setText("");
                }
            } catch (NumberFormatException e) {
                earliestReviewDateLabel.setText("无效的输入");
            }
        } else {
            earliestReviewDateLabel.setText("");
        }
    }
    
    /**
     * 校验预计评审日期和最早评审日期
     */
    private void validateReviewDates() {
        // 清除之前的提示信息
        validationLabel.setText("");
        
        LocalDate expectedReviewDate = expectedReviewDatePicker.getValue();
        
        // 只在预计评审日期和最早评审日期都有值时才进行验证
        if (expectedReviewDate != null && 
            !earliestReviewDateLabel.getText().isEmpty() && 
            !earliestReviewDateLabel.getText().equals("无效的输入")) {
            try {
                LocalDate earliestReviewDate = LocalDate.parse(earliestReviewDateLabel.getText());
                
                if (expectedReviewDate.isBefore(earliestReviewDate)) {
                    validationLabel.setText("当前预计评审日期早于最早评审日期");
                }
            } catch (Exception e) {
                // 解析日期失败，不做处理
                AppLogger.error("解析日期失败: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 设置要编辑的项目
     * @param project 项目
     */
    public void setProject(Project project) {
        this.editingProject = project;
        this.isEditing = true;
        
        // 填充表单数据
        projectNameField.setText(project.getName());
        onlineDatePicker.setValue(project.getOnlineDate());
        registrationPeriodField.setText(String.valueOf(project.getRegistrationPeriod()));
        reviewPeriodField.setText(String.valueOf(project.getReviewPeriod()));
        expectedReviewDatePicker.setValue(project.getExpectedReviewDate());
        remarkArea.setText(project.getRemark());
        
        // 计算并显示报名截止日期和最早评审日期
        registrationEndDateLabel.setText(DateCalculator.formatDate(project.getRegistrationEndDate()));
        earliestReviewDateLabel.setText(DateCalculator.formatDate(project.getEarliestReviewDate()));
        
        // 编辑模式下，如果项目名称已填写，启用确定按钮
        validateProjectName();
    }
    
    /**
     * 获取项目对象
     * @return 项目对象，如果验证失败则返回null
     */
    public Project getProject() {
        if (validateInputs()) {
            String name = projectNameField.getText();
            LocalDate onlineDate = onlineDatePicker.getValue();
            
            // 处理可能为空的字段
            int regPeriod = 0;
            if (!registrationPeriodField.getText().trim().isEmpty()) {
                regPeriod = Integer.parseInt(registrationPeriodField.getText());
            }
            
            int reviewPeriod = 0;
            if (!reviewPeriodField.getText().trim().isEmpty()) {
                reviewPeriod = Integer.parseInt(reviewPeriodField.getText());
            }
            
            LocalDate expectedReviewDate = expectedReviewDatePicker.getValue();
            String remark = remarkArea.getText();
            
            // 计算报名截止日期和最早评审日期，如果上网日期为空则设为null
            LocalDate regEndDate = null;
            LocalDate earliestReviewDate = null;
            
            if (onlineDate != null && regPeriod > 0) {
                regEndDate = DateCalculator.calculateDateAfterWorkingDays(onlineDate, regPeriod);
            }
            
            if (onlineDate != null && reviewPeriod > 0) {
                earliestReviewDate = DateCalculator.calculateFirstWorkingDayAfterNaturalDays(onlineDate, reviewPeriod);
            }
            
            // 如果是编辑模式，则更新现有项目
            if (isEditing) {
                editingProject.setName(name);
                editingProject.setOnlineDate(onlineDate);
                editingProject.setRegistrationPeriod(regPeriod);
                editingProject.setReviewPeriod(reviewPeriod);
                editingProject.setExpectedReviewDate(expectedReviewDate);
                editingProject.setRemark(remark);
                editingProject.setRegistrationEndDate(regEndDate);
                editingProject.setEarliestReviewDate(earliestReviewDate);
                
                return editingProject;
            } else {
                // 创建新项目
                Project newProject = new Project();
                newProject.setName(name);
                newProject.setOnlineDate(onlineDate);
                newProject.setRegistrationPeriod(regPeriod);
                newProject.setReviewPeriod(reviewPeriod);
                newProject.setExpectedReviewDate(expectedReviewDate);
                newProject.setRemark(remark);
                newProject.setRegistrationEndDate(regEndDate);
                newProject.setEarliestReviewDate(earliestReviewDate);
                
                return newProject;
            }
        }
        
        return null;
    }
    
    /**
     * 验证输入
     * @return 是否验证通过
     */
    private boolean validateInputs() {
        // 清除之前的提示信息
        validationLabel.setText("");
        
        // 验证项目名称 - 只有项目名称是必填项
        if (projectNameField.getText().trim().isEmpty()) {
            validationLabel.setText("请输入项目名称");
            projectNameField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return false;
        } else {
            projectNameField.setStyle("");
        }
        
        // 验证报名期限 - 如果已填写，必须是有效的正整数
        if (!registrationPeriodField.getText().trim().isEmpty()) {
            try {
                int regPeriod = Integer.parseInt(registrationPeriodField.getText());
                if (regPeriod <= 0) {
                    validationLabel.setText("报名期限必须大于0");
                    return false;
                }
            } catch (NumberFormatException e) {
                validationLabel.setText("报名期限必须是有效的数字");
                return false;
            }
        }
        
        // 验证评审周期 - 如果已填写，必须是有效的正整数
        if (!reviewPeriodField.getText().trim().isEmpty()) {
            try {
                int reviewPeriod = Integer.parseInt(reviewPeriodField.getText());
                if (reviewPeriod <= 0) {
                    validationLabel.setText("评审周期必须大于0");
                    return false;
                }
            } catch (NumberFormatException e) {
                validationLabel.setText("评审周期必须是有效的数字");
                return false;
            }
        }
        
        // 验证预计评审日期和最早评审日期 - 只在两者都有值时检查
        LocalDate expectedReviewDate = expectedReviewDatePicker.getValue();
        
        if (expectedReviewDate != null && onlineDatePicker.getValue() != null && 
            !reviewPeriodField.getText().trim().isEmpty() && 
            !earliestReviewDateLabel.getText().isEmpty() && 
            !earliestReviewDateLabel.getText().equals("无效的输入")) {
            try {
                LocalDate earliestReviewDate = LocalDate.parse(earliestReviewDateLabel.getText());
                
                if (expectedReviewDate.isBefore(earliestReviewDate)) {
                    String message = "当前预计评审日期早于最早评审日期";
                    validationLabel.setText(message);
                    
                    // 弹出提示对话框
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("日期校验");
                    alert.setHeaderText("日期校验失败");
                    alert.setContentText(message);
                    alert.showAndWait();
                    
                    return false;
                }
            } catch (Exception e) {
                // 解析日期失败，继续验证
                AppLogger.error("解析日期失败: " + e.getMessage(), e);
            }
        }
        
        return true;
    }
    
    /**
     * 设置按钮事件
     */
    private void setupButtonActions() {
        // 注册对话框按钮事件
        if (dialogPane != null) {
            AppLogger.info("设置项目添加对话框按钮事件");
            
            // 处理确定按钮点击
            dialogPane.lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                AppLogger.info("确定按钮被点击");
                
                // 先验证项目名称
                String name = projectNameField.getText().trim();
                if (name.isEmpty()) {
                    AppLogger.info("项目名称为空，禁止提交");
                    projectNameField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                    validationLabel.setText("请输入项目名称");
                    validationLabel.setVisible(true);
                    event.consume(); // 如果项目名称为空，取消事件传播
                    return;
                }
                
                // 再验证其他输入
                if (!validateInputs()) {
                    event.consume(); // 如果验证失败，取消事件传播
                }
            });
            
            // 确保确定和取消按钮有明确的样式
            dialogPane.lookupButton(ButtonType.OK).getStyleClass().add("action-button");
            dialogPane.lookupButton(ButtonType.CANCEL).getStyleClass().add("cancel-button");
            
            // 设置按钮文本
            ((javafx.scene.control.Button) dialogPane.lookupButton(ButtonType.OK)).setText("确定");
            ((javafx.scene.control.Button) dialogPane.lookupButton(ButtonType.CANCEL)).setText("取消");
        } else {
            AppLogger.error("dialogPane为null，无法设置按钮事件");
        }
    }
    
    /**
     * 关闭对话框
     */
    private void closeDialog() {
        if (dialogPane != null) {
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }
} 