package com.timelordtty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 应用程序日志工具类
 * 
 * 注意：此类已废弃，实际使用SLF4J与Logback进行日志记录。
 * 保留此类是为了向后兼容，方法会被正确路由到Logback。
 * 新的日志路径为：service_data/logs
 */
public class AppLogger {
    
    private static final Logger LOGGER = Logger.getLogger("com.timelordtty");
    // 已不再使用此路径，实际日志路径由logback.xml中的配置决定
    // 路径已更新为：service_data/logs
    private static final String LOG_FOLDER = "service_data/logs";
    private static FileHandler fileHandler;
    private static boolean initialized = false;
    private static final AtomicLong TRACK_SEQ = new AtomicLong(0);
    
    static {
        initLogger();
    }
    
    /**
     * 初始化日志系统，设置应用程序名称
     * 注意：此方法已不再是主要初始化方法，日志初始化由Logback完成
     * @param appName 应用程序名称
     */
    public static synchronized void init(String appName) {
        if (!initialized) {
            initLogger();
        } else {
            info("日志系统已经初始化，应用程序名称: " + appName);
        }
    }
    
    /**
     * 初始化日志系统
     * 注意：此方法已不再是主要初始化方法，日志初始化由Logback完成
     */
    private static void initLogger() {
        if (initialized) {
            return;
        }
        
        try {
            // 创建日志目录 - 注意使用正确的路径
            Path logDir = Paths.get(LOG_FOLDER);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            
            // 日志格式化器
            Formatter formatter = new Formatter() {
                @Override
                public String format(LogRecord record) {
                    LocalDateTime datetime = LocalDateTime.now();
                    String timestamp = datetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                    return String.format("[%s] [%s] %s: %s%n", 
                                        timestamp, 
                                        record.getLevel().getName(), 
                                        record.getLoggerName(), 
                                        record.getMessage());
                }
            };
            
            // 配置控制台处理器
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(formatter);
            consoleHandler.setLevel(Level.ALL);
            
            // 移除默认处理器
            for (Handler handler : LOGGER.getHandlers()) {
                LOGGER.removeHandler(handler);
            }
            
            // 添加自定义处理器
            LOGGER.addHandler(consoleHandler);
            
            // 当前日期作为日志文件名（使用统一的日志路径）
            String logFileName = LOG_FOLDER + File.separator + 
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";
            
            // 配置文件处理器
            fileHandler = new FileHandler(logFileName, true);
            fileHandler.setFormatter(formatter);
            fileHandler.setLevel(Level.ALL);
            LOGGER.addHandler(fileHandler);
            
            // 不使用父日志器的处理器
            LOGGER.setUseParentHandlers(false);
            LOGGER.setLevel(Level.ALL);
            
            initialized = true;
            info("日志系统初始化完成，日志文件: " + logFileName);
        } catch (IOException e) {
            System.err.println("初始化日志系统失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 记录信息级别的日志
     * @param message 日志消息
     */
    public static void info(String message) {
        LOGGER.info(message);
    }
    
    /**
     * 记录警告级别的日志
     * @param message 日志消息
     */
    public static void warn(String message) {
        LOGGER.warning(message);
    }
    
    /**
     * 记录警告级别的日志
     * @param message 日志消息
     */
    public static void warning(String message) {
        LOGGER.warning(message);
    }
    
    /**
     * 记录调试级别的日志
     * @param message 日志消息
     */
    public static void debug(String message) {
        LOGGER.fine(message);
    }
    
    /**
     * 记录错误级别的日志
     * @param message 日志消息
     */
    public static void error(String message) {
        LOGGER.severe(message);
    }
    
    /**
     * 记录错误级别的日志，包含异常
     * @param message 日志消息
     * @param throwable 异常
     */
    public static void error(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }
    
    /**
     * 设置跟踪ID
     * @return 生成的跟踪ID
     */
    public static String setTrackingId() {
        return "TID-" + System.currentTimeMillis() + "-" + TRACK_SEQ.incrementAndGet();
    }
    
    /**
     * 清除跟踪ID
     */
    public static void clearTrackingId() {
        // 由于我们没有使用ThreadLocal来存储跟踪ID，这个方法只是作为占位符
    }
    
    /**
     * 记录文本纠错开始的日志
     * @param textLength 文本长度
     */
    public static void textCorrectionStart(int textLength) {
        info("开始文本纠错处理 - 输入文本长度: " + textLength + " 字符");
    }
    
    /**
     * 记录文档生成的日志
     * @param docType 文档类型
     * @param templatePath 模板路径
     * @param outputPath 输出路径
     */
    public static void documentGeneration(String docType, String templatePath, String outputPath) {
        info("生成" + docType + "文档 - 模板: " + templatePath + " - 输出: " + outputPath);
    }
    
    /**
     * 记录文档生成错误的日志
     * @param docType 文档类型
     * @param step 步骤
     * @param error 错误信息
     * @param t 异常
     */
    public static void documentError(String docType, String step, String error, Exception t) {
        if (t != null) {
            error("文档生成错误 - 类型: " + docType + " - 步骤: " + step + " - 错误: " + error, t);
        } else {
            error("文档生成错误 - 类型: " + docType + " - 步骤: " + step + " - 错误: " + error);
        }
    }
    
    /**
     * 记录API错误的日志
     * @param api API名称
     * @param errorMessage 错误信息
     * @param t 异常(可选)
     */
    public static void apiError(String api, String errorMessage, Throwable t) {
        if (t != null) {
            error("API错误: " + api + " - " + errorMessage, t);
        } else {
            error("API错误: " + api + " - " + errorMessage);
        }
    }
    
    /**
     * 记录金额转换信息的日志
     * @param input 输入金额
     * @param output 输出金额
     * @param isNumToText 是否为数字转文本
     */
    public static void amountConversion(String input, String output, boolean isNumToText) {
        String direction = isNumToText ? "数字→大写" : "大写→数字";
        info("金额转换: " + direction + " - 输入: '" + input + "' - 输出: '" + output + "'");
    }
    
    /**
     * 记录金额转换错误的日志
     * @param input 输入金额
     * @param error 错误信息
     * @param isNumToText 是否为数字转文本
     */
    public static void amountError(String input, String error, boolean isNumToText) {
        String direction = isNumToText ? "数字→大写" : "大写→数字";
        error("金额转换失败: " + direction + " - 输入: '" + input + "' - 错误: " + error);
    }
    
    /**
     * 关闭日志系统
     */
    public static void close() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }
} 