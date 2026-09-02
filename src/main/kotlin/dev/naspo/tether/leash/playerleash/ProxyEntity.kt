package dev.naspo.tether.leash.playerleash

import com.bekvon.bukkit.residence.commands.placeholders
import dev.naspo.tether.Tether
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Ageable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

/**
 * A proxy entity is a special invisible, invulnerable, no AI entity that is used for player leashing.
 * It was what the leash holder actually leashes, and the proxied player is constantly teleported to
 * this entity every tick. This is how player leashing works.
 */
class ProxyEntity private constructor(
    private val plugin: Tether,
    private val proxiedPlayer: Player,
    private val entity: Mob // The actual backing entity.
) {
    companion object {
        private val entityType: Class<Zombie> = Zombie::class.java
        private const val PDC_KEY = "naspodev_tether_playerleash_proxy_entity"

        /**
         * This method performs the following actions:
         * - Spawns the proxy entity (with its special properties) at the proxied player's location.
         * - Leashes the proxy entity, and therefore its proxied player, to the leash holder.
         * - Has the proxied player be constantly teleported to this entity every tick.
         *
         * @param proxiedPlayer The player for which this entity is the proxy of.
         * @param leashHolder The leash holder of this proxy entity (and therefore also the leasholder of its
         * proxied player).
         * @param plugin The plugin instance.
         * @return The created ProxyEntity.
         * @throws Exception Throws the error that occurred during creation and attachment of this proxy entity,
         * if one occurred.
         */
        fun attach(proxiedPlayer: Player, leashHolder: Player, plugin: Tether): ProxyEntity {
            val entity: Mob = spawn(proxiedPlayer, plugin)
            val proxyEntity = ProxyEntity(plugin, proxiedPlayer, entity)

            try {
                proxyEntity.setLeashHolder(leashHolder)
            } catch (e: Exception) {
                proxyEntity.destroy()
                throw e
            }

            return proxyEntity
        }

        /**
         * Spawn the proxy entity at its player's location.
         */
        private fun spawn(proxiedPlayer: Player, plugin: Tether): Mob {
            val location: Location = proxiedPlayer.location
            val world: World = proxiedPlayer.world

            return world.spawn(location, entityType) {
                attachPDC(it, plugin)
                configureAttributes(it)
            }
        }

        // Creates and attaches a Persistent Data Container to the entity, marking is as a special proxy entity.
        private fun attachPDC(entity: LivingEntity, plugin: Tether) {
            val pdc = entity.persistentDataContainer
            pdc.set(NamespacedKey(plugin, PDC_KEY), PersistentDataType.STRING, "_")
        }

        // Sets special properties on the entity. (Ex. invisible, silent, etc...)
        private fun configureAttributes(entity: Mob) {
            entity.isInvisible = true
            entity.isInvulnerable = true
            entity.isSilent = true
//            entity.isAware = false
            entity.canPickupItems = false
            entity.equipment.clear()

            if (entity is Ageable) {
                entity.setAdult()
            }
        }
    }

    // Computed property to get the leash holder.
    /**
     * Returns the leash holder of this proxy entity, (and therefore the leash holder of its proxied player).
     */
    val leashHolder: Entity
        get() = entity.leashHolder

    // The repeating task to teleport the proxied player to the proxy entity every tick.
    private var teleportationTask: BukkitTask? = null
    // Regulates the proxy entity's velocity.
    private var velocityRegulationTask: BukkitTask? = null

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

        if (entity.isLeashed) {
            startTeleportationTask()
//            startVelocityRegulationTask()
        } else {
            // If the entity is not leashed, i.e. player was null, then destroy this proxy entity.
            destroy()
        }
    }

    // -- Private --

    // Creates and starts a task to constantly teleport the proxied player to this proxy entity every tick.
    private fun startTeleportationTask() {
        teleportationTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            proxiedPlayer.teleport(entity.location)

            plugin.server.mobGoals.removeAllGoals(entity)
            var leashHodlerLocation: Location = entity.leashHolder.location
            leashHodlerLocation = leashHodlerLocation.subtract(1.0, 0.0, 1.0)
            entity.pathfinder.moveTo(leashHodlerLocation)
        }, 0, 1)
    }

    private fun startVelocityRegulationTask() {
        velocityRegulationTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            entity.velocity = entity.velocity.multiply(0)
        }, 20, 20)
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