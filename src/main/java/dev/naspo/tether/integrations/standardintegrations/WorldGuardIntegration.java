package dev.naspo.tether.integrations.standardintegrations;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.naspo.tether.Tether;
import dev.naspo.tether.integrations.Integration;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class WorldGuardIntegration extends Integration {
    private WorldGuard worldGuardAPI;
    private FlagRegistry flagRegistry;
    private StateFlag leashFlag;

    public WorldGuardIntegration(Tether tetherPlugin) {
        super(tetherPlugin, "WorldGuard");
    }

    @Override
    protected boolean onEnable() {
        this.worldGuardAPI = WorldGuard.getInstance();
        this.flagRegistry = worldGuardAPI.getFlagRegistry();
        return registerLeashFlag();
    }

    @Override
    public boolean canLeash(LivingEntity clicked, Player player) {
        // Region data can be accessed via the RegionContainer object.
        RegionContainer regionContainer = worldGuardAPI.getPlatform().getRegionContainer();

        // Query the state of our custom leash StateFlag at the clicked entity's location for a player.
        // (Note that conversions using WorldGuardPlugin and WorldGuard's BukkitAdapter are needed as WorldGuard
        // uses their own custom Player and Location objects).
         return regionContainer.createQuery().testState(
                BukkitAdapter.adapt(clicked.getLocation()),
                WorldGuardPlugin.inst().wrapPlayer(player),
                leashFlag);
    }

    /**
     * Registers Tether's custom "leash" flag.
     *
     * @return true if the flag was successfully registered.
     */
    private boolean registerLeashFlag() {
        final String LEASH_FLAG_STRING = "leash";

        try {
            StateFlag stateFlag = new StateFlag(LEASH_FLAG_STRING, true);
            flagRegistry.register(stateFlag);
            this.leashFlag = stateFlag;
            return true;
        } catch (FlagConflictException e) {
            // If there is a flag conflict, log that as an error to the console.
            Flag<?> flag = flagRegistry.get(LEASH_FLAG_STRING);
            if (flag != null) {
                tetherPlugin.getLogger().warning("Couldn't register the 'leash' WorldGuard flag! It looks like another " +
                        "plugin registered it. WorldGuard integration with Tether will not work.");
            }
        }
        return false;
    }

}
