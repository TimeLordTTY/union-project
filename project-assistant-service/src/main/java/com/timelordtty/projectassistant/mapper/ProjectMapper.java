package com.timelordtty.projectassistant.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.timelordtty.projectassistant.model.Project;

/**
 * 项目数据访问层接口
 * 
 * @author timelordtty
 */
@Mapper
public interface ProjectMapper {
    
    /**
     * 查询所有项目
     * 
     * @return 项目列表
     */
    List<Project> findAll();
    
    /**
     * 根据ID查询项目
     * 
     * @param id 项目ID
     * @return 项目对象
     */
    Project findById(@Param("id") Long id);
    
    /**
     * 根据名称模糊查询项目
     * 
     * @param name 项目名称关键字
     * @return 匹配的项目列表
     */
    List<Project> findByNameLike(@Param("name") String name);
    
    /**
     * 查询指定日期范围内的项目
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param startDateTime 开始日期时间
     * @param endDateTime 结束日期时间
     * @return 日期范围内的项目列表
     */
    List<Project> findByDateRange(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * 插入项目
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
    int deleteById(@Param("id") Long id);
} 