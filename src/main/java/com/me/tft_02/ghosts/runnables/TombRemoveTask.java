package com.me.tft_02.ghosts.runnables;

import java.util.Iterator;

import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.datatypes.TombBlock;
import com.me.tft_02.ghosts.util.Misc;
import com.me.tft_02.ghosts.util.TombstoneManager;

public class TombRemoveTask extends BukkitRunnable {

    @Override
    public void run() {
        long cTime = System.currentTimeMillis() / Misc.TIME_CONVERSION_FACTOR;
        for (Iterator<TombBlock> iter = DatabaseManager.tombList.iterator(); iter.hasNext();) {
            TombBlock tBlock = iter.next();

            //"empty" option checks
            if (Config.getInstance().keepUntilEmpty || Config.getInstance().removeWhenEmpty) {
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
                            if (item != null)
                                isEmpty = false;
                            break;
                        }
                    }
                    if (Config.getInstance().keepUntilEmpty) {
                        if (!isEmpty)
                            continue;
                    }
                    if (Config.getInstance().removeWhenEmpty) {
                        if (isEmpty) {
                            TombstoneManager.destroyTomb(tBlock);
                            iter.remove();
                        }
                    }
                }
            }

            //Block removal check
            if (Config.getInstance().cenotaphRemove) {
                if (Config.getInstance().levelBasedRemoval) {
                    if (cTime > Math.min(tBlock.getTime() + tBlock.getOwnerLevel() * Config.getInstance().levelBasedTime, tBlock.getTime() + Config.getInstance().removeTime)) {
                        TombstoneManager.destroyTomb(tBlock);
                        iter.remove();
                    }
                }
                else {
                    if (cTime > (tBlock.getTime() + Config.getInstance().removeTime)) {
                        TombstoneManager.destroyTomb(tBlock);
                        iter.remove();
                    }
                }
            }
        }
    }

}
