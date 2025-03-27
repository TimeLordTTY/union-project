package com.timelordtty.projectCalendar.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.Project;
import com.timelordtty.projectCalendar.utils.DatabaseManager;

/**
 * 项目数据访问对象类，负责项目数据的CRUD操作
 */
public class ProjectDao {
    
    /**
     * 获取所有项目
     * @return 项目列表
     */
    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects ORDER BY online_date DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Project project = mapResultSetToProject(rs);
                projects.add(project);
            }
            
        } catch (SQLException e) {
            AppLogger.error("获取所有项目时发生数据库错误: " + e.getMessage(), e);
        }
        
        return projects;
    }
    
    /**
     * 根据ID获取项目
     * @param id 项目ID
     * @return 项目对象，如果未找到则返回null
     */
    public Project getProjectById(Long id) {
        String sql = "SELECT * FROM projects WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProject(rs);
                }
            }
            
        } catch (SQLException e) {
            AppLogger.error("根据ID获取项目时发生数据库错误: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * 插入新项目
     * @param project 项目对象
     * @return 是否成功插入
     */
    public boolean insertProject(Project project) {
        String sql = "INSERT INTO projects (name, review_period, online_date, " +
                    "registration_period, registration_end_date, earliest_review_date, " +
                    "expected_review_date, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setPreparedStatementParameters(pstmt, project);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        project.setId(generatedKeys.getLong(1));
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            AppLogger.error("插入项目时发生数据库错误: " + e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * 更新项目
     * @param project 项目对象
     * @return 是否成功更新
     */
    public boolean updateProject(Project project) {
        String sql = "UPDATE projects SET name = ?, review_period = ?, online_date = ?, " +
                    "registration_period = ?, registration_end_date = ?, earliest_review_date = ?, " +
                    "expected_review_date = ?, remark = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            setPreparedStatementParameters(pstmt, project);
            pstmt.setLong(9, project.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            AppLogger.error("更新项目时发生数据库错误: " + e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * 删除项目
     * @param id 项目ID
     * @return 是否成功删除
     */
    public boolean deleteProject(Long id) {
        String sql = "DELETE FROM projects WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            AppLogger.error("删除项目时发生数据库错误: " + e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * 设置PreparedStatement参数
     * @param pstmt PreparedStatement对象
     * @param project 项目对象
     * @throws SQLException SQL异常
     */
    private void setPreparedStatementParameters(PreparedStatement pstmt, Project project) throws SQLException {
        pstmt.setString(1, project.getName());
        pstmt.setInt(2, project.getReviewPeriod());
        
        if (project.getOnlineDate() != null) {
            pstmt.setDate(3, Date.valueOf(project.getOnlineDate()));
        } else {
            pstmt.setNull(3, java.sql.Types.DATE);
        }
        
        pstmt.setInt(4, project.getRegistrationPeriod());
        
        if (project.getRegistrationEndDate() != null) {
            pstmt.setDate(5, Date.valueOf(project.getRegistrationEndDate()));
        } else {
            pstmt.setNull(5, java.sql.Types.DATE);
        }
        
        if (project.getEarliestReviewDate() != null) {
            pstmt.setDate(6, Date.valueOf(project.getEarliestReviewDate()));
        } else {
            pstmt.setNull(6, java.sql.Types.DATE);
        }
        
        if (project.getExpectedReviewDate() != null) {
            pstmt.setDate(7, Date.valueOf(project.getExpectedReviewDate()));
        } else {
            pstmt.setNull(7, java.sql.Types.DATE);
        }
        
        pstmt.setString(8, project.getRemark());
    }
    
    /**
     * 将ResultSet映射为Project对象
     * @param rs ResultSet结果集
     * @return Project对象
     * @throws SQLException SQL异常
     */
    private Project mapResultSetToProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        
        project.setId(rs.getLong("id"));
        project.setName(rs.getString("name"));
        project.setReviewPeriod(rs.getInt("review_period"));
        
        Date onlineDate = rs.getDate("online_date");
        if (onlineDate != null) {
            project.setOnlineDate(onlineDate.toLocalDate());
        }
        
        project.setRegistrationPeriod(rs.getInt("registration_period"));
        
        Date registrationEndDate = rs.getDate("registration_end_date");
        if (registrationEndDate != null) {
            project.setRegistrationEndDate(registrationEndDate.toLocalDate());
        }
        
        Date earliestReviewDate = rs.getDate("earliest_review_date");
        if (earliestReviewDate != null) {
            project.setEarliestReviewDate(earliestReviewDate.toLocalDate());
        }
        
        Date expectedReviewDate = rs.getDate("expected_review_date");
        if (expectedReviewDate != null) {
            project.setExpectedReviewDate(expectedReviewDate.toLocalDate());
        }
        
        project.setRemark(rs.getString("remark"));
        
        return project;
    }
} 