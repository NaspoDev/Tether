package dev.naspo.tether.listeners;

import dev.naspo.tether.services.LeashMobService;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;

public class PlayerLeashEntityListener implements Listener {
    private final LeashMobService leashMobService;

    public PlayerLeashEntityListener(LeashMobService leashMobService) {
        this.leashMobService = leashMobService;
    }

    // Specific leash event to apply blacklist/whitelist to mobs that are leashable
    // in the base game. This is not possible to catch with the PlayerInteractAtEntityEvent.
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerLeash(PlayerLeashEntityEvent event) {
        Entity entity = event.getEntity();
        if (leashMobService.isEntityRestricted(entity)) {
            event.setCancelled(true);
        }
    }
}
