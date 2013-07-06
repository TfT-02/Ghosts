package com.me.tft_02.ghosts.runnables;

import java.util.Iterator;

import org.bukkit.block.Chest;
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
        long cTime = System.currentTimeMillis() / Misc.TIME_CONVERSION_FACTOR;
        for (Iterator<TombBlock> iter = DatabaseManager.tombList.iterator(); iter.hasNext();) {
            TombBlock tBlock = iter.next();

            //"empty" option checks
            if (Config.getInstance().getKeepUntilEmpty() || Config.getInstance().getRemoveWhenEmpty()) {
                if (tBlock.getBlock().getState() instanceof Chest) {
                    boolean isEmpty = true;

                    Chest sChest = (Chest) tBlock.getBlock().getState();
                    Chest lChest = (tBlock.getLargeBlock() != null) ? (Chest) tBlock.getLargeBlock().getState() : null;

                    for (ItemStack item : sChest.getInventory().getContents()) {
                        if (item != null) {
                            isEmpty = false;
                        }
                        break;
                    }
                    if (lChest != null && !isEmpty) {
                        for (ItemStack item : lChest.getInventory().getContents()) {
                            if (item != null) {
                                isEmpty = false;
                            }
                            break;
                        }
                    }
                    if (Config.getInstance().getKeepUntilEmpty()) {
                        if (!isEmpty) {
                            continue;
                        }
                    }
                    if (Config.getInstance().getRemoveWhenEmpty()) {
                        if (isEmpty) {
                            TombstoneManager.destroyTomb(tBlock);
                            iter.remove();
                        }
                    }
                }
            }

            //Block removal check
            if (Config.getInstance().getTombRemoveTime() > 0) {
                if (Config.getInstance().getLevelBasedTime() > 0) {
                    if (cTime > Math.min(tBlock.getTime() + tBlock.getOwnerLevel() * Config.getInstance().getLevelBasedTime(), tBlock.getTime() + Config.getInstance().getTombRemoveTime())) {
                        TombstoneManager.destroyTomb(tBlock);
                        iter.remove();
                    }
                }
                else {
                    if (cTime > (tBlock.getTime() + Config.getInstance().getTombRemoveTime())) {
                        TombstoneManager.destroyTomb(tBlock);
                        iter.remove();
                    }
                }
            }
        }
    }

}
