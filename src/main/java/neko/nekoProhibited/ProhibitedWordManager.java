package neko.nekoProhibited;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Set;

public class ProhibitedWordManager {
    private static final Logger logger = Logger.getLogger("NekoProhibited");
    private final DatabaseManager dbManager;
    private final YAMLManager yamlManager;
    private List<String> prohibitedWords;
    private List<String> originalWords; // 保存原始违禁词
    private Set<String> prohibitedWordSet; // 使用Set进行快速查找

    public ProhibitedWordManager(DatabaseManager dbManager, YAMLManager yamlManager) {
        this.dbManager = dbManager;
        this.yamlManager = yamlManager;
        this.prohibitedWords = new ArrayList<>();
        this.originalWords = new ArrayList<>();
        this.prohibitedWordSet = new HashSet<>();
        initializeWordsFromYAML();
    }

    /**
     * 初始化违禁词：每次启动时都从数据库拉取并写入本地YAML文件
     */
    private void initializeWordsFromYAML() {
        // 每次启动都从数据库加载违禁词并写入YAML文件
        loadProhibitedWordsFromDatabaseAndSaveToYAML();
    }

    /**
     * 从YAML文件加载违禁词
     */
    private void loadProhibitedWordsFromYAML() {
        prohibitedWords.clear();
        originalWords.clear();
        prohibitedWordSet.clear();
        
        List<String> words = yamlManager.loadProhibitedWords();
        for (String word : words) {
            if (word != null && !word.trim().isEmpty()) {
                originalWords.add(word.trim());
                String lowerWord = word.trim().toLowerCase();
                prohibitedWords.add(lowerWord);
                prohibitedWordSet.add(lowerWord);
            }
        }
        
        logger.info("已从YAML文件加载 " + prohibitedWords.size() + " 个违禁词");
    }

    /**
     * 从数据库加载违禁词并写入YAML文件
     */
    private void loadProhibitedWordsFromDatabaseAndSaveToYAML() {
        prohibitedWords.clear();
        originalWords.clear();
        prohibitedWordSet.clear();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT word FROM prohibited_words");
             ResultSet rs = stmt.executeQuery()) {
            
            List<String> wordsToSave = new ArrayList<>();
            
            while (rs.next()) {
                String word = rs.getString("word");
                if (word != null && !word.trim().isEmpty()) {
                    String trimmedWord = word.trim();
                    originalWords.add(trimmedWord);
                    String lowerWord = trimmedWord.toLowerCase();
                    prohibitedWords.add(lowerWord);
                    prohibitedWordSet.add(lowerWord);
                    
                    // 添加到待保存列表
                    wordsToSave.add(trimmedWord);
                }
            }
            
            // 保存到YAML文件
            yamlManager.saveProhibitedWords(wordsToSave);
            
            logger.info("已从数据库加载并写入YAML文件 " + prohibitedWords.size() + " 个违禁词");
        } catch (SQLException e) {
            logger.severe("从数据库加载违禁词时出错: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("保存违禁词到YAML文件时出错: " + e.getMessage());
        }
    }

    /**
     * 从数据库加载违禁词（不使用Redis）
     */
    public void loadProhibitedWords() {
        prohibitedWords.clear();
        originalWords.clear();
        prohibitedWordSet.clear();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT word FROM prohibited_words");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String word = rs.getString("word");
                if (word != null && !word.trim().isEmpty()) {
                    originalWords.add(word.trim());
                    String lowerWord = word.trim().toLowerCase();
                    prohibitedWords.add(lowerWord);
                    prohibitedWordSet.add(lowerWord); // 添加到Set中以提高查找效率
                }
            }
            
