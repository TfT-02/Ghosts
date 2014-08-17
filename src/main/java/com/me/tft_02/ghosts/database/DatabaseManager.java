package com.me.tft_02.ghosts.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.datatypes.TombBlock;

public class DatabaseManager {
    private static String worldsDirectory = Ghosts.getMainDirectory() + "worlds" + File.separator;
    private static File worldsDir = new File(worldsDirectory);

    public static ConcurrentLinkedQueue<TombBlock> tombList = new ConcurrentLinkedQueue<TombBlock>();
    public static HashMap<Location, TombBlock> tombBlockList = new HashMap<Location, TombBlock>();
    public static HashMap<UUID, ArrayList<TombBlock>> playerTombList = new HashMap<UUID, ArrayList<TombBlock>>();

    // Players that are actually ghosts
    public static Set<UUID> ghosts = new HashSet<UUID>();

    // Combine these into one GhostPlayer object and save that instead
    public static HashMap<UUID, Boolean> playerRespawns = new HashMap<UUID, Boolean>();
    public static HashMap<UUID, List<ItemStack>> playerGhostItems = new HashMap<UUID, List<ItemStack>>();
    public static HashMap<UUID, Location> playerLastDeathLocation = new HashMap<UUID, Location>();

    public static void loadAllData() {
        for (World world : Ghosts.p.getServer().getWorlds()) {
            loadData(world.getName());
        }
        loadGhostList();
    }

    public static void saveAllData() {
        for (World world : Ghosts.p.getServer().getWorlds()) {
            saveData(world.getName());
        }
        saveGhostList();
    }

    private static void saveData(String worldName) {
        saveTombList(worldName);
    }

    private static void loadData(String worldName) {
        loadTombList(worldName);
    }

    private static void loadGhostList() {
        try {
            File file = new File(worldsDirectory + "ghosts.db");
            if (!file.exists()) {
                return;
            }
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] split = line.split(":");
                //player
                UUID playerUniqueId = UUID.fromString(split[0]);

                if (playerUniqueId == null) {
                    Ghosts.p.debug("Invalid entry in database " + file.getName());
                    continue;
                }

                ghosts.add(playerUniqueId);
            }
            scanner.close();
        }
        catch (IOException e) {
            Ghosts.p.getLogger().warning("Error loading ghost list: " + e);
        }
    }

    private static void saveGhostList() {
        try {
            File file = new File(worldsDirectory + "ghosts.db");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for (UUID playerUniqueId : ghosts) {

                bw.append(playerUniqueId.toString());
                bw.append(":");
//                bw.append("");
                bw.newLine();
            }
            bw.close();
        }
        catch (IOException e) {
            Ghosts.p.getLogger().warning("Error saving ghost list: " + e);
        }
    }

    private static void loadTombList(String worldName) {
        String worldDirectory = worldsDirectory + worldName + File.separator;

        try {
            if (worldsDir.mkdir()) {
                Ghosts.p.debug("Created worlds directory.");
            }
        }
        catch (Exception e) {
            Ghosts.p.getLogger().severe(e.toString());
        }

        try {
            File worldDir = new File(worldDirectory);
            if (worldDir.mkdir()) {
                Ghosts.p.debug("Created " + worldName + " directory.");
            }
        }
        catch (Exception e) {
            Ghosts.p.getLogger().severe(e.toString());
        }

        try {
            File file = new File(worldDirectory + "tombList.db");
            if (!file.exists()) {
                return;
            }
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] split = line.split(":");
                //block:largeblock:sign:ownerName:level:time
                Block block = readBlock(split[0]);
                Block largeBlock = readBlock(split[1]);
                Block signBlock = readBlock(split[2]);
                String ownerUniqueIdString = split[3];
                String ownerName = split[4];
                int level = Integer.valueOf(split[5]);
                long time = Long.valueOf(split[6]);

                if (block == null || ownerName == null) {
                    Ghosts.p.debug("Invalid entry in database " + file.getName());
                    continue;
                }

                UUID ownerUniqueId = UUID.fromString(ownerUniqueIdString);
                TombBlock tombBlock = new TombBlock(block, largeBlock, signBlock, ownerUniqueId, ownerName, level, time);
                tombList.offer(tombBlock);
                // Used for quick tombStone lookup
                tombBlockList.put(block.getLocation(), tombBlock);
                if (largeBlock != null) {
                    tombBlockList.put(largeBlock.getLocation(), tombBlock);
                }
                if (signBlock != null) {
                    tombBlockList.put(signBlock.getLocation(), tombBlock);
                }

                ArrayList<TombBlock> playerList = playerTombList.get(ownerUniqueId);
                if (playerList == null) {
                    playerList = new ArrayList<TombBlock>();
                    playerTombList.put(ownerUniqueId, playerList);
                }
                playerList.add(tombBlock);
            }
            scanner.close();
        }
        catch (IOException e) {
            Ghosts.p.getLogger().warning("Error loading tombstone list: " + e);
        }
    }

    public static void saveTombList(String worldName) {
        String worldDirectory = worldsDirectory + worldName + File.separator;

        try {
            File file = new File(worldDirectory + "tombList.db");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            for (TombBlock tombBlock : tombList) {
                // Skip not this world
                if (!tombBlock.getBlock().getWorld().getName().equalsIgnoreCase(worldName)) {
                    continue;
                }

                bw.append(printBlock(tombBlock.getBlock()));
                bw.append(":");
                bw.append(printBlock(tombBlock.getLargeBlock()));
                bw.append(":");
                bw.append(printBlock(tombBlock.getSign()));
                bw.append(":");
                bw.append(tombBlock.getOwnerUniqueId().toString());
                bw.append(":");
                bw.append(tombBlock.getOwnerName());
                bw.append(":");
                bw.append(Integer.toString(tombBlock.getOwnerLevel()));
                bw.append(":");
                bw.append(String.valueOf(tombBlock.getTime()));

//                bw.append("");
                bw.newLine();
            }
            bw.close();
        }
        catch (IOException e) {
            Ghosts.p.getLogger().warning("Error saving tombstone list: " + e);
        }
    }

    private static String printBlock(Block b) {
        if (b == null) {
            return "";
        }
        return b.getWorld().getName() + "," + b.getX() + "," + b.getY() + "," + b.getZ();
    }

    private static Block readBlock(String b) {
        if (b.length() == 0) {
            return null;
        }

        String[] split = b.split(",");
        //world,x,y,z
        World world = Ghosts.p.getServer().getWorld(split[0]);
        if (world == null) {
            return null;
        }
        return world.getBlockAt(Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
    }

    public static HashMap<UUID, ArrayList<TombBlock>> getTombstoneList() {
        return playerTombList;
    }

    public static void setLastDeathLocation(Player player) {
        playerLastDeathLocation.put(player.getUniqueId(), player.getLocation());
    }

    public static Location getLastDeathLocation(Player player) {
        if (playerLastDeathLocation.containsKey(player.getUniqueId())) {
            return playerLastDeathLocation.get(player.getUniqueId());
        }
        return null;
    }
}
