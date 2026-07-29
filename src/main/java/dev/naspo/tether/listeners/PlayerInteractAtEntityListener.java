package dev.naspo.tether.listeners;

import dev.naspo.tether.Tether;
import dev.naspo.tether.utils.ExceptionUtils;
import dev.naspo.tether.exceptions.NoPermissionException;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import dev.naspo.tether.services.LeashEntityService;
import dev.naspo.tether.services.LeashPlayerService;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.logging.Level;

// PlayerInteractAtEntityEvent is used as its more general than PlayerLeashEntityEvent, which is needed
// for handling mobs that are not leasable by default.
public class PlayerInteractAtEntityListener implements Listener {
    private final Tether plugin;
    private final LeashEntityService leashEntityService;
    private final LeashPlayerService leashPlayerService;

    public PlayerInteractAtEntityListener(
            Tether plugin,
            LeashEntityService leashEntityService,
            LeashPlayerService leashPlayerService) {
        this.plugin = plugin;
        this.leashEntityService = leashEntityService;
        this.leashPlayerService = leashPlayerService;
    }

    @EventHandler
    private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        // TODO: REMOVE THIS
        plugin.getLogger().info("Entity right clicked: " + event.getRightClicked().getType());
        // Important: The order of cases are important as we want to filter out Player and LeashHitch.
        switch (event.getRightClicked()) {
            case Player _ -> handlePlayerInteractAtPlayer(event);
            case LeashHitch _ -> handlePlayerInteractAtLeashHitch(event);
            default -> handlePlayerInteractAtEntity(event);
        }
    }

    private void handlePlayerInteractAtPlayer(PlayerInteractAtEntityEvent event) {
        if (!leashPlayerService.isPlayerLeashEnabled()) return;

        Player player = event.getPlayer();

        // Try to leash the player.
        try {
            leashPlayerService.playerLeashPlayer(player, (Player) event.getRightClicked());
        } catch (NoPermissionException ignored) {
        } catch (LeashException e) {
            ExceptionUtils.handleLeashException(player, event, e, plugin);
        }
    }

    /**
     * Handles PlayerInteractAtEntityEvent where the entity is NOT a Player or LeashHitch.
     *
     * @param event The PlayerInteractAtEntityEvent to handle.
     * @throws IllegalArgumentException if the event's entity is a Player, a LeashHitch, or is not Leashable.
     */
    private void handlePlayerInteractAtEntity(PlayerInteractAtEntityEvent event) throws IllegalArgumentException {
        // TODO: REMOVE THIS
        plugin.getLogger().info("we got to handlePlayerInteractAtEntity");
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (entity instanceof Player) throw new IllegalArgumentException("Event entity must not be a Player here.");
        if (entity instanceof LeashHitch) throw new IllegalArgumentException("Event entity must not be a LeashHitch here.");
        if (!(entity instanceof Leashable leashable)) throw new IllegalArgumentException("Event entity must be Leashable here.");

        // If they are holding shears, try to process the interaction.
        if (player.getInventory().getItemInMainHand().getType().equals(Material.SHEARS)) {
            try {
                leashEntityService.handleShearsInteract(player, entity);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, e.getMessage());
            } catch (LeashException e) {
                // If the interaction is denied, we must cancel the event.
                event.setCancelled(true);
                ExceptionUtils.handleLeashException(player, event, e, plugin);
            }
            return;
        }

        // If they are sneaking while right-clicking the entity, try leashing entities together.
        if (player.isSneaking()) {
            try {
                leashEntityService.handleSneakInteract(player, entity);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, e.getMessage());
            } catch (LeashException e) {
                ExceptionUtils.handleLeashException(player, event, e, plugin);
            }
            return;
        }

        // If they have a lead in their hand...
        if (player.getInventory().getItemInMainHand().getType().equals(Material.LEAD)) {
            // If the entity is already leashed by a player, return.
            // Explanation:
            // Either the leash holder is the player in this event, in which case other game events can handle unleashing the mob;
            // or it's leashed by another player, in which case the game can handle denying them the leash.
            if (leashable.isLeashed() && leashable.getLeashHolder() instanceof Player) return;

            // Try to leash the mob.
            try {
                leashEntityService.leashEntityToPlayer(player, entity);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, e.getMessage());
            } catch (NoPermissionException e) {
                event.setCancelled(true);
            } catch (LeashException e) {
                ExceptionUtils.handleLeashException(player, event, e, plugin);
                event.setCancelled(true);
            }
        }
    }

    private void handlePlayerInteractAtLeashHitch(PlayerInteractAtEntityEvent event) {
        try {
            leashEntityService.handleFenceLeashing(event.getPlayer(), event.getRightClicked().getLocation());
        } catch (LeashException e) {
            ExceptionUtils.handleLeashException(event.getPlayer(), event, e, plugin);
        }
    }
}
