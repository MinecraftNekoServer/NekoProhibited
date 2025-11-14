package neko.nekoProhibited;

import org.bukkit.plugin.java.JavaPlugin;

public final class NekoProhibited extends JavaPlugin {
    private DatabaseManager databaseManager;
    private YAMLManager yamlManager;
    private ProhibitedWordManager prohibitedWordManager;
    private int keepAliveTaskId;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("NekoProhibited 插件正在启动...");
        
        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化数据库连接
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        
        // 初始化YAML文件管理器
        yamlManager = new YAMLManager(this);
        
        // 初始化违禁词管理器
        prohibitedWordManager = new ProhibitedWordManager(databaseManager, yamlManager);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        
        // 注册命令
        getCommand("prohibited").setExecutor(new ProhibitedCommand(this));
        
        // 设置数据库连接保持活跃的定时任务（每60秒执行一次）
        keepAliveTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (databaseManager != null) {
                databaseManager.keepConnectionAlive();
            }
        }, 1200L, 1200L); // 1200 ticks = 60秒 (20 ticks per second)
        
        getLogger().info("NekoProhibited 插件启动完成!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("NekoProhibited 插件正在关闭...");
        
        // 取消定时任务
        if (keepAliveTaskId != -1) {
            getServer().getScheduler().cancelTask(keepAliveTaskId);
        }
        
        // 关闭数据库连接
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        
        getLogger().info("NekoProhibited 插件已关闭!");
    }
    
    public ProhibitedWordManager getProhibitedWordManager() {
        return prohibitedWordManager;
    }
}
