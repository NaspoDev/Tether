package dev.naspo.tether.listeners;

import dev.naspo.tether.exceptions.leashexception.LeashErrorType;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import dev.naspo.tether.services.IntegrationManager;
import dev.naspo.tether.services.LeashMobService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractListener implements Listener {
    private final LeashMobService leashMobService;
    private final IntegrationManager integrationManager;

    public PlayerInteractListener(final LeashMobService leashMobService, final IntegrationManager integrationManager) {
        this.leashMobService = leashMobService;
        this.integrationManager = integrationManager;
    }

    @EventHandler
    private void onPlayerInteract(final PlayerInteractEvent event) {
        // Ensuring it's a right-click on a fence with the main hand.
        // Used for fence post functionality for mobs that are not leashable by default as fence post
        // functionality won't work for them.
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;
        if (!event.getClickedBlock().getType().name().toLowerCase().endsWith("fence")) return;
        final Location location = event.getClickedBlock().getLocation();
        final Player player = event.getPlayer();
        // Claim checks.
        if (!integrationManager.canLeash(location, player)) return;
        leashMobService.handleFenceLeashing(player, location);
    }
}
