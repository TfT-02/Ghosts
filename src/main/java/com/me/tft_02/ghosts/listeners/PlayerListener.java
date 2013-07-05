package com.me.tft_02.ghosts.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.datatypes.TombBlock;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.util.Misc;
import com.me.tft_02.ghosts.util.Permissions;
import com.me.tft_02.ghosts.util.TombstoneManager;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (Ghosts.p.ghostManager.isGhost(player)) {
            if (DatabaseManager.playerRespawns.containsKey(player.getName())) {
                Location location = DatabaseManager.getLastDeathLocation(player);
                if (location != null) {
                    Location respawnLocation = Misc.getRandomLocation(location, Config.getInstance().getMinimumRange(), Config.getInstance().getMaximumRange());
                    Ghosts.p.debug(player.getName() + " died at " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
                    Ghosts.p.debug(player.getName() + " will respawn at " + respawnLocation.getBlockX() + " " + respawnLocation.getBlockY() + " " + respawnLocation.getBlockZ());

                    player.sendMessage(LocaleLoader.getString("Ghost.Respawn"));
                    event.setRespawnLocation(respawnLocation);
                    DatabaseManager.playerRespawns.put(player.getName(), false);
                }
            }
            else {
                Ghosts.p.ghostManager.setGhost(player, false);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Ghosts.p.ghostManager.addPlayer(player);

        if (Ghosts.p.ghostManager.isGhost(player)) {
            Ghosts.p.ghostManager.setGhost(player, false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();

        if (Ghosts.p.ghostManager.isGhost(player)) {
            item.setPickupDelay(60);
            event.setCancelled(true);
        }
    }

    /**
     * Monitor PlayerInteractEntityEvent events.
     * 
     * @param event The event to watch
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (entity instanceof Player) {
            Player target = (Player) entity;

            if (Ghosts.p.ghostManager.isGhost(player) || Ghosts.p.ghostManager.isGhost(target)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block.getType() != Material.CHEST) {
            return;
        }

        // We'll do quickloot on rightclick of chest if we're going to destroy it anyways
        //        if (!Ghosts.p.destroyQuickLoot || !Ghosts.p.noDestroy) {
        //            System.out.println("L88");
        //            return;
        //        }

        Player player = event.getPlayer();

        if (!Permissions.quickLoot(player)) {
            Ghosts.p.debug(player.getName() + " has no permission to quickloot");
            return;
        }

        TombBlock tBlock = DatabaseManager.tombBlockList.get(block.getLocation());
        if (tBlock == null || !(tBlock.getBlock().getState() instanceof Chest)) {
            return;
        }

        if (!tBlock.getOwner().equals(player.getName())) {
            Ghosts.p.debug(player.getName() + " is not the owner!");
            return;
        }

        // I think quickloot is succes here
        Ghosts.p.ghostManager.setGhost(player, false);

        Chest sChest = (Chest) tBlock.getBlock().getState();
        Chest lChest = (tBlock.getLargeBlock() != null) ? (Chest) tBlock.getLargeBlock().getState() : null;

        ItemStack[] items = sChest.getInventory().getContents();
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
            sChest.getInventory().clear(cSlot);
        }
        if (lChest != null) {
            items = lChest.getInventory().getContents();
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
                lChest.getInventory().clear(cSlot);
            }
        }

        if (!overflow) {
            // We're quicklooting, so no need to resume this interaction
            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY); //TODO: Minor bug here - if you're holding a sign, it'll still pop up
            event.setCancelled(true);

            if (Config.getInstance().getDestroyQuickloot()) {
                TombstoneManager.destroyTomb(tBlock);
            }
        }

        // Manually update inventory for the time being.
        player.updateInventory();
    }
}
