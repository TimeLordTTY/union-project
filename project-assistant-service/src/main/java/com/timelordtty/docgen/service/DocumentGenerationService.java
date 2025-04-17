package com.timelordtty.docgen.service;

import java.io.File;
import java.util.Map;

import com.timelordtty.docgen.model.DocumentGenerationResult;

/**
 * 文档生成服务接口
 */
public interface DocumentGenerationService {
    
    /**
     * 根据模板生成文档
     * 
     * @param templatePath 模板文件路径
     * @param outputPath 输出文件路径
     * @param data 数据参数
     * @return 文档生成结果
     */
    DocumentGenerationResult generateFromTemplate(String templatePath, String outputPath, Map<String, Object> data);
    
    /**
     * 根据模板文件生成文档
     * 
     * @param templateFile 模板文件
     * @param outputFile 输出文件
     * @param data 数据参数
     * @return 文档生成结果
     */
    DocumentGenerationResult generateFromTemplate(File templateFile, File outputFile, Map<String, Object> data);
} 