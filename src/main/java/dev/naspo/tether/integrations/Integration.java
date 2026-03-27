package dev.naspo.tether.integrations;

import dev.naspo.tether.Tether;

public abstract class Integration {
    protected final Tether tetherPlugin;
    protected final String pluginName;
    protected boolean enabled = false;

    public Integration(Tether tetherPlugin, String pluginName) {
        this.tetherPlugin = tetherPlugin;
        this.pluginName = pluginName;
    }

    /**
     * Initializes the integration.
     * Checks if the integration should enable, and runs logic to do so.
     *
     * @return true if the integration was enabled.
     */
    public boolean init() {
        if (isPluginPresent()) {
            enabled = onEnable();
            return enabled;
        }
        return false;
    }

    /**
     * The actual logic to enable the integration.
     *
     * @return true if the integration successfully enabled.
     */
    protected abstract boolean onEnable();

    private boolean isPluginPresent() {
        return tetherPlugin.getServer().getPluginManager().getPlugin(pluginName) != null;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
