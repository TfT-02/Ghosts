package com.me.tft_02.ghosts.managers.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.datatypes.TombBlock;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.managers.TombstoneManager;
import com.me.tft_02.ghosts.util.Misc;
import com.me.tft_02.ghosts.util.Permissions;

public class PlayerManager {

    private static List<String> hasJumped = new ArrayList<String>();
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

    public static void doubleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (!Permissions.doubleJump(player)) {
            Ghosts.p.debug("No permission");
            return;
        }

        if (!Ghosts.p.ghostManager.isGhost(player)) {
            return;
        }

        String name = player.getName();
        World world = player.getWorld();

        boolean wallJump = true;
        int blocks = 5;

        Vector jump = player.getVelocity().multiply(1).setY(0.17 * blocks);
        Vector look = player.getLocation().getDirection().multiply(0.5);

        if (!event.isFlying() || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            Ghosts.p.debug("Flying and or in creative");
            return;
        }

        if (wallJump) {
            Block block = player.getTargetBlock(null, 2);

            if (block.getType() == Material.AIR) {
                setFlight(player, false);
                return;
            }
        }

        if (hasJumped.contains(name)) {
            setFlight(player, false);
            return;
        }

        player.setVelocity(jump.add(look));

        if (!hasJumped.contains(player.getName())) {
            hasJumped.add(player.getName());
        }

        setFlight(player, false);
        event.setCancelled(true);

        if (Config.getInstance().getGhostJumpSound()) {
            player.playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 10, -10);
        }

        if (Config.getInstance().getGhostJumpEffect()) {
            for (int i = 0; i <= 10; i++) {
                world.playEffect(player.getLocation(), Effect.SMOKE, i);
            }
        }
    }

    public static void move(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!Permissions.doubleJump(player)) {
            setFlight(player, false);
            return;
        }

        Location location = player.getLocation();
        Block block = location.getBlock().getRelative(BlockFace.DOWN);

        if (block.getType() != Material.AIR) {
            enableDoubleJump(player);
        }
    }

    private static void setFlight(Player player, boolean state) {
        player.setFlying(state);
        player.setAllowFlight(state);
    }

    public static void enableDoubleJump(Player player) {
        if (hasJumped.contains(player.getName())) {
            hasJumped.remove(player.getName());
        }

        player.setAllowFlight(true);
        player.setFlying(false);
    }

    public static void disableDoubleJump(Player player) {
        setFlight(player, false);
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
}
