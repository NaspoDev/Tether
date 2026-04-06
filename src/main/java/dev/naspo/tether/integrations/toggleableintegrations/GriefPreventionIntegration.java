package dev.naspo.tether.integrations.toggleableintegrations;

import dev.naspo.tether.Tether;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GriefPreventionIntegration extends ToggleableIntegration {
    private DataStore dataStore;

    public GriefPreventionIntegration(final Tether tetherPlugin) {
        super(tetherPlugin, "GriefPrevention", "griefprevention");
    }

    @Override
    protected boolean onEnable() {
        return true;
    }

    @Override
    public boolean canLeash(final Location location, final Player player) {
        final Claim claim = getDataStore().getClaimAt(location, true, null);
        // If there is a claim here, return true if the player has explicit permission or is ignoring claims.
        if (claim != null) {
            return claim.hasExplicitPermission(player.getUniqueId(), ClaimPermission.Access) ||
                    getDataStore().getPlayerData(player.getUniqueId()).ignoreClaims;
        }
        // Otherwise always return true if there is no claim.
        return true;
    }

    public DataStore getDataStore() {
        if (dataStore == null) {
            dataStore = GriefPrevention.instance.dataStore;
        }
        return dataStore;
    }
}
