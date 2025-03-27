package com.timelordtty.projectCalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.service.ProjectService;
import com.timelordtty.projectCalendar.utils.DateCalculator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * 项目添加对话框控制器
 */
public class ProjectAddDialogController {
    
    @FXML private TextField projectNameField;
    @FXML private DatePicker onlineDatePicker;
    @FXML private TextField registrationPeriodField;
    @FXML private TextField reviewPeriodField;
    @FXML private DatePicker expectedReviewDatePicker;
    @FXML private ComboBox<String> expectedReviewTimeComboBox;
    @FXML private DatePicker expertReviewDatePicker;
    @FXML private ComboBox<String> expertReviewTimeComboBox;
    @FXML private TextArea remarkArea;
    @FXML private Label registrationEndDateLabel;
    @FXML private Label earliestReviewDateLabel;
    @FXML private Label suggestedOnlineDateLabel;
    @FXML private Label validationLabel;
    @FXML private DialogPane dialogPane;
    
    private ProjectService projectService;
    private Project editingProject;
    private boolean isEditing = false;
    private boolean isReadOnly = false;
    
    // 生成时间选项的方法
    private List<String> generateTimeOptions() {
        List<String> options = new ArrayList<>();
        for (int hour = 8; hour <= 18; hour++) {
            options.add(String.format("%02d:00", hour));
            if (hour < 18) {
                options.add(String.format("%02d:30", hour));
            }
        }
        return options;
    }
    
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
            
            // 设置时间选择器
            List<String> timeOptions = generateTimeOptions();
            expectedReviewTimeComboBox.setItems(FXCollections.observableArrayList(timeOptions));
            expertReviewTimeComboBox.setItems(FXCollections.observableArrayList(timeOptions));
            
            // 默认选择9:00
            expectedReviewTimeComboBox.setValue("09:00");
            expertReviewTimeComboBox.setValue("09:00");
            
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
            
            // 使用Platform.runLater确保在JavaFX线程完成UI初始化后设置按钮事件和禁用状态
            Platform.runLater(() -> {
                try {
                    // 如果dialogPane已经被设置，则设置按钮事件
                    if (dialogPane != null) {
                        setupButtonActions();
                    }
                } catch (Exception e) {
                    AppLogger.error("设置按钮事件时发生异常: " + e.getMessage(), e);
                }
            });
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
        expertReviewDatePicker.setConverter(converter);
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
        
