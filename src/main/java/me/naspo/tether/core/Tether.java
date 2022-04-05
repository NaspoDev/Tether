package me.naspo.tether.core;

import me.naspo.tether.leash.LeashMob;
import me.naspo.tether.leash.LeashPlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Tether extends JavaPlugin {

    LeashMob leashMob = new LeashMob(this);
    LeashPlayer leashPlayer = new LeashPlayer(this);
    Commands commands = new Commands(this);
    TabCompleter tabCompleter = new TabCompleter();

    @Override
    public void onEnable() {
        super.onEnable();
        this.saveDefaultConfig();
        this.getLogger().info("Tether has been enabled!");
        this.getServer().getPluginManager().registerEvents(leashMob, this);
        if (Objects.requireNonNull(this.getConfig().getString("config-version")).equalsIgnoreCase("2")) {
            this.getServer().getPluginManager().registerEvents(leashPlayer, this);
        }
        Objects.requireNonNull(this.getCommand("tether")).setExecutor(commands);
        Objects.requireNonNull(this.getCommand("tether")).setTabCompleter(tabCompleter);

    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.getLogger().info("Tether has been disabled!");
    }
}
