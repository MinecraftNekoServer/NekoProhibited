package neko.nekoProhibited;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger logger = Logger.getLogger("NekoProhibited");
    private Connection connection;
    private final NekoProhibited plugin;
    private String dbType;
    private String sqlitePath;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlDatabase;
    private String mysqlUsername;
    private String mysqlPassword;
    private boolean mysqlUseSSL;

    public DatabaseManager(NekoProhibited plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        // 从配置文件加载数据库设置
        dbType = plugin.getConfig().getString("database.type", "sqlite");
        
        // SQLite配置
        sqlitePath = plugin.getDataFolder().getAbsolutePath() + File.separator + 
                    plugin.getConfig().getString("database.sqlite.path", "db/prohibited.db");
        
        // MySQL配置
        mysqlHost = plugin.getConfig().getString("database.mysql.host", "localhost");
        mysqlPort = plugin.getConfig().getInt("database.mysql.port", 3306);
        mysqlDatabase = plugin.getConfig().getString("database.mysql.database", "neko_prohibited");
        mysqlUsername = plugin.getConfig().getString("database.mysql.username", "root");
        mysqlPassword = plugin.getConfig().getString("database.mysql.password", "password");
        mysqlUseSSL = plugin.getConfig().getBoolean("database.mysql.useSSL", false);
    }

    public void connect() {
        try {
            if ("mysql".equalsIgnoreCase(dbType)) {
                // MySQL连接
                String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=UTC", 
                                         mysqlHost, mysqlPort, mysqlDatabase, mysqlUseSSL);
                connection = DriverManager.getConnection(url, mysqlUsername, mysqlPassword);
                logger.info("成功连接到MySQL数据库: " + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase);
            } else {
                // SQLite连接（默认）
                // 确保数据库目录存在（仅SQLite需要）
                File dbDir = new File(plugin.getDataFolder(), "db");
                if (!dbDir.exists()) {
                    dbDir.mkdirs();
                }
                
                String url = "jdbc:sqlite:" + sqlitePath;
                connection = DriverManager.getConnection(url);
                logger.info("成功连接到SQLite数据库: " + sqlitePath);
            }
            
            // 初始化数据库表
            initializeTables();
        } catch (SQLException e) {
            logger.severe("连接数据库失败: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("数据库连接已关闭");
            }
        } catch (SQLException e) {
            logger.severe("关闭数据库连接时出错: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeTables() {
        try {
            // 创建违禁词表
            String createTableSQL = "CREATE TABLE IF NOT EXISTS prohibited_words (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "word TEXT NOT NULL UNIQUE, " +
                    "description TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

            // SQLite使用AUTOINCREMENT，MySQL使用AUTO_INCREMENT
            if ("sqlite".equalsIgnoreCase(dbType)) {
                createTableSQL = "CREATE TABLE IF NOT EXISTS prohibited_words (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "word TEXT NOT NULL UNIQUE, " +
                        "description TEXT, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            }

            try (PreparedStatement stmt = connection.prepareStatement(createTableSQL)) {
                stmt.execute();
            }
            
            logger.info("数据库表初始化完成");
        } catch (SQLException e) {
            logger.severe("初始化数据库表时出错: " + e.getMessage());
        }
    }
}