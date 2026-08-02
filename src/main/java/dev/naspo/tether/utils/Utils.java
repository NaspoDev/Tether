package dev.naspo.tether.utils;

import dev.naspo.tether.Tether;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

// General plugin utils.
public class Utils {

    /**
     * Utility method to make using ChatColor easier.
     *
     * @param text The string to translate into supporting colour codes.
     * @return The translated string.
     */
    public static String chatColor(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}