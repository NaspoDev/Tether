package dev.naspo.tether.integrations.toggleableintegrations;

import dev.naspo.tether.Tether;
import dev.naspo.tether.integrations.IntegrationEnablePhase;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GriefPreventionIntegration extends ToggleableIntegration {
    private DataStore dataStore;

    public GriefPreventionIntegration(Tether tetherPlugin) {
        super(tetherPlugin, "GriefPrevention", "griefprevention");
    }

    @Override
    public IntegrationEnablePhase getEnablePhase() {
        return IntegrationEnablePhase.ON_ENABLE;
    }

    @Override
    protected boolean onEnable() {
        dataStore = GriefPrevention.instance.dataStore;
        return true;
    }

    @Override
    public boolean canLeash(Location location, Player player) {
        Claim claim = dataStore.getClaimAt(location, true, null);
        // If there is a claim here, return true if the player has explicit permission or is ignoring claims.
        if (claim != null) {
            return claim.hasExplicitPermission(player.getUniqueId(), ClaimPermission.Access) ||
                    dataStore.getPlayerData(player.getUniqueId()).ignoreClaims;
        }
        // Otherwise always return true if there is no claim.
        return true;
    }
}
