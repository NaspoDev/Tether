package dev.naspo.tether.listeners;

import dev.naspo.tether.Tether;
import dev.naspo.tether.Utils;
import dev.naspo.tether.exceptions.NoPermissionException;
import dev.naspo.tether.exceptions.leashexception.LeashErrorType;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import dev.naspo.tether.services.LeashMobService;
import dev.naspo.tether.services.LeashPlayerService;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

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

    // A more general event than PlayerLeashEntityEvent, used for leashing mobs that
    // are not leasable by default.
    @EventHandler
    private void onPlayerInteractAtMob(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        // Some event filtering before we can confirm it's a player trying to leash a mob.
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (!(event.getRightClicked() instanceof LivingEntity entity)) return;

        if (entity.isLeashed()) return;
        if (entity.getLeashHolder().equals(player)) {
            event.setCancelled(true);
            return;
        }

        // If they have a lead in their hand we can try to leash the mob.
        if (player.getInventory().getItemInMainHand().getType().equals(Material.LEAD)) {
            try {
                leashMobService.playerLeashMob(player, entity);
            } catch (NoPermissionException e) {
                event.setCancelled(true);
            } catch (LeashException e) {
                // Only need to explicitly handle the LAND_CLAIM_RESTRICTION LeashException type.
                if (e.getType() == LeashErrorType.LAND_CLAIM_RESTRICTION) {
                    event.setCancelled(true);
                    player.sendMessage(Utils.chatColor(Utils.getPrefix(plugin) + plugin.getConfig().getString(
                            "messages.in-claim-deny-mob")));
                }
            }
        }
    }

    @EventHandler
    private void onPlayerInteractAtPlayer(PlayerInteractAtEntityEvent event) {
        // If player leashing is disabled, return.
        if (!plugin.getConfig().getBoolean("player-leash.enabled")) return;
        // Other event filtering...
        if (!(event.getRightClicked() instanceof Player)) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();

        // Try to leash the player.
        try {
            leashPlayerService.playerLeashPlayer(player, (Player) event.getRightClicked());
        } catch (NoPermissionException ignored) {
        } catch (LeashException e) {
            switch (e.getType()) {
                case TARGET_PLAYER_RIDING -> player.sendMessage(Utils.chatColor(Utils.getPrefix(plugin) +
                        plugin.getConfig().getString("messages.cannot-leash-riding-player")));
                case LAND_CLAIM_RESTRICTION -> {
                    event.setCancelled(true);
                    player.sendMessage(Utils.chatColor(Utils.getPrefix(plugin) + plugin.getConfig().getString(
                            "messages.in-claim-deny-player")));
                }
                case PREVENT_NESTING -> player.sendMessage(Utils.chatColor(Utils.getPrefix(plugin) +
                        plugin.getConfig().getString("messages.prevent-nesting")));
            }
        }
    }
}
