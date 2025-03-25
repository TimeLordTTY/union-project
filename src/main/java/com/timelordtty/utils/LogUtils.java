package com.timelordtty.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 日志工具类，用于记录应用程序运行过程中的各种日志信息
 * 
 * @author tianyu.tang
 * @version 1.0
 * @since 2023-03-25
 */
public class LogUtils {

    private static final String LOG_FOLDER = "logs";
    private static final String APP_LOGGER_NAME = "com.timelordtty";
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    
    // 应用根日志记录器
    private static final Logger APP_LOGGER = Logger.getLogger(APP_LOGGER_NAME);
    
    // 专用日志记录器
    private static final Logger API_LOGGER = Logger.getLogger(APP_LOGGER_NAME + ".api");
    private static final Logger AMOUNT_LOGGER = Logger.getLogger(APP_LOGGER_NAME + ".amount");
    private static final Logger DOC_LOGGER = Logger.getLogger(APP_LOGGER_NAME + ".document");
    private static final Logger TEXT_LOGGER = Logger.getLogger(APP_LOGGER_NAME + ".text");
    
    static {
        try {
            // 创建日志目录
            Path logDir = Paths.get(LOG_FOLDER);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            
            // 创建自定义格式化器
            CustomFormatter formatter = new CustomFormatter();
            
            // 创建并配置文件处理器 - 应用程序日志
            FileHandler appFileHandler = new FileHandler(LOG_FOLDER + File.separator + "app.log", true);
            appFileHandler.setFormatter(formatter);
            appFileHandler.setLevel(Level.INFO);
            APP_LOGGER.addHandler(appFileHandler);
            APP_LOGGER.setUseParentHandlers(false);
            
            // API日志
            FileHandler apiFileHandler = new FileHandler(LOG_FOLDER + File.separator + "api.log", true);
            apiFileHandler.setFormatter(formatter);
            apiFileHandler.setLevel(Level.ALL);
            API_LOGGER.addHandler(apiFileHandler);
            API_LOGGER.setUseParentHandlers(false);
            
            // 金额转换日志
            FileHandler amountFileHandler = new FileHandler(LOG_FOLDER + File.separator + "amount.log", true);
            amountFileHandler.setFormatter(formatter);
            amountFileHandler.setLevel(Level.INFO);
            AMOUNT_LOGGER.addHandler(amountFileHandler);
            AMOUNT_LOGGER.setUseParentHandlers(false);
            
            // 文档生成日志
            FileHandler docFileHandler = new FileHandler(LOG_FOLDER + File.separator + "document.log", true);
            docFileHandler.setFormatter(formatter);
            docFileHandler.setLevel(Level.INFO);
            DOC_LOGGER.addHandler(docFileHandler);
            DOC_LOGGER.setUseParentHandlers(false);
            
            // 文本纠错日志
            FileHandler textFileHandler = new FileHandler(LOG_FOLDER + File.separator + "text.log", true);
            textFileHandler.setFormatter(formatter);
            textFileHandler.setLevel(Level.ALL);
            TEXT_LOGGER.addHandler(textFileHandler);
            TEXT_LOGGER.setUseParentHandlers(false);
            
            logSystemInfo();
        } catch (IOException e) {
            System.err.println("初始化日志系统失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录系统信息
     */
    private static void logSystemInfo() {
        APP_LOGGER.info("=== 多功能工具集启动 ===");
        APP_LOGGER.info("操作系统: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        APP_LOGGER.info("Java版本: " + System.getProperty("java.version"));
        APP_LOGGER.info("用户目录: " + System.getProperty("user.dir"));
    }

    /**
     * 自定义日志格式化程序
     */
    private static class CustomFormatter extends Formatter {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            sb.append(dateFormat.format(new Date(record.getMillis())))
              .append(" [").append(record.getLevel()).append("] ")
              .append(record.getLoggerName()).append(": ")
              .append(formatMessage(record))
              .append(System.lineSeparator());
            
            if (record.getThrown() != null) {
                try {
                    sb.append("异常信息: ").append(System.lineSeparator());
                    for (StackTraceElement element : record.getThrown().getStackTrace()) {
                        sb.append("\tat ").append(element).append(System.lineSeparator());
                    }
                } catch (Exception ex) {
                    sb.append("无法获取异常堆栈: ").append(ex.getMessage()).append(System.lineSeparator());
                }
            }
            
            return sb.toString();
        }
    }
    
    // ======================== 通用日志方法 ========================
    
    public static void info(String message) {
        APP_LOGGER.info(message);
    }
    
    public static void warning(String message) {
        APP_LOGGER.warning(message);
    }
    
    public static void error(String message) {
        APP_LOGGER.severe(message);
    }
    
    public static void error(String message, Throwable t) {
        APP_LOGGER.log(Level.SEVERE, message, t);
    }
    
    public static void debug(String message) {
        APP_LOGGER.fine(message);
    }
    
    // ======================== API日志方法 ========================
    
    public static void apiRequest(String url, String method, String requestBody) {
        API_LOGGER.info("API请求: " + method + " " + url);
        API_LOGGER.fine("请求体: " + requestBody);
    }
    
    public static void apiResponse(String url, int statusCode, String responseBody) {
        API_LOGGER.info("API响应: " + url + " 状态码: " + statusCode);
        API_LOGGER.fine("响应体: " + responseBody);
    }
    
    public static void apiError(String url, String message, Throwable t) {
        API_LOGGER.log(Level.SEVERE, "API错误: " + url + " - " + message, t);
    }
    
    // ======================== 金额转换日志方法 ========================
    
    public static void logAmountConversion(String input, String output, boolean isNumToText) {
        String direction = isNumToText ? "数字→大写" : "大写→数字";
        AMOUNT_LOGGER.info("金额转换: " + direction + " 输入: '" + input + "' 输出: '" + output + "'");
    }
    
    public static void logAmountError(String input, String error, boolean isNumToText) {
        String direction = isNumToText ? "数字→大写" : "大写→数字";
        AMOUNT_LOGGER.warning("金额转换失败: " + direction + " 输入: '" + input + "' 错误: " + error);
    }
    
    // ======================== 文档生成日志方法 ========================
    
    public static void logDocumentGeneration(String templatePath, String dataPath, String outputPath, boolean isWord) {
        String docType = isWord ? "Word文档" : "Excel表格";
        DOC_LOGGER.info("文档生成: 类型: " + docType);
        DOC_LOGGER.info("模板路径: " + templatePath);
        DOC_LOGGER.info("数据路径: " + dataPath);
        DOC_LOGGER.info("输出路径: " + outputPath);
    }
    
    public static void logDocumentProgress(String step, String details) {
        DOC_LOGGER.info("文档生成进度: " + step + (details != null ? " - " + details : ""));
    }
    
    public static void logDocumentError(String step, String error, Throwable t) {
        if (t != null) {
            DOC_LOGGER.log(Level.SEVERE, "文档生成失败: " + step + " - " + error, t);
        } else {
            DOC_LOGGER.severe("文档生成失败: " + step + " - " + error);
        }
    }
    
    // ======================== 文本纠错日志方法 ========================
    
    public static void logTextCorrectionStart(String text) {
        TEXT_LOGGER.info("开始文本纠错, 输入文本长度: " + text.length() + " 字符");
        TEXT_LOGGER.fine("输入文本: '" + text + "'");
    }
    
    public static void logTextCorrectionResult(String originalText, String correctedText, int changesCount) {
        TEXT_LOGGER.info("文本纠错完成, 共有 " + changesCount + " 处变更");
        if (!originalText.equals(correctedText)) {
            TEXT_LOGGER.fine("原文本: '" + originalText + "'");
            TEXT_LOGGER.fine("纠正后: '" + correctedText + "'");
        } else {
            TEXT_LOGGER.fine("文本未发生变化");
        }
    }
    
    public static void logTextReplacement(String originalText, List<String> fromStrings, List<String> toStrings) {
        TEXT_LOGGER.info("文本替换, 共 " + fromStrings.size() + " 组替换规则");
        for (int i = 0; i < fromStrings.size(); i++) {
            TEXT_LOGGER.fine("替换规则 #" + (i+1) + ": '" + fromStrings.get(i) + "' → '" + toStrings.get(i) + "'");
        }
        TEXT_LOGGER.fine("原文本: '" + originalText + "'");
    }
    
    public static void logTextReplacementResult(String resultText, int totalReplacements) {
        TEXT_LOGGER.info("文本替换完成, 共替换 " + totalReplacements + " 处");
        TEXT_LOGGER.fine("结果文本: '" + resultText + "'");
    }
    
    public static void logBaiduApiTokenRequest() {
        TEXT_LOGGER.info("请求百度API访问令牌");
    }
    
    public static void logBaiduApiTokenResponse(String tokenInfo, long expireTime) {
        TEXT_LOGGER.info("获取访问令牌成功, 过期时间: " + new Date(expireTime));
        TEXT_LOGGER.fine("令牌信息: " + tokenInfo);
    }
    
    public static void logBaiduApiRequest(String text) {
        TEXT_LOGGER.info("发送百度文本纠错请求, 文本长度: " + text.length() + " 字符");
        TEXT_LOGGER.fine("请求文本: '" + text + "'");
    }
    
    public static void logBaiduApiResponse(String responseJson) {
        TEXT_LOGGER.info("接收百度文本纠错响应");
        TEXT_LOGGER.fine("响应内容: " + responseJson);
    }
} 