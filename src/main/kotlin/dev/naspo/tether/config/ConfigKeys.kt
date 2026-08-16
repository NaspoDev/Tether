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

    object EntityLeash {
        val useWhitelistOverBlacklist: ConfigKey<Boolean> =
            ConfigKeyFactory.createBoolean("use-whitelist-over-blacklist", false)
        val entityBlacklist: ConfigKey<List<String>> =
            ConfigKeyFactory.createStringList("entity-blacklist", listOf("EXAMPLEMOB"), listOf("blacklisted-mobs"))
        val entityWhitelist: ConfigKey<List<String>> =
            ConfigKeyFactory.createStringList("entity-whitelist", listOf("EXAMPLEMOB"),listOf("whitelisted-mobs"))
    }

    object PlayerLeash {
        val enabled: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("player-leash.enabled", false)
        val escapable: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("player-leash.escapable", true)
        val messageOnLeashed: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("player-leash.message-on-leashed", true)
        val preventNesting: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("player-leash.prevent-nesting", false)
    }

    object Hooks {
        val griefPreventionEnabled: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("hooks.griefprevention", false)
        val townyEnabled: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("hooks.towny", false)
        val landsEnabled: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("hooks.lands", false)
        val griefDefenderEnabled: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("hooks.griefdefender", false)
        val residenceEnabled: ConfigKey<Boolean> = ConfigKeyFactory.createBoolean("hooks.residence", false)
    }

    object Messages {
        val pluginPrefix: ConfigKey<String> =
            ConfigKeyFactory.createString("messages.prefix", "<dark_gray>[<gold>Tether<dark_gray>] <reset>")
        val pluginReloaded: ConfigKey<String> =
            ConfigKeyFactory.createString("messages.reload", "<gray>Tether has been reloaded!")
        val noPermission: ConfigKey<String> =
            ConfigKeyFactory.createString("messages.no-permission", "<red>You do not have permission!")
        val leashTargetInProtectedLand: ConfigKey<String> =
            ConfigKeyFactory.createString("messages.leash-target-in-protected-land", "<gray>Leash target is in protected land.")
        val playerLeashedEscapable: ConfigKey<String> =
            ConfigKeyFactory.createString("messages.player-leashed-escapable", "<gray>You've been leashed! Press <gold>crouch <gray>to escape.")
        val playerLeashedNotEscapable: ConfigKey<String> =
            ConfigKeyFactory.createString("messages.player-leashed-not-escapable", "<gray>You've been leashed!")
        val cannotLeashRidingPlayer: ConfigKey<String> =
            ConfigKeyFactory.createString("messages.cannot-leash-riding-player", "<gray>You cannot leash players that are riding an entity.")
        val preventNesting: ConfigKey<String> =
            ConfigKeyFactory.createString("messages.prevent-nesting", "<gray>You cannot leash a player while your are leashed.")
    }
}
