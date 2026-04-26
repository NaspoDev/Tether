package dev.naspo.tether.listeners;

import dev.naspo.tether.Tether;
import dev.naspo.tether.utils.ExceptionUtils;
import dev.naspo.tether.utils.Utils;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import dev.naspo.tether.services.LeashMobService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractListener implements Listener {
    private final Tether plugin;
    private final LeashMobService leashMobService;

    public PlayerInteractListener(Tether plugin, LeashMobService leashMobService) {
        this.plugin = plugin;
        this.leashMobService = leashMobService;
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        // Ensuring it's a right-click on a fence with the main hand.
        // Used for fence post functionality for mobs that are not leashable by default as fence post
        // functionality won't work for them.
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getClickedBlock().getType().name().toLowerCase().endsWith("fence")) return;
        try {
            leashMobService.handleFenceLeashing(event.getPlayer(), event.getClickedBlock().getLocation());
        } catch (LeashException e) {
            ExceptionUtils.handleLeashException(event.getPlayer(), event, e, plugin);
        }
    }
}
