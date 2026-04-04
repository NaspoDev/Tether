package dev.naspo.tether.services;

import dev.naspo.tether.Tether;
import dev.naspo.tether.integrations.Integration;
import dev.naspo.tether.integrations.standardintegrations.WorldGuardIntegration;
import dev.naspo.tether.integrations.toggleableintegrations.GriefDefenderIntegration;
import dev.naspo.tether.integrations.toggleableintegrations.GriefPreventionIntegration;
import dev.naspo.tether.integrations.toggleableintegrations.LandsIntegration;
import dev.naspo.tether.integrations.toggleableintegrations.ResidenceIntegration;
import dev.naspo.tether.integrations.toggleableintegrations.TownyIntegration;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

// Manages integrations.
public class IntegrationManager {

    private final List<Integration> integrations;

    public IntegrationManager(final Tether plugin) {

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

    public void enableIntegrations() {
        for (final Integration integration : integrations) {
            integration.enable();
        }
    }

    public boolean canLeash(final Location location, final Player player) {
        for (final Integration integration : integrations) {
            if (integration.isEnabled()) {
                if (!integration.canLeash(location, player)) {
                    return false;
                }
            }
        }
        return true;
    }
}
