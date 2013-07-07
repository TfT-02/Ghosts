package com.me.tft_02.ghosts.config;

import java.util.List;

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

    public List<String> disableInWorlds;

    public boolean getDestroyQuickloot() { return config.getBoolean("Removal.Destroy_Quickloot", true); }

    /* TOMBSTONE SETTINGS */
    public boolean getPreventDestroy() { return config.getBoolean("Tombstones.Prevent_Destroy", true); }
    public boolean getNoInterfere() { return config.getBoolean("Tombstones.No_Interfere", true); }
    public boolean getVoidCheck() { return config.getBoolean("Tombstones.Void_Check", true); }
    public boolean getCreeperProtection() { return config.getBoolean("Tombstones.Creeper_Protection", true); }

    public boolean getUseTombstoneSign() { return config.getBoolean("Tombstones.Place_Sign", true); }

    public String signMessage[] = new String[] {
            "{name}",
            "RIP",
            "{date}",
            "{time}"
    };

    // Remove Settings
    public int getTombRemoveTime() { return config.getInt("Tombstones.Remove.Time", 3600); }
    public int getLevelBasedTime() { return config.getInt("Tombstones.Remove.Level_Based_Time", 0); }
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

    /* MISC SETTINGS */
    public String getDateFormat() { return config.getString("Misc.Date_Format", "MM/dd/yyyy"); }
    public String getTimeFormat() { return config.getString("Misc.Time_Format", "hh:mm a"); }

    public boolean getGhostJumpEnabled() { return config.getBoolean("Ghost.Double_Jump.Enabled", true); }
    public boolean getGhostJumpSound() { return config.getBoolean("Ghost.Double_Jump.Sound", true); }
    public boolean getGhostJumpEffect() { return config.getBoolean("Ghost.Double_Jump.Effect", true); }
    /* @formatter:on */
}
