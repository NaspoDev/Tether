![Tether Banner](https://i.imgur.com/32kVuw7.png)

## Overview
The Tether plugin allows players to leash any mob, and other players.

It supports all default leashing mechanics, has a mob whitelist and blacklist, and integrates with your favourite land management plugins.

Available on [Spigot](https://www.spigotmc.org/resources/tether.100941/), [Modrinth](https://modrinth.com/plugin/tether), and [Hangar](https://hangar.papermc.io/Naspo/Tether).

## Features
- Leash any mob in Minecraft.
- Attach a leashed mob to a fence (right-click the fence).
- Leash mobs together (sneak + right-click the mob).
- Mob leash blacklist and whitelist.
- Leash players.

## Integrations
Tether integrates with the following plugins (for both mob and player leashing):
- WorldGuard (using the "leash" flag).
  - The "leash" flag respects the default INTERACT flag, which in turn respects the BUILD flag.
  - However, in the \_\_global\_\_ region, only BUILD is checked.
- GriefPrevention
- Towny
- Lands
- GriefDefender
- Residence (using the "leash" flag).
- Citizens - Respects your NPCs' "leashable" setting.

## Commands
- `/tether reload` - Reloads the configuration.


## Permissions
- `tether.leashplayers` - Allows leashing other players. (Make sure to also enable player leashing in the config).
- `tether.reload` - Allows /tether reload

## Images
![Leashing Villager](https://cdn.modrinth.com/data/cached_images/e8b4f5636bd2877dd899c48164c53dbc3d0d7476.png)

![Leashing Player](https://i.imgur.com/lwu19ne.png)

![Leashing Multiple Unleashable Mobs](https://cdn.modrinth.com/data/cached_images/bb276111001c757b2f64495ee64e69b9512756e0.png)

## Plugin Demo
See Tether in action [here](https://imgur.com/a/f8gp2PZ)!