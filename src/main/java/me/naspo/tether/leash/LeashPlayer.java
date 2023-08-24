package me.naspo.tether.leash;

import me.naspo.tether.core.Tether;
import me.naspo.tether.core.Utils;
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

    private ClaimCheckManager claimCheckManager;
    private Tether plugin;

    public LeashPlayer(Tether plugin, ClaimCheckManager claimCheckManager) {
        this.plugin = plugin;
        this.claimCheckManager = claimCheckManager;
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
                    player.sendMessage(Utils.chatColor(Utils.prefix +
                            plugin.getConfig().getString("messages.cannot-leash-riding-player")));
                    return;
                }
                mob.setHealth(0);
                return;
            }

            //Claim checks.
            if (!(claimCheckManager.canLeashPlayer(clicked, player))) {
                event.setCancelled(true);
                player.sendMessage(Utils.chatColor(Utils.prefix + plugin.getConfig().getString(
                        "messages.in-claim-deny-player")));
                return;
            }

            //Leashing the player.
            if (plugin.getConfig().getBoolean("player-leash.prevent-nesting")) {
                if (player.getVehicle() != null) {
                    player.sendMessage(Utils.chatColor(Utils.prefix + plugin.getConfig().getString(
                            "messages.prevent-nesting")));
                    return;
                }
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
                                clicked.sendMessage(Utils.chatColor(Utils.prefix +
                                        plugin.getConfig().getString("messages.player-leashed-escapable")));
                            } else {
                                clicked.sendMessage(Utils.chatColor(Utils.prefix +
                                        "messages.player-leashed-not-escapable"));
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

    private void onDismountEscapable(EntityDismountEvent event) {
        if (event.getDismounted().equals(mob)) {
            mob.setHealth(0);
        }
    }

    private void onDismountNotEscapable(EntityDismountEvent event) {
        if (event.getDismounted().equals(mob)) {
            event.setCancelled(true);
        }
    }

    //Kill the mob on lead break.
    @EventHandler
    private void onLeadBreak(EntityUnleashEvent event) {
        if (event.getEntity().equals(mob)) {
            if (mob.getHealth() > 0) {
                mob.setHealth(0);
            }
        }
    }

    @EventHandler
    private void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity().equals(mob)) {
            event.getDrops().clear();
        }
    }

}
