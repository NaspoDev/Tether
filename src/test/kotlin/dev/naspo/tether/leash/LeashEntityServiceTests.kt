package dev.naspo.tether.leash

import dev.naspo.tether.Tether
import dev.naspo.tether.config.ConfigAccessor
import dev.naspo.tether.config.ConfigKeys
import dev.naspo.tether.integrations.IntegrationManager
import io.papermc.paper.entity.Leashable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Cow
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
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

    // Note: DLE stands for "Default Leashable Entity".

    @Test
    fun `test isEntityRestricted with blacklist`() {
        plugin.config.set(ConfigKeys.EntityLeash.entityBlacklist.path, listOf("COW"))
        val location = Location(world, 0.0, 64.0, 0.0)
        val cow: Entity = world.spawnEntity(location, EntityType.COW)
        assertTrue(leashEntityService.isEntityRestricted(cow))
    }

    @Test
    fun `test isEntityRestricted with whitelist`() {
        plugin.config.set(ConfigKeys.EntityLeash.useWhitelistOverBlacklist.path, true)
        plugin.config.set(ConfigKeys.EntityLeash.entityWhitelist.path, listOf("COW"))
        val location = Location(world, 0.0, 64.0, 0.0)
        val cow: Entity = world.spawnEntity(location, EntityType.COW)
        assertFalse(leashEntityService.isEntityRestricted(cow))
    }

    // TODO: Once MockBukkit fixes leashing, test leashEntityToPlayer with a bunch of conditions.
}