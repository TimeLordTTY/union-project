package com.timelordtty.projectassistant.entity;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

/**
 * 项目实体类
 */
@Data
public class Project {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private Date startDate;
    
    private Date endDate;
    
    private String manager;
    
    private String status;
    
    private BigDecimal budget;
    
    private Integer priority;
    
    private Date createdTime;
    
    private Date updatedTime;
} 