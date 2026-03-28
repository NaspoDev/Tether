package dev.naspo.tether.integrations.toggleableintegrations;

import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import dev.naspo.tether.Tether;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GriefDefenderIntegration extends ToggleableIntegration {
    private Core griefDefenderAPI;

    public GriefDefenderIntegration(Tether tetherPlugin) {
        super(tetherPlugin, "GriefDefender", "griefdefender");
    }

    @Override
    protected boolean onEnable() {
        griefDefenderAPI = GriefDefender.getCore();
        return true;
    }

    @Override
    public boolean canLeash(LivingEntity clicked, Player player) {
        Claim claim = griefDefenderAPI.getClaimAt(clicked.getLocation());

        if (claim != null) {
            if (claim.isWilderness()) {
                return true;
            } else {
                return claim.isUserTrusted(player.getUniqueId(), TrustTypes.ACCESSOR);
            }
        }
        return true;
    }
}
