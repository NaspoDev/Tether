package me.naspo.tether.leash;

import me.naspo.tether.core.Tether;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.entity.EntityDismountEvent;

public class LeashPlayer implements Listener {

    String prefix;
    String leashedEscapable;
    String leashedNotEscapable;

    Tether plugin;
    public LeashPlayer(Tether plugin) {
        this.plugin = plugin;

        prefix = plugin.getConfig().getString("messages.prefix");
        leashedEscapable = (prefix + "You've been leashed! Press &6crouch &fto escape.");
        leashedNotEscapable = (prefix + "You've been leashed!");
    }

    @EventHandler
    public void interactConfigCheck(PlayerInteractAtEntityEvent event) {
        if (plugin.getConfig().getBoolean("player-leash.enabled")) {
            onInteract(event);
        }
    }

    @EventHandler
    public void dismountConfigCheck(EntityDismountEvent event) {
        if (plugin.getConfig().getBoolean("player-leash.enabled")) {
            if (plugin.getConfig().getBoolean("player-leash.escapable")) {
                onDismountEscapable(event);
            } else {
                onDismountNotEscapable(event);
            }
        }
    }

    private LivingEntity mob;

    public void onInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        if (event.getRightClicked() instanceof Player) {
            Player clicked = (Player) event.getRightClicked();

            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                return;
            }

            if (!(player.hasPermission("tether.use.players"))) {
                return;
            }

            if (clicked.getVehicle() != null) {
                if (!(clicked.getVehicle().equals(mob))) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix
                            + "You cannot leash players that are riding an entity."));
                    return;
                }
                mob.setHealth(0);
                return;
            }

            World world = clicked.getWorld();
            Location loc = clicked.getLocation();

            int leads;

            if (player.getInventory().getItemInMainHand().getType().equals(Material.LEAD)) {
                leads = player.getInventory().getItemInMainHand().getAmount();

                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        mob = (LivingEntity) world.spawnEntity(loc, EntityType.CHICKEN);
                        mob.setInvisible(true);
                        mob.setInvulnerable(true);
                        mob.setSilent(true);
                        mob.addPassenger(clicked);
                        mob.setLeashHolder(player);
                        if (plugin.getConfig().getBoolean("player-leash.message-on-leashed")) {
                            if (plugin.getConfig().getBoolean("player-leash.escapable")) {
                                clicked.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        leashedEscapable));
                            } else {
                                clicked.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        leashedNotEscapable));
                            }
                        }
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

    public void onDismountEscapable(EntityDismountEvent event) {
        if (event.getDismounted().equals(mob)) {
            mob.setHealth(0);
        }
    }

    public void onDismountNotEscapable(EntityDismountEvent event) {
        if (event.getDismounted().equals(mob)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeadBreak(EntityUnleashEvent event) {
        if (event.getEntity().equals(mob)) {
            if (mob.getHealth() > 0) {
                mob.setHealth(0);
            }
        }
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity().equals(mob)) {
            event.getDrops().clear();
        }
    }
}
