package com.me.tft_02.ghosts.runnables;

import org.bukkit.scheduler.BukkitRunnable;

import com.me.tft_02.ghosts.database.DatabaseManager;

public class SaveTimerTask extends BukkitRunnable {

    @Override
    public void run() {
        DatabaseManager.saveAllData();
    }
}
