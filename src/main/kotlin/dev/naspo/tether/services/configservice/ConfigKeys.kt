package dev.naspo.tether.services.configservice

// Defining all config keys here, with backwards compatability support.

sealed class ConfigKey<T>(
    val path: String,
    val legacyPaths: List<String> = emptyList()
)

// Entity Leash
object UseWhitelistOverBlacklist: ConfigKey<Boolean>("use-whitelist-over-blacklist")
object EntityBlacklist: ConfigKey<List<String>>("entity-blacklist", listOf("blacklisted-mobs"))
object EntityWhitelist: ConfigKey<List<String>>("entity-whitelist", listOf("whitelisted-mobs"))

// Player Leash
object PlayerLeashEnabled: ConfigKey<Boolean>("player-leash.enabled")
object PlayerLeashEscapable: ConfigKey<Boolean>("player-leash.escapable")
object PlayerLeashMessageOnLeashed: ConfigKey<Boolean>("player-leash.message-on-leashed")
object PlayerLeashPreventNesting: ConfigKey<Boolean>("player-leash.prevent-nesting")

// Hooks
object GriefPreventionHookEnabled: ConfigKey<Boolean>("hooks.griefprevention")
object TownyHookEnabled: ConfigKey<Boolean>("hooks.towny")
object LandsHookEnabled: ConfigKey<Boolean>("hooks.lands")
object GriefDefenderHookEnabled: ConfigKey<Boolean>("hooks.griefdefender")
object ResidenceHookEnabled: ConfigKey<Boolean>("hooks.residence")

// Messages
object PluginPrefix: ConfigKey<String>("messages.prefix")
object PluginReloadedMessage: ConfigKey<String>("messages.reload")
object NoPermissionMessage: ConfigKey<String>("messages.no-permission")
object LeashTargetInProtectedLandMessage: ConfigKey<String>("messages.leash-target-in-protected-land")
object PlayerLeashedEscapableMessage: ConfigKey<String>("messages.player-leashed-escapable")
object PlayerLeashedNotEscapableMessage: ConfigKey<String>("messages.player-leashed-not-escapable")
object CannotLeashRidingPlayerMessage: ConfigKey<String>("messages.cannot-leash-riding-player")
object PreventNestingMessage: ConfigKey<String>("messages.prevent-nesting")
