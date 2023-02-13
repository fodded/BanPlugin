package me.fodded.banplugin.commands;

import me.fodded.banplugin.Main;
import me.fodded.banplugin.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;

public class BanCommand extends Command {

    public BanCommand() {
        super("ban");

        try {
            SimpleCommandMap simpleCommandMap = (SimpleCommandMap) Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap").invoke(Bukkit.getServer());
            simpleCommandMap.register(this.getName(), "bansystem", this);
        } catch (ReflectiveOperationException ex) {
            Main.getPlugin().getLogger().severe("Could not register command: " + ex);
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(args[0]);
            Main.getPlugin().getBanHandler().banPlayer(target, null, ChatUtil.format("&fCheating through the use of unfair game advantages."));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("network.ban")) {
            player.sendMessage(Main.getPlugin().getNoPermissions());
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(ChatUtil.format("&c/ban <name> <reason: c/b>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore()) {
            player.sendMessage(ChatUtil.format("&cWrong nickname!"));
            return true;
        }

        if (Main.getDatabaseConnection().isPlayerBanned(target.getUniqueId())) {
            sender.sendMessage(ChatUtil.format("&cThis player is already banned!"));
            return true;
        }

        String reason = args[1];
        reason = getReason(reason);

        Main.getPlugin().getBanHandler().banPlayer(target, player, reason);
        return true;
    }

    private String getReason (String reason){
        if(reason.equalsIgnoreCase("b")) {
            return "&fBoosting your account to improve your stats.";
        }
        return "&fCheating through the use of unfair game advantages.";
    }
}

