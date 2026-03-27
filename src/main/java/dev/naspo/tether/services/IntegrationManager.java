package dev.naspo.tether.services;

import dev.naspo.tether.Tether;
import dev.naspo.tether.integrations.WorldGuardIntegration;

// Manages integrations.
public class IntegrationManager {
    private Tether plugin;

    private WorldGuardIntegration worldGuard;

    public IntegrationManager(Tether plugin) {
        this.plugin = plugin;

        worldGuard = new WorldGuardIntegration(plugin);
    }

    public void enableIntegrations() {
        worldGuard.init();
    }

    public boolean canLeash() {
        // TODO: implement
    }
}
