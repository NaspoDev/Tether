package dev.naspo.tether.integrations.toggleableintegrations;

import dev.naspo.tether.Tether;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class GriefPreventionIntegration extends ToggleableIntegration {
    private DataStore dataStore;

    GriefPreventionIntegration(Tether tetherPlugin) {
        super(tetherPlugin, "GriefPrevention", "griefprevention");
    }

    @Override
    protected boolean onEnable() {
        dataStore = GriefPrevention.instance.dataStore;
        return true;
    }
}
