package dev.naspo.tether.integrations;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import dev.naspo.tether.Tether;

import java.util.HashSet;
import java.util.logging.Level;

// Responsible for managing hooks (integrations).
public class HookManager {

    private Tether plugin;
    private final HashSet<TempHook> enabledHooks;

    // WorldGuard stuff
    private FlagRegistry flagRegistry;
    private StateFlag leashFlag;

    public HookManager(Tether plugin) {
        this.plugin = plugin;
        this.enabledHooks = new HashSet<>();
    }

    /**
     * Checks the status of all hooks and stores them. If a toggleable hook is marked as enabled in the config
     * but the dependency for that hook does not exist, a warning will be logged to the console and
     * that hook will not be considered enabled.
     */
    public void initializeHooks() {
        for (TempHook hook : TempHook.values()) {
            boolean pluginExists = plugin.getServer().getPluginManager().getPlugin(hook.getPluginName()) != null;

            // If it's a toggleable hook, check if it's marked as enabled.
            if (hook.getConfigKey() != null) {
                if (plugin.getConfig().getBoolean("hooks." + hook.getConfigKey())) {
                    // If the hook's dependency is null, log a warning.
                    if (!pluginExists) {
                        plugin.getLogger().log(Level.WARNING, hook.getPluginName() + " hook set to true in config, " +
                                "but the plugin does not exist on the server. The hook will not work!");
                    } else {
                        // Otherwise everything is in order, add the hook to enabledHooks.
                        enabledHooks.add(hook);
                    }
                }
            } else {
                if (pluginExists) {
                    enabledHooks.add(hook);

                    if (hook.getPluginName().equals("WorldGuard")) {
                        initializeWorldGuardHook();
                    }
                }
            }
        }
    }

    // Returns the enabled status of a Hook.
    public boolean isHookEnabled(TempHook hook) {
        return enabledHooks.contains(hook);
    }

    // Initializes WorldGuard integration and registers Tether's custom "leash" flag.
    private void initializeWorldGuardHook() {
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            return;
        }

        this.flagRegistry = WorldGuard.getInstance().getFlagRegistry();

        String leashFlagText = "leash";
        try {
            StateFlag stateFlag = new StateFlag(leashFlagText, false);
            flagRegistry.register(stateFlag);
            this.leashFlag = stateFlag;
        } catch (FlagConflictException e) {
            // If there is a flag conflict, log that as an error to the console.
            Flag<?> flag = flagRegistry.get(leashFlagText);
            if (flag != null) {
                plugin.getLogger().warning("Couldn't register the 'leash' WorldGuard flag! It looks like another " +
                        "plugin registered it. WorldGuard integration with Tether will not work.");
            }
        }
    }
}
