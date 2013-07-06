package com.me.tft_02.ghosts;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import com.me.tft_02.ghosts.commands.GhostsCommand;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.listeners.BlockListener;
import com.me.tft_02.ghosts.listeners.EntityListener;
import com.me.tft_02.ghosts.listeners.PlayerListener;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.managers.player.GhostManager;
import com.me.tft_02.ghosts.runnables.SaveTimerTask;
import com.me.tft_02.ghosts.runnables.TombRemoveTask;
import com.me.tft_02.ghosts.util.LogFilter;
import com.me.tft_02.ghosts.util.Misc;
import com.me.tft_02.ghosts.util.UpdateChecker;

public class Ghosts extends JavaPlugin {

    /* Managers */
    public GhostManager ghostManager;

    /* File Paths */
    private static String mainDirectory;

    public static Ghosts p;

    // Jar Stuff
    public static File ghosts;

    // Update Check
    public boolean updateAvailable;

    /**
     * Run things on enable.
     */
    @Override
    public void onEnable() {
        p = this;
        getLogger().setFilter(new LogFilter(this));

        setupFilePaths();

        registerEvents();
        getCommand("ghosts").setExecutor(new GhostsCommand());
        getCommand("resurrect").setExecutor(new GhostsCommand());

        ghostManager = new GhostManager(this);

        DatabaseManager.loadAllData();

        scheduleTasks();

        checkForUpdates();

        if (Config.getInstance().getStatsTrackingEnabled()) {
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            }
            catch (IOException e) {}
        }
    }

    /**
     * Run things on disable.
     */
    @Override
    public void onDisable() {
        DatabaseManager.saveAllData();

        getServer().getScheduler().cancelTasks(this);
    }

    public static String getMainDirectory() {
        return mainDirectory;
    }

    public void debug(String message) {
        getLogger().info("[Debug] " + message);
    }

    /**
     * Schedules all tasks
     */
    private void scheduleTasks() {
        // Start save timer. Run every 10 minutes (default)
        if (Config.getInstance().getSaveInterval() > 0) {
            new SaveTimerTask().runTaskTimer(this, 10 * 60 * Misc.TICK_CONVERSION_FACTOR, 10 * 60 * Misc.TICK_CONVERSION_FACTOR);
        }

        // Start removal timer. Run every 5 seconds
        if (Config.getInstance().getTombRemoveTime() > 0) {
            new TombRemoveTask().runTaskTimer(this, 5 * Misc.TICK_CONVERSION_FACTOR, 5 * Misc.TICK_CONVERSION_FACTOR);
        }
    }

    /**
     * Registers all event listeners
     */
    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        // Register events
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new BlockListener(), this);
        pluginManager.registerEvents(new EntityListener(), this);
    }

    /**
     * Setup the various storage file paths
     */
    private void setupFilePaths() {
        ghosts = getFile();
        mainDirectory = getDataFolder().getPath() + File.separator;
    }

    private void checkForUpdates() {
        if (Config.getInstance().getUpdateCheckEnabled()) {
            try {
                updateAvailable = UpdateChecker.updateAvailable();
            }
            catch (Exception e) {
                updateAvailable = false;
            }

            if (updateAvailable) {
                this.getLogger().log(Level.INFO, LocaleLoader.getString("UpdateChecker.Outdated"));
                this.getLogger().log(Level.INFO, LocaleLoader.getString("UpdateChecker.New_Available"));
            }
        }
    }
}
