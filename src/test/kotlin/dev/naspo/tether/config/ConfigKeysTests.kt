package dev.naspo.tether.config

import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class ConfigKeysTests {

    private lateinit var config: YamlConfiguration
    private lateinit var allConfigKeys: List<ConfigKey<*>>

    @BeforeEach
    fun setUp() {
        // Read config.yml
        val stream = requireNotNull(javaClass.classLoader.getResourceAsStream("config.yml"))
        config = YamlConfiguration.loadConfiguration(
            InputStreamReader(stream, StandardCharsets.UTF_8)
        )

        // Reference ConfigKeys and it's sub-objects to initialize them (since objects are lazy-init).
        // We need to do this because each key in these sub-objects initializes itself with the ConfigKeyFactory,
        // which will add each created ConfigKey to its `all` property for our tests to use.
        ConfigKeys.EntityLeash
        ConfigKeys.PlayerLeash
        ConfigKeys.Hooks
        ConfigKeys.Messages

        allConfigKeys = ConfigKeyFactory.all
    }

    @Test
    fun `test that a ConfigKey exists for each key in config_yml`() {
        val configFileKeys: Set<String> = getLeafKeys()
        val configKeyPaths: Set<String> = allConfigKeys.map { it.path }.toSet()

        val missingKeys: Set<String> = configFileKeys - configKeyPaths
        val extraKeys: Set<String> = configKeyPaths - configFileKeys
        val message: () -> String = {
            var result = "Defined ConfigKeys do not match the keys in the config.yml."
            if (missingKeys.isNotEmpty()) result += "\nMissing ConfigKeys for paths: $missingKeys"
            if (extraKeys.isNotEmpty()) result += "\nExtra ConfigKeys for paths: $extraKeys"
            result
        }

        assertTrue(missingKeys.isEmpty() && extraKeys.isEmpty(), message)
    }

    @Test
    fun `test ConfigKeys default values match corresponding values in config_yml`() {
        for (configKey in allConfigKeys) {
            val configFileValue = config.get(configKey.path)
            val configKeyDefaultValue = configKey.defaultValue
            assertEquals(
                configFileValue,
                configKeyDefaultValue,
                "ConfigKey defaultValue mismatch for path: '${configKey.path}'." +
                        "\nConfigKey's expected value is '$configFileValue' but got '$configKeyDefaultValue'.")
        }
    }

    /**
     * Returns all leaf keys from the config.yml.
     * A leaf key can be defined as a key with a direct value, so not a configuration section key.
     *
     * This is achieved by getting paths via `YamlConfiguration.getKeys(true)`, but ignoring
     * any path that is a [org.bukkit.configuration.ConfigurationSection].
     *
     * Example: For the "player-leash" configuration section, a string of "player-leash" is returned as a path in
     * `YamlConfiguration.getKeys(true)`. But "player-leash" not a key with a direct value, it just defines a
     * `ConfigurationSection`, so we ignore it.
     */
    private fun getLeafKeys(): Set<String> {
        val paths: Set<String> = config.getKeys(true)
        return paths.filter { !config.isConfigurationSection(it) }.toSet()
    }
}