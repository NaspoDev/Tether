package me.naspo.tether.leash;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.TrustTypes;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.naspo.tether.core.Tether;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;

//Manages all protected land checks when leashing a mob or player.
public class ClaimCheckManager {
    private Claim claimGPR;
    private Town town;
    private LandsIntegration landsIntegration;
    private com.griefdefender.api.claim.Claim claimGD;

    private HashMap<String, Boolean> checkIsEnabled;

    private Tether plugin;

    public ClaimCheckManager(Tether plugin, boolean[] checkIsEnabled){
        this.plugin = plugin;
        this.checkIsEnabled = new HashMap<>();

        this.checkIsEnabled.put("griefprevention", checkIsEnabled[0]);
        this.checkIsEnabled.put("towny", checkIsEnabled[1]);
        this.checkIsEnabled.put("lands", checkIsEnabled[2]);
        this.checkIsEnabled.put("griefdefender", checkIsEnabled[3]);

        if (this.checkIsEnabled.get("lands")) {
            landsIntegration = new LandsIntegration(plugin);
        }
    }

    // ---------- Main/called checks for can leash ----------

    boolean canLeashMob(LivingEntity clicked, Player player) {
        if (checkIsEnabled.get("griefprevention")) {
            return griefPreventionMobCheck(clicked, player);
        }
        if (checkIsEnabled.get("towny")) {
            return townyMobCheck(clicked, player);
        }
        if (checkIsEnabled.get("lands")) {
            return landsMobCheck(clicked, player);
        }
        if (checkIsEnabled.get("griefdefender")) {
            return griefDefenderMobCheck(clicked, player);
        }

        return true;
    }

    boolean canLeashPlayer(Player clicked, Player player) {
        if (checkIsEnabled.get("griefprevention")) {
            return griefPreventionPlayerCheck(clicked, player);
        }
        if (checkIsEnabled.get("towny")) {
            return townyPlayerCheck(clicked, player);
        }
        if (checkIsEnabled.get("lands")) {
            return landsPlayerCheck(clicked, player);
        }
        if (checkIsEnabled.get("griefdefender")) {
            return griefDefenderPlayerCheck(clicked, player);
        }

        return true;
    }

    // ---------- Individual (private) checks ----------

    // --- Mob checks ---

    private boolean griefPreventionMobCheck(LivingEntity clicked, Player player) {
        claimGPR = GriefPrevention.instance.dataStore.getClaimAt(clicked.getLocation(), true, null);
        if (claimGPR != null) {
            return claimGPR.hasExplicitPermission(player.getUniqueId(), ClaimPermission.Access);
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

    // --- Player checks ---

    private boolean griefPreventionPlayerCheck(Player clicked, Player player) {
        claimGPR = GriefPrevention.instance.dataStore.getClaimAt(clicked.getLocation(), true, null);
        if (claimGPR != null) {
            return claimGPR.hasExplicitPermission(player.getUniqueId(), ClaimPermission.Access);
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
