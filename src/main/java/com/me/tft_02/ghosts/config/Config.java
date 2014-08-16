package com.me.tft_02.ghosts.config;

import java.util.List;

import org.bukkit.Material;

import com.me.tft_02.ghosts.datatypes.RecoveryType;
import com.me.tft_02.ghosts.items.ResurrectionScroll.Tier;
import com.me.tft_02.ghosts.util.StringUtils;

public class Config extends AutoUpdateConfigLoader {
    private static Config instance;

    private Config() {
        super("config.yml");
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }

        return instance;
    }

    @Override
    protected void loadKeys() {}

    /* @formatter:off */

    /* GENERAL SETTINGS */
    public String getLocale() { return config.getString("General.Locale", "en_us"); }
    public int getSaveInterval() { return config.getInt("General.Save_Interval", 10); }
    public boolean getStatsTrackingEnabled() { return config.getBoolean("General.Stats_Tracking", true); }
    public boolean getUpdateCheckEnabled() { return config.getBoolean("General.Update_Check", true); }
    public boolean getPreferBeta() { return config.getBoolean("General.Prefer_Beta", false); }
    public boolean getVerboseLoggingEnabled() { return config.getBoolean("General.Verbose_Logging", false); }
    public boolean getConfigOverwriteEnabled() { return config.getBoolean("General.Config_Update_Overwrite", true); }
    public List<String> getDisabledWorlds() { return config.getStringList("General.Disabled_In_Worlds"); }

    /* TOMBSTONE SETTINGS */
    // General Tombstone Settings
    public boolean getPreventDestroy() { return config.getBoolean("Tombstones.General.Prevent_Destroy", true); }
    public boolean getNoInterfere() { return config.getBoolean("Tombstones.General.No_Interfere", true); }
    public boolean getVoidCheck() { return config.getBoolean("Tombstones.General.Void_Check", true); }
    public boolean getCreeperProtection() { return config.getBoolean("Tombstones.General.Creeper_Protection", true); }
    public boolean getUseTombstoneSign() { return config.getBoolean("Tombstones.General.Place_Sign", true); }

    public String signMessage[] = new String[] {
            "{name}",
            "RIP",
            "{date}",
            "{time}"
    };

    // Losses Settings
    public int getLossesVanillaXP() { return config.getInt("Tombstones.Losses.Vanilla_XP", 10); }
    public int getLossesmcMMOXP() { return config.getInt("Tombstones.Losses.mcMMO_XP", 10); }
    public int getLossesItems() { return config.getInt("Tombstones.Losses.Items", 100); }

    // Recovery Settings
    public int getRecoveryVanillaXP(RecoveryType recoveryType, Tier tier) { return config.getInt("Tombstones.Recovery." + StringUtils.getPrettyRecoveryTypeString(recoveryType) + checkTier(recoveryType, tier) +".Vanilla_XP"); }
    public int getRecoverymcMMOXP(RecoveryType recoveryType, Tier tier) { return config.getInt("Tombstones.Recovery." + StringUtils.getPrettyRecoveryTypeString(recoveryType) + checkTier(recoveryType, tier) +".mcMMO_XP"); }
    public int getRecoveryItems(RecoveryType recoveryType, Tier tier) { return config.getInt("Tombstones.Recovery." + StringUtils.getPrettyRecoveryTypeString(recoveryType) + checkTier(recoveryType, tier) +".Items"); }
    public boolean getDestroyTomb(RecoveryType recoveryType, Tier tier) { return config.getBoolean("Tombstones.Recovery." + StringUtils.getPrettyRecoveryTypeString(recoveryType) + checkTier(recoveryType, tier) +".Destroy_Tomb"); }

    private String checkTier(RecoveryType recoveryType, Tier tier) {
        return (recoveryType == RecoveryType.RESURRECTION_SCROLL) ? ".Tier_" + tier.toNumerical() : "";
    }

    // Remove Settings
    public int getTombRemoveTime() { return config.getInt("Tombstones.Remove.Time", 3600); }
    public int getLevelBasedTime() { return config.getInt("Tombstones.Remove.Level_Based_Time", 0); }
    public boolean getDestroyQuickloot() { return config.getBoolean("Tombstones.Remove.After_Quickloot", true); }
    public boolean getRemoveWhenEmpty() { return config.getBoolean("Tombstones.Remove.When_Empty", true); }
    public boolean getKeepUntilEmpty() { return config.getBoolean("Tombstones.Remove.Keep_Until_Empty", false); }

    /* RESPAWN SETTINGS */
    public boolean getRespawnFromSky() { return config.getBoolean("Respawn.From_Sky", true); }
    public boolean getSetOnFire()  { return config.getBoolean("Respawn.Set_On_Fire", true); }
    public boolean getExplosionTrail() { return config.getBoolean("Respawn.Explosion_Trail", false); }
    public boolean getExplosionImpact() { return config.getBoolean("Respawn.Explosion_On_Impact", false); }
    public boolean getThunder() { return config.getBoolean("Respawn.Thunder", true); }
    public int getMinimumRange() { return config.getInt("Respawn.Minimum_Range", 40); }
    public int getMaximumRange() { return config.getInt("Respawn.Maximum_Range", 100); }

    /* ITEMS */
    public int getResurrectionScrollUseCost() { return config.getInt("Items.Resurrection_Scroll.Use_Cost", 1); }
    public Material getResurrectionScrollItem() { return Material.matchMaterial(config.getString("Items.Resurrection_Scroll.Item_Name", "PAPER")); }
    public Material getResurrectionScrollIngredientEdges() { return Material.matchMaterial(config.getString("Items.Resurrection_Scroll.IngredientEdges", "PAPER")); }
    public Material getResurrectionScrollIngredientMiddle() { return Material.matchMaterial(config.getString("Items.Resurrection_Scroll.IngredientMiddle", "GLOWSTONE_DUST")); }

    /* MISC SETTINGS */
    public String getDateFormat() { return config.getString("Misc.Date_Format", "MM/dd/yyyy"); }
    public String getTimeFormat() { return config.getString("Misc.Time_Format", "hh:mm a"); }

    /* @formatter:on */
}
