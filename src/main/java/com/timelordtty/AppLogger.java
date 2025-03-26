package com.timelordtty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.Level;

/**
 * 应用程序日志工具类
 * 简化版，使用SLF4J和Logback实现
 * 
 * @author tianyu.tang
 * @version 1.1
 */
public class AppLogger {
    
    // 不同业务模块的日志记录器
    private static final Logger APP_LOGGER = LoggerFactory.getLogger("com.timelordtty");
    private static final Logger API_LOGGER = LoggerFactory.getLogger("com.timelordtty.api");
    private static final Logger AMOUNT_LOGGER = LoggerFactory.getLogger("com.timelordtty.amount");
    private static final Logger TEXT_LOGGER = LoggerFactory.getLogger("com.timelordtty.text");
    private static final Logger DOC_LOGGER = LoggerFactory.getLogger("com.timelordtty.document");
    
    // MDC键值
    private static final String TRACE_ID = "traceId";
    private static final String MODULE = "module";
    
    // MDC常量
    private static final String MDC_TRACKING_ID = "trackingId";
    private static final String MDC_MODULE = "module";
    
    // 跟踪ID生成器
    private static final AtomicLong TRACK_SEQ = new AtomicLong(0);
    
    // 日志初始化标志
    private static boolean isInitialized = false;
    
