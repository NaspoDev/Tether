package dev.naspo.tether.services;

import dev.naspo.tether.Tether;
import dev.naspo.tether.integrations.Integration;
import dev.naspo.tether.integrations.IntegrationEnablePhase;
import dev.naspo.tether.integrations.standardintegrations.WorldGuardIntegration;
import dev.naspo.tether.integrations.toggleableintegrations.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

// Manages integrations.
public class IntegrationManager {
    private Tether plugin;

    private final List<Integration> integrations;

    public IntegrationManager(Tether plugin) {
        this.plugin = plugin;

        // Immutable list of integrations.
        integrations = initializeIntegrationClasses();
    }

    // Enables integrations that should be enabled during the onLoad plugin lifecycle phase.
    public void enableIntegrationsOnLoad() {
        for (Integration integration : integrations) {
            if (integration.getEnablePhase() == IntegrationEnablePhase.ON_LOAD) {
                integration.enable();
            }
        }
    }

    // Enables integrations that should be enabled during the onEnable plugin lifecycle phase.
    public void enableIntegrationsOnEnable() {
        for (Integration integration : integrations) {
            if (integration.getEnablePhase() == IntegrationEnablePhase.ON_ENABLE) {
                integration.enable();
            }
        }
    }

    /**
     * Checks if leashing is permitted by all enabled integrations.
     *
     * @param location The location where leashing would occur. (i.e. the location of a clicked LivingEntity or fence post).
     * @param player   The player trying to leash.
     * @return true if the player is permitted to leash at that location.
     */
    public boolean canLeash(Location location, Player player) {
        for (Integration integration : integrations) {
            if (integration.isEnabled()) {
                if (!integration.canLeash(location, player)) {
                    return false;
                }
            }
        }
        return true;
    }

    // Returns an immutable list of initialized integrations classes.
    // Important: Ordering of integrations here is important.
    // Checks are done in order, so integrations should be added in order of priority descending.
    private List<Integration> initializeIntegrationClasses() {
        List<Integration> list = new ArrayList<>();

        if (isPluginPresent("WorldGuard")) {
            list.add(new WorldGuardIntegration(plugin));
        }
        if (isPluginPresent("GriefPrevention")) {
            list.add(new GriefPreventionIntegration(plugin));
        }
        if (isPluginPresent("Towny")) {
            list.add(new TownyIntegration(plugin));
        }
        if (isPluginPresent("Lands")) {
            list.add(new LandsIntegration(plugin));
        }
        if (isPluginPresent("GriefDefender")) {
            list.add(new GriefDefenderIntegration(plugin));
        }
        if (isPluginPresent("Residence")) {
            list.add(new ResidenceIntegration(plugin));
        }

        // Return an immutable list by making a copy of our temporary mutable builder list.
        return List.copyOf(list);
    }

    private boolean isPluginPresent(String pluginName) {
        return plugin.getServer().getPluginManager().getPlugin(pluginName) != null;
    }
}
