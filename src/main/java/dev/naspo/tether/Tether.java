package dev.naspo.tether;

import dev.naspo.tether.commandstuff.Commands;
import dev.naspo.tether.commandstuff.TabCompleter;
import dev.naspo.tether.listeners.*;
import dev.naspo.tether.services.ClaimCheckService;
import dev.naspo.tether.services.LeashMobService;
import dev.naspo.tether.services.LeashPlayerService;
import dev.naspo.tether.services.hookmanager.HookManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Tether extends JavaPlugin {
    private HookManager hookManager;
    private ClaimCheckService claimCheckService;
    private LeashMobService leashMobService;
    private LeashPlayerService leashPlayerService;

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
        leashPlayerService = new LeashPlayerService(this, claimCheckService);
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new PlayerInteractAtEntityListener(this, leashMobService, leashPlayerService), this);
        this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(leashMobService), this);
        this.getServer().getPluginManager().registerEvents(new PlayerLeashEntityListener(leashMobService), this);
        this.getServer().getPluginManager().registerEvents(new EntityDeathListener(), this);
        this.getServer().getPluginManager().registerEvents(new EntityDismountListener(this, leashPlayerService), this);
        this.getServer().getPluginManager().registerEvents(new EntityUnleashListener(), this);
    }

    private void registerCommands() {
        this.getCommand("tether").setExecutor(new Commands(this));
        this.getCommand("tether").setTabCompleter(new TabCompleter());
    }
}
