package com.me.tft_02.ghosts.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.datatypes.StatsType;
import com.me.tft_02.ghosts.datatypes.database.DatabaseType;
import com.me.tft_02.ghosts.datatypes.player.PlayerProfile;
import com.me.tft_02.ghosts.util.Misc;
import com.me.tft_02.ghosts.util.StringUtils;

import org.apache.commons.lang.ArrayUtils;

public final class FlatfileDatabaseManager implements DatabaseManager {
    private final File usersFile;
    private static final Object fileWritingLock = new Object();

    protected FlatfileDatabaseManager() {
        usersFile = new File(Ghosts.getUsersFilePath());
        checkStructure();
    }

    public void purgeOldUsers() {
        int removedPlayers = 0;
        long currentTime = System.currentTimeMillis();

        Ghosts.p.getLogger().info("Purging old users...");

        BufferedReader in = null;
        FileWriter out = null;
        String usersFilePath = Ghosts.getUsersFilePath();

        // This code is O(n) instead of O(n²)
        synchronized (fileWritingLock) {
            try {
                in = new BufferedReader(new FileReader(usersFilePath));
                StringBuilder writer = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    String[] character = line.split(":");
                    UUID uuid = UUID.fromString(character[0]);
                    long lastPlayed = 0;
                    boolean rewrite = false;
                    try {
                        lastPlayed = Long.parseLong(character[21]) * Misc.TIME_CONVERSION_FACTOR;
                    }
                    catch (NumberFormatException e) {
                    }
                    if (lastPlayed == 0) {
                        OfflinePlayer player = Ghosts.p.getServer().getOfflinePlayer(uuid);
                        lastPlayed = player.getLastPlayed();
                        rewrite = true;
                    }

                    if (currentTime - lastPlayed > PURGE_TIME) {
                        removedPlayers++;
                    }
                    else {
                        if (rewrite) {
                            // Rewrite their data with a valid time
                            character[21] = Long.toString(lastPlayed);
                            String newLine = org.apache.commons.lang.StringUtils.join(character, ":");
                            writer.append(newLine).append("\r\n");
                        }
                        else {
                            writer.append(line).append("\r\n");
                        }
                    }
                }

                // Write the new file
                out = new FileWriter(usersFilePath);
                out.write(writer.toString());
            }
            catch (IOException e) {
                Ghosts.p.getLogger().severe("Exception while reading " + usersFilePath + " (Are you sure you formatted it correctly?)" + e.toString());
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException e) {
                        // Ignore
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }

        Ghosts.p.getLogger().info("Purged " + removedPlayers + " users from the database.");
    }

    public boolean removeUser(UUID uuid) {
        boolean worked = false;

        BufferedReader in = null;
        FileWriter out = null;
        String usersFilePath = Ghosts.getUsersFilePath();

        synchronized (fileWritingLock) {
            try {
                in = new BufferedReader(new FileReader(usersFilePath));
                StringBuilder writer = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    // Write out the same file but when we get to the player we want to remove, we skip his line.
                    if (!worked && line.split(":")[0].equals(uuid.toString())) {
                        Ghosts.p.getLogger().info("User found, removing...");
                        worked = true;
                        continue; // Skip the player
                    }

                    writer.append(line).append("\r\n");
                }

                out = new FileWriter(usersFilePath); // Write out the new file
                out.write(writer.toString());
            }
            catch (Exception e) {
                Ghosts.p.getLogger().severe("Exception while reading " + usersFilePath + " (Are you sure you formatted it correctly?)" + e.toString());
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException e) {
                        // Ignore
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }

        return worked;
    }

