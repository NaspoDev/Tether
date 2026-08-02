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

fun sendPlayerMessage(
    player: Player,
    configAccessor: ConfigAccessor,
    message: String,
    withPrefix: Boolean = true
) {
    var msg = message
    if (withPrefix) {
        msg = configAccessor.get(ConfigKeys.Messages.pluginPrefix) + message
    }
    player.sendMessage(chatColor(message))
}