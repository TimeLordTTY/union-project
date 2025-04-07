package com.timelordtty.projectassistant.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timelordtty.projectassistant.common.ApiResponse;
import com.timelordtty.projectassistant.entity.Project;
import com.timelordtty.projectassistant.service.ProjectService;

/**
 * 项目管理控制器
 */
@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;
    
    /**
     * 获取所有项目
     */
    @GetMapping
    public ApiResponse<List<Project>> getAllProjects() {
        return ApiResponse.success(projectService.getAllProjects());
    }
    
    /**
     * 根据ID获取项目
     */
    @GetMapping("/{id}")
    public ApiResponse<Project> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        if (project != null) {
            return ApiResponse.success(project);
        } else {
            return ApiResponse.error("项目不存在");
        }
    }
    
    /**
     * 创建项目
     */
    @PostMapping
    public ApiResponse<Project> createProject(@RequestBody Project project) {
        try {
            Project created = projectService.createProject(project);
            return ApiResponse.success(created);
        } catch (Exception e) {
            return ApiResponse.error("创建项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public ApiResponse<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        try {
            project.setId(id);
            Project updated = projectService.updateProject(project);
            if (updated != null) {
                return ApiResponse.success(updated);
            } else {
                return ApiResponse.error("项目不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("更新项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteProject(@PathVariable Long id) {
        try {
            boolean result = projectService.deleteProject(id);
            if (result) {
                return ApiResponse.success(true);
            } else {
                return ApiResponse.error("项目不存在或删除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("删除项目失败: " + e.getMessage());
        }
    }
} 