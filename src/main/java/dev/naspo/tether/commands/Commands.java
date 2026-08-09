package dev.naspo.tether.commands;

import dev.naspo.tether.Tether;
import dev.naspo.tether.config.ConfigAccessor;
import dev.naspo.tether.config.ConfigKeys;
import dev.naspo.tether.messages.MessagesKt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
    private final Tether plugin;
    private final ConfigAccessor configAccessor;

    // "Did you mean /tether reload?" messages.
    private final static String didYouMeanReloadMessageFormatted = "<gray>Did you mean <gold>/tether reload<gray>?";
    private final static String didYouMeanReloadMessagePlain = "Did you mean /tether reload?";

    public Commands(Tether plugin, ConfigAccessor configAccessor) {
        this.plugin = plugin;
        this.configAccessor = configAccessor;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Player command stuff.
        if (sender instanceof Player player) {
            // The only command is the reload command, so perform a one-and-only permission check here.
            if (!(player.hasPermission("tether.reload"))) {
                MessagesKt.sendPlayerMessage(player, configAccessor.get(ConfigKeys.Messages.INSTANCE.getNoPermission()));
                return true;
            }

            // If there is anything but 1 arg, send "did you mean /tether reload?" message.
            if (args.length != 1) {
                MessagesKt.sendPlayerPrefixedMessage(player, didYouMeanReloadMessageFormatted, configAccessor);
                return false;
            }

            // Reload command.
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                MessagesKt.sendPlayerPrefixedMessage(
                        player,
                        configAccessor.get(ConfigKeys.Messages.INSTANCE.getPluginReloaded()),
                        configAccessor
                );
                return true;
            } else {
                // If args[0] is not "reload", send "did you mean /tether reload?" message.
                MessagesKt.sendPlayerPrefixedMessage(player, didYouMeanReloadMessageFormatted, configAccessor);
            }
            return false;
        }

        // Console command stuff.
        if (args.length != 1) {
            // If there is anything but 1 arg, send "did you mean /tether reload?" message.
            sender.sendMessage(didYouMeanReloadMessagePlain);
            return false;
        } else if (args[0].equalsIgnoreCase("reload")) {
            // Reload command.
            plugin.reloadConfig();
            sender.sendMessage("Tether has been reloaded.");
            return true;
        } else {
            // If args[0] is not "reload", send "did you mean /tether reload?" message.
            sender.sendMessage(didYouMeanReloadMessagePlain);
        }
        return false;
    }
}
