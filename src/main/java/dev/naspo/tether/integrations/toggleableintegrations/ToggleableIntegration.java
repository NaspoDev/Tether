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
}
