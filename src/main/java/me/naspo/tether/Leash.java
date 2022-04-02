package me.naspo.tether;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

public class Leash implements Listener {

    Tether plugin;
    Leash(Tether plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        if (!(event.getRightClicked() instanceof LivingEntity)) {
            return;
        }
        LivingEntity clicked = (LivingEntity) event.getRightClicked();

        if (clicked instanceof Player) {
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
