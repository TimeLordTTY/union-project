package com.timelordtty.projectCalendar.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.timelordtty.AppLogger;

/**
 * 数据库管理工具类，负责管理数据库连接和初始化
 */
public class DatabaseManager {
    // 基本数据库连接配置
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    
    // 数据库文件名（不含路径和扩展名）
    private static final String DB_NAME = "projectCalendar";
    
    // 可能的数据库路径列表（按优先级排序）
    private static final String[] POSSIBLE_DB_PATHS = {
        "./data/" + DB_NAME,       // 应用根目录下的data文件夹
        "../data/" + DB_NAME,      // 应用上级目录下的data文件夹
        "../../data/" + DB_NAME,   // 应用上上级目录下的data文件夹
        "./PersonalApps/项目管理小助手/data/" + DB_NAME // 指定的项目管理小助手数据目录
    };
    
    // 当前使用的数据库URL
    private static String currentDbUrl = null;
    
    // 数据库结构定义
    private static final String CREATE_PROJECTS_TABLE = 
            "CREATE TABLE IF NOT EXISTS projects (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(255) NOT NULL, " +
            "review_period INTEGER NOT NULL, " +
            "online_date DATE, " +
            "registration_period INTEGER NOT NULL, " +
            "registration_end_date DATE, " +
            "earliest_review_date DATE, " +
            "expected_review_time TIMESTAMP, " +  // 开标时间，存储日期和时间
            "expert_review_time TIMESTAMP, " +    // 专家评审时间，存储日期和时间
            "remark TEXT)";
    
    // 数据库结构升级SQL语句
    private static final String ALTER_TABLE_ADD_EXPERT_REVIEW_TIME = 
            "ALTER TABLE projects ADD COLUMN IF NOT EXISTS expert_review_time TIMESTAMP";
    
    private static final String ALTER_TABLE_RENAME_EXPECTED_REVIEW_DATE = 
            "ALTER TABLE projects ALTER COLUMN expected_review_date RENAME TO expected_review_time";
    
    private static final String ALTER_TABLE_CHANGE_TYPE = 
            "ALTER TABLE projects ALTER COLUMN expected_review_time TIMESTAMP";
    
