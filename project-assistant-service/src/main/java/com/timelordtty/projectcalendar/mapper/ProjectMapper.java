package com.timelordtty.projectcalendar.mapper;

import com.timelordtty.projectcalendar.model.Project;
import com.timelordtty.projectcalendar.model.Status;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 项目数据访问接口
 */
@Mapper
public interface ProjectMapper {
    
    /**
     * 获取所有项目
     * 
     * @return 项目列表
     */
    List<Project> selectAll();
    
    /**
     * 根据ID获取项目
     * 
     * @param id 项目ID
     * @return 项目对象
     */
    Project selectById(Long id);
    
    /**
     * 根据状态获取项目
     * 
     * @param status 项目状态
     * @return 项目列表
     */
    List<Project> selectByStatus(Status status);
    
    /**
     * 根据名称模糊查询项目
     * 
     * @param name 项目名称关键字
     * @return 项目列表
     */
    List<Project> selectByNameLike(String name);
    
    /**
     * 根据日期范围查询项目
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 项目列表
     */
    List<Project> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 插入新项目
     * 
     * @param project 项目对象
     * @return 受影响的行数
     */
    int insert(Project project);
    
    /**
     * 更新项目
     * 
     * @param project 项目对象
     * @return 受影响的行数
     */
    int update(Project project);
    
    /**
     * 删除项目
     * 
     * @param id 项目ID
     * @return 受影响的行数
     */
    int deleteById(Long id);
    
    /**
     * 创建项目表
     */
    void createTable();
} 