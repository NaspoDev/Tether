package dev.naspo.tether.services;

import dev.naspo.tether.Tether;
import dev.naspo.tether.integrations.Integration;
import dev.naspo.tether.integrations.IntegrationEnablePhase;
import dev.naspo.tether.integrations.standardintegrations.WorldGuardIntegration;
import dev.naspo.tether.integrations.toggleableintegrations.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

// Manages integrations.
public class IntegrationManager {
    private Tether plugin;

    private final List<Integration> integrations;

    public IntegrationManager(Tether plugin) {
        this.plugin = plugin;

        // Creating an immutable list of integrations (via List.of()).
        // Important: Ordering of integrations here is important.
        // Checks are done in order, so integrations should be added in order of priority descending.
        integrations = List.of(
                new WorldGuardIntegration(plugin),
                new GriefPreventionIntegration(plugin),
                new TownyIntegration(plugin),
                new LandsIntegration(plugin),
                new GriefDefenderIntegration(plugin),
                new ResidenceIntegration(plugin)
        );
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
}
