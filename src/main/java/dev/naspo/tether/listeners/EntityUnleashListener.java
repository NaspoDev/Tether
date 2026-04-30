package dev.naspo.tether.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;

public class EntityUnleashListener implements Listener {

    @EventHandler
    private void onEntityUnleashEvent(EntityUnleashEvent event) {
        // If it's the plugin's entity for player leashing that was unleashed, kill it.
        if (event.getEntity().hasMetadata("naspodev_tether_plugin")) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            if (entity.getHealth() > 0) {
                entity.setHealth(0);
            }
        }
    }
}
