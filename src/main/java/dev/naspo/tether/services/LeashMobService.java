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
import org.bukkit.entity.*;
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
     * @throws InvalidParameterException if the LivingEntity passed in is a Player.
     * @throws NoPermissionException     if the player does not have permission.
     * @throws LeashException            when the leash operation fails for a given reason (LeashErrorType).
     */
    public void playerLeashMob(Player player, LivingEntity entity) throws InvalidParameterException,
            NoPermissionException, LeashException {
        if (entity instanceof Player) throw new InvalidParameterException();

        // Blacklist/whitelist check.
        if (isEntityRestricted(entity)) throw new LeashException(LeashErrorType.MOB_RESTRICTED);

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

    /**
     * Deals with leashing mobs to and from a fence.
     *
     * @param player   The player that right-clicked the fence or leash hitch.
     * @param location The location of the fence or leash hitch.
     */
    public void handleFenceLeashing(Player player, Location location) {
        // Transfer mobs from fence to player:
        // First wait for the PlayerLeashEntityEvent to finish then set the player as the leash holder for the rest of
        // the mobs still leashed to the fence. (The mobs still leashed to the fence at that point would be mobs not
        // leashable by default).
        if (getMobsLeashedByPlayer(player).isEmpty() && !getMobsLeashedToFence(location).isEmpty()) {
            transferMobsFromFenceToPlayer(player, location);
            return;
        }

        // Leashing mobs to a fence:
        if (!getMobsLeashedByPlayer(player).isEmpty()) {
            transferMobsFromPlayerToFence(player, location);
        }
    }

    /**
     * Deals with sneak-interaction, specifically looks for leashing mobs together and will
     * do so if applicable.
     *
     * @param player The player who sneak-interacted with an entity.
     * @param entity The LivingEntity that was sneak-interacted with. (Not `Mob` because NPCs are supported).
     */
    public void handleSneakInteract(Player player, LivingEntity entity) {
        if (entity instanceof Player) return;
        if (entity.isLeashed() && entity.getLeashHolder().equals(player)) return;

        for (Mob mob : getMobsLeashedByPlayer(player)) {
            mob.setLeashHolder(entity);
        }
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

    private List<Mob> getMobsLeashedByPlayer(Player player) {
        List<Mob> leashedMobs = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof Mob mob) {
                if (mob.isLeashed() && mob.getLeashHolder() instanceof Player holder && holder.equals(player)) {
                    leashedMobs.add(mob);
                }
            }
        }
        return leashedMobs;
    }

    /**
     * @param location The location of the fence or leash hitch.
     * @return The list of mobs leashed to that fence.
     */
    private List<Mob> getMobsLeashedToFence(Location location) {
        List<Mob> leashedMobs = new ArrayList<>();

        // Find the leash hitch.
        LeashHitch leashHitch = null;
        for (Entity entity : location.getWorld().getNearbyEntities(location, 1, 1, 1)) {
            if (entity instanceof LeashHitch lh) {
                leashHitch = lh;
                break;
            }
        }

        // If there is a leash hitch, find all entities leashed to it.
        if (leashHitch != null) {
            for (Entity entity : leashHitch.getWorld().getNearbyEntities(leashHitch.getLocation(), 10, 10, 10)) {
                if (entity instanceof Mob mob) {
                    if (mob.isLeashed() && mob.getLeashHolder() instanceof LeashHitch holder && holder.equals(leashHitch)) {
                        leashedMobs.add(mob);
                    }
                }
            }
        }
        return leashedMobs;
    }

    private void transferMobsFromFenceToPlayer(Player player, Location fenceLocation) {
        List<Mob> mobs = getMobsLeashedToFence(fenceLocation);
        for (Mob mob : mobs) {
            mob.setLeashHolder(player);
        }
    }

    private void transferMobsFromPlayerToFence(Player player, Location fenceLocation) {
        List<Mob> leashedMobs = getMobsLeashedByPlayer(player);

        // Finding the leash hitch on the fence.
        LeashHitch leashHitch = null;
        for (Entity entity : fenceLocation.getWorld().getNearbyEntities(fenceLocation, 1, 1, 1)) {
            if (entity instanceof LeashHitch lh) {
                leashHitch = lh;
                break;
            }
        }

        // If there is no leash hitch we have to create one.
        if (leashHitch == null) {
            // The location that the hitch should be. Cloning as to not modify the fenceLocation value.
            // 0.5 is added to properly visually align the hitch.
            Location hitchLocation = fenceLocation.clone().add(0.5, 0.5, 0.5);
            leashHitch = (LeashHitch) fenceLocation.getWorld().spawnEntity(hitchLocation, EntityType.LEASH_KNOT);
            for (Mob mob : leashedMobs) {
                mob.setLeashHolder(leashHitch);
            }
        } else {
            for (Mob mob : leashedMobs) {
                mob.setLeashHolder(leashHitch);
            }
        }
    }
}
