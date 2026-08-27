package dev.naspo.tether.leash.playerleash

import dev.naspo.tether.Tether
import io.papermc.paper.entity.Leashable
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.persistence.PersistentDataType

/**
 * The special invisible entity used in player leashing.
 *
 * @property player The player for which this entity is the proxy of.
 * @property plugin The plugin instance.
 */
class ProxyEntity(
    private val player: Player,
    private val plugin: Tether
) {
    companion object {
        private val entityType = EntityType.ZOMBIE
        private const val PDC_KEY = "naspodev_tether_playerleash_proxy_entity"
    }

    // The actual backing entity.
    private var entity: LivingEntity? = null
    // NamespacedKey for Persistent Data Container
    private val namespacedKey = NamespacedKey(plugin, PDC_KEY)

    /**
     * Spawn the proxy entity at its player's location.
     * This will not spawn multiple entities if called multiple times.
     */
    fun spawn() {
        // If its already spawned do nothing.
        if (entity != null) return

        val location: Location = player.location
        val world: World = player.world

        entity = world.spawnEntity(location, entityType) as LivingEntity

        attachPDC()
        configureAttributes()
    }

    /**
     * Leashes this entity, and therefore its proxied player, to a player.
     *
     * ### How It Works
     * The proxied player (player associated with this Proxy Entity) will be
     * constantly teleported to this entity every tick. This is how player leashing actually works.
     *
     * ### Params
     * @param player The player to be the leash holder of this proxy (and its proxied player).
     * If `null`, this will unleash and delete the proxy.
     */
    fun setLeashHolder(player: Player?) {
        val entity = entity ?: return

        // TODO: implement this
    }

    // --- Private ---

    // Creates and attaches a pdc to the entity, marking is as a special proxy entity.
    private fun attachPDC() {
        val entity = entity ?: return
        val pdc = entity.persistentDataContainer
        pdc.set(namespacedKey, PersistentDataType.STRING, "_")
    }

    // Sets special properties on the entity. (Ex. invisible, no AI, etc...)
    private fun configureAttributes() {
        val entity = entity ?: return
        entity.isInvisible = true
        entity.isInvulnerable = true
        entity.isSilent = true
        entity.setAI(false)
    }

}