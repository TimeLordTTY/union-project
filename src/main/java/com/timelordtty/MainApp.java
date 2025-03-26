package com.timelordtty;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

/**
 * 多功能工具应用主类
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            AppLogger.info("应用程序启动中...");
            
            // 加载主界面FXML
            URL fxmlUrl = getClass().getResource("/fxml/MainView.fxml");
            if (fxmlUrl == null) {
                AppLogger.error("找不到FXML资源: /fxml/MainView.fxml");
                
                // 尝试另一个路径
                fxmlUrl = getClass().getResource("/MainView.fxml");
                if (fxmlUrl == null) {
                    showErrorAndExit("资源加载错误", "无法加载主界面FXML资源", "请确保应用程序安装正确，且资源文件未被损坏。");
                    return;
                }
                AppLogger.info("从备用路径加载FXML: /MainView.fxml");
            } else {
                AppLogger.info("成功加载FXML: " + fxmlUrl);
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            // 设置标题和场景
            primaryStage.setTitle("项目日历");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
            
            AppLogger.info("应用启动成功!");
        } catch (Exception e) {
            AppLogger.error("应用启动失败: " + e.getMessage(), e);
            showErrorAndExit("启动错误", "应用程序启动失败", "错误详情: " + e.getMessage());
        }
    }
    
    @Override
    public void init() throws Exception {
        super.init();
        initLogging();
    }
    
    /**
     * 初始化日志系统
     */
    private void initLogging() {
        try {
            // 确保日志目录存在
            ensureLogsDirectoryExists();
            
            // 检查日志配置文件
            checkLogConfiguration();
            
            // 初始化日志系统，设置更详细的日志级别
            System.setProperty("log.level", "DEBUG");
            AppLogger.init("MultiTools");
            AppLogger.info("日志系统初始化完成，版本: " + getAppVersion());
            
            // 记录应用配置信息
            AppLogger.info("应用工作目录: " + System.getProperty("user.dir"));
            AppLogger.info("Java版本: " + System.getProperty("java.version"));
            AppLogger.info("操作系统: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        } catch (Exception e) {
            System.err.println("初始化日志系统失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检查日志配置文件
     */
    private void checkLogConfiguration() {
        // 检查是否已经通过系统属性指定了日志配置
        String logConfigFile = System.getProperty("java.util.logging.config.file");
        if (logConfigFile != null && !logConfigFile.isEmpty()) {
            System.out.println("使用指定的日志配置文件: " + logConfigFile);
            return;
        }
        
        // 检查conf目录下是否存在日志配置文件
        Path confPath = Paths.get("conf/logging.properties");
        if (Files.exists(confPath)) {
            System.setProperty("java.util.logging.config.file", confPath.toString());
            System.out.println("使用conf目录下的日志配置文件: " + confPath.toAbsolutePath());
            return;
        }
        
        // 检查当前目录下是否存在日志配置文件
        Path localPath = Paths.get("logging.properties");
        if (Files.exists(localPath)) {
            System.setProperty("java.util.logging.config.file", localPath.toString());
            System.out.println("使用当前目录下的日志配置文件: " + localPath.toAbsolutePath());
            return;
        }
        
        // 如果都不存在，尝试从classpath读取并创建一个临时配置
        try (InputStream is = getClass().getResourceAsStream("/logging.properties")) {
            if (is != null) {
                // 将配置复制到conf目录
                Path targetDir = Paths.get("conf");
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                
                Path targetFile = targetDir.resolve("logging.properties");
                Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
                
                System.setProperty("java.util.logging.config.file", targetFile.toString());
                System.out.println("从classpath复制日志配置文件到: " + targetFile.toAbsolutePath());
            } else {
                System.out.println("未找到日志配置文件，使用默认配置");
            }
        } catch (IOException e) {
            System.err.println("复制日志配置文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取应用版本
     */
    private String getAppVersion() {
        String version = getClass().getPackage().getImplementationVersion();
        return version != null ? version : "1.0.0";
    }
    
    /**
     * 确保日志目录存在
     */
    private void ensureLogsDirectoryExists() {
        try {
            // 尝试从系统属性获取日志目录，默认为当前目录下的logs文件夹
            String logsDir = System.getProperty("log.dir", "logs");
            Path logPath = Paths.get(logsDir);
            
            if (!Files.exists(logPath)) {
                Files.createDirectories(logPath);
                System.out.println("创建日志目录: " + logPath.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("创建日志目录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 显示错误对话框并退出应用
     */
    private void showErrorAndExit(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 应用程序入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        launch(args);
    }
} 