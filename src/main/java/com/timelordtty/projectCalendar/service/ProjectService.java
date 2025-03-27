package com.timelordtty.projectCalendar.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.timelordtty.AppLogger;
import com.timelordtty.projectCalendar.Project;
import com.timelordtty.projectCalendar.dao.ProjectDao;
import com.timelordtty.projectCalendar.utils.DateCalculator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 项目服务类，处理项目业务逻辑
 * 使用单例模式
 */
public class ProjectService {
    
    private static ProjectService instance;
    private ProjectDao projectDao;
    private ObservableList<Project> projects;
    
    /**
     * 私有构造函数
     */
    private ProjectService() {
        projectDao = new ProjectDao();
        projects = FXCollections.observableArrayList();
        loadProjects();
    }
    
    /**
     * 获取单例实例
     * @return 项目服务实例
     */
    public static synchronized ProjectService getInstance() {
        if (instance == null) {
            instance = new ProjectService();
        }
        return instance;
    }
    
    /**
     * 加载所有项目
     */
    private void loadProjects() {
        try {
            AppLogger.info("加载项目数据");
            List<Project> loadedProjects = projectDao.getAllProjects();
            projects.clear();
            
            if (loadedProjects != null && !loadedProjects.isEmpty()) {
                projects.addAll(loadedProjects);
                AppLogger.info("成功加载 " + loadedProjects.size() + " 个项目");
            } else {
                AppLogger.info("没有找到项目数据或项目数据为空");
            }
        } catch (Exception e) {
            AppLogger.error("加载项目数据时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取所有项目
     * @return 项目Observable列表
     */
    public ObservableList<Project> getProjects() {
        return projects;
    }
    
    /**
     * 获取所有项目（列表形式）
     * @return 项目列表
     */
    public List<Project> getAllProjects() {
        try {
            List<Project> allProjects = projectDao.getAllProjects();
            if (allProjects == null || allProjects.isEmpty()) {
                AppLogger.info("获取所有项目：数据为空");
                return new ArrayList<>();
            }
            return allProjects;
        } catch (Exception e) {
            AppLogger.error("获取所有项目时发生异常: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据ID获取项目
     * @param id 项目ID
     * @return 项目，如果未找到则返回null
     */
    public Project getProjectById(Long id) {
        try {
            return projectDao.getProjectById(id);
        } catch (Exception e) {
            AppLogger.error("根据ID获取项目时发生异常: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 保存项目（新增或更新）
     * @param project 要保存的项目
     * @return 是否成功保存
     */
    public boolean saveProject(Project project) {
        try {
            boolean result;
            
            if (project.getId() == null) {
                // 新增项目
                AppLogger.info("保存新项目: " + project.getName());
                result = projectDao.insertProject(project);
            } else {
                // 更新项目
                AppLogger.info("更新项目: " + project.getName() + " (ID: " + project.getId() + ")");
                result = projectDao.updateProject(project);
            }
            
            // 如果保存成功，刷新项目列表
            if (result) {
                loadProjects();
            }
            
            return result;
        } catch (Exception e) {
            AppLogger.error("保存项目时发生异常: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 删除项目
     * @param project 要删除的项目
     * @return 是否成功删除
     */
    public boolean deleteProject(Project project) {
        try {
            if (project == null || project.getId() == null) {
                AppLogger.error("无法删除项目：项目为空或ID为空");
                return false;
            }
            
            AppLogger.info("删除项目: " + project.getName() + " (ID: " + project.getId() + ")");
            boolean result = projectDao.deleteProject(project.getId());
            
            // 如果删除成功，刷新项目列表
            if (result) {
                loadProjects();
            }
            
            return result;
        } catch (Exception e) {
            AppLogger.error("删除项目时发生异常: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 搜索项目
     * @param keyword 搜索关键词
     * @return 匹配的项目列表
     */
    public List<Project> searchProjects(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return new ArrayList<>(projects);
            }
            
            String lowerKeyword = keyword.toLowerCase();
            
            return projects.stream()
                .filter(project -> project.getName().toLowerCase().contains(lowerKeyword) || 
                                 (project.getRemark() != null && 
                                 project.getRemark().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
        } catch (Exception e) {
            AppLogger.error("搜索项目时发生异常: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取指定日期的所有项目
     * @param date 日期
     * @return 该日期的项目列表
     */
    public List<Project> getProjectsForDate(LocalDate date) {
        try {
            if (date == null) {
                return new ArrayList<>();
            }
            
            return projects.stream()
                .filter(project -> {
                    // 检查该日期是否是项目的任何关键日期
                    return (project.getOnlineDate() != null && date.equals(project.getOnlineDate())) ||
                           (project.getRegistrationEndDate() != null && date.equals(project.getRegistrationEndDate())) ||
                           (project.getEarliestReviewDate() != null && date.equals(project.getEarliestReviewDate())) ||
                           (project.getExpectedReviewDate() != null && date.equals(project.getExpectedReviewDate()));
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            AppLogger.error("获取指定日期的项目时发生异常: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取本周和下周需要提醒的项目
     * @return 需要提醒的项目列表
     */
    public List<Project> getThisWeekAndNextWeekProjects() {
        try {
        LocalDate today = LocalDate.now();
            LocalDate endOfNextWeek = today.plusDays(14); // 两周内
            
            return projects.stream()
                .filter(project -> {
                    // 检查项目的关键日期是否在本周或下周
                    boolean isRegistrationEndDateInRange = project.getRegistrationEndDate() != null && 
                                                         !project.getRegistrationEndDate().isBefore(today) && 
                                                         !project.getRegistrationEndDate().isAfter(endOfNextWeek);
                    
                    boolean isExpectedReviewDateInRange = project.getExpectedReviewDate() != null && 
                                                        !project.getExpectedReviewDate().isBefore(today) && 
                                                        !project.getExpectedReviewDate().isAfter(endOfNextWeek);
                    
                    return isRegistrationEndDateInRange || isExpectedReviewDateInRange;
                })
                .sorted(Comparator.comparing(p -> {
                    // 首先按照最近的关键日期排序
                    LocalDate date1 = p.getRegistrationEndDate();
                    LocalDate date2 = p.getExpectedReviewDate();
                    
                    if (date1 == null && date2 == null) {
                        return LocalDate.MAX;
                    } else if (date1 == null) {
                        return date2;
                    } else if (date2 == null) {
                        return date1;
                    } else {
                        return date1.isBefore(date2) ? date1 : date2;
                    }
                }))
                .collect(Collectors.toList());
        } catch (Exception e) {
            AppLogger.error("获取提醒项目时发生异常: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 判断项目是否在本周内
     * @param project 项目
     * @return 是否在本周内
     */
    public boolean isProjectInThisWeek(Project project) {
        if (project == null) {
            return false;
        }
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            
            // 检查项目的关键日期是否在本周内
            return (project.getRegistrationEndDate() != null && 
                   !project.getRegistrationEndDate().isBefore(startOfWeek) && 
                   !project.getRegistrationEndDate().isAfter(endOfWeek)) ||
                   (project.getExpectedReviewDate() != null && 
                   !project.getExpectedReviewDate().isBefore(startOfWeek) && 
                   !project.getExpectedReviewDate().isAfter(endOfWeek));
        } catch (Exception e) {
            AppLogger.error("判断项目是否在本周内时发生异常: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 判断项目是否在下周内
     * @param project 项目
     * @return 是否在下周内
     */
    public boolean isProjectInNextWeek(Project project) {
        if (project == null) {
            return false;
        }
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate startOfNextWeek = startOfWeek.plusDays(7);
            LocalDate endOfNextWeek = startOfNextWeek.plusDays(6);
            
            // 检查项目的关键日期是否在下周内
            return (project.getRegistrationEndDate() != null && 
                   !project.getRegistrationEndDate().isBefore(startOfNextWeek) && 
                   !project.getRegistrationEndDate().isAfter(endOfNextWeek)) ||
                   (project.getExpectedReviewDate() != null && 
                   !project.getExpectedReviewDate().isBefore(startOfNextWeek) && 
                   !project.getExpectedReviewDate().isAfter(endOfNextWeek));
        } catch (Exception e) {
            AppLogger.error("判断项目是否在下周内时发生异常: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 计算报名截止日期
     * @param onlineDate 上网日期
     * @param registrationPeriod 报名期限（工作日）
     * @return 报名截止日期
     */
    public LocalDate calculateRegistrationEndDate(LocalDate onlineDate, int registrationPeriod) {
        try {
            if (onlineDate == null || registrationPeriod <= 0) {
                return null;
            }
            
            return DateCalculator.addWorkingDays(onlineDate, registrationPeriod);
        } catch (Exception e) {
            AppLogger.error("计算报名截止日期时发生异常: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 计算最早评审日期
     * @param registrationEndDate 报名截止日期
     * @param daysAfterRegistration 报名结束后的天数（默认为1）
     * @return 最早评审日期
     */
    public LocalDate calculateEarliestReviewDate(LocalDate registrationEndDate, int daysAfterRegistration) {
        try {
            if (registrationEndDate == null) {
                return null;
            }
            
            // 报名结束后第一个工作日
            LocalDate earliestReviewDate = registrationEndDate.plusDays(daysAfterRegistration);
            
            // 确保最早评审日期不在周末
            DayOfWeek dayOfWeek = earliestReviewDate.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SATURDAY) {
                earliestReviewDate = earliestReviewDate.plusDays(2); // 跳过周末
            } else if (dayOfWeek == DayOfWeek.SUNDAY) {
                earliestReviewDate = earliestReviewDate.plusDays(1); // 跳过周日
            }
            
            return earliestReviewDate;
        } catch (Exception e) {
            AppLogger.error("计算最早评审日期时发生异常: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 计算预计评审日期
     * @param registrationEndDate 报名截止日期
     * @param reviewPeriod 评审周期（自然日）
     * @return 预计评审日期
     */
    public LocalDate calculateExpectedReviewDate(LocalDate registrationEndDate, int reviewPeriod) {
        try {
            if (registrationEndDate == null || reviewPeriod <= 0) {
                return null;
            }
            
            return registrationEndDate.plusDays(reviewPeriod);
        } catch (Exception e) {
            AppLogger.error("计算预计评审日期时发生异常: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 刷新项目数据
     */
    public void refreshProjects() {
        loadProjects();
    }
} 