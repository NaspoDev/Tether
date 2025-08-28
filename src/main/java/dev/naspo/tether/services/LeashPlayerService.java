package dev.naspo.tether.services;

import dev.naspo.tether.Tether;
import dev.naspo.tether.Utils;
import dev.naspo.tether.exceptions.NoPermissionException;
import dev.naspo.tether.exceptions.leashexception.LeashErrorType;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

// Responsible for logic related to leashing players.
public class LeashPlayerService {
    private final Tether plugin;
    private final ClaimCheckService claimCheckService;

    public LeashPlayerService(Tether plugin, ClaimCheckService claimCheckService) {
        this.plugin = plugin;
        this.claimCheckService = claimCheckService;
    }

    /**
     * Have the player leash a player if they are allowed.
     * Checks things like current land claims, player permissions, and more.
     *
     * @param player The player to be the leash holder.
     * @param target The player to be leashed.
     * @throws NoPermissionException if the player does not have permission.
     * @throws LeashException        when the leash operation fails for a given reason (LeashErrorType).
     */
    public void playerLeashPlayer(Player player, Player target) throws NoPermissionException, LeashException {
        // Permission check.
        if (!player.hasPermission("tether.use.players")) throw new NoPermissionException();

        // If the target player is a Citizens NPC, deny the leash.
        // Citizen NPCs of type "Player" should not be leashable as per Citizens.
        if (target.hasMetadata("NPC")) throw new LeashException(LeashErrorType.NPC_UNLEASHABLE);

        // If the target is already riding an entity...
        if (target.getVehicle() != null) {
            // If the entity they are riding is now the plugin's player leashing entity, don't allow the leash.
            if (!(target.getVehicle().hasMetadata("naspodev_tether_plugin"))) {
                throw new LeashException(LeashErrorType.TARGET_PLAYER_RIDING);
            }
            // If they are riding the plugin's player leashing entity (i.e hasMetadata("naspodev_tether_plugin"),
            // kill the entity.
            ((LivingEntity) target.getVehicle()).setHealth(0);
            return;
        }

        // Claim checks.
        if (!claimCheckService.canLeashPlayer(target, player))
            throw new LeashException(LeashErrorType.LAND_CLAIM_RESTRICTION);

        // Nesting check. Checks if the player is riding an entity.
        if (plugin.getConfig().getBoolean("player-leash.prevent-nesting")) {
            if (player.getVehicle() != null) {
                throw new LeashException(LeashErrorType.PREVENT_NESTING);
            }
        }

        // At this point we can actually leash the player.

        World world = target.getWorld();
        Location loc = target.getLocation();

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
                mob.addPassenger(target);
                mob.setLeashHolder(player);

                // If players should receive a message upon being leashed, send them the appropriate message
                // based on whether player leashing is set to escapable or not.
                if (plugin.getConfig().getBoolean("player-leash.message-on-leashed")) {
                    if (plugin.getConfig().getBoolean("player-leash.escapable")) {
                        target.sendMessage(Utils.chatColor(Utils.getPrefix(plugin) +
                                plugin.getConfig().getString("messages.player-leashed-escapable")));
                    } else {
                        target.sendMessage(Utils.chatColor(Utils.getPrefix(plugin) +
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

    // Checks if the mob being dismounted is the plugin's, then sets its health to 0.
    public void onDismountEscapable(EntityDismountEvent event) {
        if (event.getDismounted().hasMetadata("naspodev_tether_plugin")) {
            ((LivingEntity) event.getDismounted()).setHealth(0);
        }
    }

    // Checks if the mob being dismounted is the plugin's, then cancels the event.
    public void onDismountNotEscapable(EntityDismountEvent event) {
        if (event.getDismounted().hasMetadata("naspodev_tether_plugin")) {
            event.setCancelled(true);
        }
    }
}
