package dev.naspo.tether.services;

import dev.naspo.tether.Tether;
import dev.naspo.tether.exceptions.NoPermissionException;
import dev.naspo.tether.exceptions.leashexception.LeashErrorType;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Responsible for logic related to leashing mobs.
public class LeashMobService {
    private final Tether plugin;
    private final ClaimCheckService claimCheckService;

    public LeashMobService(Tether plugin, ClaimCheckService claimCheckService) {
        this.plugin = plugin;
        this.claimCheckService = claimCheckService;
    }

    /**
     * Have the player leash a mob if they are allowed.
     * Checks things like current land claims, player permissions, and more.
     *
     * @param player The player to be the leash holder.
     * @param entity The non-player LivingEntity to be leashed. (Not `Mob` because NPCs are supported).
     */
    public void playerLeashMob(Player player, LivingEntity entity) throws InvalidParameterException,
            NoPermissionException, LeashException {
        if (entity instanceof Player) throw new InvalidParameterException();

        // Permission check.
        if (!player.hasPermission("tether.use")) throw new NoPermissionException();

        // Claim checks.
        if (!claimCheckService.canLeashMob(entity, player))
            throw new LeashException(LeashErrorType.LAND_CLAIM_RESTRICTION);

        // If the entity is a Citizens NPC, check if it can be leashed.
        if (entity.hasMetadata("NPC")) {
            net.citizensnpcs.api.npc.NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
            // If the NPC cannot be leashed, return.
            if (npc.data().get(NPC.Metadata.LEASH_PROTECTED, true)) {
                throw new LeashException(LeashErrorType.NPC_UNLEASHABLE);
            }
        }

        // Checking if clicked entity passes blacklist/whitelist check.
        if (isEntityRestricted(entity)) throw new LeashException(LeashErrorType.MOB_RESTRICTED);

        // Begin the leashing process.
        // Keep track of the player's leads, prevents duping.
        int leads;
        leads = player.getInventory().getItemInMainHand().getAmount();

        // Leashing the mob.
        // The actual leashing process has to run in a scheduler with a slight delay,
        // due to the way the event works.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            entity.setLeashHolder(player);

            // If a lead was not removed from the player's inventory, remove one.
            ItemStack lead = new ItemStack(Material.LEAD, 1);
            if (player.getInventory().getItemInMainHand().getAmount() == (leads - 1)) {
                return;
            }
            player.getInventory().removeItem(lead);
        }, 1L);
    }

    public void handleFenceRightClick(PlayerInteractEvent playerInteractEvent) {
        Player player = playerInteractEvent.getPlayer();
        Block fence = playerInteractEvent.getClickedBlock();
        Location fenceLocation = fence.getLocation();

        // Waiting for the PlayerLeashEntityEvent (which would have fired at this point) to finish.
        // We need to wait for it to finish because it's outcome determines what extra stuff we need to do.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Find the mobs that the player may still be currently leashing.
            // Will be empty if all mobs they tried to attach to the fence are leashable by default, as the
            // PlayerLeashEntityEvent was handled by the game. This will only catch mobs that are still held on to
            // by the player because they are not leashable by default.
            List<Mob> leashedMobs = new ArrayList<>();
            for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                if (entity instanceof Mob mob) {
                    if (mob.isLeashed() && mob.getLeashHolder() instanceof Player holder && holder.equals(player)) {
                        leashedMobs.add(mob);
                    }
                }
            }

            // Finding the leash hitch on the fence.
            LeashHitch leashHitch = null;
            for (Entity entity : fenceLocation.getWorld().getNearbyEntities(fenceLocation, 5, 5, 5)) {
                if (entity instanceof LeashHitch lh) {
                    leashHitch = lh;
                    break;
                }
            }

            // If there is no leash hitch, that means they are trying to leash a mob that is not leashable by default
            // to the fence. So we have to create a leash hitch on the fence and set that as the leash holder for the mob.
            if (leashHitch == null) {
                // The location that the hitch should be. Cloning as to not modify the fenceLocation value. 0.5 is
                // added to properly visually align the hitch.
                Location hitchLocation = fenceLocation.clone().add(0.5, 0.5, 0.5);
                leashHitch = (LeashHitch) fence.getWorld().spawnEntity(hitchLocation, EntityType.LEASH_KNOT);
                for (Mob mob : leashedMobs) {
                    mob.setLeashHolder(leashHitch);
                }
            } else {
                // If there is a leash hitch present, then (some or all of) the mob(s) they are trying to leash to the
                // fence are leashable by default, and the game handled the leash hitch creation and attachment.
                // At this point, if there are any mobs in our leashedMobs list they are sure to be mobs that
                // are not leasheable by default, so we need to attach them to the hitch that the game created
                // for the other mobs.
                for (Mob mob : leashedMobs) {
                    mob.setLeashHolder(leashHitch);
                }
            }
        }, 1L);
    }

    // Checks the whitelist or blacklist to see whether the entity is restricted from being leashed or not.
    public boolean isEntityRestricted(Entity entity) {
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
