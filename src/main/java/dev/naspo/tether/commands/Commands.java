package dev.naspo.tether.commands;

import dev.naspo.tether.Tether;
import dev.naspo.tether.config.ConfigAccessor;
import dev.naspo.tether.config.ConfigKeys;
import dev.naspo.tether.utils.UtilsKt;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Commands implements CommandExecutor {
    private final Tether plugin;
    private final ConfigAccessor configAccessor;

    public Commands(Tether plugin, ConfigAccessor configAccessor) {
        this.plugin = plugin;
        this.configAccessor = configAccessor;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("tether")) {
            if (sender instanceof Player) {
                //player stuff
                Player player = (Player) sender;
                if (!(player.hasPermission("tether.reload"))) {
                    UtilsKt.sendPlayerMessage(player, configAccessor.get(ConfigKeys.Messages.INSTANCE.getNoPermission()));
                    return true;
                }
                if (args.length == 0) {
                    UtilsKt.sendPlayerPrefixedMessage(player, "Did you mean &6/tether reload?", configAccessor);
                    return true;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    plugin.reloadConfig();
                    UtilsKt.sendPlayerPrefixedMessage(
                            player,
                            configAccessor.get(ConfigKeys.Messages.INSTANCE.getPluginReloaded()),
                            configAccessor
                    );
                    return true;
                }
                UtilsKt.sendPlayerPrefixedMessage(player, "Did you mean &6/tether reload?", configAccessor);
            }
            //console stuff
            if (args.length == 0) {
                sender.sendMessage("Did you mean /tether reload?");
            } else if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                sender.sendMessage("Tether has been reloaded");
            } else {
                sender.sendMessage("Did you mean /tether reload?");
            }
        }

        return false;
    }
}
