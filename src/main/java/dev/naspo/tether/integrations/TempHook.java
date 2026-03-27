package dev.naspo.tether.integrations;

// Represents hooks which are optional, and enabled in the config.
public enum TempHook {
    GRIEF_PREVENTION("GriefPrevention", "griefprevention"),
    TOWNY("Towny", "towny"),
    LANDS("Lands", "lands"),
    GRIEF_DEFENDER("GriefDefender", "griefdefender"),
    RESIDENCE("Residence", "residence"),
    WORLD_GUARD("WorldGuard", null);

    private final String pluginName;
    private final String configKey; // should be null for hooks not togglable in the config.

    TempHook(String pluginName, String configKey) {
        this.pluginName = pluginName;
        this.configKey = configKey;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getConfigKey() {
        return configKey;
    }
}