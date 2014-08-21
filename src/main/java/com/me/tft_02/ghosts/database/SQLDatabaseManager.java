package com.me.tft_02.ghosts.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Location;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.datatypes.StatsType;
import com.me.tft_02.ghosts.datatypes.database.DatabaseType;
import com.me.tft_02.ghosts.datatypes.player.PlayerProfile;
import com.me.tft_02.ghosts.util.Misc;
import com.me.tft_02.ghosts.util.StringUtils;

import snaq.db.ConnectionPool;

public final class SQLDatabaseManager implements DatabaseManager {
    private static final String ALL_QUERY_VERSION = "deaths+find_tomb+ress_scrolls_used_t1+ress_scrolls_used_t2+ress_scrolls_used_t3+ress_scroll_used_others_t1+ress_scroll_used_others_t2+ress_scroll_used_others_t3+ress_scroll_received_t1+ress_scroll_received_t2+ress_scroll_received_t3+given_up";
    private String tablePrefix = Config.getInstance().getMySQLTablePrefix();

    private final int POOL_FETCH_TIMEOUT = 360000;

    private final Map<UUID, Integer> cachedUserIDs = new HashMap<UUID, Integer>();

    private ConnectionPool miscPool;
    private ConnectionPool loadPool;
    private ConnectionPool savePool;

    private ReentrantLock massUpdateLock = new ReentrantLock();

    protected SQLDatabaseManager() {
        String connectionString = "jdbc:mysql://" + Config.getInstance().getMySQLServerName() + ":" + Config.getInstance().getMySQLServerPort() + "/" + Config.getInstance().getMySQLDatabaseName();

        try {
            // Force driver to load if not yet loaded
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
            //throw e; // aborts onEnable()  Riking if you want to do this, fully implement it.
        }

        Properties connectionProperties = new Properties();
        connectionProperties.put("user", Config.getInstance().getMySQLUserName());
        connectionProperties.put("password", Config.getInstance().getMySQLUserPassword());
        connectionProperties.put("autoReconnect", "true");
        connectionProperties.put("cachePrepStmts", "true");
        connectionProperties.put("prepStmtCacheSize", "64");
        connectionProperties.put("prepStmtCacheSqlLimit", "2048");
        connectionProperties.put("useServerPrepStmts", "true");
        miscPool = new ConnectionPool("Ghosts-Misc-Pool",
                0 /*No Minimum really needed*/,
                Config.getInstance().getMySQLMaxPoolSize(PoolIdentifier.MISC) /*max pool size */,
                Config.getInstance().getMySQLMaxConnections(PoolIdentifier.MISC) /*max num connections*/,
                0 /* idle timeout of connections */,
                connectionString,
                connectionProperties);
        loadPool = new ConnectionPool("Ghosts-Load-Pool",
                1 /*Minimum of one*/,
                Config.getInstance().getMySQLMaxPoolSize(PoolIdentifier.LOAD) /*max pool size */,
                Config.getInstance().getMySQLMaxConnections(PoolIdentifier.LOAD) /*max num connections*/,
                0 /* idle timeout of connections */,
                connectionString,
                connectionProperties);
        savePool = new ConnectionPool("Ghosts-Save-Pool",
                1 /*Minimum of one*/,
                Config.getInstance().getMySQLMaxPoolSize(PoolIdentifier.SAVE) /*max pool size */,
                Config.getInstance().getMySQLMaxConnections(PoolIdentifier.SAVE) /*max num connections*/,
                0 /* idle timeout of connections */,
                connectionString,
                connectionProperties);
        miscPool.init(); // Init first connection
        miscPool.registerShutdownHook(); // Auto release on jvm exit  just in case
        loadPool.init();
        loadPool.registerShutdownHook();
        savePool.init();
        savePool.registerShutdownHook();

        checkStructure();

    }

