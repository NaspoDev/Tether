package dev.naspo.tether.listeners;

import dev.naspo.tether.services.LeashPlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {

    // Clear drops when the special invisible mob used for player leashing dies.
    @EventHandler
    private void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity().hasMetadata(LeashPlayerService.PLAYER_LEASH_MOB_METADATA_KEY)) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }
}
