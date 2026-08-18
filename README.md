![Tether Banner](https://i.imgur.com/32kVuw7.png)

# Overview

Tether is the ultimate leashing solution. You can leash any mob or player, and can take full control over leashing
across your server with a multitude of configuration options and integrations.

It supports all default leashing mechanics, has a mob whitelist and blacklist, and integrates with your favourite land management plugins.

## Download
[<img src="https://i.imgur.com/SgO3sQT.png" width="180" alt="Available on Modrinth">](https://modrinth.com/plugin/tether)
[<img src="https://i.imgur.com/r1ZIvDN.png" width="180" alt="Available on Hangar">](https://hangar.papermc.io/Naspo/Tether)

Alternatively, you can also download Tether from its [releases on GitHub](https://github.com/NaspoDev/Tether/releases).

## Compatability
[<img src="https://i.imgur.com/E76tnrm.png" width="180" alt="Available for Paper">](https://papermc.io/)
[<img src="https://i.imgur.com/HkbCj5L.png" width="180" alt="Available for Purpur">](https://purpurmc.org/)

Versions before 4.0.0-beta.2 also support plain [Spigot](https://www.spigotmc.org/) servers.

# Features

- Leash any mob in Minecraft.
- Attach any leashed mob to a fence (right-click the fence).
- Leash any mobs together (sneak + right-click the mob).
- Entity leash blacklist and whitelist.
  - Tip: You can also write DEFAULT_LEASHABLE_ENTITIES to target all entities that are leashable by default.
- Leash players.

# Integrations

Tether integrates with the following plugins (for both entity and player leashing):

- WorldGuard (using the "leash" flag).
  - The "leash" flag respects the default INTERACT flag, which in turn respects the BUILD flag.
  - However, in the \_\_global\_\_ region, only BUILD is checked.
- GriefPrevention
- Towny
- Lands
- GriefDefender
- Residence (using the "leash" flag).
- Citizens - Respects your NPCs' "leashable" setting.

# Commands

- `/tether reload` - Reloads the configuration.

# Permissions

- `tether.leashplayers` - Allows leashing other players. (Make sure to also enable player leashing in the config).
- `tether.reload` - Allows /tether reload

# Images

![Leashing Villager](https://cdn.modrinth.com/data/cached_images/e8b4f5636bd2877dd899c48164c53dbc3d0d7476.png)

![Leashing Player](https://i.imgur.com/lwu19ne.png)

![Leashing Multiple Unleashable Mobs](https://cdn.modrinth.com/data/cached_images/bb276111001c757b2f64495ee64e69b9512756e0.png)

# Plugin Demo

See Tether in action [here](https://imgur.com/a/f8gp2PZ)!
