package neko.nekoProhibited;

import org.bukkit.plugin.java.JavaPlugin;

public final class NekoProhibited extends JavaPlugin {
    private DatabaseManager databaseManager;
    private ProhibitedWordManager prohibitedWordManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("NekoProhibited 插件正在启动...");
        
        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化数据库连接
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        
        // 初始化违禁词管理器
        prohibitedWordManager = new ProhibitedWordManager(databaseManager);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        
        getLogger().info("NekoProhibited 插件启动完成!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("NekoProhibited 插件正在关闭...");
        
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
