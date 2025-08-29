package dev.naspo.tether.listeners;

import dev.naspo.tether.services.LeashMobService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;

public class PlayerLeashEntityListener implements Listener {
    private final LeashMobService leashMobService;

    public PlayerLeashEntityListener(LeashMobService leashMobService) {
        this.leashMobService = leashMobService;
    }

    // This event get cancelled in all cases except for when it's a mob being leashed to another mob.
    // In all other cases Tether completely handles leashing.
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        Entity entity = event.getEntity();

        if (leashMobService.isEntityRestricted(entity)) {
            event.setCancelled(true);
        }

        if (!(event.getLeashHolder() instanceof Mob)) {
            event.setCancelled(true);
        }
    }
}
