package dev.naspo.tether.config

import dev.naspo.tether.Tether

/**
 * For each [ConfigKey] (as defined in [ConfigKeys])...
 *
 * If the config contains it's legacy key and not the modern equivalent, copy the value(s) from the legacy
 * key to the modern one, then delete the legacy one.
 *
 * This is expected to be called during plugin enable, right before `config.options().copyDefaults()` is called.
 *
 * ### Example
 * `blacklisted-mobs` changed to `entity-blacklist`.
 * On startup, check if the user's config has the old `blacklisted-mobs` key. If it does, create the
 * `entity-blacklist` key and copy over the values. Then delete `blacklisted-mobs`.
 */
fun migrateConfigGracefully(plugin: Tether) {
    val config = plugin.config

    for (configKey in ConfigKeyFactory.all) {
        // Find the existing legacy key in the config, if it exists.
        val presentLegacyKey: String? = configKey.legacyPaths.firstOrNull {
            config.contains(it, true)
        }

        val modernPath: String = configKey.path

        // If there is a legacy key present, and it's modern key equivalent is not present,
        // copy the values over and delete the legacy key.
        if (presentLegacyKey != null && !config.contains(modernPath, true)) {
            plugin.logger.info("Legacy config key '$presentLegacyKey' was found and will be replaced with " +
                    "its modern equivalent: '${modernPath}'. Value(s) will be copied over.")
            config.set(modernPath, config.get(presentLegacyKey))
            config.set(presentLegacyKey, null)
        }
    }
}