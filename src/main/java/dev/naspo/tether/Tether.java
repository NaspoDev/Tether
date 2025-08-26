package dev.naspo.tether;

import dev.naspo.tether.commandstuff.Commands;
import dev.naspo.tether.commandstuff.TabCompleter;
import dev.naspo.tether.leash.LeashPlayer;
import dev.naspo.tether.listeners.PlayerInteractAtEntityListener;
import dev.naspo.tether.listeners.PlayerInteractListener;
import dev.naspo.tether.listeners.PlayerLeashEntityListener;
import dev.naspo.tether.services.ClaimCheckService;
import dev.naspo.tether.services.LeashMobService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Tether extends JavaPlugin {
    private LeashMobService leashMobService;
    private ClaimCheckService claimCheckService;

    private boolean[] enableHooks = new boolean[4];

    @Override
    public void onEnable() {
        // config.yml stuff
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        this.getLogger().info("Tether has been enabled!");

        hooksCheck();
        instantiateClasses();
        registerEvents();
        registerCommands();
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Tether has been disabled!");
    }

    // Checks which hooks are enabled.
    private void hooksCheck() {
        // GriefPrevention check.
        if (this.getConfig().getBoolean("hooks.griefprevention")) {
            if (this.getServer().getPluginManager().getPlugin("GriefPrevention") == null) {
                this.getLogger().log(Level.WARNING, "GriefPrevention hook set to true in config, but " +
                        "the plugin does not exist on the server. The hook will not work!");
            } else {
                enableHooks[0] = true;
            }
        }

        // Towny check.
        if (this.getConfig().getBoolean("hooks.towny")) {
            if (this.getServer().getPluginManager().getPlugin("Towny") == null) {
                this.getLogger().log(Level.WARNING, "Towny hook set to true in config, but " +
                        "the plugin does not exist on the server. The hook will not work!");
            } else {
                enableHooks[1] = true;
            }
        }

        // Lands check.
        if (this.getConfig().getBoolean("hooks.lands")) {
            if (this.getServer().getPluginManager().getPlugin("Lands") == null) {
                this.getLogger().log(Level.WARNING, "Lands hook set to true in config, but " +
                        "the plugin does not exist on the server. The hook will not work!");
            } else {
                enableHooks[2] = true;
            }
        }

        // GriefDefender check.
        if (this.getConfig().getBoolean("hooks.griefdefender")) {
            if (this.getServer().getPluginManager().getPlugin("GriefDefender") == null) {
                this.getLogger().log(Level.WARNING, "GriefDefender hook set to true in config, but " +
                        "the plugin does not exist on the server. The hook will not work!");
            } else {
                enableHooks[3] = true;
            }
        }
    }

    private void instantiateClasses() {
        claimCheckService = new ClaimCheckService(this, enableHooks);
        leashMobService = new LeashMobService(this, claimCheckService);
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new PlayerInteractAtEntityListener(this, leashMobService), this);
        this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(leashMobService), this);
        this.getServer().getPluginManager().registerEvents(new PlayerLeashEntityListener(leashMobService), this);
        this.getServer().getPluginManager().registerEvents(new LeashPlayer(this, claimCheckService), this);
    }

    private void registerCommands() {
        this.getCommand("tether").setExecutor(new Commands(this));
        this.getCommand("tether").setTabCompleter(new TabCompleter());
    }
}
