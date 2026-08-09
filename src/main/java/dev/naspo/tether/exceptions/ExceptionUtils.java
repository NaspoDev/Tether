package dev.naspo.tether.exceptions;

import dev.naspo.tether.config.ConfigAccessor;
import dev.naspo.tether.config.ConfigKeys;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import dev.naspo.tether.messages.MessagesKt;
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
            case TARGET_PLAYER_RIDING -> MessagesKt.sendPlayerPrefixedMessage(
                    player,
                    configAccessor.get(ConfigKeys.Messages.INSTANCE.getCannotLeashRidingPlayer()),
                    configAccessor
            );
            case LAND_PROTECTED -> {
                event.setCancelled(true);
                MessagesKt.sendPlayerPrefixedMessage(
                        player,
                        configAccessor.get(ConfigKeys.Messages.INSTANCE.getLeashTargetInProtectedLand()),
                        configAccessor
                );
            }
            case PREVENT_NESTING -> MessagesKt.sendPlayerPrefixedMessage(
                    player,
                    configAccessor.get(ConfigKeys.Messages.INSTANCE.getPreventNesting()),
                    configAccessor
            );
        }
    }
}
