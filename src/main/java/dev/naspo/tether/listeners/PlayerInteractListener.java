package dev.naspo.tether.listeners;

import dev.naspo.tether.Tether;
import dev.naspo.tether.exceptions.ExceptionUtils;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import dev.naspo.tether.leash.LeashEntityService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractListener implements Listener {
    private final Tether plugin;
    private final LeashEntityService leashEntityService;

    public PlayerInteractListener(Tether plugin, LeashEntityService leashEntityService) {
        this.plugin = plugin;
        this.leashEntityService = leashEntityService;
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        // Check for fence leashing.
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getClickedBlock().getType().name().toLowerCase().endsWith("fence")) return;
        try {
            leashEntityService.handleFenceLeashing(event.getPlayer(), event.getClickedBlock().getLocation());
        } catch (LeashException e) {
            ExceptionUtils.handleLeashException(event.getPlayer(), event, e, plugin);
        }
    }
}
