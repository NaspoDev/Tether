package me.naspo.tether.core;

import org.bukkit.ChatColor;

public class Utils {
    private static Tether plugin;

    public static String prefix;

    Utils(Tether plugin) {
        Utils.plugin = plugin;

        reloadVars();
    }

    public static void reloadConfigs() {
        plugin.reloadConfig();

        reloadVars();
    }

    private static void reloadVars() {
        prefix = plugin.getConfig().getString("messages.prefix");
    }

    public static String chatColor(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
