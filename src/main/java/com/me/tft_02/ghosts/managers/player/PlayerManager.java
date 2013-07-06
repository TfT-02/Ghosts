package com.me.tft_02.ghosts.managers.player;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.datatypes.TombBlock;
import com.me.tft_02.ghosts.managers.TombstoneManager;

public class PlayerManager {

    public static void quickLoot(PlayerInteractEvent event, Player player, TombBlock tombBlock) {
        Chest smallChest = (Chest) tombBlock.getBlock().getState();
        Chest largeChest = (tombBlock.getLargeBlock() != null) ? (Chest) tombBlock.getLargeBlock().getState() : null;
        ItemStack[] items = smallChest.getInventory().getContents();
        boolean overflow = false;

        for (int cSlot = 0; cSlot < items.length; cSlot++) {
            ItemStack item = items[cSlot];
            if (item == null) {
                continue;
            }
            if (item.getType() == Material.AIR) {
                continue;
            }
            int slot = player.getInventory().firstEmpty();
            if (slot == -1) {
                overflow = true;
                break;
            }
            player.getInventory().setItem(slot, item);
            smallChest.getInventory().clear(cSlot);
        }
        if (largeChest != null) {
            items = largeChest.getInventory().getContents();
            for (int cSlot = 0; cSlot < items.length; cSlot++) {
                ItemStack item = items[cSlot];
                if (item == null) {
                    continue;
                }
                if (item.getType() == Material.AIR) {
                    continue;
                }
                int slot = player.getInventory().firstEmpty();
                if (slot == -1) {
                    overflow = true;
                    break;
                }
                player.getInventory().setItem(slot, item);
                largeChest.getInventory().clear(cSlot);
            }
        }

        if (!overflow) {
            // We're quicklooting, so no need to resume this interaction
            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY); //TODO: Minor bug here - if you're holding a sign, it'll still pop up
            event.setCancelled(true);

            if (Config.getInstance().getDestroyQuickloot()) {
                TombstoneManager.destroyTombstone(tombBlock);
            }
        }

        // Manually update inventory for the time being.
        player.updateInventory();
    }
}
