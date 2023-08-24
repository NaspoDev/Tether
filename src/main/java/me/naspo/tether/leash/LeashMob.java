package me.naspo.tether.leash;

import me.naspo.tether.core.Tether;
import me.naspo.tether.core.Utils;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class LeashMob implements Listener {
    private LivingEntity clicked;

    private ClaimCheckManager claimCheckManager;
    private Tether plugin;

    public LeashMob(Tether plugin, ClaimCheckManager claimCheckManager) {
        this.plugin = plugin;
        this.claimCheckManager = claimCheckManager;
    }

    // Leashing the mob, general event.
    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        if (event.getRightClicked() instanceof LivingEntity) {
            if (event.getRightClicked() instanceof Player) {
                return;
            }
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

            // Keeps track of the player's leads, prevents duping.
            int leads;
            // Actually leashing the mob.
            if (player.getInventory().getItemInMainHand().getType().equals(Material.LEAD)) {
                leads = player.getInventory().getItemInMainHand().getAmount();

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
}
