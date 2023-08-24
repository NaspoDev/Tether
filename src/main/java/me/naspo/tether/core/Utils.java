package me.naspo.tether.core;

import org.bukkit.ChatColor;

public class Utils {
    private static Tether plugin;

    public static String prefix;

    Utils(Tether plugin) {
        Utils.plugin = plugin;

        reloadVars();
    }

    // Reloads config.yml and call to reload global variables.
    public static void reloadConfigs() {
        plugin.reloadConfig();
        reloadVars();
    }

    // Reloads (updates) global variables that get values from config.
    private static void reloadVars() {
        prefix = plugin.getConfig().getString("messages.prefix");
    }

    // Utility method to make using ChatColor easier.
    public static String chatColor(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
