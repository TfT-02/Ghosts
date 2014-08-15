package com.me.tft_02.ghosts.listeners;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.runnables.player.UpdateInventoryTask;
import com.me.tft_02.ghosts.util.ItemUtils;

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
}
