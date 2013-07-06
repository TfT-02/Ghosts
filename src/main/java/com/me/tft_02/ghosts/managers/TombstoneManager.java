package com.me.tft_02.ghosts.managers;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.datatypes.TombBlock;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.util.BlockUtils;
import com.me.tft_02.ghosts.util.Misc;
import com.me.tft_02.ghosts.util.Permissions;

public class TombstoneManager {

    public static boolean createTombstone(PlayerDeathEvent event) {
        // Get the current player location.
        Player player = event.getEntity();
        Location location = player.getLocation();
        Block block = player.getWorld().getBlockAt(location);

        // If we run into something we don't want to destroy, go one up.
        if (BlockUtils.cannotBeReplaced(block.getState())) {
            block = block.getRelative(BlockFace.UP);
        }

        //Don't create the chest if it or its sign would be in the void
        //TODO Why would anyone disable this?
        if (Config.getInstance().getVoidCheck() && ((block.getY() > player.getWorld().getMaxHeight() - 1) || (block.getY() > player.getWorld().getMaxHeight()) || player.getLocation().getY() < 1)) {
            Ghosts.p.debug("Chest would be in the Void. Inv dropped.");
            return false;
        }

        // Check if the player has a chest.
        int playerChestCount = 0;
        int playerSignCount = 0;
        for (ItemStack item : event.getDrops()) {
            if (item == null) {
                continue;
            }
            if (item.getType() == Material.CHEST) {
                playerChestCount += item.getAmount();
            }
            if (item.getType() == Material.SIGN) {
                playerSignCount += item.getAmount();
            }
        }

        if (playerChestCount == 0 && !Permissions.freechest(player)) {
            Ghosts.p.debug("No chest! Inv dropped.");
            return false;
        }

        // Check if we can replace the block.
        block = BlockUtils.findPlace(block, false);
        if (block == null) {
            Ghosts.p.debug("No room to place chest. Inv dropped.");
            return false;
        }

        // Check if there is a nearby chest
        //TODO Why would anyone disable this?
        if (Config.getInstance().getNoInterfere() && BlockUtils.checkChest(block, Material.CHEST)) {
            Ghosts.p.debug("Existing chest interfering with chest placement. Inv dropped.");
            return false;
        }

        int removeChestCount = 1;
        int removeSignCount = 0;

        // Do the check for a large chest block here so we can check for interference
        Block lBlock = BlockUtils.findLarge(block);

        // Set the current block to a chest, init some variables for later use.
        block.setType(Material.CHEST);
        // We're running into issues with 1.3 where we can't cast to a Chest :(
        BlockState state = block.getState();
        if (!(state instanceof Chest)) {
            Ghosts.p.debug("Could not access chest. Inv dropped.");
            return false;
        }
        Chest sChest = (Chest) state;
        Chest largeChest = null;
        int slot = 0;
        int maxSlot = sChest.getInventory().getSize();

        // Check if they need a large chest.
        if (event.getDrops().size() > maxSlot) {
            // If they are allowed spawn a large chest to catch their entire inventory.
            if (lBlock != null && Permissions.largechest(player)) {
                removeChestCount = 2;
                // Check if the player has enough chests
                if (playerChestCount >= removeChestCount || Permissions.freechest(player)) {
                    lBlock.setType(Material.CHEST);
                    largeChest = (Chest) lBlock.getState();
                    maxSlot = maxSlot * 2;
                }
                else {
                    removeChestCount = 1;
                }
            }
        }

        // Don't remove any chests if they get a free one.
        if (Permissions.freechest(player)) {
            removeChestCount = 0;
        }

        // Check if we have signs enabled, if the player can use signs, and if the player has a sign or gets a free sign
        Block signBlock = null;
        if (Config.getInstance().getUseTombstoneSign() && Permissions.sign(player) && (playerSignCount > 0 || Permissions.freesign(player))) {
            // Find a place to put the sign, then place the sign.
            signBlock = sChest.getWorld().getBlockAt(sChest.getX(), sChest.getY() + 1, sChest.getZ());
            if (BlockUtils.canBeReplaced(signBlock.getState())) {
                BlockUtils.createSign(signBlock, player);
                removeSignCount += 1;
            }
            else if (largeChest != null) {
                signBlock = largeChest.getWorld().getBlockAt(largeChest.getX(), largeChest.getY() + 1, largeChest.getZ());
                if (BlockUtils.canBeReplaced(signBlock.getState())) {
                    BlockUtils.createSign(signBlock, player);
                    removeSignCount += 1;
                }
            }
        }
        // Don't remove a sign if they get a free one
        if (Permissions.freesign(player))
            removeSignCount -= 1;

        // Create a TombBlock for this tombstone
        TombBlock tombBlock = new TombBlock(sChest.getBlock(), (largeChest != null) ? largeChest.getBlock() : null, signBlock, player.getName(), player.getLevel() + 1, (System.currentTimeMillis() / 1000));

        // Add tombstone to list
        DatabaseManager.tombList.offer(tombBlock);

        // Add tombstone blocks to tombBlockList
        DatabaseManager.tombBlockList.put(tombBlock.getBlock().getLocation(), tombBlock);
        if (tombBlock.getLargeBlock() != null)
            DatabaseManager.tombBlockList.put(tombBlock.getLargeBlock().getLocation(), tombBlock);
        if (tombBlock.getSign() != null)
            DatabaseManager.tombBlockList.put(tombBlock.getSign().getLocation(), tombBlock);

        // Add tombstone to player lookup list
        ArrayList<TombBlock> pList = DatabaseManager.playerTombList.get(player.getName());
        if (pList == null) {
            pList = new ArrayList<TombBlock>();
            DatabaseManager.playerTombList.put(player.getName(), pList);
        }
        pList.add(tombBlock);

        DatabaseManager.saveTombList(player.getWorld().getName());

        // Next get the players inventory using the getDrops() method.
        for (Iterator<ItemStack> iter = event.getDrops().listIterator(); iter.hasNext();) {
            ItemStack item = iter.next();
            if (item == null)
                continue;
            // Take the chest(s)
            if (removeChestCount > 0 && item.getType() == Material.CHEST) {
                if (item.getAmount() >= removeChestCount) {
                    item.setAmount(item.getAmount() - removeChestCount);
                    removeChestCount = 0;
                }
                else {
                    removeChestCount -= item.getAmount();
                    item.setAmount(0);
                }
                if (item.getAmount() == 0) {
                    iter.remove();
                    continue;
                }
            }

            // Take a sign
            if (removeSignCount > 0 && item.getType() == Material.SIGN) {
                item.setAmount(item.getAmount() - 1);
                removeSignCount -= 1;
                if (item.getAmount() == 0) {
                    iter.remove();
                    continue;
                }
            }

            // Add items to chest if not full.
            if (slot < maxSlot) {
                if (slot >= sChest.getInventory().getSize()) {
                    if (largeChest == null)
                        continue;
                    largeChest.getInventory().setItem(slot % sChest.getInventory().getSize(), item);
                }
                else {
                    sChest.getInventory().setItem(slot, item);
                }
                iter.remove();
                slot++;
            }
            else if (removeChestCount == 0)
                break;
        }

        sendNotificationMessages(player, event);
        return true;
    }

