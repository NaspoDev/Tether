package dev.naspo.tether.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerUnleashEntityListener implements Listener {

    @EventHandler
    private void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
        Bukkit.getServer().broadcastMessage("PlayerUnleashEntityEvent Fired");

        if (!(event.getEntity() instanceof LivingEntity)) return;
        Bukkit.getServer().broadcastMessage("The entity involved is a LivingEntity");

        LivingEntity entity = (LivingEntity) event.getEntity();
        Bukkit.getServer().broadcastMessage("Cancelling the event");
        event.setCancelled(true);
        Bukkit.getServer().broadcastMessage("Setting leash holder to null");
        entity.setLeashHolder(null);

        // TODO: I think i'd need to check if it'll duplicate with the manual lead dropping in LeashMobService
        Bukkit.getServer().broadcastMessage("dropping a lead");
        entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(Material.LEAD, 1));
    }
}
