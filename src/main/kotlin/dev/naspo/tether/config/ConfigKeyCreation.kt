package dev.naspo.tether.config

import org.bukkit.configuration.file.FileConfiguration

/**
 * A ConfigReader is function used for reading a single, specific value type out of a [FileConfiguration] at a given path.
 * If there is an invalid value at the specified path, an [IllegalStateException] will be thrown.
 */
typealias ConfigReader<T> = (FileConfiguration, String) -> T

/**
 * A defined set of [ConfigReader] for different types.
 *
 * Each reader is paired with a [ConfigKeyFactory] function of the same type,
 * so, these are only meant to be directly called by the [ConfigKeyFactory].
 */
private object ConfigReaders {
    val string: ConfigReader<String> = {config, path ->
        if (config.isString(path)) config.getString(path)!!
        else throw IllegalStateException("Expected string value at config path: $path")
    }
    val boolean: ConfigReader<Boolean> = {config, path ->
        if (config.isBoolean(path)) config.getBoolean(path)
        else throw IllegalStateException("Expected boolean value at config path: $path")
    }
    val int: ConfigReader<Int> = {config, path ->
        if (config.isInt(path)) config.getInt(path)
        else throw IllegalStateException("Expected int value at config path: $path")
    }
    val stringList: ConfigReader<List<String>> = {config, path ->
        if (config.isList(path)) config.getStringList(path)
        else throw IllegalStateException("Expected string list value at config path: $path")
    }
}

/**
 * Factory for creating [ConfigKey]s of certain types.
 */
object ConfigKeyFactory {
    fun createString(path: String, defaultValue: String, legacyPaths: List<String> = emptyList()): ConfigKey<String> {
        return ConfigKey(path, defaultValue, legacyPaths, ConfigReaders.string)
    }

    fun createBoolean(path: String, defaultValue: Boolean, legacyPaths: List<String> = emptyList()): ConfigKey<Boolean> {
        return ConfigKey(path, defaultValue, legacyPaths, ConfigReaders.boolean)
    }

    fun createInt(path: String, defaultValue: Int, legacyPaths: List<String> = emptyList()): ConfigKey<Int> {
        return ConfigKey(path, defaultValue, legacyPaths, ConfigReaders.int)
    }

    fun createStringList(path: String, defaultValue: List<String>, legacyPaths: List<String> = emptyList()): ConfigKey<List<String>> {
        return ConfigKey(path, defaultValue, legacyPaths, ConfigReaders.stringList)
    }
}