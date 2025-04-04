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
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
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
    
    // 复制按钮
    @FXML private Button copyBidTimeBtn;
    @FXML private Button copyExpertTimeBtn;
    @FXML private Button copyExpertMeetingBtn;
    
    private ProjectService projectService;
    private Project editingProject;
    private boolean isEditing = false;
    private boolean isReadOnly = false;
    
    // 悬浮提示框
    private Popup validationPopup;
    private Text popupText;
    
    // 生成时间选项的方法
    private List<String> generateTimeOptions() {
        List<String> options = new ArrayList<>();
        
        // 生成常用时间选项（整点和半点）
        for (int hour = 0; hour < 24; hour++) {
            // 添加整点
            options.add(String.format("%02d:00", hour));
            // 添加半点
            options.add(String.format("%02d:30", hour));
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
            
            // 初始化悬浮提示框
            initValidationPopup();
            
            // 设置日期选择器格式
            setupDatePickers();
            
            // 设置时间选择器
            List<String> timeOptions = generateTimeOptions();
            expectedReviewTimeComboBox.setItems(FXCollections.observableArrayList(timeOptions));
            expertReviewTimeComboBox.setItems(FXCollections.observableArrayList(timeOptions));
            
            // 设置ComboBox为可编辑，允许直接输入时间
            expectedReviewTimeComboBox.setEditable(true);
            expertReviewTimeComboBox.setEditable(true);
            
            // 设置时间选择器的格式验证
            setupTimeComboBoxValidation(expectedReviewTimeComboBox);
            setupTimeComboBoxValidation(expertReviewTimeComboBox);
            
            // 默认选择9:00
            expectedReviewTimeComboBox.setValue("09:00");
            expertReviewTimeComboBox.setValue("09:00");
            
            // 隐藏原来的validationLabel
            if (validationLabel != null) {
                validationLabel.setVisible(false);
                validationLabel.setManaged(false);
            }
            
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
                // 这里使用内嵌的完整try-catch块
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
     * 初始化悬浮提示框
     */
    private void initValidationPopup() {
        validationPopup = new Popup();
        validationPopup.setAutoHide(true);
        
        // 创建警告图标
        javafx.scene.shape.SVGPath warningIcon = new javafx.scene.shape.SVGPath();
        warningIcon.setContent("M12,1L21,20H3L12,1M12,5L7.5,16H16.5L12,5M11,10V12H13V10H11M11,14V16H13V14H11Z");
        warningIcon.getStyleClass().add("warning-icon");
        warningIcon.setScaleX(0.8);
        warningIcon.setScaleY(0.8);
        
        // 创建文本
        popupText = new Text();
        popupText.getStyleClass().add("popup-validation-text");
        
        // 创建水平布局容器，图标在左，文本在右
        javafx.scene.layout.HBox contentBox = new javafx.scene.layout.HBox(10);
        contentBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        contentBox.getChildren().addAll(warningIcon, popupText);
        
        // 创建容器并应用样式
        StackPane container = new StackPane();
        container.getStyleClass().add("popup-validation");
        container.getChildren().add(contentBox);
        
        validationPopup.getContent().add(container);
    }
    
    /**
     * 显示悬浮提示
     * @param control 控件
     * @param message 提示信息
     */
    private void showValidationPopup(javafx.scene.Node control, String message) {
        if (validationPopup.isShowing()) {
            validationPopup.hide();
        }
        
        popupText.setText(message);
        
        // 调整位置，放在控件上方
        javafx.geometry.Bounds bounds = control.localToScreen(control.getBoundsInLocal());
        validationPopup.show(control, 
                             bounds.getMinX(), 
                             bounds.getMinY() - 50); // 上方50像素
        
        // 2.5秒后自动隐藏
        new Thread(() -> {
            try {
                Thread.sleep(2500);
                Platform.runLater(() -> {
                    if (validationPopup.isShowing()) {
                        validationPopup.hide();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
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
            projectNameField.setStyle("-fx-border-color: #FF6B6B; -fx-border-width: 2px;");
            showValidationPopup(projectNameField, "请输入项目名称");
            
            // 禁用确定按钮
            if (dialogPane != null) {
                dialogPane.lookupButton(ButtonType.OK).setDisable(true);
            }
        } else {
            // 项目名称有效，恢复输入框样式并启用确定按钮
            projectNameField.setStyle("");
            
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
        
        // 重置开标时间输入框样式
        expectedReviewDatePicker.setStyle("");
        
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
                            // 如果开标时间早于最早评审日期，显示提示
                            expectedReviewDatePicker.setStyle("-fx-border-color: #FF6B6B; -fx-border-width: 2px;");
                            showValidationPopup(expectedReviewDatePicker, "当前开标时间早于最早评审日期");
                        } else if (earliestReviewDate.isAfter(expectedReviewDate)) {
                            // 如果最早评审日期晚于开标时间，显示提示
                            expectedReviewDatePicker.setStyle("-fx-border-color: #FF6B6B; -fx-border-width: 2px;");
                            showValidationPopup(expectedReviewDatePicker, "当前最早评审日期晚于开标时间");
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
        
        // 重置开标时间输入框样式
        expectedReviewDatePicker.setStyle("");
        
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
                expectedReviewDatePicker.setStyle("-fx-border-color: #FF6B6B; -fx-border-width: 2px;");
                showValidationPopup(expectedReviewDatePicker, "当前开标时间早于最早评审日期");
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
                showAlert("错误", "请输入项目名称", AlertType.ERROR);
                return false;
            }
            
            // 验证报名期限
            String regPeriodText = registrationPeriodField.getText().trim();
            if (regPeriodText.isEmpty()) {
                showAlert("错误", "请输入报名期限", AlertType.ERROR);
                return false;
            }
            
            try {
                int regPeriod = Integer.parseInt(regPeriodText);
                if (regPeriod < 0) {
                    showAlert("错误", "报名期限必须大于等于0", AlertType.ERROR);
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("错误", "报名期限必须是数字", AlertType.ERROR);
                return false;
            }
            
            // 验证评审周期
            String reviewPeriodText = reviewPeriodField.getText().trim();
            if (reviewPeriodText.isEmpty()) {
                showAlert("错误", "请输入评审周期", AlertType.ERROR);
                return false;
            }
            
            try {
                int reviewPeriod = Integer.parseInt(reviewPeriodText);
                if (reviewPeriod < 0) {
                    showAlert("错误", "评审周期必须大于等于0", AlertType.ERROR);
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("错误", "评审周期必须是数字", AlertType.ERROR);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            AppLogger.error("验证输入时发生异常: " + e.getMessage(), e);
            showAlert("错误", "验证输入时发生异常: " + e.getMessage(), AlertType.ERROR);
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
        // 创建一个悬浮提示框，样式与校验提示一致，但显示时间更长
        Popup alertPopup = new Popup();
        alertPopup.setAutoHide(true);
        
        // 创建图标
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        if (alertType == AlertType.ERROR) {
            // 错误图标
            icon.setContent("M11,15H13V17H11V15M11,7H13V13H11V7M12,2C6.47,2 2,6.5 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M12,20A8,8 0 0,1 4,12A8,8 0 0,1 12,4A8,8 0 0,1 20,12A8,8 0 0,1 12,20Z");
            icon.setStyle("-fx-fill: #D32F2F;"); // 红色
        } else if (alertType == AlertType.WARNING) {
            // 警告图标
            icon.setContent("M12,2L1,21H23M12,6L19.53,19H4.47M11,10V14H13V10M11,16V18H13V16");
            icon.setStyle("-fx-fill: #FFA000;"); // 黄色
        } else {
            // 信息图标
            icon.setContent("M11,9H13V7H11M12,20C7.59,20 4,16.41 4,12C4,7.59 7.59,4 12,4C16.41,4 20,7.59 20,12C20,16.41 16.41,20 12,20M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M11,13H13V17H11V13Z");
            icon.setStyle("-fx-fill: #1976D2;"); // 蓝色
        }
        icon.setScaleX(1.2);
        icon.setScaleY(1.2);
        
        // 标题和消息文本
        Text titleText = new Text(title);
        titleText.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-fill: -pink-text;");
        
        Text messageText = new Text(message);
        messageText.setStyle("-fx-fill: #424242; -fx-font-size: 14px;");
        messageText.setWrappingWidth(250);
        
        // 创建确定按钮
        Button okButton = new Button("确定");
        okButton.getStyleClass().addAll("button", "action-button");
        okButton.setOnAction(e -> alertPopup.hide());
        okButton.setPrefWidth(80);
        
        // 垂直布局
        javafx.scene.layout.VBox contentBox = new javafx.scene.layout.VBox(10);
        contentBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        // 标题行使用水平布局
        javafx.scene.layout.HBox titleBox = new javafx.scene.layout.HBox(10);
        titleBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        titleBox.getChildren().addAll(icon, titleText);
        
        // 将所有元素添加到垂直布局
        contentBox.getChildren().addAll(titleBox, messageText, okButton);
        contentBox.setPadding(new Insets(15));
        
        // 创建容器并应用样式
        StackPane container = new StackPane();
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; " + 
                         "-fx-border-color: -pink-border; -fx-border-width: 1.5; -fx-border-radius: 10; " +
                         "-fx-effect: dropshadow(three-pass-box, -pink-shadow, 10, 0, 0, 4);");
        container.getChildren().add(contentBox);
        
        alertPopup.getContent().add(container);
        
        // 显示在窗口中央
        if (dialogPane != null && dialogPane.getScene() != null && dialogPane.getScene().getWindow() != null) {
            javafx.stage.Window window = dialogPane.getScene().getWindow();
            alertPopup.show(window, 
                        window.getX() + window.getWidth() / 2 - 150, 
                        window.getY() + window.getHeight() / 2 - 100);
            
            // 5秒后自动隐藏
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    Platform.runLater(() -> {
                        if (alertPopup.isShowing()) {
                            alertPopup.hide();
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
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
                // 项目名称为空时禁用确定按钮
                String name = projectNameField.getText().trim();
                okButton.setDisable(name.isEmpty());
                
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
            // 禁用时间选择框
            expectedReviewTimeComboBox.setEditable(false);
            expectedReviewTimeComboBox.setDisable(true);
            expertReviewDatePicker.setEditable(false);
            expertReviewDatePicker.setDisable(true);
            expertReviewTimeComboBox.setEditable(false);
            expertReviewTimeComboBox.setDisable(true);
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
    
    /**
     * 复制开标时间提示语
     */
    @FXML
    private void copyBidTimePrompt() {
        try {
            // 获取项目名称和开标时间
            String projectName = projectNameField.getText().trim();
            if (projectName.isEmpty()) {
                showAlert("无法复制提示语", "请先输入项目名称", AlertType.WARNING);
                return;
            }
            
            LocalDate bidDate = expectedReviewDatePicker.getValue();
            String bidTimeStr = expectedReviewTimeComboBox.getValue();
            if (bidDate == null || bidTimeStr == null || bidTimeStr.isEmpty()) {
                showAlert("无法复制提示语", "请先设置开标时间", AlertType.WARNING);
                return;
            }
            
            // 检查专家评审时间是否在同一天
            LocalDate expertDate = expertReviewDatePicker.getValue();
            String expertTimeStr = expertReviewTimeComboBox.getValue();
            
            String promptText;
            if (expertDate != null && expertTimeStr != null && !expertTimeStr.isEmpty() && bidDate.equals(expertDate)) {
                // 如果在同一天，使用组合格式
                promptText = projectName + " （" + bidTimeStr + " 开 " + expertTimeStr + " 评）侯昱晓\n";
            } else {
                // 否则使用原有格式
                promptText = projectName + "（" + bidTimeStr + " 开）侯昱晓\n";
            }
            
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
            // 获取项目名称和专家评审时间
            String projectName = projectNameField.getText().trim();
            if (projectName.isEmpty()) {
                showAlert("无法复制提示语", "请先输入项目名称", AlertType.WARNING);
                return;
            }
            
            LocalDate expertDate = expertReviewDatePicker.getValue();
            String expertTimeStr = expertReviewTimeComboBox.getValue();
            if (expertDate == null || expertTimeStr == null || expertTimeStr.isEmpty()) {
                showAlert("无法复制提示语", "请先设置专家评审时间", AlertType.WARNING);
                return;
            }
            
            // 检查开标时间是否在同一天
            LocalDate bidDate = expectedReviewDatePicker.getValue();
            String bidTimeStr = expectedReviewTimeComboBox.getValue();
            
            String promptText;
            if (bidDate != null && bidTimeStr != null && !bidTimeStr.isEmpty() && expertDate.equals(bidDate)) {
                // 如果在同一天，使用组合格式
                promptText = projectName + " （" + bidTimeStr + " 开 " + expertTimeStr + " 评）侯昱晓\n";
            } else {
                // 否则使用原有格式
                promptText = projectName + "（" + expertTimeStr + " 评）侯昱晓\n";
            }
            
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
            // 获取专家评审时间
            LocalDate expertDate = expertReviewDatePicker.getValue();
            String expertTimeStr = expertReviewTimeComboBox.getValue();
            if (expertDate == null || expertTimeStr == null || expertTimeStr.isEmpty()) {
                showAlert("无法复制提示语", "请先设置专家评审时间", AlertType.WARNING);
                return;
            }
            
            // 格式化日期时间
            String formattedDate = expertDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String dateTimeStr = formattedDate + " " + expertTimeStr;
            
            // 构建提示语
            String promptText = "请专家老师参加：" + dateTimeStr + "的会议，会议地址位于：上海市黄浦区中山南一路210号北大楼203室（申朋招标）"
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
        
        // 显示成功提示
        showValidationPopup(copyBidTimeBtn, "提示语已复制到剪贴板");
    }
    
    /**
     * 设置时间ComboBox的验证
     * @param comboBox 时间选择框
     */
    private void setupTimeComboBoxValidation(ComboBox<String> comboBox) {
        // 添加文本变更监听
        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                // 时间格式验证（HH:MM）
                String timePattern = "([0-1][0-9]|2[0-3]):[0-5][0-9]";
                if (newValue.matches(timePattern)) {
                    // 格式正确，清除错误样式
                    comboBox.getEditor().setStyle("");
                } else if (!newValue.isEmpty() && !newValue.matches(timePattern)) {
                    // 格式不正确时显示错误样式
                    comboBox.getEditor().setStyle("-fx-border-color: #FF6B6B; -fx-border-width: 2px;");
                } else {
                    // 输入为空时恢复样式
                    comboBox.getEditor().setStyle("");
                }
            }
        });
        
        // 失去焦点时验证并格式化
        comboBox.getEditor().focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) { // 失去焦点时
                String text = comboBox.getEditor().getText();
                if (text != null && !text.isEmpty()) {
                    String timePattern = "([0-1][0-9]|2[0-3]):[0-5][0-9]";
                    if (text.matches(timePattern)) {
                        // 无需额外处理，保持当前值
                    } else {
                        // 格式不正确时恢复上一个有效值或默认值
                        if (comboBox.getValue() != null) {
                            comboBox.getEditor().setText(comboBox.getValue());
                        } else {
                            comboBox.setValue("09:00");
                        }
                        showValidationPopup(comboBox, "请输入有效的时间格式（HH:MM）");
                    }
                    comboBox.getEditor().setStyle("");
                }
            }
        });
    }
} 