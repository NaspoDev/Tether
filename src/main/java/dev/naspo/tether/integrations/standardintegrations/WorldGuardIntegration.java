package dev.naspo.tether.integrations.standardintegrations;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
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

    public WorldGuardIntegration(final Tether tetherPlugin) {
        super(tetherPlugin, "WorldGuard");
    }

    @Override
    protected boolean onEnable() {
        this.worldGuardAPI = WorldGuard.getInstance();
        this.flagRegistry = worldGuardAPI.getFlagRegistry();
        return registerLeashFlag();
    }

    @Override
    public boolean canLeash(final Location location, final Player player) {
        // WorldGuard uses their own custom Player, Location, and World objects, so I am converting them here.
        final LocalPlayer wgLocalPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        final com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
        final com.sk89q.worldedit.world.World wgWorld = BukkitAdapter.adapt(location.getWorld());

        // If they have WorldGuard region bypass permission, return true.
        final boolean canBypass = worldGuardAPI.getPlatform().getSessionManager().hasBypass(wgLocalPlayer, wgWorld);
        if (canBypass) {
            return true;
        }

        // Region data can be accessed via the RegionContainer object.
        final RegionContainer regionContainer = worldGuardAPI.getPlatform().getRegionContainer();

        // Query the state of our custom leash StateFlag at the location for a player.
        return regionContainer.createQuery().testState(wgLocation, wgLocalPlayer, leashFlag);
    }

    /**
     * Registers Tether's custom "leash" flag.
     *
     * @return true if the flag was successfully registered.
     */
    private boolean registerLeashFlag() {
        final String LEASH_FLAG_STRING = "leash";

        try {
            final StateFlag stateFlag = new StateFlag(LEASH_FLAG_STRING, true);
            flagRegistry.register(stateFlag);
            this.leashFlag = stateFlag;
            return true;
        } catch (final FlagConflictException e) {
            // If there is a flag conflict, log that as an error to the console.
            final Flag<?> flag = flagRegistry.get(LEASH_FLAG_STRING);
            if (flag != null) {
                tetherPlugin.getLogger().warning("Couldn't register the 'leash' WorldGuard flag! It looks like another " +
                        "plugin registered it. WorldGuard integration with Tether will not work.");
            }
        }
        return false;
    }

}
