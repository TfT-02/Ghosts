package com.me.tft_02.ghosts.util.player;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.datatypes.player.GhostPlayer;

import com.google.common.collect.ImmutableList;

public final class UserManager {

    private UserManager() {}

    /**
     * Track a new user.
     *
     * @param ghostPlayer the player profile to start tracking
     */
    public static void track(GhostPlayer ghostPlayer) {
        ghostPlayer.getPlayer().setMetadata(Ghosts.playerDataKey, new FixedMetadataValue(Ghosts.p, ghostPlayer));
    }

    /**
     * Remove a user.
     *
     * @param player The Player object
     */
    public static void remove(Player player) {
        player.removeMetadata(Ghosts.playerDataKey, Ghosts.p);
    }

    /**
     * Clear all users.
     */
    public static void clearAll() {
        for (Player player : Ghosts.p.getServer().getOnlinePlayers()) {
            remove(player);
        }
    }

    /**
     * Save all users ON THIS THREAD.
     */
    public static void saveAll() {
        ImmutableList<Player> onlinePlayers = ImmutableList.copyOf(Ghosts.p.getServer().getOnlinePlayers());
        Ghosts.p.debug("Saving ghostPlayers... (" + onlinePlayers.size() + ")");

        for (Player player : onlinePlayers) {
            getPlayer(player).getProfile().save();
        }
    }

    public static Collection<GhostPlayer> getPlayers() {
        Collection<GhostPlayer> playerCollection = new ArrayList<GhostPlayer>();

        for (Player player : Ghosts.p.getServer().getOnlinePlayers()) {
            if (hasPlayerDataKey(player)) {
                playerCollection.add(getPlayer(player));
            }
        }

        return playerCollection;
    }

    /**
     * Get the GhostPlayer of a player by name.
     *
     * @param playerName The name of the player whose GhostPlayer to retrieve
     * @return the player's GhostPlayer object
     */
    public static GhostPlayer getPlayer(String playerName) {
        return retrieveGhostPlayer(playerName, false);
    }

    public static GhostPlayer getOfflinePlayer(OfflinePlayer player) {
        if (player instanceof Player) {
            return getPlayer((Player) player);
        }

        return retrieveGhostPlayer(player.getName(), true);
    }

    public static GhostPlayer getOfflinePlayer(String playerName) {
        return retrieveGhostPlayer(playerName, true);
    }

    public static GhostPlayer getPlayer(Player player) {
        return (GhostPlayer) player.getMetadata(Ghosts.playerDataKey).get(0).value();
    }

    private static GhostPlayer retrieveGhostPlayer(String playerName, boolean offlineValid) {
        Player player = Ghosts.p.getServer().getPlayerExact(playerName);

        if (player == null) {
            if (!offlineValid) {
                Ghosts.p.getLogger().warning("A valid GhostPlayer object could not be found for " + playerName + ".");
            }

            return null;
        }

        return getPlayer(player);
    }

    public static boolean hasPlayerDataKey(Entity entity) {
        return entity != null && entity.hasMetadata(Ghosts.playerDataKey);
    }
}