    /**
     * 私有构造函数，防止实例化
     */
    private DatabaseManager() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * 初始化数据库
     */
    public static void initialize() {
        try {
            // 加载H2数据库驱动
            Class.forName("org.h2.Driver");
            
            // 尝试获取数据库连接
            findValidDatabasePath();
            
            if (currentDbUrl == null) {
                AppLogger.error("找不到有效的数据库路径，将使用默认路径");
                currentDbUrl = "jdbc:h2:./data/" + DB_NAME + ";MODE=MySQL";
                
                // 确保data目录存在
                File dataDir = new File("./data");
                if (!dataDir.exists()) {
                    dataDir.mkdirs();
                    AppLogger.info("创建数据目录: " + dataDir.getAbsolutePath());
                }
            }
            
            // 获取连接并创建表
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // 创建项目表
                AppLogger.info("执行表创建SQL: " + CREATE_PROJECTS_TABLE);
                stmt.execute(CREATE_PROJECTS_TABLE);
                
                // 尝试进行表结构升级
                try {
                    // 检查列是否存在，不存在则添加
                    DatabaseMetaData meta = conn.getMetaData();
                    
                    // 检查专家评审时间字段
                    boolean hasExpertReviewTime = false;
                    ResultSet columns = meta.getColumns(null, null, "PROJECTS", "EXPERT_REVIEW_TIME");
                    if (columns.next()) {
                        hasExpertReviewTime = true;
                        AppLogger.info("表结构检查：专家评审时间字段已存在");
                    } else {
                        columns = meta.getColumns(null, null, "projects", "expert_review_time");
                        if (columns.next()) {
                            hasExpertReviewTime = true;
                            AppLogger.info("表结构检查：专家评审时间字段已存在（小写）");
                        }
                    }
                    
                    // 如果专家评审时间字段不存在，添加它
                    if (!hasExpertReviewTime) {
                        AppLogger.info("升级表结构：添加专家评审时间字段");
                        stmt.execute(ALTER_TABLE_ADD_EXPERT_REVIEW_TIME);
                    }
                    
                    // 检查开标时间字段类型
                    boolean needsTypeChange = false;
                    columns = meta.getColumns(null, null, "PROJECTS", "EXPECTED_REVIEW_TIME");
                    if (!columns.next()) {
                        columns = meta.getColumns(null, null, "projects", "expected_review_time");
                        if (!columns.next()) {
                            // 如果expected_review_time不存在，检查expected_review_date是否存在
                            columns = meta.getColumns(null, null, "PROJECTS", "EXPECTED_REVIEW_DATE");
                            if (columns.next()) {
                                needsTypeChange = true;
                                AppLogger.info("升级表结构：需要将expected_review_date重命名为expected_review_time");
                                try {
                                    stmt.execute(ALTER_TABLE_RENAME_EXPECTED_REVIEW_DATE);
                                    stmt.execute(ALTER_TABLE_CHANGE_TYPE);
                                    AppLogger.info("表结构升级成功：开标时间字段已升级");
                                } catch (SQLException e) {
                                    AppLogger.error("无法更改列类型: " + e.getMessage(), e);
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    AppLogger.error("表结构升级过程中发生错误: " + e.getMessage(), e);
                }
                
                // 检查表是否存在
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet rs = meta.getTables(null, null, "PROJECTS", null);
                if (rs.next()) {
                    AppLogger.info("数据库初始化成功：projects表已创建或已存在");
                } else {
                    rs = meta.getTables(null, null, "projects", null);
                    if (rs.next()) {
                        AppLogger.info("数据库初始化成功：projects表已创建或已存在（小写）");
                    } else {
                        // 如果表不存在，再次尝试创建
                        AppLogger.warning("未检测到表，尝试再次创建表");
                        stmt.execute("DROP TABLE IF EXISTS projects");
                        stmt.execute(CREATE_PROJECTS_TABLE);
                        
                        // 再次检查表是否存在
                        rs = meta.getTables(null, null, "PROJECTS", null);
                        if (rs.next() || meta.getTables(null, null, "projects", null).next()) {
                            AppLogger.info("表创建成功");
                        } else {
                            AppLogger.error("数据库初始化失败：未能创建projects表");
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            AppLogger.error("H2数据库驱动加载失败: " + e.getMessage(), e);
        } catch (SQLException e) {
            AppLogger.error("数据库初始化时发生SQL错误: " + e.getMessage(), e);
        } catch (Exception e) {
            AppLogger.error("数据库初始化时发生未知错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 查找有效的数据库路径
     */
    private static void findValidDatabasePath() {
        for (String path : POSSIBLE_DB_PATHS) {
            // 首先检查.mv.db文件是否存在
            File dbFile = new File(path + ".mv.db");
            if (dbFile.exists()) {
                // 找到数据库文件
                currentDbUrl = "jdbc:h2:" + path + ";MODE=MySQL";
                AppLogger.info("找到现有数据库文件: " + dbFile.getAbsolutePath());
                AppLogger.info("使用数据库URL: " + currentDbUrl);
                return;
            }
            
            // 检查目录是否存在（如果数据库文件不存在，但目录存在，也可以使用）
            File dir = new File(dbFile.getParent());
            if (dir.exists() && dir.isDirectory()) {
                currentDbUrl = "jdbc:h2:" + path + ";MODE=MySQL";
                AppLogger.info("找到可用数据目录: " + dir.getAbsolutePath());
                AppLogger.info("使用数据库URL: " + currentDbUrl);
                return;
            }
        }
        
        // 如果没有找到现有数据库或可用目录，记录日志
        AppLogger.warning("未找到现有数据库文件或有效目录，将尝试使用默认路径");
    }
    
    /**
     * 获取数据库连接
     * @return 数据库连接对象
     * @throws SQLException 如果获取连接失败
     */
    public static Connection getConnection() throws SQLException {
        // 如果尚未初始化URL，则进行初始化
        if (currentDbUrl == null) {
            findValidDatabasePath();
            
            if (currentDbUrl == null) {
                currentDbUrl = "jdbc:h2:./data/" + DB_NAME + ";MODE=MySQL";
                AppLogger.warning("获取连接时未找到有效数据库路径，使用默认路径: " + currentDbUrl);
            }
        }
        
        // 尝试建立连接
        AppLogger.info("尝试连接数据库: " + currentDbUrl);
        Connection connection = DriverManager.getConnection(currentDbUrl, DB_USER, DB_PASSWORD);
        return connection;
    }
    
    /**
     * 关闭数据库连接
     * @param connection 数据库连接对象
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                AppLogger.error("关闭数据库连接时发生错误: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 获取当前使用的数据库URL
     * @return 数据库URL
     */
    public static String getCurrentDbUrl() {
        return currentDbUrl;
    }
} 