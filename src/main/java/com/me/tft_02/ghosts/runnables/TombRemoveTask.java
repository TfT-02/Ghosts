package com.me.tft_02.ghosts.runnables;

import java.util.Iterator;

import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.datatypes.TombBlock;
import com.me.tft_02.ghosts.managers.TombstoneManager;
import com.me.tft_02.ghosts.util.Misc;

public class TombRemoveTask extends BukkitRunnable {

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis() / Misc.TIME_CONVERSION_FACTOR;
        for (Iterator<TombBlock> iter = DatabaseManager.tombList.iterator(); iter.hasNext();) {
            TombBlock tombBlock = iter.next();

            //"empty" option checks
            if (Config.getInstance().getKeepUntilEmpty() || Config.getInstance().getRemoveWhenEmpty()) {
                BlockState blockState = tombBlock.getBlock().getState();

                if (blockState instanceof Chest) {
                    boolean isEmpty = true;

                    Chest smallChest = (Chest) blockState;
                    Chest largeChest = (tombBlock.getLargeBlock() != null) ? (Chest) tombBlock.getLargeBlock().getState() : null;

                    Inventory blockInventory = smallChest.getBlockInventory();

                    for (ItemStack item : blockInventory.getContents()) {
                        if (item != null) {
                            isEmpty = false;
                            break;
                        }
                    }

                    if (largeChest != null && !isEmpty) {
                        for (ItemStack item : largeChest.getBlockInventory().getContents()) {
                            if (item != null) {
                                isEmpty = false;
                                break;
                            }
                        }
                    }

                    if (Config.getInstance().getKeepUntilEmpty()) {
                        if (!isEmpty) {
                            continue;
                        }
                    }

                    if (Config.getInstance().getRemoveWhenEmpty()) {
                        if (isEmpty) {
                            TombstoneManager.destroyTombstone(tombBlock);
                            iter.remove();
                        }
                    }
                }
                else {
                    TombstoneManager.destroyTombstone(tombBlock);
                    iter.remove();
                }
            }

            //Block removal check
            if (Config.getInstance().getTombRemoveTime() > 0) {
                if (Config.getInstance().getLevelBasedTime() > 0) {
                    if (currentTime > Math.min(tombBlock.getTime() + tombBlock.getOwnerLevel() * Config.getInstance().getLevelBasedTime(), tombBlock.getTime() + Config.getInstance().getTombRemoveTime())) {
                        TombstoneManager.destroyTombstone(tombBlock);
                        iter.remove();
                    }
                }
                else {
                    if (currentTime > (tombBlock.getTime() + Config.getInstance().getTombRemoveTime())) {
                        TombstoneManager.destroyTombstone(tombBlock);
                        iter.remove();
                    }
                }
            }
        }
    }
}
