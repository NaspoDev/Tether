package dev.naspo.tether;

import dev.naspo.tether.commands.Commands;
import dev.naspo.tether.commands.TabCompleter;
import dev.naspo.tether.listeners.*;
import dev.naspo.tether.services.IntegrationManager;
import dev.naspo.tether.services.LeashMobService;
import dev.naspo.tether.services.LeashPlayerService;
import org.bukkit.plugin.java.JavaPlugin;

public final class Tether extends JavaPlugin {
    private IntegrationManager integrationManager;
    private LeashMobService leashMobService;
    private LeashPlayerService leashPlayerService;

    // WorldGuard integration requires that custom flags are registered during onLoad(), i.e. before the plugin
    // is enabled. (I'm also just enabling all integrations here are this point so it's cleaner).
    @Override
    public void onLoad() {
        instantiateClasses();
        integrationManager.enableIntegrations();
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        this.getLogger().info("Tether has been enabled!");

        registerEvents();
        registerCommands();
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Tether has been disabled!");
    }

    private void instantiateClasses() {
        integrationManager = new IntegrationManager(this);
        leashMobService = new LeashMobService(this, integrationManager);
        leashPlayerService = new LeashPlayerService(this, integrationManager);
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new PlayerInteractAtEntityListener(this, leashMobService, leashPlayerService), this);
        this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(leashMobService, integrationManager), this);
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
