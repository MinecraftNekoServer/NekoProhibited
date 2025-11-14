package neko.nekoProhibited;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.regex.Pattern;

public class ChatListener implements Listener {
    private final NekoProhibited plugin;

    public ChatListener(NekoProhibited plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // 检查是否启用了违禁词检测
        if (!plugin.getConfig().getBoolean("prohibited.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // 替换消息中的违禁词
        String filteredMessage = filterMessage(message);
        
        if (!message.equals(filteredMessage)) {
            // 如果消息被过滤，则更新消息
            event.setMessage(filteredMessage);
            
            // 可以在这里添加日志记录或其他处理逻辑
            plugin.getLogger().info("玩家 " + player.getName() + " 的消息中的违禁词已被替换: " + message + " -> " + filteredMessage);
        }
    }
    
    private String filterMessage(String message) {
        String filteredMessage = message;
        
        // 获取所有原始违禁词并逐个替换
        for (String prohibitedWord : plugin.getProhibitedWordManager().getOriginalWords()) {
            filteredMessage = replaceProhibitedWord(filteredMessage, prohibitedWord, "杂鱼");
        }
        
        return filteredMessage;
    }
    
    private String replaceProhibitedWord(String message, String prohibitedWord, String replacement) {
        // 创建正则表达式来匹配插入了分隔符和字符替换的违禁词
        String regex = createRegexForProhibitedWord(prohibitedWord);
        return message.replaceAll("(?i)" + regex, replacement);
    }

    /**
     * 为违禁词创建正则表达式，匹配插入了分隔符和字符替换的情况
     */
    private String createRegexForProhibitedWord(String word) {
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            regex.append(createCharPattern(c));
            // 在每个字符后添加可选的分隔符模式
            if (i < word.length() - 1) {
                regex.append("[\\s\\u00A0\\u2000-\\u200F\\u2028-\\u202F\\u205F-\\u206F\\\\.\\-,_'\"!@#$%^&*()\\[\\]{}|;:<>?/`~0134578@!lz9]*");
            }
        }
        return regex.toString();
    }
    
    /**
     * 为单个字符创建包含相似字符替换的模式
     */
    private String createCharPattern(char c) {
        switch (c) {
            case 'a':
                return "[a@]";
            case 's':
                return "[s$5]";
            case 'i':
                return "[i!1l]";
            case 'e':
                return "[e3]";
            case 'o':
                return "[o0]";
            case 't':
                return "[t7]";
            case 'b':
                return "[b8]";
            case 'g':
                return "[g9]";
            case 'z':
                return "[z]";
            default:
                return Pattern.quote(String.valueOf(c));
        }
    }
}
