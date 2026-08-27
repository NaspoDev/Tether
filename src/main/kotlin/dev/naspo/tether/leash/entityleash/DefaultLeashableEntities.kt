package dev.naspo.tether.leash.entityleash

import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster

// Defines entities which are leashable by default in the vanilla game, and provides utility for checking
// if an entity is leashable by default.
// Default leashable entity data is based on this: https://minecraft.wiki/w/Lead

// A list of entities that are unconditionally leashable by default. i.e. they are always leashable by default.
private val unconditionalDefaultLeashableEntities: List<EntityType> = listOf(
    // Mobs
    EntityType.ALLAY,
    EntityType.ARMADILLO,
    EntityType.AXOLOTL,
    EntityType.BEE,

    // Boats
    EntityType.OAK_BOAT,
    EntityType.SPRUCE_BOAT,
    EntityType.BIRCH_BOAT,
    EntityType.JUNGLE_BOAT,
    EntityType.ACACIA_BOAT,
    EntityType.DARK_OAK_BOAT,
    EntityType.MANGROVE_BOAT,
    EntityType.CHERRY_BOAT,
    EntityType.PALE_OAK_BOAT,
    EntityType.BAMBOO_RAFT,

    // Boats with chests
    EntityType.OAK_CHEST_BOAT,
    EntityType.SPRUCE_CHEST_BOAT,
    EntityType.BIRCH_CHEST_BOAT,
    EntityType.JUNGLE_CHEST_BOAT,
    EntityType.ACACIA_CHEST_BOAT,
    EntityType.DARK_OAK_CHEST_BOAT,
    EntityType.MANGROVE_CHEST_BOAT,
    EntityType.CHERRY_CHEST_BOAT,
    EntityType.PALE_OAK_CHEST_BOAT,
    EntityType.BAMBOO_CHEST_RAFT,

    // Mobs con't...
    EntityType.CAMEL,
    EntityType.CAT,
    EntityType.CHICKEN,
    EntityType.COPPER_GOLEM,
    EntityType.COW,
    EntityType.DOLPHIN,
    EntityType.DONKEY,
    EntityType.FOX,
    EntityType.FROG,
    EntityType.GLOW_SQUID,
    EntityType.GOAT,
    EntityType.HAPPY_GHAST,
    EntityType.HOGLIN,
    EntityType.HORSE,
    EntityType.IRON_GOLEM,
    EntityType.LLAMA,
    EntityType.MOOSHROOM,
    EntityType.MULE,
    EntityType.OCELOT,
    EntityType.PARROT,
    EntityType.PIG,
    EntityType.POLAR_BEAR,
    EntityType.RABBIT,
    EntityType.SHEEP,
    EntityType.SKELETON_HORSE,
    EntityType.SNIFFER,
    EntityType.SNOW_GOLEM,
    EntityType.SQUID,
    EntityType.STRIDER,
    EntityType.SULFUR_CUBE,
    EntityType.TRADER_LLAMA,
    EntityType.ZOGLIN
)

/**
 * A condition under which an entity becomes leashable.
 * (Some entities are only leashable under certain conditions).
 * @param isMet A lambda which performs the leash condition check.
 */
private enum class LeashCondition(val isMet: (Entity) -> Boolean) {
    NOT_HOSTILE({ it !is Monster }),
    NOT_MOUNTED_BY_HOSTILE_MOB({ entity ->
        entity.passengers.none { it is Monster }
    })
}

// A list of entities that are conditionally leashable by default. i.e. they are only leashable under certain conditions.
// Some entities need multiple conditions to be met for them to become leashable.
private val conditionalDefaultLeashableEntities: Map<EntityType, List<LeashCondition>> = mapOf(
    EntityType.NAUTILUS to listOf(LeashCondition.NOT_HOSTILE),
    EntityType.WOLF to listOf(LeashCondition.NOT_HOSTILE),
    EntityType.ZOMBIE_NAUTILUS to listOf(LeashCondition.NOT_HOSTILE, LeashCondition.NOT_MOUNTED_BY_HOSTILE_MOB),
    EntityType.CAMEL_HUSK to listOf(LeashCondition.NOT_MOUNTED_BY_HOSTILE_MOB),
    EntityType.ZOMBIE_HORSE to listOf(LeashCondition.NOT_MOUNTED_BY_HOSTILE_MOB),
)

/**
 * Returns true if an entity is leashable by default.
 */
fun isEntityLeashableByDefault(entity: Entity): Boolean {
    if (unconditionalDefaultLeashableEntities.contains(entity.type)) {
        return true
    } else if (conditionalDefaultLeashableEntities.keys.contains(entity.type)) {
        // Check if leash conditions are met for the entity.
        val leashConditions: List<LeashCondition> = conditionalDefaultLeashableEntities[entity.type] ?: return false
        return leashConditions.all { it.isMet(entity) }
    } else {
        return false
    }
}