    public void purgeStatlessUsers() {
        massUpdateLock.lock();
        Ghosts.p.getLogger().info("Purging statless users...");

        Connection connection = null;
        Statement statement = null;
        int purged = 0;

        try {
            connection = getConnection(PoolIdentifier.MISC);
            statement = connection.createStatement();

            purged = statement.executeUpdate("DELETE FROM " + tablePrefix + "stats WHERE "
                    + "deaths = 0 AND find_tomb = 0 AND ress_scrolls_used_t1 = 0 AND "
                    + "ress_scrolls_used_t2 = 0 AND ress_scrolls_used_t2 = 0 AND "
                    + "ress_scroll_used_others_t1 = 0 AND ress_scroll_used_others_t2 = 0 AND "
                    + "ress_scroll_used_others_t3 = 0 AND ress_scroll_received_t1 = 0 AND "
                    + "ress_scroll_received_t2 = 0 AND ress_scroll_received_t3 = 0 "
                    + "AND given_up = 0;");

            statement.executeUpdate("DELETE FROM `" + tablePrefix + "data` WHERE NOT EXISTS (SELECT * FROM `" + tablePrefix + "stats` `s` WHERE `" + tablePrefix + "data`.`user_id` = `s`.`user_id`)");
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            massUpdateLock.unlock();
        }

        Ghosts.p.getLogger().info("Purged " + purged + " users from the database.");
    }

    public void purgeOldUsers() {
        massUpdateLock.lock();
        Ghosts.p.getLogger().info("Purging inactive users older than " + (PURGE_TIME / 2630000L) + " months...");

        Connection connection = null;
        Statement statement = null;
        int purged = 0;

        try {
            connection = getConnection(PoolIdentifier.MISC);
            statement = connection.createStatement();

            purged = statement.executeUpdate("DELETE FROM u, s, d USING " + tablePrefix + "users u " +
                    "JOIN " + tablePrefix + "stats s ON (u.id = s.user_id) " +
                    "JOIN " + tablePrefix + "data d ON (u.id = d.user_id) " +
                    "WHERE ((UNIX_TIMESTAMP() - lastlogin) > " + PURGE_TIME + ")");
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            massUpdateLock.unlock();
        }

        Ghosts.p.getLogger().info("Purged " + purged + " users from the database.");
    }

    @Override
    public boolean removeUser(UUID uuid) {
        return false;
    }

    public boolean removeUser(String playerName) {
        boolean success = false;
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection(PoolIdentifier.MISC);
            statement = connection.prepareStatement("DELETE FROM u, s, d " +
                    "USING " + tablePrefix + "users u " +
                    "JOIN " + tablePrefix + "stats s ON (u.id = s.user_id) " +
                    "JOIN " + tablePrefix + "data d ON (u.id = d.user_id) " +
                    "WHERE u.user = ?");

            statement.setString(1, playerName);

            success = statement.executeUpdate() != 0;
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }

        if (success) {
            Misc.profileCleanup(playerName);
        }

