package me.fodded.banplugin.listeners;

import me.fodded.banplugin.Main;
import me.fodded.banplugin.data.Database;
import me.fodded.banplugin.utils.ChatUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.text.SimpleDateFormat;
import java.util.UUID;


public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerConnect(AsyncPlayerPreLoginEvent event) {
        Database database = Main.getDatabaseConnection();
        if(!database.isPlayerBanned(event.getUniqueId())) {
            return;
        }

        UUID uuid = event.getUniqueId();

        long date_banned = database.getBanDate(uuid);
        long time_banned = database.getBanDuration(uuid);
        long time_left = (date_banned + time_banned) - System.currentTimeMillis() - 97200000L;

        String banDurString = new SimpleDateFormat("D'd' H'h' m'm' s's'").format(time_left);
        String banMessage = ChatUtil.format( "\n&cYou are temporarily banned for &f" + banDurString + " &cfrom this server!\n\n");
        banMessage += ChatUtil.format( "&7Reason: &f" + database.getBanReason(uuid) + "\n");
        banMessage += ChatUtil.format( "&7Find out more: &b&nhttps://mc.rankedskywars.net/appeal/");

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, banMessage);
    }
}
