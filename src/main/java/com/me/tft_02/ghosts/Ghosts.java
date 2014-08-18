package com.me.tft_02.ghosts;

import java.io.File;
import java.io.IOException;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.me.tft_02.ghosts.commands.GhostsCommand;
import com.me.tft_02.ghosts.commands.ResurrectCommand;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.items.ResurrectionScroll;
import com.me.tft_02.ghosts.listeners.BlockListener;
import com.me.tft_02.ghosts.listeners.EntityListener;
import com.me.tft_02.ghosts.listeners.InventoryListener;
import com.me.tft_02.ghosts.listeners.PlayerListener;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.managers.player.GhostManager;
import com.me.tft_02.ghosts.runnables.SaveTimerTask;
import com.me.tft_02.ghosts.runnables.TombRemoveTask;
import com.me.tft_02.ghosts.util.LogFilter;
import com.me.tft_02.ghosts.util.Misc;

import net.gravitydevelopment.updater.ghosts.Updater;
import org.mcstats.Metrics;

public class Ghosts extends JavaPlugin {

    /* Managers */
    private GhostManager ghostManager;

    /* File Paths */
    private static String mainDirectory;

    public static Ghosts p;

    // Jar Stuff
    public static File ghosts;

    // Dependencies
    private boolean mcMMOEnabled = false;

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
        registerCustomRecipes();

        getCommand("ghosts").setExecutor(new GhostsCommand());
        getCommand("resurrect").setExecutor(new ResurrectCommand());

        ghostManager = new GhostManager(this);

        DatabaseManager.loadAllData();

        scheduleTasks();
        setupMcMMO();

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

    private void setupMcMMO() {
        if (getServer().getPluginManager().isPluginEnabled("mcMMO")) {
            mcMMOEnabled = true;
            debug("mcMMO found!");
        }
    }

    public boolean isMcMMOEnabled() {
        return mcMMOEnabled;
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
        pluginManager.registerEvents(new InventoryListener(), this);
    }

    private void registerCustomRecipes() {
        getServer().addRecipe(ResurrectionScroll.getResurrectionScrollRecipe());
    }

    /**
     * Setup the various storage file paths
     */
    private void setupFilePaths() {
        ghosts = getFile();
        mainDirectory = getDataFolder().getPath() + File.separator;
    }

    private void checkForUpdates() {
        if (!Config.getInstance().getUpdateCheckEnabled()) {
            return;
        }

        Updater updater = new Updater(this, 60787, ghosts, Updater.UpdateType.NO_DOWNLOAD, false);

        if (updater.getResult() != Updater.UpdateResult.UPDATE_AVAILABLE) {
            this.updateAvailable = false;
            return;
        }

        if (updater.getLatestType().equals("beta") && !Config.getInstance().getPreferBeta()) {
            this.updateAvailable = false;
            return;
        }

        this.updateAvailable = true;
        getLogger().info(LocaleLoader.getString("UpdateChecker.Outdated"));
        getLogger().info(LocaleLoader.getString("UpdateChecker.New_Available"));
    }

    public GhostManager getGhostManager() {
        return ghostManager;
    }
}
