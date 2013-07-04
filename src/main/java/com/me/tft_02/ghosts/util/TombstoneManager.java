package com.me.tft_02.ghosts.util;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.datatypes.TombBlock;

public class TombstoneManager {

    public void createTombstone(Player player, Location location) {

    }

    public void destroyCenotaph(Location loc) {
        destroyTomb(DatabaseManager.tombBlockList.get(loc));
    }

    public static void destroyTomb(TombBlock tombBlock) {
        if (tombBlock.getBlock().getChunk().load() == false) {
            Ghosts.p.getLogger().severe("Error loading world chunk trying to remove cenotaph at " + tombBlock.getBlock().getX() + "," + tombBlock.getBlock().getY() + "," + tombBlock.getBlock().getZ() + " owned by " + tombBlock.getOwner() + ".");
            return;
        }

        tombBlock.getBlock().setType(Material.AIR);
        if (tombBlock.getLargeBlock() != null)
            tombBlock.getLargeBlock().setType(Material.AIR);

        removeTomb(tombBlock, true);

        Player p = Ghosts.p.getServer().getPlayer(tombBlock.getOwner());
        if (p != null) {
            p.sendMessage("Your cenotaph has broken.");
        }
    }

    public static void removeTomb(TombBlock tBlock, boolean removeList) {
        if (tBlock == null) {
            return;
        }

        DatabaseManager.tombBlockList.remove(tBlock.getBlock().getLocation());
        if (tBlock.getLargeBlock() != null)
            DatabaseManager.tombBlockList.remove(tBlock.getLargeBlock().getLocation());

        // Remove just this tomb from tombList
        ArrayList<TombBlock> tList = DatabaseManager.playerTombList.get(tBlock.getOwner());
        if (tList != null) {
            tList.remove(tBlock);
            if (tList.size() == 0) {
                DatabaseManager.playerTombList.remove(tBlock.getOwner());
            }
        }

        if (removeList)
            DatabaseManager.tombList.remove(tBlock);

        if (tBlock.getBlock() != null) {
            DatabaseManager.saveTombList(tBlock.getBlock().getWorld().getName());
        }
    }
}
