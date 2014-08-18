package com.me.tft_02.ghosts.listeners;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.items.ResurrectionScroll;
import com.me.tft_02.ghosts.items.ResurrectionScroll.Tier;
import com.me.tft_02.ghosts.runnables.player.UpdateInventoryTask;
import com.me.tft_02.ghosts.util.ItemUtils;
import com.me.tft_02.ghosts.util.RecipeUtil;

public class InventoryListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        final HumanEntity whoClicked = event.getWhoClicked();

//        if (!whoClicked.hasMetadata(Ghosts.playerDataKey)) {
//            return;
//        }

        ItemStack result = event.getRecipe().getResult();

        if (!ItemUtils.isGhostsItem(result)) {
            return;
        }

        new UpdateInventoryTask((Player) whoClicked).runTaskLater(Ghosts.p, 0);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        boolean isUpgradeRecipe = RecipeUtil.areEqual(event.getRecipe(), ResurrectionScroll.getResurrectionScrollUpgradeRecipe());

        if (!isUpgradeRecipe) {
            return;
        }

        CraftingInventory inventory = event.getInventory();
        boolean resurrectionScrollFound = false;
        int tier = 0;

        for (ItemStack itemStack : inventory.getMatrix()) {
            if (itemStack == null) {
                continue;
            }

            if (ItemUtils.isResurrectionScroll(itemStack)) {
                resurrectionScrollFound = true;
                tier = Tier.getTier(itemStack).toNumerical();
            }
        }

        if (!resurrectionScrollFound || tier >= 3) {
            inventory.setResult(null);
            return;
        }

        inventory.setResult(ResurrectionScroll.getResurrectionScroll(1, tier + 1));
    }
}
