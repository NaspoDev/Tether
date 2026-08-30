package dev.naspo.tether.leash.playerleash

import dev.naspo.tether.Tether
import dev.naspo.tether.config.ConfigAccessor
import dev.naspo.tether.config.ConfigKeys
import dev.naspo.tether.exceptions.NoPermissionException
import dev.naspo.tether.exceptions.leashexception.LeashErrorType
import dev.naspo.tether.exceptions.leashexception.LeashException
import dev.naspo.tether.integrations.IntegrationManager
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * Used for leashing players.
 * Keeps track of all currently leashed players.
 */
class PlayerLeashManager(
    private val plugin: Tether,
    private val configAccessor: ConfigAccessor,
    private val integrationManager: IntegrationManager
) {
    val leashedPlayers: MutableMap<Player, ProxyEntity> = mutableMapOf()

    /**
     * Leashes a player to a player.
     * Respects land protection integrations.
     *
     * @param target The player to be leashed.
     * @param leashHolder The player to be the leash holder.
     *
     * @throws IllegalArgumentException if the target and leash holder are the same player.
     * @throws NoPermissionException if the leash holder does not have permission.
     * @throws LeashException when the leash operation fails for a given reason ([LeashErrorType]).
     */
    // This @Throws annotation is only here for java interop. Since Kotlin doesn't have checked exceptions,
    // java thinks that it doesn't throw anything, so we have to tell it.
    @Throws(IllegalArgumentException::class, NoPermissionException::class, LeashException::class)
    fun leashPlayer(target: Player, leashHolder: Player) {
        validateLeash(target, leashHolder)

        // Leash the player.
        val proxyEntity = ProxyEntity.attach(target, leashHolder, plugin)
        leashedPlayers[target] = proxyEntity


    }

    /**
     * Unleash a player.
     *
     * @param player The player to be unleashed.
     */
    fun unleashPlayer(player: Player) {
        val proxyEntity: ProxyEntity = leashedPlayers[player] ?: return
        proxyEntity.setLeashHolder(null)
        leashedPlayers.remove(player)
    }

    /**
     * Returns true if the given player is leashed.
     */
    fun isPlayerLeashed(player: Player): Boolean {
        return leashedPlayers.contains(player)
    }

    /**
     * Returns the leash holder for the given player.
     *
     * @throws IllegalStateException if the player is not leashed.
     */
    fun getLeashHolderForPlayer(player: Player): Entity {
        val proxyEntity: ProxyEntity = leashedPlayers[player]
            ?: throw IllegalStateException("Player was expected to be leashed but is not.")
        return proxyEntity.leashHolder
    }

    // -- Private --

    /**
     * Performs multiple validations to check if leashHolder can leash the target player.
     *
     * @param target The player to be leashed.
     * @param leashHolder The would-be leashHolder.
     */
    private fun validateLeash(target: Player, leashHolder: Player) {
        if (target == leashHolder) throw IllegalArgumentException("Target and leash holder cannot be the same player!")

        // Permission check. ("tether.use.players" is deprecated, here for backwards compatibility).
        if (!leashHolder.hasPermission("tether.leashplayers") &&
            !leashHolder.hasPermission("tether.use.players")
        ) {
            throw NoPermissionException()
        }

        // If the target player is a Citizens NPC, deny the leash.
        // Citizen NPCs of type "Player" should not be leashable as per Citizens.
        if (target.hasMetadata("NPC")) throw LeashException(LeashErrorType.NPC_UNLEASHABLE)

        // If the target is riding an entity, don't allow the leash.
        if (target.vehicle != null) throw LeashException(LeashErrorType.TARGET_PLAYER_RIDING)

        // Integration checks.
        if (!integrationManager.canLeash(target.location, leashHolder)) {
            throw LeashException(LeashErrorType.LAND_PROTECTED)
        }

        // Nesting check. Nesting refers to a leashed player leashing another player.
        // If the to-be leash holder is leashed, and prevent nesting is enabled in the config, throw an error.
        if (isPlayerLeashed(leashHolder) && configAccessor.get(ConfigKeys.PlayerLeash.preventNesting)) {
            throw LeashException(LeashErrorType.PREVENT_NESTING)
        }
    }
}