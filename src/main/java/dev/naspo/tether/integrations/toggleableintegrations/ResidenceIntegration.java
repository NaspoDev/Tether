package dev.naspo.tether.integrations.toggleableintegrations;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import dev.naspo.tether.Tether;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ResidenceIntegration extends ToggleableIntegration {
    private Residence residenceAPI;

    public ResidenceIntegration(Tether tetherPlugin) {
        super(tetherPlugin, "Residence", "residence");
    }

    @Override
    protected boolean onEnable() {
        residenceAPI = Residence.getInstance();
        return true;
    }

    @Override
    public boolean canLeash(LivingEntity clicked, Player player) {
        ClaimedResidence residence = residenceAPI.getResidenceManager().getByLoc(clicked.getLocation());

        if (residence != null) {
            ResidencePermissions perms = residence.getPermissions();
            return perms.playerHas(player, "leash", true);
        }
        return true;
    }
}
