package dev.naspo.tether.services;

import dev.naspo.tether.Tether;
import dev.naspo.tether.integrations.Integration;
import dev.naspo.tether.integrations.standardintegrations.WorldGuardIntegration;
import dev.naspo.tether.integrations.toggleableintegrations.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

// Manages integrations.
public class IntegrationManager {
    private Tether plugin;

    private final List<Integration> integrations;
    private final HashSet<Integration> enabledIntegrations;

    public IntegrationManager(Tether plugin) {
        this.plugin = plugin;

        integrations = Arrays.asList(
                new WorldGuardIntegration(plugin),
                new GriefPreventionIntegration(plugin),
                new TownyIntegration(plugin),
                new LandsIntegration(plugin),
                new GriefDefenderIntegration(plugin),
                new ResidenceIntegration(plugin)
        );

        enabledIntegrations = new HashSet<>();
    }

    public void enableIntegrations() {
        for (Integration integration : integrations) {
            if (integration.enable()) {
                enabledIntegrations.add(integration);
            }
        }
    }

    public boolean canLeash(LivingEntity clicked, Player player) {
        for (Integration integration : enabledIntegrations) {
            // Non-toggleable integrations get priority in the check (e.g. WorldGuard).
            if (!(integration instanceof ToggleableIntegration)) {
                if (!integration.canLeash(clicked, player)) {
                    return false;
                }
            }
        }

        for (Integration integration : enabledIntegrations) {
            // Now check toggleable integrations...
            if (integration instanceof ToggleableIntegration) {
                if (!integration.canLeash(clicked, player)) {
                    return false;
                }
            }
        }

        return true;
    }
}
