package dev.naspo.tether.leash;

import dev.naspo.tether.Tether;
import dev.naspo.tether.config.*;
import dev.naspo.tether.exceptions.NoPermissionException;
import dev.naspo.tether.exceptions.leashexception.LeashErrorType;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import dev.naspo.tether.integrations.IntegrationManager;
import io.papermc.paper.entity.Leashable;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for leashing logic for any {@link Leashable} that is not a {@link Player}
 *
 * @see LeashPlayerService
 */
public class LeashEntityService {
    private final Tether plugin;
    private ConfigAccessor configAccessor;
    private final IntegrationManager integrationManager;

    public LeashEntityService(Tether plugin, ConfigAccessor configAccessor, IntegrationManager integrationManager) {
        this.plugin = plugin;
        this.configAccessor = configAccessor;
        this.integrationManager = integrationManager;
    }

    /**
     * Leash a non-player entity to a player if allowed.
     * Checks things like current land claims, player permissions, and more.
     *
     * @param player The player to be the leash holder.
     * @param entity The non-player entity to be leashed.
     * @throws IllegalArgumentException if the entity provided is a Player or is not Leashable.
     * @throws NoPermissionException    if the player does not have permission.
     * @throws LeashException           when the leash operation fails for a given reason (LeashErrorType).
     */
    public void leashEntityToPlayer(Player player, Entity entity) throws IllegalArgumentException,
            NoPermissionException, LeashException {
        Leashable leashable = validateTarget(entity);

        // Land protection integration check.
        checkLandProtection(entity.getLocation(), player);

        // If the entity is a Citizens NPC, check if it can be leashed.
        if (entity.hasMetadata("NPC")) {
            net.citizensnpcs.api.npc.NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
            // If the NPC cannot be leashed, return.
            if (npc.data().get(NPC.Metadata.LEASH_PROTECTED, true)) {
                throw new LeashException(LeashErrorType.NPC_UNLEASHABLE);
            }
        }

        // Blacklist/whitelist check.
        if (isEntityRestricted(entity)) throw new LeashException(LeashErrorType.ENTITY_RESTRICTED);

        // If the entity is leashable by default, let the game handle leashing.
        if (DefaultLeashableEntitiesKt.isEntityLeashableByDefault(entity)) {
            return;
        }

        // If the target entity is leashed to a fence or other mob, drop a lead.
        // This must be done because PlayerUnleashEntityEvent, which drops a lead for a mob upon being unleashed, doesn't
        // trigger for mobs that aren't leashable by default that are being transferred from a fence or mob to a player.
        if (leashable.isLeashed() && (leashable.getLeashHolder() instanceof LeashHitch || leashable.getLeashHolder() instanceof Mob)) {
            entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(Material.LEAD, 1));
        }

        // Begin the leashing process.
        // Keep track of the player's leads, prevents duping.
        // (itemStackInMainHand will be a lead).
        ItemStack itemStackInMainHand = player.getInventory().getItemInMainHand();
        int leads = itemStackInMainHand.getAmount();

        // Leashing the entity.
        // The actual leashing process has to run in a scheduler with a slight delay,
        // due to the way the event works.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            leashable.setLeashHolder(player);

