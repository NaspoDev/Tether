package dev.naspo.tether.listeners

import dev.naspo.tether.config.ConfigAccessor
import dev.naspo.tether.config.ConfigKeys
import dev.naspo.tether.leash.LeashPlayerService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class EntityDamageByEntityListener(
    val configAccessor: ConfigAccessor,
    val leashPlayerService: LeashPlayerService,
) : Listener {

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val player = event.damager as? Player ?: return
        if (leashPlayerService.isPlayerLeashed(player) &&
                configAccessor.get(ConfigKeys.PlayerLeash.suppressLeashedPlayer)) {
            event.isCancelled = true
        }
    }
}