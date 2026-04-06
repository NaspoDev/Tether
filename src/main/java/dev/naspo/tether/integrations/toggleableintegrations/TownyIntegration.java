package dev.naspo.tether.integrations.toggleableintegrations;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import dev.naspo.tether.Tether;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class TownyIntegration extends ToggleableIntegration {
    private TownyAPI townyAPI;

    public TownyIntegration(final Tether tetherPlugin) {
        super(tetherPlugin, "Towny", "towny");
    }

    @Override
    protected boolean onEnable() {
        townyAPI = TownyAPI.getInstance();
        return true;
    }

    @Override
    public boolean canLeash(final Location location, final Player player) {
        final Town town;
        try {
            town = townyAPI.getTownBlock(location).getTown();
        } catch (final Exception e) {
            return true;
        }
        return town.getTrustedResidents().contains(player.getUniqueId());
    }
}
