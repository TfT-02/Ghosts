package com.me.tft_02.ghosts.runnables.player;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.datatypes.player.GhostPlayer;
import com.me.tft_02.ghosts.datatypes.player.PlayerProfile;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.managers.player.GhostManager;
import com.me.tft_02.ghosts.util.player.UserManager;

public class PlayerProfileLoadingTask extends BukkitRunnable {
    private static final int MAX_TRIES = 5;
    private final Player player;
    private int attempt = 0;

    public PlayerProfileLoadingTask(Player player) {
        this.player = player;
    }

    private PlayerProfileLoadingTask(Player player, int attempt) {
        this.player = player;
        this.attempt = attempt;
    }

    // WARNING: ASYNC TASK
    // DO NOT MODIFY THE McMMOPLAYER FROM THIS CODE
    @Override
    public void run() {
        // Quit if they logged out
        if (!player.isOnline()) {
            Ghosts.p.getLogger().info("Aborting profile loading recovery for " + player.getName() + " - player logged out");
            return;
        }

        // Increment attempt counter and try
        attempt++;

        PlayerProfile profile = Ghosts.getDatabaseManager().loadPlayerProfile(player.getName(), player.getUniqueId(), true);
        // If successful, schedule the apply
        if (profile.isLoaded()) {
            new ApplySuccessfulProfile(profile).runTask(Ghosts.p);
            return;
        }

        // If we've failed five times, give up
        if (attempt >= MAX_TRIES) {
            Ghosts.p.getLogger().severe("Giving up on attempting to load the PlayerProfile for " + player.getName());
            Ghosts.p.getServer().broadcast(LocaleLoader.getString("Profile.Loading.AdminFailureNotice", player.getName()), Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
            player.sendMessage(LocaleLoader.getString("Profile.Loading.Failure").split("\n"));
            return;
        }
        new PlayerProfileLoadingTask(player, attempt).runTaskLaterAsynchronously(Ghosts.p, 100 * attempt);
    }

    private class ApplySuccessfulProfile extends BukkitRunnable {
        private final PlayerProfile profile;

        private ApplySuccessfulProfile(PlayerProfile profile) {
            this.profile = profile;
        }

        // Synchronized task
        // No database access permitted
        @Override
        public void run() {
            if (!player.isOnline()) {
                Ghosts.p.getLogger().info("Aborting profile loading recovery for " + player.getName() + " - player logged out");
                return;
            }

            GhostPlayer ghostPlayer = new GhostPlayer(player, profile);
            UserManager.track(ghostPlayer);
            if (ghostPlayer.isGhost()) {
                GhostManager.ghosts.add(player.getUniqueId());
            }
//            ghostPlayer.actualizeRespawnATS();

            if (Config.getInstance().getShowProfileLoadedMessage()) {
                player.sendMessage(LocaleLoader.getString("Profile.Loading.Success"));
            }
        }
    }
}
