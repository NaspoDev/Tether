package me.naspo.tether;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.entity.EntityDismountEvent;

public class Leash implements Listener {

    Tether plugin;
    Leash(Tether plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event, EntityDismountEvent dismountEvent) {
        Player player = event.getPlayer();

        //Mob
        if (event.getRightClicked() instanceof LivingEntity) {
            LivingEntity clicked = (LivingEntity) event.getRightClicked();

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
            return;
        }

        //Player
        if (event.getRightClicked() instanceof Player) {
            if (plugin.getConfig().getBoolean("PlayerLeash")) {
                Player clicked = (Player) event.getRightClicked();

                if (!(player.hasPermission("tether.use.players"))) {
                    return;
                }
                if (clicked.getVehicle() != null) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
                            .getString("messages.prefix") + ""));
                    return;
                }

                World world = clicked.getWorld();
                Location loc = clicked.getLocation();

                LivingEntity pig = (LivingEntity) world.spawnEntity(loc, EntityType.PIG);
                pig.setInvisible(true);
                pig.addPassenger(clicked);

                if (dismountEvent.getDismounted().equals(pig)) {
                    event.setCancelled(true);
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

        /*if (!(event.getRightClicked() instanceof LivingEntity)) {
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
        }*/
    }

}
