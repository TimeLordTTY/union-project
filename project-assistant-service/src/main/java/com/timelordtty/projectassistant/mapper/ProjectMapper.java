package com.timelordtty.projectassistant.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.timelordtty.projectassistant.entity.Project;

/**
 * 项目数据访问接口
 */
@Mapper
public interface ProjectMapper {
    
    /**
     * 获取所有项目列表
     */
    List<Project> findAll();
    
    /**
     * 根据ID查询项目
     */
    Project findById(Long id);
    
    /**
     * 新增项目
     */
    int insert(Project project);
    
    /**
     * 更新项目
     */
    int update(Project project);
    
    /**
     * 删除项目
     */
    int deleteById(Long id);
} 