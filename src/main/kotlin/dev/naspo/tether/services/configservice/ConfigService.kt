package dev.naspo.tether.services.configservice

import dev.naspo.tether.Tether

class ConfigService(private val plugin: Tether) {

    fun <T> getVal(key: ConfigKey<T>): T {
        
    }
}