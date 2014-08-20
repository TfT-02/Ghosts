package com.me.tft_02.ghosts.listeners;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.TombstoneDatabase;
import com.me.tft_02.ghosts.datatypes.TombBlock;
import com.me.tft_02.ghosts.datatypes.player.GhostPlayer;
import com.me.tft_02.ghosts.items.ResurrectionScroll;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.managers.player.GhostManager;
import com.me.tft_02.ghosts.managers.player.PlayerManager;
import com.me.tft_02.ghosts.runnables.ghosts.ExplosionTrailTask;
import com.me.tft_02.ghosts.runnables.ghosts.GroundExplosionTask;
import com.me.tft_02.ghosts.runnables.ghosts.IgniteTask;
import com.me.tft_02.ghosts.runnables.player.PlayerProfileLoadingTask;
import com.me.tft_02.ghosts.util.BlockUtils;
import com.me.tft_02.ghosts.util.Misc;
import com.me.tft_02.ghosts.util.Permissions;
import com.me.tft_02.ghosts.util.player.UserManager;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
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
        //            return;
        //        }

        Player player = event.getPlayer();

        if (!Permissions.quickLoot(player)) {
            Ghosts.p.debug(player.getName() + " has no permission to quickloot");
            return;
        }

        TombBlock tombBlock = TombstoneDatabase.tombBlockList.get(block.getLocation());
        if (tombBlock == null || !(tombBlock.getBlock().getState() instanceof Chest)) {
            return;
        }

        if (!tombBlock.getOwnerUniqueId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            Ghosts.p.debug(player.getName() + " is not the owner!");
            return;
        }

        // I think quickloot is succes here
        event.setCancelled(false); //TODO Add config option; override ?
        Ghosts.p.getGhostManager().setGhost(player, false);
        PlayerManager.quickLoot(event, player, tombBlock);
    }

    /**
     * Monitor PlayerInteractEvents.
     *
     * @param event The event to monitor
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractMonitor(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                Block block = event.getClickedBlock();
                BlockState blockState = block.getState();

                /* ACTIVATION & ITEM CHECKS */
                if (BlockUtils.canActivateAbilities(blockState)) {
                    ResurrectionScroll.activationCheck(player);
                }

                break;

            case RIGHT_CLICK_AIR:
                /* ITEM CHECKS */
                ResurrectionScroll.activationCheck(player);

                break;

            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:

                break;

            default:
                break;
        }
    }

    /**
     * Watch PlayerInteractEntityEvent events.
     * 
     * @param event The event to watch
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (entity instanceof Player) {
            Player target = (Player) entity;

            boolean resurrectionScroll = ResurrectionScroll.activationCheck(player, target);
            GhostManager ghostManager = Ghosts.p.getGhostManager();

            if (!resurrectionScroll && (ghostManager.isGhost(player) || ghostManager.isGhost(target))) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Monitor PlayerJoinEvents.
     * <p>
     * These events are monitored for the purpose of initializing player
     * variables, as well as handling the MOTD display and other important
     * join messages.
     *
     * @param event The event to monitor
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Ghosts.p.getGhostManager().addPlayer(player);

        //        if (Ghosts.p.getGhostManager().isGhost(player)) {
        //            PlayerManager.enableDoubleJump(player);
        //        }

        if (Misc.isNPCEntity(player)) {
            return;
        }

        // 1 Tick delay to ensure the player is marked as online before we begin loading
        new PlayerProfileLoadingTask(player).runTaskLaterAsynchronously(Ghosts.p, 1);

        if (Permissions.updateNotifications(player) && Ghosts.p.isUpdateAvailable()) {
            player.sendMessage(LocaleLoader.getString("UpdateChecker.Outdated"));
            player.sendMessage(LocaleLoader.getString("UpdateChecker.New_Available"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();

        if (Ghosts.p.getGhostManager().isGhost(player)) {
            item.setPickupDelay(60);
            event.setCancelled(true);
        }
    }

    /**
     * Monitor PlayerQuitEvents.
     * <p>
     * These events are monitored for the purpose of resetting player
     * variables and other garbage collection tasks that must take place when
     * a player exits the server.
     *
     * @param event The event to monitor
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Ghosts.p.getGhostManager().removePlayer(player);

        if (!UserManager.hasPlayerDataKey(player)) {
            return;
        }

        GhostPlayer ghostPlayer = UserManager.getPlayer(player);
        ghostPlayer.getProfile().scheduleAsyncSave();
        UserManager.remove(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!UserManager.hasPlayerDataKey(player)) {
            return;
        }

        GhostPlayer ghostPlayer = UserManager.getPlayer(player);

        if (!ghostPlayer.getRespawn()) {
            return;
        }

        if (!Ghosts.p.getGhostManager().isGhost(player)) {
            Ghosts.p.getGhostManager().setGhost(player, false);
            //            PlayerManager.disableDoubleJump(player);
            return;
        }

        Location location = ghostPlayer.getLastDeathLocation();
        if (location == null) {
            return;
        }

        Location respawnLocation = Misc.getRandomLocation(location, Config.getInstance().getMinimumRange(), Config.getInstance().getMaximumRange());
        Ghosts.p.debug(player.getName() + " died at " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
        Ghosts.p.debug(player.getName() + " will respawn at " + respawnLocation.getBlockX() + " " + respawnLocation.getBlockY() + " " + respawnLocation.getBlockZ());

        player.sendMessage(LocaleLoader.getString("Ghost.Respawn"));
        event.setRespawnLocation(respawnLocation);
        ghostPlayer.setRespawn(false);

        // Restore saved ghost items
        List<ItemStack> playerGhostItems = ghostPlayer.getPlayerGhostItems();
        if (!playerGhostItems.isEmpty()) {
            for (ItemStack itemStack : playerGhostItems) {
                player.getInventory().addItem(itemStack);
            }
            playerGhostItems.clear();
        }

        // Restore saved remaining vanilla XP
        PlayerManager.recoverRemainingXP(player);

        //        PlayerManager.enableDoubleJump(player);

        if (Config.getInstance().getThunder()) {
            player.getWorld().playSound(new Location(player.getWorld(), respawnLocation.getX(), 100, respawnLocation.getZ()), Sound.AMBIENCE_THUNDER, 1F, 1F);
        }

        if (!Config.getInstance().getRespawnFromSky()) {
            return;
        }

        if (Config.getInstance().getSetOnFire()) {
            new IgniteTask(player, 12 * 20).runTask(Ghosts.p);
        }

        if (Config.getInstance().getExplosionTrail()) {
            new ExplosionTrailTask(player, 20).runTaskTimer(Ghosts.p, 20, 5);
        }

        if (Config.getInstance().getExplosionImpact()) {
            new GroundExplosionTask(player).runTaskTimer(Ghosts.p, 2 * 20, 3);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (Ghosts.p.getGhostManager().isGhost(player)) {
            PlayerManager.spook(player);
        }
    }

    //    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    //    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
    //        PlayerManager.doubleJump(event);
    //    }

    //    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    //    public void onPlayerMove(PlayerMoveEvent event) {
    //        Location from = event.getFrom();
    //        Location to = event.getTo();
    //
    //        int fromX = (int) from.getX();
    //        int fromY = (int) from.getY();
    //        int fromZ = (int) from.getZ();
    //        int toX = (int) to.getX();
    //        int toY = (int) to.getY();
    //        int toZ = (int) to.getZ();
    //
    //        if (fromX == toX && fromZ == toZ && fromY == toY) {
    //            return;
    //        }
    //
    //        if (!Config.getInstance().getGhostJumpEnabled()) {
    //            return;
    //        }
    //
    //        PlayerManager.move(event);
    //    }
}
