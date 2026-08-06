package dev.naspo.tether.utils

import dev.naspo.tether.config.ConfigAccessor
import dev.naspo.tether.config.ConfigKeys
import org.bukkit.ChatColor
import org.bukkit.entity.Player

// General plugin utils

/**
 * Utility method to make translating a string with color easier.
 *
 * @param text The string to translate into supporting colour codes.
 * @return The translated string.
 */
fun chatColor(text: String): String {
    return ChatColor.translateAlternateColorCodes('&', text)
}

/**
 * Send a chat color translated message to a player (without plugin prefix).
 */
fun sendPlayerMessage(player: Player, message: String) {
    player.sendMessage(chatColor(message))
}

/**
 * Send a chat color translated message to a player with plugin prefix.
 */
fun sendPlayerPrefixedMessage(
    player: Player,
    message: String,
    configAccessor: ConfigAccessor
) {
    player.sendMessage(chatColor(configAccessor.get(ConfigKeys.Messages.pluginPrefix) + message))
}