    private static void sendNotificationMessages(Player player, EntityDeathEvent event) {
        player.sendMessage(LocaleLoader.getString("Tombstone.Inventory_Stored"));

        if (event.getDrops().size() > 0) {
            player.sendMessage(LocaleLoader.getString("Tombstone.Inventory_Overflow", event.getDrops().size()));
        }

        int breakTime = (Config.getInstance().levelBasedRemoval ? Math.min(player.getLevel() + 1 * Config.getInstance().levelBasedTime, Config.getInstance().getTombRemoveTime()) : Config.getInstance().getTombRemoveTime());
        if (!Config.getInstance().keepUntilEmpty || Config.getInstance().getTombRemoveTime() > 0) {
            player.sendMessage(LocaleLoader.getString("Tombstone.Time", Misc.convertTime(breakTime)));
        }
    }

    public void destroyTombstone(Location location) {
        destroyTomb(DatabaseManager.tombBlockList.get(location));
    }

    public static void destroyTomb(TombBlock tombBlock) {
        Block block = tombBlock.getBlock();

        if (!block.getChunk().load()) {
            Ghosts.p.getLogger().severe("Error loading world chunk trying to remove tombstone at " + block.getX() + "," + block.getY() + "," + block.getZ() + " owned by " + tombBlock.getOwner() + ".");
            return;
        }

        block.setType(Material.AIR);

        if (tombBlock.getLargeBlock() != null) {
            tombBlock.getLargeBlock().setType(Material.AIR);
        }

        removeTomb(tombBlock, true);

        Player player = Ghosts.p.getServer().getPlayer(tombBlock.getOwner());
        if (player != null) {
            player.sendMessage(LocaleLoader.getString("Tombstone.Broken"));
        }
    }

    public static void removeTomb(TombBlock tombBlock, boolean removeList) {
        if (tombBlock == null) {
            return;
        }

        DatabaseManager.tombBlockList.remove(tombBlock.getBlock().getLocation());
        if (tombBlock.getLargeBlock() != null) {
            DatabaseManager.tombBlockList.remove(tombBlock.getLargeBlock().getLocation());
        }

        // Remove just this tomb from tombList
        ArrayList<TombBlock> tList = DatabaseManager.playerTombList.get(tombBlock.getOwner());
        if (tList != null) {
            tList.remove(tombBlock);
            if (tList.size() == 0) {
                DatabaseManager.playerTombList.remove(tombBlock.getOwner());
            }
        }

        if (removeList) {
            DatabaseManager.tombList.remove(tombBlock);
        }

        if (tombBlock.getBlock() != null) {
            DatabaseManager.saveTombList(tombBlock.getBlock().getWorld().getName());
        }
    }
}