package me.fodded.banplugin.commands;

import me.fodded.banplugin.Main;
import me.fodded.banplugin.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class UnbanCommand extends Command {

    public UnbanCommand() {
        super("unban");
        setAliases(Arrays.asList("forgive", "praise"));

        try {
            SimpleCommandMap simpleCommandMap = (SimpleCommandMap) Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap").invoke(Bukkit.getServer());
            simpleCommandMap.register(this.getName(), "bansystem", this);
        } catch (ReflectiveOperationException ex) {
            Main.getPlugin().getLogger().severe("Could not register command: " + ex);
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("network.unban")) {
            player.sendMessage(Main.getPlugin().getNoPermissions());
            return true;
        }

        if(args.length != 1) {
            player.sendMessage(ChatUtil.format("&c/unban <name>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(args[0]);

        if (!target.hasPlayedBefore()) {
            player.sendMessage(ChatUtil.format("&cThis player hasn't logged on the server!"));
            return true;
        }

        if (!Main.getDatabaseConnection().isPlayerBanned(target.getUniqueId())) {
            player.sendMessage(ChatUtil.format("&cThis player isn't banned!"));
            return true;
        }

        Main.getPlugin().getBanHandler().unbanPlayer(target, player);
        return true;
    }
}