package dev.naspo.tether.listeners;

import dev.naspo.tether.Tether;
import dev.naspo.tether.services.LeashPlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

public class EntityDismountListener implements Listener {
    private final Tether plugin;
    private final LeashPlayerService leashPlayerService;

    public EntityDismountListener(Tether plugin, LeashPlayerService leashPlayerService) {
        this.plugin = plugin;
        this.leashPlayerService = leashPlayerService;
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        // Player-leash check.
        // Since player leashing uses an invisible entity that the player mounts, we
        // concern ourselves with EntityDismountEvent.
        if (leashPlayerService.isPlayerLeashEnabled() && leashPlayerService.isPlayerLeashMob(event.getDismounted())) {
            if (leashPlayerService.isPlayerLeashEscapable()) {
                leashPlayerService.onDismountEscapable(event);
            } else {
                leashPlayerService.onDismountNotEscapable(event);
            }
        }
    }
}
