package com.me.tft_02.ghosts.managers.player;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.datatypes.TombBlock;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.managers.TombstoneManager;
import com.me.tft_02.ghosts.util.Misc;

public class PlayerManager {

    private static HashMap<String, Integer> lastSpook = new HashMap<String, Integer>();

    public static boolean resurrect(OfflinePlayer offlinePlayer) {
        if (!DatabaseManager.ghosts.remove(offlinePlayer.getName())) {
            return false;
        }

        if (offlinePlayer.isOnline()) {
            offlinePlayer.getPlayer().sendMessage(LocaleLoader.getString("Commands.Resurrect"));
        }
        return true;
    }

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

    public static void spook(Player player) {
        int cooldown = 60;
        long deactivatedTimeStamp = 0;
        if (lastSpook.containsKey(player.getName())) {
            deactivatedTimeStamp = lastSpook.get(player.getName());
        }

        if (Misc.cooldownOver(deactivatedTimeStamp, cooldown)) {
            player.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 1F, 1F);
            lastSpook.put(player.getName(), (int) (System.currentTimeMillis() / Misc.TIME_CONVERSION_FACTOR));
        }
    }

    public static void applyPotionEffects(Player player) {
//        Collection<PotionEffect> potionEffects = player.getActivePotionEffects();
//
//        PotionEffect potionEffect = new PotionEffect(PotionEffectType.SPEED, duration, amplifier, ambient);
//        player.addPotionEffect(potionEffect);
//
//        player.addPotionEffects(potionEffects);
    }
}
