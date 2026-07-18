package dev.naspo.tether.listeners;

import dev.naspo.tether.services.LeashPlayerService;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;

public class EntityUnleashListener implements Listener {

    @EventHandler
    private void onEntityUnleashEvent(EntityUnleashEvent event) {
        // If it's the plugin's entity for player leashing that was unleashed, kill it.
        if (event.getEntity().hasMetadata(LeashPlayerService.PLAYER_LEASH_MOB_METADATA_KEY)) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            if (entity.getHealth() > 0) {
                entity.setHealth(0);
            }
        }
    }
}
