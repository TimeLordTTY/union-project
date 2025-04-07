package com.timelordtty.projectassistant.service;

import java.util.List;

import com.timelordtty.projectassistant.entity.Project;

/**
 * 项目服务接口
 */
public interface ProjectService {
    
    /**
     * 获取所有项目
     */
    List<Project> getAllProjects();
    
    /**
     * 根据ID获取项目
     */
    Project getProjectById(Long id);
    
    /**
     * 创建项目
     */
    Project createProject(Project project);
    
    /**
     * 更新项目
     */
    Project updateProject(Project project);
    
    /**
     * 删除项目
     */
    boolean deleteProject(Long id);
} 