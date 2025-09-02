package dev.naspo.tether.listeners;

import dev.naspo.tether.services.LeashMobService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;

public class PlayerLeashEntityListener implements Listener {
    private final LeashMobService leashMobService;

    public PlayerLeashEntityListener(LeashMobService leashMobService) {
        this.leashMobService = leashMobService;
    }

    // Tether takes complete control of leashing, therefore this event should always be cancelled.
    @EventHandler
    private void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        event.setCancelled(true);

    }
}
