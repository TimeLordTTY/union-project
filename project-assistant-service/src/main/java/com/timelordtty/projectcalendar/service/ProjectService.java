package com.timelordtty.projectcalendar.service;

import com.timelordtty.projectcalendar.model.Project;
import com.timelordtty.projectcalendar.model.Status;

import java.time.LocalDate;
import java.util.List;

/**
 * 项目服务接口
 */
public interface ProjectService {
    
    /**
     * 获取所有项目
     * 
     * @return 项目列表
     */
    List<Project> getAllProjects();
    
    /**
     * 根据ID获取项目
     * 
     * @param id 项目ID
     * @return 项目对象
     */
    Project getProjectById(Long id);
    
    /**
     * 根据状态获取项目
     * 
     * @param status 项目状态
     * @return 项目列表
     */
    List<Project> getProjectsByStatus(Status status);
    
    /**
     * 搜索项目
     * 
     * @param keyword 项目名称关键字
     * @return 项目列表
     */
    List<Project> searchProjects(String keyword);
    
    /**
     * 获取日期范围内有关键日期的项目
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 项目列表
     */
    List<Project> getProjectsInDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 保存或更新项目
     * 
     * @param project 项目对象
     * @return 保存后的项目
     */
    Project saveOrUpdateProject(Project project);
    
    /**
     * 删除项目
     * 
     * @param id 项目ID
     * @return 是否删除成功
     */
    boolean deleteProject(Long id);
    
    /**
     * 初始化数据库
     */
    void initDatabase();
} 