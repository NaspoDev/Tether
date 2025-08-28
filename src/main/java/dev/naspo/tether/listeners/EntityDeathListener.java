package dev.naspo.tether.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {

    // Clear drops when the mob dies.
    @EventHandler
    private void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity().hasMetadata("naspodev_tether_plugin")) {
            event.getDrops().clear();
        }
    }
}
