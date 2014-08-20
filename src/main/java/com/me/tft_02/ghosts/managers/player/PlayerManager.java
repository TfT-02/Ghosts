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

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.datatypes.RecoveryType;
import com.me.tft_02.ghosts.datatypes.StatsType;
import com.me.tft_02.ghosts.datatypes.TombBlock;
import com.me.tft_02.ghosts.datatypes.player.GhostPlayer;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.managers.TombstoneManager;
import com.me.tft_02.ghosts.runnables.ghosts.SetXPTask;
import com.me.tft_02.ghosts.runnables.player.UpdateInventoryTask;
import com.me.tft_02.ghosts.util.ExperienceManager;
import com.me.tft_02.ghosts.util.Misc;
import com.me.tft_02.ghosts.util.player.UserManager;

public class PlayerManager {

    private static HashMap<String, Integer> lastSpook = new HashMap<String, Integer>();

    public static boolean resurrect(OfflinePlayer offlinePlayer) {
        return resurrect(offlinePlayer, "");
    }

    public static boolean resurrect(OfflinePlayer offlinePlayer, String notification) {
        boolean success = GhostManager.ghosts.remove(offlinePlayer.getUniqueId());

        if (success && offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            player.sendMessage(LocaleLoader.getString("Commands.Resurrect") + " " + notification);
            player.setFireTicks(0);
        }

        return success;
    }

    public static void quickLoot(PlayerInteractEvent event, Player player, TombBlock tombBlock) {
        Chest smallChest = (Chest) tombBlock.getBlock().getState();
        Chest largeChest = (tombBlock.getLargeBlock() != null) ? (Chest) tombBlock.getLargeBlock().getState() : null;
        ItemStack[] items = smallChest.getInventory().getContents();
        boolean overflow = false;

        for (int cSlot = 0; cSlot < items.length; cSlot++) {
            ItemStack item = items[cSlot];
            if (item == null || item.getType() == Material.AIR) {
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
                if (item == null || item.getType() == Material.AIR) {
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
                TombstoneManager.destroyTombstone(tombBlock, false);
            }
        }

        // Recover saved experience
        PlayerManager.recoverLostXP(player, Config.getInstance().getRecoveryVanillaXP(RecoveryType.FIND_TOMB));

        // Manually update inventory for the time being.
        new UpdateInventoryTask(player).runTask(Ghosts.p);

        UserManager.getPlayer(player).increaseStats(StatsType.FIND_TOMB);
    }

    public static void loseAndSaveXP(Player player) {
        ExperienceManager manager = new ExperienceManager(player);
        int currentExp = manager.getCurrentExp();
        double percentageLost = Config.getInstance().getLossesVanillaXP();
        int lostExperience = (int) Math.floor((currentExp * percentageLost * 0.01D));
        int remainingExp = currentExp - lostExperience;

        GhostPlayer ghostPlayer = UserManager.getPlayer(player);
        ghostPlayer.setSavedLostVanillaXP(lostExperience);
        ghostPlayer.setSavedRemainingVanillaXP(remainingExp);

        if (lostExperience > 0) {
            player.sendMessage(LocaleLoader.getString("Player.Death.VanillaXPLost", percentageLost));
        }
    }

    public static void recoverRemainingXP(Player player) {
        GhostPlayer ghostPlayer = UserManager.getPlayer(player);

        int remainingExp = ghostPlayer.getSavedRemainingVanillaXP();
        new SetXPTask(player, remainingExp).runTaskLater(Ghosts.p, 1);
        ghostPlayer.setSavedRemainingVanillaXP(0);
    }

    public static void recoverLostXP(Player player, double percentage) {
        GhostPlayer ghostPlayer = UserManager.getPlayer(player);
        ExperienceManager manager = new ExperienceManager(player);
        int savedVanillaXP = ghostPlayer.getSavedLostVanillaXP();
        int recoveredXP =  (int) Math.floor(savedVanillaXP * (percentage * 0.01D));

        manager.changeExp(recoveredXP);
        ghostPlayer.setSavedLostVanillaXP(0);

        if (recoveredXP > 0) {
            player.sendMessage(LocaleLoader.getString("Player.Death.VanillaXPRecover", percentage));
        }
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
