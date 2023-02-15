package me.fodded.banplugin.data;

import me.fodded.banplugin.Main;
import me.fodded.banplugin.customevents.BanEvent;
import me.fodded.banplugin.customevents.UnbanEvent;
import me.fodded.banplugin.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class BanHandler {

    public void banPlayer(OfflinePlayer target, Player staff, String reason) {
        int bansAmount = Main.getDatabaseConnection().getBanAmount(target.getUniqueId());
        int days = getDays(bansAmount);

        long time = (24*1000*60*60L) * days;
        long banDuration = (time - 1000L);

        String banDurString = new SimpleDateFormat("D'd' H'h' m'm' s's'").format(banDuration - 97200000L);

        if (target.isOnline()) {
            String reasonMsg = (ChatUtil.format("\n&cYou are temporarily banned for &f" + banDurString + " &cfrom this server!\n\n"));

            reasonMsg += ChatUtil.format( "&7Reason: &f" + reason + "\n");
            reasonMsg += ChatUtil.format( "&7Find out more: &b&nhttps://discord.gg/JqggwBw2zb \n");

            ((Player) target).kickPlayer(reasonMsg);
        }

        Main.getDatabaseConnection().execute(
                "INSERT INTO `bans` (`uuid`, "
                        + "`staff_uuid`, "
                        + "`ban_date`, "
                        + "`ban_time`, "
                        + "`reason`) VALUES (?,?,?,?,?)",
                target.getUniqueId().toString(),
                staff == null ? "CONSOLE" : staff.getUniqueId().toString(),
                System.currentTimeMillis(),
                banDuration,
                reason
        );

        BanEvent banEvent = new BanEvent(reason, (staff == null) ? "Anticheat" : staff.getName(), target.getName(), days, Main.getDatabaseConnection().getPosition(target.getUniqueId()), Main.getDatabaseConnection().getRating(target.getUniqueId()), (staff == null) ? null : staff.getUniqueId(), target.getUniqueId());
        Bukkit.getServer().getPluginManager().callEvent(banEvent);

        // sendMessageToReporters(String.valueOf(target.getUniqueId())); Reporting system is not ready yet
        setRating(target.getUniqueId(), false);
        if(staff != null) {
            staff.sendMessage(ChatUtil.format("&aYou have successfully banned " + target.getName()));
        }
    }

    public void unbanPlayer(OfflinePlayer target, Player staff) {
        Database database = Main.getDatabaseConnection();
        database.execute(
                "DELETE FROM `bans` WHERE `uuid` = ?",
                target.getUniqueId().toString()
        );

        UUID unbanned_UUID = target.getUniqueId();
        setRating(unbanned_UUID, true);

        UnbanEvent unbanEvent = new UnbanEvent(staff.getName(), target.getName(), database.getPosition(unbanned_UUID), database.getRating(unbanned_UUID), (staff == null) ? null : staff.getUniqueId(), target.getUniqueId());
        Bukkit.getServer().getPluginManager().callEvent(unbanEvent);

        staff.sendMessage(ChatUtil.format("&aYou have successfully unbanned " + target.getName()));
    }

    private static void setRating(UUID uuid, boolean unbanned) {
        Main.getDatabaseConnection().execute(
                "UPDATE `season_" + Main.getDatabaseConnection().getCurrentSeason() + "` SET `banned` = ? WHERE `uuid` = ?", unbanned, uuid.toString()
        );
    }

    private Integer getDays(Integer bansAmount) {
        switch (bansAmount) {
            case 0:
                return 30;
            case 1:
                return 90;
            case 2:
                return 180;
            default:
                return 360;
        }
    }

    private static void sendMessageToReporters(String banned_uuid) {
        String request = "SELECT * FROM `reports` ORDER BY `reported_uuid` DESC LIMIT 1000000;";

        try {
            ResultSet resultSet = Main.getDatabaseConnection().query(request, banned_uuid);
            if (resultSet == null) {
                return;
            }

            while(resultSet.next()) {
                UUID reporter_uuid = UUID.fromString(resultSet.getString("reporter_uuid"));
                OfflinePlayer reporterPlayer = Bukkit.getOfflinePlayer(reporter_uuid);

                if(reporterPlayer.isOnline()) {
                    ((Player) reporterPlayer).sendMessage(ChatUtil.format(
                            "&aYour recent report was reviewed and handled. Thank you!"
                    ));
                }
            }
            Main.getDatabaseConnection().execute("DELETE * FROM `reports` WHERE `reported_uuid` = ?", banned_uuid);
        } catch (SQLException e) {}
    }
}