        // 开标时间日期变更监听
        expectedReviewDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            validateReviewDates();
            updateSuggestedOnlineDate();
        });
        
        // 开标时间时间选择监听
        expectedReviewTimeComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            // 这里不需要进行特别处理，在获取时再组合日期和时间
        });
        
        // 专家评审时间相关监听
        expertReviewDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            // 专家评审时间无需验证
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
        
        // 清除之前可能存在的日期相关提示信息（但保留其他提示如项目名称验证）
        if (validationLabel.getText().contains("开标时间")) {
            validationLabel.setText("");
        }
        
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
                    
                    // 验证最早评审日期与开标时间的关系
                    LocalDate expectedReviewDate = expectedReviewDatePicker.getValue();
                    if (expectedReviewDate != null) {
                        if (expectedReviewDate.isBefore(earliestReviewDate)) {
                            // 如果开标时间早于最早评审日期，显示提示（但不阻止操作）
                            validationLabel.setText("当前开标时间早于最早评审日期");
                            validationLabel.setVisible(true);
                        } else if (earliestReviewDate.isAfter(expectedReviewDate)) {
                            // 如果最早评审日期晚于开标时间，显示提示（但不阻止操作）
                            validationLabel.setText("当前最早评审日期晚于开标时间");
                            validationLabel.setVisible(true);
                        }
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
        
        // 更新建议上网日期
        updateSuggestedOnlineDate();
    }
    
    /**
     * 更新建议上网日期
     */
    private void updateSuggestedOnlineDate() {
        LocalDate expectedReviewDate = expectedReviewDatePicker.getValue();
        String reviewPeriodText = reviewPeriodField.getText().trim();
        
        if (expectedReviewDate != null && !reviewPeriodText.isEmpty()) {
            try {
                int reviewPeriod = Integer.parseInt(reviewPeriodText);
                if (reviewPeriod > 0) {
                    // 建议上网日期 = 开标时间 - 评审周期天数
                    LocalDate suggestedDate = expectedReviewDate.minusDays(reviewPeriod);
                    suggestedOnlineDateLabel.setText(
                        DateCalculator.formatDate(suggestedDate)
                    );
                } else {
                    suggestedOnlineDateLabel.setText("");
                }
            } catch (NumberFormatException e) {
                suggestedOnlineDateLabel.setText("");
            }
        } else {
            suggestedOnlineDateLabel.setText("");
        }
    }
    
    /**
     * 校验开标时间和最早评审日期
     */
    private void validateReviewDates() {
        LocalDate expectedReviewDate = expectedReviewDatePicker.getValue();
        String reviewPeriodText = reviewPeriodField.getText().trim();
        LocalDate onlineDate = onlineDatePicker.getValue();
        
        // 如果缺少必要的值，则不进行验证
        if (expectedReviewDate == null || reviewPeriodText.isEmpty() || onlineDate == null) {
            return;
        }
        
        try {
            int reviewPeriod = Integer.parseInt(reviewPeriodText);
            if (reviewPeriod <= 0) {
                return;
            }
            
            // 计算最早评审日期
            LocalDate earliestReviewDate = DateCalculator.calculateFirstWorkingDayAfterNaturalDays(onlineDate, reviewPeriod);
            
            // 验证日期关系
            if (expectedReviewDate.isBefore(earliestReviewDate)) {
                validationLabel.setText("当前开标时间早于最早评审日期");
                validationLabel.setVisible(true);
            } else {
                validationLabel.setText("");
                validationLabel.setVisible(false);
            }
        } catch (NumberFormatException e) {
            // 忽略
        }
    }
    
    /**
     * 设置项目
     * @param project 项目对象
     */
    public void setProject(Project project) {
        try {
            this.editingProject = project;
            this.isEditing = true;
            
            if (project != null) {
                projectNameField.setText(project.getName());
                
                if (project.getOnlineDate() != null) {
                    onlineDatePicker.setValue(project.getOnlineDate());
                }
                
                registrationPeriodField.setText(String.valueOf(project.getRegistrationPeriod()));
                reviewPeriodField.setText(String.valueOf(project.getReviewPeriod()));
                
                if (project.getExpectedReviewTime() != null) {
                    expectedReviewDatePicker.setValue(project.getExpectedReviewDate());
                    
                    // 设置时间
                    String timeStr = String.format("%02d:%02d", 
                        project.getExpectedReviewTime().getHour(),
                        project.getExpectedReviewTime().getMinute()
                    );
                    expectedReviewTimeComboBox.setValue(timeStr);
                }
                
                if (project.getExpertReviewTime() != null) {
                    expertReviewDatePicker.setValue(project.getExpertReviewDate());
                    
                    // 设置时间
                    String timeStr = String.format("%02d:%02d", 
                        project.getExpertReviewTime().getHour(),
                        project.getExpertReviewTime().getMinute()
                    );
                    expertReviewTimeComboBox.setValue(timeStr);
                }
                
                if (project.getRemark() != null) {
                    remarkArea.setText(project.getRemark());
                }
                
                // 触发计算
                calculateDates();
                
                // 确保按钮状态正确
                Platform.runLater(this::validateProjectName);
            }
        } catch (Exception e) {
            AppLogger.error("设置项目数据时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取项目
     * @return 项目对象
     */
    public Project getProject() {
        try {
            if (editingProject == null) {
                editingProject = new Project();
            }
            
            editingProject.setName(projectNameField.getText().trim());
            
            // 上网日期
            editingProject.setOnlineDate(onlineDatePicker.getValue());
            
            // 报名期限
            try {
                int regPeriod = Integer.parseInt(registrationPeriodField.getText().trim());
                editingProject.setRegistrationPeriod(regPeriod);
            } catch (NumberFormatException e) {
                editingProject.setRegistrationPeriod(0);
            }
            
            // 评审周期
            try {
                int reviewPeriod = Integer.parseInt(reviewPeriodField.getText().trim());
                editingProject.setReviewPeriod(reviewPeriod);
            } catch (NumberFormatException e) {
                editingProject.setReviewPeriod(0);
            }
            
            // 计算报名截止日期和最早评审日期
            if (editingProject.getOnlineDate() != null) {
                // 报名截止日期
                if (editingProject.getRegistrationPeriod() > 0) {
                    LocalDate regEndDate = DateCalculator.calculateDateAfterWorkingDays(
                        editingProject.getOnlineDate(), 
                        editingProject.getRegistrationPeriod()
                    );
                    editingProject.setRegistrationEndDate(regEndDate);
                } else {
                    editingProject.setRegistrationEndDate(null);
                }
                
                // 最早评审日期
                if (editingProject.getReviewPeriod() > 0) {
                    LocalDate earliestReviewDate = DateCalculator.calculateFirstWorkingDayAfterNaturalDays(
                        editingProject.getOnlineDate(), 
                        editingProject.getReviewPeriod()
                    );
                    editingProject.setEarliestReviewDate(earliestReviewDate);
                } else {
                    editingProject.setEarliestReviewDate(null);
                }
            } else {
                editingProject.setRegistrationEndDate(null);
                editingProject.setEarliestReviewDate(null);
            }
            
            // 开标时间
            LocalDate expectedReviewDate = expectedReviewDatePicker.getValue();
            if (expectedReviewDate != null) {
                // 解析时间
                String timeStr = expectedReviewTimeComboBox.getValue();
                LocalTime time = LocalTime.of(9, 0); // 默认9:00
                if (timeStr != null && !timeStr.isEmpty()) {
                    String[] parts = timeStr.split(":");
                    if (parts.length == 2) {
                        try {
                            int hour = Integer.parseInt(parts[0]);
                            int minute = Integer.parseInt(parts[1]);
                            time = LocalTime.of(hour, minute);
                        } catch (NumberFormatException e) {
                            // 忽略，使用默认时间
                        }
                    }
                }
                
                // 设置日期时间
                editingProject.setExpectedReviewTime(LocalDateTime.of(expectedReviewDate, time));
            } else {
                editingProject.setExpectedReviewTime(null);
            }
            
            // 专家评审时间
            LocalDate expertReviewDate = expertReviewDatePicker.getValue();
            if (expertReviewDate != null) {
                // 解析时间
                String timeStr = expertReviewTimeComboBox.getValue();
                LocalTime time = LocalTime.of(9, 0); // 默认9:00
                if (timeStr != null && !timeStr.isEmpty()) {
                    String[] parts = timeStr.split(":");
                    if (parts.length == 2) {
                        try {
                            int hour = Integer.parseInt(parts[0]);
                            int minute = Integer.parseInt(parts[1]);
                            time = LocalTime.of(hour, minute);
                        } catch (NumberFormatException e) {
                            // 忽略，使用默认时间
                        }
                    }
                }
                
                // 设置日期时间
                editingProject.setExpertReviewTime(LocalDateTime.of(expertReviewDate, time));
            } else {
                editingProject.setExpertReviewTime(null);
            }
            
            // 备注
            editingProject.setRemark(remarkArea.getText());
            
            return editingProject;
        } catch (Exception e) {
            AppLogger.error("获取项目数据时发生异常: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 验证输入
     * @return 是否验证通过
     */
    private boolean validateInputs() {
        try {
            // 验证项目名称
            String name = projectNameField.getText().trim();
            if (name.isEmpty()) {
                showAlert("错误", "请输入项目名称", Alert.AlertType.ERROR);
                return false;
            }
            
            // 验证报名期限
            String regPeriodText = registrationPeriodField.getText().trim();
            if (regPeriodText.isEmpty()) {
                showAlert("错误", "请输入报名期限", Alert.AlertType.ERROR);
                return false;
            }
            
            try {
                int regPeriod = Integer.parseInt(regPeriodText);
                if (regPeriod < 0) {
                    showAlert("错误", "报名期限必须大于等于0", Alert.AlertType.ERROR);
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("错误", "报名期限必须是数字", Alert.AlertType.ERROR);
                return false;
            }
            
            // 验证评审周期
            String reviewPeriodText = reviewPeriodField.getText().trim();
            if (reviewPeriodText.isEmpty()) {
                showAlert("错误", "请输入评审周期", Alert.AlertType.ERROR);
                return false;
            }
            
            try {
                int reviewPeriod = Integer.parseInt(reviewPeriodText);
                if (reviewPeriod < 0) {
                    showAlert("错误", "评审周期必须大于等于0", Alert.AlertType.ERROR);
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("错误", "评审周期必须是数字", Alert.AlertType.ERROR);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            AppLogger.error("验证输入时发生异常: " + e.getMessage(), e);
            showAlert("错误", "验证输入时发生异常: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }
    
    /**
     * 显示警告对话框
     * @param title 标题
     * @param message 消息
     * @param alertType 警告类型
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 设置按钮事件
     */
    private void setupButtonActions() {
        try {
            if (dialogPane == null) {
                AppLogger.error("dialogPane为null，无法设置按钮事件");
                return;
            }
            
            // 获取确定按钮
            javafx.scene.Node okButton = dialogPane.lookupButton(ButtonType.OK);
            if (okButton != null) {
                // 初始禁用确定按钮，直到输入项目名称
                okButton.setDisable(true);
                AppLogger.info("初始禁用确定按钮");
                
                // 添加确定按钮的点击监听器，在提交前进行验证
                okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    if (!validateInputs()) {
                        event.consume(); // 阻止对话框关闭
                    }
                });
            } else {
                AppLogger.error("无法找到对话框确定按钮");
            }
        } catch (Exception e) {
            AppLogger.error("设置按钮事件时发生异常: " + e.getMessage(), e);
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
    
    /**
     * 设置项目服务
     * @param projectService 项目服务
     */
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }
    
    /**
     * 设置初始日期
     * @param initialDate 初始日期
     */
    public void setInitialDate(LocalDate initialDate) {
        if (initialDate != null) {
            onlineDatePicker.setValue(initialDate);
            calculateDates();
        }
    }
    
    /**
     * 设置是否只读模式
     * @param readOnly 是否只读
     */
    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
        
        // 设置所有字段为只读
        if (readOnly) {
            projectNameField.setEditable(false);
            reviewPeriodField.setEditable(false);
            onlineDatePicker.setEditable(false);
            onlineDatePicker.setDisable(true);
            registrationPeriodField.setEditable(false);
            registrationEndDateLabel.setDisable(true);
            earliestReviewDateLabel.setDisable(true);
            expectedReviewDatePicker.setEditable(false);
            expectedReviewDatePicker.setDisable(true);
            remarkArea.setEditable(false);
        }
    }
    
    /**
     * 获取对话框面板
     * @return 对话框面板
     */
    public DialogPane getDialogPane() {
        return dialogPane;
    }
    
    /**
     * 设置对话框面板
     * @param dialogPane 对话框面板
     */
    public void setDialogPane(DialogPane dialogPane) {
        this.dialogPane = dialogPane;
        if (dialogPane != null) {
            setupButtonActions();
        }
    }
} 