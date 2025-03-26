package com.timelordtty.projectCalendar.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.Project;
import com.timelordtty.projectCalendar.utils.DateCalculator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 项目服务类，提供项目的CRUD操作
 */
public class ProjectService {
    private static final String DB_URL = "jdbc:h2:./data/projectCalendar";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    
    private ObservableList<Project> projects = FXCollections.observableArrayList();
    private static ProjectService instance;
    
    private ProjectService() {
        initializeDatabase();
        loadAllProjects();
    }
    
    public static ProjectService getInstance() {
        if (instance == null) {
            instance = new ProjectService();
        }
        return instance;
    }
    
    /**
     * 初始化数据库，创建表（如果不存在）
     */
    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            String sql = "CREATE TABLE IF NOT EXISTS projects (" +
                         "id IDENTITY PRIMARY KEY, " +
                         "name VARCHAR(255) NOT NULL, " +
                         "review_period INT NOT NULL, " +
                         "online_date DATE NOT NULL, " +
                         "registration_period INT NOT NULL, " +
                         "registration_end_date DATE NOT NULL, " +
                         "earliest_review_date DATE NOT NULL, " +
                         "expected_review_date DATE NOT NULL, " +
                         "remark TEXT" +
                         ")";
            
            stmt.execute(sql);
            AppLogger.info("项目日历数据库初始化成功");
            
        } catch (SQLException e) {
            AppLogger.error("初始化数据库失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 获取数据库连接
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    /**
     * 从数据库加载所有项目
     */
    public void loadAllProjects() {
        projects.clear();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM projects ORDER BY online_date DESC")) {
            
            while (rs.next()) {
                Project project = new Project();
                project.setId(rs.getLong("id"));
                project.setName(rs.getString("name"));
                project.setReviewPeriod(rs.getInt("review_period"));
                project.setOnlineDate(rs.getDate("online_date").toLocalDate());
                project.setRegistrationPeriod(rs.getInt("registration_period"));
                project.setRegistrationEndDate(rs.getDate("registration_end_date").toLocalDate());
                project.setEarliestReviewDate(rs.getDate("earliest_review_date").toLocalDate());
                project.setExpectedReviewDate(rs.getDate("expected_review_date").toLocalDate());
                project.setRemark(rs.getString("remark"));
                
                projects.add(project);
            }
            
            AppLogger.info("从数据库加载了 " + projects.size() + " 个项目");
            
        } catch (SQLException e) {
            AppLogger.error("加载项目失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 保存项目到数据库，如果存在ID则更新，否则插入
     * @param project 要保存的项目
     * @return 成功返回true，失败返回false
     */
    public boolean saveProject(Project project) {
        if (project.getId() != null) {
            return updateProject(project);
        } else {
            return addProject(project);
        }
    }
    
    /**
     * 添加新项目
     * @param project 要添加的项目
     * @return 成功返回true，失败返回false
     */
    public boolean addProject(Project project) {
        String sql = "INSERT INTO projects (name, review_period, online_date, registration_period, " +
                     "registration_end_date, earliest_review_date, expected_review_date, remark) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, project.getName());
            pstmt.setInt(2, project.getReviewPeriod());
            pstmt.setDate(3, Date.valueOf(project.getOnlineDate()));
            pstmt.setInt(4, project.getRegistrationPeriod());
            pstmt.setDate(5, Date.valueOf(project.getRegistrationEndDate()));
            pstmt.setDate(6, Date.valueOf(project.getEarliestReviewDate()));
            pstmt.setDate(7, Date.valueOf(project.getExpectedReviewDate()));
            pstmt.setString(8, project.getRemark());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                return false;
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    project.setId(generatedKeys.getLong(1));
                    projects.add(project);
                    AppLogger.info("新增项目：" + project.getName());
                    return true;
                } else {
                    return false;
                }
            }
            
        } catch (SQLException e) {
            AppLogger.error("添加项目失败：" + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 更新现有项目
     * @param project 要更新的项目
     * @return 成功返回true，失败返回false
     */
    public boolean updateProject(Project project) {
        String sql = "UPDATE projects SET name=?, review_period=?, online_date=?, registration_period=?, " +
                     "registration_end_date=?, earliest_review_date=?, expected_review_date=?, remark=? " +
                     "WHERE id=?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, project.getName());
            pstmt.setInt(2, project.getReviewPeriod());
            pstmt.setDate(3, Date.valueOf(project.getOnlineDate()));
            pstmt.setInt(4, project.getRegistrationPeriod());
            pstmt.setDate(5, Date.valueOf(project.getRegistrationEndDate()));
            pstmt.setDate(6, Date.valueOf(project.getEarliestReviewDate()));
            pstmt.setDate(7, Date.valueOf(project.getExpectedReviewDate()));
            pstmt.setString(8, project.getRemark());
            pstmt.setLong(9, project.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // 更新内存中的项目列表
                for (int i = 0; i < projects.size(); i++) {
                    if (projects.get(i).getId().equals(project.getId())) {
                        projects.set(i, project);
                        break;
                    }
                }
                AppLogger.info("更新项目：" + project.getName());
                return true;
            } else {
                return false;
            }
            
        } catch (SQLException e) {
            AppLogger.error("更新项目失败：" + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 删除项目
     * @param project 要删除的项目
     * @return 成功返回true，失败返回false
     */
    public boolean deleteProject(Project project) {
        String sql = "DELETE FROM projects WHERE id=?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, project.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                projects.remove(project);
                AppLogger.info("删除项目：" + project.getName());
                return true;
            } else {
                return false;
            }
            
        } catch (SQLException e) {
            AppLogger.error("删除项目失败：" + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取所有项目列表
     * @return 项目列表
     */
    public ObservableList<Project> getProjects() {
        return projects;
    }
    
    /**
     * 根据日期获取项目
     * @param date 日期
     * @param dateType 日期类型 (online_date/registration_end_date/earliest_review_date/expected_review_date)
     * @return 该日期下的项目列表
     */
    public List<Project> getProjectsByDate(LocalDate date, String dateType) {
        List<Project> result = new ArrayList<>();
        
        for (Project project : projects) {
            LocalDate projectDate = null;
            
            switch (dateType) {
                case "online_date":
                    projectDate = project.getOnlineDate();
                    break;
                case "registration_end_date":
                    projectDate = project.getRegistrationEndDate();
                    break;
                case "earliest_review_date":
                    projectDate = project.getEarliestReviewDate();
                    break;
                case "expected_review_date":
                    projectDate = project.getExpectedReviewDate();
                    break;
                case "all":
                    if (project.getOnlineDate().equals(date) ||
                        project.getRegistrationEndDate().equals(date) ||
                        project.getEarliestReviewDate().equals(date) ||
                        project.getExpectedReviewDate().equals(date)) {
                        result.add(project);
                    }
                    continue;
                default:
                    continue;
            }
            
            if (projectDate != null && projectDate.equals(date)) {
                result.add(project);
            }
        }
        
        return result;
    }
    
    /**
     * 计算并更新报名截止日期
     * @param project 项目对象
     */
    public void calculateRegistrationEndDate(Project project) {
        if (project.getOnlineDate() != null && project.getRegistrationPeriod() > 0) {
            LocalDate regEndDate = DateCalculator.calculateDateAfterWorkingDays(
                project.getOnlineDate(), project.getRegistrationPeriod());
            project.setRegistrationEndDate(regEndDate);
        }
    }
    
    /**
     * 计算并更新最早评审日期
     * @param project 项目对象
     */
    public void calculateEarliestReviewDate(Project project) {
        if (project.getOnlineDate() != null && project.getReviewPeriod() > 0) {
            LocalDate earliestDate = DateCalculator.calculateFirstWorkingDayAfterNaturalDays(
                project.getOnlineDate(), project.getReviewPeriod());
            project.setEarliestReviewDate(earliestDate);
        }
    }
    
    /**
     * 获取本周和下周（如果今天是周五）的报名截止或预计评审项目
     * @return 本周或下周报名截止/预计评审的项目列表
     */
    public List<Project> getThisWeekAndNextWeekProjects() {
        List<Project> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // 获取本周的开始和结束日期
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        // 如果今天是周五，也包含下周的项目
        boolean isIncludeNextWeek = today.getDayOfWeek() == DayOfWeek.FRIDAY;
        LocalDate endDate = isIncludeNextWeek ? endOfWeek.plusDays(7) : endOfWeek;
        
        for (Project project : projects) {
            // 检查报名截止日期是否在目标范围内
            LocalDate regEndDate = project.getRegistrationEndDate();
            if (regEndDate != null && 
                !regEndDate.isBefore(today) && 
                !regEndDate.isAfter(endDate)) {
                result.add(project);
                continue; // 已添加，无需再检查预计评审日期
            }
            
            // 检查预计评审日期是否在目标范围内
            LocalDate expReviewDate = project.getExpectedReviewDate();
            if (expReviewDate != null && 
                !expReviewDate.isBefore(today) && 
                !expReviewDate.isAfter(endDate)) {
                result.add(project);
            }
        }
        
        return result;
    }
    
    /**
     * 检查项目是否在本周
     * @param project 项目
     * @return 是否在本周
     */
    public boolean isProjectInThisWeek(Project project) {
        // 获取本周的起始和结束日期
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        // 检查项目的关键日期是否在本周
        // 上网日期
        LocalDate onlineDate = project.getOnlineDate();
        // 报名截止日期
        LocalDate regEndDate = project.getRegistrationEndDate();
        // 预计评审日期
        LocalDate reviewDate = project.getExpectedReviewDate();
        
        // 检查各个日期是否在本周范围内
        if ((onlineDate.isEqual(startOfWeek) || onlineDate.isAfter(startOfWeek)) && 
            (onlineDate.isEqual(endOfWeek) || onlineDate.isBefore(endOfWeek))) {
            return true;
        } else if ((regEndDate.isEqual(startOfWeek) || regEndDate.isAfter(startOfWeek)) && 
                   (regEndDate.isEqual(endOfWeek) || regEndDate.isBefore(endOfWeek))) {
            return true;
        } else if ((reviewDate.isEqual(startOfWeek) || reviewDate.isAfter(startOfWeek)) && 
                   (reviewDate.isEqual(endOfWeek) || reviewDate.isBefore(endOfWeek))) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查项目是否在下周
     * @param project 项目
     * @return 是否在下周
     */
    public boolean isProjectInNextWeek(Project project) {
        // 获取下周的起始和结束日期
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        LocalDate startOfNextWeek = endOfWeek.plusDays(1);
        LocalDate endOfNextWeek = startOfNextWeek.plusDays(6);
        
        // 检查项目的关键日期是否在下周
        // 上网日期
        LocalDate onlineDate = project.getOnlineDate();
        // 报名截止日期
        LocalDate regEndDate = project.getRegistrationEndDate();
        // 预计评审日期
        LocalDate reviewDate = project.getExpectedReviewDate();
        
        // 检查各个日期是否在下周范围内
        if ((onlineDate.isEqual(startOfNextWeek) || onlineDate.isAfter(startOfNextWeek)) && 
            (onlineDate.isEqual(endOfNextWeek) || onlineDate.isBefore(endOfNextWeek))) {
            return true;
        } else if ((regEndDate.isEqual(startOfNextWeek) || regEndDate.isAfter(startOfNextWeek)) && 
                   (regEndDate.isEqual(endOfNextWeek) || regEndDate.isBefore(endOfNextWeek))) {
            return true;
        } else if ((reviewDate.isEqual(startOfNextWeek) || reviewDate.isAfter(startOfNextWeek)) && 
                   (reviewDate.isEqual(endOfNextWeek) || reviewDate.isBefore(endOfNextWeek))) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取指定日期的所有项目
     * @param date 日期
     * @return 项目列表
     */
    public List<Project> getProjectsForDate(LocalDate date) {
        AppLogger.info("获取日期 " + DateCalculator.formatDate(date) + " 的所有项目");
        List<Project> dateProjects = new ArrayList<>();
        
        try {
            if (date == null) {
                AppLogger.error("无法获取项目，日期为空");
                return dateProjects;
            }
            
            // 添加所有类型日期匹配的项目
            List<Project> onlineDateProjects = getProjectsByDate(date, "online_date");
            List<Project> regEndDateProjects = getProjectsByDate(date, "registration_end_date");
            List<Project> earliestReviewDateProjects = getProjectsByDate(date, "earliest_review_date");
            List<Project> expectedReviewDateProjects = getProjectsByDate(date, "expected_review_date");
            
            // 合并所有类型的项目，避免重复
            for (Project project : onlineDateProjects) {
                if (!dateProjects.contains(project)) {
                    dateProjects.add(project);
                }
            }
            
            for (Project project : regEndDateProjects) {
                if (!dateProjects.contains(project)) {
                    dateProjects.add(project);
                }
            }
            
            for (Project project : earliestReviewDateProjects) {
                if (!dateProjects.contains(project)) {
                    dateProjects.add(project);
                }
            }
            
            for (Project project : expectedReviewDateProjects) {
                if (!dateProjects.contains(project)) {
                    dateProjects.add(project);
                }
            }
            
            AppLogger.info("日期 " + DateCalculator.formatDate(date) + " 共有 " + dateProjects.size() + " 个项目");
        } catch (Exception e) {
            AppLogger.error("获取日期项目时发生异常: " + e.getMessage(), e);
        }
        
        return dateProjects;
    }
} 