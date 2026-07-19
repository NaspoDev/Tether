package dev.naspo.tether.listeners;

import dev.naspo.tether.DefaultLeashableMobsKt;
import dev.naspo.tether.Tether;
import dev.naspo.tether.utils.ExceptionUtils;
import dev.naspo.tether.exceptions.NoPermissionException;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import dev.naspo.tether.services.LeashMobService;
import dev.naspo.tether.services.LeashPlayerService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

// PlayerInteractAtEntityEvent is used as its more general than PlayerLeashEntityEvent, which is needed
// for handling mobs that are not leasable by default.
public class PlayerInteractAtEntityListener implements Listener {
    private final Tether plugin;
    private final LeashMobService leashMobService;
    private final LeashPlayerService leashPlayerService;

    public PlayerInteractAtEntityListener(
            Tether plugin,
            LeashMobService leashMobService,
            LeashPlayerService leashPlayerService) {
        this.plugin = plugin;
        this.leashMobService = leashMobService;
        this.leashPlayerService = leashPlayerService;
    }

    @EventHandler
    private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        switch (event.getRightClicked()) {
            case Player _ -> handlePlayerInteractAtPlayer(event);
            // It's important that the LivingEntity check happens after the Player check, as we want to exclude
            // player from mob handling logic. But we still need to check for LivingEntity to include NPCs. (We
            // treat NPCs as mobs here).
            case LivingEntity _ -> handlePlayerInteractAtMob(event);
            case LeashHitch _ -> handlePlayerInteractAtLeashHitch(event);
            default -> {}
        }
    }

    private void handlePlayerInteractAtPlayer(PlayerInteractAtEntityEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
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

    private void handlePlayerInteractAtMob(PlayerInteractAtEntityEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        // We cast to LivingEntity because we also treat NPCs as mobs here.
        LivingEntity entity = (LivingEntity) event.getRightClicked();
        Player player = event.getPlayer();

        // If they are holding shears, try to process the interaction.
        if (player.getInventory().getItemInMainHand().getType().equals(Material.SHEARS)) {
            try {
                leashMobService.handleShearsInteract(player, entity);
            } catch (LeashException e) {
                // If the interaction is denied, we must cancel the event.
                event.setCancelled(true);
                ExceptionUtils.handleLeashException(player, event, e, plugin);
            }
            return;
        }

        // If they are sneaking while right-clicking the mob, try leashing mobs together.
        if (player.isSneaking()) {
            try {
                leashMobService.handleSneakInteract(player, entity);
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
            if (entity.isLeashed() && entity.getLeashHolder() instanceof Player) return;

            // Try to leash the mob.
            try {
                leashMobService.playerLeashMob(player, entity);
            } catch (NoPermissionException e) {
                event.setCancelled(true);
            } catch (LeashException e) {
                ExceptionUtils.handleLeashException(player, event, e, plugin);
            }
        }
    }

    private void handlePlayerInteractAtLeashHitch(PlayerInteractAtEntityEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        try {
            leashMobService.handleFenceLeashing(event.getPlayer(), event.getRightClicked().getLocation());
        } catch (LeashException e) {
            ExceptionUtils.handleLeashException(event.getPlayer(), event, e, plugin);
        }
    }
}
