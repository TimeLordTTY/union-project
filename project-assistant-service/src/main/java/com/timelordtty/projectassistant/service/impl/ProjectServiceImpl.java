package com.timelordtty.projectassistant.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timelordtty.projectassistant.entity.Project;
import com.timelordtty.projectassistant.mapper.ProjectMapper;
import com.timelordtty.projectassistant.service.ProjectService;

/**
 * 项目服务实现类
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;
    
    @Override
    public List<Project> getAllProjects() {
        return projectMapper.findAll();
    }
    
    @Override
    public Project getProjectById(Long id) {
        return projectMapper.findById(id);
    }
    
    @Override
    @Transactional
    public Project createProject(Project project) {
        projectMapper.insert(project);
        return project;
    }
    
    @Override
    @Transactional
    public Project updateProject(Project project) {
        Project existingProject = projectMapper.findById(project.getId());
        if (existingProject == null) {
            return null;
        }
        projectMapper.update(project);
        return projectMapper.findById(project.getId());
    }
    
    @Override
    @Transactional
    public boolean deleteProject(Long id) {
        Project existingProject = projectMapper.findById(id);
        if (existingProject == null) {
            return false;
        }
        return projectMapper.deleteById(id) > 0;
    }
} 