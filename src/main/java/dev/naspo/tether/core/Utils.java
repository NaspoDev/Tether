package dev.naspo.tether.core;

import org.bukkit.ChatColor;

public class Utils {
    
    // Returns the plugins prefix.
    public static String getPrefix(Tether plugin) {
        return plugin.getConfig().getString("messages.prefix");
    }

    // Utility method to make using ChatColor easier.
    public static String chatColor(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
