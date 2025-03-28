package com.timelordtty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * 多功能工具应用主类
 */
public class MainApp extends Application {

    private MainController mainController;

    @Override
    public void start(Stage primaryStage) {
        try {
            AppLogger.info("启动应用程序");
            
            // 加载主界面
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();
            
            // 获取主控制器引用
            mainController = loader.getController();
            
            // 创建场景
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // 配置主舞台
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setTitle("项目管理小助手");
            
            // 设置主控制器的主舞台引用
            mainController.setMainStage(primaryStage);
            
            // 设置应用图标（若有）
            try {
                // 尝试从资源路径加载
                InputStream iconStream = getClass().getResourceAsStream("/images/app_icon.png");
                if (iconStream != null) {
                    primaryStage.getIcons().add(new Image(iconStream));
                    AppLogger.info("成功加载应用图标");
                } else {
                    // 如果图标不存在，则创建一个简单的内存中图标
                    AppLogger.info("图标文件不存在，使用程序生成的图标");
                    
                    // 创建一个简单的JavaFX内存图标（16x16像素的小图标）
                    javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(16, 16);
                    javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
                    
                    // 绘制日历图标
                    gc.setFill(javafx.scene.paint.Color.rgb(141, 110, 99)); // #8D6E63 褐色
                    gc.fillRect(0, 0, 16, 16);
                    
                    gc.setStroke(javafx.scene.paint.Color.rgb(93, 64, 55)); // #5D4037 深褐色
                    gc.strokeRect(0, 0, 16, 16);
                    
                    // 绘制日历顶部
                    gc.setFill(javafx.scene.paint.Color.rgb(93, 64, 55));
                    gc.fillRect(0, 0, 16, 4);
                    
                    // 绘制白色文本区域
                    gc.setFill(javafx.scene.paint.Color.WHITE);
                    gc.fillRect(2, 6, 12, 8);
                    
                    // 绘制日历格线
                    gc.setStroke(javafx.scene.paint.Color.LIGHTGRAY);
                    gc.strokeLine(5, 6, 5, 14);
                    gc.strokeLine(9, 6, 9, 14);
                    gc.strokeLine(2, 10, 14, 10);
                    
                    // 转换为JavaFX图像
                    javafx.scene.image.WritableImage image = new javafx.scene.image.WritableImage(16, 16);
                    canvas.snapshot(null, image);
                    
                    primaryStage.getIcons().add(image);
                }
            } catch (Exception e) {
                AppLogger.warn("未能加载应用图标: " + e.getMessage());
            }
            
            // 显示主窗口
            primaryStage.show();
            
            // 在应用程序完全启动后显示欢迎气泡
            // 使用一个更长的延迟，确保UI完全初始化
            Thread delayedWelcomeThread = new Thread(() -> {
                try {
                    // 等待2秒，确保UI完全加载
                    Thread.sleep(2000);
                    
                    // 在JavaFX线程中运行
                    Platform.runLater(() -> {
                        if (mainController != null) {
                            // 显示欢迎气泡
                            mainController.showWelcomeBubble();
                            
                            // 启动工作时长提醒定时器
                            mainController.startWorkTimeReminder();
                            
                            AppLogger.info("已启动欢迎气泡和工作时间提醒");
                        } else {
                            AppLogger.warning("无法获取主控制器，无法显示欢迎气泡");
                        }
                    });
                } catch (Exception e) {
                    AppLogger.error("显示欢迎气泡时发生异常: " + e.getMessage(), e);
                }
            });
            
            // 设置为守护线程，不阻止JVM退出
            delayedWelcomeThread.setDaemon(true);
            delayedWelcomeThread.start();
            
            AppLogger.info("应用程序启动完成");
        } catch (Exception e) {
            AppLogger.error("应用程序启动失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 应用初始化
     */
    @Override
    public void init() throws Exception {
        super.init();
        AppLogger.info("初始化应用程序");
        
        // 初始化数据库
        AppLogger.info("初始化数据库");
        com.timelordtty.projectCalendar.utils.DatabaseManager.initialize();
        
        // 在这里可以添加应用程序的初始化逻辑
        // 例如创建必要的目录、加载配置等
    }
    
    /**
     * 应用关闭时的清理工作
     */
    @Override
    public void stop() throws Exception {
        AppLogger.info("关闭应用程序");
        
        // 在这里可以添加应用程序关闭时的清理逻辑
        // 例如保存未保存的数据、释放资源等
        
        super.stop();
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