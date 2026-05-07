package dev.naspo.tether.integrations.standardintegrations;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.naspo.tether.Tether;
import dev.naspo.tether.integrations.Integration;
import dev.naspo.tether.integrations.IntegrationEnablePhase;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;

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

        // Query for the applicable regions.
        ApplicableRegionSet applicableRegionSet = regionContainer.createQuery().getApplicableRegions(wgLocation);
        Set<ProtectedRegion> protectedRegions = applicableRegionSet.getRegions();

        // Query for the state of the default BUILD and INTERACT flags, and Tether's custom LEASH flag.
        // This is needed because Tether's LEASH flag should respect BUILD and INTERACT.
        boolean build = regionContainer.createQuery().testBuild(wgLocation, wgLocalPlayer);
        boolean interact = regionContainer.createQuery().testState(wgLocation, wgLocalPlayer, Flags.INTERACT);
        boolean leash = regionContainer.createQuery().testState(wgLocation, wgLocalPlayer, leashFlag);

        /*
        Note on calculating an outcome based on the state of different flags:

        As per WorldGuard definition, flags like INTERACT "are used in tandem with the BUILD flag - if the player can
        build, then these flags do not need to be checked (although they are still checked for DENY), so they
        are false by default." - comment in com.sk89q.worldguard.protection.flags.Flags

        This means that if BUILD is set to ALLOW (true), then we can assume that flags like INTERACT are also allowed
        and therefore shouldn't manually check them. (If we try and check these flags manually anyway, they will return false/DENY).

        If BUILD is set to DENY (false), then we can check to see if these other sub-flags like INTERACT are allowed.

        The reason that we needed to note this down for Tether is because Tether needs to do manual flag calculation
        for its custom LEASH flag.
         */

        // If protectedRegions is empty, then we are in the __global__ region.
        if (protectedRegions.isEmpty()) {
            // In the __global__ region, I am only checking for the BUILD flag.
            return build;
        }

        // protectedRegions is NOT empty, we are in a user defined region.

        if (build) {
            // If BUILD is allowed, then INTERACT is also assumed as per worldguard definitions, so just check LEASH.
            return leash;
        } else {
            // Otherwise if BUILD is not allowed, then check INTERACT, then LEASH.
            if (!interact) {
                return false;
            }
            return leash;
        }
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
