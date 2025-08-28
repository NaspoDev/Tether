package dev.naspo.tether.services.hookmanager;

import dev.naspo.tether.Tether;

import java.util.HashMap;
import java.util.logging.Level;

// Responsible for storing the status of hooks.
public class HookManager {

    private Tether plugin;
    private final HashMap<Hook, Boolean> hookEnabledStatuses;

    public HookManager(Tether plugin) {
        this.plugin = plugin;
        this.hookEnabledStatuses = new HashMap<>();
        performStartupHooksCheck();
    }

    /**
     * Checks the status of all hooks and stores them. If a hooks is marked as enabled in the config
     * but the dependency for that hook does not exist, a warning will be logged to the console and
     * that hook will not be considered enabled.
     */
    private void performStartupHooksCheck() {
        for (Hook hook : Hook.values()) {
            // If it's set as enabled in the config...
            if (plugin.getConfig().getBoolean("hooks." + hook.getConfigKey())) {
                // If the dependency for that hook exists is null, log a warning.
                if (plugin.getServer().getPluginManager().getPlugin(hook.getPluginName()) == null) {
                    plugin.getLogger().log(Level.WARNING, hook.getPluginName() + " hook set to true in config, " +
                            "but the plugin does not exist on the server. The hook will not work!");
                } else {
                    // Otherwise everything is in order, mark the hook as enabled.
                    hookEnabledStatuses.put(hook, true);
                }
            }
        }
    }

    public boolean isHookEnabled(Hook hook) {
        return hookEnabledStatuses.getOrDefault(hook, false);
    }
}