            // If a lead was not removed from the player's inventory, remove one.
            if (player.getInventory().getItemInMainHand().getAmount() == (leads - 1)) {
                return;
            }
            // If there is more than one lead in the ItemStack, simply reduce the amount by 1.
            if (leads > 1) {
                itemStackInMainHand.setAmount(leads - 1);
                // Otherwise if there is only one lead in the ItemStack, remove the ItemStack entirely.
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        }, 1L);
    }

    /**
     * Deals with leashing entities to and from a fence.
     *
     * @param player   The player that right-clicked the fence or leash hitch.
     * @param location The location of the fence or leash hitch.
     * @throws LeashException when the leash operation fails for a given reason (LeashErrorType).
     */
    public void handleFenceLeashing(Player player, Location location) throws LeashException {
        List<Mob> mobsLeashedByPlayer = getMobsLeashedByPlayer(player);
        List<Mob> mobsLeashedToFence = getMobsLeashedToFence(location);

        // If the following condition is met, then this has nothing to do with fence leashing. Return.
        if (mobsLeashedByPlayer.isEmpty() && mobsLeashedToFence.isEmpty()) {
            return;
        }

        // Land protection integration check.
        checkLandProtection(location, player);

        if (mobsLeashedByPlayer.isEmpty() && !mobsLeashedToFence.isEmpty()) {
            transferMobsFromFenceToPlayer(player, location);
        } else if (!mobsLeashedByPlayer.isEmpty()) {
            transferMobsFromPlayerToFence(player, location);
        }
    }

    /**
     * Deals with sneak-interaction, specifically looks for leashing entities together and will
     * do so if applicable.
     *
     * @param player The player who sneak-interacted with an entity.
     * @param entity The leashable entity that was sneak-interacted with.
     * @throws IllegalArgumentException if the entity provided is a Player or is not Leashable.
     * @throws LeashException when the leash operation fails for a given reason (LeashErrorType).
     */
    public void handleSneakInteract(Player player, Entity entity) throws IllegalArgumentException, LeashException {
        Leashable leashable = validateTarget(entity);
        // If the target entity is leashed by the player, exit and allow the game to handle unleashing the entity.
        if (leashable.isLeashed() && leashable.getLeashHolder().equals(player)) return;

        // Land protection integration check.
        checkLandProtection(entity.getLocation(), player);

        // Execute the leash holder transfer.
        // Set the leash holder of all mobs leashed by the player to the target entity.
        // (No default leashable mob check here, this logic can overlap with the game and it's fine).
        for (Mob mob : getMobsLeashedByPlayer(player)) {
            mob.setLeashHolder(entity);
        }
    }

    /**
     * Handles interacting with a Leashable Entity with shears in hand.
     *
     * @param player The player who interacted with an entity while holding shears.
     * @param entity The leashable entity that was sneak-interacted with.
     * @throws IllegalArgumentException if the Entity provided is a Player or is not Leashable.
     * @throws LeashException           when the leash operation fails for a given reason (LeashErrorType).
     */
    public void handleShearsInteract(Player player, Entity entity) throws IllegalArgumentException, LeashException {
        Leashable leashable = validateTarget(entity);
        if (!leashable.isLeashed()) return;

        // If the player is the leasher, don't check permissions, always allow to unleash.
        if (leashable.getLeashHolder().equals(player)) {
        } else {
            checkLandProtection(entity.getLocation(), player);
        }
    }

    // Checks the whitelist or blacklist to see whether the entity is restricted from being leashed or not.
    public boolean isEntityRestricted(Entity entity) {
        String entityName = entity.getType().name().toUpperCase();

        // If whitelist is set to be used over blacklist, check the whitelist only.
        if (configAccessor.get(ConfigKeys.EntityLeash.INSTANCE.getUseWhitelistOverBlacklist())) {
            List<String> whitelist = configAccessor.get(ConfigKeys.EntityLeash.INSTANCE.getEntityWhitelist())
                    .stream().map(String::toUpperCase).toList();

            if (whitelist.contains(entityName)) {
                return false;
            } else if (ConfigTokenKt.containsConfigToken(whitelist, ConfigToken.DEFAULT_LEASHABLE_ENTITIES) &&
                    DefaultLeashableEntitiesKt.isEntityLeashableByDefault(entity)) {
                return false;
            } else {
                return true;
            }
        } else {
            // Blacklist is set to be used...
            List<String> blacklist = configAccessor.get(ConfigKeys.EntityLeash.INSTANCE.getEntityBlacklist())
                    .stream().map(String::toUpperCase).toList();

            if (blacklist.contains(entityName)) {
                return true;
            } else if (ConfigTokenKt.containsConfigToken(blacklist, ConfigToken.DEFAULT_LEASHABLE_ENTITIES) &&
                    DefaultLeashableEntitiesKt.isEntityLeashableByDefault(entity)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Validates that the given entity is a legal target for leash operations.
     * Meaning that it must be {@link Leashable}.
     * @param entity The entity to validate.
     * @return the given entity, narrowed to {@link Leashable}.
     * @throws IllegalArgumentException if the Entity is not Leashable.
     */
    private Leashable validateTarget(Entity entity) throws IllegalArgumentException {
        if (!(entity instanceof Leashable leashable)) throw new IllegalArgumentException("Target entity must be leashable.");
        return leashable;
    }

    /**
     * Checks if leashing is allowed by land protection integrations.
     *
     * @param location The location where leashing would occur. (i.e. the location of a clicked LivingEntity or fence post).
     * @param player   The player trying to leash.
     */
    private void checkLandProtection(Location location, Player player) throws LeashException {
        if (!integrationManager.canLeash(location, player)) {
            throw new LeashException(LeashErrorType.LAND_PROTECTED);
        }
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
            // (There is no default leashable mob check here, this logic can overlap with the game and it's fine).
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
                // (There is no default leashable mob check here, this logic can overlap with the game and it's fine).
                mob.setLeashHolder(leashHitch);
            }
        }
    }
}
