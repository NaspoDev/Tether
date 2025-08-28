package dev.naspo.tether.exceptions.leashexception;

/**
 * Specific types of leash errors used for LeashException.
 */
public enum LeashErrorType {
    LAND_CLAIM_RESTRICTION, // leash target is in a land claim.
    NPC_UNLEASHABLE, // When an NPC is unleashable.
    MOB_RESTRICTED, // Mob is either blacklisted or not on the whitelist.
    TARGET_PLAYER_RIDING, // The target player is riding an entity and can't be leashed.
    PREVENT_NESTING // The player trying to leash is riding an entity while prevent-nesting is disabled in the config.
}
