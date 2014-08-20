package com.me.tft_02.ghosts.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.me.tft_02.ghosts.datatypes.player.GhostPlayer;
import com.me.tft_02.ghosts.locale.LocaleLoader;

public class ItemUtils {

    public static boolean isGhostsItem(ItemStack item) {
        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();
        return itemMeta.hasLore() && itemMeta.getLore().contains("Ghosts Item");
    }

    public static boolean isResurrectionScroll(ItemStack item) {
        if (!isGhostsItem(item)) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();
        return itemMeta.hasDisplayName() && itemMeta.getDisplayName().equals(ChatColor.GOLD + LocaleLoader.getString("Item.ResurrectionScroll.Name"));
    }

    public static List<ItemStack> saveGhostItems(GhostPlayer ghostPlayer, List<ItemStack> drops) {
        List<ItemStack> ghostItems = new ArrayList<ItemStack>();
        List<ItemStack> remainingDrops = new ArrayList<ItemStack>();

        for (ItemStack itemStack : drops) {
            if (isResurrectionScroll(itemStack)) {
                ghostItems.add(itemStack);
                continue;
            }

            remainingDrops.add(itemStack);
        }

        if (!ghostItems.isEmpty()) {
            ghostPlayer.setPlayerGhostItems(ghostItems);
        }

        return remainingDrops;
    }
}
