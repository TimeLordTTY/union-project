package com.timelordtty.docgen.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档生成结果模型类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentGenerationResult {
    
    private String outputFilePath;  // 输出文件路径
    private boolean success;        // 是否成功
    private String message;         // 消息
    private long processTime;       // 处理时间(毫秒)
    
    /**
     * 创建成功结果
     * 
     * @param outputFilePath 输出文件路径
     * @param processTime 处理时间
     * @return 生成结果
     */
    public static DocumentGenerationResult success(String outputFilePath, long processTime) {
        DocumentGenerationResult result = new DocumentGenerationResult();
        result.setOutputFilePath(outputFilePath);
        result.setSuccess(true);
        result.setMessage("文档生成成功");
        result.setProcessTime(processTime);
        return result;
    }
    
    /**
     * 创建失败结果
     * 
     * @param message 错误信息
     * @return 生成结果
     */
    public static DocumentGenerationResult error(String message) {
        DocumentGenerationResult result = new DocumentGenerationResult();
        result.setSuccess(false);
        result.setMessage(message);
        result.setProcessTime(0);
        return result;
    }
} 