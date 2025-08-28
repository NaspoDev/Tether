package dev.naspo.tether;

import dev.naspo.tether.commandstuff.Commands;
import dev.naspo.tether.commandstuff.TabCompleter;
import dev.naspo.tether.leash.LeashPlayer;
import dev.naspo.tether.listeners.PlayerInteractAtEntityListener;
import dev.naspo.tether.listeners.PlayerInteractListener;
import dev.naspo.tether.listeners.PlayerLeashEntityListener;
import dev.naspo.tether.services.ClaimCheckService;
import dev.naspo.tether.services.LeashMobService;
import dev.naspo.tether.services.hookmanager.HookManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Tether extends JavaPlugin {
    private HookManager hookManager;
    private LeashMobService leashMobService;
    private ClaimCheckService claimCheckService;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        this.getLogger().info("Tether has been enabled!");

        instantiateClasses();
        registerEvents();
        registerCommands();
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Tether has been disabled!");
    }

    private void instantiateClasses() {
        hookManager = new HookManager(this); // will perform hook check.
        claimCheckService = new ClaimCheckService(this, hookManager);
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
