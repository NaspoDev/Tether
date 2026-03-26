package dev.naspo.tether.integration;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import dev.naspo.tether.Tether;

// Manages WorldGuard integration.
public class WorldGuardIntegration {
    private final Tether plugin;
    private final FlagRegistry flagRegistry;
    private StateFlag leashFlag;

    public WorldGuardIntegration(Tether plugin) {
        this.plugin = plugin;
        flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        registerLeashFlag(flagRegistry);
    }

    // Register's Tether's custom WorldGuard "leash" flag.
    private void registerLeashFlag(FlagRegistry flagRegistry) {
        String leashFlagText = "leash";
        try {
            StateFlag stateFlag = new StateFlag(leashFlagText, false);
            flagRegistry.register(stateFlag);
            leashFlag = stateFlag;
        } catch (FlagConflictException e) {
            // If there is a flag conflict, log that as an error to the console.
            Flag<?> flag = flagRegistry.get(leashFlagText);
            if (flag != null) {
                plugin.getLogger().severe("Couldn't register the 'leash' WorldGuard flag! " +
                        "It looks like another plugin registered it." + "\nWorldGuard integration with Tether will not work.");
            }
        }
    }

    public boolean canLeash()
}
