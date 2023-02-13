package me.fodded.banplugin.utils;

import net.md_5.bungee.api.ChatColor;

public class ChatUtil {
    public static String format(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
