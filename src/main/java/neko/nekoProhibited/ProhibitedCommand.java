package neko.nekoProhibited;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProhibitedCommand implements CommandExecutor {
    private final NekoProhibited plugin;

    public ProhibitedCommand(NekoProhibited plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§c用法: /prohibited <add|remove|list> [参数]");
            sender.sendMessage("§c  add <违禁词> [描述] - 添加违禁词");
            sender.sendMessage("§c  remove <违禁词> - 删除违禁词");
            sender.sendMessage("§c  list - 列出所有违禁词");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":
                return handleAddCommand(sender, args);
            case "remove":
                return handleRemoveCommand(sender, args);
            case "list":
                return handleListCommand(sender, args);
            default:
                sender.sendMessage("§c未知子命令: " + subCommand);
                sender.sendMessage("§c用法: /prohibited <add|remove|list> [参数]");
                return true;
        }
    }

    private boolean handleAddCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c用法: /prohibited add <违禁词> [描述]");
            return true;
        }

        String word = args[1];
        String description = "";
        
        if (args.length > 2) {
            StringBuilder descBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                descBuilder.append(args[i]).append(" ");
            }
            description = descBuilder.toString().trim();
        }

        if (plugin.getProhibitedWordManager().addProhibitedWord(word, description)) {
            sender.sendMessage("§a成功添加违禁词: " + word);
        } else {
            sender.sendMessage("§c添加违禁词失败: " + word);
        }

        return true;
    }

    private boolean handleRemoveCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c用法: /prohibited remove <违禁词>");
            return true;
        }

        String word = args[1];

        if (plugin.getProhibitedWordManager().removeProhibitedWord(word)) {
            sender.sendMessage("§a成功删除违禁词: " + word);
        } else {
            sender.sendMessage("§c删除违禁词失败: " + word + " (可能不存在)");
        }

        return true;
    }

    private boolean handleListCommand(CommandSender sender, String[] args) {
        sender.sendMessage("§a=== 违禁词列表 ===");
        
        for (String word : plugin.getProhibitedWordManager().getOriginalWords()) {
            sender.sendMessage("§e- " + word);
        }
        
        sender.sendMessage("§a==================");
        return true;
    }
}