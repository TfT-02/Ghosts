package com.me.tft_02.ghosts.runnables.ghosts;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ExplosionTrailTask extends BukkitRunnable {
    private Player player;
    private int number;

    public ExplosionTrailTask(Player player, int explosions) {
        this.player = player;
        this.number = explosions;
    }

    @Override
    public void run() {
        if (number > 0) {
            Location location = player.getLocation();
            location.getWorld().createExplosion(location, 0);

            if (number <= 1) {
                this.cancel();
            }

            number--;
        }
    }
}