            logger.info("已加载 " + prohibitedWords.size() + " 个违禁词");
        } catch (SQLException e) {
            logger.severe("加载违禁词时出错: " + e.getMessage());
        }
    }

    public boolean isProhibited(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        
        // 首先检查是否包含完整的违禁词（快速检查）
        for (String prohibitedWord : prohibitedWordSet) {
            if (lowerText.contains(prohibitedWord)) {
                return true;
            }
        }
        
        // 如果快速检查未发现违禁词，再使用更复杂的检测方法
        for (String prohibitedWord : prohibitedWords) {
            if (containsProhibitedWord(lowerText, prohibitedWord)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 检查文本是否包含违禁词，包括各种变体（如空格、符号、数字替换等）
     */
    private boolean containsProhibitedWord(String text, String prohibitedWord) {
        // 检查原始文本
        if (text.contains(prohibitedWord)) {
            return true;
        }
        
        // 移除所有空格和常见分隔符
        String normalizedText = text.replaceAll("[\\s\\u00A0\\u2000-\\u200F\\u2028-\\u202F\\u205F-\\u206F\\\\.\\-,_'\"!@#$%^&*()\\[\\]{}|;:<>?/`~]+", "");
        
        // 检查移除分隔符后的文本
        if (normalizedText.contains(prohibitedWord)) {
            return true;
        }
        

        // 额外检查：处理数字绕过（如"傻123逼"）

        if (containsProhibitedWordWithDigits(text, prohibitedWord)) {

            return true;

        }

        

        // 额外检查：处理字母绕过（如"傻asd逼"）

        if (containsProhibitedWordWithLetters(text, prohibitedWord)) {

            return true;

        }

        

        // 创建正则表达式来匹配插入了分隔符的违禁词

        String regex = createRegexForProhibitedWord(prohibitedWord);

        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text).find();

    }

    /**
     * 检查文本是否包含带有数字分隔的违禁词（如"傻123逼"）
     */
    private boolean containsProhibitedWordWithDigits(String text, String prohibitedWord) {
        // 更精确的检查：只检测在违禁词字符间插入数字的情况
        // 构建一个正则表达式，匹配违禁词字符间可能有数字的模式
        StringBuilder pattern = new StringBuilder();

        for (int i = 0; i < prohibitedWord.length(); i++) {
            if (i > 0) {
                pattern.append("[0-9]*"); // 在字符之间允许数字
            }
            pattern.append(Pattern.quote(String.valueOf(prohibitedWord.charAt(i))));
        }

        return Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    /**
     * 检查文本是否包含带有字母分隔的违禁词（如"傻asd逼"）
     */
    private boolean containsProhibitedWordWithLetters(String text, String prohibitedWord) {
        // 更精确的检查：只移除插入在违禁词字符之间的字母
        // 创建一个模式来匹配违禁词，其中字符之间可能有字母
        StringBuilder pattern = new StringBuilder();

        for (int i = 0; i < prohibitedWord.length(); i++) {
            if (i > 0) {
                pattern.append("[a-zA-Z]*"); // 在字符之间允许字母
            }
            pattern.append(Pattern.quote(String.valueOf(prohibitedWord.charAt(i))));
        }

        return Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    /**
     * 为违禁词创建正则表达式，匹配插入了分隔符的情况
     */
    private String createRegexForProhibitedWord(String word) {
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            regex.append(Pattern.quote(String.valueOf(word.charAt(i))));
            // 在每个字符后添加可选的分隔符模式
            if (i < word.length() - 1) {
                regex.append("[\\s\\u00A0\\u2000-\\u200F\\u2028-\\u202F\\u205F-\\u206F\\\\.,_'\"!@#$%^&*()\\[\\]{}|;:<>?/`~0-9l z，。；：！？\"'（）【】《》、]*");

            }
        }
        return regex.toString();
    }

    /**
     * 添加新的违禁词到数据库和YAML文件
     */
    public boolean addProhibitedWord(String word, String description) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO prohibited_words (word, description) VALUES (?, ?)")) {
            
            stmt.setString(1, word.trim());
            stmt.setString(2, description != null ? description : "");
            stmt.executeUpdate();
            
            // 同步到YAML文件
            yamlManager.addProhibitedWord(word.trim());
            
            // 重新加载违禁词列表
            loadProhibitedWordsFromYAML();
            
            return true;
        } catch (SQLException e) {
            logger.severe("添加违禁词时出错: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从数据库删除违禁词
     */
    public boolean removeProhibitedWord(String word) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM prohibited_words WHERE word = ?")) {
            
            stmt.setString(1, word);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // 从YAML文件中删除
                yamlManager.removeProhibitedWord(word);
                
                // 重新加载违禁词列表
                loadProhibitedWordsFromYAML();
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.severe("删除违禁词时出错: " + e.getMessage());
            return false;
        }
    }

    public List<String> getProhibitedWords() {
        return new ArrayList<>(prohibitedWords);
    }
    
    public List<String> getOriginalWords() {
        return new ArrayList<>(originalWords);
    }
}