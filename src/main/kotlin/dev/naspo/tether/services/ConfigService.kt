package dev.naspo.tether.services

enum class ConfigKey(val path: String, val legacyPaths: List<String>) {
    USE_WHITELIST_OVER_BLACKLIST("use-whitelist-over-blacklist")
}