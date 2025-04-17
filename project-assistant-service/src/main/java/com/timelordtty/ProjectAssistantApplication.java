package com.timelordtty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目管理小助手后端服务主应用类
 * 
 * @author TimeLordTTY
 */
@SpringBootApplication
@MapperScan("com.timelordtty.projectcalendar.mapper")
public class ProjectAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectAssistantApplication.class, args);
    }
} 