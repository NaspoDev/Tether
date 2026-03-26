package dev.naspo.tether.integration;

import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import dev.naspo.tether.Tether;

import java.util.HashMap;
import java.util.logging.Level;

// Responsible for managing hooks (integrations).
public class HookManager {

    private Tether plugin;
    private final HashMap<OptionalHook, Boolean> optionalHookEnabledStatuses;

    // WorldGuard stuff
    private FlagRegistry flagRegistry;
    private StateFlag leashFlag;

    public HookManager(Tether plugin) {
        this.plugin = plugin;
        this.optionalHookEnabledStatuses = new HashMap<>();
    }

    public void initializeHooks() {
        initializeOptionalHooks();
    }

    /**
     * Checks the status of all optional hooks and stores them. If a hooks is marked as enabled in the config
     * but the dependency for that hook does not exist, a warning will be logged to the console and
     * that hook will not be considered enabled.
     */
    private void initializeOptionalHooks() {
        for (OptionalHook hook : OptionalHook.values()) {
            // If it's set as enabled in the config...
            if (plugin.getConfig().getBoolean("hooks." + hook.getConfigKey())) {
                // If the hook's dependency is null, log a warning.
                if (plugin.getServer().getPluginManager().getPlugin(hook.getPluginName()) == null) {
                    plugin.getLogger().log(Level.WARNING, hook.getPluginName() + " hook set to true in config, " +
                            "but the plugin does not exist on the server. The hook will not work!");
                } else {
                    // Otherwise everything is in order, mark the hook as enabled.
                    optionalHookEnabledStatuses.put(hook, true);
                }
            }
        }
    }

    // Returns the enabled status of an OptionalHook.
    public boolean isHookEnabled(OptionalHook hook) {
        return optionalHookEnabledStatuses.getOrDefault(hook, false);
    }

    private void initializeWorldGuardHook() {

    }
}
