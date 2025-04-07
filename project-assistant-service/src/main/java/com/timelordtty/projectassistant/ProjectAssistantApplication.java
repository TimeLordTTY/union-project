package com.timelordtty.projectassistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目管理小助手应用入口
 */
@SpringBootApplication
@MapperScan("com.timelordtty.projectassistant.mapper")
public class ProjectAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectAssistantApplication.class, args);
    }

} 