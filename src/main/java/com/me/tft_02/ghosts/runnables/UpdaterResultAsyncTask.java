package com.me.tft_02.ghosts.runnables;

import org.bukkit.scheduler.BukkitRunnable;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.locale.LocaleLoader;

import net.gravitydevelopment.updater.ghosts.Updater;
import net.gravitydevelopment.updater.ghosts.Updater.UpdateResult;
import net.gravitydevelopment.updater.ghosts.Updater.UpdateType;

public class UpdaterResultAsyncTask extends BukkitRunnable {
    private Ghosts plugin;

    public UpdaterResultAsyncTask(Ghosts plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Updater updater = new Updater(plugin, 60787, Ghosts.ghosts, UpdateType.NO_DOWNLOAD, false);

        if (updater.getResult() != UpdateResult.UPDATE_AVAILABLE) {
            plugin.setUpdateAvailable(false);
            return;
        }

        if (updater.getLatestType().equals("beta") && !Config.getInstance().getPreferBeta()) {
            plugin.setUpdateAvailable(false);
            return;
        }

        plugin.setUpdateAvailable(true);
        plugin.getLogger().info(LocaleLoader.getString("UpdateChecker.Outdated"));
        plugin.getLogger().info(LocaleLoader.getString("UpdateChecker.New_Available"));
    }
}
