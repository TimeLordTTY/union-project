package com.timelordtty.projectassistant.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timelordtty.projectassistant.model.Project;
import com.timelordtty.projectassistant.service.ProjectService;

/**
 * 项目管理控制器
 * 
 * @author timelordtty
 */
@RestController
@RequestMapping("/api/project")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    /**
     * 获取所有项目
     * 
     * @return 项目列表
     */
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }
    
    /**
     * 获取指定ID的项目
     * 
     * @param id 项目ID
     * @return 项目详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(project);
    }
    
    /**
     * 按名称搜索项目
     * 
     * @param keyword 关键字
     * @return 匹配的项目列表
     */
    @GetMapping("/search")
    public ResponseEntity<List<Project>> searchProjects(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(projectService.searchProjectsByName(keyword));
    }
    
    /**
     * 获取指定月份的项目
     * 
     * @param year 年份
     * @param month 月份（1-12）
     * @return 当月项目列表
     */
    @GetMapping("/month")
    public ResponseEntity<List<Project>> getProjectsByMonth(
            @RequestParam int year, 
            @RequestParam int month) {
        return ResponseEntity.ok(projectService.getProjectsByMonth(year, month));
    }
    
    /**
     * 创建新项目
     * 
     * @param project 项目信息
     * @return 创建后的项目
     */
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        // 处理日期时间字段
        processDateTimeFields(project);
        
        Project createdProject = projectService.createProject(project);
        return ResponseEntity.ok(createdProject);
    }
    
    /**
     * 更新项目
     * 
     * @param id 项目ID
     * @param project 更新信息
     * @return 更新后的项目
     */
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(
            @PathVariable Long id, 
            @RequestBody Project project) {
        // 确保ID一致
        project.setId(id);
        
        // 处理日期时间字段
        processDateTimeFields(project);
        
        Project updatedProject = projectService.updateProject(project);
        return ResponseEntity.ok(updatedProject);
    }
    
    /**
     * 删除项目
     * 
     * @param id 项目ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteProject(@PathVariable Long id) {
        boolean deleted = projectService.deleteProject(id);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", deleted);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取今日需要处理的项目
     * 
     * @return 今日项目列表
     */
    @GetMapping("/today")
    public ResponseEntity<List<Project>> getTodayProjects() {
        return ResponseEntity.ok(projectService.getTodayProjects());
    }
    
    /**
     * 计算日期
     * 
     * @param onlineDate 上网日期
     * @param registrationPeriod 报名期限（工作日）
     * @param reviewPeriod 评审周期（自然日）
     * @return 计算结果
     */
    @GetMapping("/calculate-dates")
    public ResponseEntity<Map<String, Object>> calculateDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onlineDate,
            @RequestParam(required = false) Integer registrationPeriod,
            @RequestParam(required = false) Integer reviewPeriod) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("onlineDate", onlineDate);
        
        // 计算报名截止日期
        if (registrationPeriod != null) {
            LocalDate registrationEndDate = projectService.calculateRegistrationEndDate(
                    onlineDate, registrationPeriod);
            result.put("registrationEndDate", registrationEndDate);
            
            // 如果提供了评审周期，计算最早评审日期
            if (reviewPeriod != null && registrationEndDate != null) {
                LocalDate earliestReviewDate = projectService.calculateEarliestReviewDate(
                        registrationEndDate, reviewPeriod);
                result.put("earliestReviewDate", earliestReviewDate);
            }
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 处理日期时间字段
     * 
     * @param project 项目对象
     */
    private void processDateTimeFields(Project project) {
        // 处理开标时间
        if (project.getExpectedReviewDate() != null && project.getExpectedReviewTimeOnly() != null) {
            LocalDateTime expectedReviewTime = LocalDateTime.of(
                    project.getExpectedReviewDate(), 
                    project.getExpectedReviewTimeOnly());
            project.setExpectedReviewTime(expectedReviewTime);
        }
        
        // 处理专家评审时间
        if (project.getExpertReviewDate() != null && project.getExpertReviewTimeOnly() != null) {
            LocalDateTime expertReviewTime = LocalDateTime.of(
                    project.getExpertReviewDate(), 
                    project.getExpertReviewTimeOnly());
            project.setExpertReviewTime(expertReviewTime);
        }
    }
} 