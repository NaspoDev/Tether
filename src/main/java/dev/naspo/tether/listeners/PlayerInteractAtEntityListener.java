package dev.naspo.tether.listeners;

import dev.naspo.tether.services.LeashMobService;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractAtEntityListener implements Listener {
    private final LeashMobService leashMobService;

    public PlayerInteractAtEntityListener(LeashMobService leashMobService) {
        this.leashMobService = leashMobService;
    }

    // A more general event than PlayerLeashEntityEvent, used for leashing mobs that
    // are not leasable by default.
    @EventHandler
    private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        // Some event filtering before we can confirm it's a player trying to leash a mob.
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (!(event.getRightClicked() instanceof Mob)) return;

        Mob clickedMob = (Mob) event.getRightClicked();

        if (clickedMob.isLeashed()) return;
        if (clickedMob.getLeashHolder().equals(player)) {
            event.setCancelled(true);
            return;
        }

        // If they have a lead in their hand we can try to leash the mob.
        if (player.getInventory().getItemInMainHand().getType().equals(Material.LEAD)) {
            try {
                leashMobService.playerLeashMob(player, clickedMob);
            } catch (Exception e) {
                // Event needs to be cancelled if this fails.
                event.setCancelled(true);
                // Send the player a message in accordance of the reason for failure.
                e.
            }
        }
    }
}
