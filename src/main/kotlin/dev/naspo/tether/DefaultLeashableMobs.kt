package dev.naspo.tether

import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.bukkit.entity.Monster

// Defines mobs which are leashable by default in the vanilla game, and provides utility for checking
// if a mob is leashable by default.
// Default leashable mob data is based on this: https://minecraft.wiki/w/Lead

// A list of mobs that are unconditionally leashable by default. i.e. they are always leashable by default.
private val unconditionalDefaultLeashableMobs: List<EntityType> = listOf(
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
 * A condition under which a mob becomes leashable.
 * (Some mobs are only leashable under certain conditions).
 * @param isMet A lambda which performs the leash condition check.
 */
private enum class LeashCondition(val isMet: (Mob) -> Boolean) {
    NOT_HOSTILE({ it !is Monster }),
    NOT_MOUNTED_BY_HOSTILE_MOB({ mob ->
        mob.passengers.none { it is Monster }
    })
}

// A list of mobs that are conditionally leashable by default. i.e. they are only leashable under certain conditions.
// Some mobs need multiple conditions to be met for them to become leashable.
private val conditionalDefaultLeashableMobs: Map<EntityType, List<LeashCondition>> = mapOf(
    EntityType.NAUTILUS to listOf(LeashCondition.NOT_HOSTILE),
    EntityType.WOLF to listOf(LeashCondition.NOT_HOSTILE),
    EntityType.ZOMBIE_NAUTILUS to listOf(LeashCondition.NOT_HOSTILE, LeashCondition.NOT_MOUNTED_BY_HOSTILE_MOB),
    EntityType.CAMEL_HUSK to listOf(LeashCondition.NOT_MOUNTED_BY_HOSTILE_MOB),
    EntityType.ZOMBIE_HORSE to listOf(LeashCondition.NOT_MOUNTED_BY_HOSTILE_MOB),
)

/**
 * Returns true if a mob is leashable by default.
 */
fun isMobLeashableByDefault(mob: Mob): Boolean {
    return if (unconditionalDefaultLeashableMobs.contains(mob.type)) {
        Bukkit.getServer().logger.info("The mob ${mob.type.name} is unconditionally leashable by default.")
        true
    } else if (conditionalDefaultLeashableMobs.keys.contains(mob.type)) {
        // Check if leash conditions are met for the mob.
        val leashConditions: List<LeashCondition> = conditionalDefaultLeashableMobs[mob.type] ?: return false
//        leashConditions.all { it.isMet(mob) }

        // TODO: TEMP - uncomment above and remove when done
        val res = leashConditions.all { it.isMet(mob) }
        if (res) {
            Bukkit.getServer().logger.info("The mob ${mob.type.name} is conditionally leashable by default, and all conditions passed.")
        } else {
            Bukkit.getServer().logger.info("The mob ${mob.type.name} is conditionally leashable by default, conditions did NOT pass.")
        }
        res
    } else {
        false
    }
}