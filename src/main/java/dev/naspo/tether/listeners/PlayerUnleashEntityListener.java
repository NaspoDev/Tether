package dev.naspo.tether.listeners;

import dev.naspo.tether.Tether;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerUnleashEntityListener implements Listener {
    private final Tether plugin;

    public PlayerUnleashEntityListener(Tether plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) event.getEntity();

        /*
        Wait 1 tick for event outcome, then check if a lead was dropped. If not, drop one.
        Reason: PlayerUnleashEntityEvent doesn't trigger for mobs not leashable by default that are being transferred
        from a fence or mob to a player, so we trigger it manually. Therefore, we need to differentiate from a manual
        call and a natural one by checking if a lead was dropped.
         */
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Entity e : entity.getWorld().getNearbyEntities(entity.getLocation(), 1, 1, 1)) {
                if (e instanceof Item item) {
                    if (item.getItemStack().getType() == Material.LEAD) {
                        // A lead was dropped, return.
                        return;
                    }
                }
            }
            // A lead could not be found, so one must have never dropped. Drop one.
            entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(Material.LEAD, 1));
        }, 1L);
    }
}
