package com.me.tft_02.ghosts.runnables.player;

import org.bukkit.scheduler.BukkitRunnable;

import com.me.tft_02.ghosts.datatypes.player.PlayerProfile;

public class PlayerProfileSaveTask extends BukkitRunnable {
    private PlayerProfile playerProfile;

    public PlayerProfileSaveTask(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    @Override
    public void run() {
        playerProfile.save();
    }
}
