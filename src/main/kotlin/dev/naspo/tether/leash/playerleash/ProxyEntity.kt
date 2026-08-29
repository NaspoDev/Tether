package dev.naspo.tether.leash.playerleash

import dev.naspo.tether.Tether
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask

/**
 * The special invisible entity used in player leashing.
 *
 * ### How It Works
 * The proxy entity is a special invisible, invulnerable, no AI entity. It was what the leash holder actually leashes,
 * and the proxied player is constantly teleported to this entity every tick.
 * This is how player leashing actually works.
 *
 * ### Upon Initialization
 * - The proxy entity will be spawned (with its special properties) at the proxied player's location.
 * - The proxy entity, and therefore its proxied player, will be leashed to the leash holder.
 * - The proxied player will be constantly teleported to this entity every tick.
 *
 * ### Properties
 * @property proxiedPlayer The player for which this entity is the proxy of.
 * @property leashHolder The leash holder of this proxy entity, and therefore also it's proxied player.
 * @property plugin The plugin instance.
 */
class ProxyEntity(
    private val proxiedPlayer: Player,
    private val leashHolder: Player,
    private val plugin: Tether
) {
    companion object {
        private val entityType: Class<Zombie> = Zombie::class.java
        private const val PDC_KEY = "naspodev_tether_playerleash_proxy_entity"
    }

    // The actual backing entity.
    private val entity: LivingEntity = spawn()
    // NamespacedKey for Persistent Data Container
    private val namespacedKey = NamespacedKey(plugin, PDC_KEY)
    // The repeating task to teleport the proxied player to the proxy entity every tick.
    private var teleportationTask: BukkitTask? = null

    init {
        entity.setLeashHolder(leashHolder)
        startTeleportationTask()
    }

    /**
     * Leashes this entity, and therefore its proxied player, to a player.
     *
     * Note that upon intialization of ProxyEntity, it will already be leashed to the provided leash holder.
     * This method is indended to be used to leash this proxy entity to a different leash holder, or to unleash it.
     *
     * @param player The player to be the leash holder of this proxy (and its proxied player).
     * If `null`, this will unleash and delete the proxy.
     */
    fun setLeashHolder(player: Player?) {
        entity.setLeashHolder(player)

        // If the entity is not leashed, i.e. player was null, then destroy this proxy entity.
        if (!entity.isLeashed) {
            destroy()
        }
    }

    // --- Private ---

    /**
     * Spawn the proxy entity at its player's location.
     */
    private fun spawn(): LivingEntity {
        val location: Location = proxiedPlayer.location
        val world: World = proxiedPlayer.world

        return world.spawn(location, entityType) {
            attachPDC(it)
            configureAttributes(it)
        }
    }

    // Creates and attaches a pdc to the entity, marking is as a special proxy entity.
    private fun attachPDC(entity: LivingEntity) {
        val pdc = entity.persistentDataContainer
        pdc.set(namespacedKey, PersistentDataType.STRING, "_")
    }

    // Sets special properties on the entity. (Ex. invisible, no AI, etc...)
    private fun configureAttributes(entity: LivingEntity) {
        entity.isInvisible = true
        entity.isInvulnerable = true
        entity.isSilent = true
        entity.setAI(false)
    }

    // Creates and starts a task to constatly teleport the proxied player to this proxy entity every tick.
    private fun startTeleportationTask() {
        teleportationTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            proxiedPlayer.teleport(entity.location)
        }, 0, 1)
    }

    /**
     * Destroy this proxy entity.
     * This will cancel the teleportation task and removes the entity.
     */
    private fun destroy() {
        teleportationTask?.cancel()
        entity.remove()
    }
}