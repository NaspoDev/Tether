package dev.naspo.tether.core;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Commands implements CommandExecutor {

    Tether plugin;
    Commands(Tether plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("tether")) {
            if (sender instanceof Player) {
                //player stuff
                Player player = (Player) sender;
                if (!(player.hasPermission("tether.reload"))) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(
                            plugin.getConfig().getString("messages.no-permission"))));
                    return true;
                }
                if (args.length == 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(plugin.getConfig().getString("messages.prefix"))
                                    + "Did you mean &6/tether reload?"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    Utils.reloadConfigs();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.
                            requireNonNull(plugin.getConfig().getString("messages.prefix")) +
                            Objects.requireNonNull(plugin.getConfig().getString("messages.reload"))));
                    return true;
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(plugin.getConfig().getString("messages.prefix"))
                                + "Did you mean &6/tether reload?"));
            }
            //console stuff
            if (args.length == 0) {
                sender.sendMessage("Did you mean /tether reload?");
            } else if (args[0].equalsIgnoreCase("reload")) {
                Utils.reloadConfigs();
                sender.sendMessage("Tether has been reloaded");
            } else {
                sender.sendMessage("Did you mean /tether reload?");
            }
        }

        return false;
    }
}
