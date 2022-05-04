package me.naspo.tether.leash;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.naspo.tether.core.Tether;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

//Manages all protected land checks when leashing a mob or player.
public class ClaimCheckManager {
    private Claim claim;
    private Town town;
    private LandsIntegration landsIntegration;

    private boolean griefPreventionIsEnabled;
    private boolean townyIsEnabled;
    private boolean landsIsEnabled;

    private Tether plugin;

    public ClaimCheckManager(Tether plugin, boolean griefPreventionIsEnabled, boolean townyIsEnabled, boolean landsIsEnabled){
        this.plugin = plugin;

        this.griefPreventionIsEnabled = griefPreventionIsEnabled;
        this.townyIsEnabled = townyIsEnabled;
        this.landsIsEnabled = landsIsEnabled;

        if (landsIsEnabled) {
            landsIntegration = new LandsIntegration(plugin);
        }
    }

    // ---------- Main/called checks for can leash ----------

    boolean canLeashMob(LivingEntity clicked, Player player) {
        if (griefPreventionIsEnabled) {
            return griefPreventionMobCheck(clicked, player);
        }
        if (townyIsEnabled) {
            return townyMobCheck(clicked, player);
        }
        if (landsIsEnabled) {
            return landsMobCheck(clicked, player);
        }

        return true;
    }

    boolean canLeashPlayer(Player clicked, Player player) {
        if (griefPreventionIsEnabled) {
            return griefPreventionPlayerCheck(clicked, player);
        }
        if (townyIsEnabled) {
            return townyPlayerCheck(clicked, player);
        }
        if (landsIsEnabled) {
            return landsPlayerCheck(clicked, player);
        }

        return true;
    }

    // ---------- Individual (private) checks ----------

    // --- Mob checks ---

    private boolean griefPreventionMobCheck(LivingEntity clicked, Player player) {
        claim = GriefPrevention.instance.dataStore.getClaimAt(clicked.getLocation(), true, null);
        if (claim != null) {
            return claim.hasExplicitPermission(player.getUniqueId(), ClaimPermission.Access);
        }
        return true;
    }

    private boolean townyMobCheck(LivingEntity clicked, Player player) {
        try {
            town = TownyAPI.getInstance().getTownBlock(clicked.getLocation()).getTown();
        } catch (Exception e) {
            return true;
        }
        return town.getTrustedResidents().contains(player.getUniqueId());
    }

    private boolean landsMobCheck(LivingEntity clicked, Player player) {
        if (landsIntegration.isClaimed(clicked.getLocation())) {
            return landsIntegration.getAreaByLoc(clicked.getLocation()).getLand()
                    .getTrustedPlayers()
                    .contains(player.getUniqueId());
        }
        return true;
    }

    // --- Player checks ---

    private boolean griefPreventionPlayerCheck(Player clicked, Player player) {
        claim = GriefPrevention.instance.dataStore.getClaimAt(clicked.getLocation(), true, null);
        if (claim != null) {
            return claim.hasExplicitPermission(player.getUniqueId(), ClaimPermission.Access);
        }
        return true;
    }

    private boolean townyPlayerCheck(Player clicked, Player player) {
        try {
            town = TownyAPI.getInstance().getTownBlock(clicked.getLocation()).getTown();
        } catch (Exception e) {
            return true;
        }
        return town.getTrustedResidents().contains(player.getUniqueId());
    }

    private boolean landsPlayerCheck(Player clicked, Player player) {
        if (landsIntegration.isClaimed(clicked.getLocation())) {
            return landsIntegration.getAreaByLoc(clicked.getLocation()).getLand()
                    .getTrustedPlayers()
                    .contains(player.getUniqueId());
        }
        return true;
    }
}
