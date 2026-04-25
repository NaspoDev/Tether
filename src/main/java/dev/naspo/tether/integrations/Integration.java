package dev.naspo.tether.integrations;

import dev.naspo.tether.Tether;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class Integration {
    protected final Tether tetherPlugin;
    protected final String pluginName;
    protected boolean enabled = false;

    public Integration(Tether tetherPlugin, String pluginName) {
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
     * Checks if leashing is permitted based on this integration.
     *
     * @param location The location where leashing would occur. (i.e. the location of a clicked LivingEntity or fence post).
     * @param player  The player trying to leash.
     * @return true if the player is permitted to leash at that location.
     */
    public abstract boolean canLeash(Location location, Player player);
}
