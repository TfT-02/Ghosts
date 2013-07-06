package com.me.tft_02.ghosts.runnables.ghosts;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GroundExplosionTask extends BukkitRunnable {
    private Player player;

    public GroundExplosionTask(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (player.getVelocity().length() < 0.5) {
            player.getWorld().createExplosion(player.getLocation(), 5F, true);
            this.cancel();
        }
    }
}
