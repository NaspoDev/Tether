package dev.naspo.tether.listeners;

import dev.naspo.tether.services.LeashMobService;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;

public class PlayerLeashEntityListener implements Listener {
    private final LeashMobService leashMobService;

    public PlayerLeashEntityListener(LeashMobService leashMobService) {
        this.leashMobService = leashMobService;
    }

    // Tether takes complete control of leashing LivingEntities, therefore this event should always be cancelled
    // if it's to do with a LivingEntity.
    @EventHandler
    private void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            event.setCancelled(true);
        }
    }
}
