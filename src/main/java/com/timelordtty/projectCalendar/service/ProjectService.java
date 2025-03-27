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
                    boolean isOnlineDate = project.getOnlineDate() != null && date.equals(project.getOnlineDate());
                    boolean isRegistrationEndDate = project.getRegistrationEndDate() != null && date.equals(project.getRegistrationEndDate());
                    boolean isEarliestReviewDate = project.getEarliestReviewDate() != null && date.equals(project.getEarliestReviewDate());
                    
                    // 检查专家评审时间
                    boolean isExpertReviewDate = project.getExpertReviewTime() != null && 
                                                date.equals(project.getExpertReviewTime().toLocalDate());
                    
                    // 检查开标时间
                    boolean isExpectedReviewDate = project.getExpectedReviewTime() != null && 
                                                 date.equals(project.getExpectedReviewTime().toLocalDate());
                    
                    return isOnlineDate || isRegistrationEndDate || isEarliestReviewDate || 
                           isExpertReviewDate || isExpectedReviewDate;
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
            LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            LocalDate startOfNextWeek = endOfWeek.plusDays(1);
            LocalDate endOfNextWeek = startOfNextWeek.plusDays(6);
            
            // 根据今天是星期几，决定要获取的日期范围
            LocalDate startDate = today; // 从今天开始
            LocalDate endDate;
            
            DayOfWeek dayOfWeek = today.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                // 如果今天是周五、周六或周日，提醒到下周末
                endDate = endOfNextWeek;
            } else {
                // 如果是周一到周四，只提醒到本周末
                endDate = endOfWeek;
            }
            
            // 筛选项目：只有报名截止日期、开标时间和专家评审时间在范围内的项目
            return projects.stream()
                .filter(project -> {
                    // 检查项目的报名截止日期是否在范围内
                    boolean isRegistrationEndDateInRange = project.getRegistrationEndDate() != null && 
                                                         !project.getRegistrationEndDate().isBefore(startDate) && 
                                                         !project.getRegistrationEndDate().isAfter(endDate);
                    
                    // 检查项目的专家评审时间是否在范围内
                    boolean isExpertReviewDateInRange = project.getExpertReviewTime() != null && 
                                                       !project.getExpertReviewTime().toLocalDate().isBefore(startDate) && 
                                                       !project.getExpertReviewTime().toLocalDate().isAfter(endDate);
                    
                    // 检查项目的开标时间是否在范围内
                    boolean isExpectedReviewDateInRange = project.getExpectedReviewTime() != null && 
                                                        !project.getExpectedReviewTime().toLocalDate().isBefore(startDate) && 
                                                        !project.getExpectedReviewTime().toLocalDate().isAfter(endDate);
                    
                    return isRegistrationEndDateInRange || isExpertReviewDateInRange || isExpectedReviewDateInRange;
                })
                .sorted(Comparator.comparing(p -> {
                    // 首先按照最近的关键日期排序（报名截止日期、专家评审时间、开标时间）
                    LocalDate date1 = p.getRegistrationEndDate();
                    LocalDate date2 = p.getExpertReviewTime() != null ? p.getExpertReviewTime().toLocalDate() : null;
                    LocalDate date3 = p.getExpectedReviewTime() != null ? p.getExpectedReviewTime().toLocalDate() : null;
                    
                    LocalDate earliestDate = null;
                    
                    // 找出最早的有效日期
                    if (date1 != null) {
                        earliestDate = date1;
                    }
                    
                    if (date2 != null && (earliestDate == null || date2.isBefore(earliestDate))) {
                        earliestDate = date2;
                    }
                    
                    if (date3 != null && (earliestDate == null || date3.isBefore(earliestDate))) {
                        earliestDate = date3;
                    }
                    
                    return earliestDate != null ? earliestDate : LocalDate.MAX;
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
        
            // 检查项目的报名截止日期、专家评审时间或开标时间是否在本周内
            return (project.getRegistrationEndDate() != null && 
                   !project.getRegistrationEndDate().isBefore(startOfWeek) && 
                   !project.getRegistrationEndDate().isAfter(endOfWeek)) ||
                  (project.getExpertReviewDate() != null && 
                   !project.getExpertReviewDate().isBefore(startOfWeek) && 
                   !project.getExpertReviewDate().isAfter(endOfWeek)) ||
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
        
            // 检查项目的报名截止日期、专家评审时间或开标时间是否在下周内
            return (project.getRegistrationEndDate() != null && 
                   !project.getRegistrationEndDate().isBefore(startOfNextWeek) && 
                   !project.getRegistrationEndDate().isAfter(endOfNextWeek)) ||
                  (project.getExpertReviewDate() != null && 
                   !project.getExpertReviewDate().isBefore(startOfNextWeek) && 
                   !project.getExpertReviewDate().isAfter(endOfNextWeek)) ||
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
    
    /**
     * 获取指定日期范围内有关键日期的项目
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 项目列表
     */
    public List<Project> getProjectsWithDatesInRange(LocalDate startDate, LocalDate endDate) {
        try {
            List<Project> allProjects = getAllProjects();
            List<Project> projectsInRange = new ArrayList<>();
            
            for (Project project : allProjects) {
                // 检查项目的各个关键日期是否在指定范围内
                boolean isInRange = false;
                
                // 检查报名截止日期
                if (project.getRegistrationEndDate() != null) {
                    if (!project.getRegistrationEndDate().isBefore(startDate) && 
                        !project.getRegistrationEndDate().isAfter(endDate)) {
                        isInRange = true;
                    }
                }
                
                // 检查开标时间
                if (!isInRange && project.getExpectedReviewTime() != null) {
                    LocalDate expReviewDate = project.getExpectedReviewTime().toLocalDate();
                    if (!expReviewDate.isBefore(startDate) && 
                        !expReviewDate.isAfter(endDate)) {
                        isInRange = true;
                    }
                }
                
                // 检查专家评审时间
                if (!isInRange && project.getExpertReviewTime() != null) {
                    LocalDate expertReviewDate = project.getExpertReviewTime().toLocalDate();
                    if (!expertReviewDate.isBefore(startDate) && 
                        !expertReviewDate.isAfter(endDate)) {
                        isInRange = true;
                    }
                }
                
                // 如果有任一关键日期在范围内，添加到结果列表
                if (isInRange) {
                    projectsInRange.add(project);
                }
            }
            
            return projectsInRange;
        } catch (Exception e) {
            AppLogger.error("获取日期范围内项目失败: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
} 