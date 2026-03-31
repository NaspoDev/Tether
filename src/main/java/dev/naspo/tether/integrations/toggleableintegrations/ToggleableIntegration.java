package dev.naspo.tether.integrations.toggleableintegrations;

import dev.naspo.tether.Tether;
import dev.naspo.tether.integrations.Integration;

// An integration that is toggleable via the config (aka a hook).
public abstract class ToggleableIntegration extends Integration {
    private final String configKey;

    public ToggleableIntegration(Tether tetherPlugin, String pluginName, String configKey) {
        super(tetherPlugin, pluginName);
        this.configKey = configKey;
    }

    @Override
    public boolean enable() {
        if (isEnabledInConfig()) {
            if (isPluginPresent()) {
                enabled = onEnable();
                return enabled;
            }
            tetherPlugin.getLogger().warning(pluginName + " hook is set to true in config, but the plugin does " +
                    "not exist on the server. The hook will not work!");
        }
        return false;
    }

    /**
     * Checks if this integration is set to enabled in the config.
     * <br>
     * This method is not concerned with if the integration is actually enabled and running, that would be {@code isEnabled()}.
     * @return true if the integration is set to enabled in the config.
     */
    private boolean isEnabledInConfig() {
        return tetherPlugin.getConfig().getBoolean("hooks." + configKey);
    }
}
