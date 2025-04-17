package com.timelordtty.projectcalendar.service.impl;

import com.timelordtty.projectcalendar.mapper.ProjectMapper;
import com.timelordtty.projectcalendar.model.Project;
import com.timelordtty.projectcalendar.model.Status;
import com.timelordtty.projectcalendar.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;

/**
 * 项目服务实现类
 */
@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    @Autowired
    public ProjectServiceImpl(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @PostConstruct
    public void init() {
        initDatabase();
    }

    @Override
    public List<Project> getAllProjects() {
        return projectMapper.selectAll();
    }

    @Override
    public Project getProjectById(Long id) {
        return projectMapper.selectById(id);
    }

    @Override
    public List<Project> getProjectsByStatus(Status status) {
        return projectMapper.selectByStatus(status);
    }

    @Override
    public List<Project> searchProjects(String keyword) {
        return projectMapper.selectByNameLike(keyword);
    }

    @Override
    public List<Project> getProjectsInDateRange(LocalDate startDate, LocalDate endDate) {
        return projectMapper.selectByDateRange(startDate, endDate);
    }

    @Override
    @Transactional
    public Project saveOrUpdateProject(Project project) {
        if (project.getId() == null) {
            projectMapper.insert(project);
            log.info("新建项目: {}", project.getName());
        } else {
            projectMapper.update(project);
            log.info("更新项目: {}", project.getName());
        }
        return project;
    }

    @Override
    @Transactional
    public boolean deleteProject(Long id) {
        int result = projectMapper.deleteById(id);
        if (result > 0) {
            log.info("删除项目ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void initDatabase() {
        try {
            projectMapper.createTable();
            log.info("初始化数据库表结构完成");
        } catch (Exception e) {
            log.error("初始化数据库表结构失败: {}", e.getMessage(), e);
        }
    }
} 