package dev.naspo.tether.integrations;

import dev.naspo.tether.Tether;
import org.bukkit.entity.LivingEntity;
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
     * Initializes the integration.
     * Checks if the integration should enable, and runs logic to do so.
     *
     * @return true if the integration was enabled.
     */
    public boolean init() {
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

    private boolean isPluginPresent() {
        return tetherPlugin.getServer().getPluginManager().getPlugin(pluginName) != null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Checks if the clicked LivingEntity can be leashed based on this integration.
     *
     * @param clicked The clicked LivingEntity. (Includes Player).
     * @param player  The player trying to leash.
     * @return true if the player is permitted to leash the clicked LivingEntity.
     */
    public abstract boolean canLeash(LivingEntity clicked, Player player);
}
