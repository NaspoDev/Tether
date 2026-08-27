package dev.naspo.tether.listeners;

import dev.naspo.tether.Tether;
import dev.naspo.tether.config.ConfigAccessor;
import dev.naspo.tether.config.ConfigKeys;
import dev.naspo.tether.exceptions.ExceptionUtils;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import dev.naspo.tether.leash.entityleash.LeashEntityService;
import dev.naspo.tether.leash.LeashPlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractListener implements Listener {
    private final ConfigAccessor configAccessor;
    private final LeashEntityService leashEntityService;
    private final LeashPlayerService leashPlayerService;

    public PlayerInteractListener(ConfigAccessor configAccessor,
                                  LeashEntityService leashEntityService,
                                  LeashPlayerService leashPlayerService,
                                  Tether plugin) {
        this.configAccessor = configAccessor;
        this.leashEntityService = leashEntityService;
        this.leashPlayerService = leashPlayerService;
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        // Player leash - suppress leashed player check
        if (leashPlayerService.isPlayerLeashed(event.getPlayer()) &&
                configAccessor.get(ConfigKeys.PlayerLeash.INSTANCE.getSuppressLeashedPlayer())) {
            event.setCancelled(true);
        }

        // Fence leashing check
        if (isFenceLeashInteraction(event)) {
            try {
                leashEntityService.handleFenceLeashing(event.getPlayer(), event.getClickedBlock().getLocation());
            } catch (LeashException e) {
                ExceptionUtils.handleLeashException(event.getPlayer(), event, e, configAccessor);
            }
        }
    }

    /**
     * Helper method to check if the interaction in a {@link PlayerInteractEvent} is a fence leashing
     * related interaction.
     */
    private boolean isFenceLeashInteraction(PlayerInteractEvent event) {
        return event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                event.getHand() == EquipmentSlot.HAND &&
                event.getClickedBlock().getType().name().toLowerCase().endsWith("fence");
    }
}
