package dev.naspo.tether.services;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.TrustTypes;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import dev.naspo.tether.Tether;
import dev.naspo.tether.services.hookmanager.Hook;
import dev.naspo.tether.services.hookmanager.HookManager;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

//Manages all protected land checks when leashing a mob or player.
public class ClaimCheckService {
    private DataStore gpDataStore;
    private Town town;
    private LandsIntegration landsIntegration;
    private com.griefdefender.api.claim.Claim claimGD;

    private Tether plugin;
    private HookManager hookManager;

    public ClaimCheckService(Tether plugin, HookManager hookManager) {
        this.plugin = plugin;
        this.hookManager = hookManager;

        // Initializing anything needed for integrations.
        if (hookManager.isHookEnabled(Hook.GRIEF_PREVENTION)) {
            this.gpDataStore = GriefPrevention.instance.dataStore;
        }
        if (hookManager.isHookEnabled(Hook.LANDS)) {
            this.landsIntegration = new LandsIntegration(plugin);
        }
    }

    // Called when leashing a mob to check if the hooks (land claims) allow it.
    boolean canLeashMob(LivingEntity clicked, Player player) {
        if (hookManager.isHookEnabled(Hook.GRIEF_PREVENTION)) {
            return griefPreventionMobCheck(clicked, player);
        }
        if (hookManager.isHookEnabled(Hook.TOWNY)) {
            return townyMobCheck(clicked, player);
        }
        if (hookManager.isHookEnabled(Hook.LANDS)) {
            return landsMobCheck(clicked, player);
        }
        if (hookManager.isHookEnabled(Hook.GRIEF_DEFENDER)) {
            return griefDefenderMobCheck(clicked, player);
        }

        return true;
    }

    // Called when leashing a player to check if the hooks (land claims) allow it.
    public boolean canLeashPlayer(Player clicked, Player player) {
        if (hookManager.isHookEnabled(Hook.GRIEF_PREVENTION)) {
            return griefPreventionPlayerCheck(clicked, player);
        }
        if (hookManager.isHookEnabled(Hook.TOWNY)) {
            return townyPlayerCheck(clicked, player);
        }
        if (hookManager.isHookEnabled(Hook.LANDS)) {
            return landsPlayerCheck(clicked, player);
        }
        if (hookManager.isHookEnabled(Hook.GRIEF_DEFENDER)) {
            return griefDefenderPlayerCheck(clicked, player);
        }

        return true;
    }

    // The following methods check if leashing of a mob will be allowed, based on land claims...

    private boolean griefPreventionMobCheck(LivingEntity clicked, Player player) {
        Claim claim = gpDataStore.getClaimAt(clicked.getLocation(), true, null);
        // If there is a claim here, return true if the player has explicit permission or is ignoring claims.
        if (claim != null) {
            return claim.hasExplicitPermission(player.getUniqueId(), ClaimPermission.Access) ||
                    GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId()).ignoreClaims;
        }
        // Otherwise always return true if there is no claim.
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

    private boolean griefDefenderMobCheck(LivingEntity clicked, Player player) {
        claimGD = GriefDefender.getCore().getClaimAt(clicked.getLocation());
        if (claimGD != null) {
            if (claimGD.isWilderness()) {
                return true;
            } else {
                return claimGD.isUserTrusted(player.getUniqueId(), TrustTypes.ACCESSOR);
            }
        }
        return true;
    }

    // The following methods check if leashing of a player will be allowed, based on land claims...

    private boolean griefPreventionPlayerCheck(Player clicked, Player player) {
        Claim claim = gpDataStore.getClaimAt(clicked.getLocation(), true, null);
        // If there is a claim here, return true if the player has explicit permission or is ignoring claims.
        if (claim != null) {
            return claim.hasExplicitPermission(player.getUniqueId(), ClaimPermission.Access) ||
                    GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId()).ignoreClaims;
        }
        // Otherwise always return true if there is no claim.
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

    private boolean griefDefenderPlayerCheck(Player clicked, Player player) {
        claimGD = GriefDefender.getCore().getClaimAt(clicked.getLocation());
        if (claimGD != null) {
            if (claimGD.isWilderness()) {
                return true;
            } else {
                return claimGD.isUserTrusted(player.getUniqueId(), TrustTypes.ACCESSOR);
            }
        }
        return true;
    }
}
