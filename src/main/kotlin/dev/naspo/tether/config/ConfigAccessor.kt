package dev.naspo.tether.config

import dev.naspo.tether.Tether

class ConfigAccessor(private val plugin: Tether) {

    // Private helper property. Shorthand to `plugin.config`
    private val config
        get() = plugin.config

    /**
     * Gets the value of a [ConfigKey] from the plugin's config.yml.
     *
     * If the ConfigKey's path doesn't exist, it's legacy paths will be searched for (if applicable).
     * If it's legacy paths don't exist, it's default value will be returned.
     */
    fun <T> get(configKey: ConfigKey<T>): T {
        val path: String? = resolvePath(configKey)
        if (path == null) {
            logPathNotFound(configKey)
            return configKey.defaultValue
        } else {
            // There is a valid path, now try to read the value.
            return try {
                configKey.reader(config, path)
            } catch (e: IllegalStateException) {
                plugin.logger.warning(e.message)
                plugin.logger.warning("Config path $path has an invalid value for its type. " +
                        "Using default value of ${configKey.defaultValue}")
                configKey.defaultValue
            }
        }
    }

    /**
     * Finds the valid path to use for a [ConfigKey].
     * Returns null if there is none.
     */
    private fun <T> resolvePath(configKey: ConfigKey<T>): String? {
        val path: String = configKey.path
        if (config.contains(path)) {
            return path
        } else {
            // Try and find a legacy path.
            return configKey.legacyPaths.firstOrNull { config.contains(it) }
        }
    }

    /**
     * Logs a config path not found warning to the console.
     */
    private fun <T> logPathNotFound(configKey: ConfigKey<T>) {
        if (configKey.legacyPaths.isEmpty()) {
            plugin.logger.warning(
                "Config path ${configKey.path} not found. Using default value of ${configKey.defaultValue}"
            )
        } else {
            plugin.logger.warning(
                "Config path ${configKey.path} and it's legacies were not found. " +
                        "Using default value of ${configKey.defaultValue}"
            )
        }
    }
}