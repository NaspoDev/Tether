package dev.naspo.tether.listeners

import dev.naspo.tether.config.ConfigAccessor
import dev.naspo.tether.config.ConfigKeys
import dev.naspo.tether.leash.LeashPlayerService
import dev.naspo.tether.leash.playerleash.PlayerLeashManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class EntityDamageByEntityListener(
    val configAccessor: ConfigAccessor,
    val playerLeashManager: PlayerLeashManager,
) : Listener {

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        // Player leash suppress leashed players check.
        val damager: Player = event.damager as? Player ?: return
        if (playerLeashManager.isPlayerLeashed(damager) &&
                configAccessor.get(ConfigKeys.PlayerLeash.suppressLeashedPlayer)) {
            event.isCancelled = true
        }
    }
}