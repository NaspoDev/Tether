package me.naspo.tether.leash;

import me.naspo.tether.core.Tether;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class LeashMob implements Listener {

    Tether plugin;
    public LeashMob(Tether plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        if (event.getRightClicked() instanceof LivingEntity) {
            LivingEntity clicked = (LivingEntity) event.getRightClicked();

            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                return;
            }

            if (clicked.isLeashed()) {
                if (clicked.getLeashHolder().equals(player)) {
                    event.setCancelled(true);
                }
                return;
            }

            if (!(player.hasPermission("tether.use"))) {
                return;
            }

            int leads;

            if (player.getInventory().getItemInMainHand().getType().equals(Material.LEAD)) {
                leads = player.getInventory().getItemInMainHand().getAmount();

                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        clicked.setLeashHolder(player);
                        ItemStack lead = new ItemStack(Material.LEAD, 1);
                        if (player.getInventory().getItemInMainHand().getAmount() == (leads - 1)) {
                            return;
                        }
                        player.getInventory().removeItem(lead);
                    }
                }, 1L);
            }
        }
    }
}
