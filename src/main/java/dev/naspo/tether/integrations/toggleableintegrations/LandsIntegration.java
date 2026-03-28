package dev.naspo.tether.integrations.toggleableintegrations;

import dev.naspo.tether.Tether;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class LandsIntegration extends ToggleableIntegration {
    private me.angeschossen.lands.api.LandsIntegration landsAPI;

    public LandsIntegration(Tether tetherPlugin) {
        super(tetherPlugin, "Lands", "lands");
    }

    @Override
    protected boolean onEnable() {
        landsAPI = me.angeschossen.lands.api.LandsIntegration.of(tetherPlugin);
        return true;
    }

    @Override
    public boolean canLeash(LivingEntity clicked, Player player) {
        Land land = landsAPI.getArea(clicked.getLocation()).getLand();
        return land.getTrustedPlayers().contains(player.getUniqueId());
    }
}
