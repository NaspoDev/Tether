package dev.naspo.tether.leash;

import dev.naspo.tether.core.Tether;
import dev.naspo.tether.core.Utils;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class LeashMob implements Listener {
    private ClaimCheckManager claimCheckManager;
    private Tether plugin;

    public LeashMob(Tether plugin, ClaimCheckManager claimCheckManager) {
        this.plugin = plugin;
        this.claimCheckManager = claimCheckManager;
    }

    // A more general event than PlayerLeashEntityEvent, used for leashing mobs that
    // are not leasable by default.
    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        LivingEntity clicked;

        if (event.getRightClicked() instanceof LivingEntity) {
            // If it was a player that was right-clicked, return.
            if (event.getRightClicked() instanceof Player) {
                return;
            }
            // Capture the clicked entity.
            clicked = (LivingEntity) event.getRightClicked();

            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                return;
            }

            // If the mob is already leashed cancel the event.
            if (clicked.isLeashed()) {
                if (clicked.getLeashHolder().equals(player)) {
                    event.setCancelled(true);
                }
                return;
            }

            // If they have a lead in their hand...
            if (player.getInventory().getItemInMainHand().getType().equals(Material.LEAD)) {
                // Permission check.
                if (!(player.hasPermission("tether.use"))) {
                    return;
                }

                // Claim checks.
                if (!(claimCheckManager.canLeashMob(clicked, player))) {
                    event.setCancelled(true);
                    player.sendMessage(Utils.chatColor(Utils.prefix + plugin.getConfig().getString(
                            "messages.in-claim-deny-mob")));
                    return;
                }

                // Checking if clicked entity passes blacklist/whitelist check.
                if (isEntityRestricted(clicked)) {
                    return;
                }

                // Keep track of the player's leads, prevents duping.
                int leads;
                leads = player.getInventory().getItemInMainHand().getAmount();

                // Leashing the mob.
                // The actual leashing process has to run in a scheduler with a slight delay,
                // due to the way the event works.
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        clicked.setLeashHolder(player);

                        // If a lead was not removed from the player's inventory, remove one.
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

    // Specific leash event to apply blacklist/whitelist to mobs that are leashable
    // in the base game. This is not possible to catch with the PlayerInteractAtEntityEvent.
    @EventHandler
    public void onLeash(PlayerLeashEntityEvent event) {
        Entity entity = event.getEntity();

        // Checking if clicked entity passes blacklist/whitelist check.
        if (isEntityRestricted(entity)) {
            event.setCancelled(true);
        }
    }

    // Checks the whitelist or blacklist to see whether the entity is restricted from being leashed or not.
    private boolean isEntityRestricted(Entity entity) {
        // Use whitelist check.
        // If whitelist is set to be used over blacklist, check the whitelist only, else use blacklist.
        if (plugin.getConfig().getBoolean("use-whitelist-over-blacklist")) {
            // Whitelist check.
            // Getting whitelist values and converting all to uppercase.
            List<String> whitelist = plugin.getConfig().getStringList("whitelisted-mobs")
                    .stream().map(String::toUpperCase).collect(Collectors.toList());

            if (!whitelist.contains(entity.getType().name())) {
                return true;
            }
        } else {
            // Blacklist check.
            // Getting blacklist values and converting all to uppercase.
            List<String> blacklist = plugin.getConfig().getStringList("blacklisted-mobs")
                    .stream().map(String::toUpperCase).collect(Collectors.toList());

            if (blacklist.contains(entity.getType().name())) {
                return true;
            }
        }
        return false;
    }
}
