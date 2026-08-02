package dev.naspo.tether;

import dev.naspo.tether.commands.Commands;
import dev.naspo.tether.commands.TabCompleter;
import dev.naspo.tether.config.ConfigAccessor;
import dev.naspo.tether.listeners.*;
import dev.naspo.tether.integrations.IntegrationManager;
import dev.naspo.tether.leash.LeashEntityService;
import dev.naspo.tether.leash.LeashPlayerService;
import org.bukkit.plugin.java.JavaPlugin;

public final class Tether extends JavaPlugin {
    private IntegrationManager integrationManager;
    private ConfigAccessor configAccessor;
    private LeashEntityService leashEntityService;
    private LeashPlayerService leashPlayerService;

    @Override
    public void onLoad() {
        instantiateClasses();
        // Some integrations need to be enabled during the onLoad lifecycle phase.
        integrationManager.enableIntegrationsOnLoad();
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        this.getLogger().info("Tether has been enabled!");

        integrationManager.enableIntegrationsOnEnable();

        registerEvents();
        registerCommands();
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Tether has been disabled!");
    }

    private void instantiateClasses() {
        integrationManager = new IntegrationManager(this);
        configAccessor = new ConfigAccessor(this);
        leashEntityService = new LeashEntityService(this, integrationManager);
        leashPlayerService = new LeashPlayerService(this, integrationManager, configAccessor);
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new PlayerInteractAtEntityListener(this, leashEntityService, leashPlayerService), this);
        this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(this, leashEntityService), this);
        this.getServer().getPluginManager().registerEvents(new EntityDeathListener(), this);
        this.getServer().getPluginManager().registerEvents(new EntityDismountListener(this, leashPlayerService), this);
        this.getServer().getPluginManager().registerEvents(new EntityUnleashListener(), this);
    }

    private void registerCommands() {
        this.getCommand("tether").setExecutor(new Commands(this, configAccessor));
        this.getCommand("tether").setTabCompleter(new TabCompleter());
    }
}
