package dev.naspo.tether.utils;

import dev.naspo.tether.Tether;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

// General plugin utils.
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
