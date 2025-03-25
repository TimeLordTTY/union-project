package com.timelordtty.utils;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * API日志记录工具类
 * 用于详细记录API请求和响应信息，支持跟踪ID和性能监控
 * 
 * @author tianyu.tang
 * @version 1.0
 */
public class ApiLogger {
    
    private static final Logger API_LOGGER = Logger.getLogger("com.timelordtty.api");
    
    // API类型常量
    public static final String API_TYPE_BAIDU = "BaiduAPI";
    public static final String API_TYPE_OTHER = "OtherAPI";
    
    /**
     * 记录API请求开始
     * 
     * @param apiType API类型
     * @param method 请求方法
     * @param url 请求URL
     * @param params 请求参数
     * @return 请求跟踪ID
     */
    public static String logRequestStart(String apiType, String method, String url, String params) {
        String traceId = generateTraceId();
        API_LOGGER.info(String.format("[%s] [请求开始] [TraceID: %s] [类型: %s] [%s %s]", 
                formatTime(), traceId, apiType, method, maskSensitiveInfo(url)));
        
        if (params != null && !params.isEmpty()) {
            API_LOGGER.fine(String.format("[%s] [请求参数] [TraceID: %s] %s", 
                    formatTime(), traceId, maskSensitiveInfo(params)));
        }
        
        return traceId;
    }
    
    /**
     * 记录API请求结束
     * 
     * @param traceId 请求跟踪ID
     * @param statusCode 响应状态码
     * @param response 响应内容
     * @param duration 请求耗时(毫秒)
     */
    public static void logRequestEnd(String traceId, int statusCode, String response, long duration) {
        boolean isSuccess = statusCode >= 200 && statusCode < 300;
        
        String statusType = isSuccess ? "成功" : "失败";
        Level logLevel = isSuccess ? Level.INFO : Level.WARNING;
        
        API_LOGGER.log(logLevel, String.format("[%s] [请求结束] [TraceID: %s] [状态: %s-%d] [耗时: %dms]", 
                formatTime(), traceId, statusType, statusCode, duration));
        
        if (response != null && !response.isEmpty()) {
            API_LOGGER.fine(String.format("[%s] [响应内容] [TraceID: %s] %s", 
                    formatTime(), traceId, response.length() > 500 ? response.substring(0, 500) + "..." : response));
        }
    }
    
    /**
     * 记录API错误
     * 
     * @param traceId 请求跟踪ID
     * @param message 错误信息
     * @param exception 异常对象
     */
    public static void logError(String traceId, String message, Throwable exception) {
        API_LOGGER.severe(String.format("[%s] [请求错误] [TraceID: %s] %s", 
                formatTime(), traceId, message));
        
        if (exception != null) {
            API_LOGGER.log(Level.SEVERE, String.format("[%s] [异常堆栈] [TraceID: %s]", 
                    formatTime(), traceId), exception);
        }
    }
    
    /**
     * 记录令牌相关操作
     * 
     * @param operation 操作类型
     * @param apiType API类型
     * @param details 详情
     */
    public static void logTokenOperation(String operation, String apiType, String details) {
        API_LOGGER.info(String.format("[%s] [令牌操作] [类型: %s] [%s] %s", 
                formatTime(), apiType, operation, details));
    }
    
    /**
     * 记录百度API调用信息
     * 
     * @param function API功能
     * @param inputLength 输入长度
     * @param outputLength 输出长度
     * @param duration 处理时间(毫秒)
     */
    public static void logBaiduApiUsage(String function, int inputLength, int outputLength, long duration) {
        API_LOGGER.info(String.format("[%s] [百度API] [功能: %s] [输入: %d字符] [输出: %d字符] [耗时: %dms]", 
                formatTime(), function, inputLength, outputLength, duration));
    }
    
    /**
     * 生成跟踪ID
     */
    private static String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 格式化当前时间
     */
    private static String formatTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }
    
    /**
     * 掩盖敏感信息
     */
    private static String maskSensitiveInfo(String text) {
        if (text == null) return "";
        
        // 掩盖访问令牌
        text = text.replaceAll("(access_token=)([^&\"]+)", "$1********");
        // 掩盖API密钥
        text = text.replaceAll("(api_key=|client_id=|client_secret=)([^&\"]+)", "$1********");
        // 掩盖其他可能的敏感内容
        text = text.replaceAll("(password=|pwd=|secret=)([^&\"]+)", "$1********");
        
        return text;
    }
    
    /**
     * 记录请求-响应周期(便捷方法)
     */
    public static void logApiCall(String apiType, String method, String url, String requestBody, 
                                 int statusCode, String responseBody, long duration) {
        String traceId = logRequestStart(apiType, method, url, requestBody);
        logRequestEnd(traceId, statusCode, responseBody, duration);
    }
} 