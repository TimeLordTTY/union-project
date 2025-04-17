package com.timelordtty.corrector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 百度API配置类
 */
@Configuration
@ConfigurationProperties(prefix = "baidu.text-correction")
@Data
public class BaiduApiConfig {
    
    private String apiKey;
    private String secretKey;
    private String url;
    
} 