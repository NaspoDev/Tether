package dev.naspo.tether.integrations;

import dev.naspo.tether.Tether;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class Integration {
    protected final Tether tetherPlugin;
    protected final String pluginName;
    protected boolean enabled = false;

    public Integration(final Tether tetherPlugin, final String pluginName) {
        this.tetherPlugin = tetherPlugin;
        this.pluginName = pluginName;
    }

    /**
     * Enables the integration (if it should).
     *
     * @return true if the integration was enabled.
     */
    public boolean enable() {
        if (isPluginPresent()) {
            enabled = onEnable();
            return enabled;
        }
        return false;
    }

    /**
     * The actual logic to enable the integration.
     *
     * @return true if the integration successfully enabled.
     */
    protected abstract boolean onEnable();

    protected boolean isPluginPresent() {
        return tetherPlugin.getServer().getPluginManager().getPlugin(pluginName) != null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Checks if a mob can be leashed at the given location based on this integration.
     *
     * @param location The location the mob is trying to be leashed at.
     * @param player   The player trying to leash.
     * @return true if the player is permitted to leash a mob at the given location.
     */
    public abstract boolean canLeash(Location location, Player player);
}
