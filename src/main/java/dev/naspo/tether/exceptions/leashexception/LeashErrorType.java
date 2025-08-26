package dev.naspo.tether.exceptions.leashexception;

/**
 * Specific types of leash errors used for LeashException.
 */
public enum LeashErrorType {
    LAND_CLAIM_RESTRICTION, // leash target is in a land claim.
    NPC_UNLEASHABLE, // The NPC is set as unleashable.
    MOB_RESTRICTED // Mob is either blacklisted or not on the whitelist.
}
