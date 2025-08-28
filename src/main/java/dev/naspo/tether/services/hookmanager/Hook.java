package dev.naspo.tether.services.hookmanager;

public enum Hook {
    GRIEF_PREVENTION("GriefPrevention", "griefprevention"),
    TOWNY("Towny", "towny"),
    LANDS("Lands", "lands"),
    GRIEF_DEFENDER("GriefDefender", "griefdefender");

    private final String pluginName;
    private final String configKey;

    Hook(String pluginName, String configKey) {
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