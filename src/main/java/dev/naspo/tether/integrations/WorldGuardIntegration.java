package dev.naspo.tether.integrations;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import dev.naspo.tether.Tether;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class WorldGuardIntegration extends Integration {
    private FlagRegistry flagRegistry;
    private StateFlag leashFlag;

    public WorldGuardIntegration(Tether tetherPlugin) {
        super(tetherPlugin, "WorldGuard");
    }

    @Override
    protected boolean onEnable() {
        this.flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        return registerLeashFlag();
    }

    @Override
    public boolean canLeash(LivingEntity clicked, Player player) {
        // TODO: implement
        return false;
    }

    /**
     * Registers Tether's custom "leash" flag.
     *
     * @return true if the flag was successfully registered.
     */
    private boolean registerLeashFlag() {
        String leashFlagText = "leash";

        try {
            StateFlag stateFlag = new StateFlag(leashFlagText, false);
            flagRegistry.register(stateFlag);
            this.leashFlag = stateFlag;
            return true;
        } catch (FlagConflictException e) {
            // If there is a flag conflict, log that as an error to the console.
            Flag<?> flag = flagRegistry.get(leashFlagText);
            if (flag != null) {
                tetherPlugin.getLogger().warning("Couldn't register the 'leash' WorldGuard flag! It looks like another " +
                        "plugin registered it. WorldGuard integration with Tether will not work.");
            }
        }
        return false;
    }

}
