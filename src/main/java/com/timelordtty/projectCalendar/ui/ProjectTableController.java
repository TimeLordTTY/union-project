package com.timelordtty.projectCalendar.ui;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.Project;
import com.timelordtty.projectCalendar.service.ProjectService;
import com.timelordtty.projectCalendar.utils.DateCalculator;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;

/**
 * 项目表格控制器，负责管理项目表格相关逻辑
 */
public class ProjectTableController {
    
    private ProjectService projectService;
    
    // 表格及其组件
    private TableView<Project> projectTableView;
    private TextField searchField;
    
    // 项目列表数据
    private ObservableList<Project> projectData = FXCollections.observableArrayList();
    private FilteredList<Project> filteredData;
    
    // 回调函数
    private Consumer<Project> onProjectSelectedCallback;
    private Runnable onAddProjectCallback;
    private Consumer<Project> onEditProjectCallback;
    private Consumer<Project> onDeleteProjectCallback;
    
    /**
     * 构造函数
     * @param projectService 项目服务
     * @param projectTableView 项目表格视图
     * @param searchField 搜索框
     */
    public ProjectTableController(ProjectService projectService, 
                                 TableView<Project> projectTableView, 
                                 TextField searchField) {
        this.projectService = projectService;
        this.projectTableView = projectTableView;
        this.searchField = searchField;
    }
    
    /**
     * 设置项目选择回调
     * @param callback 回调函数
     */
    public void setOnProjectSelectedCallback(Consumer<Project> callback) {
        this.onProjectSelectedCallback = callback;
    }
    
    /**
     * 设置添加项目回调
     * @param callback 回调函数
     */
    public void setOnAddProjectCallback(Runnable callback) {
        this.onAddProjectCallback = callback;
    }
    
    /**
     * 设置编辑项目回调
     * @param callback 回调函数
     */
    public void setOnEditProjectCallback(Consumer<Project> callback) {
        this.onEditProjectCallback = callback;
    }
    
    /**
     * 设置删除项目回调
     * @param callback 回调函数
     */
    public void setOnDeleteProjectCallback(Consumer<Project> callback) {
        this.onDeleteProjectCallback = callback;
    }
    
