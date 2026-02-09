package org.example.adminpanel.utils;

import org.bukkit.ChatColor;

public class ChatUtils {
    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
