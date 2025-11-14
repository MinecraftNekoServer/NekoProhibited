package neko.nekoProhibited;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class YAMLManager {
    private static final Logger logger = Logger.getLogger("NekoProhibited");
    private final NekoProhibited plugin;
    private File yamlFile;
    private YamlConfiguration yamlConfig;
    private static final String YAML_FILE_NAME = "prohibited_words.yml";

    public YAMLManager(NekoProhibited plugin) {
        this.plugin = plugin;
        initializeYAMLFile();
    }

    private void initializeYAMLFile() {
        yamlFile = new File(plugin.getDataFolder(), YAML_FILE_NAME);
        
        // 如果文件不存在，创建它
        if (!yamlFile.exists()) {
            try {
                yamlFile.getParentFile().mkdirs();
                yamlFile.createNewFile();
            } catch (IOException e) {
                logger.severe("创建YAML文件失败: " + e.getMessage());
                return;
            }
        }
        
        // 加载YAML配置
        yamlConfig = YamlConfiguration.loadConfiguration(yamlFile);
        logger.info("YAML文件已初始化: " + yamlFile.getAbsolutePath());
    }

    /**
     * 从YAML文件读取所有违禁词
     */
    public List<String> loadProhibitedWords() {
        List<String> words = yamlConfig.getStringList("prohibited_words");
        if (words == null) {
            words = new ArrayList<>();
        }
        logger.info("已从YAML文件加载 " + words.size() + " 个违禁词");
        return words;
    }

    /**
     * 将违禁词列表保存到YAML文件
     */
    public void saveProhibitedWords(List<String> words) {
        yamlConfig.set("prohibited_words", words);
        try {
            yamlConfig.save(yamlFile);
            logger.info("已保存 " + words.size() + " 个违禁词到YAML文件");
        } catch (IOException e) {
            logger.severe("保存YAML文件失败: " + e.getMessage());
        }
    }

    /**
     * 向YAML文件添加一个违禁词
     */
    public void addProhibitedWord(String word) {
        List<String> words = loadProhibitedWords();
        if (!words.contains(word)) {
            words.add(word);
            saveProhibitedWords(words);
        }
    }

    /**
     * 从YAML文件删除一个违禁词
     */
    public void removeProhibitedWord(String word) {
        List<String> words = loadProhibitedWords();
        if (words.remove(word)) {
            saveProhibitedWords(words);
        }
    }

    /**
     * 检查YAML文件是否为空
     */
    public boolean isYAMLFileEmpty() {
        List<String> words = loadProhibitedWords();
        return words.isEmpty();
    }

    public File getYamlFile() {
        return yamlFile;
    }

    public YamlConfiguration getYamlConfig() {
        return yamlConfig;
    }
}