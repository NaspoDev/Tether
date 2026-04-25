package dev.naspo.tether.integrations.toggleableintegrations;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import dev.naspo.tether.Tether;
import dev.naspo.tether.integrations.IntegrationEnablePhase;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class TownyIntegration extends ToggleableIntegration {
    private TownyAPI townyAPI;

    public TownyIntegration(Tether tetherPlugin) {
        super(tetherPlugin, "Towny", "towny");
    }

    @Override
    public IntegrationEnablePhase getEnablePhase() {
        return IntegrationEnablePhase.ON_ENABLE;
    }

    @Override
    protected boolean onEnable() {
        townyAPI = TownyAPI.getInstance();
        return true;
    }

    @Override
    public boolean canLeash(Location location, Player player) {
        Town town;
        try {
            town = townyAPI.getTownBlock(location).getTown();
        } catch (Exception e) {
            return true;
        }
        return town.getTrustedResidents().contains(player.getUniqueId());
    }
}
