package com.timelordtty.projectcalendar.controller;

import com.timelordtty.projectcalendar.model.Project;
import com.timelordtty.projectcalendar.model.Status;
import com.timelordtty.projectcalendar.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 项目管理接口控制器
 */
@RestController
@RequestMapping("/projects")
@Slf4j
@CrossOrigin
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 获取所有项目
     * 
     * @return 项目列表
     */
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * 根据ID获取项目
     * 
     * @param id 项目ID
     * @return 项目对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        if (project != null) {
            return ResponseEntity.ok(project);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 根据状态获取项目
     * 
     * @param status 项目状态
     * @return 项目列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Project>> getProjectsByStatus(@PathVariable Status status) {
        List<Project> projects = projectService.getProjectsByStatus(status);
        return ResponseEntity.ok(projects);
    }

    /**
     * 搜索项目
     * 
     * @param keyword 项目名称关键字
     * @return 项目列表
     */
    @GetMapping("/search")
    public ResponseEntity<List<Project>> searchProjects(@RequestParam String keyword) {
        List<Project> projects = projectService.searchProjects(keyword);
        return ResponseEntity.ok(projects);
    }

    /**
     * 根据日期范围获取项目
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 项目列表
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Project>> getProjectsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Project> projects = projectService.getProjectsInDateRange(startDate, endDate);
        return ResponseEntity.ok(projects);
    }

    /**
     * 创建新项目
     * 
     * @param project 项目对象
     * @return 创建后的项目
     */
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project savedProject = projectService.saveOrUpdateProject(project);
        return new ResponseEntity<>(savedProject, HttpStatus.CREATED);
    }

    /**
     * 更新项目
     * 
     * @param id 项目ID
     * @param project 项目对象
     * @return 更新后的项目
     */
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        Project existingProject = projectService.getProjectById(id);
        if (existingProject == null) {
            return ResponseEntity.notFound().build();
        }
        
        project.setId(id);
        Project updatedProject = projectService.saveOrUpdateProject(project);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * 删除项目
     * 
     * @param id 项目ID
     * @return 响应状态
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        Project existingProject = projectService.getProjectById(id);
        if (existingProject == null) {
            return ResponseEntity.notFound().build();
        }
        
        boolean deleted = projectService.deleteProject(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
} 