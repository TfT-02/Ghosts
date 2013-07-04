package com.me.tft_02.ghosts.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.datatypes.TombBlock;

public class DatabaseManager {
    private static String worldsDirectory = Ghosts.getMainDirectory() + "worlds" + File.separator;
    private static File worldsDir = new File(worldsDirectory);

    public static ConcurrentLinkedQueue<TombBlock> tombList = new ConcurrentLinkedQueue<TombBlock>();
    public static HashMap<Location, TombBlock> tombBlockList = new HashMap<Location, TombBlock>();
    public static HashMap<String, ArrayList<TombBlock>> playerTombList = new HashMap<String, ArrayList<TombBlock>>();
    public static HashMap<String, EntityDamageEvent> deathCause = new HashMap<String, EntityDamageEvent>();

    public static HashMap<String, Boolean> playerRespawns = new HashMap<String, Boolean>();
    public static HashMap<String, Location> playerLastDeathLocation = new HashMap<String, Location>();

    public static void loadTombList(String worldName) {
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
                //block:lblock:sign:owner:level:time
                Block block = readBlock(split[0]);
                Block largeBlock = readBlock(split[1]);
                Block signBlock = readBlock(split[2]);
                String owner = split[3];
                int level = Integer.valueOf(split[4]);
                long time = Long.valueOf(split[5]);

                if (block == null || owner == null) {
                    Ghosts.p.debug("Invalid entry in database " + file.getName());
                    continue;
                }
                TombBlock tombBlock = new TombBlock(block, largeBlock, signBlock, owner, level, time);
                tombList.offer(tombBlock);
                // Used for quick tombStone lookup
                tombBlockList.put(block.getLocation(), tombBlock);
                if (largeBlock != null) {
                    tombBlockList.put(largeBlock.getLocation(), tombBlock);
                }
                if (signBlock != null) {
                    tombBlockList.put(signBlock.getLocation(), tombBlock);
                }

                ArrayList<TombBlock> pList = playerTombList.get(owner);
                if (pList == null) {
                    pList = new ArrayList<TombBlock>();
                    playerTombList.put(owner, pList);
                }
                pList.add(tombBlock);
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
            for (Iterator<TombBlock> iter = tombList.iterator(); iter.hasNext();) {
                TombBlock tombBlock = iter.next();
                // Skip not this world
                if (!tombBlock.getBlock().getWorld().getName().equalsIgnoreCase(worldName)) {
                    continue;
                }

                StringBuilder builder = new StringBuilder();

                bw.append(printBlock(tombBlock.getBlock()));
                bw.append(":");
                bw.append(printBlock(tombBlock.getLargeBlock()));
                bw.append(":");
                bw.append(printBlock(tombBlock.getSign()));
                bw.append(":");
                bw.append(tombBlock.getOwner());
                bw.append(":");
                bw.append(Integer.toString(tombBlock.getOwnerLevel()));
                bw.append(":");
                bw.append(String.valueOf(tombBlock.getTime()));

                bw.append(builder.toString());
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

    public HashMap<String, ArrayList<TombBlock>> getCenotaphList() {
        return playerTombList;
    }

    public static Location getLastDeathLocation(Player player) {
        if (playerLastDeathLocation.containsKey(player.getName())) {
            return playerLastDeathLocation.get(player.getName());
        }
        return null;
    }
}
