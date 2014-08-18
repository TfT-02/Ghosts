package com.me.tft_02.ghosts.runnables.ghosts;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.me.tft_02.ghosts.util.ExperienceManager;

public class SetXPTask extends BukkitRunnable {
    private Player player;
    private int amount;

    public  SetXPTask(Player player, int amount) {
        this.player = player;
        this.amount = amount;
    }

    @Override
    public void run() {
        ExperienceManager manager = new ExperienceManager(player);
        manager.setExp(amount);
    }
}
