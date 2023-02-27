package me.fodded.banplugin;

import lombok.Getter;
import me.fodded.banplugin.commands.BanCommand;
import me.fodded.banplugin.commands.UnbanCommand;
import me.fodded.banplugin.data.BanHandler;
import me.fodded.banplugin.data.Database;
import me.fodded.banplugin.listeners.PlayerJoinListener;
import me.fodded.banplugin.utils.ChatUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main extends JavaPlugin {

    @Getter
    private static Main plugin;

    @Getter
    private BanHandler banHandler;

    @Getter
    private static Connection connection;

    @Getter
    private static Database databaseConnection;

    public @Getter String noPermissions = ChatUtil.format("&cYou do not have permissions to use this command!");

    @Override
    public void onEnable() {
        plugin = this;
        banHandler = new BanHandler();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        new BanCommand();
        new UnbanCommand();

        getConfig().options().copyDefaults(true);
        getConfig().options().copyHeader(true);
        saveDefaultConfig();

        establishConnection();
        databaseConnection = new Database();
        databaseConnection.createTables(
                "CREATE TABLE IF NOT EXISTS bans (" +
                        "uuid VARCHAR(36) NOT NULL," +
                        "staff_uuid VARCHAR(36) NOT NULL," +
                        "ban_date BIGINT(36) NOT NULL," +
                        "ban_time BIGINT(36) NOT NULL," +
                        "reason VARCHAR(100) NOT NULL);"
        );
    }

    @Override
    public void onDisable() {

    }

    private void establishConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + this.getConfig().getString("ip") + ":" + this.getConfig().getString("port") + "/" + this.getConfig().getString("database") + "?useUnicode=true&amp;characterEncoding=UTF-8";
            String user = this.getConfig().getString("user");
            String password = this.getConfig().getString("password");

            this.connection = DriverManager.getConnection(url, user, password);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