    /**
     * 初始化表格
     */
    public void initialize() {
        try {
            AppLogger.info("初始化项目表格");
            
            // 创建过滤数据
            filteredData = new FilteredList<>(projectData, p -> true);
            
            // 配置表格列
            configureTableColumns();
            
            // 配置表格行和上下文菜单
            configureTableRowsAndContextMenu();
            
            // 配置搜索功能
            configureSearch();
            
            // 加载项目数据
            refreshProjects();
            
            AppLogger.info("项目表格初始化完成");
        } catch (Exception e) {
            AppLogger.error("初始化项目表格失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 配置表格列
     */
    private void configureTableColumns() {
        try {
            AppLogger.info("配置项目表格列");
            
            // 清除现有列
            projectTableView.getColumns().clear();
            
            // 项目名称列
            TableColumn<Project, String> nameColumn = new TableColumn<>("项目名称");
            nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
            nameColumn.setPrefWidth(150);
            
            // 项目报名截止日期列
            TableColumn<Project, LocalDate> registrationEndDateColumn = new TableColumn<>("报名截止日期");
            registrationEndDateColumn.setCellValueFactory(cellData -> 
                    new SimpleObjectProperty<>(cellData.getValue().getRegistrationEndDate()));
            registrationEndDateColumn.setCellFactory(column -> new TableCell<Project, LocalDate>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(DateCalculator.formatDate(date));
                        
                        // 设置样式：如果日期已过，则显示红色
                        if (date.isBefore(LocalDate.now())) {
                            setStyle("-fx-text-fill: #ff4444;");
                        } else if (date.isEqual(LocalDate.now())) {
                            setStyle("-fx-text-fill: #ff8800;");
                        } else if (date.isBefore(LocalDate.now().plusDays(7))) {
                            setStyle("-fx-text-fill: #ffbb00;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            });
            registrationEndDateColumn.setPrefWidth(120);
            
            // 预计评审日期列
            TableColumn<Project, LocalDate> reviewDateColumn = new TableColumn<>("预计评审日期");
            reviewDateColumn.setCellValueFactory(cellData -> 
                    new SimpleObjectProperty<>(cellData.getValue().getExpectedReviewDate()));
            reviewDateColumn.setCellFactory(column -> new TableCell<Project, LocalDate>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(DateCalculator.formatDate(date));
                        
                        // 设置样式：如果日期已过，则显示红色
                        if (date.isBefore(LocalDate.now())) {
                            setStyle("-fx-text-fill: #ff4444;");
                        } else if (date.isEqual(LocalDate.now())) {
                            setStyle("-fx-text-fill: #ff8800;");
                        } else if (date.isBefore(LocalDate.now().plusDays(7))) {
                            setStyle("-fx-text-fill: #ffbb00;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            });
            reviewDateColumn.setPrefWidth(120);
            
            // 项目状态列
            TableColumn<Project, String> statusColumn = new TableColumn<>("项目状态");
            statusColumn.setCellValueFactory(cellData -> {
                Project project = cellData.getValue();
                String status = project.getStatusText();
                return new SimpleStringProperty(status);
            });
            statusColumn.setCellFactory(column -> new TableCell<Project, String>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    
                    if (empty || status == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(status);
                        
                        // 根据状态设置文本颜色
                        switch (status) {
                            case "已完成":
                                setStyle("-fx-text-fill: #4CAF50;"); // 绿色
                                break;
                            case "进行中":
                                setStyle("-fx-text-fill: #2196F3;"); // 蓝色
                                break;
                            case "即将开始":
                                setStyle("-fx-text-fill: #FF9800;"); // 橙色
                                break;
                            case "已过期":
                                setStyle("-fx-text-fill: #F44336;"); // 红色
                                break;
                            default:
                                setStyle("");
                                break;
                        }
                    }
                }
            });
            statusColumn.setPrefWidth(100);
            
            // 添加所有列
            projectTableView.getColumns().addAll(nameColumn, registrationEndDateColumn, reviewDateColumn, statusColumn);
            
            // 配置排序
            SortedList<Project> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(projectTableView.comparatorProperty());
            projectTableView.setItems(sortedData);
            
            // 设置默认排序为按报名截止日期升序排序
            projectTableView.getSortOrder().add(registrationEndDateColumn);
            registrationEndDateColumn.setSortType(TableColumn.SortType.ASCENDING);
            projectTableView.sort();
            
            AppLogger.info("项目表格列配置完成");
        } catch (Exception e) {
            AppLogger.error("配置项目表格列失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 配置表格行和上下文菜单
     */
    private void configureTableRowsAndContextMenu() {
        try {
            AppLogger.info("配置项目表格行和上下文菜单");
            
            projectTableView.setRowFactory(tv -> {
                TableRow<Project> row = new TableRow<>();
                
                // 创建上下文菜单
                ContextMenu contextMenu = new ContextMenu();
                
                // 编辑项目菜单项
                MenuItem editItem = new MenuItem("编辑项目");
                editItem.setOnAction(event -> {
                    if (row.getItem() != null && onEditProjectCallback != null) {
                        onEditProjectCallback.accept(row.getItem());
                    }
                });
                
                // 删除项目菜单项
                MenuItem deleteItem = new MenuItem("删除项目");
                deleteItem.setOnAction(event -> {
                    if (row.getItem() != null && onDeleteProjectCallback != null) {
                        onDeleteProjectCallback.accept(row.getItem());
                    }
                });
                
                // 添加项目菜单项
                MenuItem addItem = new MenuItem("添加项目");
                addItem.setOnAction(event -> {
                    if (onAddProjectCallback != null) {
                        onAddProjectCallback.run();
                    }
                });
                
                // 重新加载项目菜单项
                MenuItem reloadItem = new MenuItem("刷新项目列表");
                reloadItem.setOnAction(event -> refreshProjects());
                
                contextMenu.getItems().addAll(
                    editItem, 
                    deleteItem, 
                    new SeparatorMenuItem(), 
                    addItem, 
                    reloadItem
                );
                
                // 设置点击监听器
                row.setOnMouseClicked(event -> {
                    // 检查是否有项目被选中
                    if (!row.isEmpty()) {
                        // 双击或单击，触发选择事件
                        if (event.getButton() == MouseButton.PRIMARY) {
                            if (event.getClickCount() == 2) {
                                // 双击时编辑项目
                                if (onEditProjectCallback != null) {
                                    onEditProjectCallback.accept(row.getItem());
                                }
                            } else if (event.getClickCount() == 1) {
                                // 单击时通知选中项目
                                if (onProjectSelectedCallback != null) {
                                    onProjectSelectedCallback.accept(row.getItem());
                                }
                            }
                        }
                    }
                });
                
                // 根据是否为空行决定是否显示上下文菜单
                row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(contextMenu)
                );
                
                return row;
            });
            
            AppLogger.info("项目表格行和上下文菜单配置完成");
        } catch (Exception e) {
            AppLogger.error("配置项目表格行和上下文菜单失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 配置搜索功能
     */
    private void configureSearch() {
        try {
            AppLogger.info("配置项目搜索功能");
            
            // 添加监听器，在文本变化时更新过滤条件
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(project -> {
                    // 如果没有搜索文本，显示所有项目
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    
                    // 将搜索文本转换为小写
                    String lowerCaseFilter = newValue.toLowerCase();
                    
                    // 比较项目名称
                    if (project.getName().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    
                    // 比较项目状态
                    if (project.getStatusText().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    
                    // 没有匹配
                    return false;
                });
            });
            
            AppLogger.info("项目搜索功能配置完成");
        } catch (Exception e) {
            AppLogger.error("配置项目搜索功能失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 刷新项目列表
     */
    public void refreshProjects() {
        try {
            AppLogger.info("刷新项目列表");
            
            // 保存当前选中的项目
            Project selectedProject = projectTableView.getSelectionModel().getSelectedItem();
            
            // 获取所有项目并更新列表
            List<Project> allProjects = projectService.getAllProjects();
            
            // 更新数据
            projectData.clear();
            if (allProjects != null) {
                projectData.addAll(allProjects);
                
                // 如果有之前选中的项目，尝试重新选中
                if (selectedProject != null) {
                    for (Project project : projectData) {
                        if (project.getId() == selectedProject.getId()) {
                            projectTableView.getSelectionModel().select(project);
                            break;
                        }
                    }
                }
            }
            
            AppLogger.info("项目列表刷新完成，共 " + projectData.size() + " 个项目");
        } catch (Exception e) {
            AppLogger.error("刷新项目列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 选择指定的项目
     * @param project 要选择的项目
     */
    public void selectProject(Project project) {
        try {
            if (project == null) {
                return;
            }
            
            // 查找并选择项目
            for (Project p : projectData) {
                if (p.getId() == project.getId()) {
                    projectTableView.getSelectionModel().select(p);
                    projectTableView.scrollTo(p);
                    
                    // 触发选择回调
                    if (onProjectSelectedCallback != null) {
                        onProjectSelectedCallback.accept(p);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            AppLogger.error("选择项目失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取当前选中的项目
     * @return 当前选中的项目，如果没有则返回null
     */
    public Project getSelectedProject() {
        return projectTableView.getSelectionModel().getSelectedItem();
    }
    
    /**
     * 清除当前选择
     */
    public void clearSelection() {
        projectTableView.getSelectionModel().clearSelection();
    }
} 