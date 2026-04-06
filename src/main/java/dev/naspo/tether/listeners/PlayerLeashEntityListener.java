package dev.naspo.tether.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;

public class PlayerLeashEntityListener implements Listener {

    // Tether takes complete control of leashing LivingEntities, therefore this event should always be cancelled
    // if it's to do with a LivingEntity.
    @EventHandler
    private void onPlayerLeashEntity(final PlayerLeashEntityEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            event.setCancelled(true);
        }
    }
}
