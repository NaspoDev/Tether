package me.naspo.tether;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Tether extends JavaPlugin {

    Leash leash = new Leash(this);
    Commands commands = new Commands(this);
    TabCompleter tabCompleter = new TabCompleter();

    @Override
    public void onEnable() {
        super.onEnable();
        this.saveDefaultConfig();
        this.getLogger().info("Tether has been enabled!");
        this.getServer().getPluginManager().registerEvents(leash, this);
        Objects.requireNonNull(this.getCommand("tether")).setExecutor(commands);
        Objects.requireNonNull(this.getCommand("tether")).setTabCompleter(tabCompleter);

    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.getLogger().info("Tether has been disabled!");
    }
}
