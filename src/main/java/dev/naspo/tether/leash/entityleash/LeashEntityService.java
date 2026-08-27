package dev.naspo.tether.leash.entityleash;

import dev.naspo.tether.Tether;
import dev.naspo.tether.config.*;
import dev.naspo.tether.exceptions.NoPermissionException;
import dev.naspo.tether.exceptions.leashexception.LeashErrorType;
import dev.naspo.tether.exceptions.leashexception.LeashException;
import dev.naspo.tether.integrations.IntegrationManager;
import dev.naspo.tether.leash.LeashPlayerService;
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
     * Leash a {@link Leashable} entity to a player if allowed.
     * Checks things like current land claims, player permissions, and more.
     *
     * @param player The player to be the leash holder.
     * @param entity The {@link Leashable} entity to be leashed.
     * @throws IllegalArgumentException if the entity provided is not {@link Leashable}.
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

        // If the target entity is leashed to a fence or other leashable entity, drop a lead.
        // This must be done because PlayerUnleashEntityEvent, which drops a lead for a leashable entity upon being
        // unleashed, doesn't trigger for entities that aren't leashable by default that are being transferred from
        // a fence or leashable entity to a player.
        if (leashable.isLeashed() &&
                (leashable.getLeashHolder() instanceof LeashHitch || leashable.getLeashHolder() instanceof Leashable)) {
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
        List<Leashable> entitiesLeashedByPlayer = getEntitiesLeashedByPlayer(player);
        List<Leashable> entitiesLeashedToFence = getEntitiesLeashedToFence(location);

        // If the following condition is met, then this has nothing to do with fence leashing. Return.
        if (entitiesLeashedByPlayer.isEmpty() && entitiesLeashedToFence.isEmpty()) {
            return;
        }

        // Land protection integration check.
        checkLandProtection(location, player);

        if (entitiesLeashedByPlayer.isEmpty() && !entitiesLeashedToFence.isEmpty()) {
            transferEntitiesFromFenceToPlayer(player, location);
        } else if (!entitiesLeashedByPlayer.isEmpty()) {
            transferEntitiesFromPlayerToFence(player, location);
        }
    }

    /**
     * Deals with sneak-interaction, specifically looks for leashing entities together and will
     * do so if applicable.
     *
     * @param player The player who sneak-interacted with an entity.
     * @param entity The {@link Leashable} entity that was sneak-interacted with.
     * @throws IllegalArgumentException if the entity provided is not {@link Leashable}.
     * @throws LeashException           when the leash operation fails for a given reason (LeashErrorType).
     */
    public void handleSneakInteract(Player player, Entity entity) throws IllegalArgumentException, LeashException {
        Leashable leashable = validateTarget(entity);
        // If the target entity is leashed by the player, exit and allow the game to handle unleashing the entity.
        if (leashable.isLeashed() && leashable.getLeashHolder().equals(player)) return;

        // Land protection integration check.
        checkLandProtection(entity.getLocation(), player);

        // Execute the leash holder transfer.
        // Set the leash holder of all entities leashed by the player to the target entity.
        // (No default leashable entity check here, this logic can overlap with the game and it's fine).
        for (Leashable l : getEntitiesLeashedByPlayer(player)) {
            l.setLeashHolder(entity);
        }
    }

    /**
     * Handles interacting with a {@link Leashable} Entity with shears in hand.
     *
     * @param player The player who interacted with an entity while holding shears.
     * @param entity The {@link Leashable} entity that was sneak-interacted with.
     * @throws IllegalArgumentException if the Entity provided is not {@link Leashable}.
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
     *
     * @param entity The entity to validate.
     * @return the given entity, narrowed to {@link Leashable}.
     * @throws IllegalArgumentException if the Entity is not Leashable.
     */
    private Leashable validateTarget(Entity entity) throws IllegalArgumentException {
        if (!(entity instanceof Leashable leashable))
            throw new IllegalArgumentException("Target entity must be leashable.");
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

    /**
     * Gets the entities which are currently leashed by the player.
     *
     * @return A list of {@link Leashable} entities.
     */
    private List<Leashable> getEntitiesLeashedByPlayer(Player player) {
        List<Leashable> leashedEntities = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof Leashable leashable) {
                if (leashable.isLeashed()
                        && leashable.getLeashHolder() instanceof Player holder
                        && holder.equals(player)) {
                    leashedEntities.add(leashable);
                }
            }
        }
        return leashedEntities;
    }

    /**
     * Gets the entities which are currently leashed to a fence at the given location.
     *
     * @param location The location of the fence or leash hitch.
     * @return The list of entities leashed to that fence.
     */
    private List<Leashable> getEntitiesLeashedToFence(Location location) {
        List<Leashable> leashedEntities = new ArrayList<>();

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
                if (entity instanceof Leashable leashable) {
                    if (leashable.isLeashed()
                            && leashable.getLeashHolder() instanceof LeashHitch holder
                            && holder.equals(leashHitch)) {
                        leashedEntities.add(leashable);
                    }
                }
            }
        }
        return leashedEntities;
    }

    private void transferEntitiesFromFenceToPlayer(Player player, Location fenceLocation) {
        List<Leashable> entities = getEntitiesLeashedToFence(fenceLocation);
        for (Leashable leashable : entities) {
            // Only transfer entities which are not leashable by default. Let the game handle the default ones.
            if (!DefaultLeashableEntitiesKt.isEntityLeashableByDefault(leashable)) {
                leashable.setLeashHolder(player);
            }
        }
    }

    private void transferEntitiesFromPlayerToFence(Player player, Location fenceLocation) {
        List<Leashable> leashedEntities = getEntitiesLeashedByPlayer(player);

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
        }
        // Transfer the entities from the player to the fence (leash hitch.
        for (Leashable leashable : leashedEntities) {
            // Only transfer entities which are not leashable by default. Let the game handle the default ones.
            if (!DefaultLeashableEntitiesKt.isEntityLeashableByDefault(leashable)) {
                leashable.setLeashHolder(leashHitch);
            }
        }
    }
}
