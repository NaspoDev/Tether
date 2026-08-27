package dev.naspo.tether;

import dev.naspo.tether.commands.Commands;
import dev.naspo.tether.commands.TabCompleter;
import dev.naspo.tether.config.*;
import dev.naspo.tether.listeners.*;
import dev.naspo.tether.integrations.IntegrationManager;
import dev.naspo.tether.leash.entityleash.LeashEntityService;
import dev.naspo.tether.leash.LeashPlayerService;
import org.bukkit.plugin.java.JavaPlugin;

public class Tether extends JavaPlugin {
    private ConfigAccessor configAccessor;
    private IntegrationManager integrationManager;
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
        ConfigMigrationKt.migrateConfigGracefully(this);
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
        configAccessor = new ConfigAccessor(this);
        integrationManager = new IntegrationManager(this, configAccessor);
        leashEntityService = new LeashEntityService(this, configAccessor, integrationManager);
        leashPlayerService = new LeashPlayerService(this, integrationManager, configAccessor);
        ConfigKeys.INSTANCE.ensureInitialized();
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(
                new PlayerInteractAtEntityListener(this, configAccessor, leashEntityService, leashPlayerService), this);
        this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(configAccessor, leashEntityService, leashPlayerService, this), this);
        this.getServer().getPluginManager().registerEvents(new EntityDeathListener(), this);
        this.getServer().getPluginManager().registerEvents(new EntityDismountListener(configAccessor, leashPlayerService), this);
        this.getServer().getPluginManager().registerEvents(new EntityUnleashListener(), this);
        this.getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(configAccessor, leashPlayerService, this), this);
    }

    private void registerCommands() {
        this.getCommand("tether").setExecutor(new Commands(this, configAccessor));
        this.getCommand("tether").setTabCompleter(new TabCompleter());
    }
}
