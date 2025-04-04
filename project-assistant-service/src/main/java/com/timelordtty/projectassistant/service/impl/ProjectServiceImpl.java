package com.timelordtty.projectassistant.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timelordtty.projectassistant.mapper.ProjectMapper;
import com.timelordtty.projectassistant.model.Project;
import com.timelordtty.projectassistant.service.ProjectService;

/**
 * 项目服务实现类
 * 
 * @author timelordtty
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
    public List<Project> searchProjectsByName(String keyword) {
        return projectMapper.findByNameLike(keyword);
    }

    @Override
    public List<Project> getProjectsByMonth(int year, int month) {
        // 计算月份的开始和结束日期
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // 转换为日期时间格式，用于查询
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return projectMapper.findByDateRange(startDate, endDate, startDateTime, endDateTime);
    }

    @Override
    @Transactional
    public Project createProject(Project project) {
        // 如果设置了上网日期和报名期限，计算报名截止日期
        if (project.getOnlineDate() != null && project.getRegistrationPeriod() != null) {
            LocalDate registrationEndDate = calculateRegistrationEndDate(
                    project.getOnlineDate(), 
                    project.getRegistrationPeriod());
            project.setRegistrationEndDate(registrationEndDate);
        }
        
        // 如果设置了报名截止日期和评审周期，计算最早评审日期
        if (project.getRegistrationEndDate() != null && project.getReviewPeriod() != null) {
            LocalDate earliestReviewDate = calculateEarliestReviewDate(
                    project.getRegistrationEndDate(), 
                    project.getReviewPeriod());
            project.setEarliestReviewDate(earliestReviewDate);
        }
        
        projectMapper.insert(project);
        return project;
    }

    @Override
    @Transactional
    public Project updateProject(Project project) {
        // 如果设置了上网日期和报名期限，计算报名截止日期
        if (project.getOnlineDate() != null && project.getRegistrationPeriod() != null) {
            LocalDate registrationEndDate = calculateRegistrationEndDate(
                    project.getOnlineDate(), 
                    project.getRegistrationPeriod());
            project.setRegistrationEndDate(registrationEndDate);
        }
        
        // 如果设置了报名截止日期和评审周期，计算最早评审日期
        if (project.getRegistrationEndDate() != null && project.getReviewPeriod() != null) {
            LocalDate earliestReviewDate = calculateEarliestReviewDate(
                    project.getRegistrationEndDate(), 
                    project.getReviewPeriod());
            project.setEarliestReviewDate(earliestReviewDate);
        }
        
        projectMapper.update(project);
        return project;
    }

    @Override
    @Transactional
    public boolean deleteProject(Long id) {
        return projectMapper.deleteById(id) > 0;
    }
    
    @Override
    public LocalDate calculateRegistrationEndDate(LocalDate onlineDate, int workingDays) {
        if (onlineDate == null || workingDays <= 0) {
            return null;
        }
        
        LocalDate currentDate = onlineDate;
        int remainingDays = workingDays;
        
        while (remainingDays > 0) {
            currentDate = currentDate.plusDays(1);
            
            // 跳过周末
            if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY && 
                currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                remainingDays--;
            }
        }
        
        return currentDate;
    }

    @Override
    public LocalDate calculateEarliestReviewDate(LocalDate registrationEndDate, int reviewPeriod) {
        if (registrationEndDate == null || reviewPeriod <= 0) {
            return null;
        }
        
        // 评审周期是自然日，直接添加
        return registrationEndDate.plusDays(reviewPeriod);
    }

    @Override
    public List<Project> getTodayProjects() {
        LocalDate today = LocalDate.now();
        List<Project> allProjects = projectMapper.findAll();
        List<Project> todayProjects = new ArrayList<>();
        
        for (Project project : allProjects) {
            // 上网日期是今天
            if (today.equals(project.getOnlineDate())) {
                todayProjects.add(project);
                continue;
            }
            
            // 报名截止日期是今天
            if (today.equals(project.getRegistrationEndDate())) {
                todayProjects.add(project);
                continue;
            }
            
            // 开标日期是今天
            if (project.getExpectedReviewTime() != null && 
                today.equals(project.getExpectedReviewTime().toLocalDate())) {
                todayProjects.add(project);
                continue;
            }
            
            // 专家评审日期是今天
            if (project.getExpertReviewTime() != null && 
                today.equals(project.getExpertReviewTime().toLocalDate())) {
                todayProjects.add(project);
                continue;
            }
        }
        
        return todayProjects;
    }
} 