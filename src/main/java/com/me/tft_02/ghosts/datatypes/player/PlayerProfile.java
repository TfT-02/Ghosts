package com.me.tft_02.ghosts.datatypes.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.datatypes.StatsType;
import com.me.tft_02.ghosts.managers.player.GhostManager;
import com.me.tft_02.ghosts.runnables.player.PlayerProfileSaveTask;

public class PlayerProfile {
    private final String playerName;
    private UUID uuid;
    private boolean loaded;
    private volatile boolean changed;

    boolean isGhost;
    boolean respawn;
    Location lastDeathLocation;
    int savedLostVanillaXP;
    int savedRemainingVanillaXP;
    int savedLostMcMMOXP;
    int savedRemainingMcMMOXP;
    Map<StatsType, Integer> stats = new HashMap<StatsType, Integer>();

    public PlayerProfile(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;

        isGhost = false;
        respawn = false;
        lastDeathLocation = null;
        savedLostVanillaXP = 0;
        savedRemainingVanillaXP = 0;
        savedLostMcMMOXP = 0;
        savedRemainingMcMMOXP = 0;

        for (StatsType statsType : StatsType.values()) {
            stats.put(statsType, 0);
        }
    }

    public PlayerProfile(UUID uuid, String playerName, boolean isLoaded) {
        this(uuid, playerName);
        this.loaded = isLoaded;
    }

    public PlayerProfile(UUID uuid, String playerName, boolean isGhost, boolean respawn, Location lastDeathLocation, int savedLostVanillaXP, int savedRemainingVanillaXP, int savedLostMcMMOXP, int savedRemainingMcMMOXP, Map<StatsType, Integer> stats) {
        this.playerName = playerName;
        this.uuid = uuid;

        this.isGhost = isGhost;
        this.respawn = respawn;
        this.lastDeathLocation = lastDeathLocation;
        this.savedLostVanillaXP = savedLostVanillaXP;
        this.savedRemainingVanillaXP = savedRemainingVanillaXP;
        this.savedLostMcMMOXP = savedLostMcMMOXP;
        this.savedRemainingMcMMOXP = savedRemainingMcMMOXP;
        this.stats = stats;

        loaded = true;
    }

    public void scheduleAsyncSave() {
        new PlayerProfileSaveTask(this).runTaskAsynchronously(Ghosts.p);
    }

    public void save() {
        if (!changed || !loaded) {
            return;
        }

        // TODO should this part be synchronized?
        isGhost = GhostManager.ghosts.contains(uuid);
        PlayerProfile profileCopy = new PlayerProfile(uuid, playerName, isGhost, respawn, lastDeathLocation, savedLostVanillaXP, savedRemainingVanillaXP, savedLostMcMMOXP, savedRemainingMcMMOXP, stats);
        changed = !Ghosts.getDatabaseManager().saveUser(profileCopy);

        if (changed) {
            Ghosts.p.getLogger().warning("PlayerProfile saving failed for player: " + playerName + " " + uuid);
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void setUniqueId(UUID uuid) {
        changed = true;

        this.uuid = uuid;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isGhost() {
        return isGhost;
    }

    public void setIsGhost(boolean isGhost) {
        this.isGhost = isGhost;
    }

    public boolean getRespawn() {
        return respawn;
    }

    public void setRespawn(boolean respawn) {
        this.respawn = respawn;
    }

    public Location getLastDeathLocation() {
        return lastDeathLocation;
    }

    public void setLastDeathLocation(Location lastDeathLocation) {
        this.lastDeathLocation = lastDeathLocation;
    }

    public int getSavedLostVanillaXP() {
        return savedLostVanillaXP;
    }

    public void setSavedLostVanillaXP(int savedLostVanillaXP) {
        changed = true;

        this.savedLostVanillaXP = savedLostVanillaXP;
    }

    public int getSavedRemainingVanillaXP() {
        return savedRemainingVanillaXP;
    }

    public void setSavedRemainingVanillaXP(int savedRemainingVanillaXP) {
        changed = true;

        this.savedRemainingVanillaXP = savedRemainingVanillaXP;
    }

    public int getSavedLostMcMMOXP() {
        return savedLostMcMMOXP;
    }

    public void setSavedLostMcMMOXP(int savedLostMcMMOXP) {
        changed = true;

        this.savedLostMcMMOXP = savedLostMcMMOXP;
    }

    public int getSavedRemainingMcMMOXP() {
        return savedRemainingMcMMOXP;
    }

    public void setSavedRemainingMcMMOXP(int savedRemainingMcMMOXP) {
        changed = true;

        this.savedRemainingMcMMOXP = savedRemainingMcMMOXP;
    }

    public void increaseStats(StatsType statsType) {
        changed = true;

        int amount = stats.get(statsType);
        stats.put(statsType, amount + 1);
    }

    public int getStats(StatsType statsType) {
        return stats.get(statsType);
    }
}
