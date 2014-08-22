package com.me.tft_02.ghosts.datatypes.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.datatypes.StatsType;

public class GhostPlayer {
    private Player player;
    private PlayerProfile profile;

    private List<ItemStack> playerGhostItems = new ArrayList<ItemStack>();

    private final FixedMetadataValue playerMetadata;

    public GhostPlayer(Player player, PlayerProfile profile) {
        UUID uuid = player.getUniqueId();

        this.player = player;
        playerMetadata = new FixedMetadataValue(Ghosts.p, uuid);
        this.profile = profile;
    }

    /*
     * Players & Profiles
     */

    public Player getPlayer() {
        return player;
    }

    public PlayerProfile getProfile() {
        return profile;
    }

    public FixedMetadataValue getPlayerMetadata() {
        return playerMetadata;
    }

    /*
     * Ghost items saving
     */

    public List<ItemStack> getPlayerGhostItems() {
        return playerGhostItems;
    }

    public void setPlayerGhostItems(List<ItemStack> playerGhostItems) {
        this.playerGhostItems = playerGhostItems;
    }

    /*
     * From PlayerProfile
     */

    public boolean isGhost() {
        return profile.isGhost();
    }

    public void setIsGhost(boolean isGhost) {
        profile.setIsGhost(isGhost);
    }

    public boolean getRespawn() {
        return profile.respawn;
    }

    public void setRespawn(boolean respawn) {
        profile.setRespawn(respawn);
    }

    public Location getLastDeathLocation() {
        return profile.getLastDeathLocation();
    }

    public void setLastDeathLocation(Location lastDeathLocation) {
        profile.setLastDeathLocation(lastDeathLocation);
    }

    public int getSavedLostVanillaXP() {
        return profile.getSavedLostVanillaXP();
    }

    public void setSavedLostVanillaXP(int savedLostVanillaXP) {
        profile.setSavedLostVanillaXP(savedLostVanillaXP);
    }

    public int getSavedRemainingVanillaXP() {
        return profile.getSavedRemainingVanillaXP();
    }

    public void setSavedRemainingVanillaXP(int savedRemainingVanillaXP) {
        profile.setSavedRemainingVanillaXP(savedRemainingVanillaXP);
    }

    public HashMap<String, Integer> getSavedLostMcMMOXP() {
        return profile.getSavedLostMcMMOXP();
    }

    public void setSavedLostMcMMOXP(HashMap<String, Integer> lostMcMMOXP) {
        profile.setSavedLostMcMMOXP(lostMcMMOXP);
    }

    public void clearSavedLostMcMMOXP() {
        profile.clearSavedLostMcMMOXP();
    }

    public void increaseStats(StatsType statsType) {
        profile.increaseStats(statsType);
    }

    public int getStats(StatsType statsType) {
        return profile.getStats(statsType);
    }
}
