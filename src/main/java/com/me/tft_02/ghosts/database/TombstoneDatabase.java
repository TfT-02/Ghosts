package com.me.tft_02.ghosts.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.datatypes.TombBlock;
import com.me.tft_02.ghosts.util.StringUtils;

public class TombstoneDatabase {
    public static ConcurrentLinkedQueue<TombBlock> tombList = new ConcurrentLinkedQueue<TombBlock>();
    public static HashMap<Location, TombBlock> tombBlockList = new HashMap<Location, TombBlock>();
    public static HashMap<UUID, ArrayList<TombBlock>> playerTombList = new HashMap<UUID, ArrayList<TombBlock>>();

    public static void loadAllData() {
        for (World world : Ghosts.p.getServer().getWorlds()) {
            loadTombList(world);
        }
    }

    public static void saveAllData() {
        for (World world : Ghosts.p.getServer().getWorlds()) {
            saveTombList(world);
        }
    }

    private static void loadTombList(World world) {
        File dataDir = new File(world.getWorldFolder(), "ghosts_data");

        try {
            if (dataDir.mkdir()) {
                Ghosts.p.debug("Created ghosts_data directory.");
            }
        }
        catch (Exception e) {
            Ghosts.p.getLogger().severe(e.toString());
        }

        try {
            File file = new File(dataDir, "tombList.db");
            if (!file.exists()) {
                return;
            }
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] split = line.split(":");
                //block:largeblock:sign:ownerName:level:time
                Block block = StringUtils.deSterilizeBlock(split[0]);
                Block largeBlock = StringUtils.deSterilizeBlock(split[1]);
                Block signBlock = StringUtils.deSterilizeBlock(split[2]);
                String ownerUniqueIdString = split[3];
                String ownerName = split[4];
                int level = Integer.valueOf(split[5]);
                long time = Long.valueOf(split[6]);

                if (block == null || ownerName == null) {
                    Ghosts.p.debug("Invalid entry in database " + file.getName() + " skipping entry...");
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

    public static void saveTombList(World world) {
        File dataDir = new File(world.getWorldFolder(), "ghosts_data");

        try {
            File file = new File(dataDir, "tombList.db");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            for (TombBlock tombBlock : tombList) {
                // Skip not this world
                if (!tombBlock.getBlock().getWorld().getName().equalsIgnoreCase(world.getName())) {
                    continue;
                }

                bw.append(StringUtils.sterilizeBlock(tombBlock.getBlock()));
                bw.append(":");
                bw.append(StringUtils.sterilizeBlock(tombBlock.getLargeBlock()));
                bw.append(":");
                bw.append(StringUtils.sterilizeBlock(tombBlock.getSign()));
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

    public static HashMap<UUID, ArrayList<TombBlock>> getTombstoneList() {
        return playerTombList;
    }
}
