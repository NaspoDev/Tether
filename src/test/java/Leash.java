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

        if (player.getInventory().getItemInMainHand().getType().equals(Material.LEAD)) {
            for (String s : plugin.getConfig().getStringList("blacklisted-mobs")) {
                if (clicked.getType().toString().toLowerCase().equalsIgnoreCase(s.toLowerCase())) {
                    event.setCancelled(true);
                    return;
                }
            }

            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    clicked.setLeashHolder(player);
                    ItemStack lead = new ItemStack(Material.LEAD, 1);
                    player.getInventory().removeItem(lead);
                }
            }, 1L);
        }
    }
}
