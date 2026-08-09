package dev.naspo.tether.messages

import dev.naspo.tether.config.ConfigAccessor
import dev.naspo.tether.config.ConfigKeys
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

// Helpers for sending formatted (MiniMessage) messages to players.
// MiniMessage docs: https://docs.papermc.io/adventure/minimessage/

private val mm = MiniMessage.miniMessage()

/**
 * Send a message to a player (without the plugin prefix).
 * Supports text formatting & styling with the MiniMessage format.
 */
fun sendPlayerMessage(player: Player, message: String) {
    player.sendMessage(mm.deserialize(message))
}

/**
 * Send a message to a player, prefixed with the plugin prefix.
 * Supports text formatting & styling with the MiniMessage format.
 */
fun sendPlayerPrefixedMessage(
    player: Player,
    message: String,
    configAccessor: ConfigAccessor
) {
    player.sendMessage(mm.deserialize(configAccessor.get(ConfigKeys.Messages.pluginPrefix) + message))
}