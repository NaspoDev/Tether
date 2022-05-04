package me.naspo.tether.core;

import me.naspo.tether.leash.ClaimCheckManager;
import me.naspo.tether.leash.LeashMob;
import me.naspo.tether.leash.LeashPlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.logging.Level;

public final class Tether extends JavaPlugin {
    private Utils utils;
    private LeashMob leashMob;
    private LeashPlayer leashPlayer;
    private Commands commands;
    private TabCompleter tabCompleter;
    private ClaimCheckManager claimCheckManager;

    private boolean[] enableHooks = new boolean[3];

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getLogger().info("Tether has been enabled!");

        hooksCheck();
        instantiateClasses();

        this.getServer().getPluginManager().registerEvents(leashMob, this);
        this.getServer().getPluginManager().registerEvents(leashPlayer, this);

        this.getCommand("tether").setExecutor(commands);
        this.getCommand("tether").setTabCompleter(tabCompleter);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Tether has been disabled!");
    }

    //Checks which hooks are enabled.
    private void hooksCheck() {
        //GriefPrevention check.
        if (this.getConfig().getBoolean("hooks.griefprevention")) {
            if (this.getServer().getPluginManager().getPlugin("GriefPrevention") == null) {
                this.getLogger().log(Level.WARNING, "GriefPrevention hook set to true in config, but " +
                        "the plugin does not exist on the server. The hook will not work!");
            } else {
                enableHooks[0] = true;
            }
        }

        //Towny check.
        if (this.getConfig().getBoolean("hooks.towny")) {
            if (this.getServer().getPluginManager().getPlugin("Towny") == null) {
                this.getLogger().log(Level.WARNING, "Towny hook set to true in config, but " +
                        "the plugin does not exist on the server. The hook will not work!");
            } else {
                enableHooks[1] = true;
            }
        }

        //Lands check.
        if (this.getConfig().getBoolean("hooks.lands")) {
            if (this.getServer().getPluginManager().getPlugin("Lands") == null) {
                this.getLogger().log(Level.WARNING, "Lands hook set to true in config, but " +
                        "the plugin does not exist on the server. The hook will not work!");
            } else {
                enableHooks[2] = true;
            }
        }
    }

    private void instantiateClasses() {
        utils = new Utils(this);
        claimCheckManager = new ClaimCheckManager(this, enableHooks[0], enableHooks[1], enableHooks[2]);
        leashMob = new LeashMob(this, claimCheckManager);
        leashPlayer = new LeashPlayer(this, claimCheckManager);
        commands = new Commands(this);
        tabCompleter = new TabCompleter();
    }
}
