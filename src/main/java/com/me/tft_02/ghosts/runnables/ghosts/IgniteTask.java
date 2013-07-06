package com.me.tft_02.ghosts.runnables.ghosts;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class IgniteTask extends BukkitRunnable {
    private Player player;
    private int ticks;

    public IgniteTask(Player player, int ticks) {
        this.player = player;
        this.ticks = ticks;
    }

    @Override
    public void run() {
        player.setFireTicks(ticks);
    }
}
