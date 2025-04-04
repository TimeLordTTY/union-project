package com.timelordtty.projectassistant.service;

import java.time.LocalDate;
import java.util.List;

import com.timelordtty.projectassistant.model.Project;

/**
 * 项目服务接口
 * 
 * @author timelordtty
 */
public interface ProjectService {
    
    /**
     * 获取所有项目
     * 
     * @return 项目列表
     */
    List<Project> getAllProjects();
    
    /**
     * 根据ID查询项目
     * 
     * @param id 项目ID
     * @return 项目对象
     */
    Project getProjectById(Long id);
    
    /**
     * 根据名称关键字查询项目
     * 
     * @param keyword 关键字
     * @return 匹配的项目列表
     */
    List<Project> searchProjectsByName(String keyword);
    
    /**
     * 获取指定月份的项目
     * 
     * @param year 年份
     * @param month 月份（1-12）
     * @return 项目列表
     */
    List<Project> getProjectsByMonth(int year, int month);
    
    /**
     * 创建新项目
     * 
     * @param project 项目对象
     * @return 创建后的项目（含ID）
     */
    Project createProject(Project project);
    
    /**
     * 更新项目信息
     * 
     * @param project 项目对象
     * @return 更新后的项目
     */
    Project updateProject(Project project);
    
    /**
     * 删除项目
     * 
     * @param id 项目ID
     * @return 是否删除成功
     */
    boolean deleteProject(Long id);
    
    /**
     * 计算报名截止日期
     * 
     * @param onlineDate 上网日期
     * @param workingDays 工作日天数
     * @return 计算得到的截止日期
     */
    LocalDate calculateRegistrationEndDate(LocalDate onlineDate, int workingDays);
    
    /**
     * 计算最早评审日期
     * 
     * @param registrationEndDate 报名截止日期
     * @param reviewPeriod 评审周期（自然日）
     * @return 计算得到的评审日期
     */
    LocalDate calculateEarliestReviewDate(LocalDate registrationEndDate, int reviewPeriod);
    
    /**
     * 获取今日需处理的项目
     * 
     * @return 需要处理的项目列表
     */
    List<Project> getTodayProjects();
} 