    /**
     * 初始化日志系统
     * 
     * @param appName 应用名称
     */
    public static synchronized void init(String appName) {
        // 检查是否已初始化，防止重复操作
        if (isInitialized) {
            APP_LOGGER.info("日志系统已初始化，跳过重复操作");
            return;
        }
        
        try {
            // 尝试从系统属性获取日志级别，默认为INFO
            String logLevelProp = System.getProperty("log.level", "INFO");
            Level logLevel = Level.toLevel(logLevelProp, Level.INFO);
            
            // 尝试从系统属性获取日志目录，优先使用service_data/logs，其次再用当前目录下的logs文件夹
            String logsDir = System.getProperty("log.dir", "service_data/logs");
            
            // 创建日志目录
            ensureLogsDirectoryExists(logsDir);
            
            APP_LOGGER.info("初始化日志系统 - 应用: {} - 级别: {} - 目录: {}", 
                    appName, logLevel.toString(), new File(logsDir).getAbsolutePath());
            
            isInitialized = true;
        } catch (Exception e) {
            System.err.println("初始化日志系统失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 确保日志目录存在
     * 
     * @param logsDir 日志目录路径
     * @throws IOException 如果创建目录失败
     */
    private static void ensureLogsDirectoryExists(String logsDir) throws IOException {
        Path logPath = Paths.get(logsDir);
        if (!Files.exists(logPath)) {
            Files.createDirectories(logPath);
            APP_LOGGER.info("创建日志目录: {}", logPath.toAbsolutePath());
        }
    }
    
    /**
     * 获取当前类的日志记录器
     * 
     * @param clazz 类
     * @return 日志记录器
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * 设置跟踪ID
     * 
     * @param traceId 跟踪ID
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }
    
    /**
     * 清除跟踪ID
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID);
    }
    
    /**
     * 设置模块名称
     * 
     * @param moduleName 模块名称
     */
    public static void setModule(String moduleName) {
        MDC.put(MODULE, moduleName);
    }
    
    /**
     * 清除模块名称
     */
    public static void clearModule() {
        MDC.remove(MODULE);
    }
    
    /**
     * 设置跟踪ID，用于跟踪请求链路
     * 
     * @return 生成的跟踪ID
     */
    public static String setTrackingId() {
        String trackingId = generateTrackingId();
        MDC.put(MDC_TRACKING_ID, trackingId);
        return trackingId;
    }
    
    /**
     * 清除跟踪ID
     */
    public static void clearTrackingId() {
        MDC.remove(MDC_TRACKING_ID);
    }
    
    /**
     * 生成唯一的跟踪ID
     * 
     * @return 生成的跟踪ID
     */
    private static String generateTrackingId() {
        return String.format("TID-%d-%d", System.currentTimeMillis(), TRACK_SEQ.incrementAndGet());
    }
    
    // ==================== 通用日志方法 ====================
    
    /**
     * 记录普通信息
     * 
     * @param message 日志信息
     */
    public static void info(String message) {
        APP_LOGGER.info(message);
    }
    
    /**
     * 记录警告信息
     * 
     * @param message 日志信息
     */
    public static void warn(String message) {
        APP_LOGGER.warn(message);
    }
    
    /**
     * 记录警告信息（兼容旧代码）
     * 
     * @param message 日志信息
     */
    public static void warning(String message) {
        APP_LOGGER.warn(message);
    }
    
    /**
     * 记录错误信息
     * 
     * @param message 日志信息
     */
    public static void error(String message) {
        APP_LOGGER.error(message);
    }
    
    /**
     * 记录错误信息和异常
     * 
     * @param message 日志信息
     * @param t 异常
     */
    public static void error(String message, Throwable t) {
        APP_LOGGER.error(message, t);
    }
    
    /**
     * 记录调试信息
     * 
     * @param message 日志信息
     */
    public static void debug(String message) {
        APP_LOGGER.debug(message);
    }
    
    // ==================== API相关日志方法 ====================
    
    /**
     * 记录API请求信息
     * 
     * @param apiName API名称
     * @param method 请求方法
     * @param url 请求URL
     * @param params 请求参数(可选)
     */
    public static void apiRequest(String apiName, String method, String url, String params) {
        setModule("API");
        API_LOGGER.info("API请求: {} - {} {}", apiName, method, url);
        if (params != null && !params.isEmpty()) {
            API_LOGGER.debug("请求参数: {}", maskSensitiveInfo(params));
        }
        clearModule();
    }
    
    /**
     * 记录API响应信息
     * 
     * @param apiName API名称
     * @param statusCode 状态码
     * @param response 响应内容(可选)
     * @param duration 耗时(毫秒)
     */
    public static void apiResponse(String apiName, int statusCode, String response, long duration) {
        setModule("API");
        API_LOGGER.info("API响应: {} - 状态码: {} - 耗时: {}ms", apiName, statusCode, duration);
        if (response != null && !response.isEmpty()) {
            if (response.length() > 500) {
                API_LOGGER.debug("响应内容(部分): {}", response.substring(0, 500) + "...");
            } else {
                API_LOGGER.debug("响应内容: {}", response);
            }
        }
        clearModule();
    }
    
    /**
     * 记录API错误
     * 
     * @param apiName API名称
     * @param errorMessage 错误信息
     * @param t 异常(可选)
     */
    public static void apiError(String apiName, String errorMessage, Throwable t) {
        setModule("API");
        if (t != null) {
            API_LOGGER.error("API错误: {} - {}", apiName, errorMessage, t);
        } else {
            API_LOGGER.error("API错误: {} - {}", apiName, errorMessage);
        }
        clearModule();
    }
    
    // ==================== 金额转换相关日志方法 ====================
    
    /**
     * 记录金额转换信息
     * 
     * @param input 输入金额
     * @param output 输出金额
     * @param isNumToText 是否为数字转文本
     */
    public static void amountConversion(String input, String output, boolean isNumToText) {
        String direction = isNumToText ? "数字→大写" : "大写→数字";
        setModule("金额转换");
        AMOUNT_LOGGER.info("金额转换: {} - 输入: '{}' - 输出: '{}'", direction, input, output);
        clearModule();
    }
    
    /**
     * 记录金额转换错误
     * 
     * @param input 输入金额
     * @param error 错误信息
     * @param isNumToText 是否为数字转文本
     */
    public static void amountError(String input, String error, boolean isNumToText) {
        String direction = isNumToText ? "数字→大写" : "大写→数字";
        setModule("金额转换");
        AMOUNT_LOGGER.error("金额转换失败: {} - 输入: '{}' - 错误: {}", direction, input, error);
        clearModule();
    }
    
    // ==================== 文本纠错相关日志方法 ====================
    
    /**
     * 记录文本纠错开始
     * 
     * @param textLength 输入文本长度
     */
    public static void textCorrectionStart(int textLength) {
        setModule("文本纠错");
        TEXT_LOGGER.info("开始文本纠错处理 - 输入文本长度: {} 字符", textLength);
    }
    
    /**
     * 记录文本纠错结果
     * 
     * @param originalLength 原始文本长度
     * @param correctedLength 纠正后文本长度
     * @param changesCount 变更数量
     */
    public static void textCorrectionResult(int originalLength, int correctedLength, int changesCount) {
        setModule("文本纠错");
        TEXT_LOGGER.info("文本纠错完成 - 原文本长度: {} - 纠正后长度: {} - 变更数量: {}",
                originalLength, correctedLength, changesCount);
        clearModule();
    }
    
    // ==================== 文档生成相关日志方法 ====================
    
    /**
     * 记录文档生成信息
     * 
     * @param docType 文档类型(word,excel等)
     * @param templatePath 模板路径
     * @param outputPath 输出路径
     */
    public static void documentGeneration(String docType, String templatePath, String outputPath) {
        setModule("文档生成");
        DOC_LOGGER.info("生成{}文档 - 模板: {} - 输出: {}", docType, templatePath, outputPath);
        clearModule();
    }
    
    /**
     * 记录文档生成错误
     * 
     * @param docType 文档类型(word,excel等)
     * @param step 步骤
     * @param error 错误信息
     * @param t 异常(可选)
     */
    public static void documentError(String docType, String step, String error, Throwable t) {
        setModule("文档生成");
        if (t != null) {
            DOC_LOGGER.error("{}文档生成错误 - 步骤: {} - 错误: {}", docType, step, error, t);
        } else {
            DOC_LOGGER.error("{}文档生成错误 - 步骤: {} - 错误: {}", docType, step, error);
        }
        clearModule();
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 对敏感信息进行掩码处理
     * 
     * @param text 包含敏感信息的文本
     * @return 掩码处理后的文本
     */
    private static String maskSensitiveInfo(String text) {
        // 简单的隐私数据掩码示例，可根据需求扩展
        if (text == null) return null;
        
        // 隐藏API密钥
        text = text.replaceAll("(\"api[_-]?key\"\\s*:\\s*\")(.*?)(\")", "$1********$3");
        text = text.replaceAll("(\"client[_-]?secret\"\\s*:\\s*\")(.*?)(\")", "$1********$3");
        
        // 隐藏密码
        text = text.replaceAll("(\"password\"\\s*:\\s*\")(.*?)(\")", "$1********$3");
        
        return text;
    }
} 