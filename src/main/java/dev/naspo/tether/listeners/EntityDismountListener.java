package dev.naspo.tether.listeners;

import dev.naspo.tether.Tether;
import dev.naspo.tether.config.ConfigAccessor;
import dev.naspo.tether.config.ConfigKeys;
import dev.naspo.tether.leash.LeashPlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

public class EntityDismountListener implements Listener {
    private final ConfigAccessor configAccessor;
    private final LeashPlayerService leashPlayerService;

    public EntityDismountListener(ConfigAccessor configAccessor, LeashPlayerService leashPlayerService) {
        this.configAccessor = configAccessor;
        this.leashPlayerService = leashPlayerService;
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        // Player-leash check.
        // Since player leashing uses an invisible entity that the player mounts, we
        // concern ourselves with EntityDismountEvent.
        if (configAccessor.get(ConfigKeys.PlayerLeash.INSTANCE.getEnabled()) &&
                leashPlayerService.isPlayerLeashMob(event.getDismounted())) {
            if (configAccessor.get(ConfigKeys.PlayerLeash.INSTANCE.getEscapable())) {
                leashPlayerService.onDismountEscapable(event);
            } else {
                leashPlayerService.onDismountNotEscapable(event);
            }
        }
    }
}
