package dev.naspo.tether.integrations.toggleableintegrations;

import dev.naspo.tether.Tether;
import me.angeschossen.lands.api.land.Area;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class LandsIntegration extends ToggleableIntegration {
    private me.angeschossen.lands.api.LandsIntegration landsAPI;

    public LandsIntegration(final Tether tetherPlugin) {
        super(tetherPlugin, "Lands", "lands");
    }

    @Override
    protected boolean onEnable() {
        landsAPI = me.angeschossen.lands.api.LandsIntegration.of(tetherPlugin);
        return true;
    }

    @Override
    public boolean canLeash(final Location location, final Player player) {
        final Area area = landsAPI.getArea(location);
        if (area != null) {
            final Land land = area.getLand();
            return land.getTrustedPlayers().contains(player.getUniqueId());
        }
        return true;
    }
}
