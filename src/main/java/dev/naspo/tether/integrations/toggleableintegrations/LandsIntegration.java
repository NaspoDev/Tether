package dev.naspo.tether.integrations.toggleableintegrations;

import dev.naspo.tether.Tether;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class LandsIntegration extends ToggleableIntegration {

    public LandsIntegration(Tether tetherPlugin) {
        super(tetherPlugin, "Lands", "lands");
    }

    @Override
    protected boolean onEnable() {
        return false;
    }

    @Override
    public boolean canLeash(LivingEntity clicked, Player player) {
        return false;
    }
}
