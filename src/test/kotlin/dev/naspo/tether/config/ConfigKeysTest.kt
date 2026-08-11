package dev.naspo.tether.config

import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals

class ConfigKeysTest {

    private lateinit var config: YamlConfiguration

    @BeforeEach
    fun setUp() {
        val stream = requireNotNull(javaClass.classLoader.getResourceAsStream("config.yml"))
        config = YamlConfiguration.loadConfiguration(
            InputStreamReader(stream, StandardCharsets.UTF_8)
        )
    }

    @Test
    fun tempTest() {
        val configValue = config.getString(ConfigKeys.Messages.pluginPrefix.path)
        val defaultValue = ConfigKeys.Messages.pluginPrefix.defaultValue
        assertEquals(configValue, defaultValue)
    }
}