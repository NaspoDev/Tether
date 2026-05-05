package dev.naspo.tether.integrations.standardintegrations;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.naspo.tether.Tether;
import dev.naspo.tether.integrations.Integration;
import dev.naspo.tether.integrations.IntegrationEnablePhase;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardIntegration extends Integration {
    private WorldGuard worldGuardAPI;
    private FlagRegistry flagRegistry;
    private StateFlag leashFlag;

    public WorldGuardIntegration(Tether tetherPlugin) {
        super(tetherPlugin, "WorldGuard");
    }

    @Override
    public IntegrationEnablePhase getEnablePhase() {
        return IntegrationEnablePhase.ON_LOAD;
    }

    @Override
    protected boolean onEnable() {
        this.worldGuardAPI = WorldGuard.getInstance();
        this.flagRegistry = worldGuardAPI.getFlagRegistry();
        return registerLeashFlag();
    }

    @Override
    public boolean canLeash(Location location, Player player) {
        // WorldGuard uses their own custom Player, Location, and World objects, so I am converting them here.
        LocalPlayer wgLocalPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
        com.sk89q.worldedit.world.World wgWorld = BukkitAdapter.adapt(location.getWorld());

        // If they have WorldGuard region bypass permission, return true.
        boolean canBypass = worldGuardAPI.getPlatform().getSessionManager().hasBypass(wgLocalPlayer, wgWorld);
        if (canBypass) {
            return true;
        }

        // Region data can be accessed via the RegionContainer object.
        RegionContainer regionContainer = worldGuardAPI.getPlatform().getRegionContainer();

        // Query for the state of the default "interact" flag and Tether's custom "leash" flag.
        boolean interact = regionContainer.createQuery().testState(wgLocation, wgLocalPlayer, Flags.INTERACT);
        boolean leash = regionContainer.createQuery().testState(wgLocation, wgLocalPlayer, leashFlag);

        // First check the "interact" flag, as Tether's custom "leash" flag should respect the "interact" flag first.
        // If it's false, deny the leash.
        if (!interact) {
            return false;
        }

        // At this point the "interact" flag is true, now the leash operation depends on the state of the leash flag.
        return leash;
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
