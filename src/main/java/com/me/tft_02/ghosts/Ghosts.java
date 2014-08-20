package com.me.tft_02.ghosts;

import java.io.File;
import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.me.tft_02.ghosts.commands.GhostsCommand;
import com.me.tft_02.ghosts.commands.GiveUpCommand;
import com.me.tft_02.ghosts.commands.ResurrectCommand;
import com.me.tft_02.ghosts.commands.StatsCommand;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.database.DatabaseManagerFactory;
import com.me.tft_02.ghosts.database.TombstoneDatabase;
import com.me.tft_02.ghosts.items.ResurrectionScroll;
import com.me.tft_02.ghosts.listeners.BlockListener;
import com.me.tft_02.ghosts.listeners.EntityListener;
import com.me.tft_02.ghosts.listeners.InventoryListener;
import com.me.tft_02.ghosts.hooks.McMMOListener;
import com.me.tft_02.ghosts.listeners.PlayerListener;
import com.me.tft_02.ghosts.managers.player.GhostManager;
import com.me.tft_02.ghosts.runnables.SaveTimerTask;
import com.me.tft_02.ghosts.runnables.TombRemoveTask;
import com.me.tft_02.ghosts.runnables.UpdaterResultAsyncTask;
import com.me.tft_02.ghosts.runnables.player.PlayerProfileLoadingTask;
import com.me.tft_02.ghosts.util.LogFilter;
import com.me.tft_02.ghosts.util.Misc;
import com.me.tft_02.ghosts.util.player.UserManager;

import org.mcstats.Metrics;

public class Ghosts extends JavaPlugin {
    /* Managers */
    private GhostManager ghostManager;
    private static DatabaseManager databaseManager;

    /* File Paths */
    private static String mainDirectory;
    private static String flatFileDirectory;
    private static String usersFile;

    public static Ghosts p;

    // Jar Stuff
    public static File ghosts;

    // Dependencies
    private boolean mcMMOEnabled = false;

    // Update Check
    public boolean updateAvailable;

    /* Metadata Values */
    public final static String playerDataKey = "Ghosts: Player Data";

    public static FixedMetadataValue metadataValue;

    /**
     * Run things on enable.
     */
    @Override
    public void onEnable() {
        try {
            p = this;
            getLogger().setFilter(new LogFilter(this));
            metadataValue = new FixedMetadataValue(this, true);

            setupFilePaths();
            Config.getInstance();

            databaseManager = DatabaseManagerFactory.getDatabaseManager();

            registerEvents();
            registerCustomRecipes();

            ghostManager = new GhostManager(this);

            TombstoneDatabase.loadAllData();
            setupMcMMO();

            for (Player player : getServer().getOnlinePlayers()) {
                new PlayerProfileLoadingTask(player).runTaskLaterAsynchronously(Ghosts.p, 1); // 1 Tick delay to ensure the player is marked as online before we begin loading
            }

            debug("Version " + getDescription().getVersion() + " is enabled!");

            scheduleTasks();
            registerCommands();
            setupMetrics();
            checkForUpdates();
        }
        catch (Throwable t) {
            getLogger().severe("There was an error while enabling Ghosts!");

            if (!(t instanceof ExceptionInInitializerError)) {
                t.printStackTrace();
            }
            else {
                getLogger().info("Please do not replace the Ghosts jar while the server is running.");
            }

            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * Run things on disable.
     */
    @Override
    public void onDisable() {
        try {
            TombstoneDatabase.saveAllData();
            UserManager.saveAll();
            UserManager.clearAll();
        }
        catch (NullPointerException ignored) {}

        debug("Canceling all tasks...");
        getServer().getScheduler().cancelTasks(this); // This removes our tasks
        debug("Unregister all events...");
        HandlerList.unregisterAll(this); // Cancel event registrations

        databaseManager.onDisable();
        debug("Was disabled."); // How informative!
    }

    public static String getMainDirectory() {
        return mainDirectory;
    }

    public static String getFlatFileDirectory() {
        return flatFileDirectory;
    }

    public static String getUsersFilePath() {
        return usersFile;
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public void debug(String message) {
        getLogger().info("[Debug] " + message);
    }

    private void setupMcMMO() {
        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.isPluginEnabled("mcMMO")) {
            mcMMOEnabled = true;
            debug("mcMMO found!");
            pluginManager.registerEvents(new McMMOListener(), this);
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
        getServer().addRecipe(ResurrectionScroll.getResurrectionScrollUpgradeRecipe());
    }

    private void registerCommands() {
        getCommand("ghosts").setExecutor(new GhostsCommand());
        getCommand("resurrect").setExecutor(new ResurrectCommand());
        getCommand("giveup").setExecutor(new GiveUpCommand());
        getCommand("ghstats").setExecutor(new StatsCommand());
    }

    /**
     * Setup the various storage file paths
     */
    private void setupFilePaths() {
        ghosts = getFile();
        mainDirectory = getDataFolder().getPath() + File.separator;
        flatFileDirectory = mainDirectory + "flatfile" + File.separator;
        usersFile = flatFileDirectory + "ghosts.users";
    }

    private void checkForUpdates() {
        if (!Config.getInstance().getUpdateCheckEnabled()) {
            return;
        }

        new UpdaterResultAsyncTask(this).runTaskAsynchronously(Ghosts.p);
    }

    private void setupMetrics() {
        if (Config.getInstance().getStatsTrackingEnabled()) {
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            }
            catch (IOException e) {}
        }
    }

    public void setUpdateAvailable(boolean available) {
        this.updateAvailable = available;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public GhostManager getGhostManager() {
        return ghostManager;
    }
}