        return success;
    }

    public boolean saveUser(PlayerProfile profile) {
        boolean success = true;
        PreparedStatement statement = null;
        Connection connection = null;

        try {
            connection = getConnection(PoolIdentifier.SAVE);

            int id = getUserID(connection, profile.getPlayerName(), profile.getUniqueId());

            if (id == -1) {
                id = newUser(connection, profile.getPlayerName(), profile.getUniqueId());
                if (id == -1) {
                    return false;
                }
            }

            statement = connection.prepareStatement("UPDATE " + tablePrefix + "users SET lastlogin = UNIX_TIMESTAMP() WHERE id = ?");
            statement.setInt(1, id);
            success &= (statement.executeUpdate() != 0);
            statement.close();

            statement = connection.prepareStatement("UPDATE " + tablePrefix + "stats SET "
                    + " deaths = ?, find_tomb = ?, ress_scrolls_used_t1 = ?, ress_scrolls_used_t2 = ?"
                    + ", ress_scrolls_used_t3 = ?, ress_scroll_used_others_t1 = ?, "
                    + "ress_scroll_used_others_t2 = ?, ress_scroll_used_others_t3 = ?, "
                    + "ress_scroll_received_t1 = ?, ress_scroll_received_t2 = ?, "
                    + "ress_scroll_received_t3 = ?, given_up = ? WHERE user_id = ?");
            statement.setInt(1, profile.getStats(StatsType.DEATHS));
            statement.setInt(2, profile.getStats(StatsType.FIND_TOMB));
            statement.setInt(3, profile.getStats(StatsType.RESS_SCROLL_USED_T1));
            statement.setInt(4, profile.getStats(StatsType.RESS_SCROLL_USED_T2));
            statement.setInt(5, profile.getStats(StatsType.RESS_SCROLL_USED_T3));
            statement.setInt(6, profile.getStats(StatsType.RESS_SCROLL_USED_OTHERS_T1));
            statement.setInt(7, profile.getStats(StatsType.RESS_SCROLL_USED_OTHERS_T2));
            statement.setInt(8, profile.getStats(StatsType.RESS_SCROLL_USED_OTHERS_T3));
            statement.setInt(9, profile.getStats(StatsType.RESS_SCROLL_RECEIVED_T1));
            statement.setInt(10, profile.getStats(StatsType.RESS_SCROLL_RECEIVED_T2));
            statement.setInt(11, profile.getStats(StatsType.RESS_SCROLL_RECEIVED_T3));
            statement.setInt(12, profile.getStats(StatsType.GIVEN_UP));
            statement.setInt(13, id);
            success &= (statement.executeUpdate() != 0);
            statement.close();

            statement = connection.prepareStatement("UPDATE " + tablePrefix + "data SET "
                    + " isghost = ?, respawn = ?, lastdeathlocation = ?, "
                    + "savedlostvanillaxp = ?, savedremainingvanillaxp = ?, savedlostmcmmoxp = ?, "
                    + "savedremainingmcmmoxp = ? WHERE user_id = ?");
            statement.setBoolean(1, profile.isGhost());
            statement.setBoolean(2, profile.getRespawn());
            statement.setString(3, StringUtils.sterilizeLocation(profile.getLastDeathLocation()));
            statement.setInt(4, profile.getSavedLostVanillaXP());
            statement.setInt(5, profile.getSavedRemainingVanillaXP());
            statement.setInt(6, profile.getSavedLostMcMMOXP());
            statement.setInt(7, profile.getSavedRemainingMcMMOXP());
            statement.setInt(8, id);
            success &= (statement.executeUpdate() != 0);
            statement.close();
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }

        return success;
    }

    public void newUser(String playerName, UUID uuid) {
        Connection connection = null;

        try {
            connection = getConnection(PoolIdentifier.MISC);
            newUser(connection, playerName, uuid);
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }

    private int newUser(Connection connection, String playerName, UUID uuid) {
        ResultSet resultSet = null;
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "users (user, uuid, lastlogin) VALUES (?, ?, UNIX_TIMESTAMP())", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, playerName);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();

            resultSet = statement.getGeneratedKeys();

            if (!resultSet.next()) {
                return -1;
            }

            writeMissingRows(connection, resultSet.getInt(1));
            return resultSet.getInt(1);
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }
        return -1;
    }

    public PlayerProfile loadPlayerProfile(UUID uuid) {
        return loadPlayerProfile("", uuid, false, true);
    }

    public PlayerProfile loadPlayerProfile(String playerName, UUID uuid, boolean create) {
        return loadPlayerProfile(playerName, uuid, create, true);
    }

    private PlayerProfile loadPlayerProfile(String playerName, UUID uuid, boolean create, boolean retry) {
        PreparedStatement statement = null;
        Connection connection = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection(PoolIdentifier.LOAD);
            int id = getUserID(connection, playerName, uuid);

            if (id == -1) {
                // There is no such user
                if (create) {
                    id = newUser(connection, playerName, uuid);
                    create = false;
                    if (id == -1) {
                        return new PlayerProfile(uuid, playerName, false);
                    }
                } else {
                    return new PlayerProfile(uuid, playerName, false);
                }
            }
            // There is such a user
            writeMissingRows(connection, id);

            statement = connection.prepareStatement(
                    "SELECT "
                            + "s.deaths, s.find_tomb, s.ress_scrolls_used_t1, s.ress_scrolls_used_t2, s.ress_scrolls_used_t3, s.ress_scroll_used_others_t1, s.ress_scroll_used_others_t2, s.ress_scroll_used_others_t3, s.ress_scroll_received_t1, s.ress_scroll_received_t2, s.ress_scroll_received_t3, s.given_up, "
                            + "d.isghost, d.respawn, d.lastdeathlocation, d.savedlostvanillaxp, d.savedremainingvanillaxp, d.savedlostmcmmoxp, d.savedremainingmcmmoxp, "
                            + "u.uuid "
                            + "FROM " + tablePrefix + "users u "
                            + "JOIN " + tablePrefix + "stats s ON (u.id = s.user_id) "
                            + "JOIN " + tablePrefix + "data d ON (u.id = d.user_id) "
                            + "WHERE u.id = ?");
            statement.setInt(1, id);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                try {
                    PlayerProfile profile = loadFromResult(playerName, resultSet);
                    resultSet.close();
                    statement.close();

                    if (!playerName.isEmpty() && !profile.getPlayerName().isEmpty()) {
                        statement = connection.prepareStatement(
                                "UPDATE `" + tablePrefix + "users` "
                                        + "SET user = ?, uuid = ? "
                                        + "WHERE id = ?");
                        statement.setString(1, playerName);
                        statement.setString(2, uuid.toString());
                        statement.setInt(3, id);
                        statement.executeUpdate();
                        statement.close();
                    }

                    return profile;
                }
                catch (SQLException e) {
                }
            }
            resultSet.close();
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }

        // Problem, nothing was returned

        // return unloaded profile
        if (!retry) {
            return new PlayerProfile(uuid, playerName, false);
        }

        // Retry, and abort on re-failure
        return loadPlayerProfile(playerName, uuid, create, false);
    }

    public void convertUsers(DatabaseManager destination) {
        PreparedStatement statement = null;
        Connection connection = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection(PoolIdentifier.MISC);
            statement = connection.prepareStatement(
                    "SELECT "
                            + "s.deaths, s.find_tomb, s.ress_scrolls_used_t1, s.ress_scrolls_used_t2, s.ress_scrolls_used_t3, s.ress_scroll_used_others_t1, s.ress_scroll_used_others_t2, s.ress_scroll_used_others_t3, s.ress_scroll_received_t1, s.ress_scroll_received_t2, s.ress_scroll_received_t3, s.given_up, "
                            + "d.isghost, d.respawn, d.lastdeathlocation, d.savedlostvanillaxp, d.savedremainingvanillaxp, d.savedlostmcmmoxp, d.savedremainingmcmmoxp, "
                            + "u.uuid "
                            + "FROM " + tablePrefix + "users u "
                            + "JOIN " + tablePrefix + "stats s ON (u.id = s.user_id) "
                            + "JOIN " + tablePrefix + "data d ON (u.id = d.user_id) "
                            + "WHERE u.user = ?");
            List<String> usernames = getStoredUsers();
            int convertedUsers = 0;
            long startMillis = System.currentTimeMillis();
            for (String playerName : usernames) {
                statement.setString(1, playerName);
                try {
                    resultSet = statement.executeQuery();
                    resultSet.next();
                    destination.saveUser(loadFromResult(playerName, resultSet));
                    resultSet.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
                convertedUsers++;
                Misc.printProgress(convertedUsers, progressInterval, startMillis);
            }
        }
        catch (SQLException e) {
            printErrors(e);
        }
        finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }

    }

    public List<String> getStoredUsers() {
        ArrayList<String> users = new ArrayList<String>();

        Statement statement = null;
        Connection connection = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection(PoolIdentifier.MISC);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT user FROM " + tablePrefix + "users");
            while (resultSet.next()) {
                users.add(resultSet.getString("user"));
            }
        }
        catch (SQLException e) {
            printErrors(e);
        }
        finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }

        return users;
    }

    /**
     * Checks that the database structure is present and correct
     */
    private void checkStructure() {

        PreparedStatement statement = null;
        Statement createStatement = null;
        ResultSet resultSet = null;
        Connection connection = null;

        try {
            connection = getConnection(PoolIdentifier.MISC);
            statement = connection.prepareStatement("SELECT table_name FROM INFORMATION_SCHEMA.TABLES"
                    + " WHERE table_schema = ?"
                    + " AND table_name = ?");
            statement.setString(1, Config.getInstance().getMySQLDatabaseName());
            statement.setString(2, tablePrefix + "users");
            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                createStatement = connection.createStatement();
                createStatement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "users` ("
                    + "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                    + "`user` varchar(40) NOT NULL,"
                    + "`uuid` varchar(36) NULL DEFAULT NULL,"
                    + "`lastlogin` int(32) unsigned NOT NULL,"
                    + "PRIMARY KEY (`id`),"
                    + "UNIQUE KEY `user` (`user`),"
                    + "UNIQUE KEY `uuid` (`uuid`)) DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
                createStatement.close();
            }
            resultSet.close();

            statement.setString(1, Config.getInstance().getMySQLDatabaseName());
            statement.setString(2, tablePrefix + "stats");
            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                createStatement = connection.createStatement();
                createStatement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "stats` ("
                        + "`user_id` int(10) unsigned NOT NULL,"
                        + "`deaths` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`find_tomb` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`ress_scrolls_used_t1` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`ress_scrolls_used_t2` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`ress_scrolls_used_t3` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`ress_scroll_used_others_t1` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`ress_scroll_used_others_t2` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`ress_scroll_used_others_t3` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`ress_scroll_received_t1` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`ress_scroll_received_t2` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`ress_scroll_received_t3` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`given_up` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "PRIMARY KEY (`user_id`)) "
                        + "DEFAULT CHARSET=latin1;");
                createStatement.close();
            }
            resultSet.close();

            statement.setString(1, Config.getInstance().getMySQLDatabaseName());
            statement.setString(2, tablePrefix + "data");
            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                createStatement = connection.createStatement();
                createStatement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "data` ("
                        + "`user_id` int(10) unsigned NOT NULL,"
                        //TODO Check sources if this is correct
                        + "`isghost` boolean(2) unsigned NOT NULL DEFAULT 'false',"
                        + "`respawn` boolean(2) unsigned NOT NULL DEFAULT 'false',"
                        + "`lastdeathlocation` varchar(40) unsigned NOT NULL DEFAULT 'NULL',"
                        + "`savedlostvanillaxp` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`savedremainingvanillaxp` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`savedlostmcmmoxp` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "`savedremainingmcmmoxp` int(10) unsigned NOT NULL DEFAULT '0',"
                        + "PRIMARY KEY (`user_id`)) "
                        + "DEFAULT CHARSET=latin1;");
                createStatement.close();
            }
            resultSet.close();
            statement.close();

            Ghosts.p.getLogger().info("Killing orphans");
            createStatement = connection.createStatement();
            createStatement.executeUpdate("DELETE FROM `" + tablePrefix + "stats` WHERE NOT EXISTS (SELECT * FROM `" + tablePrefix + "users` `u` WHERE `" + tablePrefix + "stats`.`user_id` = `u`.`id`)");
            createStatement.executeUpdate("DELETE FROM `" + tablePrefix + "data` WHERE NOT EXISTS (SELECT * FROM `" + tablePrefix + "users` `u` WHERE `" + tablePrefix + "data`.`user_id` = `u`.`id`)");
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (createStatement != null) {
                try {
                    createStatement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }

    }

    private Connection getConnection(PoolIdentifier identifier) throws SQLException {
        Connection connection = null;
        switch (identifier) {
            case LOAD:
                connection = loadPool.getConnection(POOL_FETCH_TIMEOUT);
                break;
            case MISC:
                connection = miscPool.getConnection(POOL_FETCH_TIMEOUT);
                break;
            case SAVE:
                connection = savePool.getConnection(POOL_FETCH_TIMEOUT);
                break;
        }
        if (connection == null) {
            throw new RuntimeException("getConnection() for " + identifier.name().toLowerCase() + " pool timed out.  Increase max connections settings.");
        }
        return connection;
    }

    private void writeMissingRows(Connection connection, int id) {
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement("INSERT IGNORE INTO " + tablePrefix + "stats (user_id) VALUES (?)");
            statement.setInt(1, id);
            statement.execute();
            statement.close();
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }

    private PlayerProfile loadFromResult(String playerName, ResultSet result) throws SQLException {
        Map<StatsType, Integer> stats = new EnumMap<StatsType, Integer>(StatsType.class);

        // TODO update these numbers when the query changes (new stat type is added)
        final int OFFSET_STATS = 1;
        final int OFFSET_DATA = 12;
        final int OFFSET_OTHER = 19;

        stats.put(StatsType.DEATHS, result.getInt(OFFSET_STATS + 1));
        stats.put(StatsType.FIND_TOMB, result.getInt(OFFSET_STATS + 2));
        stats.put(StatsType.RESS_SCROLL_USED_T1, result.getInt(OFFSET_STATS + 3));
        stats.put(StatsType.RESS_SCROLL_USED_T2, result.getInt(OFFSET_STATS + 4));
        stats.put(StatsType.RESS_SCROLL_USED_T3, result.getInt(OFFSET_STATS + 5));
        stats.put(StatsType.RESS_SCROLL_USED_OTHERS_T1, result.getInt(OFFSET_STATS + 6));
        stats.put(StatsType.RESS_SCROLL_USED_OTHERS_T2, result.getInt(OFFSET_STATS + 7));
        stats.put(StatsType.RESS_SCROLL_USED_OTHERS_T3, result.getInt(OFFSET_STATS + 8));
        stats.put(StatsType.RESS_SCROLL_RECEIVED_T1, result.getInt(OFFSET_STATS + 9));
        stats.put(StatsType.RESS_SCROLL_RECEIVED_T2, result.getInt(OFFSET_STATS + 10));
        stats.put(StatsType.RESS_SCROLL_RECEIVED_T3, result.getInt(OFFSET_STATS + 11));
        stats.put(StatsType.GIVEN_UP, result.getInt(OFFSET_STATS + 12));

        boolean isGhost = result.getBoolean(OFFSET_DATA + 1);
        boolean respawn = result.getBoolean(OFFSET_DATA + 2);
        Location lastDeathLocation = StringUtils.deSterilizeLocation(result.getString(OFFSET_DATA + 3));
        int savedLostVanillaXP = result.getInt(OFFSET_DATA + 4);
        int savedRemainingVanillaXP = result.getInt(OFFSET_DATA + 5);
        int savedLostMcMMOXP = result.getInt(OFFSET_DATA + 6);
        int savedRemainingMcMMOXP = result.getInt(OFFSET_DATA + 7);

        UUID uuid;

        try {
            uuid = UUID.fromString(result.getString(OFFSET_OTHER + 2));
        }
        catch (Exception e) {
            uuid = null;
        }

        return new PlayerProfile(uuid, playerName, isGhost, respawn, lastDeathLocation, savedLostVanillaXP, savedRemainingVanillaXP, savedLostMcMMOXP, savedRemainingMcMMOXP, stats);
    }

    private void printErrors(SQLException ex) {
        StackTraceElement element = ex.getStackTrace()[0];
        Ghosts.p.getLogger().severe("Location: " + element.getClassName() + " " + element.getMethodName() + " " + element.getLineNumber());
        Ghosts.p.getLogger().severe("SQLException: " + ex.getMessage());
        Ghosts.p.getLogger().severe("SQLState: " + ex.getSQLState());
        Ghosts.p.getLogger().severe("VendorError: " + ex.getErrorCode());
    }

    public DatabaseType getDatabaseType() {
        return DatabaseType.SQL;
    }

    private int getUserID(final Connection connection, final String playerName, final UUID uuid) {
        if (cachedUserIDs.containsKey(uuid)) {
            return cachedUserIDs.get(uuid);
        }

        ResultSet resultSet = null;
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement("SELECT id, user FROM " + tablePrefix + "users WHERE uuid = ? OR (uuid IS NULL AND user = ?)");
            statement.setString(1, uuid.toString());
            statement.setString(2, playerName);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");

                cachedUserIDs.put(uuid, id);

                return id;
            }
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }

        return -1;
    }

    @Override
    public void onDisable() {
        Ghosts.p.debug("Releasing connection pool resource...");
        miscPool.release();
        loadPool.release();
        savePool.release();
    }

    public enum PoolIdentifier {
        MISC,
        LOAD,
        SAVE;
    }
}
