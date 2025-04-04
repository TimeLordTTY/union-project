package com.timelordtty.projectassistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 项目管理小助手应用程序入口类
 * 
 * @author timelordtty
 */
@SpringBootApplication
@MapperScan("com.timelordtty.projectassistant.mapper")
@EnableScheduling
public class ProjectAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectAssistantApplication.class, args);
    }
} 