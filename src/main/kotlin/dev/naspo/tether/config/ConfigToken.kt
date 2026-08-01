package dev.naspo.tether.config

/**
 * A special word, used in the value of a config key,
 * which is used to represent something other than it's string literal.
 *
 * For example: [DEFAULT_LEASHABLE_ENTITIES] is used in the blacklist/whitelist
 * to refer to all default leashable entities.
 */
enum class ConfigToken(
    /** The token's string literal value. */
    val value: String,
    /** A list of legacy values for this token. Enables backwards compatability. */
    val legacyValues: List<String>
) {
    /** The token used in the blacklist/whitelist to refer to all default leashable entities. */
    DEFAULT_LEASHABLE_ENTITIES("DEFAULT_LEASHABLE_ENTITIES", listOf("DEFAULT_LEASHABLE_MOBS"))
}

/**
 * Helper function to check if a string list contains a [ConfigToken], including its legacy values.
 */
fun containsConfigToken(list: List<String>, token: ConfigToken): Boolean {
    return list.contains(token.value) || list.any {token.legacyValues.contains(it)}
}