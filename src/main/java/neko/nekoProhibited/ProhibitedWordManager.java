package neko.nekoProhibited;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ProhibitedWordManager {
    private static final Logger logger = Logger.getLogger("NekoProhibited");
    private final DatabaseManager dbManager;
    private List<String> prohibitedWords;
    private List<String> originalWords; // 保存原始违禁词

    public ProhibitedWordManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.prohibitedWords = new ArrayList<>();
        this.originalWords = new ArrayList<>();
        loadProhibitedWords();
    }

    public void loadProhibitedWords() {
        prohibitedWords.clear();
        originalWords.clear();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT word FROM prohibited_words");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String word = rs.getString("word");
                if (word != null && !word.trim().isEmpty()) {
                    originalWords.add(word.trim());
                    prohibitedWords.add(word.trim().toLowerCase());
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
        
        // 检查是否包含违禁词（严格模式：包括各种变体）
        for (String prohibitedWord : prohibitedWords) {
            if (containsProhibitedWord(lowerText, prohibitedWord)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 检查文本是否包含违禁词，包括各种变体（如空格、符号等）
     */
    private boolean containsProhibitedWord(String text, String prohibitedWord) {
        // 移除所有空格和常见分隔符的正则表达式
        String normalizedText = text.replaceAll("[\\s\\u00A0\\u2000-\\u200F\\u2028-\\u202F\\u205F-\\u206F\\\\.\\-,_'\"!@#$%^&*()\\[\\]{}|;:<>?/`~]+", "");
        
        // 检查原始文本
        if (normalizedText.contains(prohibitedWord)) {
            return true;
        }
        
        // 创建正则表达式来匹配插入了分隔符的违禁词
        String regex = createRegexForProhibitedWord(prohibitedWord);
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text).find();
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
                regex.append("[\\s\\u00A0\\u2000-\\u200F\\u2028-\\u202F\\u205F-\\u206F\\\\.\\-,_'\"!@#$%^&*()\\[\\]{}|;:<>?/`~]*");
            }
        }
        return regex.toString();
    }

    /**
     * 添加新的违禁词到数据库
     */
    public boolean addProhibitedWord(String word, String description) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO prohibited_words (word, description) VALUES (?, ?)")) {
            
            stmt.setString(1, word.trim());
            stmt.setString(2, description != null ? description : "");
            stmt.executeUpdate();
            
            // 重新加载违禁词列表
            loadProhibitedWords();
            
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
                // 重新加载违禁词列表
                loadProhibitedWords();
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