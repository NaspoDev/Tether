package dev.naspo.tether.exceptions;

import dev.naspo.tether.config.ConfigAccessor;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import dev.naspo.tether.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

// Utils for exception handling.
public class ExceptionUtils {

    /**
     * Centralized logic to handle a LeashException.
     * @param player The player involved with the event.
     * @param event The event. Should be Cancellable.
     * @param exception The LeashException.
     * @param configAccessor A {@link ConfigAccessor} instance.
     */
    public static void handleLeashException(
            Player player,
            Cancellable event,
            LeashException exception,
            ConfigAccessor configAccessor) {
        switch (exception.getType()) {
            case TARGET_PLAYER_RIDING -> player.sendMessage(Utils.chatColor(Utils.getPrefix(plugin) +
                    plugin.getConfig().getString("messages.cannot-leash-riding-player")));
            case LAND_PROTECTED -> {
                event.setCancelled(true);
                player.sendMessage(Utils.chatColor(Utils.getPrefix(plugin) + plugin.getConfig().getString(
                        "messages.leash-target-in-protected-land")));
            }
            case PREVENT_NESTING -> player.sendMessage(Utils.chatColor(Utils.getPrefix(plugin) +
                    plugin.getConfig().getString("messages.prevent-nesting")));
        }
    }
}
