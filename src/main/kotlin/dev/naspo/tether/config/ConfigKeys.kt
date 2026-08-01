package dev.naspo.tether.config

// Defining all config keys here, with backwards compatability support.

/**
 * Represents a type-safe key in the config.yml.
 */
class ConfigKey<T>(
    /** The string path to the value in the config. */
    val path: String,
    /** The default value. */
    val defaultValue: T,
    /** A list of legacy paths for this key. Enables backwards compatability. */
    val legacyPaths: List<String>,
    /**
     * The ConfigKey's reader. Used to read its value from the config.
     * Only meant to be used by [ConfigAccessor]. If you're trying to get a ConfigKey's value, use [ConfigAccessor.get].
     */
    val reader: ConfigReader<T>
)

/**
 * Object containing all config keys in our config.yml as type-safe [ConfigKey].
 */
object ConfigKeys {
    // Entity Leash
    val useWhitelistOverBlacklist: ConfigKey<Boolean> =
        ConfigKeyFactory.createBoolean("use-whitelist-over-blacklist", false)
    val entityBlacklist: ConfigKey<List<String>> =
        ConfigKeyFactory.createStringList("entity-blacklist", emptyList(), listOf("blacklisted-mobs"))
    val entityWhitelist: ConfigKey<List<String>> =
        ConfigKeyFactory.createStringList("entity-whitelist", emptyList(),listOf("whitelisted-mobs"))

    // Player Leash
    val playerLeashEnabled: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("player-leash.enabled", false)
    val playerLeashEscapable: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("player-leash.escapable", true)
    val playerLeashMessageOnLeashed: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("player-leash.message-on-leashed", true)
    val playerLeashPreventNesting: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("player-leash.prevent-nesting", false)

    // Hooks
    val griefPreventionHookEnabled: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("hooks.griefprevention", false)
    val townyHookEnabled: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("hooks.towny", false)
    val landsHookEnabled: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("hooks.lands", false)
    val griefDefenderHookEnabled: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("hooks.griefdefender", false)
    val residenceHookEnabled: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("hooks.residence", false)

    // Messages
    val pluginPrefix: ConfigKey<String> =
        ConfigKeyFactory.createString("messages.prefix", "&8[&6Tether&8] &r")
    val pluginReloadedMessage: ConfigKey<String> =
        ConfigKeyFactory.createString("messages.reload", "&7Tether has been reloaded!")
    val noPermissionMessage: ConfigKey<String> =
        ConfigKeyFactory.createString("messages.no-permission", "&cYou do not have permission!")
    val leashTargetInProtectedLandMessage: ConfigKey<String> =
        ConfigKeyFactory.createString("messages.leash-target-in-protected-land", "&7Leash target is in protected land.")
    val playerLeashedEscapableMessage: ConfigKey<String> =
        ConfigKeyFactory.createString("messages.player-leashed-escapable", "&7You've been leashed! Press &6crouch &7to escape.")
    val playerLeashedNotEscapableMessage: ConfigKey<String> =
        ConfigKeyFactory.createString("messages.player-leashed-not-escapable", "&7You've been leashed!")
    val cannotLeashRidingPlayerMessage: ConfigKey<String> =
        ConfigKeyFactory.createString("messages.cannot-leash-riding-player", "&7You cannot leash players that are riding an entity.")
    val preventNestingMessage: ConfigKey<String> =
        ConfigKeyFactory.createString("messages.prevent-nesting", "&7You cannot leash a player while your are leashed.")
}
