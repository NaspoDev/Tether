package dev.naspo.tether.leash;

import dev.naspo.tether.core.Tether;
import dev.naspo.tether.core.Utils;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.event.entity.EntityDismountEvent;

public class LeashPlayer implements Listener {

    private final ClaimCheckManager claimCheckManager;
    private final Tether plugin;

    public LeashPlayer(Tether plugin, ClaimCheckManager claimCheckManager) {
        this.plugin = plugin;
        this.claimCheckManager = claimCheckManager;
    }

    // Checks if player leashing is enabled. (Checks upon PlayerInteractAtEntityEvent).
    @EventHandler
    public void interactConfigCheck(PlayerInteractAtEntityEvent event) {
        if (plugin.getConfig().getBoolean("player-leash.enabled")) {
            onInteract(event);
        }
    }

    // Checks if players can escape being leashed. (Checks upon EntityDismountEvent).
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

    // Leashing the player, general event.
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

            // If the entity is a Citizens NPC, deny the leash.
            // Citizen NPCs of type "Player" should not be leashable as per Citizens.
            if (clicked.hasMetadata("NPC")) {
                return;
            }

            // If the player is already riding an entity, don't allow the leash.
            if (clicked.getVehicle() != null) {
                if (!(clicked.getVehicle().hasMetadata("naspodev_tether_plugin"))) {
                    player.sendMessage(Utils.chatColor(Utils.prefix +
                            plugin.getConfig().getString("messages.cannot-leash-riding-player")));
                    return;
                }
                // If they are riding an entity, but the entity is the plugin's player leashing entity
                // (i.e hasMetadata("naspodev_tether_plugin"),  kill the entity.
                ((LivingEntity) clicked.getVehicle()).setHealth(0);
                return;
            }

            // Claim checks.
            if (!(claimCheckManager.canLeashPlayer(clicked, player))) {
                event.setCancelled(true);
                player.sendMessage(Utils.chatColor(Utils.prefix + plugin.getConfig().getString(
                        "messages.in-claim-deny-player")));
                return;
            }

            // Nesting check. Checks if the player that clicked the other play is riding an entity.
            if (plugin.getConfig().getBoolean("player-leash.prevent-nesting")) {
                if (player.getVehicle() != null) {
                    player.sendMessage(Utils.chatColor(Utils.prefix + plugin.getConfig().getString(
                            "messages.prevent-nesting")));
                    return;
                }
            }

            World world = clicked.getWorld();
            Location loc = clicked.getLocation();

            // Keeps track of leads in the leasher's hand. (Prevents duping).
            int leads;

            // If they are holding a lead...
            if (player.getInventory().getItemInMainHand().getType().equals(Material.LEAD)) {
                leads = player.getInventory().getItemInMainHand().getAmount();

                // The actual leashing process has to run in a scheduler with a slight delay,
                // due to the way the event works.
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Spawn a chicken (set invisible, invulnerable, etc)
                    LivingEntity mob = (LivingEntity) world.spawnEntity(loc, EntityType.CHICKEN);
                    mob.setMetadata("naspodev_tether_plugin", new FixedMetadataValue(plugin, "_"));
                    mob.setInvisible(true);
                    mob.setInvulnerable(true);
                    mob.setSilent(true);
                    mob.addPassenger(clicked);
                    mob.setLeashHolder(player);

                    // If players should receive a message upon being leashed, send them the appropriate message
                    // based on whether player leashing is set to escapable or not.
                    if (plugin.getConfig().getBoolean("player-leash.message-on-leashed")) {
                        if (plugin.getConfig().getBoolean("player-leash.escapable")) {
                            clicked.sendMessage(Utils.chatColor(Utils.prefix +
                                    plugin.getConfig().getString("messages.player-leashed-escapable")));
                        } else {
                            clicked.sendMessage(Utils.chatColor(Utils.prefix +
                                    "messages.player-leashed-not-escapable"));
                        }
                    }

                    // If a lead wasn't removed from the leasher's inventory, remove one.
                    ItemStack lead = new ItemStack(Material.LEAD, 1);
                    if (player.getInventory().getItemInMainHand().getAmount() == (leads - 1)) {
                        return;
                    }
                    player.getInventory().removeItem(lead);
                }, 1L);
            }
        }
    }

    // Checks if the mob being dismounted is the plugin's, then sets its health to 0.
    private void onDismountEscapable(EntityDismountEvent event) {
        if (event.getDismounted().hasMetadata("naspodev_tether_plugin")) {
            ((LivingEntity) event.getDismounted()).setHealth(0);
        }
    }

    // Checks if the mob being dismounted is the plugin's, then cancels the event.
    private void onDismountNotEscapable(EntityDismountEvent event) {
        if (event.getDismounted().hasMetadata("naspodev_tether_plugin")) {
            event.setCancelled(true);
        }
    }

    //Kill the mob on lead break.
    @EventHandler
    private void onLeadBreak(EntityUnleashEvent event) {
        if (event.getEntity().hasMetadata("naspodev_tether_plugin")) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            if (entity.getHealth() > 0) {
                entity.setHealth(0);
            }
        }
    }

    // Clear drops when the mob dies.
    @EventHandler
    private void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity().hasMetadata("naspodev_tether_plugin")) {
            event.getDrops().clear();
        }
    }

}