    public boolean saveUser(PlayerProfile profile) {
        UUID uuid = profile.getUniqueId();

        BufferedReader in = null;
        FileWriter out = null;
        String usersFilePath = Ghosts.getUsersFilePath();

        synchronized (fileWritingLock) {
            try {
                // Open the file
                in = new BufferedReader(new FileReader(usersFilePath));
                StringBuilder writer = new StringBuilder();
                String line;

                // While not at the end of the file
                while ((line = in.readLine()) != null) {
                    // Read the line in and copy it to the output if it's not the player we want to edit
                    String[] character = line.split(":");
                    if (!character[0].equals(uuid.toString())) {
                        writer.append(line).append("\r\n");
                    }
                    else {
                        // Otherwise write the new player information
                        writer.append(uuid.toString()).append(":");
                        writer.append(profile.getPlayerName()).append(":");
                        writer.append(profile.isGhost()).append(":");
                        writer.append(profile.getRespawn()).append(":");
                        writer.append(StringUtils.sterilizeLocation(profile.getLastDeathLocation())).append(":");
                        writer.append(profile.getSavedLostVanillaXP()).append(":");
                        writer.append(profile.getSavedRemainingVanillaXP()).append(":");
                        writer.append(StringUtils.sterilizeMcMMOXP(profile.getSavedLostMcMMOXP())).append(":");
                        writer.append(profile.getStats(StatsType.DEATHS)).append(":");
                        writer.append(profile.getStats(StatsType.FIND_TOMB)).append(":");
                        writer.append(profile.getStats(StatsType.RESURRECTION_SCROLLS_USED_T1)).append(":");
                        writer.append(profile.getStats(StatsType.RESURRECTION_SCROLLS_USED_T2)).append(":");
                        writer.append(profile.getStats(StatsType.RESURRECTION_SCROLLS_USED_T3)).append(":");
                        writer.append(profile.getStats(StatsType.RESURRECTION_SCROLLS_USED_OTHERS_T1)).append(":");
                        writer.append(profile.getStats(StatsType.RESURRECTION_SCROLLS_USED_OTHERS_T2)).append(":");
                        writer.append(profile.getStats(StatsType.RESURRECTION_SCROLLS_USED_OTHERS_T3)).append(":");
                        writer.append(profile.getStats(StatsType.RESURRECTION_SCROLLS_RECEIVED_T1)).append(":");
                        writer.append(profile.getStats(StatsType.RESURRECTION_SCROLLS_RECEIVED_T2)).append(":");
                        writer.append(profile.getStats(StatsType.RESURRECTION_SCROLLS_RECEIVED_T3)).append(":");
                        writer.append(profile.getStats(StatsType.GIVEN_UP)).append(":");
                        writer.append(System.currentTimeMillis() / Misc.TIME_CONVERSION_FACTOR).append(":");
                        writer.append("\r\n");
                    }
                }

                // Write the new file
                out = new FileWriter(usersFilePath);
                out.write(writer.toString());
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException e) {
                        // Ignore
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }
    }

    public void newUser(String playerName, UUID uuid) {
        BufferedWriter out = null;
        synchronized (fileWritingLock) {
            try {
                // Open the file to write the player
                out = new BufferedWriter(new FileWriter(Ghosts.getUsersFilePath(), true));

                // Add the player to the end
                out.append(uuid != null ? uuid.toString() : "").append(":"); // UUID
                out.append(playerName).append(":"); // Username
                out.append("false:"); // isGhost
                out.append("false:"); // respawn
                out.append("null:"); // lastDeathLocation
                out.append("0:"); // savedLostVanillaXP
                out.append("0:"); // savedRemainingVanillaXP
                out.append("null:"); // savedLostMcMMOXP
                out.append("0:"); // DEATHS
                out.append("0:"); // FIND_TOMB
                out.append("0:"); // RESURRECTION_SCROLLS_USED_T1
                out.append("0:"); // RESURRECTION_SCROLLS_USED_T2
                out.append("0:"); // RESURRECTION_SCROLLS_USED_T3
                out.append("0:"); // RESURRECTION_SCROLLS_USED_OTHERS_T1
                out.append("0:"); // RESURRECTION_SCROLLS_USED_OTHERS_T2
                out.append("0:"); // RESURRECTION_SCROLLS_USED_OTHERS_T3
                out.append("0:"); // RESURRECTION_SCROLLS_RECEIVED_T1
                out.append("0:"); // RESURRECTION_SCROLLS_RECEIVED_T2
                out.append("0:"); // RESURRECTION_SCROLLS_RECEIVED_T3
                out.append("0:"); // GIVEN_UP
                out.append(String.valueOf(System.currentTimeMillis() / Misc.TIME_CONVERSION_FACTOR)).append(":"); // LastLogin

                // Add more in the same format as the line above

                out.newLine();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }
    }

    public PlayerProfile loadPlayerProfile(UUID uuid) {
        return loadPlayerProfile("", uuid, false);
    }

    public PlayerProfile loadPlayerProfile(String playerName, UUID uuid, boolean create) {
        BufferedReader in = null;
        String usersFilePath = Ghosts.getUsersFilePath();

        synchronized (fileWritingLock) {
            try {
                // Open the user file
                in = new BufferedReader(new FileReader(usersFilePath));
                String line;

                while ((line = in.readLine()) != null) {
                    // Find if the line contains the player we want.
                    String[] character = line.split(":");

                    if (!character[0].equalsIgnoreCase(uuid.toString())) {
                        continue;
                    }

                    // Update playerName in database after name change
                    if (!character[1].equalsIgnoreCase(playerName)) {
                        Ghosts.p.debug("Name change detected: " + character[1] + " => " + playerName);
                        character[1] = playerName;
                    }

                    return loadFromLine(character);
                }

                // Didn't find the player, create a new one
                if (create) {
                    newUser(playerName, uuid);
                    return new PlayerProfile(uuid, playerName, true);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                // I have no idea why it's necessary to inline tryClose() here, but it removes
                // a resource leak warning, and I'm trusting the compiler on this one.
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }

        // Return unloaded profile
        return new PlayerProfile(uuid, playerName);
    }

    public void convertUsers(DatabaseManager destination) {
        BufferedReader in = null;
        String usersFilePath = Ghosts.getUsersFilePath();
        int convertedUsers = 0;
        long startMillis = System.currentTimeMillis();

        synchronized (fileWritingLock) {
            try {
                // Open the user file
                in = new BufferedReader(new FileReader(usersFilePath));
                String line;

                while ((line = in.readLine()) != null) {
                    String[] character = line.split(":");

                    try {
                        destination.saveUser(loadFromLine(character));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    convertedUsers++;
//                    Misc.printProgress(convertedUsers, progressInterval, startMillis);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }
    }

    public List<String> getStoredUsers() {
        ArrayList<String> users = new ArrayList<String>();
        BufferedReader in = null;
        String usersFilePath = Ghosts.getUsersFilePath();

        synchronized (fileWritingLock) {
            try {
                // Open the user file
                in = new BufferedReader(new FileReader(usersFilePath));
                String line;

                while ((line = in.readLine()) != null) {
                    String[] character = line.split(":");
                    users.add(character[0]);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }
        return users;
    }

    /**
     * Checks that the file is present and valid
     */
    private void checkStructure() {
        if (usersFile.exists()) {
            BufferedReader in = null;
            FileWriter out = null;
            String usersFilePath = Ghosts.getUsersFilePath();

            synchronized (fileWritingLock) {
                try {
                    in = new BufferedReader(new FileReader(usersFilePath));
                    StringBuilder writer = new StringBuilder();
                    String line;
                    HashSet<String> players = new HashSet<String>();

                    while ((line = in.readLine()) != null) {
                        // Remove empty lines from the file
                        if (line.isEmpty()) {
                            continue;
                        }

                        // Length checks depend on last character being ':'
                        if (line.charAt(line.length() - 1) != ':') {
                            line = line.concat(":");
                        }
                        String[] character = line.split(":");

                        // Prevent the same player from being present multiple times
                        if (!players.add(character[0])) {
                            continue;
                        }

                        String oldVersion = null;

                        // If they're valid, rewrite them to the file.
                        if (character.length == 21) {
                            writer.append(line).append("\r\n");
                            continue;
                        }

                        StringBuilder newLine = new StringBuilder(line);

//                        if (character.length <= 35) {
//                            // Introduction of Fishing
//                            // Version 1.2.00
//                            // commit a814b57311bc7734661109f0e77fc8bab3a0bd29
//                            newLine.append(0).append(":");
//                            newLine.append(0).append(":");
//                            if (oldVersion == null) {
//                                oldVersion = "1.2.00";
//                            }
//                        }

                        // Remove any blanks that shouldn't be there, and validate the other fields
                        String[] newCharacter = newLine.toString().split(":");
                        boolean corrupted = false;

                        for (int i = 0; i < newCharacter.length; i++) {
                            if (newCharacter[i].isEmpty() && !(i == 41)) {
                                corrupted = true;

                                if (newCharacter.length != 21) {
                                    newCharacter = (String[]) ArrayUtils.remove(newCharacter, i);
                                }
                            }

                            if (!StringUtils.isInt(newCharacter[i]) && !(i == 0 || i == 1 || i == 2 || i == 3 || i == 4 || i == 7)) {
                                corrupted = true;
                                newCharacter[i] = "0";
                            }
                        }

                        if (corrupted) {
                            Ghosts.p.debug("Updating corrupted database line for player " + newCharacter[1]);
                        }

                        if (oldVersion != null) {
                            Ghosts.p.debug("Updating database line from before version " + oldVersion + " for player " + character[1]);
                        }

                        if (corrupted || oldVersion != null) {
                            newLine = new StringBuilder(org.apache.commons.lang.StringUtils.join(newCharacter, ":"));
                            newLine = newLine.append(":");
                        }

                        writer.append(newLine).append("\r\n");
                    }

                    // Write the new file
                    out = new FileWriter(usersFilePath);
                    out.write(writer.toString());
                }
                catch (IOException e) {
                    Ghosts.p.getLogger().severe("Exception while reading " + usersFilePath + " (Are you sure you formatted it correctly?)" + e.toString());
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (IOException e) {
                            // Ignore
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        }
                        catch (IOException e) {
                            // Ignore
                        }
                    }
                }
            }

            return;
        }

        usersFile.getParentFile().mkdir();

        try {
            Ghosts.p.debug("Creating Ghosts.users file...");
            new File(Ghosts.getUsersFilePath()).createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PlayerProfile loadFromLine(String[] character) {
        UUID uuid;
        try {
            uuid = UUID.fromString(character[0]);
        }
        catch (Exception e) {
            uuid = null;
        }

        boolean isGhost = Boolean.valueOf(character[2]);
        boolean respawn = Boolean.valueOf(character[3]);
        Location lastDeathLocation = StringUtils.deSterilizeLocation(character[4]);
        int savedLostVanillaXP = Integer.valueOf(character[5]);
        int savedRemainingVanillaXP = Integer.valueOf(character[6]);
        HashMap<String, Integer> savedLostMcMMOXP = StringUtils.deSterilizeMcMMOXP(character[7]);
        Map<StatsType, Integer> stats = getStatsMapFromLine(character);

        return new PlayerProfile(uuid, character[1], isGhost, respawn, lastDeathLocation, savedLostVanillaXP, savedRemainingVanillaXP, savedLostMcMMOXP, stats);
    }

    private Map<StatsType, Integer> getStatsMapFromLine(String[] character) {
        Map<StatsType, Integer> stats = new EnumMap<StatsType, Integer>(StatsType.class);

        stats.put(StatsType.DEATHS, Integer.valueOf(character[8]));
        stats.put(StatsType.FIND_TOMB, Integer.valueOf(character[9]));
        stats.put(StatsType.RESURRECTION_SCROLLS_USED_T1, Integer.valueOf(character[10]));
        stats.put(StatsType.RESURRECTION_SCROLLS_USED_T2, Integer.valueOf(character[11]));
        stats.put(StatsType.RESURRECTION_SCROLLS_USED_T3, Integer.valueOf(character[12]));
        stats.put(StatsType.RESURRECTION_SCROLLS_USED_OTHERS_T1, Integer.valueOf(character[13]));
        stats.put(StatsType.RESURRECTION_SCROLLS_USED_OTHERS_T2, Integer.valueOf(character[14]));
        stats.put(StatsType.RESURRECTION_SCROLLS_USED_OTHERS_T3, Integer.valueOf(character[15]));
        stats.put(StatsType.RESURRECTION_SCROLLS_RECEIVED_T1, Integer.valueOf(character[16]));
        stats.put(StatsType.RESURRECTION_SCROLLS_RECEIVED_T2, Integer.valueOf(character[17]));
        stats.put(StatsType.RESURRECTION_SCROLLS_RECEIVED_T3, Integer.valueOf(character[18]));
        stats.put(StatsType.GIVEN_UP, Integer.valueOf(character[19]));

        return stats;
    }

    public DatabaseType getDatabaseType() {
        return DatabaseType.FLATFILE;
    }

    @Override
    public void onDisable() { }
}
