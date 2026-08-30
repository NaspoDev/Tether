package dev.naspo.tether.listeners;

import dev.naspo.tether.Tether;
import dev.naspo.tether.config.ConfigAccessor;
import dev.naspo.tether.config.ConfigKeys;
import dev.naspo.tether.exceptions.ExceptionUtils;
import dev.naspo.tether.exceptions.NoPermissionException;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import dev.naspo.tether.leash.entityleash.LeashEntityService;
import dev.naspo.tether.leash.LeashPlayerService;
import dev.naspo.tether.leash.playerleash.PlayerLeashManager;
import io.papermc.paper.entity.Leashable;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

// PlayerInteractAtEntityEvent is used as its more general than PlayerLeashEntityEvent, which is needed
// for handling entities that are not leasable by default.
public class PlayerInteractAtEntityListener implements Listener {
    private final Tether plugin;
    private final ConfigAccessor configAccessor;
    private final LeashEntityService leashEntityService;
    private final PlayerLeashManager playerLeashManager;

    public PlayerInteractAtEntityListener(
            Tether plugin,
            ConfigAccessor configAccessor,
            LeashEntityService leashEntityService,
            PlayerLeashManager playerLeashManager) {
        this.plugin = plugin;
        this.configAccessor = configAccessor;
        this.leashEntityService = leashEntityService;
        this.playerLeashManager = playerLeashManager;
    }

    @EventHandler
    private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        // Important: The order of cases are important as we want to filter out Player and LeashHitch.
        switch (event.getRightClicked()) {
            case Player _ -> handlePlayerInteractAtPlayer(event);
            case LeashHitch _ -> handlePlayerInteractAtLeashHitch(event);
            default -> handlePlayerInteractAtEntity(event);
        }
    }

    private void handlePlayerInteractAtPlayer(PlayerInteractAtEntityEvent event) {
        if (!configAccessor.get(ConfigKeys.PlayerLeash.INSTANCE.getEnabled())) return;

        Player interactor = event.getPlayer();
        Player target = (Player) event.getRightClicked();

        if (!playerLeashManager.isPlayerLeashed(target)) {
            // If the interactor is holding a lead, try to leash the player.
            if (interactor.getInventory().getItemInMainHand().getType().equals(Material.LEAD)) {
                try {
                    playerLeashManager.leashPlayer(target, interactor);
                } catch (NoPermissionException ignored) {
                } catch (LeashException e) {
                    ExceptionUtils.handleLeashException(interactor, event, e, configAccessor);
                }
            }
        } else {
            // If the interactor is the leash holder, unleash the player.
            if (playerLeashManager.getLeashHolderForPlayer(target) == interactor) {
                playerLeashManager.unleashPlayer(target);
            }
        }
    }

    private void handlePlayerInteractAtLeashHitch(PlayerInteractAtEntityEvent event) {
        try {
            leashEntityService.handleFenceLeashing(event.getPlayer(), event.getRightClicked().getLocation());
        } catch (LeashException e) {
            ExceptionUtils.handleLeashException(event.getPlayer(), event, e, configAccessor);
        }
    }

    /**
     * Handles PlayerInteractAtEntityEvent where the entity is {@link Leashable}.
     *
     * @param event The PlayerInteractAtEntityEvent to handle.
     * @throws IllegalArgumentException if the event's entity is not {@link Leashable}.
     */
    private void handlePlayerInteractAtEntity(PlayerInteractAtEntityEvent event) throws IllegalArgumentException {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

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
                ExceptionUtils.handleLeashException(player, event, e, configAccessor);
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
                ExceptionUtils.handleLeashException(player, event, e, configAccessor);
            }
            return;
        }

        // If they have a lead in their hand...
        if (player.getInventory().getItemInMainHand().getType().equals(Material.LEAD)) {
            // If the entity is already leashed by a player, return.
            // Explanation:
            // Either the leash holder is the player in this event, in which case other game events can handle unleashing the entity;
            // or it's leashed by another player, in which case the game can handle denying them the leash.
            if (leashable.isLeashed() && leashable.getLeashHolder() instanceof Player) return;

            // Try to leash the entity.
            try {
                leashEntityService.leashEntityToPlayer(player, entity);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, e.getMessage());
            } catch (NoPermissionException e) {
                event.setCancelled(true);
            } catch (LeashException e) {
                ExceptionUtils.handleLeashException(player, event, e, configAccessor);
                event.setCancelled(true);
            }
        }
    }
}
