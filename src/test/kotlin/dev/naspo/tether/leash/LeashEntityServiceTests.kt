package dev.naspo.tether.leash

import dev.naspo.tether.Tether
import dev.naspo.tether.config.ConfigAccessor
import dev.naspo.tether.integrations.IntegrationManager
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Cow
import org.bukkit.entity.EntityType
import org.junit.jupiter.api.AfterEach
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
    private lateinit var world: World
    private lateinit var player: PlayerMock

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(Tether::class.java)
        configAccessor = ConfigAccessor(plugin)
        integrationManager = IntegrationManager(plugin, configAccessor)
        leashEntityService = LeashEntityService(plugin, configAccessor, integrationManager)
        world = WorldMock()
        player = server.addPlayer()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    // Note: DLE stands for "Default Leashable Entity"

    @Test
    fun `test leashEntityToPlayer with DLE`() {
        var location = Location(world,  0.0, 64.0, 0.0)
        var cow = world.spawnEntity(location, EntityType.COW)
        assertDoesNotThrow {
            leashEntityService.leashEntityToPlayer(player, cow)
        }
    }
}