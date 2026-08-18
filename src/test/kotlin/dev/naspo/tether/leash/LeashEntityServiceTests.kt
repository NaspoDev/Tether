package dev.naspo.tether.leash

import dev.naspo.tether.Tether
import dev.naspo.tether.config.ConfigAccessor
import dev.naspo.tether.integrations.IntegrationManager
import io.papermc.paper.entity.Leashable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Cow
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mockbukkit.mockbukkit.world.WorldMock


class LeashEntityServiceTests {

    private lateinit var server: ServerMock
    private lateinit var plugin: Tether
    private lateinit var configAccessor: ConfigAccessor
    private lateinit var integrationManager: IntegrationManager
    private lateinit var leashEntityService: LeashEntityService
    private lateinit var world: WorldMock
    private lateinit var player: PlayerMock

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(Tether::class.java)
        configAccessor = ConfigAccessor(plugin)
        integrationManager = IntegrationManager(plugin, configAccessor)
        leashEntityService = LeashEntityService(plugin, configAccessor, integrationManager)
        world = server.addSimpleWorld("test_world")
        player = server.addPlayer()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    // Note: DLE stands for "Default Leashable Entity"

    @Test
    fun `test leashEntityToPlayer with unconditional DLE`() {
        // TODO: Currently just testing if leashing even works. Unfortunately it doesn't look like it.
        // TODO: I've reached out to MockBukkit for support...
        val location = Location(world,  0.0, 64.0, 0.0)
        val cow = world.spawnEntity(location, EntityType.COW)
        if (cow !is Leashable) return // we know this is true, just using this check for smart casting

        cow.teleport(player.location)
        player.setItemInHand(ItemStack(Material.LEAD, 64))

//        assertDoesNotThrow { leashEntityService.leashEntityToPlayer(player, cow) }

        cow.setLeashHolder(player)
        server.scheduler.performOneTick() // with or without doesn't work
        assertTrue(cow.isLeashed)
    }
}